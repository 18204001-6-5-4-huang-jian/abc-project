 <template lang="html">
    <div class="cc-chart-content-panel-wrapper">
        <div class="cc-body" :class="{'editing':showEditPanel}">
            <!-- 引入组件charttag -->
           <ChartTag :_left="tagLeft" :_top="tagTop" :sc="sc" @changetagcolor="changetagcolor" :tagpoint="tagpoint" :admin="admin" :newtag="newtag" :spantext="spantext"
            @changenewtag="changenewtag" @changespantext="changespantext" @savepoint="savepoint" :cleantagtext="cleantagtext"
            :tagxname="tagxname" :taglinename="taglinename">
           </ChartTag>
            <!--chart-->
            <div id="cc-chart-container" v-show=" showtype == 'chart'">
                
            </div>
            <!--表格table-->
            <div class="cc-table-content" v-show=" showtype == 'table'">
                <div class="cc-table-header">
                    <div class="cc-table-data-count">
                                                 共&nbsp;<span v-text="dataCount"></span>&nbsp;条数据
                    </div>
                    <div class="cc-table-data-time" v-show="dataUpdateTime">
                                                更新时间:&nbsp;<span v-text="dataUpdateTime"></span>
                    </div>
                </div>
                <div class="cc_search_toolbar" v-if="haveTool">
                    <div class="cc_st_inputs" v-for="(bar,index) in toolbars" :key="index">
                             {{bar.text}}
                        <input type="text" v-if="(bar.type=='date')" :isdate="true" :name="bar.name" :placeholder="('请输入'+bar.text)" data-date-format="yyyy-mm-dd" @keydown.enter="cc_search_now" readonly>
                         
                        <input :type="bar.type" v-if="(bar.type!='date')" :name="bar.name" :placeholder="('请输入'+bar.text)" @keydown.enter="cc_search_now">
                    </div>
                    <button @click="cc_search_now">查 询</button>
                </div>
                <div id="cc-table-container">
                      
                </div>
            </div>
            <!--说明caption-->
            <div class="cc-caption-content" v-show= "showtype == 'caption'">
                <div class="caption_wrapper_list">
                    <div class="caption_list_title">
                        <span></span>
                                                                             图表说明                                        
                    </div>
                    <div class="nocaption">
                        <img src="../../../webapp/images/tag/icon-write.png" v-show=" admin == 1 "/>
                            <textarea v-model="message"  :readonly="read"
                        @keydown.enter.prevent="saveText" @blur="saveText" >
                            </textarea>
                    </div>
                </div>
                <div class="caption_wrapper_list">
                    <div class="caption_list_title">
                        <span></span>
                                                                               图表标注                                        
                     <div class="title_div" v-for="(item,index) in arr">
                          <p :style="{background:item.marker.fillColor}"></p>
                          <h6>{{item.marker.text}}</h6><!--文本内容-->
                          <h5>{{item.series_name}}</h5><!--线的名字-->
                          <h4>{{item.point_id | h_timechange}}</h4><!--时间戳-->
                          <h3 @click="changeDime(index,$event)"><img src="../../../webapp/images/tag/icon-write-hover.png" id="_img"  @click="changeDime(index,$event)"/>&nbsp;&nbsp;编辑</h3>
                     </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import ChartTag from '../common/charttag.vue'
export default{
    name:'chart-content-panel',
    components:{
       ChartTag
    },
    data(){
        return{
            dataCount:0,
            updateTimer: null,
            haveTool:false,
            toolbars:[],
            tagLeft: 0,
            tagTop: 0,
            tagpoint: null,
            admin:0,
            exports:0,
            newtag:false,
            cleantagtext:false,
            sc:"#ED6723",
            spantext: '',
            tagxname: '',
            taglinename:'',
            arr:[],
            message:'图表还没有说明，点击添加',
            read:"readonly",
            fillcolor:""
        }
    },
    mounted(){
    	//用来判断说明和标注
        this.admin = parseInt(sessionStorage.marker);
        //显示setFullScreenWatch
        this.$store.commit('setFullScreenWatch',{isWatch:true});
        if(!this.curWatchChart){
            //隐藏setFullScreenWatch并且进行跳转dashboard页面
            this.$store.commit('setFullScreenWatch',{isWatch:false});
            this.$router.push({name:'dashboard'});
            return false;
        }
    },
    computed:{
        isCustomChart(){
            return this.curWatchChart ? this.curWatchChart.type === 'research_data_list'||this.curWatchChart.type === 'research_stock_card' : true;
        },
        isFromShareBoard(){
            return this.dashBoard ? (this.dashBoard.creator_id != sessionStorage.uid):false;
        },
        showEditPanel(){
            return this.isEditStyle && !this.isFullScreenWatchTable;
        },
        isEditStyle(){
            return this.$store.state.fullscreen.isEditStyle;
        },
        isFullScreenWatch(){
            return this.$store.state.fullscreen.isFullScreenWatch;
        },
        isFullScreenWatchTable(){
            return this.$store.state.fullscreen.isFullScreenWatchTable;
        },
        isSwitchingChart(){
            return this.$store.state.fullscreen.isSwitchingChart;
        },
        isShowChartList(){
            return this.$store.state.fullscreen.isShowChartList;
        },
        dashBoard(){
            return this.$store.state.dashboard.curDashBoard;
        },
        curWatchChartIdx(){
            return this.$store.state.fullscreen.curWatchChartIdx;
        },
         //当前看板信息
        curWatchChart(){
            if(this.dashBoard && this.isFullScreenWatch){
                var chart = this.dashBoard.charts[this.curWatchChartIdx];
                return chart?chart:null;
            }else{
                return null;
            }
        },
        curWatchChart_copy(){
            return _.cloneDeep(this.$store.state.fullscreen.curWatchChart_copy);
        },
        isTableChart(){
            if(this.curWatchChart && this.isFullScreenWatch){
                let optionType = this.curWatchChart.options && this.curWatchChart.options.chart && this.curWatchChart.options.chart.type;
                return this.curWatchChart.chartType == 'table' || this.curWatchChart.type.indexOf('table') != -1 || optionType == 'table' && this.curWatchChart.type.indexOf('research') == -1;
            }
            return false;
        },
        dataUpdateTime(){
            if(this.curWatchChart && this.isFullScreenWatch){
                if(this.curWatchChart.dataTable && this.curWatchChart.dataTable.chart_update_time){
                    var date = new Date(parseInt(this.curWatchChart.dataTable.chart_update_time));
                    return [date.getFullYear(),date.getMonth()+1,date.getDate()].join('-').replace(/(?=\b\d\b)/g, '0');
                }else{
                    return '';
                }
            }else{
                return '';
            }
        },
        country(){
            return this.$store.state.dashboard.lang?'cn':'en'
        },
        curDay(){
            return this.$store.state.dashboard.curDay;
        },
        showtype(){
            return this.$store.state.fullscreen.showtype;
        },
    },
    watch:{
        isFullScreenWatch: function(newVal){
            if(!newVal) return false;
            // 清除修改列表缓存
            this.$store.commit('zk_resetFullscreenState');
            this.$store.commit('zk_setCurWatchChart_copy',{
                from: '全屏切换',
                chart: _.cloneDeep(this.curWatchChart)
            });
        },
        isFullScreenWatchTable: function(newVal){
        	var self = this;
            if(newVal){
                //在DOM更新之后执行
                this.$nextTick(self.drawTable());
            }else{
                this.$nextTick(self.drawChart());
            }
        },
        curWatchChart_copy(val){
            if(!val){
                return false;
            }
            if(this.isTableChart){
                this.$store.commit('setFullScreenWatchTable',{isWatch:true});
            }else{
                if(this.updateTimer){
                    window.clearTimeout(this.updateTimer)
                }
                this.updateTimer = window.setTimeout(()=>{
                    this.drawChart();
                },0);
            }
        },
        showEditPanel(val){
            if(this.updateTimer){
                window.clearTimeout(this.updateTimer)
            }
            this.updateTimer = window.setTimeout(()=>{
                if(!this.isFullScreenWatchTable){
                    this.drawChart()
                }
            },0);
        },
        /*监听是否切换说明*/
        showtype(newval){
          var self = this;
          if(self.admin == 0 || self.admin == null){
            self.read = "readonly";
          }else{
          	//可以编辑说明
            if(self.admin == 1){
                self.read = null;
            }
          }
          //存在description
          if(self.curWatchChart.description){
             self.message = self.curWatchChart.description;
         }else{
            //不存在判断admin的权限
            if(self.admin == 0 || self.admin == null){
               self.message = "图表暂时没有说明,您不可编辑";
            }
         }
            if(newval  ==  "caption"){
                $.get({
                    url:"/api/v1/chart/markers/"+self.curWatchChart.id,
                    contentType:'application/json;chartset=utf-8',
                    success:(res)=>{
                        console.log(res);
                        if (!res.success){
                            layer.msg(res.message);
                        }else{
                             self.arr = res.data.list;
                        }
                    },
                    complete(){
                        console.log("----请求标注数据成功----");
                    }
                })
            }
        }
    },
    methods:{
        //获取图表数据
        drawChart(){
            var self = this;
            if(!self.curWatchChart){
                console.warn('curWatchChart is null');
                return false;
            }
            if(this.isTableChart){
                console.warn('wrong draw');
                return false;
            }
            $('#cc-chart-container').empty(); 
            var chart = _.cloneDeep(self.curWatchChart);
            var load_index = layer.load(2);
            $('.layui-layer-loading span.layui-layer-setwin').html('加载中...');
            $.get({
                url: chart.dataSourceUrl.indexOf('?') !== -1 ? chart.dataSourceUrl + "&tp=" + moment(this.curDay).format("YYYY-MM-DD") : chart.dataSourceUrl + "?tp=" + moment(this.curDay).format("YYYY-MM-DD"),
                success:function(resp){
                    layer.close(load_index);
                    if(resp.table && resp.table.data){
                        chart.dataTable = _.cloneDeep(resp.table);
                        self.$store.commit('ky_setChartOriginalData',{
                            id: chart.id,
                            data: resp.table.data.rows.splice(0)
                        });
                        self.$store.commit('setCurFullChart', {
                            data: ChartHelper.getExportData(chart, chart.dataTable),
                            title: chart.title,
                        });
                        if(chart.type == 'research_stock_card'){
                            ChartHelper.drawResearchStockCard('cc-chart-container', {
                                params: $.extend({},chart.dataTable.data.params,{country:self.country}),
                                rows: chart.dataTable.data.rows,
                            }, true);
                        }else if(chart.type == 'research_data_list'){
        
                            ChartHelper.drawResearchDataList('cc-chart-container', {
                                params: chart.dataTable.data.params,
                                rows: chart.dataTable.data.rows
                            }, true);
                        }else {
                            self.drawChartData(chart.dataTable.data, chart);
                        }
                    }else{
                        console.error(resp);
                        new PNotify({
                            text:'绘制失败',
                            type:'error'
                        });
                    }
                },
                error:function(resp){
                    layer.close(load_index);
                    console.error(resp);
                    if(resp.status === 401){
                        //跳转login页面
                        self.$router.push({
                            name: 'login'
                        })
                        return false;
                    }
                    new PNotify({
                        text:'绘制失败',
                        type:'error'
                    });
                }
            });
        },
        //配置图表参数，绘制图表
        drawChartData(data, chart){
            var self = this;
            $('#cc-chart-container').empty();
            try{
                let highChartConfig = ChartHelper.getLiveChartConfigV2(chart, {
                    width: $('.cc-body').width(),
                    height: $('.cc-body').height()-60,//减去padding
                    backgroundColor: '#1b1f29',
                    borderColor: '#262a36',
                    xAxis:{
                        lineColor: '#262a36',
                        gridLineColor: '#262a36'
                    },
                    yAxis:{
                        lineColor: '#262a36',
                        gridLineColor: '#262a36'
                    },
                    mode: 'fullscreen',
                     //是开启标注
                    tagging: true,
                    admin: (self.admin == 1),
                    tagfn:{
                        click:function(info){
                            setTimeout(function(){
                                //判断标注存在
                                if(info.point.marker&&info.point.marker.text){
                                    //标注存在
                                    self.spantext = info.point.marker.text;
                                    self.sc =  info.point.marker.fillColor;
                                    self.newtag = false;
                                    $("#charttag").show(100);
                                }else{
                                    //标注不存在
                                    //管理员新标注点
                                    if(self.admin == 1){
                                        self.newtag = true;
                                        self.sc = "#ED6723";
                                        $("#charttag").show(100);
                                        setTimeout(function(){
                                            $("#tagtextinput").focus();
                                        },300);
                                    }else{
                                        //非管理
                                        return;
                                    }
                                }
                                //点xy信息
                                if(info.point.name){
                                    self.tagxname = info.point.name;
                                }else{
                                    self.tagxname = new Date(info.point.x).toLocaleDateString();
                                }
                                if(info.point.series.name){
                                    self.taglinename = info.point.series.name;
                                }
                                //弹窗位置控制
                                if(info.point.plotX + 330 > $('.cc-chart-content-panel-wrapper').width()){
                                    self.tagLeft = info.point.plotX - 270;
                                    $(".triangleout").hide();
                                    $(".triangleoutright").show();
                                }else{
                                    self.tagLeft = info.point.plotX + 95;
                                    $(".triangleout").show();
                                    $(".triangleoutright").hide();
                                }
                                self.tagTop = info.point.plotY + 45;
                                self.tagpoint =  info.point;
                                //添加消失事件
                                $(".abcdata-wrapper").one("click",function(e){
                                    //消失之前判断有输入则保存 没输入取消点
                                    var _input = $("#tagtextinput");
                                    if(!_input.is(":hidden")){//如果是输入状态
                                        // console.log(info.point)
                                        if($.trim(_input.val()).length>0){
                                            self.savepoint(info.point,{
                                                fillColor: $(".color-bar>.active>i").css("background-color"),
                                                text: _input.val(),
                                                enabled: true,
                                                lineColor: "#fff",
                                                radius: 8,
                                                lineWidth: 2,
                                            })
                                        }else{
                                            self.savepoint(info.point,undefined)
                                        }
                                    }
                                    //消除输入框文字
                                    self.cleantagtext = true;
                                    $("#charttag").hide();
                                    setTimeout(function(){
                                        self.cleantagtext = false;
                                    },100)
                                })
                            },100);
                            
                        },
                        mouseOut:function(){
                            //self.tagShow = false;
                        },
                    }
                });
                if(highChartConfig._type == 'Map'){
                    $('#cc-chart-container').highcharts('Map', highChartConfig);
                }else if(highChartConfig._type == 'StockChart'){
                    $('#cc-chart-container').highcharts('StockChart', highChartConfig);
                }else{
                    $('#cc-chart-container').highcharts(highChartConfig);
                }
            } catch(e) {
                console.warn(e);
            }
            this.$store.commit('setSwitchingChart',{isSwitching:false});
        },
        //获取表格数据
        drawTable(){
            var self = this;
            if(!this.curWatchChart){
                console.warn('curWatchChart is null');
                return;
            }
            var chart = _.cloneDeep(this.curWatchChart);
            var load_index = layer.load(2);
            $('.layui-layer-loading span.layui-layer-setwin').html('加载中...');
            $.get({
                url: chart.dataSourceUrl.indexOf('?') !== -1 ? chart.dataSourceUrl + "&tp=" + moment(this.curDay).format("YYYY-MM-DD") : chart.dataSourceUrl + "?tp=" + moment(this.curDay).format("YYYY-MM-DD"),
                success:function(resp){
                    if(resp.table && resp.table.data){
                        self.$store.commit('setCurFullChart', {
                            data: ChartHelper.getExportData(chart, resp.table),
                            title: ''
                        });
                        //存在特定数据展示
                        if (typeof resp.table.table != 'undefined' && resp.table.table) {
                            self.drawTableData(resp.table.table);
                        }else {
                            self.drawTableData(self.getPieWidgetData(resp.table.data));
                        }
                    }else{
                        console.error(resp);
                        $('#cc-table-container').empty();
                        self.dataCount = 0;
                        if(!chart.toolbar){
                        	//存在toolbar字段，港股通数据,查询失败不报错
                            new PNotify({
                                text:'绘制失败',
                                type:'error'
                            });
                        }
                    }
                    layer.close(load_index);
                },
                error:function(resp){
                    layer.close(load_index);
                    console.error(resp);
                    if(resp.status === 401){
                        self.$router.push({
                            name: 'login'
                        })
                        return false;
                    }
                    $('#cc-table-container').empty();
                    self.dataCount = 0;
                    new PNotify({
                        text:'绘制失败',
                        type:'error'
                    });
                }
            });
        },
         //配置表格参数,绘制表格
        drawTableData(data){
            var self = this;
            let width = $('.cc-body').width();
            let height = $('.cc-body').height()-35;
            if(this.haveTool){
                height = height - 65;
                if(!data.rows || data.rows.length==0){
                    $('#cc-table-container').empty();
                    new PNotify({
                        text:'暂无数据',
                        type:'error'
                    })
                    return;
                }
            }

            if(!data.rows || !data.rows.length){
                $('#cc-table-container').empty();
                new PNotify({
                    text:'数据格式错误',
                    type:'error'
                })
                 return;
            }
            self.dataCount = data.rows.length-1;
            // 自定义组件
            if(data.hasOwnProperty('params')){
                let _rows = this.getResearchWidgetData(data);
                data.rows = _rows;
            }
             
            var colNum = data.rows[0].length || Object.keys(data.rows[0] || {}).length;
            if(data.rows[0][0] == 'date' || data.rows[0][0] == 'datetime'){
                data.rows[0][0] = '日期';
            }
            var headRows = [];
            for (var i = 0; i < data.rows.length; i++) {
                let isHeader = true;
                for (var j = 0; j < data.rows[i].length; j++) {
                    if (!isNaN(parseInt(data.rows[i][j]))) {
                        isHeader = false;
                    }
                }
                if (!isHeader) {
                    break;
                }else {
                    headRows.push(data.rows[i]);
                }
            }

            var config = {
                data: data.rows,
                width: width,
                height: height,
                manualColumnResize: true,
                fixedRowsTop: headRows.length > 0?headRows.length:1,
                cells: function (row, col, prop) {
                    return {
                        readOnly: true
                    };
                }
            };
            if(!colNum || colNum <= 1 || colNum > 20){
                config.stretchH = 'all';
                config.wordWrap= false;
            } else {
                config.colWidths = ((width-10) / colNum);
            }

            //只支持时间维度且类型为时间戳的格式化
            let isDateTime = false;
            //判断是否是chart
            if (this.curWatchChart.options && this.curWatchChart.type.indexOf('chart') != -1){
                //判断是否是饼图，雷达图
                if(this.curWatchChart.chartType && this.curWatchChart.chartType.indexOf('pie') == -1
                    && this.curWatchChart.chartType.indexOf('polar') == -1
                    && this.curWatchChart.chartType.indexOf('radar') == -1) {
                    //判断是否存在时间维度
                    if (this.curWatchChart.options.xAxis) {
                        if (this.curWatchChart.options.xAxis.type == 'datetime') {
                            isDateTime = true;
                        }
                    }
                }
            }
            //简单粗暴，只要首列的第一个标示是‘日期’均标示时间类型
            if (data.rows[0][0] == '日期' || data.rows[0][0] == '时间') {
                isDateTime = true;
            }

            if (isDateTime && data.rows.length > 1 && $.isNumeric(data.rows[1][0])){
                config.columns = [{
                    renderer:self.formatDate
                }]
                for(var i = 0; i < colNum-1; i++){
                    config.columns.push({});
                }
            }

            new Handsontable(document.getElementById("cc-table-container"),config);
            this.$store.commit('setSwitchingChart',{isSwitching:false});
        },
        //转置ResearchData，可以在表格中显示
        getResearchWidgetData(config){
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
        getPieWidgetData(data){
            if (this.curWatchChart.type.indexOf('chart') != -1
                && this.curWatchChart.chartType
                && (this.curWatchChart.chartType.indexOf('pie') != -1
                    || this.curWatchChart.chartType.indexOf('polar') != -1
                    || this.curWatchChart.chartType.indexOf('radar') != -1)) {
                var _data = {},
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
        formatDate(instance, td, row, col, prop, value, cellProperties){
            if($.isNumeric(value)){
                var date = new Date(parseInt(value));
                td.innerHTML = [date.getFullYear(),date.getMonth()+1,date.getDate()].join('-').replace(/(?=\b\d\b)/g, '0');
            }else{
                td.innerHTML = value;
            }
            return td;
        },
       
        getLocalTime(_time){
            var _date = new Date(parseInt(_time));
            var year = _date.getFullYear();
            var month = _date.getMonth() + 1;
            if(month<10){month = '0' + month};
            var day = _date.getDate();
            if(day<10){day = '0' + day};
            return (year+ '-' +month+ '-' +day);
        },
        beginEdit(){
            this.$store.commit('zk_toggleEditPanel',true);
        },
        changenewtag(state){
            this.newtag = state;
        },
        changespantext(text){
            this.spantext = text;
        },
        //保存标注点信息
        savepoint(point,_marker){
            // console.log(point);
            if(_marker){
                var self = this;
                console.log("-----保存标注------");
                $.post({
                    //设置数据点标注信息
                    url:"/api/v1/chart/set-marker/" + self.curWatchChart.id,
                    contentType: 'application/json;chartset=utf-8',
                    data:JSON.stringify({
                        series_id: point.series.index,
                        point_id: point.name?point.name:point.x,//有name优先使用name
                        marker: _marker,
                        series_name:point.series.name
                    }),
                    success:(resp)=>{
                        if(self.showtype != "caption"){
                            point.update({
                            marker: _marker
                        })
                      }
                    },
                    complete(){
                        
                    }
                })
            }else{
                //取消数据点标注
                var self = this;
                $.post({
                    url:"/api/v1/chart/unset-marker/" + self.curWatchChart.id,
                    contentType:'application/json;chartset=utf-8',
                    data:JSON.stringify({
                        series_id: point.series.index,
                        point_id: point.name?point.name:point.x,
                        series_name:point.series.name
                    }),
                    success:(resp)=>{
                        point.update({
                            marker: _marker
                        });
                    },
                    complete(){
                        
                    }
                })
                
            }
        },
        changetagcolor(color){
            this.sc = color;
        },
        
       //编辑说明
        saveText(){
            console.log("------编辑说明------");
            var self = this;
            self.curWatchChart.description = self.message;
             $.post({
                  url:"/api/v1/chart/set-desp/"+self.curWatchChart.id,
                  contentType:'application/json;chartset=utf-8',
                  data:JSON.stringify({
                  description:self.message
                 }),
                 success:(res)=>{
                     console.log(res);
                  },
                 complete(){
                      console.log("------修改说明成功------");
                  }
              })
         },      
      //编辑标注
      changeDime(index,e){
        var self = this;
        // console.log(self.admin);
        if(self.admin == 1){
        $(".triangleoutright").show();
        $(".triangleout").hide();
        $("#charttag").show(100,function(){
                console.log("------子组件charttag---------");
            });
            self.tagpoint = {
                series:{
                    name : self.arr[index].series_name,//线的名字    
                    index: self.arr[index].series_id//索引
                },
                x:self.arr[index].point_id//x轴
            }
            self.tagLeft = e.target.offsetLeft-350;
            self.tagTop  = e.target.offsetTop +250;
            self.spantext= self.arr[index].marker.text;
            self.sc = self.arr[index].marker.fillColor;
            self.taglinename = self.arr[index].series_name;
            //时间戳转换补零
            function add0(m) {
                return m < 10 ? '0' + m : m
            }
            var time = new Date(parseInt(self.arr[index].point_id));
            var y = time.getFullYear();
            var m = time.getMonth() + 1;
            var d = time.getDate();
            self.tagxname = y + '.' + add0(m) + '.' + add0(d);
            setTimeout(function(){
              $(".abcdata-wrapper").one("click",function(e){
                    //消失之前判断有输入则保存 没输入取消点
                    var _input = $("#tagtextinput");
                    if(!_input.is(":hidden")){//如果是输入状态
                        if($.trim(_input.val()).length>0){
                            self.savepoint(info.point,{
                                fillColor: $(".color-bar>.active>i").css("background-color"),
                                text: _input.val(),
                                enabled: true,
                                lineColor: "#fff",
                                radius: 8,
                                lineWidth: 2,
                            })
                        }else{
                            self.savepoint(info.point,undefined)
                        }
                    }
                    //消除输入框文字
                    self.cleantagtext = true;
                    $("#charttag").hide();
                    setTimeout(function(){
                    self.cleantagtext = false;
                    },100)
               })
            },100);
         }else{
                new PNotify({
                        text:'您没有权限进行标注',
                        type:'error'
                })
             }
          }
      }
}
</script>
<style type="text/css" lang="less">
    .cc-chart-content-panel-wrapper {
        position: absolute;
        left: 0px;
        top: 50px;
        right: 0px;
        bottom: 0px;
        padding: 0px;
        margin: 0px;
        width: 100%;
    }
    .cc-body {
        padding: 40px 20px 20px 20px;
        width: 100%;
        height: 100%;
    }
    #cc-chart-container {
        width: 100%;
        height:100%;
        overflow-y:auto;
    }
    .cc-table-content {
        width: 100%;
        height:100%;
        position: relative;
        overflow-y: auto;
        overflow-x: hidden;
    }
    .cc-table-header {
        display: flex;
        align-items: center;
        width: 100%;
        padding: 0;
        margin: 0 0 5px;
        height: 30px;
        position: relative;
    }
    .cc-table-data-count,
    .cc-table-data-time {
        margin: 0px 10px;
        font-size: 12px;
    }
    /*table-container样式*/
    #cc-table-container{
        width: 100%;
        height: calc(100% - 35px);
        height: -webkit-calc(100% - 35px);
        color: #79839c;
        overflow-y: auto;
    }
    #cc-table-container td{
        background: inherit;
        text-align: left;
        vertical-align: middle;
    }
    #cc-table-container tbody > tr:nth-child(2n) > td {background: #1b1f29;}
    #cc-table-container tbody > tr:nth-child(2n+1) > td {background: #20242e;}
    #cc-table-container .handsontable th{
        background-color: #20242e;
    }
    #cc-table-container .handsontable th,
    #cc-table-container .handsontable td{
        line-height: 18px;
        padding: 6px 0;
        text-align: center;
        border:0!important;
        color: #7985a3!important;
        box-sizing: border-box;
    }
    #cc-table-container .ht_clone_top tbody > tr > td{
        background-color: #20242e;
        font-weight: bold;
    }
    #cc-table-container tbody > tr > td:nth-child(1){
        padding-left: 15px;
    }
    #cc-table-container .ht_master tbody > tr > td{
        font-size: 12px;
    }
    .cc_search_date{
        height: 100%;
        color: #287ddc;
        border: 1px solid #fff;
        width: 90px;
        display: flex;
        align-items: center;
        justify-content:center;
        font-size:12px;
        float: right;
        z-index:999;
        position: absolute;
        right: 0;
        top: 0;
        cursor: pointer;
    }
    .cc_search_date.active{
        border-color:#ddd;
        border-bottom-color: #fdfdfd;
        background-color: #fdfdfd;
    }
    .cc_search_date img{
        margin-left: 5px;
        transition: all 0.3s linear;
    }
    .cc_search_date.active img{
        transform: rotate(-180deg);
    }
    .cc_search_toolbar{
        height: 55px;
        width: 100%;
        background-color: #fdfdfd;
        border: 1px solid #ddd;
        padding: 0 20px;
        display: flex;
        align-items: center;
        z-index: 99;
        margin-top: -6px;
        margin-bottom: 15px;
    }
    .cc_st_inputs{
        height: 100%;
        display: flex;
        align-items: center;
        font-size: 12px;
        color: #333;
        margin-right: 20px;
    }
    .cc_st_inputs input{
        width: 150px;
        margin-left: 20px;
        border: 1px solid #eee;
        height: 30px;
        outline: none;
        text-indent: 10px;
        line-height: 30px;
    }
    .cc_st_inputs input::-webkit-input-placeholder { /* WebKit browsers */
        color: #999;
    }
    .cc_st_inputs input:-moz-placeholder { /* Mozilla Firefox 4 to 18 */
        color: #999;
    }
    .cc_st_inputs input::-moz-placeholder { /* Mozilla Firefox 19+ */
        color: #999;
    }
    .cc_st_inputs input:-ms-input-placeholder { /* Internet Explorer 10+ */
        color: #999;
    }
    .cc_search_toolbar button{
        height: 30px;
        width: 100px;
        background-color: #287ddc;
        color: #fff;
        outline: none;
        border:none;
        font-size: 12px;
    }
    .cc_search_toolbar button:hover{
        opacity: 0.8;
    }
    /*图标样式编辑*/
    .edit-chart-style{
        position: absolute;
        right: 20px;
        top: 5px;
        font-size: 12px;
        color: #666;
        cursor: pointer;
        transition: all 1 0.3s;
    }
    .edit-chart-style .icon-edit{
        width: 20px;
        height: 20px;
        display: inline-block;
        margin: 5px;
        vertical-align: -11px;
        background: url(/images/icon-edit-normal.png) no-repeat center;
        background-size: 100%;
    }
    .edit-chart-style:hover{
        color: #287ddc;
    }
    .edit-chart-style:hover .icon-edit{
        background-image: url(/images/icon-edit-hover.png);
    }
    .cc-body.editing {
        width: calc(100% - 220px);
    }
    .chart-style-wrapper{
        position: absolute;
        right: 0;
        top: 0;
        border-left: 1px solid #eee;
        width: 219px;
        height: 100%;
        background: #eff3f6;
    }
    .caption_wrapper_list{
        padding: 0 20px 20px;
        .caption_list_title{
            span{
                display: inline-block;
                height: 16px;
                width: 3px;
                margin-right: 8px;
                margin-top: -2px;
                background-color: #0FACD9;
                vertical-align: middle;
            }
            color:#0FACD9;
        }
    }
    .cc-caption-content{
    	width: 100%;
    	height:850px;
    	overflow-y: scroll;
    }
   .cc-caption-content .caption_wrapper_list:nth-child(2){
   	   width: 100%;
       padding-top:6.4%;
   }
   .caption_wrapper_list .title_div{
        width: 1440px;
        height: 80px;
        line-height:80px;
        border: 1px solid #596175;
        margin: 10px 0;
        position: relative;
    }
    .caption_wrapper_list .title_div p{
        float: left;    
        text-align:center;
        width:16px;
        height: 16px;
        border-radius:50%;
        background:#ED6723;
        border:2px solid #FFFFFF;
        margin-top:12px;
        margin-left: 14.1px;
    }
    .caption_wrapper_list .title_div h6{
        float: left;
        font-size:12px;
        color: #7985A3;
        width:727px;
        height: 54px;
        margin-top:12px;
        margin-left:36.9px;
    }
    .caption_wrapper_list .title_div h5{
        float: left;
        font-size: 12px;
        color:#7985A3;
        width:80.4px;
        height: 17px;
        line-height: 17px;
        margin-top:33px;
        margin-left:60px;
    }
    .caption_wrapper_list .title_div h4{
        float: left;
        font-size: 12px;
        color:#7985A3;
        width: 75px;
        height: 17px;
        line-height: 17px;
        margin-top: 33px;
        margin-left:110px;
    }
    .caption_wrapper_list .title_div h3{
        float: left;
        font-size:12px;
        color: #0FACD9;
        width: 49.8px;
        height: 16px;
        margin-top: 33px;
        margin-left:160px;
    }
    .nocaption{
        margin-top: 15px;
    }
  .nocaption  textarea{
  	text-indent: 0.5em;
    text-align: none;
    color: #596175;
    width:1420px;
    height:50px;
    display: block;
    border: none;
    font-size:12px;
    resize : none;
  }
  .nocaption img{
    width:16px;
    height: 16px;
    display:block;
    float: left;
  }
</style>
