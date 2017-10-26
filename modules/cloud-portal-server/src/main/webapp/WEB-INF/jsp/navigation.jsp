<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="application" type="de.papke.cloud.portal.model.ApplicationModel"--%>

<nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
    <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="<c:url value="/" />"><c:out value="${application.applicationTitle}" /></a>
    </div>

    <ul class="nav navbar-top-links navbar-right">
        <li class="dropdown">
            <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                <i class="fa fa-user fa-fw"></i> <c:out value="${application.username}" /> <i class="fa fa-caret-down"></i>
            </a>
            <ul class="dropdown-menu dropdown-user">
                <li><a href="<c:url value="/user/profile" />"><i class="fa fa-user fa-fw"></i> User Profile</a>
                </li>
                <li class="divider"></li>
                <li><a href="<c:url value="/logout" />"><i class="fa fa-sign-out fa-fw"></i> Logout</a>
                </li>
            </ul>
        </li>
    </ul>

    <div class="navbar-default sidebar" role="navigation">
        <div class="sidebar-nav navbar-collapse">
            <ul class="nav" id="side-menu">
                <li class="sidebar-search">
                    <div class="input-group custom-search-form">
                        <input type="text" class="form-control" placeholder="Search...">
                        <span class="input-group-btn">
                        <button class="btn btn-default" type="button">
                            <i class="fa fa-search"></i>
                        </button>
                    </span>
                    </div>
                </li>
                <li>
                    <a href="<c:url value="/" />"><i class="fa fa-dashboard fa-fw"></i> Dashboard</a>
                </li>
                <c:if test="${application.isAdmin}">
	                <li>
	                    <a href="#"><i class="fa fa-key fa-fw"></i> Credentials Admin<span class="fa arrow"></span></a>
	                    <ul class="nav nav-second-level collapse in">
	                        <c:forEach items="${application.cloudProviderList}" var="cloudProvider">
	                            <li>
	                                <a href="#"><i class="fa fa-cloud fa-fw"></i> <c:out value="${fn:toUpperCase(fn:substring(cloudProvider, 0, 1))}${fn:toLowerCase(fn:substring(cloudProvider, 1,fn:length(cloudProvider)))}" /><span class="fa arrow"></span></a>
	                            </li>
	                            <ul class="nav nav-third-level collapse in">
	                                <li>
	                                    <a href="<c:url value="/credentials/list/${cloudProvider}" />"><i class="fa fa-list fa-fw"></i> List<span class="fa arrow"></span></a>
	                                </li>
	                                <li>
	                                    <a href="<c:url value="/credentials/create/form/${cloudProvider}" />"><i class="fa fa-plus fa-fw"></i> Create<span class="fa arrow"></span></a>
	                                </li>
	                            </ul>
	                        </c:forEach>
	                    </ul>
	                </li>
                </c:if>
                <li>
                    <a href="#"><i class="fa fa-bar-chart-o fa-fw"></i> Virtual Machines<span class="fa arrow"></span></a>
                    <ul class="nav nav-second-level collapse in">
                        <c:forEach items="${application.cloudProviderList}" var="cloudProvider">
                            <li>
                                <a href="#"><i class="fa fa-cloud fa-fw"></i> <c:out value="${fn:toUpperCase(fn:substring(cloudProvider, 0, 1))}${fn:toLowerCase(fn:substring(cloudProvider, 1,fn:length(cloudProvider)))}" /><span class="fa arrow"></span></a>
                            </li>
                            <ul class="nav nav-third-level collapse in">
		                        <li>
		                            <a href="<c:url value="/vm/create/${cloudProvider}" />"><i class="fa fa-plus fa-fw"></i> Create<span class="fa arrow"></span></a>
		                        </li>
                            </ul>
                        </c:forEach>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</nav>