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

package uk.ac.ebi.fg.annotare2.db.model.enums;

import com.google.common.annotations.GwtCompatible;

/**
 * @author Olga Melnichuk
 */
@GwtCompatible
public enum DataFileStatus {
    TO_BE_STORED("processing..."),
    STORED("uploaded"),
    TO_BE_ASSOCIATED("verifying..."),
    ASSOCIATED("uploaded (FTP)"),
    MD5_ERROR("MD5 error"),
    FILE_NOT_FOUND_ERROR("file not found"),
    ERROR("error"),
    REMOVED("removed"),
    ARCHIVED("Archived");

    private final String title;

    private DataFileStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean isFinal() {
        return TO_BE_STORED != this && TO_BE_ASSOCIATED != this;
    }

    public boolean isOk() {
        return STORED == this || ASSOCIATED == this || REMOVED == this || ARCHIVED == this;
    }

    public boolean isLocal() {
        return STORED == this;
    }
}
