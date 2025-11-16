

(function($) {
    /* "use strict" */
	
 var dzChartlist = function(){
	
	var screenWidth = $(window).width();
	//let draw = Chart.controllers.line.__super__.draw; //draw shadow
    var activity1 = function(){
		var optionsArea = {
          series: [{
            name: "",
            data: [1400, 800, 1200, 550, 1550, 600, 1250]
          },
		  {
            name: "",
            data: [500, 600, 300, 1200, 1200, 800, 1400]
          }
        ],
          chart: {
          height: 370,
          type: 'area',
		  group: 'social',
		  toolbar: {
            show: false
          },
          zoom: {
            enabled: false
          },
        },
        dataLabels: {
          enabled: false
        },
		
        legend: {
			show:false,
          tooltipHoverFormatter: function(val, opts) {
            return val + ' - ' + opts.w.globals.series[opts.seriesIndex][opts.dataPointIndex] + ''
          },
		  markers: {
			fillColors:['var(--secondary)','var(--primary)'],
			width: 3,
			height: 16,
			strokeWidth: 0,
			radius: 16
		  }
        },
        markers: {
          size: [8,0],
		  strokeWidth: [4,0],
		  strokeColors: ['#fff','#fff'],
		  border:4,
		  radius: 4,
		  colors:['#2A353A','#2A353A','#fff'],
          hover: {
            size: 10,
          }
        },
        xaxis: {
          categories: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
		  labels: {
		   style: {
			  colors: '#3E4954',
			  fontSize: '14px',
			   fontFamily: 'Poppins',
			  fontWeight: 100,
			  
			},
		  },
		  axisBorder:{
			  show: false,
		  }
        },
		yaxis: {
			labels: {
				show: true,
				align: 'right',
				minWidth: 15,
				offsetX:-16,
				style: {
				  colors: '#666666',
				  fontSize: '14px',
				   fontFamily: 'Poppins',
				  fontWeight: 100,
				  
				},
			},
		},
		fill: {
			colors:['#fff','#FF9432'],
			type:'gradient',
			opacity: 1,
			gradient: {
				shade:'light',
				shadeIntensity: 1,
				colorStops: [ 
				  [
					{
					  offset: 0,
					  color: 'var(--secondary)',
					  opacity: 0.4
					},
					{
					  offset: 0.6,
					  color: 'var(--secondary)',
					  opacity: 0.25
					},
					{
					  offset: 100,
					  color: 'var(--secondary)',
					  opacity: 0
					}
				  ],
				  [
					{
					  offset: 0,
					  color: 'var(--primary)',
					  opacity: .4
					},
					{
					  offset: 50,
					  color: 'var(--primary)',
					  opacity: 0.25
					},
					{
					  offset: 100,
					  color: '#fff',
					  opacity: 0
					}
				  ]
				]

		  },
		},
		colors:['var(--secondary)','var(--primary)'],
		stroke:{
			curve : "straight",
			 width: 3,
		},
        grid: {
          borderColor: '#e1dede',
		  strokeDashArray:8,
		  
			xaxis: {
				lines: {
				show: true,
				opacity: 0.5,
				}
			},
			yaxis: {
				lines: {
				show: true,
				opacity: 0.5,
				}
			},
			row: {
				colors: undefined,
				opacity: 0.5
			},  
			column: {
				colors: undefined,
				opacity: 0.5
			},  
        },
		 responsive: [{
			breakpoint: 1602,
			options: {
				markers: {
					 size: [6,6,4],
					 hover: {
						size: 7,
					  }
				},chart: {
				height: 230,
			},	
			},
			
		 }]
        };
		var chartArea = new ApexCharts(document.querySelector("#activity1"), optionsArea);
        chartArea.render();

	}
	var swipercard = function() {
		var swiper = new Swiper('.crypto-Swiper', {
			speed: 1500,	
			slidesPerView: 4,
			spaceBetween: 40,
			parallax: true,
			loop:false,
			
			autoplay: {
				delay: 1000,
			 },
			breakpoints: {
				
			  300: {
				slidesPerView: 1,
				spaceBetween: 30,
			  },	
			  576: {
				slidesPerView: 2,
				spaceBetween: 30,
			  },
			  991: {
				slidesPerView: 3,
				spaceBetween: 30,
			  },
			  1200: {
				slidesPerView: 3,
				spaceBetween: 30,
			  },
			  1600: {
				slidesPerView: 4,
				spaceBetween: 30,
			  },
			},
		});
	}
	
	/* Function ============ */
		return {
			init:function(){
			},
			
			
			load:function(){
				activity1();
				swipercard();
				
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