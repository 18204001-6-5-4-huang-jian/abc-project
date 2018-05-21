<template>
    <transition name="component-fade">
        <div class="order-wrap">
            <div class="order-head">
                <div class="order-nav-bar clearfix">
                    <div class="logo abcdata-icon pointer" @click="direct('introduction')"></div>
                    <user-bar></user-bar>
                </div>
                <div class="order-title">
                	<h3>Waiting for payment confirmation</h3>
                </div>
            </div>
            <div class="payment-item">
            	<img src="../../../webapp/images/_old/icon-daojishi.png" class="img"/>
            	<p class="p">您的到账通知已经提交，系统会在1~3个工作日内确认到账信息，确认到账后，会自动为您开通<span v-text="plan_name"></span></spam>版本权限。</p>
                <div>
                	 <button class="watch" @click="watch('orderhistory')">查看订单</button><button class="return" @click="return_my('my')">返回首页</button>
                </div>
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
            	plan_name:''
            }
        },
        mounted() {
            GaHelper.sendPageView();
        	var self = this;
        	if(localStorage.getItem("plan_name")){
        		self.plan_name = localStorage.getItem("plan_name")
        	}else{
        		self.plan_name = 'Enterprise';
        	}
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
            watch(target){
            	this.$router.push({
                    name: target
                })
            },
            return_my(target){
            	this.$router.push({
                    name: target
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
    	width: 60%;
    	margin: 0 auto;
    	margin-top: 60px;
    }
    .payment-item .p{
    	text-align: center;
    	font-size: 16px;
    	margin-top: 50px;
    }
    .payment-item img{
    	width: 200px;
    	height: 200px;
    	display: block;
    	margin: 20px auto;
    }
    .payment-item div{
    	width: 80%;
    	margin:40px auto;
    	display:flex;
    	align-items: center;
    	justify-content: center;
    }
    .payment-item div button{
    	width: 100px;
    	height: 30px;
    	line-height: 30px;
    	text-align: center;
    	margin-top: 50px;
        color: #fff;
		border-radius: 5px;
		font-size: 14px;
		margin: 0 15px;
    }
     .payment-item div .watch{
     	background:#1296db;
     }
     .payment-item div .return{
      	background:#091f27;
     }
</style>
