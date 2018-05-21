<template>
    <div class="table-widget-wrapper" :id="id"></div>
</template>

<script>
export default {
    name:'table-widget',
    props: {
    	id: {
    		type: String,
    		default: ''
    	}
    },
    methods: {
    	render(element, index, chartWrapper) {
    		// 渲染表格s
            let self = this;
            $('#' + self.id).empty();
    		let handsontableConfig = ChartHelper.getTableConfig(chartWrapper, {
    			//减去padding
                width: element.width(),
                height: element.height() - 10,
                fixedRowsTop: 0,
                disableVisualSelection: true
           });
        	try{
            	new Handsontable(document.getElementById(self.id), handsontableConfig);
            	self.$emit('onSuccess', {
            		index: index,
            		chartWrapper: chartWrapper
            	});
                $('.table-widget-wrapper .handsontable td').unbind().bind('mousedown', function(event) {
                    event.stopPropagation();
                });
                //($('.table-widget-wrapper .handsontable td').innerHTML).toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,');
        	    } catch(e){ 
        		self.$emit('onError', {
            		index: index,
            		error: chartWrapper
            	});
        	}
    	}
   },
}
</script>

<style lang="css">
    .table-widget-wrapper {
        width: 100%;
        height: 100%;
    }
    .table-widget-wrapper .ht_master .wtHolder {
        overflow: hidden !important;
    }
    .table-widget-wrapper td{
		background: inherit;
        text-align: left;
        vertical-align: middle;
	}
    .table-widget-wrapper tbody > tr:nth-child(2n) > td {background: #1b1f29;}
    .table-widget-wrapper tbody > tr:nth-child(2n+1) > td {background: #20242e;}
    .table-widget-wrapper tbody > tr:hover{background: transparent;}
    .table-widget-wrapper .handsontable th{
        background-color: #f8fbfd;
    }
    .table-widget-wrapper .handsontable th,
    .table-widget-wrapper .handsontable td{
        line-height: 18px;
        padding: 6px 0;
        text-align: center;
        color: #7985a3!important;
        border:0!important;
        box-sizing: border-box;
    }
    .table-widget-wrapper .ht_clone_top tbody > tr > td{
        background-color: #e1e5e8!important;
        font-weight: bold;
    }
    .table-widget-wrapper tbody > tr > td:nth-child(1){
        padding-left: 15px;
    }
    .table-widget-wrapper .ht_master tbody > tr > td{
        font-size: 12px;
    }
 /*新增表头固定代码*/
.dashboard-wrapper .research-widgets .bootstrap-table .table thead tr{
	    display:table;
		width:100%;
		table-layout:fixed;
		position: relative;
}
.dashboard-wrapper .research-widgets .bootstrap-table .table tbody{
		display: block;
		overflow-y:auto;
		min-height: 120px;
		max-height: 380px;
}
.dashboard-wrapper .research-widgets .bootstrap-table .table tbody tr{
		display:table;
		width:100%;
		table-layout:fixed;
		text-indent:3px;
}
</style>