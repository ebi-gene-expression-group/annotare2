<%--
~ Copyright 2009-2016 European Molecular Biology Laboratory
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
--%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib uri="http://htmlcompressor.googlecode.com/taglib/compressor" prefix="compress"%>
<%
    String pageName = request.getParameter("pageName");
    String pageTitle = "";

    if (pageName.equals("about.html")) {
        pageContext.setAttribute("aboutClass", " active");
        pageTitle = "About Annotare";
    } else if (pageName.equals("login.jsp")) {
        pageContext.setAttribute("loginClass", " active");
        pageTitle = "Login";
    } else if(pageName.equals("privacy-notice.jsp")) {
        pageContext.setAttribute("privacyNoticeClass", " active");
        pageTitle = "Privacy Notice";
    } else if (pageName.equals("sign-up.jsp")) {
        pageContext.setAttribute("signUpClass", " active");
        pageTitle = "Register";
    } else if ("help/getting_started.html".equals(pageName)) {
        pageTitle = "Getting Started";
    } else if ("help/accepted_processed_ma_file_formats.html".equals(pageName)) {
        pageTitle = "Accepted Processed Microarray Files Formats";
    } else if ("help/accepted_raw_ma_file_formats.html".equals(pageName)) {
        pageTitle = "Accepted Raw Microarray Files Formats";
    } else if ("help/describe_exp.html".equals(pageName)) {
        pageTitle = "Describe Experiment";
    } else if ("help/file_upload.html".equals(pageName)) {
        pageTitle = "Upload Files and Assign to Samples";
    } else if ("help/sample_attributes.html".equals(pageName)) {
        pageTitle = "Sample Attributes";
    } else if ("help/seq_lib_spec.html".equals(pageName)) {
        pageTitle = "Sequencing Library Information";
    } else if ("help/strict_mtab_matrix.html".equals(pageName)) {
        pageTitle = "Strict MAGE-TAB Format for Matrix Data";
    } else if ("help/submit_exp.html".equals(pageName)) {
        pageTitle = "Submit Experiment";
    } else if ("help/time_saving_features.html".equals(pageName)) {
        pageTitle = "Time Saving Features";
    } else if ("help/two_color_ma.html".equals(pageName)) {
        pageTitle = "Two-colour Microarrays";
    } else if ("help/validate_exp.html".equals(pageName)) {
        pageTitle = "Validate Experiment";
    } else if ("help/experiment_types.html".equals(pageName)) {
        pageTitle = "Experiment Types";
    } else if ("help/file_upload.html#assign-files".equals(pageName)) {
        pageTitle = "Assign Files";
    }
    if (!pageTitle.isEmpty()) {
        pageTitle = pageTitle + " < ";
    }

    if (pageName.startsWith("help/")) {
        pageContext.setAttribute("helpClass", " class=\"active\"");
        pageTitle = pageTitle + "Guide < ";
    }

    pageContext.setAttribute("pageTitle", pageTitle);
%>
<script>
    var localFrameworkVersion = '1.1'; // 1.1 or 1.2 or compliance or other
    // if you select compliance or other we will add some helpful
    // CSS styling, but you may need to add some CSS yourself
    var newDataProtectionNotificationBanner = document.createElement('script');
    newDataProtectionNotificationBanner.src = 'https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/js/ebi-global-includes/script/5_ebiFrameworkNotificationBanner.js?legacyRequest='+localFrameworkVersion;
    document.head.appendChild(newDataProtectionNotificationBanner);
    newDataProtectionNotificationBanner.onload = function() {
        ebiFrameworkRunDataProtectionBanner(); // invoke the banner
    };
</script>
<style>
    div#data-protection-banner a {
        color: white;
    }

    .icon-functional:before,
    .icon-generic:before {
        padding-right: 0.5rem;
    }

    a#data-protection-agree {
        float: right;
    }
</style>
<compress:html enabled="true" removeComments="true" compressCss="true" compressJavaScript="false" yuiJsDisableOptimizations="true">
    <t:frontierpage>
        <jsp:attribute name="title">${pageTitle}Annotare &lt; EMBL-EBI</jsp:attribute>

        <jsp:body>
            <jsp:include page="/assets/pages/${param.pageName}"/>
            <c:if test="${fn:startsWith(param.pageName, 'help/')}">
                <script defer src="https://cdnjs.cloudflare.com/ajax/libs/iframe-resizer/4.2.1/iframeResizer.min.js"></script>
                <script>
                    (function() {
                        function updateIframeHash() {
                            var hash = window.location.hash;
                            var iframes = document.querySelectorAll('iframe.row');
                            for (var i = 0; i < iframes.length; i++) {
                                var src = iframes[i].src;
                                if (src) {
                                    var baseUrl = src.split('#')[0];
                                    var newUrl = baseUrl + hash;
                                    if (iframes[i].src !== newUrl) {
                                        iframes[i].src = newUrl;
                                    }
                                }
                            }
                        }

                        updateIframeHash();
                        window.addEventListener('hashchange', updateIframeHash);

                        document.addEventListener("DOMContentLoaded", function(event) {
                            if (typeof iFrameResize === 'function') {
                                iFrameResize({inPageLinks: true}, 'iframe.row');
                            }
                        });

                        window.addEventListener('message', function(event) {
                            //Listen to messages came from your GitHub Pages domain
                            if (event.origin !== 'https://ebi-gene-expression-group.github.io') {
                                return;
                            }
                            if (event.data && event.data.type === 'iframe-resize') {
                                const iframes = document.querySelectorAll('iframe.row');
                                for (var i = 0; i < iframes.length; i++) {
                                    iframes[i].style.height = (event.data.height) + 'px';
                                }
                            }
                        });
                    })();
                </script>
            </c:if>
        </jsp:body>
    </t:frontierpage>
</compress:html>