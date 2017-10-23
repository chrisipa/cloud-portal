$(function() {
	
	$("form :button").each(function(){
		
		var button = $(this);
		var form = $(this).closest('form');
		
		$(button).click(function(e){
			
			e.preventDefault();
			
			var originalActionUrl = $(form).attr('action');
			
			if ($(form).validate()) {
				$(form).attr('action', originalActionUrl + "/" + $(button).attr('id'));
				$(form).submit();
				$(form).attr('action', originalActionUrl);
			}
		});
	});
	
	$('#cancel').click(function(){
	    history.back();
    });
});