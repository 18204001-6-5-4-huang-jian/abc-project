<template>
	<div class="account-table-wrap">
		<table id="account-table-wrap">
			<tr class="table-header table-row">
				<th class="table-head table-cell-1">用户信息</th>
				<th class="table-head table-cell-2">邮箱</th>
				<th class="table-head table-cell-3">激活中公司</th>
			</tr>
			<tr v-for="(data,index) in tabData" class="table-body table-row">
				<td class="table-td table-cell-1">
					<div class="account-name" v-text="data.name"></div>
				</td>
				<td class="table-td table-cell-2">
					<div class="user-email" v-text="data.email"></div>
				</td>
				<td class="table-td table-cell-3">
					<div class="product-num" v-text="data.products && data.products.length"></div>
					<ul class="product-list">
						<li class="product-item" v-for="(list, index) in data.products">
							<div v-text="list.name"></div>
							<span v-text="list.due"></span>
						</li>
					</ul>
				</td>
			</tr>
		</table>
		<slot name="pager"></slot>
	</div>
</template>
<script type="text/javascript">
	export default{
		name: 'accountWidget',
		props:{
			tabData:{
				type: Array,
				default: function(){
					return []
				}
			}
		}
	}
</script>
<style type="text/css" lang="less">
	@import '../../../../less/base.less';
	.account-table-wrap{
		height: 100%;
		overflow: auto;
		text-align: center;
	}
	#account-table-wrap{
		width: 100%;
		color: @6color;
		text-align: left;
		.table-row{
			border-bottom: 1px solid #e5e5e5;
			font-size: @12font;
			line-height: 1.5;
		}
		.table-head{
			padding: 10px 0;
			color: @3color;
		}
		.table-cell{
			padding: 15px 40px;
			padding-right: 0;
		}
		.table-cell-1{
			.table-cell();
			width:15%;
		}
		.table-cell-2{
			width:20%;
		}
		.table-cell-3{
			width:65%;
		}
		.order-id{
			font-size: @14font;
			margin-bottom: 5px;
		}
		.order-date{
			color: @9color;
		}
		.user-email{
			font-size: @14font;
		}
		.product-num{
			display: table-cell;
			font-size: 30px;
			color: @baseColor;
			padding-right: 50px;
		}
		.product-list{
			display: table-cell;
			vertical-align: middle;
			.product-item{
				display: table-cell;
				padding-right: 50px;
			}
		}
	}
</style>