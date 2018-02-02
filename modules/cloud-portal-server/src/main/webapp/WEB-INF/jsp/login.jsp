<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--@elvariable id="application" type="de.papke.cloud.portal.model.ApplicationModel"--%>

<jsp:include page="header.jsp" />

<div class="container">
    <div class="row">
        <div class="col-md-4 col-md-offset-4">
            <div class="login-panel panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title"><c:out value="${application.applicationTitle}" /><c:if test="${application.devMode}"> [dev mode]</c:if></h3>
                </div>
                <div class="panel-body">
                    <form method="post" action="<c:url value="/login" />" role="form">
                        <fieldset>
                            <div class="form-group">
                                <input class="form-control" placeholder="E-mail" name="username" type="email" autofocus>
                            </div>
                            <div class="form-group">
                                <input class="form-control" placeholder="Password" name="password" type="password" value="">
                            </div>
                            <div class="checkbox">
                                <label>
                                    <input name="remember" type="checkbox" value="Remember Me">Remember Me
                                </label>
                            </div>
                            <input type="submit" hidden="true" />
                            <!-- Change this to a button or input when using this as a form -->
                            <a href="javascript:document.forms[0].submit()" class="btn btn-lg btn-success btn-block">Login</a>
                        </fieldset>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp" />