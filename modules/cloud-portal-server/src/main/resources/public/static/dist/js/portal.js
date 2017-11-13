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
				
				var buttonId = $(button).attr('id');
				
				if (typeof buttonId !== 'undefined') {
					
					e.preventDefault();
					var form = $(this).closest('form');
					var originalActionUrl = $(form).attr('action');
					
					if ($(form).valid()) {
						
						if (buttonId == "plan" || buttonId == "apply") {
							$('#myModal').modal('toggle');
						}
						
						var submit = true;
						
						if (buttonId.startsWith('delete')) {
							submit = confirm('Do you really want to delete this item?');
						}
							
						if (submit) {
							$(form).attr('action', originalActionUrl + "/" + buttonId);
							$(form).submit();
							$(form).attr('action', originalActionUrl);
						}
					}
				}
			});
		}
	});
	
	$('#datatable').DataTable({
        responsive: true,
        order: [[ 0, 'desc' ]]
    });
});