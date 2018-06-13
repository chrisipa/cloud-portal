<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="application" type="de.papke.cloud.portal.model.ApplicationModel"--%>
<%--@elvariable id="console" type="de.papke.cloud.portal.model.ScriptingConsoleModel"--%>

<jsp:include page="header.jsp" />

<div id="wrapper">

    <jsp:include page="navigation.jsp" />

    <div id="page-wrapper">
        <div class="row">
            <div class="col-lg-12">
                <h4 class="page-header">Scripting Console</h4>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-12">
                <div class="alert alert-info">
                    <i class="fa fa-dollar fa-fw"></i>
                    <c:forEach items="${console.variableList}" var="variable">
                       <c:out value="${variable}" />&nbsp;|&nbsp; 
                    </c:forEach>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-6">
                <form method="post" action="<c:url value="/console" />" target="result" enctype="multipart/form-data">
	                <div class="panel panel-default" id="script-code-panel">
	                    <div class="panel-heading">
	                       <input class="inline" type="file" name="file"/>
	                       <button type="submit" id="execute" type="button" class="btn btn-danger btn-xs btn-right">Execute</button>
	                    </div>
	                    <div class="panel-body no-padding">
                            <div id="editor" class="script" onclick="resetFile()"><c:out value="${console.lastScript}" /></div>
	                    </div>
	                </div>
	                <input id="script" name="script" type="hidden" />
                </form>
            </div>
            <div class="col-lg-6">
                <div class="panel panel-default" id="script-output-panel">
                    <div class="panel-heading">Output</div>
                    <div class="panel-body no-padding">
                        <iframe name="result" id="result" frameborder="0" class="script"></iframe>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp" />

<script type="text/javascript">
$(document).ready(function() {
	var editor = ace.edit("editor");
    editor.setTheme("ace/theme/eclipse");
    editor.session.setMode("ace/mode/groovy");
    editor.setOptions({
        fontSize: "12pt"
   	});
});
</script>