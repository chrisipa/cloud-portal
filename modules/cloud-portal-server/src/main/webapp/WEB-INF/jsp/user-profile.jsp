<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--@elvariable id="application" type="de.papke.cloud.portal.model.ApplicationModel"--%>

<jsp:include page="header.jsp" />

<div id="wrapper">

	<jsp:include page="navigation.jsp" />

	<div id="page-wrapper">
		<div class="row">
			<div class="col-lg-12">
				<h4 class="page-header">User Profile</h4>
				<div class="form-group">
					<label>Username</label>
					<input class="form-control" value="${application.user.username}" />
				</div>
				<div class="form-group">
					<label>Given Name</label>
					<input class="form-control" value="${application.user.givenName}" />
				</div>
				<div class="form-group">	
					<label>Surname</label>
					<input class="form-control" value="${application.user.surName}" />
				</div>	
				<div class="form-group">	
					<label>Email</label>
					<input class="form-control" value="${application.user.email}" />
				</div>	
				<div class="form-group"> 
	                <label>Groups</label>
	                <ul>
	                    <c:forEach items="${application.user.groups}" var="group">
	                        <li><c:out value="${group}" /></li>
	                    </c:forEach>
	                </ul>
	            </div>
            </div>  
		</div>
	</div>
</div>

<jsp:include page="footer.jsp" />