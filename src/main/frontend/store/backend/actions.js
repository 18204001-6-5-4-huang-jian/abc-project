export default{
	zk_getComponentData({state,commit,rootState},preload){
		/*
			先请求数据，再切换tab列表
		 */
		let _index = layer.load(2);
		console.log(preload);
		// let url = '/api/v1/';
		let url;
		if(preload && preload.curList === 'order'){
		// 订单假数据
			url = '/json/order.json';
		}else{
		// 账号假数据
			url = '/json/account.json';
		}
		// switch(state.activeTabIndex){
		//  case 1:
		//      url += state.curList + '/' +state.activeTabIndex;
		//      break;
		//  case 2:
		//      url += state.curList + '/' +state.activeTabIndex;
		//      break;
		//  default:
		//      url += state.curList + '/' +state.activeTabIndex;
		//      break;
		// }
		$.ajax({
			type: 'get',
			url: url,
			data:{
				page: preload && preload.curPage,
				search: state.curSearch
			},
			timeout: 5000,
			success(resp){
				if(typeof resp === 'string'){
					resp = JSON.parse(resp);
				}
				if(resp.success){
					if(preload && preload.curList){
						commit('zk_setCurList',preload.curList);
					}
					commit('zk_setComponentData',resp.data);
					if(preload && typeof preload.activeTabIndex !== 'undefined'){
						commit('zk_setActiveTabIndex',preload.activeTabIndex);
					}
				}
			},
			error(resp){
				console.error(resp)
			},
			complete(){
				layer.close(_index);
			}
		})
	},
	zk_changeOrderDetail({state,commit,rootState},preload){
		console.log(preload,"修改备注");
		$.ajax({
			type: 'post',
			url: '/api/v1/',
			data:preload.item,
			timeout: 5000,
			success(resp){
				if(resp.success){
					commit('zk_changeOrderDetail',preload);
				}else{
					console.log(resp.message || '修改失败');
				}
			},
			complete(){
				console.log('done');
			}
		})
	}
}