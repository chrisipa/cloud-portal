<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--@elvariable id="self" type="de.papke.cloud.portal.model.Data"--%>

<jsp:include page="header.jsp" />

<div id="wrapper">

    <jsp:include page="navigation.jsp" />

    <div id="page-wrapper">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Virtual Machines</h1>
                <form method="post" action="<c:url value="/vm/provision" />" role="form">
                    <div class="form-group">
                        <label>Cloud Provider</label>
                        <select class="form-control" name="provider">
                            <c:forEach items="${self.cloudProviderList}" var="cloudProvider">
                                <option><c:out value="${cloudProvider}" /></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Prefix</label>
                        <input class="form-control" name="prefix">
                        <p class="help-block">Prefix for cloud resources</p>
                    </div>
                    <div class="form-group">
                        <label>Hostname</label>
                        <input class="form-control" name="hostname">
                        <p class="help-block">Hostname for virtual machine</p>
                    </div>
                    <div class="form-group">
                        <label>SSH Public Key</label>
                        <textarea class="form-control" rows="3" name="ssh-public-key"></textarea>
                    </div>
                    <div class="form-group">
                        <label>SSH Private Key</label>
                        <textarea class="form-control" rows="3" name="ssh-private-key"></textarea>
                    </div>
                    <div class="form-group">
                        <label>Bootstrap Script</label>
                        <textarea class="form-control" rows="3" name="bootstrap-script"></textarea>
                    </div>
                    <button type="submit" class="btn btn-default">Create</button>
                    <button type="reset" class="btn btn-default">Reset</button>
                </form>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
    </div>
    <!-- /#page-wrapper -->

</div>
<!-- /#wrapper -->

<jsp:include page="footer.jsp" />