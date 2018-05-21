<template>
	<div class="pager" :style="pagerStyle">
		<ul class="pager-inner">
			<li class="pager-btn" :class="{disable:!preDiff}" @click="showPre()">&lt;</li>
			<li class="pager-btn" v-if="preDiff>2" @click="setCurPage(1)">1</li>
			<li class="ellipsis" v-if="preDiff>2">...</li>
			<li v-for="n in totalPage" class="pager-btn" v-if="Math.abs(n-curPage)<3" :class="{active:n === curPage}" v-text="n" @click="setCurPage(n)"></li>
			<li class="ellipsis" v-if="nextDiff>2">...</li>
			<li class="pager-btn" v-if="nextDiff>2" @click="setCurPage(totalPage)" v-text="totalPage"></li>
			<li class="pager-btn" :class="{disable:!nextDiff}" @click="showNext()">&gt;</li>
		</ul>
	</div>
</template>
<script type="text/javascript">
	export default{
		name: 'pager',
		props:{
			curPage:{
				type: Number,
				default: 1
			},
			totalPage:{
				type: Number,
				default: 10
			},
			pagerStyle:{
				type: [String,Object]
			}
		},
		computed:{
			preDiff(){
				return Math.abs(this.curPage - 1);
			},
			nextDiff(){
				return Math.abs(this.totalPage - this.curPage);
			}
		},
		methods:{
			setCurPage(n){
				this.$emit('setCurPage',n);
			},
			showPre(){
				if(this.curPage - 1 <= 0){
					return false;
				}
				this.$emit('setCurPage',this.curPage - 1);
			},
			showNext(){
				if(this.curPage + 1 > this.totalPage){
					return false;
				}
				this.$emit('setCurPage',this.curPage + 1);
			}
		}

	}
</script>
<style type="text/css" scoped lang="less">
	@base: #355867;
	@border: #ccc;
	@fontColor: #333;
	.pager{
		margin: 0 auto;
		.pager-inner{
			list-style: none;
			overflow: hidden;
			.pager-btn{
				float: left;
				width: 2em;
				height: 2em;
				border: 1px solid @border;
				color: @fontColor;
				text-align: center;
				line-height: 2em;
				border-radius: .3em;
				margin-right: .5em;
				cursor: pointer;
				user-select:none;
			}
			.pager-btn.active{
				background: @base;
				color: #fff;
			}
			.pager-btn.disable{
				background: #e5e5e5;
				cursor: not-allowed;
			}
			.ellipsis{
				user-select:none;
				margin-right: .5em;
				float: left;
				width: 2em;
				height: 2em;
			}
		}
	}
</style>