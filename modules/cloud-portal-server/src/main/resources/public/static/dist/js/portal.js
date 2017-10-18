$(function() {
	console.log("ready");
	$('#plan').click(function(){
	    $('#action').val("plan");
    });
	$('#apply').click(function(){
	    $('#action').val("apply");
    });
});