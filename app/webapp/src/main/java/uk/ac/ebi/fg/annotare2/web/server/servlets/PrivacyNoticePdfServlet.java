package uk.ac.ebi.fg.annotare2.web.server.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uk.ac.ebi.fg.annotare2.web.server.servlets.ServletNavigation.PRIVACY_NOTICE_PDF;

public class PrivacyNoticePdfServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PRIVACY_NOTICE_PDF.forward(getServletConfig().getServletContext(), request, response);
    }
}
