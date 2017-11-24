<%@page import="de.papke.cloud.portal.pojo.Menu"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="menuEntry" class="de.papke.cloud.portal.pojo.Menu" scope="request"/>

<li>
<c:if test="${menuEntry.title != null}">
	   <c:choose>
	       <c:when test="${menuEntry.path != null}">
	           <a href="<c:url value="${menuEntry.path}" />"><i class="fa ${menuEntry.icon} fa-fw"></i> <c:out value="${menuEntry.title}" /></a>
	       </c:when>
           <c:otherwise>
               <a href="#"><i class="fa ${menuEntry.icon} fa-fw"></i> <c:out value="${menuEntry.title}" /><span class="fa arrow"></span></a>
           </c:otherwise>	           
	   </c:choose>
</c:if>

<c:if test="${param.level == 2}">
    <c:set var="levelClass" value="nav-second-level" />
</c:if>
<c:if test="${param.level == 3}">
    <c:set var="levelClass" value="nav-third-level" />
</c:if>

<c:if test="${fn:length(menuEntry.menus) > 0}">
<ul class="nav ${levelClass}">
	<c:forEach items="${menuEntry.menus}" var="subMenuEntry">
	    <c:set var="menuEntry" value="${subMenuEntry}" scope="request" />
	    <jsp:include page="menu-entry.jsp">
	        <jsp:param name="level" value="${param.level + 1}" />
	    </jsp:include>    
	</c:forEach>
</ul>
</c:if>
</li>