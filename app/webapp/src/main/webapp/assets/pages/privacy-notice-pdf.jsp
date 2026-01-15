<%--
  ~ Copyright 2009-2018 European Molecular Biology Laboratory
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Privacy Notice</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <style>
        html, body { height: 100%; margin: 0; }
        .actions { padding: 10px 16px; background: #f7f7f7; border-bottom: 1px solid #ddd; }
        .actions a { color: #1240ab; text-decoration: none; }
        .actions a:hover { text-decoration: underline; }
        .viewer { height: calc(100vh - 52px); }
    </style>
</head>
<body>
<div class="viewer">
    <object data="${pageContext.request.contextPath}/assets/pdf/Privacy_Notice_Annotare.pdf"
            type="application/pdf" width="100%" height="100%">
        <iframe src="${pageContext.request.contextPath}/assets/pdf/Privacy_Notice_Annotare.pdf" width="100%" height="100%"></iframe>
        <p>
            Your browser does not support embedded PDFs. You can
            <a href="${pageContext.request.contextPath}/assets/pdf/Privacy_Notice_Annotare.pdf">download the PDF instead</a>.
        </p>
    </object>
</div>

</body>
</html>
