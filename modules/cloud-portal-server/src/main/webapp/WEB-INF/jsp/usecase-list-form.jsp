<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="application" type="de.papke.cloud.portal.model.ApplicationModel"--%>
<%--@elvariable id="useCase" type="de.papke.cloud.portal.model.UseCaseModel"--%>

<jsp:include page="header.jsp" />

<div id="wrapper">

	<jsp:include page="navigation.jsp" />

	<div id="page-wrapper">
		<div class="row">
			<div class="col-lg-12">
			    <h4 class="page-header">Use Case List</h4>
				<form id="main-form" method="get" action="<c:url value="/usecase" />" target="output"
					role="form">
					<table id="datatable" class="table table-striped table-bordered" cellspacing="0" width="100%">
                        <thead>
                            <tr>
                                <th>Actions</th>
                                <th>Date</th>
                                <th>Username</th>
                                <th>Command</th>
                                <th>Success</th>
                                <th>Variables</th>
                                <th>Expiration Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${useCase.provisionLogs}" var="provisionLog">
                                <tr>
                                    <td width="140">
                                        <c:if test="${application.user.admin}">
                                            <button id="unlink/action/${useCase.id}/${provisionLog.id}" type="submit" class="btn btn-warning btn-circle" data-toggle="tooltip" title="Unlink item">
                                                <i class="fa fa-unlink"></i>
                                            </button>
                                        </c:if>
                                        <c:if test="${provisionLog.success == true && provisionLog.command == 'apply'}">
                                            <button id="destroy/action/${useCase.id}/${provisionLog.id}" type="submit" class="btn btn-danger btn-circle" data-toggle="tooltip" title="Destroy item">
                                                <i class="fa fa-trash"></i>
                                            </button>
                                        </c:if>
                                        <c:if test="${provisionLog.privateKey != null}">
                                            <a href="<c:url value="/provision-log/private-key/${provisionLog.id}" />" class="btn btn-success btn-circle" data-toggle="tooltip" title="Download private key" target="_blank" role="button">
                                                <i class="fa fa-key"></i>
                                            </a>
                                        </c:if>                                            
                                        <c:if test="${application.user.admin}">
                                            <a href="<c:url value="/provision-log/result/${provisionLog.id}" />" class="btn btn-warning btn-circle" data-toggle="tooltip" title="Download result zip" target="_blank" role="button">
                                                <i class="fa fa-download"></i>
                                            </a>
                                        </c:if>
                                    </td>
                                    <td data-order="<fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${provisionLog.date}"/>"><fmt:formatDate pattern="dd.MM.yyyy HH:mm:ss" value="${provisionLog.date}"/></td>
                                    <td><c:out value="${provisionLog.username}" /></td>
                                    <td><c:out value="${provisionLog.command}" /></td>
                                    <td><c:out value="${provisionLog.success}" /></td>
                                    <td>
                                    <c:forEach var="variable" items="${provisionLog.variableMap}">
                                        <p><c:out value="${variable.key}" /> = <c:out value="${useCase.isSecret(variable.key) ? '****' : variable.value}" /></p>
                                    </c:forEach>
                                    </td>
                                    <td data-order="<fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${provisionLog.expirationDate}"/>"><fmt:formatDate pattern="dd.MM.yyyy HH:mm:ss" value="${provisionLog.expirationDate}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <jsp:include page="output.jsp" />
				</form>
			</div>
		</div>
	</div>
</div>

<jsp:include page="footer.jsp" />