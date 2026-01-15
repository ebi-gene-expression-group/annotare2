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
<%@tag description="Frontier page template" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="title" required="true" %>
<%@attribute name="localnav" fragment="true" required="false" %>
<%@attribute name="extracss" fragment="true" %>
<%@attribute name="extrajs" fragment="true" %>
<%@attribute name="extradeferjs" fragment="true" %>

<!DOCTYPE html>
<html lang="en"> <!--<![endif]-->
<head>
    <meta charset="utf-8"/>
    <title>Annotare &lt; EMBL-EBI</title>
    <meta name="description" content="Annotare, the ArrayExpress submission tool"/><!-- Describe what this page is about -->
    <meta name="keywords" content="bioinformatics, europe, institute"/>
    <!-- 3 to 10 keywords about the content of this page (not the whole project) -->
    <meta name="author" content="EMBL-EBI"/><!-- Your [project-name] here -->
    <meta name="HandheldFriendly" content="true"/>
    <meta name="MobileOptimized" content="width"/>
    <meta name="viewport" content="width=device-width,initial-scale=1"/>
    <meta name="theme-color" content="#70BDBD"/> <!-- Android Chrome mobile browser tab color -->
    <!-- Get suggested SEO and social metatags at:
         https://www.ebi.ac.uk/style-lab/websites/patterns/meta-copy.html -->

    <!-- Add information on the life cycle of this page -->
    <meta name="ebi:owner" content="John Doe"/> <!-- Who should be contacted about changes -->
    <meta name="ebi:review-cycle" content="30"/> <!-- In days, how often should the content be reviewed -->
    <meta name="ebi:last-review" content="2015-12-20"/> <!-- The last time the content was reviewed -->
    <meta name="ebi:expiry" content="2016-01-20"/> <!-- When this content is no longer relevant -->

    <!-- If you link to any other sites frequently, consider optimising performance with a DNS prefetch -->
    <link rel="dns-prefetch" href="//www.ebi.ac.uk"/>

    <!-- If you have custom icon, replace these as appropriate.
         You can generate them at realfavicongenerator.net -->
    <link rel="icon" type="image/x-icon"
          href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/favicon.ico"/>
    <link rel="icon" type="image/png"
          href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/favicon-32x32.png"/>
    <link rel="icon" type="image/png" sizes="192x192"
          href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/android-chrome-192x192.png"/>
    <!-- Android (192px) -->
    <link rel="apple-touch-icon-precomposed" sizes="114x114"
          href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/apple-icon-114x114.png"/>
    <!-- For iPhone 4 Retina display (114px) -->
    <link rel="apple-touch-icon-precomposed" sizes="72x72"
          href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/apple-icon-72x72.png"/>
    <!-- For iPad (72px) -->
    <link rel="apple-touch-icon-precomposed" sizes="144x144"
          href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/apple-icon-144x144.png"/>
    <!-- For iPad retinat (144px) -->
    <link rel="apple-touch-icon-precomposed"
          href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/apple-icon-57x57.png"/>
    <!-- For iPhone (57px) -->
    <link rel="mask-icon"
          href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/safari-pinned-tab.svg"
          color="#ffffff"/> <!-- Safari icon for pinned tab -->
    <meta name="msapplication-TileColor" content="#2b5797"/> <!-- MS Icons -->
    <meta name="msapplication-TileImage"
          content="//ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/images/logos/EMBL-EBI/favicons/mstile-144x144.png"/>

    <!-- CSS: implied media=all -->
    <!-- CSS concatenated and minified via ant build script-->

    <link rel="stylesheet" href="https://ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/css/ebi-global.css"
          type="text/css" media="all"/>
    <link rel="stylesheet" href="https://dev.ebi.emblstatic.net/web_guidelines/EBI-Icon-fonts/v1.3/fonts.css"
          type="text/css" media="all"/>

    <!-- Use this CSS file for any custom styling -->
    <!--
      <link rel="stylesheet" href="css/custom.css" type="text/css" media="all" />
    -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/annotare.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/annotare-colours.css">
    <!-- If you have a custom header image or colour -->
    <!--
    <meta name="ebi:masthead-color" content="#000" />
    <meta name="ebi:masthead-image" content="//www.ebi.ac.uk/web_guidelines/EBI-Framework/images/backgrounds/embl-ebi-background.jpg" />
    -->

    <!-- you can replace this with theme-[projectname].css. See http://www.ebi.ac.uk/web/style/colour for details of how to do this -->
    <!-- also inform ES so we can host your colour palette file -->
    <!-- end CSS-->
    <t:analytics></t:analytics>
</head>

<body class="level2"><!-- add any of your classes or IDs -->
<t:analytics-body></t:analytics-body>
<t:banner></t:banner>
<!-- add any of your classes or IDs -->
<div id="skip-to">
    <a href="#content">Skip to main content</a>
</div>

<header id="masthead-black-bar" class="clearfix masthead-black-bar">
</header>
<div id="content">
    <div data-sticky-container>
        <header id="masthead" class="masthead" data-sticky data-sticky-on="large" data-top-anchor="content:top"
                data-btm-anchor="content:bottom">
            <div class="masthead-inner row columns">
                <!-- local-title -->
                <div class="columns medium-12">
                    <a href="${pageContext.request.contextPath}/" title="Back to Annotare homepage">
                        <div class="media-object" id="local-title">
                            <div class="media-object-section hide-for-small-only">
                                <img class="svg"
                                     src="${pageContext.request.contextPath}/assets/images/annotare-logo-64.svg"
                                     alt="Annotare logo" width="64" height="64">
                            </div>
                            <div class="media-object-section">
                                <h1>Annotare</h1>
                            </div>
                        </div>
                    </a>
                </div>
                <!-- /local-title -->
                <!-- local-nav -->
                <nav>
                    <ul id="local-nav" class="menu float-left" data-description="navigational">
                        <li class="first"><a href="${pageContext.request.contextPath}/"
                                             title="Annotare ${project.version} rev.${buildNumber}">Home</a></li>
                        <li class="first"><a href="https://www.ebi.ac.uk/biostudies/ArrayExpress/studies" target="_blank">Go to BioStudies</a></li>
                        <li${helpClass}><a href="${pageContext.request.contextPath}/help">Submission Guide</a></li>
                        <li class="last${aboutClass}"><a href="${pageContext.request.contextPath}/about">About
                            Annotare</a></li>
                    </ul>
                    <!-- If you need to include functional (as opposed to purely navigational) links in your local menu,
                         add them here, and give them a class of "functional". Remember: you'll need a class of "last" for
                         whichever one will show up last...
                         For example: -->
                    <ul class="dropdown menu float-right" data-description="functional">
                        <li class="functional last"><a href="${pageContext.request.contextPath}/about#contact"
                                                       class="icon icon-generic" data-icon="\">Contact us</a></li>
                        <c:choose>
                            <c:when test="${sessionScope.loggedin != null}">
                                <li class="functional last"><a href="${pageContext.request.contextPath}/logout/"
                                                               class="icon icon-functional login"
                                                               data-icon="l">Logout</a></li>
                                <li class="functional"><a href="${pageContext.request.contextPath}/account/"
                                                          style="pointer-events: none; cursor: default;"
                                                          class="icon icon-generic account"
                                                          data-icon="M">${sessionScope.email}</a></li>
                            </c:when>
                            <c:otherwise>
                                <li class="functional last${loginClass}"><a
                                        href="${pageContext.request.contextPath}/login/"
                                        class="icon icon-functional login" data-icon="l">Login</a></li>
                                <li class="functional${signUpClass}"><a
                                        href="${pageContext.request.contextPath}/sign-up/"
                                        class="icon icon-functional register" data-icon="7">Register</a></li>
                            </c:otherwise>
                        </c:choose>
                        <%--<li class="functional"><a href="#" class="icon icon-generic feedback" data-icon="\">Feedback</a></li>--%>

                    </ul>
                </nav>
                <!-- /local-nav -->
            </div>
        </header>
    </div>

    <section id="main-content-area" role="main" class="row columns">
        <jsp:doBody/>
    </section>
</div>

<footer>
    <div id="global-footer" class="global-footer">
        <nav id="global-nav-expanded" class="global-nav-expanded row columns">
            <!-- Footer will be automatically inserted by footer.js -->
        </nav>
        <section id="ebi-footer-meta" class="ebi-footer-meta row columns">
            <!-- Footer meta will be automatically inserted by footer.js -->
        </section>
    </div>
</footer>
<div id="data-protection-message-configuration"
     data-message="This website requires cookies, and the limited processing of your personal data in order to function.
         By using the site you are agreeing to this as outlined in our
         <a target='_blank\' href='/fg/annotare/privacy-notice-document/'
         class='white-color'>Privacy Notice</a> and <a target='_blank' href='https://www.ebi.ac.uk/about/terms-of-use\'
                                                       class='white-color'>Terms of Use</a>."
     data-service-id="Annotare" data-data-protection-version="1"></div>

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<!--
<script>window.jQuery || document.write('<script src="../js/libs/jquery-1.10.2.min.js"><\/script>')</script>
-->

<script defer="defer" src="//ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/js/script.js"></script>

<!-- The Foundation theme JavaScript -->
<script src="//ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/libraries/foundation-6/js/foundation.js"></script>
<script src="//ebi.emblstatic.net/web_guidelines/EBI-Framework/v1.3/js/foundationExtendEBI.js"></script>
<script type="text/JavaScript">$(document).foundation();</script>
<script type="text/JavaScript">$(document).foundationExtendEBI();</script>

<!-- Google Analytics details... -->
<!-- Change UA-XXXXX-X to be your site's ID -->
<%--<script--%>
<%--        type="text/javascript">!function (e, a, n, t, i, o, c) {--%>
<%--    e.GoogleAnalyticsObject = i, e[i] = e[i] || function () {--%>
<%--        (e[i].q = e[i].q || []).push(arguments)--%>
<%--    }, e[i].l = 1 * new Date, o = a.createElement(n), c = a.getElementsByTagName(n)[0], o.async = 1, o.src = "//www.google-analytics.com/analytics.js", c.parentNode.insertBefore(o, c)--%>
<%--}(window, document, "script", 0, "ga"), ga("create", "UA-629242-1", {cookieDomain: "auto"}), ga("require", "linkid", "linkid.js"), ga("set", "anonymizeIp", !0), ga("send", "pageview");</script>--%>
<!--! end of #wrapper -->
<!--
<script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/plugins.js"></script>
<script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/script.js"></script>
-->
<!--<script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/cookiebanner.js"></script>
<script defer="defer" src="//www.ebi.ac.uk/web_guidelines/js/foot.js"></script>-->
<!-- end scripts-->

<!-- Google Analytics details... -->
<!-- Change UA-XXXXX-X to be your site's ID -->
<!--
<script>
  window._gaq = [['_setAccount','UAXXXXXXXX1'],['_trackPageview'],['_trackPageLoadTime']];
  Modernizr.load({
    load: ('https:' == location.protocol ? '//ssl' : '//www') + '.google-analytics.com/ga.js'
  });
</script>
-->
</body>
</html>
