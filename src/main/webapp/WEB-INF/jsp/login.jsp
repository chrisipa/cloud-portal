<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--@elvariable id="self" type="de.papke.cloud.portal.model.Data"--%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title><c:out value="${self.applicationTitle}" /></title>

    <!-- Bootstrap Core CSS -->
    <link href="<c:url value="/static/vendor/bootstrap/css/bootstrap.min.css" />" rel="stylesheet">

    <!-- MetisMenu CSS -->
    <link href="<c:url value="/static/vendor/metisMenu/metisMenu.min.css" />" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="<c:url value="/static/dist/css/sb-admin-2.css" />" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="<c:url value="/static/vendor/font-awesome/css/font-awesome.min.css" />" rel="stylesheet" type="text/css">

</head>

<body class="bg-dark">

<div class="container">
    <div class="card card-login mx-auto mt-5">
        <div class="card-header"><c:out value="${self.applicationTitle}" /></div>
        <div class="card-body">
            <form method="post" action="<c:url value="/login" />">
                <div class="form-group">
                    <label for="username">Username</label>
                    <input class="form-control" id="username" type="text" name="username" aria-describedby="usernameHelp" placeholder="Enter username">
                </div>
                <div class="form-group">
                    <label for="password">Password</label>
                    <input class="form-control" id="password" type="password" name="password" placeholder="Password">
                </div>
                <div class="form-group">
                    <div class="form-check">
                        <label class="form-check-label">
                            <input class="form-check-input" type="checkbox"> Remember Password</label>
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <button class="btn btn-primary">Login</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- jQuery -->
<script src="<c:url value="/static/vendor/jquery/jquery.min.js" />"></script>

<!-- Bootstrap Core JavaScript -->
<script src="<c:url value="/static/vendor/bootstrap/js/bootstrap.min.js" />"></script>

<!-- Metis Menu Plugin JavaScript -->
<script src="<c:url value="/static/vendor/metisMenu/metisMenu.min.js" />"></script>

<!-- Custom Theme JavaScript -->
<script src="<c:url value="/static/dist/js/sb-admin-2.js" />"></script>

</body>

</html>