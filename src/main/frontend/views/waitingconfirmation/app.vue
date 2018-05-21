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
            	<p class="p">请输入您发起汇款的银行卡账户信息，发起到账通知后，系统将为您确认是否到账 : </p>
            	<div class="payment-information">
            		<p><span class="span_">账户名称 :</span> <input type="text" class="input name"/></p>
            		<p><span class="span_">支付卡号 :</span> <input type="text" class="card"/></p>
            		<p><span class="span_">开户银行 :</span> <input type="text" class="bank"/></p>
            		<p class="p_date">
            			<span class="span_">汇款金额 :</span> <input type="text" class="price"/>
            			<img src="../../../webapp/images/_old/icon-rmb.png" class="img_"/>
            		</p>
            		<p class="p_date">
            	    <span class="span_ ">汇款日期 :</span> <input class="date" id="demo" style="cursor: pointer;"/>
            		<img src="../../../webapp/images/_old/icon-date.png" class="img_"/>
            		</p>
            	</div>
            	<button @click="finish()" class="finish-remittance">发起到账通知</button>
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
            	order_id:''
            }
        },
        mounted() {
            GaHelper.sendPageView();
        	var self = this;
        	$(".input").focus();
        	//datetimepicker初始化
        	$("#demo").datetimepicker({
			format: 'yyyy-mm-dd',//日期的格式
		    startDate:'1900-01-01',//选择器的开始日期
		    autoclose:true,//日期选择完成后是否关闭选择框
		    bootcssVer:3,//显示向左向右的箭头
		    language:'zh_CN',//语言
		    minView: "month",//表示日期选择的最小范围，默认是hour
		    endDate: new Date(),
		    todayBtn: true,
			todayHighlight: true
        });
        $(".datetimepicker").addClass("changedatetimepicker");
        //金额输入框千分位
        $(".price").blur(function(){
            var result = '', counter = 0;
		    var num = ($(".price").val() || 0).toString();
		    for (var i = num.length - 1; i >= 0; i--) {
		        counter++;
		        result = num.charAt(i) + result;
		        if (!(counter % 3) && i != 0) { result = ',' + result; }
		    }
		    $(".price").val(result);
         })
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
            verify(){
            	if(!$(".name").val().trim()){
				new PNotify({
					text:'name can\'t be empty',
					type:'error',
				})
				return false;
			    };
			    if(!$(".card").val().trim()){
				new PNotify({
					text:'card can\'t be empty',
					type:'error',
				})
				return false;
			    };
			    if(!/^(([1-9][0-9]*)|(([0]\.\d{1,2}|[1-9][0-9]*\.\d{1,2})))$/.test($(".card").val().trim())){
				new PNotify({
					text:'card must be number',
					type:'error',
				})
				return false;
			    }
			    if(!$(".bank").val().trim()){
				new PNotify({
					text:'bank can\'t be empty',
					type:'error',
				})
				return false;
			    };
			    if(!$(".price").val().trim()){
				new PNotify({
					text:'price can\'t be empty',
					type:'error',
				})
				return false;
			    };
//              if(!/^(([1-9][0-9]*)|(([0]\.\d{1,2}|[1-9][0-9]*\.\d{1,2})))$/.test($(".price").val().trim())){
//				new PNotify({
//					text:'price must be number',
//					type:'error',
//				})
//				return false;
//			    }
			    if(!$(".date").val().trim()){
				new PNotify({
					text:'date can\'t be empty',
					type:'error',
				})
				return false;
			    };
            },
            finish(){
            	var self = this;
            	self.order_id = localStorage.getItem("order_id");
            	if(self.verify() == false){
				return false;
			    }
                $.ajax({
               	url:"api/v1/orders/transfer/" + self.order_id,
               	type:'post',
               	contentType: 'application/x-www-form-urlencoded;charset=utf-8',
               	data:{
               		name:$(".name").val(),
               		card:$(".card").val(),
               		bank:$(".bank").val(),
               		price:$(".price").val(),
               		date:$(".date").val()
               	},
        		success:(res)=>{
          			self.$router.push({
                    name:'waitingconfirmationdown'
                });
        		}
               })
            	
            }
        },
    }
</script>
<style type="text/css">
    .changedatetimepicker{
   	background: #FFFFFF !important;
   	margin-left: -50px;
   	margin-top:20px;
    }
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
    .payment-item .payment-information{
    	width:400px;
    	height:240px;
    	margin: 0 auto;
    	border-radius: 5px;
    	margin-top: 30px;
    	border: 1px solid gray;
    }
    .payment-item .payment-information .span_{
    	font-weight: bold;
    }
    .payment-item .payment-information p:first-child{
    	padding-top: 20px;
    }
    .payment-item .payment-information p:last-child{
    	padding-bottom: 20px;
    }
     .payment-item .payment-information p{
     	font-size: 16px;
     	text-indent:14%;
     	padding: 8px 0;
     	width: 100%;
     }
     .payment-item .payment-information .huang-div{
     	width:100%;
     	height: 40px;
     	text-indent: 14%;
     	margin-top: 5px;
     }
     .payment-item .payment-information .huang-div h6{
     	float: left;
     	width: 130px;
     	font-size: 16px;
     	font-weight: bold;
     }
     .payment-item .payment-information .huang-div .box{
     	float: left;
     	margin-left:-24px;
     }
     .payment-item .payment-information input{
     	border: 1px solid gray;
     	text-indent: 8px;
     	width: 200px;
     	border-radius:4px;
     }
     .payment-item .payment-information input::-webkit-input-placeholder {
      /* WebKit browsers */
       text-align:left;
      }
     .payment-item .payment-information .p_date{
     	position: relative;
     }
     .payment-item .payment-information .p_date img{
     	position: absolute;
     	right:75px;
     	top: 12px;
     	pointer-events:none;
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
		cursor: pointer;
     }
</style>
