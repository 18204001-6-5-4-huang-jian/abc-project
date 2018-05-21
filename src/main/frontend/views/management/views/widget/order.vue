<template>
	<div class="order-table-wrap">
		<table id="order-table-wrap">
			<tr class="table-header table-row">
				<th class="table-head table-cell-1">订单号</th>
				<th class="table-head table-cell-2">联系邮箱</th>
				<th class="table-head table-cell-3">买家信息</th>
				<th class="table-head table-cell-4">方案类型</th>
				<th class="table-head table-cell-5">备注信息</th>
			</tr>
			<tr v-for="(data,index) in tabData" class="table-body table-row">
				<td class="table-td table-cell-1">
					<div class="order-id" v-text="data.id"></div>
					<span class="order-date" v-text="data.orderDate"></span>
				</td>
				<td class="table-td table-cell-2">
					<div class="user-email" v-text="data.email"></div>
				</td>
				<td class="table-td table-cell-3">
					<div class="user-name" v-text="data.userInfo.name"></div>
					<div class="user-company" v-text="data.userInfo.company"></div>
					<div class="user-address" v-text="data.userInfo.address"></div>
				</td>
				<td class="table-td table-cell-4">
					<div>
						<span class="plan-status first" v-if="data.plan.status === 0">首</span>
						<span class="plan-status upgrade" v-if="data.plan.status === 1">升</span>
						<span class="plan-status second" v-if="data.plan.status === 2">续</span>
						<span class="plan-name" v-text="data.plan.name + ' ' + data.plan.level.long + ' ' + data.plan.level.long_unit"></span>
					</div>
					<div class="product-wrap">
						<span class="product" v-for="product in data.plan.products" v-text="product"></span>
					</div>
					<div class="tool-tip">
						<div v-text="data.plan.range"></div>
						<ul class="clearfix">
							<li class="tool-tip-product" v-for="item in data.plan.products" v-text="item"></li>
						</ul>
					</div>
				</td>
				<td class="table-td table-cell-5">
					<span class="edit-text" v-if="!data.remarks && editingIndex !== index" @click="editRemark(index)">备注</span>
					<span v-if="data.remarks && !remarks.trim() && editingIndex === -1" v-text="data.remarks"></span>
					<img class="edit-img" v-if="data.remarks && !remarks.trim() && editingIndex === -1" @click="editRemark(index,data)" src="/images/back/icon_edit.png" alt="编辑备注">
					<textarea v-if="editingIndex === index" v-model="remarks" v-focus class="edit-input" placeholder="请输入备注信息"></textarea>
					<button v-if="editingIndex === index" class="btn-cancel-edit" @click="cancelEdit">取消</button>
					<button v-if="editingIndex === index" class="btn-submit-edit" :class="{disable:!remarks.trim()}" @click="changeRemark($event,data,index)">提交</button>
				</td>
			</tr>
		</table>
		<slot name="pager"></slot>
	</div>
</template>
<script type="text/javascript">
	export default{
		name: 'orderWidget',
		props:{
			tabData:{
				type: Array,
				default: function(){
					return []
				}
			}
		},
		data(){
			return {
				editingIndex: -1,
				remarks: ''
			}
		},
		directives:{
			focus:{
				inserted(el){
					el.focus()
				}
			}
		},
		mounted(){
			// 滚动条
			$('.order-table-wrap').height($('html,body').height() - 106);
		},
		methods:{
			editRemark(index,data){
				this.editingIndex = index;
				if(data && data.remarks && data.remarks.trim()){
					this.remarks = data.remarks.trim();
				}
			},
			changeRemark(ev,data,index){
				let value = this.remarks.trim();
				if(!value)return false;
				this.$store.dispatch('zk_changeOrderDetail',$.extend({},data,{index: index}))
				this.cancelEdit();
			},
			cancelEdit(){
				this.editingIndex = -1;
				this.remarks = "";
			}
		}
	}
</script>
<style type="text/css" scoped lang="less">
	@import '../../../../less/base.less';
	.order-table-wrap{
		height: 100%;
		overflow: auto;
		text-align: center;
	}
	#order-table-wrap{
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
			width:15%;
		}
		.table-cell-3{
			width:30%;
		}
		.table-cell-4{
			width:20%;
			position: relative;
			&:hover{
				.tool-tip{
					opacity: 1;
					z-index: 1;
				}
			}
			.tool-tip{
				position: absolute;
				z-index: -1;
				width: 220px;
				top: 68px;
				left: -30px;
				padding: 10px;
				opacity: 0;
				transition: all .5s ease-out .5s;
				background: #fff;
				box-shadow: 0 0 2px 2px #ddd;
				border-radius: 4px;
				.clearfix{
					.tool-tip-product{
						float: left;
						margin-right: 20px;
						line-height: 1.5;
						font-weight: bold;
						color: @baseColor;
					}
				}
			}
		}
		.table-cell-5{
			position: relative;
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
		.user-name{
			font-size: @14font;
		}
		.user-company{
			color: @9color;
		}
		.user-address{
			.user-company();
		}
		.plan-name{
			font-size: @14font;
		}
		.product-wrap{
			white-space: nowrap;
			overflow: hidden;
			text-overflow:ellipsis;
			max-width: 140px;
			.product{
				color: @9color;
				margin-right: 5px;
			}
		}
		.plan-status{
			display: inline-block;
			width: 18px;
			height: 18px;
			text-align: center;
			line-height: 17px;
			border-radius: 3px;
			color: #fff;
		}
		.first{
			.plan-status();
			background: #86a0ac;
			border: 1px solid @baseColor;
		}
		.second{
			.plan-status();
			background: #f36c3b;
			border: 1px solid #f9845b;
		}
		.upgrade{
			.plan-status();
			background: #f29830;
			border: 1px solid #f5b264;
		}
		.edit-text{
			color: @baseColor;
			cursor: pointer;
		}
		.edit-img{
			cursor: pointer;
			margin-left: 10px;
		}
		.edit-input{
			position: absolute;
			width: 100%;
			bottom: 3px;
			top: 3px;
			resize:none;
			padding: 3px;
			border: 1px solid @border;
		}
		::placeholder{
			color: @disable
		}
		.btn-cancel-edit{
			position: absolute;
			bottom: 4px;
			right: 38px;
			width: 38px;
			height: 18px;
			border: 1px solid @lightBorder;
		}
		.btn-submit-edit{
			.btn-cancel-edit();
			right: 0px;
			&.disable{
				color: @disable;
			}
		}

	}
</style>