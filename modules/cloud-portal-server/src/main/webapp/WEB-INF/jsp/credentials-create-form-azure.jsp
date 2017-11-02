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
			<div class="col-lg-12">
			    <h4 class="page-header">Credentials Admin > Azure > Create</h4>
				<form method="post" action="<c:url value="/credentials" />">
					<div class="form-group">
						<label>Group</label> <input type="text" class="form-control" name="group"
							required="required">
					</div>
					<div class="form-group">
                        <label>Subscription ID</label> <input type="text" class="form-control" name="subscriptionId"
                            required="required">
                    </div>
					<div class="form-group">
                        <label>Tenant ID</label> <input type="text" class="form-control" name="tenantId"
                            required="required">
                    </div>
					<div class="form-group">
                        <label>Client ID</label> <input type="text" class="form-control" name="clientId"
                            required="required">
                    </div>
					<div class="form-group">
                        <label>Client Secret</label> <input type="password" class="form-control" name="clientSecret"
                            required="required">
                    </div>
                    <button id="create/action/azure" type="submit" class="btn btn-warning">Create</button>
                    <button id="cancel" type="button" class="btn btn-default">Cancel</button>
				</form>
			</div>
		</div>
	</div>
</div>

<jsp:include page="footer.jsp" />