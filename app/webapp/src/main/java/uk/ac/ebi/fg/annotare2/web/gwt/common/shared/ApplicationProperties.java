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

package uk.ac.ebi.fg.annotare2.web.gwt.common.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Map;

/**
 * @author Olga Melnichuk
 */
public class ApplicationProperties implements IsSerializable {

    private boolean isFtpEnabled;
    private String ftpHostname;
    private String ftpPath;
    private String ftpUsername;
    private String ftpPassword;
    private boolean isAsperaEnabled;
    private String asperaUrl;
    private boolean isGlobusEnabled;
    private String globusTransferComponentURL;
    private String globusTransferAPIURL;
    private String contextPath;
    private boolean isSubmitButtonEnabled;
    private boolean isCreateButtonEnabled;
    private Map<String, String> uploadTutorialUrls;
    private Map<String, String> samplesTutorialUrls;
    private Map<String, String> assignmentTutorialUrls;

    ApplicationProperties() {
    }

    private ApplicationProperties(ApplicationProperties other) {
        this.isFtpEnabled = other.isFtpEnabled;
        this.ftpHostname = other.ftpHostname;
        this.ftpPath = other.ftpPath;
        this.ftpUsername = other.ftpUsername;
        this.ftpPassword = other.ftpPassword;
        this.isAsperaEnabled = other.isAsperaEnabled;
        this.asperaUrl = other.asperaUrl;
        this.isGlobusEnabled = other.isGlobusEnabled;
        this.globusTransferComponentURL = other.globusTransferComponentURL;
        this.globusTransferAPIURL = other.globusTransferAPIURL;
        this.contextPath = other.contextPath;
        this.isSubmitButtonEnabled = other.isSubmitButtonEnabled;
        this.isCreateButtonEnabled = other.isCreateButtonEnabled;
        this.uploadTutorialUrls = other.uploadTutorialUrls == null ? null : new java.util.HashMap<String, String>(other.uploadTutorialUrls);
        this.samplesTutorialUrls = other.samplesTutorialUrls == null ? null : new java.util.HashMap<String, String>(other.samplesTutorialUrls);
        this.assignmentTutorialUrls = other.assignmentTutorialUrls == null ? null : new java.util.HashMap<String, String>(other.assignmentTutorialUrls);
    }

    public boolean isFtpEnabled() {
        return isFtpEnabled;
    }

    public String getFtpHostname() {
        return ftpHostname;
    }

    public String getFtpPath() {
        return ftpPath;
    }

    public String getFtpUsername() {
        return ftpUsername;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public boolean isAsperaEnabled() {
        return isAsperaEnabled;
    }

    public String getAsperaUrl() {
        return asperaUrl;
    }

    public boolean isGlobusEnabled() {return isGlobusEnabled;}

    public String getGlobusTransferComponentURL() {return globusTransferComponentURL;}

    public String getGlobusTransferAPIURL() {return globusTransferAPIURL;}

    public String getContextPath() {return contextPath;}

    public boolean isSubmitButtonEnabled() {
        return isSubmitButtonEnabled;
    }

    public boolean isCreateButtonEnabled() {
        return isCreateButtonEnabled;
    }

    public Map<String, String> getUploadTutorialUrls() {return uploadTutorialUrls;}

    public Map<String, String> getSamplesTutorialUrls() {return samplesTutorialUrls;}

    public Map<String, String> getAssignmentTutorialUrls() {return assignmentTutorialUrls;}

    public static class Builder {
        private final ApplicationProperties properties;

        public Builder() {
            this.properties = new ApplicationProperties();
        }

        public Builder setFtpEnabled(boolean isEnabled) {
            properties.isFtpEnabled = isEnabled;
            return this;
        }

        public Builder setFtpHostname(String hostname) {
            properties.ftpHostname = hostname;
            return this;
        }

        public Builder setFtpPath(String path) {
            properties.ftpPath = path;
            return this;
        }

        public Builder setFtpUsername(String username) {
            properties.ftpUsername = username;
            return this;
        }

        public Builder setFtpPassword(String password) {
            properties.ftpPassword = password;
            return this;
        }

        public Builder setAsperaEnabled(boolean isEnabled) {
            properties.isAsperaEnabled = isEnabled;
            return this;
        }

        public Builder setAsperaUrl(String url) {
            properties.asperaUrl = url;
            return this;
        }

        public Builder setGlobusEnabled(boolean isEnabled) {
            properties.isGlobusEnabled = isEnabled;
            return this;
        }

        public Builder setGlobusTransferComponentURL(String url) {
            properties.globusTransferComponentURL = url;
            return this;
        }

        public Builder setGlobusTransferAPIURL(String url) {
            properties.globusTransferAPIURL = url;
            return this;
        }

        public Builder setContextPath(String contextPath) {
            properties.contextPath = contextPath;
            return this;
        }

        public Builder setSubmitButtonEnabled(boolean isEnabled) {
            properties.isSubmitButtonEnabled = isEnabled;
            return this;
        }

        public Builder setCreateButtonEnabled(boolean isEnabled) {
            properties.isCreateButtonEnabled = isEnabled;
            return this;
        }

        public Builder setUploadTutorialUrls(Map<String, String> map) {
            properties.uploadTutorialUrls = map;
            return this;
        }

        public Builder setSamplesTutorialUrls(Map<String, String> map) {
            properties.samplesTutorialUrls = map;
            return this;
        }

        public Builder setAssignmentTutorialUrls(Map<String, String> map) {
            properties.assignmentTutorialUrls = map;
            return this;
        }

        public ApplicationProperties build() {
            return new ApplicationProperties(properties);
        }
    }
}
