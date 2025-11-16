

(function($) {
    /* "use strict" ¸ü¶àÏÂÔØ£ºhttp://www.bootstrapmb.com */
	
 var dzChartlist = function(){
	
	var donutChart2 = function(){
		$("span.donut3").peity("donut", {
			width: "150",
			height: "150"
		})
	}
	
	
	
	/* Function ============ */
		return {
			init:function(){
            },
			
			
			load:function(){
                donutChart2();
			},
			
			resize:function(){
			}
		}
	
	}();

	
		
	jQuery(window).on('load',function(){
		setTimeout(function(){
			dzChartlist.load();
		}, 1000); 
		
	});

     

})(jQuery);