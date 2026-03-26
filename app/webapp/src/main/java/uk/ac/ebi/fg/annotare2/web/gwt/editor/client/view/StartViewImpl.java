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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.utils.Urls;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.ArrayDesignRef;
import uk.ac.ebi.fg.annotare2.web.gwt.editor.client.view.experiment.setup.SetupExpSubmissionView;
import uk.ac.ebi.fg.annotare2.web.gwt.editor.client.view.widget.ContactUsDialog;

import java.util.List;

/**
 * @author Olga Melnichuk
 */
public class StartViewImpl extends Composite implements StartView {

    interface Binder extends UiBinder<Widget, StartViewImpl> {
        Binder BINDER = GWT.create(Binder.class);
    }

    private final ContactUsDialog contactUsDialog;

    @UiField
    SetupExpSubmissionView view;

    public StartViewImpl() {
        initWidget(Binder.BINDER.createAndBindUi(this));
        contactUsDialog = new ContactUsDialog();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        view.setPresenter(presenter);
        contactUsDialog.setPresenter(presenter);
    }

    @Override
    public void setArrayDesignList(List<ArrayDesignRef> adList) {
        view.setArrayDesignList(adList);
    }

    @UiHandler("contactButton")
    void onFeedbackButtonClick(ClickEvent event) {
        contactUsDialog.center();
    }

    @UiHandler("helpButton")
    void onHelpButtonClick(ClickEvent event) {
        Window.open(Urls.getContextUrl() + "help/", "_blank", "");
    }

    @UiHandler("faqsButton")
    void onFAQSButtonClick(ClickEvent event) {
        Window.open(Urls.getContextUrl() + "help/faq.html", "_blank", "");
    }
}
