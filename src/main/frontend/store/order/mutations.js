export default{
	zk_changePlan(state,plan){
		state.curPlan = plan;
	},
	zk_setPlanList(state, list){
		state.planList = list.reverse();
	},
	zk_setProductList(state, list){
		state.productList = list;
	},
	zk_setCurLevel(state, level){
		state.curLevel = level;
	},
	zk_setInitPlanIndex(state, num){
		state.initPlanIndex = num;
	}
}