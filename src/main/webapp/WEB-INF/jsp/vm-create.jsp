<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%--@elvariable id="self" type="de.papke.cloud.portal.model.Data"--%>

<jsp:include page="header.jsp" />

<div id="wrapper">

	<jsp:include page="navigation.jsp" />

	<div id="page-wrapper">
		<div class="row">
			<h1 class="page-header">Virtual Machines</h1>
			<div class="col-lg-3">
				<form method="post"
					action="<c:url value="/vm/provision/${self.cloudProvider}" />"
					target="output" role="form" enctype="multipart/form-data">

					<c:forEach items="${self.cloudProviderDefaultsList}" var="variable">
						<label><c:out value="${variable.title}" /></label>
						<c:choose>
							<c:when test="${fn:endsWith(variable.name, 'file')}">
								<input type="file" name="${variable.name}" required="required">
							</c:when>
							<c:otherwise>
								<c:choose>
									<c:when test="${fn:endsWith(variable.name, 'boolean')}">
									    <br />
									    <c:choose>
									       <c:when test="${variable.defaultValue == 'true'}">
									           <input type="checkbox" name="${variable.name}" required="required" checked="checked">
									       </c:when>
									       <c:otherwise>
									           <input type="checkbox" name="${variable.name}" required="required">
									       </c:otherwise> 
									    </c:choose>
									</c:when>
									<c:otherwise>
										<input class="form-control" name="${variable.name}"
											value="${variable.defaultValue}" required="required">
									</c:otherwise>
								</c:choose>
							</c:otherwise>
						</c:choose>
						<p class="help-block">
							<c:out value="${variable.description}" />
						</p>
					</c:forEach>

					<input type="hidden" id="action" name="action" value="plan" />

					<button type="submit" id="plan" class="btn btn-warning">Plan</button>
					<button type="submit" id="apply" class="btn btn-danger">Apply</button>
					
					<p>&nbsp;</p>
				</form>
			</div>
			<!-- /.col-lg-2 -->
			<div class="col-lg-9">
				<div class="form-group">
					<label>Output</label>
					<iframe name="output" id="output" frameborder="0" scrolling="yes"></iframe>
				</div>
			</div>
			<!-- /.col-lg-10 -->
		</div>
		<!-- /.row -->
	</div>
	<!-- /#page-wrapper -->

</div>
<!-- /#wrapper -->

<jsp:include page="footer.jsp" />