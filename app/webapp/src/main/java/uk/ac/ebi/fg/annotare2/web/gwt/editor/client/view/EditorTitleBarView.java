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

package uk.ac.ebi.fg.annotare2.web.gwt.editor.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import uk.ac.ebi.fg.annotare2.db.model.enums.SubmissionStatus;
import uk.ac.ebi.fg.annotare2.submission.model.ExperimentProfileType;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.SubmissionDetails;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.SubmissionType;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.ValidationResult;
import uk.ac.ebi.fg.annotare2.web.gwt.editor.client.view.experiment.setup.SetupExpSubmissionView;
import uk.ac.ebi.fg.annotare2.web.gwt.editor.client.view.widget.ContactUsDialog;

/**
 * @author Olga Melnichuk
 */
public interface EditorTitleBarView extends IsWidget {

    void setPresenter(Presenter presenter);

//    void setTitle(SubmissionType type, String accession);

    void setCurator(boolean isCurator);

    void setRtServerStatus(boolean status);

    void autoSaveStarted();

    void autoSaveStopped(String errorMessage);

    void criticalUpdateStarted();

    void criticalUpdateStopped();

    void setSubmissionDetails(SubmissionDetails result);

    void setSubmissionEnabled(boolean isEnabled);

    void setExperimentProfileType(ExperimentProfileType experimentProfileType);

    void setUserHasReferrer(boolean b);

    interface Presenter extends SetupExpSubmissionView.Presenter, ContactUsDialog.Presenter {

        void assignSubmissionToMe(AsyncCallback<Void> callback);

        void assignSubmissionToCreator(AsyncCallback<Void> callback);

        void validateSubmission(ValidationHandler handler);

        void submitSubmission(SubmissionHandler handler);

        void postFeedback(Byte score, String message, AsyncCallback<Void> callback);

        void saveCurrentUserReferrer(String referrer, AsyncCallback<Void> callback);

        String getSubmissionExportUrl();

        void checkRtServerStatus();

        void goTo(Place place);
    }

    interface ValidationHandler {

        void onSuccess(ValidationResult result);

        void onFailure();
    }

    interface SubmissionHandler {

        void onSuccess();

        void onFailure();
    }
}
