

(function($) {
    /* "use strict" */
	
 var dzChartlist = function(){
	
	var screenWidth = $(window).width();
		//let draw = Chart.controllers.line.__super__.draw; //draw shadow
		var activity = function(){
		var optionsArea = {
          series: [{
            name: "Persent",
            data: [60, 70, 80, 50, 60, 90]
          },
		  {
            name: "Visitors",
            data: [40, 50, 40, 60, 90, 90]
          }
        ],
          chart: {
          height: 300,
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
        stroke: {
          width: [3, 3, 3],
		  colors:['var(--secondary)','var(--primary)'],
		  curve: 'straight'
        },
        legend: {
			show:false,
          tooltipHoverFormatter: function(val, opts) {
            return val + ' - ' + opts.w.globals.series[opts.seriesIndex][opts.dataPointIndex] + ''
          },
		  markers: {
			fillColors:['var(--secondary)','var(--primary)'],
			width: 10,
			height: 10,
			strokeWidth: 0,
			radius: 16
		  }
        },
        markers: {
          size: [8,8],
		  strokeWidth: [4,4],
		  strokeColors: ['var(--secondary)','var(--primary)'],
		  border:2,
		  radius: 2,
		  colors:['#fff','#fff','#fff'],
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
				minWidth: 20,
				offsetX:-16,
				style: {
				  colors: '#3E4954',
				  fontSize: '14px',
				   fontFamily: 'Poppins',
				  fontWeight: 100,
				  
				},
			},
		},
		fill: {
			colors:['#fff','#fff'],
			type:'gradient',
			opacity: 1,
			gradient: {
				shade:'light',
				shadeIntensity: 1,
				colorStops: [ 
				  [
					{
					  offset: 0,
					  color: '#fff',
					  opacity: 0
					},
					{
					  offset: 0.6,
					  color: '#fff',
					  opacity: 0
					},
					{
					  offset: 100,
					  color: '#fff',
					  opacity: 0
					}
				  ],
				  [
					{
					  offset: 0,
					  color: '#fff',
					  opacity: .4
					},
					{
					  offset: 50,
					  color: '#fff',
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
		colors:['#1EA7C5','#FF9432'],
        grid: {
          borderColor: '#f1f1f1',
		  xaxis: {
            lines: {
              show: true
            }
          },
		  yaxis: {
            lines: {
              show: false
            }
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
		
		/*  const month = document.getElementById('box');
		month.addEventListener('change', activeselect);
		function activeselect(){
			console.log(box.value.split(','));
			box.value.split(',');
			//activity.series.data.series = box.value.split(',');
		
			//activity.update();
		
			
		}  */
		
		
			
		
		
	
		
		if(jQuery("#activity").length > 0){

			var dzchart = new ApexCharts(document.querySelector("#activity"), optionsArea);
			dzchart.render();
            
            jQuery('.p-static select').on('change',function(){
				if(this.value == "month"){
					dzchart.updateSeries([{
						name: "Persent",
						data: [40, 20, 10, 50, 60, 90]
					  },
					  {
						name: "Visitors",
						data: [40, 50, 10, 20, 50, 90]
					  }]
					)
				}else if(this.value == "week"){
					dzchart.updateSeries([{
						name: "Persent",
						data: [50, 30, 40, 30, 40, 70]
					  },
					  {
						name: "Visitors",
						data: [30, 40, 30, 10, 60, 80]
					  }]
					)
				}else if(this.value == "today"){
					dzchart.updateSeries([{
						name: "Persent",
						data: [30, 20, 30, 40, 50, 60]
					  },
					  {
						name: "Visitors",
						data: [20, 60, 40, 60, 10, 50]
					  }]
					)
				}
			})
			
            jQuery('#dzOldSeries').on('change',function(){
                jQuery(this).toggleClass('disabled');
                dzchart.toggleSeries('Persent');
            });
            
            jQuery('#dzNewSeries').on('change',function(){
                jQuery(this).toggleClass('disabled');
                dzchart.toggleSeries('Visitors');
            });
            
		}
      
	}
	
	var chartBarRunning = function(){
		var options  = {
			series: [
				{
					name: 'Income',
					 data: [31, 40, 28,31, 40, 28,31, 40, 28,31, 40, 28]
				}, 
				{
				  name: 'Expense',
				   data: [11, 32, 45,38, 25, 20,36, 45, 15,11, 32, 45]
				}, 
				
			],
			chart: {
			type: 'bar',
			height: 350,
			
			
			toolbar: {
				show: false,
			},
			
		},
		plotOptions: {
		  bar: {
			horizontal: false,
			endingShape:'rounded',
			columnWidth: '45%',
			borderRadius: 5,
			
		  },
		},
		colors:['#', '#77248B'],
		dataLabels: {
		  enabled: false,
		},
		markers: {
			shape: "circle",
		},
		legend: {
			show: false,
			fontSize: '12px',
			labels: {
				colors: '#000000',
				
				},
			markers: {
			width: 30,
			height: 30,
			strokeWidth: 0,
			strokeColor: '#fff',
			fillColors: undefined,
			radius: 35,	
			}
		},
		stroke: {
		  show: true,
		  width: 6,
		  colors: ['transparent']
		},
		grid: {
			borderColor: 'rgba(252, 252, 252,0.2)',
		},
		xaxis: {
		  categories: ['Jan', 'Feb', 'Mar','Apr','May','Jun','Jul','Agu', 'Sep', 'Oct','Nev','Dec'],
		  labels: {
			style: {
				colors: '#ffffff',
				fontSize: '13px',
				fontFamily: 'poppins',
				fontWeight: 100,
				cssClass: 'apexcharts-xaxis-label',
				},		
		  },
		  axisBorder: {
			show: false,
	  	 },
		  axisTicks: {
			show: false,
			borderType: 'solid',
			color: '#78909C',
			height: 6,
			offsetX: 0,
			offsetY: 0
		},
		  crosshairs: {
		  show: false,
		  }
		},
		yaxis: {
			labels: {
				offsetX:-16,
			   style: {
				  colors: '#ffffff',
				  fontSize: '13px',
				   fontFamily: 'poppins',
				  fontWeight: 100,
				  cssClass: 'apexcharts-xaxis-label',
			  },
		  },
		},
		fill: {
		  opacity: 1,
		  colors:['#ffffff', '#FFD125'],
		},
		tooltip: {
		  y: {
			formatter: function (val) {
			  return "$ " + val + " thousands"
			}
		  }
		},
		 responsive: [{
			breakpoint: 575,
			options: {
				plotOptions: {
				  bar: {
					columnWidth: '1%',
					borderRadius: -1,
				  },
				},
				chart:{
					height:250,
				},
				series: [
					{
						name: 'Projects',
						 data: [31, 40, 28,31, 40, 28,31, 40]
					}, 
					{
					  name: 'Projects',
					   data: [11, 32, 45,31, 40, 28,31, 40]
					}, 
					
				],
			}
		 }]
		};

		if(jQuery("#chartBarRunning").length > 0){

			var chart = new ApexCharts(document.querySelector("#chartBarRunning"), options);
			chart.render();
            
            jQuery('#dzIncomeSeries').on('change',function(){
                jQuery(this).toggleClass('disabled');
                chart.toggleSeries('Income');
            });
            
            jQuery('#dzExpenseSeries').on('change',function(){
                jQuery(this).toggleClass('disabled');
                chart.toggleSeries('Expense');
            });
            
		}
			
	}
	var pieChart = function(){
		var options = {
		  series: [10,20,35],
		  chart: {
		  type: 'donut',
		  height:170,
		  innerRadius: 50,  
		},
		dataLabels: {
		  enabled: false
		},
		stroke: {
		  width: 0,
		},
		plotOptions: {
			  pie: {
				 startAngle: 0, 
				  endAngle: 360,
				 donut: {
					  size: '80%',
					  labels: {
						  show:true,
						  name: {
							 
						},
						  
					  },
				 },
				 
			 },
		},
		colors:[ '#EEEEEE', '#2BC844' ,'#FD5353', 'var(--primary)'],
		legend: {
			  position: 'bottom',
			  show:false
			},
		responsive: [{
		  breakpoint: 768,
		  options: { 
		   chart: {
			  width:200
			},
		  }
		}]
		};

		var chart = new ApexCharts(document.querySelector("#pieChart"), options);
		chart.render();
	}
	var pieChart2 = function(){
		var options = {
		  series: [10,20,35],
		  chart: {
		  type: 'donut',
		  height:170,
		  innerRadius: 50,  
		},
		dataLabels: {
		  enabled: false
		},
		stroke: {
		  width: 0,
		},
		plotOptions: {
			  pie: {
				 startAngle: 0, 
				  endAngle: 360,
				 donut: {
					  size: '80%',
					  labels: {
						  show:true,
						  name: {
							 
						},
						  
					  },
				 },
				 
			 },
		},
		colors:[ '#EEEEEE', '#2BC844' ,'#FD5353', 'var(--primary)'],
		legend: {
			  position: 'bottom',
			  show:false
			},
		responsive: [{
		  breakpoint: 768,
		  options: { 
		   chart: {
			  width:200
			},
		  }
		}]
		};

		var chart = new ApexCharts(document.querySelector("#pieChart"), options);
		chart.render();
	}
	var pieChart1 = function(){
		var options = {
		  series: [10,20,35,20],
		  chart: {
		  type: 'donut',
		  height:250,
		  innerRadius: 50,  
		},
		dataLabels: {
		  enabled: false
		},
		stroke: {
		  width: 0,
		},
		plotOptions: {
			  pie: {
				 startAngle: 0, 
				  endAngle: 360,
				 donut: {
					  size: '80%',
					  labels: {
						  show:true,
						  name: {
							 
						},
						  
					  },
				 },
				 
			 },
		},
		colors:[ '#252289', '#D7D7D7' ,'#9568FF', 'var(--primary)'],
		legend: {
			  position: 'bottom',
			  show:false
			},
		responsive: [{
		  breakpoint: 768,
		  options: { 
		   chart: {
			  height:200
			},
		  }
		}]
		};

		var chart = new ApexCharts(document.querySelector("#pieChart1"), options);
		chart.render();
	}
	var pieChart2 = function(){
		var options = {
		 series: [60, 115, 155],
		 chart: {
		 type: 'donut',
		 width:130,
		 height:130,
         innerRadius: 8,   
	   },
	   dataLabels:{
		   enabled: false
	   },
	   stroke: {
		 width: 0,
	   },
	   plotOptions: {
			  pie: {
				 startAngle: 0, 
				  endAngle: 360,
				 donut: {
					  size: '80%',
					  labels: {
						  show:true,
						  name: {
							 
						},
						  
					  },
				 },
				 
			 },
		},
	   colors:['#d5dfe7', '#FF9F00', '#FD5353'],
	   legend: {
			 position: 'bottom',
			 show:false
		   },
	   responsive: [{
		 breakpoint: 1400,
		 options: {
		  chart: {
			width:120,
			 height:120
		   },
		 },
		 responsive: [{
			breakpoint: 1200,
			options: {
			  chart: {
				width: 90,
				height: 90
			  },
			  legend: {
				position: 'bottom'
			  }
			}
		  }]
		
	   }]
	   };

	   var chart = new ApexCharts(document.querySelector("#pieChart2"), options);
	   chart.render();
   }
   

	var radialchart = function(){
		  var options = {
          series: [70],
          chart: {
          type: 'radialBar',
          offsetY: 0,
		  height:250,
          sparkline: {
            enabled: true
          }
        },
        plotOptions: {
          radialBar: {
            startAngle: -135,
            endAngle: 225,
            track: {
              background: "#F1EAFF",
              strokeWidth: '100%',
              margin: 5,
            },
			
			hollow: {
              margin: 50,
              size: '65%',
              background: '#F1EAFF',
              image: undefined,
              imageOffsetX: 0,
              imageOffsetY: 0,
              position: 'front',
            },
			
            dataLabels: {
              name: {
                show: false
              },
              value: {
                offsetY: 5,
                fontSize: '12px',
				color:'#886CC0',
				fontWeight:700,
              }
            }
          }
        },
		responsive: [{
          breakpoint: 1600,
          options: {
           chart: {
			  height:250
			},
          }
        }
		
		],
        grid: {
          padding: {
            top: -10
          }
        },
		/* stroke: {
          dashArray: 4,
		  colors:'#6EC51E'
        }, */
        fill: {
          type: 'gradient',
          gradient: {
            shade: 'dark',
            type: 'horizontal',
            shadeIntensity: 0.5,
            gradientToColors: ['var(--primary)'],
            inverseColors: true,
            opacityFrom: 0.5,
            opacityTo: 1,
            stops: [0, 100]
          }
		},
		 stroke: {
          lineCap: 'round'
        },
        labels: ['Average Results'],
        };

        var chart = new ApexCharts(document.querySelector("#radialchart"), options);
        chart.render();
	
	
	}
	
	
			
			var swipercard = function() {
				var swiper = new Swiper('.mySwiper-counter', {
					speed: 1500,	
					slidesPerView: 4,
					spaceBetween: 40,
					parallax: true,
					loop:false,
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
						slidesPerView: 4,
						spaceBetween: 30,
					  },
					},
				});
			}
	
		
		
     var NewCustomers = function(){
			var options = {
					  series: [
						{
							name: 'Net Profit',
							data: [70, 150, 100, 200, 100, 150, 150,70],
						}, 				
					],
					chart: {
						type: 'area',
						height: 100,
						width: 200,
						toolbar: {
							show: false,
						},
						zoom: {
							enabled: false
						},
						sparkline: {
							enabled: true
						}
						
					},
			
					colors:['#2696FD'],
					
					dataLabels: {
					  enabled: false,
					},

				legend: {
					show: false,
				},
				stroke: {
				  show: true,
				  width: 2,
				  curve:'smooth',
				  colors:['var(--primary)'],
				},
			
				 states: {
						normal: {
							filter: {
								type: 'none',
								value: 0
							}
						},
						hover: {
							filter: {
								type: 'none',
								value: 0
							}
						},
						active: {
							allowMultipleDataPointsSelection: false,
							filter: {
								type: 'none',
								value: 0
							}
						}
				},
					xaxis: {
						categories: ['Jan', 'feb', 'Mar', 'Apr', 'May'],
						axisBorder: {
							show: false,
						},
						axisTicks: {
							show: false
						},
						labels: {
							show: false,
							style: {
								fontSize: '12px',
							}
						},
						crosshairs: {
							show: false,
							position: 'front',
							stroke: {
								width: 1,
								dashArray: 3
							}
						},
						tooltip: {
							enabled: true,
							formatter: undefined,
							offsetY: 0,
							style: {
								fontSize: '12px',
							}
						}
					},
				yaxis: {
					show: false,
				},
				fill: {
					type:'solid',
				  opacity: 0.1,
				  colors:'#2696FD'
				},
				tooltip: {
					enabled:false,
					style: {
						fontSize: '12px',
					},
					y: {
						formatter: function(val) {
							return "$" + val + " thousands"
						}
					}
				},
				responsive:[
					{
						breakpoint: 1601,
						options:{
							chart: {
								height:80,
							},
							
						}
					}
				]
			};
            var chartBar1 = new ApexCharts(document.querySelector("#NewCustomers"), options);
            chartBar1.render();
        }
	var columnChart = function(){
		var options = {
			series: [{
				name: 'Aplication Sent',
				data: [40, 55, 15,55,40, 55, 15,55,60,65,55,60,65]
			}, {
				name: 'Appllication Answered',
				data: [40, 55, 35,55,40, 55, 35,55,60,65,55,60,65]
			}, {
				name: 'Hired',
				data: [40, 17, 55, 55,40, 17, 55, 55,60,65,55,60,65]
			}],
			chart: {
				type: 'bar',
				height: 150,
				width:230,
				stacked: true,				
				offsetX: -20,
				
				toolbar: {
					show: false,
				}
			},
			responsive: [{
				breakpoint: 480,
				options: {
					legend: {
						position: 'bottom',
						offsetX: -20,
						offsetY: 0
					}
				}
			}],
			plotOptions: {
				bar: {
					horizontal: false,
					columnWidth: '30%',
					endingShape: "rounded",
					startingShape: "rounded",
					backgroundRadius: 20,
					colors: {
						backgroundBarColors: ['#ECECEC', '#ECECEC', '#ECECEC', '#ECECEC'],
						backgroundBarOpacity: 1,
						backgroundBarRadius: 3,
					},
				},
				
			},
			colors:['#28BE9D', '#28BE9D', '#28BE9D'],
			xaxis: {
				show: false,
				axisBorder: {
					show: false,
				},
				axisTicks:{
					show: false,
				},
				labels: {
					show: false,
					style: {
						colors: '#828282',
						fontSize: '14px',
						fontFamily: 'Poppins',
						fontWeight: 'light',
						cssClass: 'apexcharts-xaxis-label',
					},
				},
				
				crosshairs: {
					show: false,
				},
				/* labels : {
					 offsetX: -10,
				}, */
				
				//categories: ['Sun', 'Mon', 'Tue', 'Wed'],
			},
			yaxis: {
				show: false,
				
			},
			grid: {
				show: false,
			},
			toolbar: {
				enabled: false,
			},
			dataLabels: {
			  enabled: false
			},
			legend: {
				show:false
			},
			fill: {
				opacity: 1
			}
		};

		var chart = new ApexCharts(document.querySelector("#columnChart"), options);
		chart.render();
	}
	var columnChart1 = function(){
		var options = {
			series: [{
			  name: 'Inflation',
			  data: [10, 15, 8, 7, 12, 5,10]
			}],
			chart: {
				type: 'bar',
				height: 150,
				//stacked: true,
				toolbar: {
					show: false,
				}
			},
			plotOptions: {
				bar: {
					horizontal: false,
					columnWidth: '40%',
					borderRadius: 5,
					//backgroundRadius: 20,
					colors: {
						backgroundBarColors: ['#eee','#eee','#eee','#eee','#eee','#eee'],
						backgroundBarOpacity: 1,
						backgroundBarRadius: 5,
					},
				},
				
			},
			colors:['var(--primary)'],
			xaxis: {
				show: false,
				axisBorder: {
					show: false,
				},
				axisTicks:{
					show: false,
				},
				labels: {
					show: true,
					style: {
						colors: '#828282',
						fontSize: '14px',
						fontFamily: 'Poppins',
						fontWeight: 'light',
						cssClass: 'apexcharts-xaxis-label',
					},
				},
				
				crosshairs: {
					show: false,
				},
				
				categories: ['Sun', 'Mon', 'Tue','wed','Thu','Fri','Sat']
			},
			yaxis: {
				show: false
			},
			grid: {
				show: false,
			},
			toolbar: {
				enabled: false,
			},
			dataLabels: {
			  enabled: false,
			},
			legend: {
				show:false
			},
			fill: {
				opacity: 1
			},
			responsive: [{
				breakpoint: 480,
				options: {
					legend: {
						position: 'bottom',
						offsetX: -10,
						offsetY: 0
					}
				}
			}],
		};

		var chart = new ApexCharts(document.querySelector("#columnChart1"), options);
		chart.render();
	}
	
	var swiperreview = function() {
		var swiper = new Swiper('.mySwiper', {
			slidesPerView: 1,
			 spaceBetween: 40,
			loop:false,
			navigation: {
				nextEl: ".swiper-button-next",
				prevEl: ".swiper-button-prev",
			},
			breakpoints: {
				
			  300: {
				slidesPerView: 1,
				spaceBetween: 30,
			  },	
			  991: {
				slidesPerView: 2,
				spaceBetween: 30,
			  },
			  1200: {
				slidesPerView: 3,
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
				activity();
				NewCustomers();
				chartBarRunning();
				pieChart();
				pieChart1();
				pieChart2();
				columnChart();
				columnChart1();
				radialchart();
			
				
				swipercard();
				swiperreview();

			},
			
			resize:function(){
				chartBarRunning();
			}
		}
	
	}();

	
		
	jQuery(window).on('load',function(){
		setTimeout(function(){
			dzChartlist.load();
		}, 1000); 
		
	});

     

})(jQuery);