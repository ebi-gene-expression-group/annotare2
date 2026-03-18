package uk.ac.ebi.fg.annotare2.web.server.rpc;

import org.junit.Test;
import uk.ac.ebi.fg.annotare2.core.properties.AnnotareProperties;
import uk.ac.ebi.fg.annotare2.db.model.Submission;
import uk.ac.ebi.fg.annotare2.web.server.services.AccountService;
import uk.ac.ebi.fg.annotare2.web.server.services.DataFileManagerImpl;
import uk.ac.ebi.fg.annotare2.web.server.services.MessengerImpl;
import uk.ac.ebi.fg.annotare2.web.server.services.SubmissionManagerImpl;
import uk.ac.ebi.fg.annotare2.web.server.services.files.AnnotareUploadStorage;
import uk.ac.ebi.fg.annotare2.web.server.services.files.FtpManagerImpl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataFilesServiceImplTest {

    @Test
    public void testGetExtension() throws Exception {
        DataFilesServiceImpl service = new DataFilesServiceImpl(
                createMock(AccountService.class),
                createMock(SubmissionManagerImpl.class),
                createMock(DataFileManagerImpl.class),
                createMock(FtpManagerImpl.class),
                createMock(AnnotareUploadStorage.class),
                createMock(MessengerImpl.class),
                createMock(AnnotareProperties.class)
        );

        Method method = DataFilesServiceImpl.class.getDeclaredMethod("getExtension", String.class);
        method.setAccessible(true);

        assertEquals("gz", method.invoke(service, "test.tar.gz"));
        assertEquals("zip", method.invoke(service, "test.zip"));
        assertEquals("doc", method.invoke(service, "test.doc"));
        assertEquals("docx", method.invoke(service, "test.docx"));
        assertEquals("fastq_gz", method.invoke(service, "test.fastq_gz"));
        assertEquals("", method.invoke(service, "test"));
        assertEquals(null, method.invoke(service, (String)null));
    }

    @Test
    public void testProcessFileRegistrationCommon_BlockedExtension() throws Exception {
        AnnotareProperties properties = createMock(AnnotareProperties.class);
        expect(properties.getBlockedFileExtensions()).andReturn(Arrays.asList("doc", "zip")).anyTimes();
        replay(properties);

        DataFilesServiceImpl service = new DataFilesServiceImpl(
                createMock(AccountService.class),
                createMock(SubmissionManagerImpl.class),
                createMock(DataFileManagerImpl.class),
                createMock(FtpManagerImpl.class),
                createMock(AnnotareUploadStorage.class),
                createMock(MessengerImpl.class),
                properties
        );

        Method method = DataFilesServiceImpl.class.getDeclaredMethod("processFileRegistrationCommon", Submission.class, List.class, boolean.class);
        method.setAccessible(true);

        Submission submission = createMock(Submission.class);

        // test.doc is blocked
        String result = (String) method.invoke(service, submission, Arrays.asList("test.doc d41d8cd98f00b204e9800998ecf8427e"), false);
        assertTrue("test.doc should be blocked", result.contains("has a blocked extension"));

        // test.zip is blocked
        result = (String) method.invoke(service, submission, Arrays.asList("test.zip d41d8cd98f00b204e9800998ecf8427e"), false);
        assertTrue("test.zip should be blocked", result.contains("has a blocked extension"));

        // test.tar.gz is specifically blocked
        result = (String) method.invoke(service, submission, Arrays.asList("test.tar.gz d41d8cd98f00b204e9800998ecf8427e"), false);
        assertTrue("test.tar.gz should be blocked", result.contains("has a blocked extension"));
    }
}
