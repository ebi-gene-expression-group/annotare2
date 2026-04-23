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

package uk.ac.ebi.fg.annotare2.web.server.services;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.annotare2.core.components.Messenger;
import uk.ac.ebi.fg.annotare2.core.transaction.Transactional;
import uk.ac.ebi.fg.annotare2.db.model.User;
import uk.ac.ebi.fg.annotare2.web.server.UnauthorizedAccessException;
import uk.ac.ebi.fg.annotare2.web.server.servlets.utils.FormParams;
import uk.ac.ebi.fg.annotare2.web.server.servlets.utils.RequestParam;
import uk.ac.ebi.fg.annotare2.web.server.servlets.utils.ValidationErrors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static uk.ac.ebi.fg.annotare2.web.server.servlets.SessionInformation.EMAIL_SESSION_ATTRIBUTE;
import static uk.ac.ebi.fg.annotare2.web.server.servlets.SessionInformation.LOGGED_IN_SESSION_ATTRIBUTE;

/**
 * @author Olga Melnichuk
 */
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private AccountManager accountManager;
    private Messenger messenger;

    @Inject
    public AccountServiceImpl(AccountManager accountManager, Messenger messenger) {
        this.accountManager = accountManager;
        this.messenger = messenger;
    }

    public boolean isLoggedIn(HttpServletRequest request) {
        return LOGGED_IN_SESSION_ATTRIBUTE.exists(request.getSession());
    }

    @Transactional
    public ValidationErrors signUp(HttpServletRequest request) throws AccountServiceException {
        SignUpParams params = new SignUpParams(request);
        ValidationErrors errors = params.validate();
        if (errors.isEmpty()) {
            if (accountManager.doesExist(params.getEmail())) {
                errors.append(FormParams.EMAIL_PARAM, "User with this email already exists");
            } else {
                User user = accountManager.createUser(params.getName(), params.getEmail(), params.getPassword());
                try {
                    messenger.send(
                            MessengerImpl.NEW_USER_TEMPLATE,
                            ImmutableMap.of(
                                    "to.name", user.getName(),
                                    "to.email", user.getEmail(),
                                    "verification.token", user.getVerificationToken()
                            ),
                            user
                    );
                } catch (RuntimeException x) {
                    log.error("There was a problem sending email", x);
                }
            }
        }
        return errors;
    }

    @Transactional
    public ValidationErrors changePassword(HttpServletRequest request) throws AccountServiceException {
        ChangePasswordParams params = new ChangePasswordParams(request);
        ValidationErrors errors = params.validate();
        if (errors.isEmpty()) {
            if (!accountManager.doesExist(params.getEmail())) {
                errors.append(FormParams.EMAIL_PARAM, "User with this email does not exist");
            } else {
                if (null == params.getToken()) {
                    User user = accountManager.requestChangePassword(params.getEmail());
                    try {
                        messenger.send(
                                MessengerImpl.CHANGE_PASSWORD_REQUEST_TEMPLATE,
                                ImmutableMap.of(
                                        "to.name", user.getName(),
                                        "to.email", user.getEmail(),
                                        "verification.token", user.getVerificationToken()
                                ),
                                user
                        );
                    } catch (RuntimeException x) {
                        log.error("There was a problem sending email", x);
                    }
                } else {
                    if (!accountManager.isPasswordChangeRequested(params.getEmail())) {
                        throw new AccountServiceException("Change password request is invalid; please try again");
                    } else if (!accountManager.isVerificationTokenValid(params.getEmail(), params.getToken())) {
                        errors.append(FormParams.TOKEN_PARAM, "Incorrect code; please try again or request a new one");
                    } else if(accountManager.isVerificationTokenExpired(params.getEmail())) {
                        errors.append(FormParams.TOKEN_PARAM, "Token expired. Please request a new one");
                    }
                    else if (null != params.getPassword()) {
                        User user = accountManager.processChangePassword(params.getEmail(), params.getPassword());
                        try {
                            messenger.send(
                                    MessengerImpl.CHANGE_PASSWORD_CONFIRMATION_TEMPLATE,
                                    ImmutableMap.of(
                                            "to.name", user.getName(),
                                            "to.email", user.getEmail()
                                    ),
                                    user
                            );
                        } catch (RuntimeException x) {
                            log.error("There was a problem sending an email", x);
                        }
                    }
                }
            }
        }
        return errors;
    }

    @Transactional
    public ValidationErrors verifyEmail(HttpServletRequest request) throws AccountServiceException {
        VerifyEmailParams params = new VerifyEmailParams(request);
        ValidationErrors errors = params.validate();
        if (errors.isEmpty()) {
            if (!accountManager.doesExist(params.getEmail())) {
                errors.append(FormParams.EMAIL_PARAM, "User with this email does not exist");
            } else {
                if (!accountManager.isEmailVerified(params.getEmail())) {
                    if(isTokenInvalid(params.getEmail(), params.getToken())) {
                        handleInvalidToken(errors, params.getEmail());
                    } else {
                        User user = accountManager.setEmailVerified(params.getEmail());
                        try {
                            messenger.send(
                                    MessengerImpl.WELCOME_TEMPLATE,
                                    ImmutableMap.of(
                                            "to.name", user.getName(),
                                            "to.email", user.getEmail()
                                    ),
                                    user
                            );
                        } catch (RuntimeException x) {
                            log.error("There was a problem sending an email", x);
                        }
                    }
                }

            }
        }
        return errors;
    }

    @Transactional
    public ValidationErrors resendVerifyEmail(String email) throws AccountServiceException {
        ValidationErrors errors = new ValidationErrors();
        User user = accountManager.requestVerifyEmail(email);
        if (null == user) {
            errors.append(FormParams.EMAIL_PARAM, "User with this email does not exist");
        } else {
            sendVerificationEmail(user);

        }
        return errors;
    }

    private void sendVerificationEmail(User user) {
        try {
            messenger.send(
                    MessengerImpl.VERIFY_EMAIL_TEMPLATE,
                    ImmutableMap.of(
                            "to.name", user.getName(),
                            "to.email", user.getEmail(),
                            "verification.token", user.getVerificationToken()
                    ),
                    user
            );
        } catch (RuntimeException x) {
            log.error("There was a problem sending email", x);
        }
    }

    @Transactional
    public ValidationErrors login(HttpServletRequest request) throws AccountServiceException {
        LoginParams params = new LoginParams(request);
        ValidationErrors errors = params.validate();
        if (errors.isEmpty()) {
            if (!accountManager.isValid(params.getEmail(), params.getPassword())) {
                log.debug("User '{}' entered invalid params", params.getEmail());
                throw new AccountServiceException("Sorry, the email or password you entered is not valid");
            }
            if (!accountManager.isEmailVerified(params.getEmail())) {
                if (null != params.getToken()) {
                    if(isTokenInvalid(params.getEmail(), params.getToken())) {
                        handleInvalidToken(errors, params.getEmail());
                    } else {
                        accountManager.setEmailVerified(params.getEmail());
                    }
                } else {
                    log.debug("User '{}' needs email verification", params.getEmail());
                    errors.append(FormParams.TOKEN_PARAM, "Please enter the email verification code");
                }
            } else {
                log.debug("User '{}' logged in", params.getEmail());
                EMAIL_SESSION_ATTRIBUTE.set(request.getSession(), params.getEmail());
                LOGGED_IN_SESSION_ATTRIBUTE.set(request.getSession(), true);
            }
        }
        return errors;
    }

    private void handleInvalidToken(ValidationErrors errors, String email) {
        errors.append(FormParams.TOKEN_PARAM, "Your token is invalid or expired. A new verification email will be sent to you shortly.");
        User user = accountManager.requestVerifyEmail(email);
        sendVerificationEmail(user);
    }

    private boolean isTokenInvalid(String email, String token) {
        return !accountManager.isVerificationTokenValid(email, token) || accountManager.isVerificationTokenExpired(email);
    }

    @Transactional
    public void setPrivacyNoticeVersion(HttpServletRequest request, int version) throws AccountServiceException {

        accountManager.setPrivacyNoticeVersion(getCurrentUserEmail(request.getSession()),version);
    }

    @Transactional
    public boolean isPrivacyNoticeAccepted(HttpServletRequest request){
        return accountManager.isPrivacyNoticeAccepted(getCurrentUserEmail(request.getSession()));
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public User getCurrentUser(HttpSession session) {
        User user = null;
        String email = getCurrentUserEmail(session);
        if (null != email) {
            user = accountManager.getByEmail(email);
        }
        if (null == user) {
            throw new UnauthorizedAccessException("Sorry, you are not logged in");
        }
        return user;
    }

    public String getCurrentUserEmail(HttpSession session) {
        if (LOGGED_IN_SESSION_ATTRIBUTE.exists(session)) {
            return  (String) EMAIL_SESSION_ATTRIBUTE.get(session);
        }
        return null;
    }

    static class LoginParams extends FormParams {

        private LoginParams(HttpServletRequest request) {
            addParam(RequestParam.from(request, EMAIL_PARAM), true);
            addParam(RequestParam.from(request, PASSWORD_PARAM), true);
            addParam(RequestParam.from(request, TOKEN_PARAM), false);
        }

        public ValidationErrors validate() {
            return validateMandatory();
        }

        public String getEmail() {
            return getParamValue(EMAIL_PARAM);
        }

        public String getPassword() {
            return getParamValue(PASSWORD_PARAM);
        }

        public String getToken() {
            return getParamValue(TOKEN_PARAM);
        }
    }

    static class SignUpParams extends FormParams {

        private SignUpParams(HttpServletRequest request) {
            addParam(RequestParam.from(request, NAME_PARAM), true);
            addParam(RequestParam.from(request, EMAIL_PARAM), true);
            addParam(RequestParam.from(request, PASSWORD_PARAM), true);
            addParam(RequestParam.from(request, CONFIRM_PASSWORD_PARAM), false);
        }

        public ValidationErrors validate() {
            ValidationErrors errors = validateMandatory();

            if (!isEmailGoodEnough()) {
                errors.append(EMAIL_PARAM, "Email is not valid; should at least contain @ sign");
            }

            if (!isPasswordGoodEnough()) {
                errors.append(PASSWORD_PARAM, "Password is too weak; should be at least 4 characters long containing at least one digit");
            }

            if (!hasPasswordConfirmed()) {
                errors.append(CONFIRM_PASSWORD_PARAM, "Passwords do not match");
            }

            return errors;
        }

        public String getName() {
            return getParamValue(NAME_PARAM);
        }

        public String getEmail() {
            return getParamValue(EMAIL_PARAM);
        }

        public String getPassword() {
            return getParamValue(PASSWORD_PARAM);
        }
    }

    static class ChangePasswordParams extends FormParams {

        private ChangePasswordParams(HttpServletRequest request) {
            addParam(RequestParam.from(request, EMAIL_PARAM), false);
            addParam(RequestParam.from(request, PASSWORD_PARAM), false);
            addParam(RequestParam.from(request, CONFIRM_PASSWORD_PARAM), false);
            addParam(RequestParam.from(request, TOKEN_PARAM), false);
        }

        public ValidationErrors validate() {
            ValidationErrors errors = validateMandatory();

            if (null == getToken()) {
                if (!isEmailGoodEnough()) {
                    errors.append(EMAIL_PARAM, "Email is not valid; should at least contain @ sign");
                }
            } else {
                if (null != getPassword()) {
                    if (!isPasswordGoodEnough()) {
                        errors.append(PASSWORD_PARAM, "Password is too weak; should be at least 4 characters long containing at least one digit");
                    }

                    if (!hasPasswordConfirmed()) {
                        errors.append(CONFIRM_PASSWORD_PARAM, "Passwords do not match");
                    }
                }
            }

            return errors;
        }

        public String getEmail() {
            return getParamValue(EMAIL_PARAM);
        }

        public String getToken() {
            return getParamValue(TOKEN_PARAM);
        }

        public String getPassword() {
            return getParamValue(PASSWORD_PARAM);
        }
    }

    static class VerifyEmailParams extends FormParams {

        private VerifyEmailParams(HttpServletRequest request) {
            addParam(RequestParam.from(request, EMAIL_PARAM), true);
            addParam(RequestParam.from(request, TOKEN_PARAM), true);
        }

        public ValidationErrors validate() {
            return validateMandatory();
        }

        public String getEmail() {
            return getParamValue(EMAIL_PARAM);
        }

        public String getToken() {
            return getParamValue(TOKEN_PARAM);
        }
    }
}
