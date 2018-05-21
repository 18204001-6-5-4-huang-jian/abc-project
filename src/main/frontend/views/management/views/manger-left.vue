<template>
	<div class="mangement-left clearfix">
		<h1 class="page-header">
			<img src="/images/logo-70.png" class="img-rounded logo">
			后台管理平台
		</h1>
		<ul class="list-group">
		    <li class="list-group-item" :class="{active:curList === 'order'}" @click="setCurList('order')">
				<img src="/images/back/icon_order.png" class="img-rounded">
				订单管理
		    </li>
		    <li class="list-group-item" :class="{active:curList === 'account'}" @click="setCurList('account')">
				<img src="/images/back/icon_account.png" class="img-rounded">
		    	账号管理
		    </li>
		    <li class="list-group-item" :class="{active:curList === 'product'}" @click="setCurList('product')">
				<img src="/images/back/icon_product.png" class="img-rounded">
		    	产品管理
		    </li>
		</ul>
	</div>
</template>
<script type="text/javascript">
	export default{
		name: 'dashboard-header',
		computed: {
	        curList(){
	        	return this.$store.state.backend.curList
	        }
	    },
		methods:{
			setCurList(target){
				if(target === this.curList)return false;

				this.$store.dispatch('zk_getComponentData', {
					curList: target,
					activeTabIndex: 0
				});
			}
		}
	}
</script>
<style type="text/css" scoped>
	.mangement-left{
		/*width: 220px;*/
		height: 100%;
        background: #0e2733;
        color: #fff;
	}
	.page-header{
		background: #0a1e27;
		padding: 5px 0;
		text-align: center;
		font-size: 25px;
		margin:0 0 10px;
		border: none;
	}
	.logo{
		width: 40px;
		height: 40px;
		vertical-align: middle;
		margin-right: 5px;
	}
	.list-group-item{
		background: transparent;
		border: none;
		padding: 10px 25px;
		cursor: pointer;
	}
	.list-group-item .img-rounded{
		margin-right: 5px;
	}
	.list-group-item.active{
		background: #0a1e27;
	}
</style>