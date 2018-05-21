<template>
    <transition name="component-fade">
        <div class="order-wrap">
            <div class="order-head">
               <div class="order-nav-bar clearfix">
                    <div class="logo abcdata-icon pointer" @click="direct('introduction')"></div>
                    <user-bar></user-bar>
                </div>
                <div class="order-title">
                	<h3>Order Information</h3>
                </div>
            </div>
            <div class="payment-item">
            	<p class="p">您需要支付<span class="span">{{'￥'+(order_information.price_total) | currency}}</span> , 请使用网上银行或至银行柜台，汇款至阿博茨科技银行账户 :</p>
            	<div class="payment-information">
            		<p><span class="span_">收款账户 :</span> {{order_information.company}}</p>
            		<p><span class="span_">收款卡号 :</span> {{order_information.card}}</p>
            		<p><span class="span_">开户银行 :</span> {{order_information.bank}}</p>
            	</div>
            	<button @click="finish()" class="finish-remittance">我已经完成汇款</button>
            </div>
        </div>
    </transition>
</template>
<script>
    import UserBar from '../common/userbar.vue'
    export default {
        components: {
            UserBar
        },
        data() {
            return {
            	order_id:'',
            	order_information:{}
            }
        },
        mounted() {
             GaHelper.sendPageView();
        	//移动端自动回到顶部
        	$("html,body").animate({scrollTop:0},100);
        	var self = this;
        	self.order_id = localStorage.getItem("order_id",);
        	$.get({
        		url:'api/v1/orders/payment/'+self.order_id,
        		contentType: 'application/json;charset=utf-8',
        		success:(res)=>{
        			self.order_information = res.data;
        		},
        		complete:(resp)=>{
        			
				}
        	})
        },
        filters:{
        	currency(value){
			if(typeof value !== "string" || value.length < 4){
				return value
			}else if(value.length > 4 && value.length <= 7){
				return value.slice(0, -3) + ',' + value.slice(-3)
			}else if(value.length > 7){
				return value.slice(0, -6) + ',' + value.slice(-6, -3) + ',' + value.slice(-3);
			}
		},
        },
        computed: {
        	
        },
        methods:{
            direct(target, pro) {
                if (pro) {
                    GaHelper.sendEvent(GaHelper.Usercenter.buyDashboard, pro.name);
                }
                this.$router.push({
                    name: target
                })
            },
            finish(){
            	this.$router.push({
            		name:'waitingconfirmation'
            	})
            }
            
        },
    }
</script>
<style type="text/css" scoped>
    /*头部*/
    .order-head {
        height: 270px;
        color: #fff;
        background: #091f27;
        padding-top: 15px;
    }
    .order-head .item-1180 .order-title {
        font-size: 40px;
        font-weight: normal;
    }
    .order-head .item-1180 .order-tips {
        margin-top: 10px;
    }
    .order-head .order-title{
    	width: 960px;
    	margin:30px auto;
    }
    .order-head h3{
    	font-size: 40px;
        font-weight: normal;
    }
    .item-1180 .item_box {
        width: 100%;
        height: 60px;
        line-height: 60px;
        margin-top: 15px;
        text-align: center;
    }
    .item-1180 .item-title {
        font-size: 14px;
        font-weight: bold;
        display: table;
        width: auto;
        height: 36px;
        line-height: 36px;
        text-indent: 5px;
        margin: 0 auto;
        background: #10A7D8;
        color: #FFFFFF;
    }
    .order-nav-bar {
        height: 70px;
        margin: 0 60px;
    }
    .logo {
        width: 70px;
        height: 70px;
        background-position: 0 -546px;
        float: left;
    }
    .payment-item{
    	width: 50%;
    	margin: 0 auto;
    	margin-top: 60px;
    }
    .payment-item .p{
    	text-align: center;
    	font-size: 16px;
    }
    .payment-item .span{
    	color: #35DB00;
    	font-weight: bold;
    }
    .payment-item .payment-information{
    	width:500px;
    	height:160px;
    	border-radius: 5px;
    	margin: 0 auto;
    	margin-top: 30px;
    	border: 1px solid gray;
    }
    .payment-item .payment-information .span_{
    	font-weight: bold;
    }
     .payment-item .payment-information p{
     	font-size: 16px;
     	text-indent:14%;
     	padding: 12px 0;
     }
     .payment-item button{
     	height: 38px;
		width:44%;
		color: #fff;
		background:#355867;
		border-radius: 5px;
		font-size: 18px;
		margin-top: 30px;
		margin-left:28%;
     }
</style>
