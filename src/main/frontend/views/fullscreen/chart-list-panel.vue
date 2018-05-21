<template lang="html">
    <div class="cc-chart-list-panel-wrapper" v-show="isShowChartList">
        <ul class="cc-chart-list-container">
            <li class="cc-chart-item-wrapper" v-for="(chart,index) in charts">
                <div class="cc-chart-item-content" :class="{'active':(index==curWatchChartIdx)}" @click="switchChart(index)" v-if="(chart.type != 'excel_image')">
                    <div class="icon" :class="backgroundImage(chart)"></div>
                    <div class="cc-chart-item-title">
                        <span v-text="chart.title"></span>
                    </div>
                </div>
            </li>
        </ul>
    </div>
</template>

<script>
export default {
    name:'chart-list-panel',
    computed:{          
        isFullScreenWatch(){
            return this.$store.state.fullscreen.isFullScreenWatch;
        },
        isShowChartList(){
            return this.$store.state.fullscreen.isShowChartList;
        },
        curWatchChartIdx(){
            return this.$store.state.fullscreen.curWatchChartIdx;
        },
        dashBoard(){
            return this.$store.state.dashboard.curDashBoard;
        },
        charts(){
            if(this.dashBoard && this.isFullScreenWatch){
                return this.dashBoard.charts || [];
            }
            return [];
        }
    },
    watch:{
        curWatchChartIdx(val){
            this.$store.commit('zk_setCurWatchChart_copy',{
                from:'changeIndex',
                chart: this.charts[val]
            });
        }
    },
    methods:{
        switchChart(index){
            this.$store.commit('setCurWatchChartIdx',{idx:index});
        },
        backgroundImage(chart){
            if(chart.chartType){
                switch (chart.chartType) {
                    case 'table':
                        return 'table';
                    case 'line':
                        return 'line';
                    case 'scatter':
                        return 'scatter';
                    case 'area':
                        return 'area';
                    case 'bar':
                        return 'bar';
                    case 'columns':
                        return 'columns';
                    case 'column':
                        return 'column';
                    default:
                        return 'line';
                }
            }else{
                return 'line';
            }
        }
    }
}
</script>
<style lang="css">
    .cc-chart-list-panel-wrapper {
        position: absolute;
		top: 51px;
		left: 0px;
        bottom: 0px;
		z-index: 3000;
		width: 220px;
    }
    .cc-chart-list-container {
        padding: 20px 0px 0px 0px;
		margin: 0px;
		width: 100%;
        height: 100%;
		overflow-x: hidden;
		overflow-y: auto;
		list-style: none;
    }
    .cc-chart-item-wrapper {
        padding: 0px;
        margin: 0px;
    }
    .cc-chart-item-content {
        display: -webkit-flex;
        display: flex;
        justify-content: flex-start;
        align-items: center;
        padding: 0px 20px;
        margin: 0px;
        width: 100%;
        height: 60px;
        cursor: pointer;
    }
    .cc-chart-item-content .icon {
        flex-basis: 40px;
        padding: 0px;
        margin: 0px 10px 0px 0px;
        width: 40px;
        height: 40px;
        background-repeat: no-repeat;
        background-size: inherit;
    }
    .cc-chart-item-content .cc-chart-item-title {
        width: 130px;
        max-height: 34px;
        text-align: left;
        font-size: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        word-wrap: break-word;
        word-break: break-all;
    }
</style>
