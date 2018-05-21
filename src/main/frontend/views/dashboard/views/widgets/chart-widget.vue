<template>
    <div class="chart-widget-wrapper" :id="id"></div>
</template>

<script>
export default {
    name:'chart-widget',
    props: {
        id: {
            type: String,
            default: ''
        }
    },
    methods:{
        render(element, index, chartWrapper) {
            // 渲染图表
            let self = this;
            $('#' + self.id).empty();
            try{
                let highChartConfig = ChartHelper.getLiveChartConfigV2(chartWrapper, {
                    onload: function(){
                        self.$emit('onSuccess', {
                            index: index,
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
                    $('#' + self.id).highcharts('Map', highChartConfig);
                }else if(highChartConfig._type == 'StockChart'){
                    $('#' + self.id).highcharts('StockChart', highChartConfig);
                }else{
                    $('#' + self.id).highcharts(highChartConfig);
                }
            }catch(e){
                self.$emit('onError', {
                    index: index,
                    error: chartWrapper
                });
            }
        }
    }
}
</script>
<style lang="css">

  </style>
