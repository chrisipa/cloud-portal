<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%--@elvariable id="application" type="de.papke.cloud.portal.model.ApplicationModel"--%>

<jsp:include page="header.jsp" />

<div id="wrapper">

	<jsp:include page="navigation.jsp" />

	<div id="page-wrapper">
		<div class="row">
			<h4 class="page-header">Credentials Admin</h4>
			<div class="col-lg-12">
				<form method="post" action="/credentials">
					<div class="form-group">
						<label>Group</label> <input class="form-control" name="group"
							required="required">
					</div>
					<div class="form-group">
						<label>Provider</label> 
						<select class="form-control" name="provider">
							<c:forEach items="${application.cloudProviderList}" var="provider">
								<option><c:out
										value="${fn:toUpperCase(fn:substring(provider, 0, 1))}${fn:toLowerCase(fn:substring(provider, 1,fn:length(provider)))}" /></option>
							</c:forEach>
						</select>
					</div>
					<div class="form-group">
                        <label>Username</label> <input class="form-control" name="username"
                            required="required">
                    </div>
					<div class="form-group">
                        <label>Password</label> <input class="form-control" name="password"
                            required="required">
                    </div>
                    <button id="create/action" type="submit" class="btn btn-warning">Create</button>
                    <button id="cancel" type="button" class="btn btn-default">Cancel</button>
				</form>
			</div>
		</div>
	</div>
</div>

<jsp:include page="footer.jsp" />