<template>
	<div class="mangement-right clearfix">
		<div class="panel panel-default">
		    <div class="panel-heading clearfix">
			    <div class="pull-left panel-title" v-text="curTitle"></div>
			    <div class="dropdown pull-right">
				    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">
				    	用户名
				        <span class="caret"></span>
				    </button>
				    <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
				        <li role="presentation">
				            <a role="menuitem" tabindex="-1">用户名</a>
				        </li>
				        <li role="presentation">
				            <a role="menuitem" tabindex="-1">用户邮箱</a>
				        </li>
				        <li role="presentation" class="divider"></li>
				        <li role="presentation">
				            <a role="menuitem" tabindex="-1">退出登录</a>
				        </li>
				    </ul>
				</div>
		    </div>
		    <div class="panel-body">
		    	<div class="row">
		    		<div class="col-md-9 clearfix">
				    	<ul class="nav navbar-nav" v-show="curList==='order'">
				            <li :class='{active:activeTabIndex === 0}'><a @click="changeActiveIndex(0)">全部订单</a></li>
				            <li :class='{active:activeTabIndex === 1}'><a @click="changeActiveIndex(1)">已沟通</a></li>
				            <li :class='{active:activeTabIndex === 2}'><a @click="changeActiveIndex(2)">未沟通</a></li>
				        </ul>
				    	<ul class="nav navbar-nav" v-show="curList==='account'">
				            <li :class='{active:activeTabIndex === 0}'><a @click="changeActiveIndex(0)">全部账号</a></li>
				        </ul>
				    	<ul class="nav navbar-nav" v-show="curList==='product'">
				            <li :class='{active:activeTabIndex === 0}'><a @click="changeActiveIndex(0)">待上架产品</a></li>
				            <li :class='{active:activeTabIndex === 1}'><a @click="changeActiveIndex(1)">上架产品</a></li>
				        </ul>
		    		</div>
		    		<div class="col-md-3">
		                <div class="input-group">
		                    <input type="text" class="form-control" v-model="search">
		                    <span class="input-group-btn">
		                        <button class="btn btn-default" type="button" @click="handleSearch">查询</button>
		                    </span>
		                </div>
		    		</div>
		    	</div>
		    </div>
		</div>
		<div class="mangement-detail">
			<keep-alive>
				<component :is="curComponent" :tabData="tabData">
					<pager slot="pager" :curPage='curPage' :totalPage="parseInt(componentData.total)"  @setCurPage="setCurPage"></pager>
				</component>
			</keep-alive>
		</div>
	</div>
</template>
<script type="text/javascript">
	import orderWidget from './widget/order'
	import accountWidget from './widget/account'
	import productWidget from './widget/product'
	import pager from './widget/pager'
	export default{
		name: 'dashboard-header',
		components:{
			orderWidget,
			accountWidget,
			productWidget,
			pager
		},
		data(){
			return {
				curPage: 1,
				search: ''
			}
		},
		computed: {
			tabData(){
				return this.$store.state.backend.componentData.list;
			},
			activeTabIndex(){
				return this.$store.state.backend.activeTabIndex;
			},
			curList(){
				return this.$store.state.backend.curList;
			},
	        curComponent(){
	        	switch(this.curList){
	        		case 'product':
			        	return productWidget;
			        case 'account':
			        	return accountWidget;
		        	default:
			        	return orderWidget;
	        	}
	        },
	        curTitle(){
	        	switch(this.curList){
	        		case 'product':
	        			return '产品管理';
	        		case 'account':
	        			return '账号管理';
	        		default:
	        			return '订单管理';
	        	}
	        },
	        componentData(){
	        	return this.$store.state.backend.componentData;
	        },
	        curSearch(){
	        	return this.$store.state.backend.curSearch;
	        }
	    },
	    filters:{
			
		},
		watch:{
			curList(val){
				this.curPage = 1;
			}
		},
		methods:{
			changeActiveIndex(index){
				if(index === this.activeTabIndex)return false;

				this.curPage = 1;
				this.$store.dispatch("zk_getComponentData",{
					curList: this.curList,
					activeTabIndex: index,
					curPage: 1
				})
			},
			setCurPage(n){
				if(n === this.curPage)return false;
				
				this.curPage = n;
				this.$store.dispatch("zk_getComponentData",{
					curList: this.curList,
					activeTabIndex: this.activeTabIndex,
					curPage: n
				})
			},
			handleSearch() {
				if(!this.search.trim() && !this.curSearch.trim()){
					this.search = '';
					return false;
				}
				this.$store.commit('zk_setCurSearch',this.search);
				this.curPage = 1;
				this.$store.dispatch("zk_getComponentData",{
					curList: this.curList,
					activeTabIndex: this.activeTabIndex,
					curPage: 1
				})
			}
		},
	}
</script>
<style type="text/css" scoped lang="less">
	@import '../../../less/base.less';
	.mangement-right{
		height: 100%;
		background: #fff;
		overflow: hidden;
	}
	.panel-default{
		padding-bottom: 15px;
		border-bottom: none;
		background: #f0f0f0;
		margin-bottom: 0;
	}
	.panel-heading{
		padding: 7px 40px;
	}
	.dropdown-menu a{
		cursor: pointer;
	}
	.dropdown-menu a:hover{
		background: #e5e5e5;
	}
	.panel-title{
		line-height: 33px;
	}
	.panel-body{
		background: #fff;
		padding: 0 40px;
		border-bottom: 1px solid #ccc;
	}
	.navbar-nav a{
		padding: 10px 2px;
		margin-right: 40px;
		color: @baseColor;
		font-weight: bold;
		position: relative;
		font-size: 12px;
		cursor: pointer;
	}
	.navbar-nav li.active a:after{
		content: '';
		position: absolute;
		bottom: 0;
		left: 0;
		width: 100%;
		height: 3px;
		background: @baseColor;
	}
	.input-group{
		padding: 6px 0;
	}
	.input-group .form-control{
		height: 28px;
		padding: 0 8px;
		border-radius: 4px;
	}
	.input-group .input-group-btn .btn{
		padding: 3px 12px;
		background: @baseColor;
		color: #fff;
		border-color: @baseColor;
		border-radius: 4px;
		margin-left: 5px;
	}
	.pager{
		padding: 50px 0;
		font-size: @14font;
		position: relative;
		display: inline-block;
	}
</style>