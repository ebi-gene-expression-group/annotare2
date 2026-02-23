<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
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
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Annotare 2.0</title>
    <link type="text/css" rel="stylesheet" href="assets/css/general.css">
    <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/font-awesome.min.css">
    <t:analytics></t:analytics>
    <t:chatbot></t:chatbot>
    <script type="text/javascript" src="${pageContext.request.contextPath}/UserApp/UserApp.nocache.js"></script>
</head>
<body revision="${buildNumber}">
<t:analytics-body></t:analytics-body>
<t:chatbot-body></t:chatbot-body>
<!-- OPTIONAL: include this if you want history support -->
<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

<!-- RECOMMENDED if your web app will not function without JavaScript enabled -->
<noscript>
    <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
    </div>
</noscript>

</body>
</html>

