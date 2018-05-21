export default {

	setCurDashBoard(state, dashBoard){
		state.curDashBoard = $.extend(true, {}, state.dashBoardDefault, dashBoard);
	},
	setCurDashBoardShare(state, params){
		state.curDashBoard.share = params.share;
	},
	setOwnDashBoardList(state, dashBoards){
		state.ownDashBoardList = dashBoards;
	},
	setConcernDashBoardList(state, dashBoards){
		state.concernDashBoardList = dashBoards;
	},
	setDashBoardGroup(state, params){
		state.dashBoardGroup = params.group;
	},
	saveDashBoardPosition(state, chartPositions){
		var self = this;
		var params = {
			title: state.curDashBoard.title,
			charts: []
		};
		var chartPositionsClone = $.extend(true, [], chartPositions);
        chartPositionsClone.sort(function(pre, next){
            return pre.y - next.y == 0 ? pre.x - next.x : pre.y - next.y;
        });
		for (var i = 0; i < chartPositionsClone.length; i++) {
			let index = parseInt(chartPositionsClone[i].i);
			params.charts.push({
				id: parseInt(state.curDashBoard.charts[index].id),
				title: state.curDashBoard.charts[index].title,
				position: chartPositionsClone[i]
			});
		}
		$.post({
			url:U.getApiPre() + '/api/v1/dash-board/save?id='+state.curDashBoard.id,
			dataType: 'json',
			contentType: 'application/json',
			data: JSON.stringify(params),
			success:function(resp){
				if(resp.success){
					console.log('mutations','saveDashBoardPosition','save success');
				}else{
					console.error('mutations','saveDashBoardPosition',resp.message||'network error');
				}
			},
			error:function(resp){
				if(resp.status == 401){
					location.href = '/views/report/login.html';
				}else {
					console.error('mutations','saveDashBoardPosition',resp.responstText||'network error');
				}
			}
		});
	},
	addDashBoard(state, params){
        for (var i = 0; i < state.ownDashBoardList.length; i++) {
            if(params.id == state.ownDashBoardList[i].id){
                return;
            }
        }
		state.ownDashBoardList.unshift($.extend(true, {}, state.dashBoardDefault, params));
	},
	deleteDashBoard(state, params){
		state.ownDashBoardList.splice(params.index, 1);
	},
	watchDashBoard(state, params){
		if (params.index == -1) {
			state.curDashBoard.watched = !state.curDashBoard.watched;
			for (var i = 0; i < state.concernDashBoardList.length; i++) {
				if (state.curDashBoard.id == state.concernDashBoardList[i].id) {
					let dashBoard = state.concernDashBoardList[i];
					dashBoard.watched = state.curDashBoard.watched;
					state.concernDashBoardList.splice(i, 1, dashBoard);
				}
			}
		}else {
			let dashBoard = state.concernDashBoardList[params.index];
			dashBoard.watched = !dashBoard.watched;
			state.concernDashBoardList.splice(params.index, 1, dashBoard);
			if (dashBoard.id == state.curDashBoard.id) {
				state.curDashBoard.watched = dashBoard.watched;
			}
		}
	},
	updateOwnDashBoard(state, params){
		state.ownDashBoardList.splice(params.index, 1, $.extend(true, {}, params.dashBoard));
	},
	updateConcernDashBoard(state, params){
		state.concernDashBoardList.splice(params.index, 1, $.extend(true, {}, params.dashBoard));
	},
	pushChartToDashBoard(state, chartWrapper){
		state.curDashBoard.charts.push({
			id: chartWrapper.id,
			title: chartWrapper.title,
			type: chartWrapper.type,
			chartType: chartWrapper.chartType,
			options: chartWrapper.options,
			position: chartWrapper.position,
			dataSourceUrl: chartWrapper.dataSourceUrl,
			image_url: chartWrapper.image_url,
			updateTime: 0,
			dataTime: 0,
			status: 0,
			dataTable: null
		});
		state.curDashBoard.chartPositions.push(chartWrapper.position);
		state.curDashBoard.chartCount++;
	},
	startSearchingChart(state){
		state.isSearchingChart = true;
		state.myCharts.charts = [];
		state.myCharts.page = 0;
		state.myCharts.hasMore = false;

        state.resouceCharts.charts = [];
        state.resouceCharts.page = 0;
        state.resouceCharts.hasMore = false;

		state.dashBoardCharts.charts = [];
		state.dashBoardCharts.page = 0;
		state.dashBoardCharts.hasMore = false;

		state.otherCharts.charts = [];
		state.otherCharts.page = 0;
		state.otherCharts.hasMore = false;
	},
	setChartSearchText(state, text){
		state.chartSearchText = text;
	},
	stopSearchingChart(state){
		state.isSearchingChart = false;

		state.myCharts.charts = [];
		state.myCharts.page = 0;

        state.resouceCharts.charts = [];
        state.resouceCharts.page = 0;

		state.dashBoardCharts.charts = [];
		state.dashBoardCharts.page = 0;

		state.otherCharts.charts = [];
		state.otherCharts.page = 0;
	},
	setMyCharts(state, params){
		state.myCharts = $.extend(true, {}, params);
	},
	setResouceCharts(state, params){
		state.resouceCharts = $.extend(true, {}, params);
	},
	setDashBoardCharts(state, params){
		state.dashBoardCharts = $.extend(true, {}, params);
	},
	setOtherCharts(state, params){
		state.otherCharts = $.extend(true, {}, params);
	},
	setDashBoardScrollTop(state, params){
    	state.scrollTop = params.scrollTop;
    },
    setDashBoardId(state, params){
		state.dashBoardId = params.bid;
	},
    clearDashBoardState(state){
        console.debug('dashboard mutations clearDashBoardState');
        state.ownDashBoardList = [{
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
		}];
		state.concernDashBoardList = [];
		state.dashBoardGroup = 0;
        state.curDashBoard = $.extend(true, {}, state.dashBoardDefault);
        state.dashBoardId = '';
    },
    /*设置到期时间 plan*/
    zk_setProInfo(state,obj){
    	state.remaining = obj.remaining;
    	state.plan_name = obj.plan_name;
    	state.product_id = obj.product_id;
    },
    //设置看板ID
    hj_changePId(state,obj){
    	state.productId = obj.product_id;
    },
    //改变语言类型
	zk_setLanguage(state, lang){
		state.lang = lang;
	},
	zk_setCurDay(state, curDay){
		if(state.curDay === curDay) return false;
		state.curDay = curDay;
	},
	zk_setLangArr(state, arr){
		if(arr instanceof Array){
			state.lang = arr[0];
		}
		state.availableLang = arr;
	},
	zk_updateCharts(state, charts){
		state.curDashBoard.charts = charts;
	},
	hj_setcategory(state,hj){
		state.hj = hj;
	},
	hj_setcategory_1(state,h_j){
		state.h_j = h_j;
	},
	hj_saveArray(state,array){
        state.titleArray = array;
	}
}
