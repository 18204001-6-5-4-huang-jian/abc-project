export default {
	loadDashBoard(context, dashBoard){
		// 设置看板id
		context.commit('setCurDashBoard', dashBoard);
		let url = U.getApiPre() + '/api/v1/user-products/' + context.state.curDashBoard.id + '?lang=' + (context.state.lang === 'en' ? ' ': context.state.lang);
		ChartHelper._initHC(null,context.state.lang === 'zh_CN'?'cn':'en');
		if(dashBoard.id === "demo"){
			if(context.state.lang === 'zh_CN'){
				url = '/json/demo-cn.json';
			}else{
				url = '/json/demo.json';
			}
		}
		$.get({
			url: url,
			success: function(resp){
				if(resp.success){
					if(resp.data.product_id){
						//console.log(resp.data.product_id);
						context.commit('hj_changePId',resp.data);
						context.commit('hj_saveArray',resp.data.board.charts)
					}
					//保证每个chart都存在position
					var sortNodes = [],
						charts = resp.data.board.charts,
						preNode = null;
					for (var i = 0; i < charts.length; i++) {
						if(!charts[i].position || typeof charts[i].position == 'undefined'){
							if(preNode == null){
								charts[i].position = {
									x: 0,
									y: 0,
									w: 6,
									h: 4,
									width: 0,
									height: 0,
									i: i.toString(),
									id: charts[i].id
								}
							}else{
								charts[i].position = {
									x: parseInt(preNode.position.x) + parseInt(preNode.position.w) > 6 ? 0 : parseInt(preNode.position.x) + parseInt(preNode.position.w),
									y: parseInt(preNode.position.x) + parseInt(preNode.position.w) > 6 ? parseInt(preNode.position.y) + parseInt(preNode.position.h) : parseInt(preNode.position.y),
									w: 6,
									h: 4,
									width: 0,
									height: 0,
									i: i.toString(),
									id: charts[i].id
								}
							}
						}else{
							charts[i].position = {
								x: parseInt(charts[i].position.x),
								y: parseInt(charts[i].position.y),
								w: parseInt(charts[i].position.w),
								h: parseInt(charts[i].position.h),
								width: 0,
								height: 0,
								i: i.toString(),
								id: charts[i].id
							}
						}
						//更新时间初始化0
						charts[i].updateTime = 0;
						charts[i].dataTime = 0;
						//图表数据状态：0没有数据，１正在请求数据，２数据请求完成并有数据，３数据格式错误
						charts[i].status = 0;
						charts[i].isResizing= false;
						charts[i].isMoving = false;
						//数据保存
						charts[i].dataTable = null;
						sortNodes.push(charts[i].position);
						preNode = charts[i];
					}
					//排序
					sortNodes.sort(function(pre, next){
						return pre.y - next.y == 0 ? pre.x - next.x : pre.y - next.y;
					});
					var sortCharts = [];
					for (var i = 0; i < sortNodes.length; i++) {
						let index = parseInt(sortNodes[i].i);
						sortCharts.splice(i, 1, charts[index]);
					}
					context.commit('setCurDashBoard', {
						id: resp.data.id,
						title: resp.data.title,
						creator_id: resp.data.creator_id,
						creator_name: resp.data.creator_name,
						status: 'success',
						share: resp.data.share,
						watched: resp.data.watched,
						charts: charts,
						chartPositions: sortNodes
					});
					context.commit('zk_setProInfo', {
						remaining:resp.data.remaining,
						plan_name:resp.data.plan_name,
						product_id: resp.data.product_id
					});
				}else{
					if(resp.status == 1){
						window.location.href = '/#/my';
					}else if(resp.status == 2){
						context.commit('setCurDashBoard', $.extend(true, {}, context.state.curDashBoard, {
								status:'failed',
								creator_name: resp.data.creator_name,
								creator_email: resp.data.creator_email
							}));
						//更新我关注的看板列表中数据状态
						for (var i = 0; i < context.state.concernDashBoardList.length; i++) {
							if (context.state.concernDashBoardList[i].id == context.state.curDashBoard.id) {
								context.commit('updateConcernDashBoard', {
									index: i,
									dashBoard: $.extend(true, {}, context.state.concernDashBoardList[i], {status: 'failed'})
								});
								break;
							}
						}
					}else {
						context.commit('setCurDashBoard', $.extend(true, {}, context.state.curDashBoard,
							{status:'failed'}));
					}
				}
			},
			error: function(resp){
				if(resp.status == 401){
					window.location.href = '/#/login';
				}
			}
		});
	}
}
