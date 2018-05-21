<template>
<transition name="component-fade">
	<div class="order-finish-wrap">
		<div class="order-head">
			<div class="order-nav-bar clearfix">
				<div class="logo abcdata-icon pointer" @click="direct('introduction')"></div>
				<div class="login" @click="direct('login')" v-if="!isLogin">
					<i class="abcdata-icon icon-login"></i>
					<span>LOGIN</span>
				</div>
				<users-bar></users-bar>	
			</div>
			<div class="item-1180">
				<h1 class="order-title">Order successfully</h1>
			</div>
		</div>
		<div class="content-wrap">
			<div class="icon-order-finish"></div>
			<div class="text-wrapper">
				<p class="order-success-title">您已经完成支付<span v-text="'￥'+ price +','"></span>恭喜您升级到<span v-text="plan"></span>版本，<span style="text-decoration: underline;cursor: pointer;" @click="godashboard('my')">开始使用吧！</span></p>
				<!-- <div class="order-success-suggest">
					Your payment is bank transfer,please remittance within the given time.We will received the remittance in the first time for you to open account.
				</div>
				<div class="remittance-instructions">
					<div class="remittance-instructions-title">
						Remittance instructions
					</div>
					<div class="account-information">
						<p class="account-text">
							<span class="account-key">Collecting bank:</span>
							<span class="account-value">中国银行（香港）</span>
						</p>
						<p class="account-text">
							<span class="account-key">Child account name:</span>
							<span class="account-value">YYGP-ZHAO XUMING</span>
						</p>
						<p class="account-text">
							<span class="account-key">Child account number:</span>
							<span class="account-value">0641546161556</span>
						</p>
						<p class="account-text">
							<span class="account-key">SWIFT CODE:</span>
							<span class="account-value">BKCHKCHCHCH</span>
						</p>
						<p class="account-text">
							<span class="account-key">Bank address in Chinese:</span>
							<span class="account-value">香港中环皇后大中1号</span>
						</p>
						<p class="account-text">
							<span class="account-key">Bank address in English:</span>
							<span class="account-value">1Granden Road,Central. Hong Kong</span>
						</p>
						<p class="account-text">
							<span class="account-key">Correspondence address:</span>
							<span class="account-value">Suites 3201-3204, One Exhcnag Square,8</span>
						</p>
					</div>
				</div> -->
			</div>
		</div>
		<div class="order-finish-footer clearfix item-1180">
			<div class="copyright">
				Copyright &copy 2016 - 2017 Eversight.AI. All Rights Reserved. 
			</div>
			<div class="contact">
				<a class="contact-email" href="mailto:service@Eversight.ai">Email:service@Eversight.ai</a>
			</div>
		</div>
	</div>
</transition>
</template>
<script type="text/javascript">
import UsersBar from '../common/userbar.vue'
export default{
	name: 'order-finish',
	data(){
		return{
			id:null,
			isLogin: false,
			price:'',
			plan:''
		}
	},
	mounted(){
		GaHelper.sendPageView();
		var self = this;
        //this.id = this.$route.params.id
        self.id = self.$route.params.id
		if(sessionStorage.getItem('uid') && $.cookie()['token']){
			self.isLogin = true;
		};
		self.price = localStorage.getItem('price');
		self.plan = localStorage.getItem('plan');
	},
	components: {
		UsersBar,
	},
	methods:{
		direct(target){
			this.$router.push({
				name: target
			})
		},
		godashboard(target){
			this.$router.push({
				name: target
			})
		}
	}
}
</script>
<style type="text/css" scoped>
	.order-finish-wrap{
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
	}
	.order-head{
		height: 270px;
		color: #fff;
		background: #091f27;
		border-top: 1px solid transparent;
	}
	.order-head .item-1180 .order-title{
		font-size: 40px;
		font-weight: normal;
	}
	.order-nav-bar{
		height: 70px;
		margin: 24px 60px 55px;
	}
	.logo{
		width: 70px;
		height: 70px;
		background-position: 0 -546px;
		float: left;
	}
	.login{
		float: right;
		width: 110px;
		height: 35px;
		line-height: 33px;
		text-align: center;
		cursor: pointer;
		border: 1px solid #fff;
		color: #fff;
		border-radius: 5px;
		transition: background 0.2s linear;
		font-weight: bold;
	}
	.icon-login{
		width: 10px;
		height: 12px;
		display: inline-block;
		vertical-align: 0px;
		margin-right: 10px;
		background-position: 0px -445px;
	}
	.login:hover{
		color: #0a1e27;
		background: #fff;
	}
	.login:hover .icon-login{
		background-position: -11px -444px;
	}
	/*内容区*/
	.icon-order-finish{
		width: 130px;
		height: 130px;
		margin: 90px auto 0;
		background: url(/images/icon-order.png) no-repeat center;
		background-size: cover;
	}
	.order-success-title{
		font-size: 16px;
		color:#36af4a;
		text-align: center;
		margin: 40px 0 24px;
		font-weight: bold;
	}
	.order-success-suggest{
		width: 620px;
		margin: 0 auto;
		text-align: center;
		line-height: 1.5;
	}
	.remittance-instructions-title{
		font-size: 16px;
		color:#333;
		text-align: center;
		margin: 40px 0 24px;
	}
	.account-information{
		width: 540px;
		height: 200px;
		margin: 0 auto 80px;
		background-color: #f6fbfc;
		padding: 10px 25px;
		font-size: 12px;
		border-radius: 3px;
	}
	.account-text{
		line-height: 25px;
	}
	.account-key{
		color: #78909c;
		display: inline-block;
		width: 180px;
	}
	/*底部*/
	.order-finish-footer{
		position: absolute;
		bottom: 0;
		/*width: 100%;*/
		left: 50%;
		transform: translateX(-50%);
		line-height: 100px;
		color: #797979;
		font-size: 12px;
		border-top: 1px solid #eee;
	}
	.order-finish-footer .copyright{
		float: left;
	}
	.order-finish-footer .contact{
		float: right;
		padding-right: 10px;
	}
	.order-finish-footer .contact .contact-email{
		color: #797979;
	}
</style>