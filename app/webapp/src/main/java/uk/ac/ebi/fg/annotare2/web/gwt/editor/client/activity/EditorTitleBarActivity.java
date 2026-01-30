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

package uk.ac.ebi.fg.annotare2.web.gwt.editor.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;
import uk.ac.ebi.fg.annotare2.db.model.enums.SubmissionStatus;
import uk.ac.ebi.fg.annotare2.submission.model.ExperimentProfileType;
import uk.ac.ebi.fg.annotare2.submission.model.OntologyTerm;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.AdfServiceAsync;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.ApplicationDataServiceAsync;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.CurrentUserAccountServiceAsync;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.SubmissionServiceAsync;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.rpc.AsyncCallbackWrapper;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.rpc.ReportingAsyncCallback;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.rpc.ReportingAsyncCallback.FailureMessage;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.view.CookieDialog;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.*;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.dto.UserDto;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.exepriment.ExperimentSetupSettings;
import uk.ac.ebi.fg.annotare2.web.gwt.editor.client.event.*;
import uk.ac.ebi.fg.annotare2.web.gwt.editor.client.proxy.ExperimentDataProxy;
import uk.ac.ebi.fg.annotare2.web.gwt.editor.client.view.EditorTitleBarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static uk.ac.ebi.fg.annotare2.web.gwt.common.client.rpc.AsyncCallbackWrapper.callbackWrap;
import static uk.ac.ebi.fg.annotare2.web.gwt.editor.client.EditorUtils.getSubmissionId;

/**
 * @author Olga Melnichuk
 */
public class EditorTitleBarActivity extends AbstractActivity implements EditorTitleBarView.Presenter {

    private final EditorTitleBarView view;
    private final PlaceController placeController;
    private final CurrentUserAccountServiceAsync userService;
    private final SubmissionServiceAsync submissionService;
    private final ApplicationDataServiceAsync dataService;
    private final AdfServiceAsync adfService;
    private final ExperimentDataProxy expData;
    private final static String SUBMISSION_READONLY_COOKIE = "Submission_ReadOnly_Shown";

    private EventBus eventBus;

    @Inject
    public EditorTitleBarActivity(EditorTitleBarView view,
                                  PlaceController placeController,
                                  CurrentUserAccountServiceAsync userService,
                                  SubmissionServiceAsync submissionService,
                                  ApplicationDataServiceAsync dataService,
                                  AdfServiceAsync adfService, ExperimentDataProxy expData) {
        this.view = view;
        this.placeController = placeController;
        this.userService = userService;
        this.submissionService = submissionService;
        this.dataService = dataService;
        this.adfService = adfService;
        this.expData = expData;
    }

    public EditorTitleBarActivity withPlace(Place place) {
        return this;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.addHandler(AutoSaveEvent.getType(), new AutoSaveEventHandler() {
            @Override
            public void autoSaveStarted(AutoSaveEvent event) {
                view.autoSaveStarted();
            }

            @Override
            public void autoSaveStopped(AutoSaveEvent event) {
                view.autoSaveStopped(null);
            }

            @Override
            public void autoSaveFailed(AutoSaveEvent event) {
                view.autoSaveStopped(event.getErrorMessage());
            }
        });

        eventBus.addHandler(CriticalUpdateEvent.getType(), new CriticalUpdateEventHandler() {
            @Override
            public void criticalUpdateStarted(CriticalUpdateEvent event) {
                 view.criticalUpdateStarted();
            }

            @Override
            public void criticalUpdateFinished(CriticalUpdateEvent event) {
                 view.criticalUpdateStopped();
            }
        });

        view.setPresenter(this);
        containerWidget.setWidget(view.asWidget());
        initAsync();
    }

    public void goTo(Place place) {
        placeController.goTo(place);
    }

    private void initAsync() {
        dataService.getApplicationProperties(AsyncCallbackWrapper.callbackWrap(
                new ReportingAsyncCallback<ApplicationProperties>(FailureMessage.UNABLE_TO_LOAD_APP_PROPERTIES) {
                    @Override
                    public void onSuccess(ApplicationProperties result) {
                        view.setSubmissionEnabled(result.isSubmitButtonEnabled());
                    }
                }
        ));

        userService.me(AsyncCallbackWrapper.callbackWrap(
                new ReportingAsyncCallback<UserDto>(FailureMessage.UNABLE_TO_LOAD_SUBMISSION) {
                    @Override
                    public void onSuccess(UserDto result) {
                        view.setCurator(result.isCurator());
                        view.setUserHasReferrer(result.hasReferrer());
                        getSubmissionDetail();
                    }
                }
        ));
    }

    private void showSubmissionReadOnlyMessage(SubmissionDetails details) {
        if (!"YEZ".equalsIgnoreCase(Cookies.getCookie(SUBMISSION_READONLY_COOKIE))) {
            final SubmissionStatus submissionStatus = details.getStatus();
            userService.me(AsyncCallbackWrapper.callbackWrap(
                    new ReportingAsyncCallback<UserDto>(FailureMessage.UNABLE_TO_LOAD_USER_INFORMATION) {
                        @Override
                        public void onSuccess(UserDto result) {
                            String message = null;
                            if (submissionStatus != SubmissionStatus.IN_PROGRESS && !result.isCurator()) {
                                switch (submissionStatus) {
                                    case IN_CURATION:
                                        message = "<p>This submission can no longer be modified. Please do not make any changes on these forms, as they will be lost. </p><p>If you'd like to modify any aspects of your submission, e.g. add/remove samples, change release date, update protocol description, please click the \"Contact Us\" button above and tell us what modifications are required. The curator in charge of your submission will respond as soon as possible.</p>";
                                        break;
                                    case PRIVATE_IN_AE:
                                        message = "<p>Curation for this experiment was completed and your experiment has already been loaded into ArrayExpress. This submission can no longer be modified, and there may be curational changes to your experiment that are presented on the ArrayExpress website but not reflected on the Annotare forms. Please do not make any changes on these forms, as they will be lost. </p><p>To change release date and/or publication details, please use <a target=\"_blank\" href=\"https://www.ebi.ac.uk/biostudies/submissions/\">https://www.ebi.ac.uk/biostudies/submissions/</a>. Login details were emailed to you when your experiment was loaded into ArrayExpress.</p><p>If you'd like to modify other aspects of your submission, e.g. add/remove samples, please click the \"Contact Us\" button above and tell us what modifications are required. A curator will respond as soon as possible.</p>";
                                        break;
                                    case PUBLIC_IN_AE:
                                        message = "<p>This experiment is already public in ArrayExpress and this Annotare record can no longer be modified. Please do not make any changes on these forms, as they will be lost. </p><p>If you'd like to modify any aspects of your submission, e.g. add/remove samples, turn the experiment private again because the work is unpublished, update protocol description, please click the \"Contact Us\" button above and tell us what modifications are required. A curator will respond as soon as possible.</p>";
                                        break;
                                    default:
                                        message = "<p>This submission can no longer be modified. Please do not make any changes on these forms, as they will be lost. </p><p>To change release date and/or publication details, please use <a target=\"_blank\" href=\"https://www.ebi.ac.uk/biostudies/submissions/\">https://www.ebi.ac.uk/biostudies/submissions/</a>. Login details were emailed to you when your experiment was loaded into ArrayExpress.</p><p>If you'd like to modify other aspects of your submission, e.g. add/remove samples, please click the \"Contact Us\" button above and tell us what modifications are required. A curator will respond as soon as possible.</p>";
                                        break;
                                }
                            }

                            if (result.isCurator() &&
                                    (submissionStatus == SubmissionStatus.SUBMITTED || submissionStatus == SubmissionStatus.RESUBMITTED) ) {
                                message = "<p>This submission is being updated at the moment. Please refresh the page in one minute to see the new status. Please do not make any changes on these forms, as they will be lost.</p>";
                            }

                            if (message!=null) {
                                Date cookieExpiryDate = new Date();
                                CalendarUtil.addMonthsToDate(cookieExpiryDate, 3);
                                final DialogBox dialogBox = new CookieDialog(
                                        "Submission cannot be modified",
                                        message,
                                        SUBMISSION_READONLY_COOKIE, cookieExpiryDate
                                );
                                dialogBox.show();
                            }

                        }
                    }
            ));
        }
    }


    private void getSubmissionDetail() {
        submissionService.getSubmissionDetails(getSubmissionId(), AsyncCallbackWrapper.callbackWrap(
                new ReportingAsyncCallback<SubmissionDetails>(FailureMessage.UNABLE_TO_LOAD_SUBMISSION_DETAILS) {
                    @Override
                    public void onSuccess(SubmissionDetails submissionDetails) {
                        view.setSubmissionDetails(submissionDetails);
                        if (submissionDetails.getType().equals(SubmissionType.EXPERIMENT)) {
                            setExperimentProfileType();
                        }
                        showSubmissionReadOnlyMessage(submissionDetails);
                    }
                }
        ));
    }

    private void setExperimentProfileType() {
        expData.getExperimentProfileTypeAsync(new AsyncCallbackWrapper<ExperimentProfileType>() {
            @Override
            public void onFailure(Throwable throwable) {
            }
            @Override
            public void onSuccess(ExperimentProfileType experimentProfileType) {
                view.setExperimentProfileType(experimentProfileType);
            }
        });
    }

    @Override
    public void assignSubmissionToMe(final AsyncCallback<Void> callback) {
        submissionService.assignSubmissionToMe(getSubmissionId(), callbackWrap(callback));
    }

    @Override
    public void checkRtServerStatus() {
        submissionService.checkRtServerStatus(getSubmissionId(),AsyncCallbackWrapper.callbackWrap(
                new ReportingAsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        view.setRtServerStatus(aBoolean);
                    }
                }
        ));
    }


    @Override
    public void assignSubmissionToCreator(final AsyncCallback<Void> callback) {
        submissionService.assignSubmissionToCreator(getSubmissionId(), callbackWrap(callback));
    }

    @Override
    public void validateSubmission(final EditorTitleBarView.ValidationHandler handler) {
        submissionService.validateSubmission(getSubmissionId(), callbackWrap(
                new ReportingAsyncCallback<ValidationResult>(FailureMessage.GENERIC_FAILURE) {
                    @Override
                    public void onFailure(Throwable caught) {
                        super.onFailure(caught);
                        handler.onFailure();
                        publishValidationResult(new ValidationResult(caught));
                    }

                    @Override
                    public void onSuccess(ValidationResult result) {
                        handler.onSuccess(result);
                        publishValidationResult(result);
                    }
                }
        ));
    }

    @Override
    public void submitSubmission(final EditorTitleBarView.SubmissionHandler handler) {
        submissionService.submitSubmission(getSubmissionId(), callbackWrap(
                new ReportingAsyncCallback<Void>(FailureMessage.GENERIC_FAILURE) {
                    @Override
                    public void onFailure(Throwable caught) {
                        handler.onFailure();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        handler.onSuccess();
                    }
                }
        ));
    }

    private void publishValidationResult(ValidationResult result) {
        eventBus.fireEvent(new ValidationFinishedEvent(result));
    }

    @Override
    public void setupNewSubmission(ExperimentSetupSettings settings, List<OntologyTerm> experimentDesigns, AsyncCallback<Void> callback) {
        submissionService.setupExperiment(getSubmissionId(), settings, experimentDesigns, callbackWrap(callback));
    }

    @Override
    public void getArrayDesigns(String query, int limit, AsyncCallback<ArrayList<ArrayDesignRef>> callback) {
        dataService.getArrayDesignList(query, limit, callbackWrap(callback));
    }

//    @Override
//    public void importFile(AsyncCallback<Void> callback) {
//        adfService.importBodyData(getSubmissionId(), callbackWrap(callback));
//    }

    @Override
    public String getSubmissionExportUrl() {
        return GWT.getModuleBaseURL().replace("/" + GWT.getModuleName(), "") + "export?submissionId=" + getSubmissionId();
    }

    @Override
    public void sendMessage(String subject, String message) {
        submissionService.sendMessage(getSubmissionId(), subject, message,
                callbackWrap(
                        new ReportingAsyncCallback<Void>(FailureMessage.GENERIC_FAILURE) {

                            @Override
                            public void onSuccess(Void result) {
                            }
                        }
                )
        );
    }

    @Override
    public void postFeedback(Byte score, String comment, AsyncCallback<Void> callback) {
        submissionService.postFeedback(getSubmissionId(), score, comment,
                callbackWrap(callback)
        );
    }

    @Override
    public void saveCurrentUserReferrer(String referrer, AsyncCallback<Void> callback) {
        userService.saveCurrentUserReferrer(referrer, callbackWrap(callback));
    }

    /*@Override
    public void setExperimentalDesigns(List<OntologyTerm> experimentalDesigns)
    {
        //Not Being Used here Actual in SetupActivity
    }*/
}
