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

package uk.ac.ebi.fg.annotare2.web.server.servlets;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.annotare2.web.server.services.AccountService;
import uk.ac.ebi.fg.annotare2.web.server.services.AccountServiceException;
import uk.ac.ebi.fg.annotare2.web.server.servlets.utils.FormParams;
import uk.ac.ebi.fg.annotare2.web.server.servlets.utils.ValidationErrors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uk.ac.ebi.fg.annotare2.web.server.servlets.ServletNavigation.*;
import static uk.ac.ebi.fg.annotare2.web.server.servlets.SessionInformation.EMAIL_SESSION_ATTRIBUTE;
import static uk.ac.ebi.fg.annotare2.web.server.servlets.SessionInformation.INFO_SESSION_ATTRIBUTE;

/**
 * @author Olga Melnichuk
 */
public class LoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);

    @Inject
    private AccountService accountService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("Login details submitted; validating..");
        ValidationErrors errors = new ValidationErrors();
        try {
            errors.append(accountService.login(request));
            if (errors.isEmpty()) {
                log.debug("Login details are valid; Authorization succeeded");
                HOME.restoreAndRedirect(request, response);
                return;
            } else if (!errors.getErrors(FormParams.TOKEN_PARAM).isEmpty()) {
                log.debug("Email needs to be activated; redirecting");
                saveEmailInSessionIfPresent(request);
                INFO_SESSION_ATTRIBUTE.set(request.getSession(), "The email verification code has been sent to you, please enter it now");
                VERIFY_EMAIL.redirect(request, response);
                return;
            }
            log.debug("Login details are invalid");
        } catch (AccountServiceException e) {
            log.debug("Authorization failed");
            errors.append(e.getMessage());
        }

        request.setAttribute("errors", errors);
        LOGIN.forward(getServletConfig().getServletContext(), request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGIN.forward(getServletConfig().getServletContext(), request, response);
    }

    private void saveEmailInSessionIfPresent(HttpServletRequest request) {
        String email = request.getParameter(FormParams.EMAIL_PARAM);
        if (email != null) {
            EMAIL_SESSION_ATTRIBUTE.set(request.getSession(), email);
        }
    }
}
