export default {
	isOwnDashBoard(state){
		return state.curDashBoard.creator_id == sessionStorage.uid;
	}
}