export default{
	zk_setUserId(state, id){
		state.userId = id;
	},
	zk_setUserAgent(state, bol){
		state.isMobile = bol;
	}
}