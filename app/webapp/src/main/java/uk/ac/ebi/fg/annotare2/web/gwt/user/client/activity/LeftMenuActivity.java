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

package uk.ac.ebi.fg.annotare2.web.gwt.user.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.ApplicationDataServiceAsync;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.ImportSubmissionServiceAsync;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.SubmissionCreateServiceAsync;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.rpc.AsyncCallbackWrapper;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.rpc.ReportingAsyncCallback;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.rpc.ReportingAsyncCallback.FailureMessage;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.view.NotificationPopupPanel;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.ApplicationProperties;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.SubmissionType;
import uk.ac.ebi.fg.annotare2.web.gwt.user.client.event.SubmissionListUpdatedEvent;
import uk.ac.ebi.fg.annotare2.web.gwt.user.client.place.ImportSubmissionPlace;
import uk.ac.ebi.fg.annotare2.web.gwt.user.client.place.SubmissionListPlace;
import uk.ac.ebi.fg.annotare2.web.gwt.user.client.view.LeftMenuView;
import uk.ac.ebi.fg.annotare2.web.gwt.user.client.view.NewWindow;
import uk.ac.ebi.fg.annotare2.web.gwt.user.client.view.SubmissionListFilter;

import static uk.ac.ebi.fg.annotare2.web.gwt.common.client.utils.Urls.getEditorUrl;
import static uk.ac.ebi.fg.annotare2.web.gwt.common.client.utils.Urls.getLauncherUrl;

public class LeftMenuActivity extends AbstractActivity implements LeftMenuView.Presenter {

    private final LeftMenuView view;
    private final PlaceController placeController;
    private final SubmissionCreateServiceAsync createService;
    private final ImportSubmissionServiceAsync importService;
    private final ApplicationDataServiceAsync dataService;

    private EventBus eventBus;

    @Inject
    public LeftMenuActivity(
            LeftMenuView view,
            PlaceController placeController,
            SubmissionCreateServiceAsync createService,
            ImportSubmissionServiceAsync importService,
            ApplicationDataServiceAsync dataService) {
        this.view = view;
        this.placeController = placeController;
        this.createService = createService;
        this.importService = importService;
        this.dataService = dataService;
    }

    public LeftMenuActivity withPlace(Place place) {
        if (place instanceof SubmissionListPlace) {
            this.view.setFilter(((SubmissionListPlace) place).getFilter());
        }
        return this;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        view.setPresenter(this);
        containerWidget.setWidget(view.asWidget());
        this.eventBus = eventBus;

        dataService.getApplicationProperties(AsyncCallbackWrapper.callbackWrap(
                new ReportingAsyncCallback<ApplicationProperties>(FailureMessage.UNABLE_TO_LOAD_APP_PROPERTIES) {
                    @Override
                    public void onSuccess(ApplicationProperties result) {
                        view.setCreateButtonEnabled(result.isCreateButtonEnabled());
                    }
                }
        ));
    }

    @Override
    public void onSubmissionFilter(SubmissionListFilter filter) {
        gotoSubmissionListViewPlace(filter);
    }

    @Override
    public void onSubmissionCreate(SubmissionType type) {
        final NewWindow window = NewWindow.open(getLauncherUrl(), "_blank", null);
        final AsyncCallback<Long> callback = AsyncCallbackWrapper.callbackWrap(
                new ReportingAsyncCallback<Long>(FailureMessage.UNABLE_TO_CREATE_SUBMISSION) {
                    @Override
                    public void onFailure(Throwable x) {
                        super.onFailure(x);
                        window.close();
                    }

                    @Override
                    public void onSuccess(final Long result) {
                        window.setUrl(getEditorUrl(result));
                        notifySubmissionListUpdated();
                    }
                });

        switch (type) {
            case EXPERIMENT:
                createService.createExperiment(callback);
                return;
            //TODO:
            //case ARRAY_DESIGN:
            //    createService.createArrayDesign(callback);
            //    return;
            default:
                NotificationPopupPanel.failure("Unknown submission type + " + type.getTitle(), null);
        }
    }

    private void notifySubmissionListUpdated() {
        eventBus.fireEvent(new SubmissionListUpdatedEvent());
    }

    @Override
    public void onSubmissionImport(SubmissionType type) {
        NotificationPopupPanel.warning("This functionality is not available at the moment", false, false);

//        switch (type) {
//            case IMPORTED_EXPERIMENT:
//                importService.createImportedExperiment(
//                        AsyncCallbackWrapper.callbackWrap(
//                                new ReportingAsyncCallback<Long>(FailureMessage.UNABLE_TO_CREATE_SUBMISSION) {
//                                    @Override
//                                    public void onSuccess(Long result) {
//                                        gotoSubmissionImport(result);
//                                        notifySubmissionListUpdated();
//                                    }
//                                }
//                        )
//                );
//                return;
//            default:
//                NotificationPopupPanel.failure("Unknown submission type + " + type.getTitle(), null);
//        }
    }

    private void gotoSubmissionImport(Long submissionId) {
        ImportSubmissionPlace place = new ImportSubmissionPlace();
        place.setSubmissionId(submissionId);
        goTo(place);
    }

    public void goTo(Place place) {
        placeController.goTo(place);
    }

    private void gotoSubmissionListViewPlace(SubmissionListFilter filter) {
        SubmissionListPlace place = new SubmissionListPlace();
        place.setFilter(filter);
        goTo(place);
    }
}
