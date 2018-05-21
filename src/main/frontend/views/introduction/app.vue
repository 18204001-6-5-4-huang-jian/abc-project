<template>
	<div class="introduction-wrap">
		<div class="introduction-content">
			<div class="introduction-banner">
				<div class="introduction-nav-bar clearfix">
					<div class="logo abcdata-icon pointer" @click="direct('introduction')"></div>
					<div class="login" @click="direct('registration')" v-if="!isLogin">
						<!--<i class="abcdata-icon icon-login"></i>-->
						<span v-text="lang=='zh_CN'?'注册':'Register'"></span>
					</div>
					<div class="login" @click="direct('login')" v-if="!isLogin" style="border: none;">
						<!--<i class="abcdata-icon icon-login"></i>-->
					    <span v-text="lang=='zh_CN'?'登录':'Login'"></span>
					</div>
					<div class="login" @click="direct('my')" v-if="isLogin">
						<span>Dashboard &#10142;</span>
					</div>
				  <div class="language-tab">
				      <div class="btn-lang" @click="changelang('zh_CN')" :class="{btn_active:lang == 'zh_CN'}">中文</div>
				      <div class="btn-lang" @click="changelang('')" :class="{btn_active:lang == ''}">En</div>
			      </div>
				</div>
				<div id="head-text" class="introduction-title-wrap">
					<h2 class="introduction-title">ONE OF THE SHARPEST TOOLS ON THE MARKET</h2>
					<h2 class="introduction-subtitle">OBTAIN AHEAD OF MARKET INSIGHT OF COMPANIES' CRITICAL OPERATIONAL DATA</h2>
				</div>
			</div>
			<div class="introduction-description">
				<div class="item-1180">
					<h2 class="description-title">
						Eversight.AI is an artificial intelligence equity research company that transforms public data into market business intelligence that is ahead of the curve.
					</h2>
					<div class="description-text">
						We primarily cover Chinese companies that publicly trade in the United States and Hong Kong among many other global companies. With cutting-edge Artificial Intelligence technologies, we deliver our clients business intelligence at the forefront of the market. The crucial information we provide is invaluable and for making your investment decisions wisely.
					</div>
				</div>
			</div>
			<div class="introduction-product-list">
				<div class="item-1180">
					<div class="production-list-title section-title" v-text="lang=='zh_CN'?'我 们 覆 盖 的 股 票':'Stocks We Cover'"></div>
					<ul class="introduction-products clearfix">
						<li class="introduction-products-item">
							<i class="abcdata-icon tencent"></i>
						</li>
						<li class="introduction-products-item right-product">
							<i class="abcdata-icon momo"></i>
						</li>
						<li class="introduction-products-item odd">
							<i class="abcdata-icon sina"></i>
						</li>
						<li class="introduction-products-item right-product odd">
							<i class="abcdata-icon net-easy"></i>
						</li>
						<li class="introduction-products-item last">
							<i class="abcdata-icon baidu"></i>
						</li>
						<li class="introduction-products-item right-product">
							<i class="abcdata-icon alibaba"></i>
						</li>
						<li class="introduction-products-item odd">
							<i class="abcdata-icon jd"></i>
						</li>
						<li class="introduction-products-item right-product odd">
							<i class="abcdata-icon cheetah"></i>
						</li>
						<li class="introduction-products-item">
							<i class="abcdata-icon ctrip"></i>
						</li>
						<li class="introduction-products-item right-product last">
							<i class="abcdata-icon vip"></i>
						</li>
					</ul>
<!-- 					<div class="more-products">
						<router-link class="more-products-link" :to="{name:'order'}">
							MORE PRODUCTS
							<i class="abcdata-icon arrow"></i>
						</router-link>
					</div> -->
				</div>
			</div>
			<div class="introduction-price">
				<div class="item-1180 clearfix">
					<!--<div class="price-title section-title">Plan & Price</div>-->
					<div class="price-title section-title" v-text="lang=='zh_CN'?'我 们 的 预 测':'Our predictions'"></div>
					<!--<ul class="price-list clearfix">

						<li class="price-list-card standard">
							<div class="serve-category">Standard</div>
							<ul class="rights-list">
								<li class="rights">
									<i class="icon-rights abcdata-icon"></i>
									<span class="rights-text">$100,000 per year for a single company 
You'll be able to see basic real-time information about a company</span>
								</li>
								<li class="rights">
									<i class="icon-rights abcdata-icon"></i>
									<span class="rights-text">Taking advantage of cutting-edge information</span>
								</li>
							</ul>
							<div class="purchase-panel">
								<div class="amount">$100,000</div>
								<div class="unit-price">
									per company/year
								</div>
								<button class="btn-purchase" @click="purchase(0)">BUY NOW</button>
							</div>
						</li>
						<li class="price-list-card advanced">
							<div class="serve-category">Advanced</div>
							<ul class="rights-list">
								<li class="rights">
									<i class="icon-rights abcdata-icon"></i>
									<span class="rights-text">$200,000 per year for a single company with competitor view</span>
								</li>
								<li class="rights">
									<i class="icon-rights abcdata-icon"></i>
									<span class="rights-text">Taking advantage of seeing not only your portfolio company but its competitors’ real-time information</span>
								</li>
							</ul>
							<div class="purchase-panel">
								<div class="amount">$200,000</div>
								<div class="unit-price">
									per company/year
								</div>
								<button class="btn-purchase" @click="purchase(1)">BUY NOW</button>
							</div>
						</li>
						<li class="price-list-card last enterprise">
							<div class="serve-category">Enterprise</div>
							<ul class="rights-list">
								<li class="rights">
									<i class="icon-rights abcdata-icon"></i>
									<span class="rights-text">$600,000 per year for all the companies that we have covered </span>
								</li>
								<li class="rights">
									<i class="icon-rights abcdata-icon"></i>
									<span class="rights-text">Taking advantage of everything from Modeling.AI.</span>
								</li>
								<li class="rights">
									<i class="icon-rights abcdata-icon"></i>
									<span class="rights-text">One of the most powerful & sharpest weapons in the market</span>
								</li>
								<li class="rights">
									<i class="icon-rights abcdata-icon"></i>
									<span class="rights-text">More than 10 companies added per month</span>
								</li>
							</ul>
							<div class="purchase-panel">
								<div class="amount">$600,000</div>
								<div class="unit-price">
									for everything/year
								</div>
								<button class="btn-purchase" @click="purchase(2)">BUY NOW</button>
							</div>
						</li>
					</ul>-->
<!-- 					<div class="demo">
						<button class="btn-demo" @click="demo">
							GET A DEMO
							<i class="abcdata-icon arrow"></i>
						</button>
					</div> -->
					<!--3d-->
					<ul id="box">
					<li class="threedslide" v-for="item in product">
						<img class="img" src="../../../webapp/images/_old/lunbo-bg.jpg">
						<img class="_img" src="../../../webapp/images/_old/img-yuan.png" />
						<img class="dashboard_img" :src="item.image_url"/>
						<h4 v-text="(item.correct).toFixed(1) + '%'"></h4>
						<h5 v-text="lang=='zh_CN'?'准确率':'CORRECT'"></h5>
						<div class="div">
							<p><span class="left-span" v-text="lang=='zh_CN'?'我们预测的营收':'Expected revenue'"></span><span class="right-span">{{item.revenue_unit + item.revenue_expected | currency}}</span></p>
							<p><span class="left-span" v-text="lang=='zh_CN'?'实际营收':'Actual revenue'"></span><span class="right-span">{{item.revenue_unit + item.revenue_actual | currency}}</span></p>
						    <p class="p"><span class="left-span" v-text="lang=='zh_CN'?'我们发布的日期':'Our release date'"></span><span class="right-span">{{item.date_release_ours}}</span></p>
						    <p><span class="left-span" v-text="lang=='zh_CN'?'官方发布日期':'Official release date'"></span><span class="right-span">{{item.date_release_official}}</span></p>
						</div>
					</li>
				    </ul>
				</div>
			</div>
			<div class="introduction-contact-us">
				<div class="item-1180">
					<div class="contact-us-title section-title" v-text="lang=='zh_CN'?'联 系 我 们':'Contact Us'"></div>
					<ul class="contact-methods-wrap clearfix">
						<li class="contact-methods email">
							<i class="abcdata-icon"></i>
							<div class="contact-text-container">
								<div class="contact-text" v-text="lang=='zh_CN'?'官方邮箱':'Email us'"></div>
								<a class="contact-detail" href="mailto:service@Eversight.ai">service@Eversight.ai</a>
							</div>
						</li>
						<li class="contact-methods phone">
							<i class="abcdata-icon"></i>
							<div class="contact-text-container">
								<div class="contact-text" v-text="lang=='zh_CN'?'演示申请':'Request a demo'"></div>
								<span class="contact-detail" v-text="lang=='zh_CN'?'欢迎发送邮件预约':'Send us email to arrange a demo'"></span>
							</div>
						</li>
						<li class="contact-methods address last">
							<i class="abcdata-icon"></i>
							<div class="contact-text-container">
								<div class="contact-text" v-text="lang=='zh_CN'?'公司地址':'Company address'"></div>
								<span class="contact-detail" v-text="lang=='zh_CN'?'我们的办公地点位于中国北京':'We are based in Beijing'"></span>
							</div>
						</li>
					</ul>
					<div class="footer">
						Copyright &copy 2016 - 2017 Eversight.AI. All Rights Reserved. 
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
export default{
	name: 'introduction',
	data(){
		return {
			first:'Stocks We Cover',
			isLogin: false,
			product:[]
		}
	},
	computed:{
		lang(){
	        	return this.$store.state.introduction.lang;
	        }
	},
	mounted(){
		var self = this;
		if(self.lang == 'zh_CN'){
			localStorage.setItem('lang','zh_CN')
		}else{
			localStorage.setItem('lang','')
		}
		$(window).scrollTop(0);
		U.init((bol)=>{
			if(bol && sessionStorage.getItem('uid')){
				self.isLogin = true;
			}
		})
		$.get({
			url:'/api/v1/products/predictions',
			contentType:'application/json;charset=utf-8',
			async:false,
			success:function(res){
				self.product = res.data.list;
			},
			error:function(error){
				console.log(error)
			}
		})
		self.$nextTick(()=>{
			$('.introduction-banner').css({'height':$('body').height()})
			$('#head-text').animate({top:'38%',opacity:1}, 700, 'linear');
			//3d轮播
			var box = document.getElementById('box');
			var a = threedslide({
				element : box,
				autoplay : true,
				next : function(a,b){
					
				},
				prev : function(a,b){
					
				},
				firstChange : function(a){
					a.style.opacity = 1;
				},
				secondChange : function(a){
					a.style.opacity = 0.5;
				},
				lastChange : function(a){
					a.style.opacity = 0.5;
				}
			})
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
		   }
    },
    watch:{
    	lang(newValue){
    		if(newValue == 'zh_CN'){
    			localStorage.setItem('lang','zh_CN')
    		}else if(newValue == ''){
    			localStorage.setItem('lang','')
    		}
    	}
    },
	methods:{
		direct(to) {
			this.$router.push({
				name:to
			})
		},
		purchase(num){
			this.$store.commit('zk_setInitPlanIndex',num)
			this.$router.push({
				name:'order'
			})
		},
		demo(){
			console.log('demo')
		},
		changelang(lang){
			if(lang == 'zh_CN'){
				//中文
				this.$store.commit('hj_changlang','zh_CN')
			}else if(lang == ''){
				//英文
				this.$store.commit('hj_changlang','')
			}
		}
	}
}
</script>
<style scoped>
	.introduction-banner{
		position: relative;
		overflow: hidden;
		text-align: center;
		background: url(/images/img-banner.jpg) no-repeat center;
		background-size: cover;
	}
	.introduction-nav-bar{
		height: 110px;
		margin: 20px 80px;
	}
	.logo{
		width: 110px;
		height: 110px;
		background-position: 0 0;
		float: left;
	}
	.login{
		float: right;
		margin-top: 35px !important;
		width: 100px;
		height: 36px;
		line-height: 36px;
		text-align: center;
		cursor: pointer;
		border: 1px solid #fff;
		color: #fff;
		border-radius: 5px;
		transition: background 0.2s linear;
		font-weight: bold;
		user-select: none;
		margin:0  5px;
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
	.introduction-title-wrap{
		width: 1055px;
		height: 85px;
		position: absolute;
		top: 48%;
		left: 50%;
		opacity: 0.2;
		transform: translateX(-50%);
		color: #fff;
		font-family: "goodTimes";
	}
	.introduction-title{
		font-size: 35px;
		margin-bottom: 35px;
	}
	.introduction-subtitle{
		font-size: 18px;
	}
	.introduction-description{
		background: url(/images/img-description.jpg) no-repeat center;
		background-size: cover;
		padding-top: 100px;
		height: 475px;
		text-align: center;
	}
	.description-title{
		font-family: "irisUpc";
		font-size: 50px;
		color: #fff;
		margin: 0 auto 50px;
		line-height: 1;
		font-weight: 100;
	}
	.description-text{
		font-size: 16px;
		color: #fff;
		line-height: 1.5;
		letter-spacing: 1px;
	}
	.section-title{
		/*font-family: 'irisUpc';*/
		/*font-family:'SimHei';*/
		/*font-size: 70px;*/
		font-size: 30px;
		color: #000;
		text-align: center;
		margin-bottom: 40px;
	}
	/*产品列表*/
	.production-list-title{
		margin-bottom: 0;
	}
	.introduction-product-list{
		padding-top: 70px;
		height: 600px;
		text-align: center;
	}
	.introduction-products{
		margin-top: 60px;
	}
	.introduction-products-item{
		position: relative;
		float: left;
		width: 235px;
		height: 140px;
	}
/*	.introduction-products-item:after{
		position: absolute;
		content: '';
		right: 0;
		top: 25px;
		height: 90px;
		border-left: 1px solid #eee;
	}
	.introduction-products-item.last:after{
		content: none;
	}*/
	.introduction-products-item .abcdata-icon{
		position: absolute;
		width: 165px;
		height: 55px;
		left: 50%;
		top: 50%;
		margin-left: -83px;
		margin-top: -28px;
	}
	.introduction-products-item .abcdata-icon.tencent{
		background-position: 0 -270px;
	}
	.introduction-products-item .abcdata-icon.momo{
		background-position: -165px -270px;
	}
	.introduction-products-item .abcdata-icon.sina{
		background-position: -330px -270px;
	}
	.introduction-products-item .abcdata-icon.net-easy{
		background-position: -495px -270px;
	}
	.introduction-products-item .abcdata-icon.baidu{
		background-position: -660px -270px;
	}
	.introduction-products-item .abcdata-icon.alibaba{
		background-position: -825px -270px;
	}
	.introduction-products-item .abcdata-icon.jd{
		background-position: 0 -325px;
	}
	.introduction-products-item .abcdata-icon.cheetah{
		background-position: -165px -325px;
	}
	.introduction-products-item .abcdata-icon.ctrip{
		background-position: -330px -325px;
	}
	.introduction-products-item .abcdata-icon.vip{
		background-position: -495px -325px;
	}
	.more-products{
		margin: 30px auto 0;
		width: 200px;
		height: 50px;
		color: #0a1e27;
		line-height: 50px;
		cursor: pointer;
		border-radius: 3px;
		transition: all 0.2s linear;
	}
	.abcdata-icon.arrow{
		display: inline-block;
		width:10px;
		height: 10px;
		vertical-align: 2px;
	}
	.more-products .abcdata-icon.arrow{
		background-position: -22px -444px;
	}
	.more-products:hover{
		background-color: #ebf3f6;
	}
	.more-products-link{
		color: #0a1e27;
	}
	/*价格列表*/
	.introduction-price{
		height: 850px;
		background: url(/images/price-bg.jpg) no-repeat center;
		background-size: cover;
		padding-top: 70px;
	}
	.price-title{
		color: #fff;
	}
	.price-list-card{
		width: 380px;
		height: 490px;
		border-radius: 5px;
		background: #fff;
		float: left;
		margin-right: 20px;
		padding: 10px 25px 0;
	}
	.serve-category{
		margin-bottom: 24px;
		text-align: center;
		font-size: 60px;
		font-family: 'irisUpc';
	}
	.price-list-card.enterprise .rights{
		margin-bottom: 10px;
	}
	.price-list-card.standard .rights{
		margin-bottom: 38px;
	}
	.price-list-card.advanced .rights{
		margin-bottom: 53px;
	}
	.rights-list{
		height: 210px;
	}
	.icon-rights{
		width: 20px;
		height: 20px;
		display: inline-block;
		vertical-align: top;
		margin-right: 10px;
		background-position: 0 -526px;
	}
	.rights-text{
		display: inline-block;
		vertical-align: top;
		width: 295px;
		/*word-break: break-all;*/
		font-size: 14px;
	}
	.purchase-panel{
		text-align: center;
	}
	.purchase-panel .amount{
		font-size: 36px;
	}
	.purchase-panel .unit-price{
		color: #666;
		font-size: 14px;
		margin: 10px 0 15px;
	}
	.purchase-panel .btn-purchase{
		transition: background 0.2s linear;
		background-color: #091f27;
		color: #fff;
		width: 100px;
		height: 36px;
		border-radius: 5px;
	}
	.purchase-panel .btn-purchase:hover{
		background-color: #16343f;
	}
	.demo{
		margin-top: 65px;
		text-align: center;
	}
	.btn-demo{
		width: 200px;
		height: 50px;
		color: #fff;
		border-radius: 3px;
		transition: background 0.2s linear;
		background: rgba(255,255,255,.3);
	}
	.btn-demo .arrow{
		background-position: -32px -444px;
	}
	.btn-demo:hover{
		color: #000;
		background: #ebf3f6;
	}
	.btn-demo:hover .arrow{
		background-position: -22px -444px;
	}
	/*联系我们*/
	.introduction-contact-us{
		padding-top: 70px;
	}
	.contact-methods{
		float: left;
		width: 380px;
		height: 350px;
		border: 1px solid #eee !important;
		margin-right: 18px;
		text-align: center;
	}
	.contact-methods.email .abcdata-icon{
		margin-top: 110px;
		display: inline-block;
		width: 65px;
		height: 65px;
		background-position: 0 -380px;
	}
	.contact-methods.phone .abcdata-icon{
		margin-top: 110px;
		display: inline-block;
		width: 65px;
		height: 65px;
		background-position: -65px -380px;
	}
	.contact-methods.address .abcdata-icon{
		margin-top: 110px;
		display: inline-block;
		width: 65px;
		height: 65px;
		background-position: -130px -380px;
	}
	.contact-methods .contact-text{
		margin: 70px 0 18px;
		font-size: 18px;
	}
	.contact-methods .contact-detail{
		font-size: 14px;
		color: #666;
	}
	.footer{
		height: 100px;
		font-size: 12px;
		line-height: 90px;
		border-top: 1px solid #eee;
		margin-top: 70px;
		color: #797979
	}
	/*重写userbar*/
	.introduction-nav-bar .userBar{
		margin-top: 45px;
	}
	/*3d轮播*/
	.introduction-price #box{
			width: 1000px;
			height: 500px;
			margin: 20px auto;
	}
	.introduction-price #box .img{
		width: 100%;
		height: 100%;
	}
	.introduction-price #box ._img{
		position: absolute;
		left: 100px;
		bottom:60px;
	}
	.introduction-price #box .dashboard_img{
		position: absolute;
		left: 260px;
		top:60px;
	}
	.introduction-price #box h4{
		position: absolute;
		bottom:140px;
		left: 160px;
		width: 50px;
		height: 20px;
		line-height: 20px;
		text-align: center;
		color: #FFFFFF;
	}
	.introduction-price #box h5{
		position: absolute;
		bottom:110px;
		left: 152px;
		width: 60px;
		height: 20px;
		line-height: 20px;
		text-align: center;
		color:darkgrey;
	}
	.introduction-price #box .div{
		width: 300px;
		height: 200px;
		position: absolute;
		top: 150px;
		right: 50px;
	}
	.introduction-price #box .div p{
		width: 100%;
		height:40px;
		line-height: 40px;
	}
	.introduction-price #box .div .p{
		margin-top: 30px;
	}
	.introduction-price #box .div p .left-span{
		display: block;
		float: left;
	}
	.introduction-price #box .div p .right-span{
		display: block;
		float: right;
	}
	.introduction-price #box li{
		transition: all 1s ease;
	}
	/*中英文切换*/
	.language-tab{
		width:150px;
		height: 30px;
		line-height:30px;
		text-align:center;
		float: right;
		margin-top: 40px !important;
	}
	.language-tab .btn-lang:first-child{
		border-right: 0.5px solid #FFFFFF;
	}
	.language-tab .btn-lang:last-child{
		border-left: 0.5px solid  #FFFFFF;
	}
	.language-tab .btn-lang{
		width: 50%;
		height: 30px;
		line-height:30px;
		text-align:center;
		float: left;
		/*font-weight: bold;*/
		cursor: pointer;
		color:#FFFFFF;
		opacity: 0.6;
	}
	.btn_active{
		color:#FFFFFF !important;
		opacity: 1 !important;
	}
</style>
