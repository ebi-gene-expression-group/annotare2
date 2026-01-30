/*
 * Copyright 2009-2016 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.fg.annotare2.core.properties;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.annotare2.core.data.SystemEfoTerm;
import uk.ac.ebi.fg.annotare2.magetabcheck.MageTabCheckProperties;
import uk.ac.ebi.fg.annotare2.magetabcheck.efo.EfoServiceProperties;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class AnnotareProperties implements DataFileStoreProperties {

    private static final Logger logger = LoggerFactory.getLogger(AnnotareProperties.class);

    private static final String ANNOTARE_PROPERTIES = "annotare.properties";

    private final MageTabCheckProperties mageTabCheckProperties;

    private final Properties properties;

    private final File tempDir;

    @Inject
    public AnnotareProperties(MageTabCheckProperties mageTabCheckProperties) {
        this.mageTabCheckProperties = mageTabCheckProperties;
        properties = load();

        tempDir = new File(System.getProperty("java.io.tmpdir"));
    }

    public List<String> getMaterialTypes() {
        return getListProperty("materialTypes");
    }

    public List<String> getSequencingHardware() {
        return getListProperty("sequencingHardware");
    }

    public Integer getWatchdogThreadCount() { return getIntegerProperty("watchdog.thread.count"); }

    public Integer getFileCopyingThreadCount() { return getIntegerProperty("fileCopying.thread.count"); }

    public File getEfoIndexDir() {
        return getDirProperty("efo.index.dir");
    }

    public File getHttpUploadDir() {
        return getDirProperty("httpupload.temp.dir");
    }

    @Override
    public File getDataStoreDir() {
        return getDirProperty("datastore.dir");
    }

    public File getExportDir() {
        return getDirProperty("export.dir");
    }

    public Boolean isFtpEnabled() {
        return  Boolean.parseBoolean(getProperty("ftp.enabled").trim());
    }

    public String getFtpPickUpDir() {
        return getProperty("ftp.pickup.dir");
    }

    public String getGlobusPickUpDir() {
        return getProperty("globus.pickup.dir");
    }

    public String getPublicFtpHostname() {
        return getProperty("ftp.public.hostname");
    }

    public String getPublicFtpPath() {
        return getProperty("ftp.public.path");
    }

    // New multi-link support: collect links for a given view (upload/samples/assignment)
    public Map<String, String> getTutorialLinks(String view) {
        Map<String, String> map = new HashMap<String, String>();
        if (isNullOrEmpty(view)) {
            return map;
        }
        String prefix = "tutorial." + view + ".";
        for (String name : properties.stringPropertyNames()) {
            if (name.startsWith(prefix) && name.endsWith(".url")) {
                String label = name.substring(prefix.length(), name.length() - 4);
                String url = getProperty(name);
                if (!isNullOrEmpty(url)) {
                    map.put(label, url);
                }
            }
        }
        return map;
    }

    public Map<String, String> getUploadTutorialLinks() {
        return getTutorialLinks("upload");
    }

    public Map<String, String> getSamplesTutorialLinks() {
        return getTutorialLinks("samples");
    }

    public Map<String, String> getAssignmentTutorialLinks() {
        return getTutorialLinks("assignment");
    }


    public String getPublicFtpUsername() {
        return getProperty("ftp.public.username");
    }

    public String getPublicFtpPassword() {
        return getProperty("ftp.public.password");
    }

    public boolean isAsperaEnabled() {
        return getBooleanProperty("aspera.enabled");
    }

    public String getAsperaPickUpDir() {
        return getProperty("aspera.pickup.dir");
    }

    public String getPublicAsperaUrl() {
        return getProperty("aspera.public.url");
    }

    public String getEmailSmtpHost() {
        return getProperty("mail.smtp.host");
    }

    public String getEmailSmtpPort() {
        return getProperty("mail.smtp.port");
    }

    public String getEmailFromAddress(String templateName) {
        if (hasProperty("mail.from." + templateName.trim()))
            return getProperty("mail.from." + templateName.trim());
        else {
            return getProperty("mail.from.address");
        }
    }

    public String getEmailBccAddress() {
        return getProperty("mail.bcc.address");
    }

    public String getEmailToAddress(String templateName) {
        return getProperty("mail.to." + templateName.trim());
    }

    public String getEmailSubject(String templateName) {
        return getProperty("mail.subject." + templateName.trim());
    }

    public String getEmailTemplate(String templateName) {
        return getProperty("mail.template." + templateName.trim());
    }

    public String getDbConnectionDriver() {
        return getProperty("db.connection.driver");
    }

    public String getDbConnectionUrl() {
        return getProperty("db.connection.url");
    }

    public String getDbConnectionUser() {
        return getProperty("db.connection.user");
    }

    public String getDbConnectionPassword() {
        return getProperty("db.connection.password");
    }

    public String getSubmissionValidatorURL(){
        return getProperty("submission.validator.url");
    }

    public EfoServiceProperties getEfoServiceProperties() {
        return mageTabCheckProperties;
    }

    public String getCookiePopupTemplatePath(){
        return getProperty("cookie.popup.template.path");
    }

    public String getTokenExpiryTime(){
        return getProperty("annotare.password.reset.token.expiry");
    }

    public String getCreateDataStoreScript() {
        return getProperty("annotare.datastore.create.script");
    }

    public String getDataStoreFileRenameScript() {
        return getProperty("annotare.datastore.file.rename.script");
    }

    public boolean isGlobusEnabled() {
        return getBooleanProperty("globus.enabled");
    }

    public String getGlobusTransferComponentURL(){
        return getProperty("globus.transfer.component.url");
    }

    public String getGlobusTransferAPIURL(){
        return getProperty("globus.transfer.api.url");
    }

    public String getContextPath() {
        return getProperty("annotare.context");
    }

    public String getSlurmOutputDirectory() {
        return getProperty("annotare.slurm.output.directory");
    }

    public String getJobSubmitterScriptPath() {
        return getProperty("annotare.slurm.job.submitter.script");
    }

    public boolean isSubmitButtonEnabled() {
        return getBooleanProperty("annotare.submitButton.enabled");
    }

    public boolean isCreateButtonEnabled() {
        return getBooleanProperty("annotare.createButton.enabled");
    }

    protected File getDirProperty(String name) {
        String property = getProperty(name);
        File dir = isNullOrEmpty(property) ?
                new File(tempDir, name.replaceAll("[.]", "-")) :
                new File(property);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                logger.error("Unable to create directory: {}", dir);
            }
        }
        return dir;
    }

    protected List<String> getListProperty(String name) {
        String property = getProperty(name);
        if (isNullOrEmpty(property)) {
            return emptyList();
        }
        return asList(property.split("\\s*,\\s*"));
    }

    protected int getIntegerProperty(String key) {
        return Integer.parseInt(getProperty(key).trim());
    }

    protected boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key).trim());
    }

    protected String getProperty(String key) {
        return nullToEmpty(properties.getProperty(key));
    }

    protected boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    private Properties load() {
        Properties defaults = load("/Annotare-default.properties", new Properties());
        Properties p = new Properties(defaults);

        String propertiesFile = System.getProperty(ANNOTARE_PROPERTIES);
        p = isNullOrEmpty(propertiesFile) ?
                load("/Annotare.properties", p) :
                load(new File(propertiesFile), p);
        return p;
    }

    private static Properties load(String resourceName, Properties properties) {
        try {
            logger.info("Loading properties from resource: " + resourceName);
            return load(AnnotareProperties.class.getResourceAsStream(resourceName), properties);
        } catch (IOException e) {
            logger.error("Unable to load properties from classpath: " + resourceName, e);
        }
        return properties;
    }

    private static Properties load(File file, Properties properties) {
        logger.info("Loading properties from file: " + file.getAbsolutePath());
        try {
            return load(new FileInputStream(file), properties);
        } catch (IOException e) {
            logger.error("Unable to load properties from file " + file.getAbsoluteFile(), e);
        }
        return properties;
    }

    public static Properties load(InputStream in, Properties properties) throws IOException {
        if (in != null) {
            properties.load(in);
        }
        return properties;
    }

    public String getEfoTermAccession(SystemEfoTerm term) {
        return getEfoTermAccession(term.getPropertyName());
    }

    private String getEfoTermAccession(String termProperty) {
        return getProperty("efo.term." + termProperty.trim());
    }

    public Collection<String> getContactRoleAccessions() {
        return filter(transform(getListProperty("contact.roles"), new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable String input) {
                return getEfoTermAccession(input);
            }
        }), new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input != null;
            }
        });
    }
}
