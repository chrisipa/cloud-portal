<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--@elvariable id="self" type="de.papke.cloud.portal.model.Data"--%>

<jsp:include page="header.jsp" />

<div id="wrapper">

	<jsp:include page="navigation.jsp" />

	<div id="page-wrapper">
		<div class="row">
			<h1 class="page-header">Virtual Machines</h1>
			<div class="col-lg-4">
				<form method="post" action="<c:url value="/vm/provision" />" target="output"
					role="form">
					<div class="form-group">
						<label>Cloud Provider</label> <select class="form-control"
							name="provider">
							<c:forEach items="${self.cloudProviderList}" var="cloudProvider">
								<option><c:out value="${cloudProvider}" /></option>
							</c:forEach>
						</select>
						<p class="help-block">Cloud provider to use for automated
							provisioning</p>
					</div>
					<div class="form-group">
						<label>Prefix</label> <input class="form-control" name="prefix">
						<p class="help-block">Prefix for cloud resources</p>
					</div>
					<div class="form-group">
						<label>Hostname</label> <input class="form-control"
							name="hostname">
						<p class="help-block">Hostname for virtual machine</p>
					</div>
					<div class="form-group">
						<label>SSH Public Key File</label> <input class="form-control"
							name="ssh-public-key-file">
						<p class="help-block">File with public key for SSH connections
							to virtual machine</p>
					</div>
					<div class="form-group">
						<label>SSH Private Key File</label> <input class="form-control"
							name="ssh-private-key-file">
						<p class="help-block">File with public key for SSH connections
							to virtual machine</p>
					</div>
					<div class="form-group">
						<label>Bootstrap Script File</label> <input class="form-control"
							name="bootstrap-script-file">
						<p class="help-block">Script to execute after virtual machine
							was created</p>
					</div>
					<button type="submit" class="btn btn-default">Create</button>
					<button type="reset" class="btn btn-default">Reset</button>
				</form>
			</div>
			<!-- /.col-lg-4 -->
			<div class="col-lg-8">
				<div class="form-group">
					<label>Output</label>
					<iframe name="output" id="output" frameborder="0" scrolling="yes"></iframe>
				</div>
			</div>
			<!-- /.col-lg-8 -->
		</div>
		<!-- /.row -->
	</div>
	<!-- /#page-wrapper -->

</div>
<!-- /#wrapper -->

<jsp:include page="footer.jsp" />