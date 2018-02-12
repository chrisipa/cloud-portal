<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="application" type="de.papke.cloud.portal.model.ApplicationModel"--%>
<%--@elvariable id="dashboard" type="de.papke.cloud.portal.model.DashboardModel"--%>

<jsp:include page="header.jsp" />

<div id="wrapper">

    <jsp:include page="navigation.jsp" />

    <div id="page-wrapper">
        <div class="row">
            <div class="col-lg-12">
                <h4 class="page-header">Dashboard</h4>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-default" id="provisioning-history-panel">
                    <div class="panel-heading">Provisioning History</div>
                    <div class="panel-body">
                        <div class="flot-chart">
                            <div class="flot-chart-content" id="provisioning-history-chart"></div>
                        </div>
                    </div>
                </div>
            </div>   
        </div>
        <div class="row">                    
            <div class="col-lg-4">
                <div class="panel panel-default">
                    <div class="panel-heading">Provisioning Command</div>
                    <div class="panel-body">
                        <div class="flot-chart">
                            <div class="flot-chart-content" id="provisioning-command-chart"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="panel panel-default">
                    <div class="panel-heading">Cloud Provider Usage</div>
                    <div class="panel-body">
                        <div class="flot-chart">
                            <div class="flot-chart-content" id="cloud-provider-usage-chart"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <div class="panel panel-default">
                    <div class="panel-heading">Operating System Usage</div>
                    <div class="panel-body">
                        <div class="flot-chart">
                            <div class="flot-chart-content" id="operating-system-usage-chart"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp" />

<script type="text/javascript">
$(document).ready(function() {
    
	// provisioning history bar chart
	$(function() {

	    var barOptions = {
	        series: {
	            bars: {
	                show: true,
	                barWidth: 86400000
	            }
	        },
	        xaxis: {
	            mode: "time",
	            timeformat: "%d.%m",
	            tickSize: [1, "day"],
	            timezone: "browser"
	        },
	        grid: {
	            hoverable: true
	        },
	        legend: {
	            show: false
	        },
	        tooltip: true,
	        tooltipOpts: {
	            content: "x: %x, y: %y"
	        }
	    };
	    var barData = {
	        label: "bar",
	        data: [
	        	<c:forEach var="entry" items="${dashboard.provisioningHistory}" varStatus="count">
	               <c:out escapeXml="false" value="[ ${entry.key}, ${entry.value} ]" /><c:if test="${count.index < fn:length(dashboard.provisioningHistory) - 1 }"><c:out value="," /></c:if>
	            </c:forEach>
	        ]
	    };
	    $.plot($("#provisioning-history-chart"), [barData], barOptions);

	});
	
    // provisioning type pie chart
    $(function() {
    
        var data = [
        	<c:forEach var="entry" items="${dashboard.provisioningCommand}" varStatus="count">
        	   <c:out escapeXml="false" value="{ label: '${entry.key}', data: ${entry.value} }" /><c:if test="${count.index < fn:length(dashboard.provisioningCommand) - 1 }"><c:out value="," /></c:if>
        	</c:forEach>
        ];
    
        var plotObj = $.plot($("#provisioning-command-chart"), data, {
            series: {
                pie: {
                    show: true
                }
            },
            grid: {
                hoverable: true
            },
            tooltip: true,
            tooltipOpts: {
                content: "%p.0%, %s", 
                shifts: {
                    x: 20,
                    y: 0
                },
                defaultTheme: false
            }
        });
    });
	
    // cloud provider usage pie chart
    $(function() {
    
        var data = [
        	<c:forEach var="entry" items="${dashboard.cloudProviderUsage}" varStatus="count">
        	   <c:out escapeXml="false" value="{ label: '${entry.key}', data: ${entry.value} }" /><c:if test="${count.index < fn:length(dashboard.cloudProviderUsage) - 1 }"><c:out value="," /></c:if>
        	</c:forEach>
        ];
    
        var plotObj = $.plot($("#cloud-provider-usage-chart"), data, {
            series: {
                pie: {
                    show: true
                }
            },
            grid: {
                hoverable: true
            },
            tooltip: true,
            tooltipOpts: {
                content: "%p.0%, %s", 
                shifts: {
                    x: 20,
                    y: 0
                },
                defaultTheme: false
            }
        });
    });
    
    // operating system usage pie chart
    $(function() {
    
        var data = [
        	<c:forEach var="entry" items="${dashboard.operatingSystemUsage}" varStatus="count">
        	   <c:out escapeXml="false" value="{ label: '${entry.key}', data: ${entry.value} }" /><c:if test="${count.index < fn:length(dashboard.operatingSystemUsage) - 1 }"><c:out value="," /></c:if>
        	</c:forEach>
        ];
    
        var plotObj = $.plot($("#operating-system-usage-chart"), data, {
            series: {
                pie: {
                    show: true
                }
            },
            grid: {
                hoverable: true
            },
            tooltip: true,
            tooltipOpts: {
                content: "%p.0%, %s",
                shifts: {
                    x: 20,
                    y: 0
                },
                defaultTheme: false
            }
        });
    });
});
</script>