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
	            <div class="table-responsive-sm">
                    <table class="table table-sm table-bordered table-striped">
                        <tbody>
                            <tr>
                                <th>Username</th>
                                <td><c:out value="${application.user.username}" /></td>
                            </tr>
                            <tr>
                                <th>Given Name</th>
                                <td><c:out value="${application.user.givenName}" /></td>
                            </tr>
                            <tr>
                                <th>Surname</th>
                                <td><c:out value="${application.user.surName}" /></td>
                            </tr>
                            <tr>
                                <th>Email</th>
                                <td><c:out value="${application.user.email}" /></td>
                            </tr>
                            <tr>
                                <th>Groups</th>
                                <td>
                                    <c:forEach items="${application.user.groups}" var="group">
                                        <c:out value="${group}" /><br />
                                    </c:forEach>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>  
		</div>
	</div>
</div>

<jsp:include page="footer.jsp" />