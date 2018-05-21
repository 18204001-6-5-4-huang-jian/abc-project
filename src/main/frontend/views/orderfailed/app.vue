<template>
    <transition name="component-fade">
        <div class="order-wrap">
            <div class="order-head">
                <div class="order-nav-bar clearfix">
                    <div class="logo abcdata-icon pointer" @click="direct('introduction')"></div>
                    <user-bar></user-bar>
                </div>
                <div class="order-title">
                	<h3>Order Failed</h3>
                </div>
            </div>
            <img src="../../../webapp/images/_old/icon-gantan.png" class="img"/>
            <p>很遗憾，系统中没有查到您的汇款信息，请与汇款银行确认是否汇款成功，或<span @click="recommit()">重新提交订单</span></p>
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
            	
            }
        },
        mounted() {
            GaHelper.sendPageView();
        	var self = this;
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
            recommit(){
            	this.$router.push({
                    name: 'order'
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
    .order-wrap p{
    	text-align: center;
    	font-size: 16px;
    }
    .order-wrap p span{
    	font-weight: bold;
    	text-decoration: underline;
    	cursor: pointer;
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
    img{
    	width: 200px;
    	height: 200px;
    	display: block;
    	margin: 20px auto;
    }
</style>
