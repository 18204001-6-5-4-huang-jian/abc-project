<template lang="html">
	<div class="dashboard-detail-wrapper">
		<!-- dashboard content -->
		<dashboard-content
			v-bind:dashBoard="curDashBoard"
			v-bind:status="curDashBoard.status"
			v-bind:charts="curDashBoard.charts"
			v-bind:chartPositions="chartPositions"
			v-bind:chartCount="chartCount"
			>
		</dashboard-content>
	</div>
</template>

<script>
import DashboardContent from './dashboard-content.vue'
export default {
	name:'dashboard-detail',
	components: {
		DashboardContent
	},
	mounted(){
		let bid = this.$route.params.bid
		if ( bid ) {
			this.$store.commit('setDashBoardId',{ bid: bid });
			if(bid !== 'demo'){
				U.init((bol)=>{
					if(!bol){
						this.$router.push({
							name: 'login'
						});
					}
				})
				this.$store.dispatch('loadDashBoard', {
					id: this.dashBoardId,
					title: this.curDashBoard.title,
					creator_id: this.curDashBoard.creator_id,
					creator_name: this.curDashBoard.creator_name,
					status: 'isLoading',
					share: this.curDashBoard.share,
					watched: this.curDashBoard.watched,
					charts: [],
					chartPositions: [],
					lang: ''
				});
			}else{
				this.$store.dispatch('loadDashBoard', {
					id: 'demo',
					title: this.curDashBoard.title,
					creator_id: this.curDashBoard.creator_id,
					creator_name: this.curDashBoard.creator_name,
					status: 'isLoading',
					share: this.curDashBoard.share,
					watched: this.curDashBoard.watched,
					charts: [],
					chartPositions: [],
					lang: ''
				});
			}
		}else {
			this.$router.push({
				name: 'my'
			});
		}
	},
	watch:{
		lang(val){
            this.$store.dispatch('loadDashBoard',{
				id: this.dashBoardId,
				title: this.curDashBoard.title,
				creator_id: this.curDashBoard.creator_id,
				creator_name: this.curDashBoard.creator_name,
				status: 'isLoading',
				share: this.curDashBoard.share,
				watched: this.curDashBoard.watched,
				charts: [],
				chartPositions: [],
				lang: val
            })
        }
	},
	computed: {
        lang(){
            return this.$store.state.dashboard.lang;
        },
        curDay() {
        	return this.$store.state.dashboard.curDay;
        },
		curDashBoard(){
			return this.$store.state.dashboard.curDashBoard;
		},
		chartPositions(){
			return this.curDashBoard.chartPositions;
		},
		chartCount(){
			return {
				id: this.curDashBoard.id,
				count: this.curDashBoard.charts.length
			}
		},
		dashBoardId(){
			return this.$store.state.dashboard.dashBoardId;
		}
	}
}
</script>
<style lang="css">
	.dashboard-detail-wrapper{
		position: absolute;
		top: 50px;
		left: 0px;
		bottom: 0px;
		width: 100%;
	}
</style>
