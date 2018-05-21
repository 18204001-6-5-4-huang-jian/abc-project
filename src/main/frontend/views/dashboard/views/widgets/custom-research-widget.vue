<template>
    <div class="custom-research-wrapper" :id="id"></div>
</template>

<script>
export default {
    name:'custom-research-widget',
    props: {
    	id: {
    		type: String,
    		default: ''
    	},
        width: {
            type: Number,
            default: 0
        },
        height: {
            type: Number,
            default: 0
        },
    },
    computed:{
        country(){
            return this.$store.state.dashboard.lang?'cn':'en'
        }
    },
    methods:{
    	render(element, index, chartWrapper){
    		// 渲染表格
            let self = this;
    		$('#' + self.id).css({
    		    "width": element.width() + 'px',
    		    "height": element.height() + 'px'
    		}).empty();
        	try {
	            if(chartWrapper.type == 'research_stock_card'){
                    ChartHelper.drawResearchStockCard(self.id, {
                        params: Object.assign(chartWrapper.dataTable.data.params,{country:this.country}),
                        rows: chartWrapper.dataTable.data.rows,
                    }, false);
                    self.$emit('onSuccess', {
                        index: index,
                        error: chartWrapper
                    });
                }else if(chartWrapper.type == 'research_data_list'){
                    ChartHelper.drawResearchDataList(self.id, {
                        params: chartWrapper.dataTable.data.params,
                        rows: chartWrapper.dataTable.data.rows
                    }, false);
                    self.$emit('onSuccess', {
                        index: index,
                        error: chartWrapper
                    });
                } else {
                    self.$emit('onError', {
                        index: index,
                        error: chartWrapper
                    });
                }
	        } catch(e) {
        		self.$emit('onError', {
            		index: index,
            		error: chartWrapper
            	});
        	}
    	}
    },
    watch: {
        width: function(val, oldVal) {
        },
        height: function(val, oldVal) {
        }
    }
}
</script>

<style lang="css">
    .custom-research-wrapper{
        width: 100%;
        height: 100%;
    }
    
</style>
