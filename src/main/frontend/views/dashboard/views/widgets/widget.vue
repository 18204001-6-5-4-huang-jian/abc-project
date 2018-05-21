<template>
	<div class="widget-wrapper" :class="{'is-resizing':chartIsResizing}" :id="'widget-'+chartId">
		<div class="widget-header-wrapper">
			<div class="widget-header-content-wrapper">
				<!--小看板header-->
				<div class="widget-header-title-wrapper" :class="{'is-editing':isEditing}">
					<span v-text="chartTitle" @mouseover="showToolTip(chartId)" @mouseout="hideToolTip(chartId)"></span>
					<input type="text" name="widget-name"
						:id="('widget-input-'+chartIndex)"
						v-model="editName"
						@keyup.enter="handleEvent('rename', editName)"
						@blur="onBlurNameWidget" />
						<select  v-show = "chartSeries.length != 0" @change="getSelectData">
							<option v-for="item in chartSeries" v-text="item.name"></option>
						</select>
						<select  v-show = "secondchartSerieses.length != 0" @change="getSelectDatasecond">
							<option v-for="item in secondchartSerieses" v-text="item.name"></option>
						</select>
				</div>
				<!--全屏按钮-->
				<div v-show="chartStatus === 4" class="widget-header-btn-wrapper btn-fullscreen" @click="handleEvent('fullscreen')" @mousedown="onMouseDownFullScreen">
					<div class="icon">
					</div>
				</div>
				<!--提示框-->
				<div class="widget-tool-tip" :id="'tool-tip'+chartId">
					<div class="tool-tip-title" v-text="chartTitle"></div>
					<span class="tool-tip-description" v-text="chartDescription"></span>
				</div>
			</div>
		</div>
		<div class="widget-tip-wrapper"
			v-show="chartUpdateTimeEx || chartDataTimeEx"
			@click.stop="doFocus($event)">
			<span :class="timePeriod(chartUpdateTime)">{{chartUpdateTimeEx}}&nbsp;{{lang?'更新':'updated'}}</span>
		</div>
		<div class="widget-content-wrapper"
			:id="('chart-'+chartId)"
			:chart-index="chartIndex"
			:class="{'is-loading':isShowLoading}"
			@click.stop="doFocus($event)">
			<slot name="widget-content">
				
			</slot>
		</div>
		<!-- loading or failed -->
		<div class="widget-content-loading-wrapper" v-if="isShowLoading">
			<div class="load8">
				<div class="loader"></div>
			</div>
		</div>
		<div class="widget-content-failed-wrapper" v-if="isShowFailed">
			<div class="icon"></div>
		</div>
	</div>
</template>
<script type="text/javascript">
	export default {
		name: 'widget',
		data(){
			return {
				isEditing: false,
				editName: '',
				selectConfig:{},
				secondchartSerieses:Array
			}
		},
		props:{
			chartId: {
				type: String,
				default: ''
			},
			chartIndex: {
				type: Number,
				default: 0
			},
			chartTitle: {
				type: String,
				default: ''
			},
			chartType:{
				type: String,
				default: ''
			},
			chartUpdateTime: {
				type: Number,
				default: 1492483658663
			},
			chartDataTime: {
				type: Number,
				default: 1492483658663
			},
			chartDescription: {
				type: String,
				default: '该卡片暂无描述'
			},
			chartStatus: {
				type: Number,
				default: 0
			},
			chartImage: {
				type: String,
				default: ''
			},
			chartIsResizing: {
				type: Boolean,
				default: false
			},
			chartIsMoving: {
				type: Boolean,
				default: false
			},
			chartSeries:{
				type: Array,
				default:[]
			},
			secondchartSeries:{
				type: Array,
				default: () => []
			},
		},
		watch:{
			
		},
		mounted(){
			var self = this;
			self.secondchartSerieses = self.secondchartSeries;
			self.$nextTick(function(){
			// $('.selectpicker').selectpicker({  
   //              'selectedText': 'cat'  
   //          });  
			})
		},
		computed:{
			chartUpdateTimeEx(){
				if (this.chartUpdateTime > 0) {
					var date = new Date(parseInt(this.chartUpdateTime));
					if(this.chartId == "84654"){
						//单独处理陌陌各季度营收
						return fecha.format(date, 'MM.DD HH:mm');
					}else{
						return this.customDateFormat(date);
					}
				}
				return 0;
			},
			chartDataTimeEx(){
				return 0;
			},
			isOwnDashBoard(){
	            return this.$store.state.dashboard.curDashBoard.creator_id == sessionStorage.uid;
	        },
	        isEnableExportData(){
	        	return this.chartType.indexOf('image') == -1;
	        },
	        isEnableExportImage(){
	        	return (this.chartType.indexOf('table') == -1 && this.chartType.indexOf('research') == -1) || this.chartType.indexOf('research_stock_pie') != -1;
	        },
	        isShowLoading(){
	        	return this.chartStatus !== 4;
	        },
	        isShowFailed(){
	        	return this.chartStatus == 3;
	        },
	        lang(){
	        	return this.$store.state.dashboard.lang;
	        },
	        dashBoard(){
            return this.$store.state.dashboard.curDashBoard;
            },
            curDay(){
               return this.$store.state.dashboard.curDay;
            }
		},
		methods:{
			customDateFormat(date){
	            var now = new Date();
	            if(date.getFullYear() == now.getFullYear()){
	                // 同一年
	                if(date.getMonth() == now.getMonth()) {
	                    // 同一月
	                    if(date.getDate() == now.getDate()) {
	                        // 同一天
	                        return fecha.format(date, 'HH:mm');
	                    } else {
	                        // 不同天
	                        return fecha.format(date, 'MM.DD HH:mm');
	                    }
	                } else {
	                    // 不同月份
	                    return fecha.format(date, 'MM.DD HH:mm');
	                }
	            } else {
	                // 不同年份
	                return fecha.format(date, 'YYYY.MM.DD HH:mm');
	            }
	        },
			onRenameWidget(){
				var self = this;
				self.isEditing = true;
				self.editName = self.chartTitle;
				self.handleEvent('edit-name', true);
				setTimeout(function(){
                    $('.widget-header-title-wrapper.is-editing input').focus();
                },50);
			},
			timePeriod(time){
				var cur = new Date(new Date().setHours(0)),
					yestoday = cur.setDate(cur.getDate()-1),
					week = cur.setDate(cur.getDate()-6),
					month = cur.setDate(cur.getDate()-23);

				if (time >= yestoday) {
					return 'yestoday'
				}
				return 'normal';
			},
			getChartShareURI(chartId) {
				// 获取图表分享URI
				return location.protocol + '//' + location.host + '/views/share/chart.html#/' + this.chartId
			},
			doFocus(evt){
				if (this.isEditing) {
					this.onBlurNameWidget();
				}
				setTimeout(function(){
					$(evt.target).focus();
				},50);
			},
			onMouseDownFullScreen(){
				if (this.chartType.indexOf('image') != -1) {
					layer.tips('图片类型不可全屏查看', '#widget-'+this.chartId+' .btn-fullscreen', {
						tips: [3, '#287ddc'],
						time: 1000
					});
				}
			},
			//显示title提示框
			showToolTip(id){
				$('#tool-tip'+id).show();
			},
			//隐藏title提示框
			hideToolTip(id){
				$('#tool-tip'+id).hide();
			},
			onBlurNameWidget(){
				this.isEditing = false;
				this.editName = '';
				this.handleEvent('edit-name', false);
			},
			//点击全屏事件
			handleEvent(eventType, options){
				var self = this;
				console.log(self.dashBoard);
				if (eventType != 'edit-name'){
					self.onBlurNameWidget();
				}
				//向父组件传递onHandEvent事件和参数
				self.$emit('onHandleEvent', eventType, {
					id:self.chartId,
					index: self.chartIndex,
					options: options
				});
			},
			//下拉菜单切换获取数据
			getSelectData(e){
				var self = this;
				let _element = $($($(e.target).parents(".widget-wrapper")[0]).find(".highcharts-container")[0]);
			    for(let i = 0 ;i < self.chartSeries.length ;i++){
			    if(e.target.value == self.chartSeries[i].name){
			    	if(self.chartSeries[i].chart_id == undefined){
			    	//改变二级下拉菜单参数重新画图
                    self.secondchartSerieses = self.chartSeries[i].series;
                    let id = self.secondchartSerieses[0].chart_id;
	                    $.get({
						url:"/api/v1/chart/" + id,
						contentType:'application/json;chartset=utf-8',
	                    success:(res)=>{
	                    //图表的配置
	                    // console.log(res.data);
	                    $.get({
	                      	url:"/api/v1/chart/query-custom-data?id="+id+"&tp=" + moment(self.curDay).format("YYYY-MM-DD"),
	                      	contentType:'application/json;chartset=utf-8',
	                      	success:(resp)=>{
	                      		//把数据放到配置里
	                      		// console.log(resp);
	                      		res.data.dataTable = resp.table;
	                      		//重新画图
	                      		self.drawSelectChart(_element,res.data);
	                      		//改变切换后的图表配置用于全屏
	                      		self.selectConfig = res.data;
	                            self.dashBoard.charts[self.chartIndex] = self.selectConfig;
	                      	},
	                      	complete(){
	                      		console.log("获取数据");
	                      	 }
	                      })
	                    },
	                    complete(){
	                        console.log("获取配置");
	                       }
					    })
			    	}else{
				    	$.get({
						url:"/api/v1/chart/" + self.chartSeries[i].chart_id,
						contentType:'application/json;chartset=utf-8',
	                    success:(res)=>{
	                    //图表的配置
	                    $.get({
	                      	url:"/api/v1/chart/query-custom-data?id="+self.chartSeries[i].chart_id+"&tp=" + moment(self.curDay).format("YYYY-MM-DD"),
	                      	contentType:'application/json;chartset=utf-8',
	                      	success:(resp)=>{
	                      		//把数据放到配置里
	                      		// console.log(resp);
	                      		res.data.dataTable = resp.table;
	                      		//重新画图
	                      		self.drawSelectChart(_element,res.data);
	                      		//改变切换后的图表配置用于全屏
	                      		self.selectConfig = res.data;
	                            self.dashBoard.charts[self.chartIndex] = self.selectConfig;
	                      	},
	                      	complete(){
	                      		console.log("获取数据");
	                      	 }
	                      })
	                    },
	                    complete(){
	                        console.log("获取配置");
	                       }
					    })
			    	}
			    }
			}
		},
		getSelectDatasecond(e){
            //二级菜单切换画图
            var self = this;
			let _element = $($($(e.target).parents(".widget-wrapper")[0]).find(".highcharts-container")[0]);
			 for(let i = 0 ;i < self.secondchartSerieses.length ;i++){
			 	 if(e.target.value == self.secondchartSerieses[i].name){
			 	 	$.get({
					url:"/api/v1/chart/" + self.secondchartSerieses[i].chart_id,
					contentType:'application/json;chartset=utf-8',
                    success:(res)=>{
                    //图表的配置
                    // console.log(res.data);
                    $.get({
                      	url:"/api/v1/chart/query-custom-data?id="+self.secondchartSerieses[i].chart_id+"&tp=" + moment(self.curDay).format("YYYY-MM-DD"),
                      	contentType:'application/json;chartset=utf-8',
                      	success:(resp)=>{
                      		//把数据放到配置里
                      		// console.log(resp);
                      		res.data.dataTable = resp.table;
                      		//重新画图
                      		self.drawSelectChart(_element,res.data);
                      		//改变切换后的图表配置用于全屏
                      		self.selectConfig = res.data;
                            self.dashBoard.charts[self.chartIndex] = self.selectConfig;
                      	},
                      	complete(){
                      		//console.log("获取数据");
                      	 }
                      })
                    },
                    complete(){
                        //console.log("获取配置");
                       }
				   })
			 	 }
			 }
		},
		//下拉菜单切换画图
		drawSelectChart(element,chartWrapper){
			    var self = this;
			    element.empty();
			    console.log(chartWrapper);//图的配置
				let highChartConfig = ChartHelper.getLiveChartConfigV2(chartWrapper,{
                    onload: function(){
                     self.$emit('onSuccess', {
                            chartWrapper: chartWrapper
                        });
                    },
                    width:element.width() - 16,//减去padding
                    height:element.height() - 10,//冗余
                    mapData: chartWrapper.highMaps,
                    backgroundColor: '#1b1f29',
                    borderColor: '#262a36',
                    xAxis:{
                        lineColor: '#262a36',
                        gridLineColor: '#262a36'
                    },
                    yAxis:{
                        lineColor: '#262a36',
                        gridLineColor: '#262a36'
                    }
			 });
			    if(highChartConfig._type == 'Map'){
                    element.highcharts('Map', highChartConfig);
                }else if(highChartConfig._type == 'StockChart'){
                    element.highcharts('StockChart', highChartConfig);
                }else{
                	//debugger
                	// console.log(JSON.stringify(highChartConfig));
                    element.highcharts(highChartConfig);
                }
			}
		}
	}
</script>
<style type="text/css" scoped>
	.widget-content-loading-wrapper .load8{
	    padding: 0px;
	    width: 45px;
	    height: 45px;
	    background-color: transparent;
	}
	.widget-content-loading-wrapper .load8 .loader {
	    width: 45px;
	    height: 45px;
	    margin: 0px;
	    padding: 0px;
	    font-size: 10px;
	    position: relative;
	    text-indent: -9999em;
	    border-radius: 50%;
	    border-top: 0.4em solid rgba(18, 180, 242, 0.2);
	    border-right: 0.4em solid rgba(18, 180, 242, 0.2);
	    border-bottom: 0.4em solid rgba(18, 180, 242, 0.2);
	    border-left: 0.4em solid #12b4f2;
	    -webkit-animation: load8 1.1s infinite linear;
	    animation: load8 1.1s infinite linear;
	}
	@-webkit-keyframes load8 {
	    0% {
	        -webkit-transform: rotate(0deg);
	        transform: rotate(0deg);
	    }
	    100% {
	        -webkit-transform: rotate(360deg);
	        transform: rotate(360deg);
	    }
	}
	@keyframes load8 {
	    0% {
	        -webkit-transform: rotate(0deg);
	        transform: rotate(0deg);
	    }
	    100% {
	        -webkit-transform: rotate(360deg);
	        transform: rotate(360deg);
	    }
	}
</style>

<style type="text/css" scoped>
	.widget-wrapper {
		display: -webkit-flex;
		display: flex;
		flex-direction: column;
		padding: 0px;
		margin: 0px;
		width: 100%;
		height: 100%;
	}
	.widget-wrapper.is-resizing {
		z-index: 0 !important;
		overflow: hidden !important;
	}
	.widget-header-wrapper {
		flex-shrink: 0;
	}
	.widget-header-content-wrapper {
		display: -webkit-flex;
		display: flex;
		align-items: center;
		padding: 20px 15px 5px;
		margin: 0px;
		width: 100%;
		position: relative;
	}
	.widget-header-content-wrapper .widget-tool-tip{
		display: none;
		position: absolute;
		left: 15px;
		top: 45px;
		z-index: 1;
		background: rgba(255,255,255,.82);
		min-height: 50px;
		margin-right: 15px;
		border: 1px solid #0facd9;
		padding: 10px;
		border-radius: 3px;
	}
	.widget-header-content-wrapper .widget-tool-tip .tool-tip-title{
		font-weight: bold;
	}
	.widget-header-content-wrapper .widget-tool-tip .tool-tip-description{
		font-size: 12px;
	}
	.widget-header-title-wrapper {
		flex-grow: 1;
		text-overflow: ellipsis;
		white-space: nowrap;
		overflow: hidden;
		color: #0facd9;
	}
	.widget-header-title-wrapper.is-editing span {
		display: none;
	}
	.widget-header-title-wrapper span {
		font-weight: 600;
	}
	.widget-header-title-wrapper.is-editing input{
		display: inline-block;
	}
	.widget-header-title-wrapper input {
		display: none;
		width: 10%;
		padding: 0px 5px;
		margin: 0px;
		outline: none;
		box-shadow: none;
		border: none;
		border-bottom: 1px solid #287ddc;
	}
	.widget-wrapper:hover .widget-header-btn-wrapper {
		display: inline-block;
		text-align: center;
	}
	.widget-header-btn-wrapper {
		flex-basis: 25px;
		flex-shrink: 0;
		display: none;
		cursor: pointer;
	}
	.widget-header-btn-wrapper .icon {
		width: 16px;
		height: 16px;
		background-size: 16px 16px;
		background-position: center;
		background-repeat: no-repeat;
		transition: all 0.1s linear;
	}
	.widget-header-btn-wrapper.btn-fullscreen .icon {
		background-image: url('/images/icon-fullscreen.png');
	}
	.widget-header-btn-wrapper.btn-dropdown-menu .icon {
		background-image: url('/images/icon-dropdown-menu.png');
	}
	.widget-header-btn-wrapper.btn-dropdown-menu:hover .icon {
		background-image: url('/images/icon-dropdown-menu-hover.png');
	}
	.widget-header-btn-wrapper.btn-dropdown-menu .dropdown-menu {
		top: auto;
		left: auto;
		right: 10px;
		padding: 10px 0px;
		min-width: 110px;
		font-size: 12px;
		border-radius: 0px;
		color: #666;
    	box-shadow: 0px 0px 6px 0px rgba(0, 0, 0, 0.3);
	}
	.widget-header-btn-wrapper.btn-dropdown-menu .dropdown-menu>li>a {
		height: 34px;
		line-height: 34px;
		padding: 0px 20px;
		transition: all 0.1s linear;
	}
	.widget-header-btn-wrapper.btn-dropdown-menu .dropdown-menu>li>a.danger{
	    color: #d73a3a;
	}
	.widget-header-btn-wrapper.btn-dropdown-menu .dropdown-menu>li>a.danger:hover,
	.widget-header-btn-wrapper.btn-dropdown-menu .dropdown-menu>li>a.danger:focus{
	    color: #fff;
	    background-color: #d73a3a;
	}
	.widget-header-btn-wrapper.btn-dropdown-menu .dropdown-menu>li>a:hover,
	.widget-header-btn-wrapper.btn-dropdown-menu .dropdown-menu>li>a:focus{
	    background-color: #eaeaea;
	}
	.widget-header-btn-wrapper.btn-dropdown-menu .dropdown-menu li.divider {
		margin: 5px 0px;
		border: none;
	}

	.c-share-wrapper.p-up {
		top: -240px;
	}
	.c-share-wrapper.p-up:before {
		border-color: #ffffff transparent transparent;
	    border-width: 10px 8px 0px;
	    top: 100%;
	}
	.c-share-wrapper.p-right {
		left: -8px;
	}
	.c-share-wrapper.p-right:before {
		left: 8px;
    	right: auto;
	}
	.widget-tip-wrapper{
		flex-shrink: 0;
		padding: 0px 15px 5px;
		margin: 0px;
		width: 100%;
		font-size: 12px;
	}
	.widget-tip-wrapper span.normal {
		color: #596175;
	}
	.widget-tip-wrapper span.yestoday {
		color: #596175;
	}
	.widget-tip-wrapper .icon {
		width: 13px;
		height: 13px;
	}
	.widget-tip-wrapper .icon.normal {
		background-image: url('/css/themes/default/icons/icon-updatetime-normal.png');
	}
	.widget-tip-wrapper .icon.yestoday {
		background-image: url('/css/themes/default/icons/icon-updatetime-red.png');
	}
	.widget-content-wrapper {
		flex-grow: 1;
		padding: 0px 8px;
		margin: 0px;
		width: 100%;
		overflow-x: hidden;
		overflow-y: auto;
	}
	.widget-content-loading-wrapper,
	.widget-content-failed-wrapper  {
		position: absolute;
		top: 0px;
		left: 0px;
		bottom: 0px;
		right: 0px;
		background: rgb(21, 25, 34);
		display: -webkit-flex;
		display: flex;
		justify-content: center;
		align-items: center;
	}
	.widget-content-loading-wrapper{
		background: rgba(21, 25, 34, .5);
	}
	.widget-content-failed-wrapper {
		top: 65px;
	}
	.widget-content-failed-wrapper .icon {
		width: 100px;
		height: 100px;
		background-image: url('/images/img-bad-data.png');
		background-size: 100px 100px;
	}
	.widget-header-title-wrapper select{
	     /*appearance:none;  
        -moz-appearance:none;  
        -webkit-appearance:none;*/
		font-size: 12px;
		outline: 0;
		background-color:#1B1F29;  
		color: #0FACD9;
		width:135px;
	    height:22px;
	    line-height:22px;
		border: 1.2px solid #0FACD9;
		padding-left:9px;
		margin-left:10px;
	}
	/*清除ie的默认选择框样式清除，隐藏下拉箭头*/
	.widget-header-title-wrapper select::-ms-expand {
		 display: none;
	}
	.title-span{
		display: inline-block;
		width: 4px;
		height:11px;
		margin-right:5px;
		margin-top: -2px;
		background:#0FACD9;
	}
</style>
