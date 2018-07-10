<template lang="html">
    <div class="dashboard-content-wrapper">

        <!-- dashboard loading -->
        <div class="dashboard-content-isloading-wrapper" v-if="(status == 'isLoading')">
            <div class="loader8"></div>
        </div>
        <!-- dashboard load success and not empty -->
        <div class="dashboard-content-success-wrapper"
            v-show="(status == 'success' && chartPositions.length != 0)"
            @scroll="onScroll($event)">
            <grid-layout :layout="chartPositions"
                         :col-num="12"
                         :row-height="60"
                         :is-draggable="isDraggable"
                         :is-resizable="isResizable"
                         :vertical-compact="true"
                         :use-css-transforms="false">
            <grid-item v-for="(item, index) in chartPositions"
                           :x="item.x"
                           :y="item.y"
                           :w="item.w"
                           :h="item.h"
                           :i="item.i"
                           :index="index"
                           :key="item.id"
                           @resize="onResize"
                           @move="onMove"
                           @resized="onResized"
                           @moved="onMoved"
                           @containerWidth="onUpdateWidth">
            <widget
                        v-bind:chartId="(charts[parseInt(item.i)].id).toString()"
                        v-bind:chartIndex="parseInt(item.i)"
                        v-bind:chartTitle="charts[parseInt(item.i)].chartSeries?charts[parseInt(item.i)].chartSeries.title_pre:charts[parseInt(item.i)].title"
                        v-bind:chartType="charts[parseInt(item.i)].type"
                        v-bind:chartUpdateTime="charts[parseInt(item.i)].updateTime"
                        v-bind:chartDataTime="charts[parseInt(item.i)].dataTime"
                        v-bind:chartStatus="charts[parseInt(item.i)].status"
                        v-bind:chartIsResizing="charts[parseInt(item.i)].isResizing"
                        v-bind:chartIsMoving="charts[parseInt(item.i)].isMoving"
                        v-bind:chartSeries="charts[parseInt(item.i)].chartSeries?charts[parseInt(item.i)].chartSeries.series:[]"
                         v-bind:secondchartSeries="charts[parseInt(item.i)].chartSeries?charts[parseInt(item.i)].chartSeries.series[0].series:[]"
                        v-bind:chartDescription="charts[parseInt(item.i)].description?charts[parseInt(item.i)].description:lang?'该卡片暂无描述':'This card has no relevant description yet'"
                        v-on:onHandleEvent="onHandleWidgetEvent">
                    <component
                            slot="widget-content"
                            :id="'widget-' + item.i"
                            :ref="parseInt(item.i) + 1"
                            :is="getWidgetByType(charts[parseInt(item.i)].type)"
                            :width="item.w"
                            :height="item.h"
                            @onSuccess="onWidgetSuccess"
                            @onError="onWidgetError">
                    </component>
            </widget>
                </grid-item>
            </grid-layout>
             <!--<div class="dashboard-content-footer-wrapper" v-show="isShowFooter">
                <span>没有更多图表......</span>
            </div> -->
        </div>
    </div>
</template>
<script>
import ChartWidget from './widgets/chart-widget.vue'
import TableWidget from './widgets/table-widget.vue'
import CustomResearchWidgte from './widgets/custom-research-widget.vue'
import ImageWidget from './widgets/image-widget.vue'
import Widget from './widgets/widget.vue'

export default {
    name: 'dashboard-content',
    components: {
        GridLayout: VueGridLayout.GridLayout,
        GridItem: VueGridLayout.GridItem,
        Widget
    },
    mounted(){
        this.dashboardName = localStorage.getItem("dashboard-name");
        this.scrollContariner = $('.dashboard-content-success-wrapper')[0];
        $("html").mouseup(function(){
          $("html").css('cursor','default');
        });
        //日报跳转进入看板
       var self = this;
       setTimeout(function(){
            if(localStorage.getItem("dailyjoin")){
                 let daily_id = localStorage.getItem("dailyjoin")
            for(let i = 0;i<self.titleArray.length;i++){
                if(self.titleArray[i].id == daily_id){
                    // console.log(self.titleArray[i].id);
                    let element = $('#widget-' + self.titleArray[i].id).parent();
                    $('.dashboard-content-success-wrapper').animate({scrollTop:($(element).offset().top)-50},300);
                     // $('.dashboard-content-success-wrapper').scrollTop(($(element).offset().top)-50);
                     //清除日报id
                    localStorage.removeItem("dailyjoin");
                }
            }
          }else if(localStorage.getItem('iconScrollTopId')){
                 //全屏之后的返回
                let iconScrollTopId = localStorage.getItem('iconScrollTopId');
                for(let i = 0;i<self.titleArray.length;i++){
                if(self.titleArray[i].id == iconScrollTopId){
                    let element = $('#widget-' + self.titleArray[i].id).parent();
                    $('.dashboard-content-success-wrapper').animate({scrollTop:($(element).offset().top)-50},300);
                     //清除iconScrollTopId
                    localStorage.removeItem('iconScrollTopId');
                  }
                }
               }
        },2000)
      
       
    },
    data(){
        return {
            highMaps: null,
            drawTimerId: null,
            lastOffset: {
                top: 0,
                time: 0
            },
            scrollContariner: null,
            widgetElements: [],
            //是否正在编辑widget
            isEditingWidget: false,
            //是否显示footer提示
            // isShowFooter: false
            dashboardName:''
        }
    },
    props: {
        dashBoard: {
            type: Object,
            default:function(){
                return {
                    id: '',
                    title: '',
                    creator_id: '',
                    creator_name: '',
                    status: 'isLoading',//isLoading，success, failed
                    share: 0,
                    watched: false,
                    update_count: 0,
                    charts: [],
                    chartPositions: [],
                    chartCount: 0
                }
            }
        },
        status: {
            type: String,
            default: 'isLoading'
        },
        charts: {
            type: Array,
            default:function(){
                return [];
            }
        },
        chartPositions:{
            type: Array,
            default:function(){
                return [];
            }
        },
        chartCount: {
            type: Object,
            default: function(){
                return {
                    id: '',
                    count: 0,
                }
            }
        }
    },
    computed:{
        isFullScreenWatch(){
            return this.$store.state.fullscreen.isFullScreenWatch;
        },
        dashBoardScrollTop(){
            return this.$store.state.dashboard.scrollTop;
        },
        changedChartList(){
            return this.$store.state.fullscreen.changedList;
        },
        isOwnDashBoard(){
            return this.dashBoard.creator_id == sessionStorage.uid;
        },
        isDraggable(){
            return false && this.isOwnDashBoard && !this.isEditingWidget;
        },
        isResizable(){
            return false && this.isOwnDashBoard && !this.isEditingWidget;
        },
        lang(){
            return this.$store.state.dashboard.lang;
        },
        curDay(){
            return this.$store.state.dashboard.curDay;
        },
        titleArray(){
            return this.$store.state.dashboard.titleArray;
        },
        productId(){
            return this.$store.state.dashboard.productId;
        }
    },
    watch: {
        curDay(){
            let updateCharts = _.clone(this.charts).map((chartWrapper,i) => {
                chartWrapper.status = 0;
                chartWrapper.dataTable = null;
                return chartWrapper
            })

            this.$store.commit('zk_updateCharts', updateCharts);

            this.startDrawTimer()
        },
        chartPositions(newVal, oldVal){
            var self = this;
           if(Math.abs(oldVal.length - newVal.length) != 0) {
           	    //切换了看板
                self.$nextTick(function(){
                    if(typeof (localStorage.getItem("dailyjoin")) != 'string'){
                        $(self.scrollContariner).scrollTop(0);
                    }
                    self.lastOffset.top = 0;
                    self.lastOffset.time = 0;
                    self.widgetElements = $('.widget-content-wrapper');
                    self.startDrawTimer(1500);
                    self.startChartTimer();
                });
            }
        },
        isFullScreenWatch: function(newVal, oldVal){
            var self = this;
            if(oldVal && !newVal){
                if(self.dashBoardScrollTop != -999){
                    $(self.scrollContariner).scrollTop(self.dashBoardScrollTop);
                    self.$store.commit('setDashBoardScrollTop',{scrollTop:-999});
                }
                //找到被编辑过的图表，更新配置参数变更状态
                self.changedChartList.map(function(chartWrapper){
                    for (var i = 0; i < self.charts.length; i++) {
                        if (chartWrapper.id == self.charts[i].id) {
                            let chartWrapperClone = self.charts[i];
                            chartWrapperClone.title = chartWrapper.title;
                            chartWrapperClone.options = $.extend(true, {}, chartWrapper.options);
                            chartWrapperClone.status = 0;
                            chartWrapperClone.dataTable = null;
                            self.updateChart(i, chartWrapperClone);
                            break;
                        }
                    }
                });
                self.scanWidget();
            }
        }
    },
    methods: {
        getWidgetByType(type) {
            if(type.indexOf('table') != -1){
                return TableWidget;
            } else if(type.indexOf('chart') != -1) {
                return ChartWidget;
            } else if(type.indexOf('research') != -1) {
                if(type != 'research_stock_pie') {
                    return CustomResearchWidgte;
                } else {
                    return ChartWidget;
                }
            } else if(type.indexOf('image') != -1){
                return ImageWidget;
            }else {
                return TableWidget;
            }
        },
        onWidgetSuccess(params) {
            let chartWrapper = this.charts[params.index];
            chartWrapper.status = 4;
            this.updateChart(params.index, chartWrapper);
        },
        onWidgetError(params) {
            let chartWrapper = this.charts[params.index];
            chartWrapper.status = 3;
            this.updateChart(params.index, chartWrapper);
        },
        onUpdateWidth(index){
            //所有GridItem完成createStyle后开启监听绘制图表
            if (index + 1 == this.chartPositions.length) {
                this.startDrawTimer(200);
            }
        },
        startDrawTimer(delay, from){
            var self = this;
            if(self.drawTimerId){
                clearTimeout(self.drawTimerId);
            }
            self.drawTimerId = setTimeout(function(){
                self.scanWidget();
            }, delay || 300);
        },
        scanWidget(){
            var self = this;
            for (var i = 0; i < self.widgetElements.length; i++) {
                let $widget = $(self.widgetElements[i]);

                let chartIndex = $widget.attr('chart-index'),
                    chartWrapper = self.charts[chartIndex];
                if (typeof chartWrapper == 'undefined') {//快速切换看板此处会出现获取不到chartWrapper
                    continue;
                }
                if (chartWrapper.status == 0 || chartWrapper.status == 2) {
                    if (self.isChartVisible($widget)) {
                      self.drawChart($widget, chartIndex, chartWrapper);
                    }
                }
            }
        },
        getScrollSpeed(){
            var self = this;
            var curTime = new Date().getTime(),
                scrollTop = self.scrollContariner.scrollTop;

            if (self.lastOffset.top == 0 && self.lastOffset.time == 0) {
                self.lastOffset = {
                    top: scrollTop,
                    time: curTime
                }
            }else {
                var distance = Math.abs(scrollTop - self.lastOffset.top),
                    time = curTime - self.lastOffset.time;
                if (time >= 500) {
                    var speed = distance/time;
                    self.lastOffset = {
                        top: scrollTop,
                        time: curTime
                    }
                    return speed;
                }
            }
        },
        onScroll(){
            var self = this;
            var speed = self.getScrollSpeed();
            if (typeof speed != 'undefined' && speed <= 1) {
                self.scanWidget();
            }else{
                self.startDrawTimer(500);
            }
        },
        drawChart(element, index, chartWrapper){
        	//开始绘制图表
            var self = this;
            //是否已经获取到数据,若是则绘制,图片类型直接绘制
            if (chartWrapper.dataTable || chartWrapper.type.indexOf('image') != -1) {
                self.drawChartData(element, index, chartWrapper);
            }else{
                chartWrapper.status = 1;
                self.updateChart(index, chartWrapper);
                $.when(self.loadChartData(chartWrapper)).done(function(resp){
                    if(!self.checkDataTable(resp.table)){
                        console.error('resp is error',resp,chartWrapper);
                        chartWrapper.status = 3;
                    }else{
                        chartWrapper.status = 2;
                        chartWrapper.updateTime = (typeof resp.table.update_time != 'undefined')?parseInt(resp.table.update_time):0;
                        chartWrapper.dataTable = $.extend(true,{},resp.table);
                    }
                    self.updateChart(index, chartWrapper);
                    if (self.isChartVisible(element) && chartWrapper.status == 2) {//判断widget是否视口内,若是则绘制
                        self.drawChartData(element, index, chartWrapper);
                    }
                }).fail(function(resp){
                    chart.status = 3;
                    self.updateChart(index, chartWrapper);
                    console.error(resp.responseText || 'network error');
                });
            }
        },
        loadChartData(chartWrapper){
        	//查询图表数据
            var defer = $.Deferred();
            var self = this;
            $.get({
                url: chartWrapper.dataSourceUrl.indexOf('?') !== -1 ? chartWrapper.dataSourceUrl + "&tp=" + moment(this.curDay).format("YYYY-MM-DD") : chartWrapper.dataSourceUrl + "?tp=" + moment(this.curDay).format("YYYY-MM-DD"),
                success:function(resp){
                    //处理百度热播图表可变的线的名字图表
                        if(resp.table.series && !chartWrapper.options.series){
                            chartWrapper.options.series = resp.table.series;
                        }else{
                            // console.log('');
                        }
                    defer.resolve(resp);
                },
                timeout: 10000,
                error:function(resp){
                    if(resp.status === 401){
                        self.$router.push({
                            name: 'login'
                        })
                    }
                }
            });
            return defer.promise();
        },
        handleDrawChartData(element, index, chartWrapper) {
            var component = this.$refs[parseInt(index) + 1][0];
            if (component != null && typeof component != 'undefined') {
                component.render(element, index, chartWrapper);
            }
        },
        drawChartData(element, index, chartWrapper){//绘制图表
            var self = this;
            setTimeout(function() {
                chartWrapper.highMaps = self.highMaps;
                self.handleDrawChartData(element, index, chartWrapper);
            }, Math.random() * 100);
        },
        isChartVisible(element){
            var visibleHeight = $('.dashboard-detail-wrapper').height();
            if (element.offset().top > 0 && element.offset().top < visibleHeight) {
                return true;
            }
            return false;
        },
        updateChart(index, chartWrapper){
            var self = this;
            self.charts.splice(index, 1, chartWrapper);
        },
        onResize(index, w, h){
            var chartWrapper = this.charts[index];
            chartWrapper.isResizing = true;
            this.updateChart(index, chartWrapper);
        },
        onResized(index, h, w, wPx, hPx){
            var self = this;
            var chartWrapper = this.charts[index];
            // chartWrapper.position = w;
            // chartWrapper.position = h;
            chartWrapper.isResizing = false;
            self.updateChart(index, chartWrapper);
            let $element = $('#chart-'+chartWrapper.id);
            $element.css({
                width: (wPx+'px'),
                height: (chartWrapper.updateTime > 0?hPx-70:hPx-50)+'px'
            });
            self.drawChartData($element, index, chartWrapper);
            setTimeout(function(){
                self.$store.commit('saveDashBoardPosition', self.chartPositions);
            }, 500);

            self.startDrawTimer(500);
        },
        onMove(index, x, y){
        },
        onMoved(index, x, y){
            var self = this;
            setTimeout(function(){
                self.$store.commit('saveDashBoardPosition', self.chartPositions);
            }, 500);
            self.startDrawTimer(500);
        },
        //点击全屏传参事件
        onHandleWidgetEvent(eventType, _huang){
        	if (eventType == 'fullscreen'){
        		   this.chartFullScreen(_huang.id, _huang.index);
            } else if (eventType == 'copy-to'){
                this.chartCopyTo(_huang.id, _huang.index);
            } else if (eventType == 'move-to'){
                this.chartMoveTo(_huang.id, _huang.index);
            } else if (eventType == 'delete'){
                this.chartDelete(_huang.id, _huang.index);
            } else if(eventType == 'export-data'){
                this.chartExportData(_huang.id, _huang.index);
            } else if(eventType == 'export-image'){
                this.chartExportImage(_huang.id, _huang.index);
            } else if(eventType == 'rename'){
                this.isEditingWidget = false;
                this.chartRename(_huang.id, _huang.index, _huang.options);
            } else if(eventType == 'edit-name'){
                //重命名图表时禁止drag和resize
                this.isEditingWidget = _huang.options;
            }
        },
        //正常全屏
        chartFullScreen(chartId, chartIndex){
            var self = this;
            var chartWrapper = self.charts[chartIndex];
            GaHelper.sendEvent(GaHelper.Dashboard.fullwatch, this.dashboardName + '看板-' + chartWrapper.title + '图表');
            if(chartWrapper.chartType == 'image') return;
            let scrollTop = self.scrollContariner.scrollTop;
            self.$store.commit('setDashBoardScrollTop',{scrollTop:scrollTop});
            self.$store.commit('setCurWatchChartIdx',{idx:chartIndex});
            self.$store.commit('ky_setFromPath', {path:self.$route.path});
            self.$router.push({
                name: 'fullscreen',
            });
        },
        chartExportData(chartId, chartIndex){
            var self = this;
            console.info(this.charts,chartIndex)
            var chartWrapper = this.charts[chartIndex];
            if(chartWrapper.status == 4){
                if(chartWrapper.type == 'research_stock_card' || chartWrapper.type == 'research_data_list'){
                    FileExport.export({
                        type: 'excel',
                        data: self.getResearchWidgetData(chartWrapper.dataTable.data),
                        fname: chartWrapper.title
                    });
                }else{
                    FileExport.export({
                        type: 'excel',
                        data: ChartHelper.getExportData(chartWrapper, chartWrapper.dataTable),
                        fname: chartWrapper.title
                    });
                }
            }
        },
        chartExportImage(chartId, chartIndex){
            var chartWrapper = this.charts[chartIndex];
            FileExport.export({
                type: 'png',
                data: {
                    sId: chartWrapper.chartType == 'image'? chartWrapper.image_url: ('widget-'+chartIndex),
                    stype: chartWrapper.chartType == 'image'?'img':'highchart'
                },
                fname: chartWrapper.title
            });
        },
        getResearchWidgetData(config) {//转换腾讯看板数据
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
        startChartTimer(){
            var self = this;
            ChartTimer.clear();
            for (var i = 0; i < self.charts.length; i++) {
                let chartWrapper = self.charts[i];
                if (typeof chartWrapper.updateFreq != 'undefined') {
                    ChartTimer.set(chartWrapper.id, {
                        callback: self.onHandleChartTimer,
                        index: i,
                        timer: null,
                        chartWrapper: chartWrapper
                    });
                    ChartTimer.startTimer(chartWrapper);
                }
            }
        },
        onHandleChartTimer(chartIndex, chartWrapper, dataTable){
            var self = this;

            var $element = $('#chart-'+chartWrapper.id);
            if (self.isChartVisible($element)) {
                let chartWrapperOrignal = self.charts[chartIndex];
                if (self.checkDataTable(dataTable)) {
                    chartWrapperOrignal.status = 2;
                    chartWrapperOrignal.dataTable = $.extend(true, {}, dataTable);
                    chartWrapperOrignal.updateTime = (typeof dataTable.data_update_time != 'undefined')?parseInt(dataTable.data_update_time):0;
                    self.updateChart(chartIndex, chartWrapperOrignal);
                    self.drawChartData($element, chartIndex, chartWrapperOrignal);
                }else {
                    chartWrapperOrignal.status = 3;
                    chartWrapperOrignal.dataTable = null;
                    self.updateChart(chartIndex, chartWrapperOrignal);
                }
            }
        },
        checkDataTable(dataTable){
            return (dataTable && dataTable.data && dataTable.data.rows && dataTable.data.rows != null
                && dataTable.data.rows.length > 0);
        }
    }
}
</script>

<style type="text/css">
    .vue-grid-layout {
        width: 100%;
    }
    .vue-grid-item {
        background-color: #fff;
    }
    .vue-grid-item > .vue-resizable-handle{
        height: 10px!important;
        width: 10px!important;
    }
</style>

<style type="text/css" scoped>
    .dashboard-content-isloading-wrapper .loader8,
    .dashboard-content-isloading-wrapper .loader8:before,
    .dashboard-content-isloading-wrapper .loader8:after {
        background: #0facd9;
        -webkit-animation: load1 1s infinite ease-in-out;
        animation: load1 1s infinite ease-in-out;
        width: 0.7em;
        height: 2em;
    }
    .dashboard-content-isloading-wrapper .loader8 {
        color: #0facd9;
        text-indent: -9999em;
        margin: 200px auto;
        position: relative;
        font-size: 11px;
        -webkit-transform: translateZ(0);
        -ms-transform: translateZ(0);
        transform: translateZ(0);
        -webkit-animation-delay: -0.16s;
        animation-delay: -0.16s;
    }
    .dashboard-content-isloading-wrapper .loader8:before,
    .dashboard-content-isloading-wrapper .loader8:after {
        position: absolute;
        top: 0;
        content: '';
    }
    .dashboard-content-isloading-wrapper .loader8:before {
        left: -1.5em;
        -webkit-animation-delay: -0.32s;
        animation-delay: -0.32s;
    }
    .dashboard-content-isloading-wrapper .loader8:after {
        left: 1.5em;
    }
    @-webkit-keyframes load1 {
        0%,
        80%,
        100% {
            box-shadow: 0 0;
            height: 2em;
        }
        40% {
            box-shadow: 0 -2em;
            height: 3em;
        }
    }
    @keyframes load1 {
        0%,
        80%,
        100% {
            box-shadow: 0 0;
            height: 2em;
        }
        40% {
            box-shadow: 0 -2em;
            height: 3em;
        }
    }
</style>
<style lang="css" scoped>
    .dashboard-content-wrapper {
        position: absolute;
        top: 0px;
        left: 0px;
        bottom: 0px;
        width: 100%;
        background-color: #151922;
    }
    .dashboard-content-isloading-wrapper,
    .dashboard-content-success-wrapper,
    .dashboard-content-failed-wrapper {
        width: 100%;
        height: 100%;
        overflow: auto;
    }
    .dashboard-content-success-wrapper .vue-grid-layout{
        background: #151922;
    }
    .dashboard-content-success-wrapper .vue-grid-item{
        background: #1b1f29;
    }
    .dashboard-content-failed-wrapper {
        padding: 45px 0px;
    }
    .dashboard-content-failed-wrapper .background{
        margin: 0 auto;
        width: 190px;
        height: 215px;
        background-image: url('/images/img-load-db-error.png');
        background-position: center;
        background-repeat: no-repeat;
        background-size: 190px 215px;
    }
    .dashboard-content-failed-wrapper .tip,
    .dashboard-content-failed-wrapper .sub-tip  {
        margin: 10px auto;
        width: 300px;
        text-align: center;
        font-size: 22px;
        color: #666;
    }
    .dashboard-content-failed-wrapper .sub-tip {
        font-size: 12px;
    }
    .dashboard-content-empty-wrapper {
        display: -webkit-flex;
        display: flex;
        justify-content: center;
        align-items: center;
        margin: 10px;
        padding: 0px;
        width: 400px;
        height: 300px;
        background-color: #fff;
    }
    .dashboard-content-empty-wrapper .icon {
        padding: 0px 0px 0px 33px;
        width: 120px;
        height: 40px;
        line-height: 40px;
        color: #666;
        background-size: 28px 28px;
        background-repeat: no-repeat;
        background-position: 0px 50%;
        background-image: url('/images/icon-def-db-add-chart.png');
        cursor: pointer;
    }
    .dashboard-content-footer-wrapper {
        width: 100%;
        height: 50px;
        line-height: 50px;
        text-align: center;
        color: #7985a3;
        background: #151922;
    }
</style>
