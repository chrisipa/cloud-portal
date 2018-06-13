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
                <h4 class="page-header">Not Allowed</h4>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-12">
                <div class="alert alert-danger">
                    You are not allowed to perform this action!
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp" />