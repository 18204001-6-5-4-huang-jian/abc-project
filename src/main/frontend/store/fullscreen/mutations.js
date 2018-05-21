export default {
    setFullScreenWatch(state,params){
		state.isFullScreenWatch = params.isWatch;
    	if(!params.isWatch){
    		state.isEditStyle = false;
    		state.curChart = {};
			state.curWatchChartIdx = -1;
			state.showtype = 'chart';
    	}
	},
	setFullScreenWatchTable(state,params){
		state.isFullScreenWatchTable = params.isWatch;
	},
	setCurWatchChartIdx(state,params){
        state.isSwitchingChart = true;
        state.isEditStyle = false;
        state.curWatchChartIdx = params.idx;
        state.isFullScreenWatchTable = false;
	},
	setShowChartList(state,params){
		state.isShowChartList = params.isShow;
	},
	setSwitchingChart(state,params){
		if(state.isSwitchingChart === params.isSwitching) return false;
		state.isSwitchingChart = params.isSwitching;
	},
	setCurFullChart(state, params) {
		state.curFullChart = params;
	},
	ky_setFromPath(state, params){
		state.fromPath = params.path;
	},
	zk_toggleEditPanel(state, istrue){
		if(istrue){
			state.isEditStyle = true;
		}else{
			state.isEditStyle = false;
		}
	},
	zk_setCurWatchChart_copy(state, params){
		state.curWatchChart_copy = _.cloneDeep(params.chart || {});
	},
	zk_addChangeChart(state,chart){
		let index = -1;
		state.changedList.map((item,i)=>{
			if(item.id === chart.id){
				index = i;
			}
		})
		if(index !== -1){
			state.changedList.splice(index, 1, chart);
		}else{
			state.changedList.push(chart);
		}
	},
	zk_resetFullscreenState(state, chart) {
		state.changedList = [];
	},
	zb_setSearchHgtState(state,boolean){
		state.isSearchHGT = boolean;
	},
    ky_setChartOriginalData(state, params){
        state.chartOriginalData = $.extend(true, {}, params);
    },
    zb_changeShowType(state,_type){
    	state.showtype = _type;
    }
}
