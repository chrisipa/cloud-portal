$(function() {
	
	$("form :button").each(function(){
		
		var button = $(this);
		
		if ($(button).attr('id') == 'cancel') {
			$(button).click(function(e){
				e.preventDefault();
				history.back();
			});
		} 
		else {
			$(button).click(function(e){
				
				e.preventDefault();
				
				var form = $(this).closest('form');
				
				var originalActionUrl = $(form).attr('action');
				
				if ($(form).validate()) {
					$(form).attr('action', originalActionUrl + "/" + $(button).attr('id'));
					$(form).submit();
					$(form).attr('action', originalActionUrl);
				}
			});
		}
	});
});