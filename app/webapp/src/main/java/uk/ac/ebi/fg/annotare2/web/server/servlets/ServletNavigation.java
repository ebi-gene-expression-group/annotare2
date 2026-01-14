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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.annotare2.web.server.servlets.utils.SessionAttribute;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Olga Melnichuk
 */
enum ServletNavigation {
    LOGIN("/login/", "/static.jsp?pageName=login.jsp"),
    SIGNUP("/sign-up/", "/static.jsp?pageName=sign-up.jsp"),
    VERIFY_EMAIL("/verify-email/", "/static.jsp?pageName=verify-email.jsp"),
    CHANGE_PASSWORD("/change-password/", "/static.jsp?pageName=change-password.jsp"),
    HOME("/", "/home.jsp"),
    EDITOR("/edit/", "/editor.jsp"),
    PRIVACY_NOTICE("/privacy-notice/","/static.jsp?pageName=privacy-notice.jsp"),
    PRIVACY_NOTICE_PDF("/privacy-notice-document/", "/static.jsp?pageName=privacy-notice-pdf.jsp");

    private static final Logger log = LoggerFactory.getLogger(ServletNavigation.class);

    private static final SessionAttribute ORIGINAL_URL = new SessionAttribute("original_url");

    private static final Pattern GWT_SRV_PARAM = Pattern.compile(".*?(gwt\\.codesvr=[0-9.:]+).*");

    private String redirectTo;

    private String forwardTo;

    ServletNavigation(String redirectTo, String forwardTo) {
        this.redirectTo = redirectTo;
        this.forwardTo = forwardTo;
    }

    public void saveAndRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Saving the original url and redirecting to {}", redirectTo);
        ORIGINAL_URL.set(request.getSession(), requestUrl(request));
        redirect(request, response);
    }

    public void restoreAndRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String originalUrl = (String) ORIGINAL_URL.get(request.getSession());
        if (originalUrl != null) {
            log.debug("Redirecting to original url {}", originalUrl);
            sendRedirect(originalUrl, response);
        } else {
            redirect(request, response);
        }
    }

    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Redirecting to {}", redirectTo);
        sendRedirect(contextBasedUrl(redirectTo, request), response);
    }

    public void forward(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        log.debug("Forwarding to {}", forwardTo);
        context.getRequestDispatcher(preserveCodeSrvParam(forwardTo, request)).forward(request, response);
    }

    private static String contextBasedUrl(String url, HttpServletRequest request) {
        return request.getContextPath() + preserveCodeSrvParam(url, request);
    }

    private static void sendRedirect(String url, HttpServletResponse response) throws IOException {
        response.sendRedirect(response.encodeRedirectURL(url));
    }

    private static String requestUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        return isNullOrEmpty(queryString) ? uri : uri + "?" + queryString;
    }

    private static String preserveCodeSrvParam(String url, HttpServletRequest request) {
        StringBuilder newUrl = new StringBuilder().append(url);
        String params = nullToEmpty(request.getQueryString());
        Matcher m = GWT_SRV_PARAM.matcher(params);
        if (m.matches()) {
            newUrl.append(url.contains("?") ? "&" : "?").append(m.group(1));
        }
        return newUrl.toString();
    }
}
