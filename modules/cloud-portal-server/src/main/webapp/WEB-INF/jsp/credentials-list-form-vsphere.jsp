<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%--@elvariable id="credentials" type="de.papke.cloud.portal.model.CredentialsModel"--%>

<jsp:include page="header.jsp" />

<div id="wrapper">

	<jsp:include page="navigation.jsp" />

	<div id="page-wrapper">
		<div class="row">
			<div class="col-lg-12">
			    <h4 class="page-header">Credentials Admin > Vsphere > List</h4>
				<form id="main-form" method="get" action="<c:url value="/credentials" />"
					role="form">
					<table id="datatable" class="table table-striped table-bordered" cellspacing="0" width="100%">
						<thead>
							<tr>
								<th>ID</th>
								<th>Group</th>
								<th>vCenter Hostname</th>
								<th>vCenter Username</th>
								<th>vCenter Password</th>
								<th>Actions</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${credentials.credentialsList}" var="cred">
								<tr>
									<td><c:out value="${cred.id}" /></td>
									<td><c:out value="${cred.group}" /></td>
									<td><c:out value="${cred.secretMap['vCenterHostname']}" /></td>
									<td><c:out value="${cred.secretMap['vCenterUsername']}" /></td>
									<td><c:out value="${cred.secretMap['vCenterPassword']}" /></td>
									<td>
									   <button id="delete/action/vsphere/${cred.id}" type="submit" class="btn btn-danger btn-circle"><i class="fa fa-times"></i></button>
									</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</form>
			</div>
		</div>
	</div>
</div>

<jsp:include page="footer.jsp" />