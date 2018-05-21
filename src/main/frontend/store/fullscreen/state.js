export default {
    curWatchChartIdx:0,
	isShowChartList:false,
	isSwitchingChart:false,
	isFullScreenWatch:false,
	isFullScreenWatchTable:false,
	// 当前全屏图表数据信息
	curFullChart: {},
	// 图表信息副本
	curWatchChart_copy: {},
	isEditStyle: false,
	// 修改过的图表列表
	changedList: [],
	// 查询沪港通
	isSearchHGT:false,
	//全屏发起位置
	fromPath: '',
    //保存图表数据
    chartOriginalData: {},
    showtype:"chart",
}
