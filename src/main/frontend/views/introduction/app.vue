<template>
	<div class="introduction-wrap swiper-container">
		<div class="introduction-content swiper-wrapper">
		<!-- introduction-banner -->
			<div class="introduction-banner introduction-banner swiper-slide">
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
				<div class="introduction-footer">
					<div class="gonext">
						<img class="img" src="../../../webapp/images/gonext.png">
					</div>
				</div>
			</div>
			<!-- introduction-description -->
			<div class="introduction-description introduction-banner swiper-slide">
				<div class="item-1180 introduction-description-container">
					<h2 class="description-title">
						Eversight.AI is an artificial intelligence equity research company that transforms public data into market business intelligence that is ahead of the curve.
					</h2>
					<div class="description-text">
						We primarily cover Chinese companies that publicly trade in the United States and Hong Kong among many other global companies. With cutting-edge Artificial Intelligence technologies, we deliver our clients business intelligence at the forefront of the market. The crucial information we provide is invaluable and for making your investment decisions wisely.
					</div>
				</div>
			</div>
			<!-- introduction-product-list -->
			<div class="introduction-product-list introduction-banner swiper-slide">
				<div class="item-1180 introduction-product-list-container">
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
				</div>
			</div>
			<!-- introduction-price -->
			<div class="introduction-price introduction-banner swiper-slide">
				<div class="item-1180 clearfix">
					<div class="price-title section-title" v-text="lang=='zh_CN'?'我 们 的 预 测':'Our predictions'"></div>
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
			<!-- introduction-contact-us -->
			<div class="introduction-contact-us introduction-banner swiper-slide">
				<div class="item-1180 introduction-contact-us-container">
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
		<div class="swiper-pagination"></div>
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
		//swiper
		var mySwiper = new Swiper('.swiper-container', {
			direction: 'vertical',
            mousewheel: true,
            pagination: {
		        el: '.swiper-pagination',
		        clickable: true,
		    }
		})
		 $('.gonext').click(function(){
		    mySwiper.slideNext();
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
		changelang(lang){
			//切换介绍页中英文
			this.$store.commit('hj_changlang',lang);
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
		height:100%;
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
		-ms-transform:translateX(-50%); 	/* IE 9 */
		-moz-transform:translateX(-50%); 	/* Firefox */
		-webkit-transform:translateX(-50%); /* Safari 和 Chrome */
		-o-transform:translateX(-50%); 	/* Opera */
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
		padding-top:100px;
		height: 100%;
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
		line-height:4;
		letter-spacing: 1px;
	}
	.introduction-footer{
		width: 80%;
		height: 50px;
		line-height: 50px;
		position: absolute;
		bottom:5%;
		left: 10%;
		/*border: 2px solid #ffffff;*/
	}
	.introduction-footer .gonext{
		border: 2px solid #fff;
	    height: 45px;
	    width: 30px;
	    border-radius: 30px;
	    cursor: pointer;
	    position: relative;
	    margin:0 auto;
	}
	.introduction-footer .gonext .img{
		width: 16px;
		vertical-align: middle;
	    position: absolute;
	    top: 12px;
	    left: 50%;
	    -ms-transform:translateX(-50%); 	/* IE 9 */
		-moz-transform:translateX(-50%); 	/* Firefox */
		-webkit-transform:translateX(-50%); /* Safari 和 Chrome */
		-o-transform:translateX(-50%); 	/* Opera */
	    transform: translateX(-50%);
	    animation: indexmove 1s infinite;
	}
	@keyframes indexmove{
	 from{
	     top:0; 
	     }
      to{
	       top:18px;
	   }
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
		/*height: 600px;*/
		height: 100%;
		text-align: center;
		background: none;
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
		height: 100%;
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
		padding-top:70px;
		background: none;
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
		cursor: pointer;
		color:#FFFFFF;
		opacity: 0.6;
	}
	.btn_active{
		color:#FFFFFF !important;
		opacity: 1 !important;
	}
	/*swiper修改pagination样式*/
	.swiper-container{
      width: 100%;
      height: 100%;
    }
	.swiper-container-vertical > .swiper-pagination-bullets{
		right:30px;
	}
    .introduction-description-container,.introduction-contact-us-container,.introduction-product-list-container{
    	margin:150px auto;
    }
</style>
