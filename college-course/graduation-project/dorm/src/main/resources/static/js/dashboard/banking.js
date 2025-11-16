

(function($) {
    /* "use strict" */
	
 var dzChartlist = function(){
	
	var screenWidth = $(window).width();
	//let draw = Chart.controllers.line.__super__.draw; //draw shadow
			var barChart = function(){
				var optionsTimeline = {
				  chart: {
					type: "bar",
					height: 200,
					stacked: true,
					toolbar: {
					  show: false
					},
					sparkline: {
					  //enabled: true
					},
					offsetX:0,
				  },
				  series: [
					 {
					  name: "New Clients",
					  data: [50, 100, 80, 50, 100, 130, 150 ]
					}
				  ],
				  
				  plotOptions: {
					bar: {
					  columnWidth: "15%",
					  endingShape: "rounded",
					  startingShape: "rounded",
					   borderRadius: 5,
					  
					  colors: {
						backgroundBarColors: ['#ffffff', '#ffffff', '#ffffff', '#ffffff','#ffffff','#ffffff'],
						backgroundBarOpacity: 1,
						backgroundBarRadius: 5,
					  },
			
					},
					distributed: true
				  },
				  colors:['#FF4646','#ffff','#FF4646','#ffff','#FF4646','#ffff'],
				  grid: {
					show:false,
					borderColor:'#f'
				  },
				  legend: {
					show: false
				  },
				  fill: {
					opacity: 1
				  },
				  dataLabels: {
					enabled: false,
					colors: ['#000'],
					dropShadow: {
					  enabled: true,
					  top: 1,
					  left: 1,
					  blur: 1,
					  opacity: 1
					}
				  },
				  xaxis: {
				   categories: ['Mon', 'Tue', 'Web', 'Thu', 'Fri', 'Sat', 'Sun'],	
				   labels: {
						style: {
						colors: '#666666',
						fontSize: '14px',
						fontFamily: 'poppins',
						fontWeight: 500,
						cssClass: 'apexcharts-xaxis-label',
						},
					},
					crosshairs: {
					show: false,
					},
					axisBorder: {
					  show: true,
					},
					axisTicks: {
						show: false,
						borderType: 'solid',
						color: '#78909C',
						height: 6,
						offsetX: 0,
						offsetY: 0
					},
				  },
				  stroke:{
					 color:'green',
					 show: true, 
					 curve: 'smooth',
					  lineCap: 'round',
					   width: 0,
				   },
				  yaxis: {
					show: false
				  },
				  
				  tooltip: {
					x: {
					  show: true
					}
				  }
				};
				var chartTimelineRender =  new ApexCharts(document.querySelector("#barChart"), optionsTimeline);
				 chartTimelineRender.render();  
			}
		
			var activity = function(){
				var optionsArea = {
				  series: [{
					name: "Persent",
					data: [60, 70, 80, 50, 60, 50, 90]
				  },
				  {
					name: "Visitors",
					data: [40, 50, 40, 60, 90, 70, 90]
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
					width: 16,
					height: 16,
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
				if(jQuery("#activity").length > 0){
		
					var dzchart = new ApexCharts(document.querySelector("#activity"), optionsArea);
					dzchart.render();
					
					jQuery('#dzNewSeries').on('change',function(){
						jQuery(this).toggleClass('disabled');
						dzchart.toggleSeries('Visitors');
					});
					
					jQuery('#dzOldSeries').on('change',function(){
						jQuery(this).toggleClass('disabled');
						dzchart.toggleSeries('Persent');
					});
					
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
				}
			  
			}	
			var activity1 = function(){
				var optionsArea = {
				  series: [{
					name: "Persent",
					data: [60, 70, 80, 50, 60, 50, 90]
				  },
				  {
					name: "Visitors",
					data: [40, 50, 40, 60, 90, 70, 90]
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
					width: 16,
					height: 16,
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
				if(jQuery("#activity1").length > 0){
		
					var dzchart = new ApexCharts(document.querySelector("#activity1"), optionsArea);
					dzchart.render();
					
					jQuery('#dzNewSeries').on('change',function(){
						jQuery(this).toggleClass('disabled');
						dzchart.toggleSeries('Persent');
					});
					
					jQuery('#dzOldSeries').on('change',function(){
						jQuery(this).toggleClass('disabled');
						dzchart.toggleSeries('Visitors');
					});
					
				}
			  
			}
 
	/* Function ============ */
		return {
			init:function(){
			},
			
			
			load:function(){
				barChart();
				activity();	
				activity1();
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