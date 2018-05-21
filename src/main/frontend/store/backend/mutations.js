export default{
	zk_setManager(state, manager){
		if(!_.isObject(manager)){
			return false;
		}
		state.manager = manager;
	},
	zk_setCurList(state, tab){
		state.curList = tab;
	},
	zk_setComponentData(state, data){
		if(_.isObject(data)){
			state.componentData = {
				total: data.total,
				list: data.list
			}
		}else{
			console.log('not object type');
		}
	},
	zk_setActiveTabIndex(state, index){
		state.activeTabIndex = index;
	},
	zk_changeOrderDetail(state, preload){
		console.log(preload);
		state.componentData.list.splice(preload.index,1,preload.item);
	},
	zk_setCurSearch(state, search){
		state.curSearch = search;
	}
}