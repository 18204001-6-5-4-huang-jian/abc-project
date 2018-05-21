export default {
	dashBoardId: '',
    productId:'',
	dashBoardDefault: {
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
        chartCount: 0,
	},
	curDashBoard: {
		id: '',
		title: '',
		creator_id: '',
		creator_name: '',
		status: 'isLoading',//isLoading，success, failed
		share: 0,//分享状态
		watched: false,//被关注状态
		update_count: 0,//图表更新数量
		charts: [],
		chartPositions: []//每一个图表的位置信息
	},
	dashBoardGroup: 0,
	OWN_DB_LIST: 0,
	CONCERN_DB_LIST: 1,

    //全屏图表位置记录
    scrollTop: -999,
    /*word-export*/
    isMakeWordOut:false,
    exportAllNums:0,
    exportNowNums:0,
    isWordOutScaning:false,
    /*剩余时间 plan*/
    remaining:0,
    plan_name:'---',
    // 看板当前语种
    lang: 'zh_CN',
    // 看板支持语种
    availableLang: [],
    // 当前时间轴日期
    curDay: '',
    charts: [],
    hj:0,
    h_j:0,
    titleArray:[]
}
