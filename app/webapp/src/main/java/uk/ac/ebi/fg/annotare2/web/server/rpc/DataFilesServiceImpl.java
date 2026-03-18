/*
 * Copyright 2009-2016 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.fg.annotare2.web.server.rpc;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.common.util.concurrent.Striped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.annotare2.core.AccessControlException;
import uk.ac.ebi.fg.annotare2.core.UnexpectedException;
import uk.ac.ebi.fg.annotare2.core.files.DataFileHandle;
import uk.ac.ebi.fg.annotare2.core.files.LocalFileHandle;
import uk.ac.ebi.fg.annotare2.core.properties.AnnotareProperties;
import uk.ac.ebi.fg.annotare2.core.transaction.Transactional;
import uk.ac.ebi.fg.annotare2.core.utils.LinuxShellCommandExecutor;
import uk.ac.ebi.fg.annotare2.core.utils.URIEncoderDecoder;
import uk.ac.ebi.fg.annotare2.db.dao.RecordNotFoundException;
import uk.ac.ebi.fg.annotare2.db.model.DataFile;
import uk.ac.ebi.fg.annotare2.db.model.ExperimentSubmission;
import uk.ac.ebi.fg.annotare2.db.model.Submission;
import uk.ac.ebi.fg.annotare2.db.model.enums.Permission;
import uk.ac.ebi.fg.annotare2.submission.model.ExperimentProfile;
import uk.ac.ebi.fg.annotare2.submission.model.FileRef;
import uk.ac.ebi.fg.annotare2.submission.transform.DataSerializationException;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.DataFilesService;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.NoPermissionException;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.OperationFailedException;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.ResourceNotFoundException;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.exepriment.DataFileRow;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.exepriment.FtpFileInfo;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.exepriment.UploadedFileInfo;
import uk.ac.ebi.fg.annotare2.web.server.services.AccountService;
import uk.ac.ebi.fg.annotare2.web.server.services.DataFileManagerImpl;
import uk.ac.ebi.fg.annotare2.web.server.services.MessengerImpl;
import uk.ac.ebi.fg.annotare2.web.server.services.SubmissionManagerImpl;
import uk.ac.ebi.fg.annotare2.web.server.services.files.AnnotareUploadStorage;
import uk.ac.ebi.fg.annotare2.web.server.services.files.FileAvailabilityChecker;
import uk.ac.ebi.fg.annotare2.web.server.services.files.FtpManagerImpl;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Ordering.natural;
import static uk.ac.ebi.fg.annotare2.web.server.rpc.transform.UIObjectConverter.uiDataFileRows;


public class DataFilesServiceImpl extends SubmissionBasedRemoteService implements DataFilesService {

    private static final long serialVersionUID = -1656426466008261462L;

    private final static String EMPTY_FILE_MD5 = "d41d8cd98f00b204e9800998ecf8427e";

    private final DataFileManagerImpl dataFileManager;
    private final FtpManagerImpl ftpManager;
    private final AnnotareUploadStorage uploadStorage;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AnnotareProperties annotareProperties;

    // Per-submission striped locks to serialize concurrent mutations on the same submission
    private static final Striped<Lock> SUBMISSION_LOCKS = Striped.lazyWeakLock(1024);

    @Inject
    public DataFilesServiceImpl(AccountService accountService,
                                SubmissionManagerImpl submissionManager,
                                DataFileManagerImpl dataFileManager,
                                FtpManagerImpl ftpManager,
                                AnnotareUploadStorage uploadStorage,
                                MessengerImpl emailSender, AnnotareProperties annotareProperties) {
        super(accountService, submissionManager, emailSender);
        this.dataFileManager = dataFileManager;
        this.ftpManager = ftpManager;
        this.uploadStorage = uploadStorage;
        this.annotareProperties = annotareProperties;
    }

    @Transactional(rollbackOn = {NoPermissionException.class, ResourceNotFoundException.class})
    @Override
    public List<DataFileRow> getFiles(long submissionId)
            throws ResourceNotFoundException, NoPermissionException {
        try {
            Submission submission = getSubmission(submissionId, Permission.VIEW);
            Collection<DataFile> filesSortedByName = natural().onResultOf(new Function<DataFile, String>() {
                @Nullable
                @Override
                public String apply(@Nullable DataFile input) {
                    return (null != input && null != input.getName()) ? input.getName().toLowerCase() : null;
                }
            }).immutableSortedCopy(submission.getFiles());
            return uiDataFileRows(filesSortedByName);
        } catch (RecordNotFoundException e) {
            throw noSuchRecord(e);
        } catch (AccessControlException e) {
            throw noPermission(e);
        }
    }

    @Transactional(rollbackOn = {NoPermissionException.class, ResourceNotFoundException.class})
    @Override
    public String initSubmissionFtpDirectory(long submissionId)
            throws ResourceNotFoundException, NoPermissionException {
        try {
            Submission submission = getSubmission(submissionId, Permission.VIEW);
            String submissionDirectory = nullToEmpty(submission.getFtpSubDirectory());
//            Disable this directory existance check as it already done at the time submission creation.
//            if (!submissionDirectory.isEmpty() && !ftpManager.doesExist(submissionDirectory)) {
//                ftpManager.createDirectory(submissionDirectory);
//            }
            return submissionDirectory;
        } catch (RecordNotFoundException e) {
            throw noSuchRecord(e);
        } catch (AccessControlException e) {
            throw noPermission(e);
        }
    }

    @Transactional(rollbackOn = {NoPermissionException.class, ResourceNotFoundException.class})
    @Override
    public List<Boolean> registerFilesBeforeUpload(long submissionId, List<UploadedFileInfo> filesInfo)
            throws ResourceNotFoundException, NoPermissionException {
        try {
            Submission submission = getSubmission(submissionId, Permission.UPDATE);

            List<Boolean> result = new ArrayList<>();
            for (UploadedFileInfo fileInfo : filesInfo) {
                result.add(!(checkFileExists(submission, fileInfo.getFileName()) || (0L == fileInfo.getFileSize())));
            }

            return result;
        } catch (AccessControlException e) {
            throw noPermission(e);
        } catch (RecordNotFoundException e) {
            throw noSuchRecord(e);
        } catch (Exception e) {
            throw unexpected(e);
        }
    }

    @Transactional(rollbackOn = {NoPermissionException.class, ResourceNotFoundException.class, OperationFailedException.class})
    @Override
    public void addUploadedFile(long submissionId, UploadedFileInfo fileInfo)
            throws ResourceNotFoundException, NoPermissionException, OperationFailedException {
        try {
            Submission submission = getSubmission(submissionId, Permission.UPDATE);
            long userId = getCurrentUser().getId();

            File uploadedFile = uploadStorage.getUploadedFile(userId, fileInfo);
            Lock lock = SUBMISSION_LOCKS.get(submission.getId());
            lock.lock();
            try {
                if (checkFileExists(submission, fileInfo.getFileName())) {
                    throw new OperationFailedException("File " + fileInfo + " already exists");
                } else if (0L == fileInfo.getFileSize()) {
                    throw new OperationFailedException("Empty file " + fileInfo.getFileName());
                } else {
                    saveFile(new LocalFileHandle(uploadedFile), null, submission, uploadedFile.length());
                    uploadStorage.removeUploadedFile(userId, fileInfo, false);
                    logger.info("Uploaded {} for submission {}", fileInfo.getFileName(), submission.getId());
                }
            } finally {
                lock.unlock();
            }
        } catch (AccessControlException e) {
            throw noPermission(e);
        } catch (RecordNotFoundException e) {
            throw noSuchRecord(e);
        } catch (OperationFailedException e) {
            logger.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            throw unexpected(e);
        }
    }

    @Transactional(rollbackOn = {NoPermissionException.class, ResourceNotFoundException.class})
    @Override
    public String registerGlobusFiles(long submissionId, List<String> filesInfo) throws ResourceNotFoundException, NoPermissionException {
        try {
            Submission submission = getSubmission(submissionId, Permission.UPDATE);
            return processFileRegistrationCommon(submission, filesInfo, true);
        } catch (AccessControlException e) {
            throw noPermission(e);
        } catch (RecordNotFoundException e) {
            throw noSuchRecord(e);
        }
    }

    @Transactional(rollbackOn = {NoPermissionException.class, ResourceNotFoundException.class})
    @Override
    public String registerFtpFiles(long submissionId, List<String> filesInfo)
            throws ResourceNotFoundException, NoPermissionException {
        try {
            Submission submission = getSubmission(submissionId, Permission.UPDATE);
            return processFileRegistrationCommon(submission, filesInfo, false);
        } catch (AccessControlException e) {
            throw noPermission(e);
        } catch (RecordNotFoundException e) {
            throw noSuchRecord(e);
        }
    }

    private String processFileRegistrationCommon(Submission submission, List<String> filesInfo, boolean isGlobus) {
        try {
            StringBuilder errors = new StringBuilder();
            Map<String,DataFileHandle> files = new HashMap<>();
            FileAvailabilityChecker fileChecker = new FileAvailabilityChecker(annotareProperties);

            List<String> blockedFileExtensions = annotareProperties.getBlockedFileExtensions();

            for (String infoStr : filesInfo) {
                FtpFileInfo info = getFtpFileInfo(infoStr);

                if (null != info) {
                    if (blockedFileExtensions.contains(getExtension(info.getFileName()).toLowerCase()) || info.getFileName().contains("tar.gz")) {
                        errors.append(" - file \"").append(info.getFileName()).append("\" has a blocked extension").append("\n");
                        continue;
                    }

                    URI fileUri;
                    URI legacyFileUri;

                    if (isGlobus) {
                        fileUri = new URI(ftpManager.getGlobusDirectory(submission.getFtpSubDirectory())
                                + URIEncoderDecoder.encode(info.getFileName()));
                        legacyFileUri = new URI(ftpManager.getGlobusRoot() + URIEncoderDecoder.encode(info.getFileName()));
                    } else {
                        fileUri = new URI(ftpManager.getDirectory(submission.getFtpSubDirectory())
                                + URIEncoderDecoder.encode(info.getFileName()));
                        legacyFileUri = new URI(ftpManager.getRoot() + URIEncoderDecoder.encode(info.getFileName()));
                    }

                    processFileRegistration(submission, errors, files, fileChecker, info, fileUri, legacyFileUri);
                } else {
                    errors.append(" - unrecognized format \"").append(infoStr).append("\"").append("\n");
                }
            }

            if (0 == errors.length()) {
                Lock lock = SUBMISSION_LOCKS.get(submission.getId());
                lock.lock();
                try {
                    for (Map.Entry<String, DataFileHandle> fileToSave : files.entrySet()) {
                        saveFile(fileToSave.getValue(), fileToSave.getKey(), submission);
                    }
                } finally {
                    lock.unlock();
                }
            }
            return errors.toString();
        } catch (URISyntaxException | IOException | DataSerializationException e) {
            throw unexpected(e);
        }
    }

    private void processFileRegistration(Submission submission, StringBuilder errors, Map<String, DataFileHandle> files, FileAvailabilityChecker fileChecker, FtpFileInfo info, URI fileUri, URI legacyFileUri) throws IOException {
        DataFileHandle fileSource = DataFileHandle.createFromUri(fileUri);
        DataFileHandle legacyFileSource = DataFileHandle.createFromUri(legacyFileUri);

        if (fileChecker.isAvailable(fileSource)) {
            validateFilesInfo(submission, errors, files, info, fileSource);
        } else if (fileChecker.isAvailable(legacyFileSource)) {
            validateFilesInfo(submission, errors, files, info, legacyFileSource);
        } else {
            errors.append(" - file \"").append(info.getFileName()).append("\" not found").append("\n");
        }
    }

    private void validateFilesInfo(Submission submission, StringBuilder errors, Map<String, DataFileHandle> files, FtpFileInfo info, DataFileHandle fileSource) {
        String digest = info.getMd5().toLowerCase();
        if (checkFileExists(submission, info.getFileName())) {
            errors.append(" - file \"").append(info.getFileName()).append("\" already exists").append("\n");
        } else if (EMPTY_FILE_MD5.equals(digest)) {
            errors.append("empty file \"").append(info.getFileName()).append("\"").append("\n");
        } else {
            files.put(digest, fileSource);
        }
    }

    @Transactional(rollbackOn = {NoPermissionException.class, ResourceNotFoundException.class, UnexpectedException.class})
    @Override
    public void renameFile(long submissionId, long fileId, String fileName)
            throws ResourceNotFoundException, NoPermissionException {
        try {
            Submission submission = getSubmission(submissionId, Permission.UPDATE);
            Lock lock = SUBMISSION_LOCKS.get(submission.getId());
            lock.lock();
            try {
            ExperimentSubmission experimentSubmission = submission instanceof ExperimentSubmission ?
                    (ExperimentSubmission)submission : null;
            ExperimentProfile experiment = submission instanceof ExperimentSubmission ?
                    experimentSubmission.getExperimentProfile() : null;
            DataFile dataFile = dataFileManager.get(fileId);
            if (submission.getFiles().contains(dataFile) && dataFile.getStatus().isFinal()) {
                String currentFileName = dataFile.getName();
                if (null != experiment) {
                    experiment.renameFile(new FileRef(currentFileName, dataFile.getDigest()), fileName);
                    experimentSubmission.setExperimentProfile(experiment);
                }
                dataFileManager.renameDataFile(dataFile, fileName);
                renameFileInDataStore(submissionId, currentFileName, fileName);
                URI remoteURI = new URI(ftpManager.getDirectory(experimentSubmission.getFtpSubDirectory()) + URLEncoder.encode(currentFileName, StandardCharsets.UTF_8.toString()));
                DataFileHandle dataFileHandle = DataFileHandle.createFromUri(remoteURI);
                if(dataFileHandle.exists() && !(dataFileHandle.getName().equals(fileName))){
                    dataFileHandle.rename(fileName);
                }
                save(submission);
            }
            } finally {
                lock.unlock();
            }
        } catch (RecordNotFoundException e) {
            throw noSuchRecord(e);
        } catch (AccessControlException e) {
            throw noPermission(e);
        } catch (DataSerializationException | URISyntaxException | UnexpectedException | IOException e) {
            throw unexpected(e);
        }
    }

    private void renameFileInDataStore(long submissionId, String currentFileName, String newFileName) {
        LinuxShellCommandExecutor executor = new LinuxShellCommandExecutor();
        try{
            executor.execute(annotareProperties.getDataStoreFileRenameScript() + " " + submissionId + " " + currentFileName + " " + newFileName);
        } catch (IOException e) {
            throw new UnexpectedException("Failed to rename file " + currentFileName + " in Datastore for submission " + submissionId, e);
        }
    }

    @Transactional(rollbackOn = {NoPermissionException.class, ResourceNotFoundException.class})
    @Override
    public void deleteFiles(long submissionId, List<Long> fileIds)
            throws ResourceNotFoundException, NoPermissionException {
        try {
            Submission submission = getSubmission(submissionId, Permission.UPDATE);
            Lock lock = SUBMISSION_LOCKS.get(submission.getId());
            lock.lock();
            try {
            ExperimentSubmission experimentSubmission = submission instanceof ExperimentSubmission ?
                    (ExperimentSubmission)submission : null;
            ExperimentProfile experiment = submission instanceof ExperimentSubmission ?
                    experimentSubmission.getExperimentProfile() : null;
            for (Long fileId : fileIds) {
                DataFile dataFile = dataFileManager.get(fileId);
                if (submission.getFiles().contains(dataFile)) {
                    if (null != experiment) {
                        experiment.removeFile(new FileRef(dataFile.getName(), dataFile.getDigest()));
                        experimentSubmission.setExperimentProfile(experiment);
                    }

                    submission.getFiles().remove(dataFile);
                    dataFileManager.deleteDataFileSoftly(dataFile);
                }
            }
            save(submission);
            } finally {
                lock.unlock();
            }
        } catch (RecordNotFoundException e) {
            throw noSuchRecord(e);
        } catch (AccessControlException e) {
            throw noPermission(e);
        } catch (IOException | DataSerializationException e) {
            throw unexpected(e);
        }
    }

    private boolean checkFileExists(final Submission submission, final String fileName) {
        Set<DataFile> files = submission.getFiles();
        for (DataFile dataFile : files) {
            if (fileName.equals(dataFile.getName())) {
                return true;
            }
        }
        return false;
    }

    private void saveFile(final DataFileHandle source, final String md5, final Submission submission, long fileSize)
            throws DataSerializationException, IOException {

//        boolean shouldStore = source instanceof LocalFileSource;
//        if (!shouldStore) {
//            if (submission instanceof ExperimentSubmission) {
//                shouldStore = !((ExperimentSubmission) submission).getExperimentProfile().getType().isSequencing();
//            } else if (submission instanceof ImportedExperimentSubmission) {
//                shouldStore = source.getName().matches("(?i)^.*[.]?(idf|sdrf)[.]txt$");
//            }
//        }
//
        dataFileManager.addFile(source, md5, submission, true, fileSize);
        save(submission);
    }

    private void saveFile(final DataFileHandle source, final String md5, final Submission submission)
            throws DataSerializationException, IOException {

//        boolean shouldStore = source instanceof LocalFileSource;
//        if (!shouldStore) {
//            if (submission instanceof ExperimentSubmission) {
//                shouldStore = !((ExperimentSubmission) submission).getExperimentProfile().getType().isSequencing();
//            } else if (submission instanceof ImportedExperimentSubmission) {
//                shouldStore = source.getName().matches("(?i)^.*[.]?(idf|sdrf)[.]txt$");
//            }
//        }
//
        dataFileManager.addFile(source, md5, submission, true);
        save(submission);
    }

    private final Pattern md5Pattern1 = Pattern.compile("^\\s*[mM][dD]5\\s+\\((.+)\\)\\s*=\\s*([0-9a-fA-F]{32})\\s*$");
    private final Pattern md5Pattern2 = Pattern.compile("^\\s*([0-9a-fA-F]{32})\\s+(.+)\\s*$");
    private final Pattern md5Pattern3 = Pattern.compile("^\\s*(.+\\S)\\s+([0-9a-fA-F]{32})\\s*$");

    private FtpFileInfo getFtpFileInfo(final String str) {
        if (null == str || str.isEmpty())
            return null;
        String fileName = null, md5 = null;

        Matcher matcher1 = md5Pattern1.matcher(str);
        Matcher matcher2 = md5Pattern2.matcher(str);
        Matcher matcher3 = md5Pattern3.matcher(str);

        if (matcher1.find()) {
            fileName = matcher1.group(1);
            md5 = matcher1.group(2);
        } else if (matcher2.find()) {
            md5 = matcher2.group(1);
            fileName = matcher2.group(2);
        } else if (matcher3.find()) {
            fileName = matcher3.group(1);
            md5 = matcher3.group(2);
        }
        if (null != md5) {
            return new FtpFileInfo(fileName, md5);
        }
        return null;
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return null;
        } else {
            int index = indexOfExtension(filename);
            return index == -1 ? "" : filename.substring(index + 1);
        }
    }

    private int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int extensionPos = filename.lastIndexOf('.');
            int lastSeparator = indexOfLastSeparator(filename);
            return lastSeparator > extensionPos ? -1 : extensionPos;
        }
    }

    private int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int lastUnixPos = filename.lastIndexOf('/');
            int lastWindowsPos = filename.lastIndexOf('\\');
            return Math.max(lastUnixPos, lastWindowsPos);
        }
    }
}
