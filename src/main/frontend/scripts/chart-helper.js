/**
 *	author: lzhang
 *	date: 2017-4-5
 *	desc: 统一图表绘制参数 888
 */
(function($, H) {
	this.ChartHelper = {
		lang: 'en',
		_initHC: function(config, params) {
			// 初始化HC
			var _default = {
				lang: {
					//小数点
					decimalPoint: '.',
					//千分号
					thousandsSep: ',',
					//国际单位符
					numericSymbols: [ "k" , "M" , "B" , "T" , "P" , "E"]
				},
				//版权信息
				credits:{
					enabled: !1
				},
				//全局参数             
				global:{
					useUTC: false
				},
				tooltip: {
				  //split:true,//tooltip分开显示
                  enabled: true,//是否显示提示框
                  hideDelay:500//隐藏时间默认是500ms
                },
				//数据列配置
				plotOptions: {
				column:{
	                //柱状图组间距
	                pointPadding: 0,
	                borderWidth: 0
	            },
			    series:{
		            connectNulls: true,// 是否连接空值点
		            lineWidth: 1.5,
		            marker: {
		                radius: 2 //数据点的半径大小
		            },
		            events: {
		                mouseOver: function () {
		                    (function(that) {
		                        clearTimeout(that.chart.timer);
		                        that.chart.timer = setTimeout(function() {
		                            if(that.chart.tooltip.options.borderColor != that.color) {
		                                that.chart.update({
		                                    tooltip: {
		                                        borderColor: that.color
		                                    }
		                                })
		                            }
		                        }, 100);
		                    })(this);
			               }
			            }
	                }
				},
				//设置图例分页样式
				legend:{
					navigation:{
						activeColor:"#7985a3",
						style:{
							color:"#7985a3"
						}
					}
				}
			};
			_default = config? $.extend(true, {}, _default, config): _default;
			H.setOptions(_default);
			// 执行回调
			if(params && typeof params === 'function') {
				params();
			} else {
				this.lang = params;
			}
		},
		_copy: function(obj){
			// 对象复制
			return JSON.parse(JSON.stringify(obj));
		},
		getTableConfig: function(chart, config) {
			// 获取表格配置参数
			config = config || { width: 400, height: 300 };
			chart  = chart  || {};
			// 表格数据
			if (chart.dataTable.data) {
				var data = chart.dataTable && chart.dataTable.data || {};
			}else if (chart.dataTable.table) {
				var data = chart.dataTable && chart.dataTable.table && chart.dataTable.table.data || {};
			}else {
				var data = {};
			}

			if (_.isEmpty(data)) {
				throw 'data is null';
			}

			// 表格列数
			let colNum = data && data.rows
				&& data.rows[0]
				&& data.rows[0].length;

			if(colNum && (data.rows[0][0] == 'date' || data.rows[0][0] == 'datetime')) {
				data.rows[0][0] = '日期';
			}

			//根据容器宽度显示列数
//			let columnWidth = 60,
//				containerWidth = config.width,
//				calcColNum = Math.floor(containerWidth/columnWidth);
//			colNum = calcColNum < colNum ? calcColNum:colNum;
//			config.columns = [];
//			for (var i = 0; i < colNum; i++) {
//				config.columns.push({});
//			}
            if (!config.mode || config.mode != 'fullscreen') {
				var columnWidth = 60,
					containerWidth = config.width,
					calcColNum = Math.floor(containerWidth/columnWidth);
				colNum = calcColNum < colNum ? calcColNum:colNum;
				config.columns = [];
				for (var i = 0; i < colNum; i++) {
					config.columns.push({});
				}
			}

			//时间戳格式化
			if ((data.rows[0][0] == '日期' || data.rows[0][0] == '时间') && data.rows.length > 1 && $.isNumeric(data.rows[1][0])){
				config.columns[0] = {
					renderer: this._formatDate
				};
			}

			//根据容器高度显示行数
			let _data = [];
			if(data && data.rows) {
				if(!config.mode || config.mode != 'fullscreen'){
					_data = data.rows.slice(0, Math.floor(config.height / 30));//行高30
				} else {
					_data = data.rows;
				}
			}
			let _default = {
				data: _data,
				width: config.width,
				height: config.height,
				fixedRowsTop: config.fixedRowsTop,
				disableVisualSelection: config.disableVisualSelection,
				colWidths: (config.width / colNum),
				manualColumnResize: true,
				readOnly: true,
				wordWrap: false
			}
			return $.extend(true, {}, _default, config);
		},
		//格式化时间
		_formatDate: function(instance, td, row, col, prop, value, cellProperties){
            if($.isNumeric(value)){
                var date = new Date(parseInt(value));
              td.innerHTML = [date.getFullYear(),date.getMonth()+1,date.getDate()].join('-').replace(/(?=\b\d\b)/g, '0');
            }else{
            	//td千分位转化
                td.innerHTML = value.toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,');
            }
            return td;
        },
		getLiveChartConfigV2: function(chart, config){
			config = config || { mapData: [] };
			chart  = chart  || { options: {} };
			var self = this;
			if(chart.options && chart.options.chart) {
				chart.options.chart.events = {
					load: function() {
						if(config.onload && typeof config.onload == 'function'){
							config.onload();
						}
					}
				};
				chart.options.chart.width  = config.width;
				chart.options.chart.height = config.height;
				chart.options.chart.backgroundColor = config.backgroundColor || '#fff';
			}else {
				chart.options.chart = {
					events:{
						load: function(){
							if(config.onload && typeof config.onload == 'function'){
								config.onload();
							}
						}
					},
					width: config.width,
					height: config.height,
					backgroundColor: config.backgroundColor || '#fff'
				}
			}
			//图表数据源设置
			//数据时间顺序处理
			if(chart.dataTable && chart.dataTable.data && chart.dataTable.data.rows){
				chart.dataTable.data.rows = this.getSortedDateTable(chart.dataTable.data.rows, false);
			}
			if(chart.dataTable && chart.dataTable.data && chart.dataTable.data.columns){
				chart.dataTable.data.colunms = this.getSortedDateTable(chart.dataTable.data.colunms, true);
			}
			let data  = chart.dataTable && chart.dataTable.data;
			let array = null;
			let _type = '';
			//线图取样，起始点和末尾点不取样
			if(chart.options.chart.type == 'line' && (!config.mode || config.mode != 'fullscreen') && data && data.rows && data.rows.length > 500){
				array = data.rows.filter(function(curValue, index, arr) {
					return index == 0 ? true : index == arr.length-1 ? true : index % 2 == 0;
				});
			}
			// 统一调整线宽
			H.getOptions().plotOptions.line.lineWidth = 1;
			H.getOptions().plotOptions.column.borderColor = config.borderColor || '#fff';
			H.getOptions().tooltip.shared = true;
			// 定制column堆叠总量
			if (chart.options.plotOptions && chart.options.plotOptions.series && chart.options.plotOptions.series.stacking) {
				if (chart.options.tooltip){
					if(!chart.options.tooltip.formatter){
						chart.options.tooltip.formatter = function () {
							var s = '<b>' + this.points[0].key + '</b>';
							$.each(this.points, function () {
								s += '<br/><span style="color:' + this.color + '">\u25CF</span>' + this.series.name + ': ' +
									this.y.toFixed(2);
							});
							if (this.points.length > 1) {
								s += '<br/><span style="color:#000">\u25CF</span>' + (self.lang === 'en' ? 'total:' : '总计:') + this.points[0].total.toFixed(2);
							}
							return s;
						}
					}
				}else{
					chart.options.tooltip = {
						formatter: function () {
							var s = '<b>' + this.points[0].key + '</b>';
							var arr = new Array();
							var s_ = s;
							var temp;
							$.each(this.points, function () {
								s_ = '<br/><span style="color:' + this.color + '">\u25CF</span>' + this.series.name + ':' +
									this.y.toFixed(2);
									arr.push(s_);
							});
                             if (arr.length != 1) {
                             	arr[arr.length-1] =  arr[arr.length-1] + '%';
                             };
 							 if (this.points.length > 2) {
                                 s_='<br/><span style="color:#000">\u25CF</span>' + (self.lang === 'en' ? 'total:' : '总计:') + this.points[0].total;
							 	arr.push(s_);
							 	temp = arr[arr.length-1];
								arr[arr.length-1]=arr[arr.length-2];
								arr[arr.length-2] = temp
							 }
							return arr;
						}
					}
				}
			}

			// 统一增加各类图表响应式
			chart.options.responsive =  {
				rules: [{
					condition: {
						maxHeight: 255,
					},
					chartOptions: {
						legend:{
							//禁用
							enabled: false
						}
					}
				}, {
					condition: {
						maxWidth: 300,
						minHeight: 255
					},
					chartOptions:{
						legend: {
							padding: 3,
							useHTML: true,
							itemStyle:{
								width:'150px',
								overflow: "hidden",
								textOverflow: 'ellipsis',
								whiteSpace: 'nowrap'
							}
						}
					}
				},{
					condition: {
						minWidth: 301,
						maxWidth: 401,
						minHeight: 255
					},
					chartOptions: {
						legend: {
							padding: 3,
							useHTML: true,
							itemStyle:{
								width:'250px',
								overflow: "hidden",
								textOverflow: 'ellipsis',
								whiteSpace: 'nowrap'
							}
						}
					}
				},{
					condition: {
						minWidth: 401,
						minHeight: 256
					},
					chartOptions:{
						legend: {
							layout: 'horizontal',
							align: 'center',
							verticalAlign: 'bottom',
							padding: 5
						}
					}
				}]
			}

			// 设置图表标题
			chart.options.title = {
				margin: 0,
				text: 'chart',
				style:{
					fontSize: '14px',
					visibility: 'hidden'
				},
			}
			// 隐藏导出按钮
			chart.options.exporting = { enabled:false };
			// 设置y轴样式
			if(chart.options.yAxis instanceof Array) {
				chart.options.yAxis[0].title = { text: null};
				chart.options.yAxis[0].lineColor = config.yAxis?config.yAxis.lineColor :'#eee';
				chart.options.yAxis[0].opposite = false;
				chart.options.yAxis[0].gridLineColor = config.yAxis?config.yAxis.gridLineColor :'#fff';
				chart.options.yAxis[0].labels = $.extend(chart.options.yAxis[0].labels || {}, {
					style:{
						color: '#7985a3'
					}
				})
				if(chart.options.yAxis[1]){
					chart.options.yAxis[1].title = { text: null};
					chart.options.yAxis[1].lineColor = config.yAxis?config.yAxis.lineColor :'#eee';
					chart.options.yAxis[1].opposite = true;
					chart.options.yAxis[1].gridLineColor = config.yAxis?config.yAxis.gridLineColor :'#fff';
					chart.options.yAxis[1].labels = $.extend(chart.options.yAxis[1].labels || {}, {
						style:{
							color: '#7985a3'
						}
					})
				}else{
					chart.options.yAxis.push({
						title: {
							text: null,
						},
						opposite: true,
						lineColor: config.yAxis?config.yAxis.lineColor :'#eee',
						gridLineColor: config.yAxis?config.yAxis.gridLineColor :'#fff',
						labels:{
							style:{
								color: '#7985a3'
							}
						}
					})
				}
			}else{
				chart.options.yAxis = [{
					title:{
						text:null
					},
					opposite: false,
					lineColor: config.yAxis?config.yAxis.lineColor :'#eee',
					gridLineColor: config.yAxis?config.yAxis.gridLineColor :'#fff',
					labels:{
						style:{
							color: '#7985a3'
						}
					}
				},{
					title: {
						text: null,
					},
					opposite: true,
					lineColor: config.yAxis?config.yAxis.lineColor :'#eee',
					gridLineColor: config.yAxis?config.yAxis.gridLineColor :'#fff',
					labels:{
						style:{
							color: '#7985a3'
						}
					}
				}]
			}

			// 设置x轴样式
			if(chart.options.xAxis){
				chart.options.xAxis.lineColor = config.xAxis?config.xAxis.gridLineColor :'#eee';
				chart.options.xAxis.gridLineColor = config.xAxis?config.xAxis.gridLineColor :'#fff',
				chart.options.xAxis.labels = $.extend(chart.options.xAxis.labels, {
					style:{
						color: '#7985a3'
					}
				})
			}else{
				chart.options.xAxis = {
					lineColor: config.xAxis?config.xAxis.gridLineColor :'#eee',
					gridLineColor: config.xAxis?config.xAxis.gridLineColor :'#fff',
					labels:{
						style:{
							color: '#7985a3'
						}
					}
				}
			}

			// 设置图例样式
			if(chart.options.legend){
				chart.options.legend.itemStyle = {
					color: '#7985a3',
					fontWeight: 'normal'
				}
			}else{
				chart.options.legend = {
					//华住看板俩个图表不显示legend
					enabled:false
					// itemStyle: {
					// 	color: '#7985a3',
					// 	fontWeight: 'normal'
					// }
				}
			}

			// 设置map Series
			if(chart.options.chart.type
				&& chart.options.chart.type.indexOf('map') > -1
				&& chart.options.series
				|| (chart.options.series && chart.options.series.some((s)=>{
						return s.type && s.type.indexOf('map') > -1
					}))
				){
				_type = 'Map';
				delete chart.options.xAxis;
				delete chart.options.yAxis;
				delete chart.options.responsive
				// map类型chart
				for (var i = 0,j = chart.options.series.length; i < j; i++) {
					delete chart.options.data;
					if(chart.options.series[i].name === 'Basemap'){
						// 基础地图
						chart.options.series[i].mapData = H.maps["countries/cn/custom/"+ this.lang +"-all-sar-taiwan"] || [];
					}
					if(chart.options.series[i].type === 'mapbubble'){
						// 气泡地图
						chart.options.series[i].data = chart.dataTable.data.rows;
						let sum = 0;
						chart.dataTable.data.rows.map((d)=>{
							sum += d.z
						})
						if(chart.options.tooltip){
							if (this.lang == 'cn') {
								chart.options.tooltip.formatter = function () {
									return this.point.city + '<br>'+ this.point.cn_name +":" + this.point.z.toFixed(2) + '<br>占比: ' + parseInt(this.point.z/sum*10000) /100 + '%' ;
								}
							}else if (this.lang == 'en') {
								chart.options.tooltip.formatter = function () {
									return this.point.city + '<br>'+ this.point.en_name +":" + this.point.z.toFixed(2) + '<br>Weight: ' + parseInt(this.point.z/sum*10000) /100 + '%' ;
								}
							}
						}else{
							if (this.lang == 'cn') {
								chart.options.tooltip = {
									formatter: function () {
										return this.point.city + '<br>'+ this.point.cn_name +":" + this.point.z.toFixed(2) + '<br>占比: ' + parseInt(this.point.z/sum*10000) /100 + '%' ;
									}
								}
							}else if (this.lang == 'en'){
								chart.options.tooltip = {
									formatter: function () {
										return this.point.city + '<br>'+ this.point.en_name +":"  + this.point.z.toFixed(2) + '<br>Weight: ' + parseInt(this.point.z/sum*10000) /100 + '%' ;
									}
								}
							}
						}
					}
					// 矩阵图
					if( chart.options.series[i].type === 'treemap'){
						// 恢复使用默认绘图函数
						_type = '';
						delete chart.options.chart.type;
						let colorNum = chart.dataTable.data.rows.length;
						chart.options.series[i].data = chart.dataTable.data.rows.sort((a,b)=>{
							if (_.isNumber(b.diff) && _.isNumber(a.diff)) {
								return b.diff - a.diff;
							} else {
								return b.value - a.value;
							}
						}).map((data,i)=>{
							let brighten = i/colorNum;
							data.color = H.Color('#2B70C7').brighten(brighten).get();
							return data;
						});	
						// 矩阵图tooltip增加diff百分比;
						var _format_data = chart.options.series[i].data;
						chart.options.tooltip.formatter = function() {
							let _diff = 0;
							_format_data.some((data,i)=>{
								if(data.name === this.key){
									_diff = data.diff;
									return true;
								}
							});
							if(typeof _diff === 'undefined'){
								return this.key + " : " + this.point.value.toFixed(2);
							}else{
								//针对value=1的图单独处理显示
								if(!this.point.dau){
									return (self.lang === 'en' ?'Change d/d : ':'每日变化率 : ') + Math.round(_diff*100)/100 + "%<br/>" + this.key + " : " + (this.point.value).toFixed(2);
								}else{
									return (self.lang === 'en' ?'Change d/d : ':'每日变化率 : ') + Math.round(_diff*100)/100 + "%<br/>" + this.key + " : " + (this.point.dau).toFixed(2);
								}
							}
						}
					}
					// 热力图
					if( chart.options.series[i].hasOwnProperty('joinBy')){
						chart.options.series[i].data = chart.dataTable.data.rows;
						let isWorld = chart.options.chart.type === 'worldmap' || (chart.options.series[i].mapData && chart.options.series[i].mapData.title.indexOf('World')>-1);
						chart.options.series[i].mapData = isWorld ? H.maps["custom/world_"+this.lang] : H.maps["countries/cn/custom/"+ this.lang +"-all-sar-taiwan"];
						if(isWorld){
							chart.options.chart.type = 'map';
						}
						chart.options.legend.enabled = false;
						if(config && config.mode === 'fullscreen' && chart.options.series[i].dataLabels){
							chart.options.series[i].dataLabels.enabled = true;
						}
						if(!chart.options.tooltip){
							chart.options.tooltip = {};
						}
						//函数formatter
						chart.options.tooltip.formatter = function(){
							//格式化小数为两位
							if(String(this.point.value).indexOf(".")>-1){
								return '<span style="color:'  + this.point.color +'">●</span> <span style="font-size: 0.85em"> '+ this.series.name + "</span><br/>" + "<br/>" + this.key + " : " + this.point.value.toFixed(2);
							}else{
								return '<span style="color:'  + this.point.color +'">●</span> <span style="font-size: 0.85em"> ' + this.series.name + "</span><br/>" + "<br/>" + this.key + " : " + this.point.value;
								
							}
						}
					}
				}
			}else {
				//清除data(避免干扰雷达图绘制)
				delete chart.options.data;
				//获取图表类型
				var _chartType = this._getChartType(chart.options);
				//校验，取样，转换数据
				chart.options.series = this._transformChartData(_chartType, chart, config);
				// 饼图数据合并替换
				if(chart.chartType === 'pie' || chart.options.chart.type == 'pie'){
					// 检测是否取前n位,剩余显示为其他
					if(chart.topn){
						var pie_limit = chart.topn,
						// 数值总合
						pie_total = 0;
						array = JSON.parse(JSON.stringify(chart.dataTable.data.rows))
						if(chart.type == 'pdf_live_chart'){
							// pdf解析格式饼图
							var pie_data = array.slice(1);
							tmp_data = pie_data.sort(function(a,b){return b[1] - a[1];});
							pie_data = tmp_data.filter(function(series, index){
								pie_total += series[1];
								return index < pie_limit
							})
							pie_data.map(function(series,index){
								pie_total -= series[1]
							})
							if (this.lang == 'cn') {
								pie_data.push(['其他',pie_total]);
							}else {
								pie_data.push(['Other',pie_total]);
							}
							// 填充第一行空数据
							pie_data.unshift(['',''])
							array = pie_data
						}else{
							// 通用数据格式饼图
							var pie_data = array[0].map(function(key,index){
								if(index !== 0){
									pie_total += array[1][index];
								}
								return {
											name:key,
											y: array[1][index]
										}
							}).sort(function(a,b){return b.y - a.y})
							pie_data.unshift(pie_data.pop())
							// 清空array填充最终数据
							array = [
								[],
								[]
							]
							//饼图数据
							pie_data.map(function(item,index){
								if(index > pie_limit)return false;
								if(index !== 0){
									pie_total -= item.y;
								}
								array[0][index] = item.name;
								array[1][index] = !isNaN(item.y)?parseInt(item.y*100)/100:'值';
							})
							if (this.lang == 'cn') {
								array[0].push('其他');
							}else {
								array[0].push('Other');
							}
							array[1].push(parseInt(pie_total*100)/100);
						}
					}
					// 统一隐藏饼图标签
					if(chart.options.plotOptions && chart.options.plotOptions.pie){
						chart.options.plotOptions.pie.dataLabels = {
							enabled: false
						};
						chart.options.plotOptions.pie.showInLegend = true;
						chart.options.plotOptions.pie.borderColor = '#fff';
					}else{
						chart.options.plotOptions.pie = {
							dataLabels:{
								enabled: false
							},
							showInLegend: true,
							borderColor: '#fff'
						}
					}
					// 统一修改饼图lengend
					if(chart.options.legend){
						chart.options.legend.enabled = true;
						chart.options.legend.padding = 3;
						chart.options.legend.labelFormatter = function() {
							if(this.name.slice(-1) === '%'){
								return this.name;
							}
							return this.name + ': ' + (Math.round(this.percentage*100)/100) + '%';
						}
					}else{
						chart.options.legend = {
							labelFormatter: function () {
								if(this.name.slice(-1) === '%'){
									return this.name;
								}
								return this.name + ': ' + (Math.round(this.percentage*100)/100) + '%';
							},
							padding:3
						}
					}
					if(chart.dataTable.data && chart.dataTable.data.rows && chart.dataTable.data.rows[0] && chart.dataTable.data.rows[0][0]){
						if(chart.options.tooltip){
							if(!chart.options.tooltip.format){
								chart.options.tooltip.formatter = function(){
									return chart.dataTable.data.rows[0][0] + ':' +this.point.name + '<br><span style="color:'  + this.point.color +'">\u25CF</span> ' + this.point.series.name + ':' + parseInt(this.point.y)
								}
							}
						}else{
							chart.options.tooltip = {
								formatter: function(){
									return chart.dataTable.data.rows[0][0] + ':' +this.point.name + '<br><span style="color:'  + this.point.color +'">\u25CF</span> ' + this.point.series.name + ':<b>' + parseInt(this.point.y) + '</b>'
								}
							}
						}
					}
				}
			}
			if(config.tagging){
				if(!chart.options.plotOptions){
					chart.options.plotOptions = {
						series: {}
					};
				}
				if(!chart.options.plotOptions.series){
					chart.options.plotOptions.series = {};
				}
				chart.options.plotOptions.series.marker = {
					enabled: true,
					radius: 0
				}
				chart.options.plotOptions.series.cursor = 'pointer';
				chart.options.plotOptions.series.point = {
                    events: {
                        click: function(e) {
                        	if(e.point.shapeType){
                        		//不是点的类型
                        		return;
                        	}
                        	//marker不存在
                        	if(!this.marker || !this.marker.text){
                        		if(config.admin){
                        			//管理员点击未标注点
                        			//添加标注样式
                        			e.point.update({
		                                marker: {
		                                	enabled: true,
		                                    lineColor: "#fff",
		                                    radius: 8,
		                                    fillColor: "#ED6723",
		                                    lineWidth: 2,
		                                }
		                            });
                        		}
                        	}
                            //得到位置
                            var info = {
                            	point:this
                            }
                            config.tagfn.click(info);
                            
                        },
                        mouseOver: function(e){

                        },
                        mouseOut: function(e){
                            //config.tagfn.mouseOut();
                        }
                    } 
                }
            }
			return $.extend(true, {
				_type: _type,
				credits:{
					enabled: false
				}
			}, chart.options);
		},
		_transformChartData: function(chartType, chart, config){//图表数据校验，取样，转换
			
			var _dataTable = $.extend(true, {}, chart.dataTable);

			if (_.isEmpty(_dataTable.series)) {//旧的数据结构，需要转换
				//时间维度数据倒序校验
				//数据抽样
				// if(chartType === 'line' && (!config.mode || config.mode !== 'fullscreen') && _dataTable.data.rows.length > 500){
				// 	_dataTable.data.rows = _dataTable.data.rows.filter(function(curValue, index, arr) {
				// 		return index == 0 ? true : index == arr.length-1 ? true : index % 2 == 0;
				// 	});
				// }
				return this._transformToSeries(chartType, chart);
			}else {//新的数据结构
				//时间维度数据倒序校验
				//数据抽样
				if (!_.isEmpty(_dataTable.series) && _dataTable.series instanceof Array) {
					if (typeof chart.options.series == 'undefined' || _.isEmpty(chart.options.series)) {
						chart.options.series = [];
					}
					_dataTable.series.forEach(function(value, index, arr){
						if (_.isEmpty(chart.options.series[index])) {
							chart.options.series[index] = {};
							if (chartType !== 'polar' && chartType !== 'chart') {
								chart.options.series[index].type = chartType;
							}
						}
						chart.options.series[index].data = value.data;
					});
					return chart.options.series;
				}else {
					throw 'series is null';
				}
			}
		},
		getLiveChartConfig: function(chart, config) {
			config = config || { mapData: [] };
			chart  = chart  || { options: {} };
			var self = this;

			if(chart.options && chart.options.chart) {
				chart.options.chart.events = {
					load: function() {
						if(config.onload && typeof config.onload == 'function'){
							config.onload();
						}
					}
				};
				chart.options.chart.width  = config.width;
				chart.options.chart.height = config.height;
				chart.options.chart.backgroundColor = config.backgroundColor || '#fff';
			}else {
				chart.options.chart = {
					events:{
						load: function(){
							if(config.onload && typeof config.onload == 'function'){
								config.onload();
							}
						}
					},
					width: config.width,
					height: config.height,
					backgroundColor: config.backgroundColor || '#fff'
				}
			}
			//图表数据源设置
			//数据时间顺序处理
			if(chart.dataTable && chart.dataTable.data && chart.dataTable.data.rows){
				chart.dataTable.data.rows = this.getSortedDateTable(chart.dataTable.data.rows, false);
			}
			if(chart.dataTable && chart.dataTable.data && chart.dataTable.data.columns){
				chart.dataTable.data.colunms = this.getSortedDateTable(chart.dataTable.data.colunms, true);
			}
			let data  = chart.dataTable && chart.dataTable.data;
			let array = null;
			let _type = '';

			//线图取样，起始点和末尾点不取样
			if(chart.options.chart.type == 'line' && (!config.mode || config.mode != 'fullscreen') && data && data.rows && data.rows.length > 500){
				array = data.rows.filter(function(curValue, index, arr) {
					return index == 0 ? true : index == arr.length-1 ? true : index % 2 == 0;
				});
			}
			// 饼图数据合并替换
			if(chart.chartType === 'pie' || chart.options.chart.type == 'pie'){
				// 检测是否取前n位,剩余显示为其他
				if(chart.topn){
					var pie_limit = chart.topn,
					// 数值总合
					pie_total = 0;
					array = JSON.parse(JSON.stringify(chart.dataTable.data.rows))
					if(chart.type == 'pdf_live_chart'){
						// pdf解析格式饼图
						var pie_data = array.slice(1);
						tmp_data = pie_data.sort(function(a,b){return b[1] - a[1];});
						pie_data = tmp_data.filter(function(series, index){
							pie_total += series[1];
							return index < pie_limit
						})
						pie_data.map(function(series,index){
							pie_total -= series[1]
						})
						if (this.lang == 'cn') {
							pie_data.push(['其他',pie_total]);
						}else {
							pie_data.push(['Other',pie_total]);
						}
						// 填充第一行空数据
						pie_data.unshift(['',''])
						array = pie_data
					}else{
						// 通用数据格式饼图
						var pie_data = array[0].map(function(key,index){
							if(index !== 0){
								pie_total += array[1][index];
							}
							return {
										name:key,
										y: array[1][index]
									}
						}).sort(function(a,b){return b.y - a.y})
						pie_data.unshift(pie_data.pop())
						// 清空array填充最终数据
						array = [
							[],
							[]
						]
						pie_data.map(function(item,index){
							if(index > pie_limit)return false;
							if(index !== 0){
								pie_total -= item.y;
							}
							array[0][index] = item.name;
							array[1][index] = !isNaN(item.y)?Math.round(item.y*100)/100:'值';
						})
						if (this.lang == 'cn') {
							array[0].push('其他');
						}else {
							array[0].push('Other');
						}
						array[1].push(Math.round(pie_total*100)/100);
					}
				}
				// 统一隐藏饼图标签
				if(chart.options.plotOptions && chart.options.plotOptions.pie){
					chart.options.plotOptions.pie.dataLabels = {
						enabled: false
					};
					chart.options.plotOptions.pie.showInLegend = true;
					chart.options.plotOptions.pie.borderColor = '#fff';
				}else{
					chart.options.plotOptions.pie = {
						dataLabels:{
							enabled: false
						},
						showInLegend: true,
						borderColor: '#fff'
					}
				}
				// 统一修改饼图lengend
				if(chart.options.legend){
					chart.options.legend.enabled = true;
					chart.options.legend.padding = 3;
					chart.options.legend.labelFormatter = function() {
						if(this.name.slice(-1) === '%'){
							return this.name;
						}
						return this.name + ': ' + (Math.round(this.percentage*100)/100) + '%';
					}
				}else{
					chart.options.legend = {
						labelFormatter: function () {
							if(this.name.slice(-1) === '%'){
								return this.name;
							}
							return this.name + ': ' + (Math.round(this.percentage*100)/100) + '%';
						},
						padding:3
					}
				}
				if(chart.dataTable.data && chart.dataTable.data.rows && chart.dataTable.data.rows[0] && chart.dataTable.data.rows[0][0]){
					if(chart.options.tooltip){
						if(!chart.options.tooltip.format){
							chart.options.tooltip.formatter = function(){
								return chart.dataTable.data.rows[0][0] + ':' +this.point.name + '<br><span style="color:'  + this.point.color +'">\u25CF</span> ' + this.point.series.name + ':' + parseInt(this.point.y)
							}
						}
					}else{
						chart.options.tooltip = {
							formatter: function(){
								return chart.dataTable.data.rows[0][0] + ':' +this.point.name + '<br><span style="color:'  + this.point.color +'">\u25CF</span> ' + this.point.series.name + ': <b>' + parseInt(this.point.y) + '</b>'
							}
						}
					}
				}
			}

			// 统一调整线宽
			H.getOptions().plotOptions.line.lineWidth = 1;
			H.getOptions().plotOptions.column.borderColor = config.borderColor || '#fff';
			H.getOptions().tooltip.shared = true;

			// 定制column堆叠总量
			if (chart.options.plotOptions && chart.options.plotOptions.series && chart.options.plotOptions.series.stacking) {
				if (chart.options.tooltip){
					if(!chart.options.tooltip.formatter){
						chart.options.tooltip.formatter = function () {
							var s = '<b>' + this.points[0].key + '</b>';
							$.each(this.points, function () {
								s += '<br/><span style="color:' + this.color + '">\u25CF</span>' + this.series.name + ': ' +
									this.y;
							});
							if (this.points.length > 1) {
								s += '<br/><span style="color:#000">\u25CF</span>' + (self.lang === 'en' ? 'total:' : '总计:') + this.points[0].total;
							}
							return s;
						}
					}
				}else{
					chart.options.tooltip = {
						formatter: function () {
							var s = '<b>' + this.points[0].key + '</b>';
							var arr = new Array();
							var s_ = s;
							var temp;
							$.each(this.points, function () {
								s_ = '<br/><span style="color:' + this.color + '">\u25CF</span>' + this.series.name + ': ' +
									this.y.toFixed(2);
									arr.push(s_);
							});

 							 if(arr.length != 1){
                             	arr[arr.length-1] =  arr[arr.length-1] + '%';
                             };
 							 if (this.points.length > 2) {
                                 s_='<br/><span style="color:#000">\u25CF</span>' + (self.lang === 'en' ? 'total:' : '总计:') + this.points[0].total;
							 	arr.push(s_);
							 	temp = arr[arr.length-1];
								arr[arr.length-1]=arr[arr.length-2];
								arr[arr.length-2] = temp
							 }
							return arr;
						}
					}

				}
			}

			// 统一增加各类图表响应式
			chart.options.responsive =  {
				rules: [{
					condition: {
						maxHeight: 255,
					},
					chartOptions: {
						legend:{
							enabled: false
						}
					}
				}, {
					condition: {
						maxWidth: 300,
						minHeight: 255
					},
					chartOptions: {
						legend: {
							padding: 3,
							useHTML: true,
							itemStyle:{
								width:'150px',
								overflow: "hidden",
								textOverflow: 'ellipsis',
								whiteSpace: 'nowrap'
							}
						}
					}
				},{
					condition: {
						minWidth: 301,
						maxWidth: 401,
						minHeight: 255
					},
					chartOptions: {
						legend: {
							padding: 3,
							useHTML: true,
							itemStyle:{
								width:'250px',
								overflow: "hidden",
								textOverflow: 'ellipsis',
								whiteSpace: 'nowrap'
							}
						}
					}
				},{
					condition: {
						minWidth: 401,
						minHeight: 256
					},
					chartOptions: {
						legend:{
							layout: 'horizontal',
							align: 'center',
							verticalAlign: 'bottom',
							padding: 5
						}
					}
				}]
			}

			if(data){
				if(chart.options.data){
					if(data && data.rows){
						chart.options.data.rows    = array ? array : data.rows;
					}else if(data.columns){
						chart.options.data.columns = data.columns;
					}
				}else{
					if(data.rows){
						chart.options.data = {
							rows: array ? array : data.rows
						}
					}else if(data.colunms){
						chart.options.data = {
							colunms: data.colunms
						}
					}
				}
				// 饼图通用格式数据转置（非pdf解析）
				if(chart.type !== 'pdf_live_chart' && (chart.chartType === 'pie' || chart.options.chart.type === 'pie') && chart.options && chart.options.data){
					chart.options.data.switchRowsAndColumns = true;
				}
				// 用系列名称覆盖数据源的名称
				if(chart.options && chart.options.chart && chart.options.chart.type && chart.options.chart.type.indexOf('map') === -1 && chart.options.series instanceof Array){
					var names = chart.options.series.map(function(series) {
						return series.name
					});
					if (_.compact(names).length && _.compact(names).length == chart.options.series.length) {
						if (chart.options.data.switchRowsAndColumns) {
							chart.options.data.rows.slice(1).map(function (series,i) {
								series[0] = names[i];
							})
						} else {
							chart.options.data.rows[0] = [''].concat( names.length ? names : [''] );
						}
					}
				}
			}
			// 设置图表标题
			chart.options.title = {
				margin: 0,
				text: 'chart',
				style:{
					fontSize: '14px',
					visibility: 'hidden'
				},
			}
			// 隐藏导出按钮
			chart.options.exporting = { enabled:false };

			// 设置y轴样式
			if(chart.options.yAxis instanceof Array) {
				chart.options.yAxis[0].title = { text: null};
				chart.options.yAxis[0].lineColor = config.yAxis?config.yAxis.lineColor :'#eee';
				chart.options.yAxis[0].opposite = false;
				chart.options.yAxis[0].gridLineColor = config.yAxis?config.yAxis.gridLineColor :'#fff';
				chart.options.yAxis[0].labels = $.extend(chart.options.yAxis[0].labels || {}, {
					style:{
						color: '#7985a3'
					}
				})
				if(chart.options.yAxis[1]){
					chart.options.yAxis[1].title = { text: null};
					chart.options.yAxis[1].lineColor = config.yAxis?config.yAxis.lineColor :'#eee';
					chart.options.yAxis[1].opposite = true;
					chart.options.yAxis[1].gridLineColor = config.yAxis?config.yAxis.gridLineColor :'#fff';
					chart.options.yAxis[1].labels = $.extend(chart.options.yAxis[1].labels || {}, {
						style:{
							color: '#7985a3'
						}
					})
				}else{
					chart.options.yAxis.push({
						title: {
							text: null,
						},
						opposite: true,
						lineColor: config.yAxis?config.yAxis.lineColor :'#eee',
						gridLineColor: config.yAxis?config.yAxis.gridLineColor :'#fff',
						labels:{
							style:{
								color: '#7985a3'
							}
						}
					})
				}
			}else{
				chart.options.yAxis = [{
					title:{
						text:null
					},
					opposite: false,
					lineColor: config.yAxis?config.yAxis.lineColor :'#eee',
					gridLineColor: config.yAxis?config.yAxis.gridLineColor :'#fff',
					labels:{
						style:{
							color: '#7985a3'
						}
					}
				},{
					title: {
						text: null,
					},
					opposite: true,
					lineColor: config.yAxis?config.yAxis.lineColor :'#eee',
					gridLineColor: config.yAxis?config.yAxis.gridLineColor :'#fff',
					labels:{
						style:{
							color: '#7985a3'
						}
					}
				}]
			}
			// 设置x轴样式
			if(chart.options.xAxis){
				chart.options.xAxis.lineColor = config.xAxis?config.xAxis.gridLineColor :'#eee';
				chart.options.xAxis.gridLineColor = config.xAxis?config.xAxis.gridLineColor :'#fff',
				chart.options.xAxis.labels = $.extend(chart.options.xAxis.labels, {
					style:{
						color: '#7985a3'
					}
				})
			}else{
				chart.options.xAxis = {
					lineColor: config.xAxis?config.xAxis.gridLineColor :'#eee',
					gridLineColor: config.xAxis?config.xAxis.gridLineColor :'#fff',
					labels:{
						style:{
							color: '#7985a3'
						}
					}
				}
			}
			// 设置图例样式
			if(chart.options.legend){
				chart.options.legend.itemStyle = {
					color: '#7985a3',
					fontWeight: 'normal'
				}
			}else{
				chart.options.legend = {
					itemStyle:{
						color: '#7985a3',
						fontWeight: 'normal'
					}
				}
			}
			// 设置Series
			if(chart.options.chart.type
				&& chart.options.chart.type.indexOf('map') > -1
				&& chart.options.series
				|| (chart.options.series && chart.options.series.some((s)=>{
						return s.type && s.type.indexOf('map') > -1
					}))
				){
				_type = 'Map';
				delete chart.options.xAxis;
				delete chart.options.yAxis;
				delete chart.options.responsive
				// map类型chart
				for (var i = 0,j = chart.options.series.length; i < j; i++) {
					delete chart.options.data;
					if(chart.options.series[i].name === 'Basemap'){
						// 基础地图
						chart.options.series[i].mapData = H.maps["countries/cn/custom/"+ this.lang +"-all-sar-taiwan"] || [];
					}
					if(chart.options.series[i].type === 'mapbubble'){
						// 气泡地图
						chart.options.series[i].data = chart.dataTable.data.rows;
						let sum = 0;
						chart.dataTable.data.rows.map((d)=>{
							sum += d.z
						})
					   if(chart.options.tooltip){
							if (this.lang == 'cn') {
								chart.options.tooltip.formatter = function () {
									return this.point.city + '<br>'+ this.point.cn_name +":" + this.point.z.toFixed(2) + '<br>占比: ' + parseInt(this.point.z/sum*10000) /100 + '%' ;
								}
							}else if (this.lang == 'en') {
								chart.options.tooltip.formatter = function () {
									return this.point.city + '<br>'+ this.point.en_name +":" + this.point.z.toFixed(2) + '<br>Weight: ' + parseInt(this.point.z/sum*10000) /100 + '%' ;
								}
							}
						}else{
							if (this.lang == 'cn') {
								chart.options.tooltip = {
									formatter: function () {
										return this.point.city + '<br>'+ this.point.cn_name +":" + this.point.z.toFixed(2) + '<br>占比: ' + parseInt(this.point.z/sum*10000) /100 + '%' ;
									}
								}
							}else if (this.lang == 'en'){
								console.log(this.point);
								chart.options.tooltip = {
									formatter: function () {
										return this.point.city + '<br>'+ this.point.en_name +":"  + this.point.z.toFixed(2) + '<br>Weight: ' + parseInt(this.point.z/sum*10000) /100 + '%' ;
									}
								}
							}
						}
					}
					// 矩阵图
					if( chart.options.series[i].type === 'treemap'){
						// 恢复使用默认绘图函数
						_type = '';
						delete chart.options.chart.type;
						let colorNum = chart.dataTable.data.rows.length;
						chart.options.series[i].data = chart.dataTable.data.rows.sort((a,b)=>{
															if (_.isNumber(b.diff) && _.isNumber(a.diff)) {
																return b.diff - a.diff;
															} else {
																return b.value - a.value;
															}
														}).map((data,i)=>{
															let brighten = i/colorNum;
															data.color = H.Color('#2B70C7').brighten(brighten).get();
															return data;
														});
						// 矩阵图tooltip增加diff百分比;
						var _format_data = chart.options.series[i].data;
						chart.options.tooltip.formatter = function() {
							let _diff = 0;
							_format_data.some((data,i)=>{
								if(data.name === this.key){
									_diff = data.diff;
									return true;
								}
							});
							if(typeof _diff === 'undefined'){
								return this.key + " : " + this.point.value;
							}else{
								return (self.lang === 'en' ?'Change d/d : ':'每日变化率 : ') + Math.round(_diff*100)/100 + "%<br/>" + this.key + " : " + this.point.value.toFixed(2) ;
							}
						}
					}
					// 热力图
					if( chart.options.series[i].hasOwnProperty('joinBy')){
						chart.options.series[i].data = chart.dataTable.data.rows;
						let isWorld = chart.options.chart.type === 'worldmap' || (chart.options.series[i].mapData && chart.options.series[i].mapData.title.indexOf('World')>-1);
						chart.options.series[i].mapData = isWorld ? H.maps["custom/world_"+this.lang] : H.maps["countries/cn/custom/"+ this.lang +"-all-sar-taiwan"];
						if(isWorld){
							chart.options.chart.type = 'map';
						}
						chart.options.legend.enabled = false;
						if(config && config.mode === 'fullscreen' && chart.options.series[i].dataLabels){
							chart.options.series[i].dataLabels.enabled = true;
						}
					}
				}
			}else if(data && data.rows && data.rows[0][0] == 'date' && !chart.options.chart.polar
				&& (!config.mode || config.mode != 'fullscreen')){
				_type = 'StockChart';

				// stock类型chart
				chart.options.scrollbar 	= {enabled:false};
				chart.options.navigator 	= {enabled:false};
				chart.options.rangeSelector = {enabled:false};
			}
			// debugger
			return $.extend(true, {
				_type: _type,
				credits:{
					enabled: false
				}
			}, chart.options);
		},
		_drawDataTable: function(container, config) {
			// 绘制Table
			if(container) {
				config = config || {};

				if(!config.showHeader) {
					// 设置是否显示表头样式
					config.classes = 'table table-hover no-header';
				}
				container.append('<table width="100%"></table>');
				let table = container.children('table');

				container.parentsUntil('.grid-stack-item-content').on("mresize",function(){
					// var H = $(this).outerHeight();
					// var h = $('.fixed-table-header').outerHeight() + 2;
					// var _h = 37;
				 //    // $('#table').bootstrapTable('destroy');
				 //    console.log(_h, parseInt((H - h) / _h) - 1)

				 //    $('#table').bootstrapTable('refreshOptions', {
				 //    	height: $('.wrapper').outerHeight(),
				 //    	data: getData(parseInt((H - h) / _h) - 1)
				 //    });
				 //
					console.log($(this), $(this).outerHeight());
				});
				return table.bootstrapTable(config);
			}
		},
		getChangeCls: function(change, country) {
			// 获取股票涨跌状态
			// 国家(cn[默认]/en)
			country  = country || 'cn';
			var up   = country == 'cn' ? 'up' : 'down',
				down = country == 'cn' ? 'down' : 'up';
			return (isNaN(change) || parseFloat(change) == 0 ? '' : parseFloat(change) > 0 ? up : down);
		},
		getChangeTip: function(change, country){
			country  = country || 'cn';
			var up   = '+',
				down = '';
			return (isNaN(change) || parseFloat(change) == 0 ? '' : parseFloat(change) > 0 ? up : down);
		},
		drawResearchStockCard: function(containerId, chart, isFullSceen) {
			// 绘制research-stock-card
			if(containerId){
				var params     = chart.params || {};
				var container  = $('#' + containerId);
				var stock_card = $('<div class="research-widgets research-stock-card"></div>');
				var card       = $('<div class="card-price">' +
										'<p class="cur">' + params.cur + '</p>' +
										'<p class="change ' + this.getChangeCls(params.change, params.country) + '">' +
											this.getChangeTip(params.change, params.country) +
											params.change + ' (' + this.getChangeTip(params.change, params.country) + params.changePer + ')' +
										'</p>' +
									'</div>');
				let table      = $('<div class="card-table"></div>');

				stock_card.append(card).append(table);
				container.empty();
				container.append(stock_card);

				let lineHeight = 27,
					containerHeight = container.height() - 90;//减去card-price高度
				if (containerHeight < 0) {
					var _rows = [];
				}else {
					var _rows = chart.rows.slice(0, Math.floor(containerHeight / lineHeight)-1);//减去header
				}
				if (_rows.length > 0) {
					table.show();
				}else {
					table.hide();
				}

				let colunmWidth = 80,
					containerWidth = container.width(),
					colNum = Math.floor(containerWidth/colunmWidth);

				if(params && params.table && params.table.columns) {
					params.table.columns.map(function(col, index) {
						if(col.type == 'link') {
							col.formatter = function(value, row, index) {
								if(value && value.title && value.url) {
									return '<a href="' + value.url + '" target="_blank" title="' + value.title + '">' + value.title + '</a>';
								} else {
									if(col.format === '万'){
										var formatValue = value > 10000 ? parseInt(value/10000) + '万' : value;
										return '<span title="' + formatValue + '">' + formatValue + '</span>';
									}
									return '<span title="' + value + '">' + value + '</span>';
								}
							}
							
						} else {
							col.formatter = function(value, row, index) {
								if(col.format === '万'){
									var formatValue = value > 10000 ? parseInt(value/10000) + '万' : value;
									return '<span title="' + formatValue + '">' + formatValue + '</span>';
								}
								return '<span title="' + value + '">' + value + '</span>';
							}
						}
						if(typeof col.cellStyle == 'object'){
							var cell_style = col.cellStyle;
							col.cellStyle  = function(value, row, index, field) {
								return {
									css: cell_style
								};
							}
						}
						col.class = 'text-nowrap';
						if (index >= colNum) {
							col.visible = false;
						}else {
							col.visible = true;
						}
					})
				}

				this._drawDataTable(table, $.extend(true, {
					sidePagination: 'client',
					showHeader: false,
					data: (isFullSceen?chart.rows:_rows) || [],
				}, params.table));
				$('.th-inner').each(function(i,el){
					$(el).attr('title',$(el).html());
				})
			}
		},
		drawResearchDataList: function(containerId, chart, isFullSceen) {
			// 绘制research-data-list
			if(containerId){
				var params    = chart.params || {};
				var container = $('#' + containerId);
				var data_list = $('<div class="research-widgets research-data-list"></div>');

				container.empty();
				container.append(data_list);

				let lineHeight = 25,
					containerHeight = container.height(),
					colcRowNum = Math.floor(containerHeight / lineHeight)-3,//减去header
					_rows = colcRowNum < chart.rows.length ? chart.rows.slice(0, colcRowNum) : chart.rows;
				if (_rows.length > 0) {
					data_list.show();
				}else {
					data_list.hide();
				}
				var _data = isFullSceen?chart.rows:_rows || [];
				let colunmWidth = 80,
					containerWidth = container.width(),
					colNum = Math.floor(containerWidth/colunmWidth);
				if(params && params.table && params.table.columns) {
					params.table.columns.map(function(col,index){
						if(!col.width)return false
						if(col.width > 80){
							colNum--;
						}else{
							colNum++;
						}
					})
					params.table.columns.map(function(col, index) {
						if(col.type == 'link') {
							col.formatter = function(value, row, index) {
								if(value && value.title && value.url) {
									return '<a href="' + value.url + '" target="_blank" title="' + value.title + '">' + value.title + '</a>';
								} else {
									return '<span title="' + value + '">' + value.toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,') + '</span>';
								}
							}
						} else {
							col.formatter = function(value, row, index) {
								return '<span title="' + value + '">' + value.toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,') + '</span>';
							}
						}

						if(typeof col.cellStyle == 'object'){
							var cell_style = col.cellStyle;
							col.cellStyle  = function(value, row, index, field) {
								return {
									css: cell_style
								};
							}
						}
						col.class = 'text-nowrap';
						if (index >= colNum) {
							col.visible = false;
						}else {
							col.visible = true;
						}
						if(!col.title.trim()){
							col.title = '&nbsp;'
						}
					});
				}
				this._drawDataTable(data_list, $.extend(true, {
					sidePagination: 'client',
					showHeader: false,
					data: _data,
				}, params.table));
				$('.th-inner').each(function(i,el){
					$(el).attr('title',$(el).html());
				})
			}
		},
		//转置ResearchData
		_getResearchWidgetData(config) {
			config = config || {};
			let data = [];

			if(config.hasOwnProperty('params') && config.hasOwnProperty('rows')){
				let col_order = [];
				config.params.table
				&& config.params.table.columns
				&& config.params.table.columns.forEach(function(val, index){
					if(val.field){
						col_order.push(val.field);
					}
				})

				if(col_order.length > 0) {
					config.rows.forEach(function(val, index) {
						let row = [];
						col_order.forEach(function(c, i){
							if(typeof val[c] == 'object') {
								row.push(val[c].title ? val[c].title : JSON.stringify(val[c]));

								if(val[c].url) {
									row.push(val[c].url);
								}
							} else {
								row.push(val[c]);
							}
						})

						if(row.length > 0){
							data.push(row);
						}
					})
				}
			}
			return data;
		},
		//转置饼图数据
		_getPieWidgetData(chart, data){
			if (chart.type.indexOf('chart') != -1
				&& (chart.chartType && (chart.chartType.indexOf('pie') != -1
					|| chart.chartType.indexOf('polar') != -1
					|| chart.chartType.indexOf('radar') != -1))) {
				let _data = {},
					_rows = [];
				for (var i = 0; i < data.rows[0].length; i++) {
					_rows.push([data.rows[0][i]]);
				}
				for (var i = 1; i < data.rows.length; i++) {
					for (var j = 0; j < data.rows[i].length; j++) {
						_rows[j].push(data.rows[i][j]);
					}
				}
				_data.rows = _rows;
				return _data;
			}else {
				return data;
			}
		},
		//格式化第一列（如果为时间戳）
		_formatColumnDateTime(chart, data){
			let isDateTime = false;
			if (chart.options && chart.type.indexOf('chart') != -1
				&& (chart.chartType && chart.chartType.indexOf('pie') == -1
					&& chart.chartType.indexOf('polar') == -1
					&& chart.chartType.indexOf('radar') == -1)) {
				if (chart.options.xAxis) {
					if (chart.options.xAxis.type == 'datetime') {
						isDateTime = true;
					}
				}
			}else if (data.rows[0][0] == '日期' || data.rows[0][0] == 'date' || data.rows[0][0] == 'datetime') {
				isDateTime = true;
			}

			if (isDateTime && data.rows.length > 1 && $.isNumeric(data.rows[1][0])){
				let _rows = data.rows.map(function(curValue, index, array) {
					if ($.isNumeric(curValue[0])) {
						let date = new Date(parseInt(curValue[0]));
						curValue[0] = [date.getFullYear(),date.getMonth()+1,date.getDate()].join('-').replace(/(?=\b\d\b)/g, '0');
						return curValue;
					}
					return curValue;
				});
				return {rows:_rows};
			}else {
				return data;
			}
		},
		getExportData(chart, dataTable){
			let _data = null;
			if (typeof dataTable.table != 'undefined' && dataTable.table) {
				return $.extend(true, [], dataTable.table.data.rows || dataTable.table.data);
			}
			if (chart.type.indexOf('research') != -1) {
				if (chart.type.indexOf('research_stock_pie') != -1) {
					_data = this._getPieWidgetData(chart, dataTable.data);
				}else {
					return this._getResearchWidgetData(dataTable.data);
				}
			}else if (chart.type.indexOf('table') != -1) {
				_data = $.extend(true, {}, dataTable.data);
			}else if (chart.type.indexOf('chart') != -1) {
				_data = this._getPieWidgetData(chart, dataTable.data);
			}
			if (_data != null) {
				return this._formatColumnDateTime(chart, _data).rows.concat();
			}else {
				return dataTable.data.rows.concat();
			}
		},
		getSortedDateTable: function(dataTable,isColumnData){
	        // 维度是否为逆序(需有序)
            if(isColumnData){
	            var isReverse = dataTable.some(function(d,i){
	                return !i && typeof d[1] !== 'undefined' && typeof d[2] !== 'undefined' && new Date(d[1]) > new Date(d[2])
	            })
	            if(isReverse){
	                return dataTable.map(function(d,i){
	                    return d.slice(0,1).concat(d.slice(1).reverse());
	                })
	            }
            }else{
            	var isReverse = dataTable.some(function(d,i){
            		return i && typeof dataTable[i+1] !== 'undefined' && new Date(d[0]) > new Date(dataTable[i+1][0])
            	})
            	if(isReverse){
            		return dataTable.slice(0,1).concat(dataTable.slice(1).reverse())
            	}
            }
            return dataTable;
		},
		getObjType: function(obj) {
			// 获取对象类型
			if(typeof obj === 'undefined') {
				return 'undefined';
			}
			var type = Object.prototype.toString.call(obj);
			if(type.indexOf('String') != -1) {
				return 'string';
			} else if(type.indexOf('Number') != -1) {
				return 'number';
			} else if(type.indexOf('Array') != -1) {
				return 'array';
			} else if(type.indexOf('Null') != -1) {
				return 'null';
			} else if(type.indexOf('Boolean') != -1) {
				return 'null';
			} else if(type.indexOf('Object') != -1) {
				return 'object';
			} else {
				return 'unknown';
			}
		},
		analyzeSerie: function(serie, xAxis, index) {
			var self = this;
			var type = this.getObjType(serie);
			if(type == 'undefined' || type == 'unknown') return null;
			if(type == 'array') {
				// 二维数组
				var serie_copy = [];
				serie.forEach(function(item, _index) {
					serie_copy.push(self.analyzeSerie(item, xAxis, _index));
				})
				return serie_copy;
			} else if(type == 'object') {
				// 对象
				if(this.getObjType(xAxis && xAxis.categories) == 'array') {
					return [xAxis.categories[index] || null, serie.y];
				} else {
					// 原始数据
					return serie.y;
				}
			} else {
				if(this.getObjType(xAxis && xAxis.categories) == 'array') {
					return [xAxis.categories[index] || null, serie];
				} else {
					// 原始数据
					return serie;
				}
			}
		},
		guessDataType: function(data, chart) {
			data = data || [];
			var head = [];
			var self = this;
			if(data && data.length >= 2) {
				var x_type = chart && chart.xAxis && chart.xAxis.type;

				for(var i = 0; i < data[0].length; i++) {
					if(i == 0) {
						head.push({
							name: x_type     == 'datetime' ? '日期' : '',
							dataType: x_type == 'datetime' ? 'date' : self.getObjType(data[1][i]) == 'number' ? 'number' : 'text'
						})
						continue;
					}

					var d1_type = self.getObjType(data[1][i]);
					if(d1_type == 'number') {
						head.push({
							name: data[0][i],
							dataType: 'number'
						})
					} else {
						head.push({
							name: data[0][i],
							dataType: 'text'
						})
					}
					
				}
			}
			return head;
		},
		transformer: function(chart) {
			var self = this;

			chart = JSON.parse(JSON.stringify(chart || {}));

			var result = {
				// 表头
				head: [],
				// 图表数据
				data: [],
				// 图表样式
				style: chart
			}
			if(this.getObjType(chart.data && chart.data.rows) == 'array') {
				// 解析data属性
				result.data = chart.data.rows || [];
				result.head = self.guessDataType(result.data, chart);
			} else {
				// 解析series属性
				var series       = JSON.parse(JSON.stringify(chart.series || []));
				var series_clean = [];
				if(series.length) {
					// 获取干净的series对象
					series.forEach(function(item, index) {
						var data = self.analyzeSerie(item.data, chart.xAxis || {}, index);
						series_clean.push({
							name: item.name || ('Series ' + (index + 1)),
							data: data
						})
					});

					var data = [];
					var x_type = chart && chart.xAxis && chart.xAxis.type;
					// 设置二维数组表头
					series_clean.forEach(function(item, index) {
						if(index == 0) {
							data.push(['']);
							result.head.push({
								name: x_type     == 'datetime' ? '日期' : '',
								dataType: x_type == 'datetime' ? 'date' : ''
							});
						}
						result.head.push({
							name: '',
							dataType: ''
						});
						data[0].push(item.name);
					})
					// 设置二维数组数据
					var x = 1;
					series_clean.forEach(function(item, index) {
						if(self.getObjType(item.data) == 'array') {
							item.data.forEach(function(d, i) {
								var is_arr = self.getObjType(d) == 'array';
								var d0     = is_arr ? d[0] : null;
								var d1     = is_arr ? d[1] : d;

								// row: i+1  col: index
								if(index == 0) {
									// 设置维度类型
									if(!result.head[index].dataType) {
										var d0_type = self.getObjType(d0);
										if(d0_type == 'number') {
											result.head[index].dataType = 'number';
										} else {
											result.head[index].dataType = 'text';
										}
									}
									data[x]    = self.getArrWithDef(series_clean.length + 1, null);
									data[x][0] = d0;
								}

								
								// 判断x轴是否相同，相同则向右扩展一列，不同则向下扩展一行
								if(x < data.length && d0 === data[x][0]) {
									data[x][index + 1] = d1;

									// 设置series类型
									if(!result.head[index + 1].dataType) {
										var d1_type = self.getObjType(d1);
										if(d1_type == 'number') {
											result.head[index + 1].dataType = 'number';
										} else {
											result.head[index + 1].dataType = 'text';
										}
									}
								} else {
									if(index > 0) {
										// 找到该列需要放置的位置
										var p  = self.getPosition(data, d0);
										var _d = self.getArrWithDef(series_clean.length + 1, null);
										_d[0]     = d0;
										_d[index + 1] = d1;

										// 插入该元素
										data.splice(p, 0, _d);

										// 设置series类型
										if(!result.head[index + 1].dataType) {
											var d1_type = self.getObjType(d1);
											if(d1_type == 'number') {
												result.head[index + 1].dataType = 'number';
											} else {
												result.head[index + 1].dataType = 'text';
											}
										}
									} else {
										data[++x]      = self.getArrWithDef(series_clean.length + 1, null);
										data[x][0]     = d0;
										data[x][index] = d1;

										// 设置series类型
										if(!result.head[index].dataType) {
											var d1_type = self.getObjType(d1);
											if(d1_type == 'number') {
												result.head[index].dataType = 'number';
											} else {
												result.head[index].dataType = 'text';
											}
										}
									}
								}
								x++;
							})
							x = 1;
						}
					})
					result.data = data;
				}
			}
			return result;
		},
		getArrWithDef: function(len, def) {
			return Array.apply(null, Array(len)).map(function() { return def})
		},
		getPosition: function(arr, item) {
			arr = arr || [];
			for(var i = 1; i < arr.length - 1; i++) {
				var type = this.getObjType(item);

				if(type == 'number') {
					if(arr[1][0] > item) {
						return 1;
					}
					if(arr[i][0] <= item && item <= arr[i + 1][0]) {
						return i + 1;
					}
				} else {
					if(arr[i][0] === item) {
						return i + 1;
					}
				}
			}
			return arr.length;
		},
		_getChartType: function(chartOptions){//获取highcharts的类型

			if (!_.isEmpty(chartOptions.chart)) {
				if (chartOptions.chart.polar) {
					return 'polar';
				}	
			}
			
			var chartType = 'line';
			if (!_.isEmpty(chartOptions.series) && chartOptions.series instanceof Array) {
				var preType = '';
				chartOptions.series.forEach(function(value, index, arr){
					if (_.isEmpty(preType)) {
						preType = value.type || 'line';
					}else if (preType === (value.type || 'line')) {
						preType = value.type || 'line';
					}else {
						preType = 'chart';//混合图
					}
				});
				chartType = preType;
			}
			
			if (!_.isEmpty(chartOptions.chart)) {
				if (chartType !== 'chart' && !_.isEmpty(chartOptions.chart.type)) {
					chartType = chartOptions.chart.type;
				}
			}

			return chartType;
		},
		getChartType: function(chart){//获取图表的类型
			var type = chart.type || '',
				chartType = chart.chartType || '',
                optionType = chart.options && chart.options.chart && chart.options.chart.type || '';
            if(type.indexOf('table') != -1 || optionType === 'table' || chart.chartType == 'table' && type.indexOf('research') == -1){
                return 'table';
            } else if(type.indexOf('research') != -1) {
                if (type === 'research_stock_card') {
					return 'stockboard';
				}else if (type === 'research_data_list') {
					return 'stocklist';
				}else if (type === 'research_stock_pie') {
					return 'pie';
				}
            } else if(type.indexOf('image') != -1 || chart.chartType == 'image'){
                return 'image';
            }else {
            	var _chartType = this._getChartType(chart.options || {});
				return _chartType;
            }
		},
		_isDateTime(num) {
			if( !_.isNumber(num) || new Date(num) === 'Invalid Date' ) {
				return false;
			}
			// 以1980年为分界猜测数字是否是时间戳
			if(num - new Date('1980').getTime() > 0) {
				return true;
			}
		},
		_transformToSeries: function(chartType, chart){
			var seriesData = [],
				_rows = $.extend(true, [], chart.dataTable.data.rows),
				_headers = _rows[0],
				rowNum = _rows.length,
				colNum = _headers.length;
			if (chartType === 'pie') {
				if ( this._isDateTime(_rows[0][1]) ) {
					chart.options.xAxis.type = "datetime";
				} else {
					chart.options.xAxis.type = 'linear';
				}
				seriesData = [];
			} else if (chartType === 'polar') {
				if ( this._isDateTime(_rows[0][1]) ) {
					chart.options.xAxis.type = "datetime";
				} else {
					chart.options.xAxis.type = 'linear';
				}
				for(var j = 1; j < rowNum; j++){
					seriesData.push([]);
				}
			} else {
				for(var j = 1; j < colNum; j++){
					seriesData.push([]);
				}
			}

			//抽取二维数组数据
			var xAxisType = chart.options.xAxis.type;
			for(var i = 1; i < rowNum; i++){
				for (var k = 1; k < colNum; k++){
					var y = (typeof _rows[i][k] === 'undefined' || _rows[i][k] === null)?null:parseFloat(_rows[i][k]);
					if (chartType === 'pie') {
						if (colNum > rowNum) {
							seriesData.push({
								name:_headers[k],
								y: y
							});
						}else {
							seriesData.push({
								name:_rows[i][0], 
								y:y
							});
						}
					}else if (chartType === 'polar') {
						seriesData[i-1].push({
							x:_headers[k],
							y:y
						});
					}else {
						var x = _rows[i][0];
						if (xAxisType === 'datetime' && !$.isNumeric(x)) {
							if (new Date(x) + '' !== 'Invalid Date') {
								x = new Date(x).getTime();
							}
						}
						if (xAxisType === 'category') {
							seriesData[k-1].push({
								name:x,
								y:y
							});
						}else {
							seriesData[k-1].push({
								x:x, 
								y:y
							});
						}
					}
				}
			}

			//还原二维数据到series的data中
			var series = chart.options.series;
			if (!_.isEmpty(series) && series instanceof Array) {
				if (chartType === 'pie') {
					if (!_.isEmpty(series[0].data) && series[0].data instanceof Array) {
						for (var index = 0; index < series[0].data.length; index++) {
							series[0].data[index].name = seriesData[index].name;
							series[0].data[index].y = seriesData[index].y;
						}
					}else {
						series[0].data = seriesData;
					}
				}else {
					seriesData.forEach(function(value, index, arr){
						if (chartType === 'polar') {
							series[index].name = _rows[index+1][0] || '';
						}else {
							series[index].name = _headers[index+1];
						}
						series[index].data = value;
					});
				}
			}else if (_.isEmpty(series)) {
				series = [];
				if (chartType === 'pie') {
					series.push({
						type: 'pie',
						name: chart.title,
						data: seriesData
					});
				}else {
					seriesData.forEach(function(value, index, arr){
						series.push({
							name: _headers[index+1],
							data: value
						});
					});
				}
			}
			return series;
		}
	}
}(jQuery, Highcharts));
