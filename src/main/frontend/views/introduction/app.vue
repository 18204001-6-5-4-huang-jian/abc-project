<template>
	<div class="introduction-wrap swiper-container">
		<div class="introduction-content swiper-wrapper">
		<!-- introduction-banner -->
			<div class="introduction-banner introduction-banner swiper-slide">
				<div class="introduction-nav-bar clearfix">
					<div class="logo abcdata-icon pointer" @click="direct('introduction')"></div>
					<div class="lang-tab">
						<div class="langtab" :class="{langtabActive:lang == 'zh_CN'}" @click="changeLang('zh_CN')">中文</div>
						<div class="line"></div>
						<div class="langtab" :class="{langtabActive:lang == ''}" @click="changeLang('')">英文</div>
					</div>
				</div>
				<div id="head-text" class="introduction-title-wrap">
					<h2 class="introduction-title" v-text="lang=='zh_CN'?'欢迎您':'WELCOME'">
					<!-- ONE OF THE SHARPEST TOOLS ON THE MARKET -->
					</h2>
					<h2 class="introduction-subtitle" v-html="lang=='zh_CN'?'即刻进入市场上最犀利的工具之一<br/><br/>掌握企业关键运营数据的市场洞察力':'ONE OF THE SHARPEST TOOLS ON THE MARKET <br/><br/> OBTAIN AHEAD OF MARKET INSIGHT OF COMPANIES CRITICAL OPERATIONAL DATA'">
					</h2>
				</div>
				<div class="introduction-footer">
					<div class="gonext">
						<img class="img" src="../../../webapp/images/gonext.png">
					</div>
				</div>
				<!-- 重构登录注册 -->
				<div class="login-container-reset">
				<!-- 邮箱登录 0-->
                    <div v-show="showpage==0">
                        <div class="login-container-wx" @click="showpage=1" ><img src="../../../webapp/images/weixin.png"></div>
                    	<div class="login-container-Logtitle" v-text="lang=='zh_CN'?'邮箱登录':'Email Login'"></div>
                    	<div>
                    		<input :placeholder="lang=='zh_CN'?'邮箱':'Email'" class="login-container-Logemail" type="email" @keyup.enter="enter()" @change="setValue($event)" @blur="setValue($event)">
                    		<input :placeholder="lang=='zh_CN'?'密码':'Password'" class="login-container-Logpwd" type="password" @keyup.enter="enter()" @change="setValue($event)" @blur="setValue($event)">
                    		<span class="login-container-Logpwd-text"  v-if="wrongTip" v-text="wrongTip" ></span>
                    		<span class="login-container-forget" @click="showpage=7" v-text="lang=='zh_CN'?'忘记密码?':'Forget password?'"></span>
                    	</div>
                    	<button class="login-container-loginup" @click="enter()" v-text="lang=='zh_CN'?'登录':'Login'"></button>
                    	<div class="login-container-create" @click="showpage=4" v-text="lang=='zh_CN'?'创建账户':'Click here to register'"></div>
                    </div>
				<!-- 微信登录 1-->
                    <div v-show="showpage==1">
                    	<div class="login-container-wxlog" @click="showpage=0"><img src="../../../webapp/images/computer.png"></div>
                    	<div class="login-container-wxtitle" v-text="lang=='zh_CN'?'微信登录':'Wechat Login'"></div>
                    	<div class="login-container-wximg">
                    		<div id="login_container_really"></div>
                    	</div>
                    	<div class="login-container-wxtext" v-text="lang=='zh_CN'?'微信扫一扫，即可登录':'Scan here to login'"></div>
                    	<div class="login-container-create-fill" @click="openNewPageWx" v-text="lang=='zh_CN'?'二维码异常?':'Having troubles with QR code?'"></div>
                    	<div class="login-container-create" @click="showpage=4" v-text="lang=='zh_CN'?'创建账户':'Click here to register'"></div>
                    </div>
                 <!-- 未注册邮箱 2-->
                 	<div v-show="showpage==2">
                 		<div class="login-container-warnlog"><img src="../../../webapp/images/warn.png"></div>
                 		<div class="login-container-warntext" :class="{login_container_warntext_en:lang==''}" v-text="lang=='zh_CN'?'抱歉，该邮箱未注册':'This email is not registered'"></div>
                 		<div class="login-container-warntxt" v-text="lang=='zh_CN'?'您可以点击下方“注册”按钮前往注册您的账户!':'You can click the  register button below to register your account!'"></div>
                 		<div class="login-container-warnfill" @click="showpage=4" v-text="lang=='zh_CN'?'注册':'Register'"></div>
                 		<div class="login-container-warnback" @click="showpage=0" v-text="lang=='zh_CN'?'返回':'Back'"></div>
                 	</div>
                 <!-- 未注册微信 3 -->
                    <div v-show="showpage==3">
                 		<div class="login-container-warnlog"><img src="../../../webapp/images/warn.png"></div>
                 		<div class="login-container-warntext" :class="{login_container_warntext_en:lang==''}" v-text="lang=='zh_CN'?'抱歉，该邮箱未注册':'This email is not registered'"></div>
                 		<div class="login-container-warntxt" v-text="lang=='zh_CN'?'您可以点击下方“注册”按钮前往注册您的账户！':'You can click on the registration button below to register your account.'"></div>
                 		<div class="login-container-warnfill" @click="showpage=4" v-text="lang=='zh_CN'?'注册':'Register'"></div>
                 		<div class="login-container-warnback" @click="showpage=0" v-text="lang=='zh_CN'?'返回':'Back'"></div>
                 	</div>
                 <!-- 注册 4-->
                 	<div v-show="showpage==4">
                 		<div class="login-container-join" :class="{login_container_join_en:lang==''}" v-text="lang=='zh_CN'?'创建账户':'Create an account'">创建账户</div>
                 		<div class="login-container-joinwx">
                 			<div class="login-container-joinwx-text" :class="{login_container_joinwx_text_en:lang==''}"  v-text="lang=='zh_CN'?'微信注册':'Wechat register'"></div>
                 			<div class="login-container-joinwx-img" @click="showpage=5"><img src="../../../webapp/images/right.png"></div>
                 			<div class="login-container-joinwx-txt"  :class="{login_container_joinwx__txt_en:lang==''}" v-text="lang=='zh_CN'?'更便捷的登录体验':'A more convenient login experience'"></div>
                 		</div>
                 		<div class="login-container-joinemail">
                 			<div class="login-container-joinemail-text" :class="{login_container_joinemail_text_en:lang==''}" v-text="lang=='zh_CN'?'邮箱注册':'Email register'"></div>
                 			<div class="login-container-joinemail-img"  @click="showpage=6"><img src="../../../webapp/images/right.png"></div>
                 			<div class="login-container-joinemail-txt" :class="{login_container_joinemail_txt_en:lang==''}"  v-text="lang=='zh_CN'?'海外用户方便快捷':'Overseas users are convenient and quick'"></div>
                 		</div>
                 		<div class="login-container-joinemail-local" :class="{login_container_joinemail_local_en:lang==''}"  v-text="lang=='zh_CN'?'已有账户？':'An existing account?'" @click="showpage=0">已有账户？</div>
                 	</div>
				<!-- 微信扫码注册 5-->
					<div v-show="showpage==5">
                    	<div class="login-container-wxtitle" v-text="lang=='zh_CN'?'微信注册':'Wechat register'"></div>
                    	<div class="login-container-wximg">
                    		<div id="login_container_really_register"></div>
                    	</div>
                    	<div class="login-container-wxtext" v-text="lang=='zh_CN'?'微信扫一扫，即可注册':'A sweep of WeChat can be registered'">微信扫一扫，即可注册</div>
                    	<div class="login-container-create-fill" @click="openNewPageWx" v-text="lang=='zh_CN'?'二维码异常?':'Two-dimensional code anomaly?'" >
                    	</div>
                    	<div class="login-container-joinemail-local login-container-joinemail-local_" :class="{login_container_joinemail_local_en:lang==''}"  v-text="lang=='zh_CN'?'已有账户？':'An existing account?'" @click="showpage=0">已有账户？</div>
					</div>
				<!-- 完善信息邮件 6-->
					<div v-show="showpage==6">
						<div class="login-container-Information"  :class="{login_container_Information_en:lang==''}"  v-text="lang=='zh_CN'?'完善个人信息':'Improve personal information'"></div>
						<div class="login-container-Informationtext">
							<input class="login-container-Informationo" type="text" :placeholder="lang=='zh_CN'?'用户名':'User name'"  @keyup.enter="register()"></input>
							<input class="login-container-Informationt" type="text" :placeholder="lang=='zh_CN'?'机构名称':'Organization name'" @keyup.enter="register()"></input>
							<input class="login-container-Informationh" type="email" :placeholder="lang=='zh_CN'?'商业邮箱':'Business mailbox'" @keyup.enter="register()"></input>
							<input class="login-container-Informationf" type="password" :placeholder="lang=='zh_CN'?'密码(最少6位)':'Password (at least 6 bits)'" @keyup.enter="register()"></input>
						</div>
						<div  class="login-container-Information-tooltip" v-if="wrongTooltip" v-text="wrongTooltip"></div>
						<div>
							<button class="login-container-Information-back" @click="showpage=0" v-text="lang=='zh_CN'?'返回':'Back'"></button>
							<button class="login-container-Information-sure" @click="register()" v-text="lang=='zh_CN'?'确认':'Sure'"></button>
						</div>
						<div class="login-container-joinemail-local" :class="{login_container_joinemail_local_en:lang==''}"  v-text="lang=='zh_CN'?'已有账户？':'Existing account?'"  @click="showpage=0"></div>
					</div>
					<!-- 完善信息微信 -->
					<div v-show="showpage==11">
						<div class="login-container-Information-wechat" :class="{login_container_Information_wechat_en:lang==''}"  v-text="lang=='zh_CN'?'完善个人信息':'Improve personal information'"></div>
						<div class="login-container-Informationtext-wechat">
							<input class="login-container-Informationo-wechat" type="text" :placeholder="lang=='zh_CN'?'用户名':'User name'" @keyup.enter="registerWechat()"></input>
							<input class="login-container-Informationt-wechat" type="text" :placeholder="lang=='zh_CN'?'机构名称':'Organization name'" @keyup.enter="registerWechat()"></input>
							<input class="login-container-Informationh-wechat" type="email" :placeholder="lang=='zh_CN'?'商业邮箱':'Business mailbox'" @keyup.enter="registerWechat()"></input>
							<input class="login-container-Informationf-wechat" type="password" :placeholder="lang=='zh_CN'?'密码(最少6位)':'Password (at least 6 bits)'" @keyup.enter="registerWechat()"></input>
						</div>
						<div  class="login-container-Information-tooltip-wechat" v-if="wrongTooltipwechat" v-text="wrongTooltipwechat"></div>
						<div>
							<button class="login-container-Information-back-wechat" @click="showpage=0" v-text="lang=='zh_CN'?'返回':'Back'"></button>
							<button class="login-container-Information-sure-wechat" @click="registerWechat()" v-text="lang=='zh_CN'?'确认':'Sure'"></button>
						</div>
						<div class="login-container-joinemail-local" @click="showpage=0" :class="{login_container_joinemail_local_en:lang==''}"  v-text="lang=='zh_CN'?'已有账户？':'Existing account?'"></div>
					</div>
				<!-- 忘记密码 7-->
					<div v-show="showpage==7">
						<div class="login-container-forgetlog"><img src="../../../webapp/images/warn.png"></div>
						<div class="login-container-forgetext" :class="{login_container_forgetext_en:lang==''}" v-text="lang=='zh_CN'?'身份验证':'Authentication'"></div>
						<input class="login-container-forgetInfor" type="text" :placeholder="lang=='zh_CN'?'您的邮箱(验证我的身份)':'Your mailbox(Verify my identity)'" @keyup.enter="forgetEmail()"></input>
						<div class="login-container-forgetInfortxt" v-if="forgetps" v-text="forgetps"></div>
						<button class="login-container-forgetsure" v-text="lang=='zh_CN'?'验证':'Verification'" @click="forgetEmail()"></button>
						<button class="login-container-forgetback" @click="showpage=0" v-text="lang=='zh_CN'?'返回':'Back'"></button>
					</div>
				<!-- 更改密码身份认证 8  -->
					<div v-show="showpage==8">
						<div class="login-container-pw"><img src="../../../webapp/images/warn.png"></div>
						<div class="login-container-pwtext" v-text="lang=='zh_CN'?'身份验证':'Authentication'"></div>
						<div class="login-container-pwInfor"  v-text="lang=='zh_CN'?'请检查您的电子邮件并完成身份认证':'Please check your email and complete identity authentication'">请检查您的电子邮件并完成身份认证</div>
						<div  class="login-container-pb"></div>
						<button class="login-container-pwtsure" v-text="lang=='zh_CN'?'验证':'Verification'" @click="identityVerify()">验证</button>
						<button class="login-container-pwback" @click="showpage=7" v-text="lang=='zh_CN'?'返回':'Back'"></button>
					</div>	
                 <!-- 邮箱注册验证 9-->
				    <div v-show="showpage==9">
						<div class="login-container-forgetlog"><img src="../../../webapp/images/hook.png"></div>
						<div class="login-container-welcometxt" :class="{login_container_welcometxt_en:lang==''}" v-text="lang=='zh_CN'?'欢迎来到Eversight.ai':'Welcome to Eversight.ai'"></div>
						<div class="login-container-gisrion"  :class="{login_container_gisrion_en:lang==''}" v-text="lang=='zh_CN'?'谢谢您注册Eversight.ai':'Thank you for registration Eversight.ai'"></div>
						<div class="login-container-cheek" v-text="lang=='zh_CN'?'请检查您的电子邮件并完成帐户激活':'Please check your email and complete the account activation'" ></div>
						<div class="login-container-emailnum" v-text="useremail"></div>
						<button class="login-container-btuboom" @click="emailActivate()"  v-text="lang=='zh_CN'?'激活':'activation'"></button>
						<button class="login-container-btugoback" @click="showpage=6" v-text="lang=='zh_CN'?'返回':'Back'"></button>				
					</div>
					<!-- 微信注册验证 10 -->
					<div v-show="showpage==10">
						<div class="login-container-forgetlog" ><img src="../../../webapp/images/hook.png"></div>
						<div class="login-container-welcometxtw" v-text="lang=='zh_CN'?'账户激活':'Account activation'">账户激活</div>
						<div  class="login-container-gisrionw" :class="{login_container_gisrionw_en:lang==''}" v-text="lang=='zh_CN'?'您输入的电子邮件地址需要激活':'The email address you entered needs to be activated'"></div>
						<div class="login-container-cheekw" :class="{login_container_cheekw_en:lang==''}"  v-text="lang=='zh_CN'?'请检查您的电子邮件并完成帐户激活':'Please check your email and complete the account activation'"></div>
						<div class="login-container-emailnumw" v-text="useremail"></div>
						<button class="login-container-btuboomw" @click="wechatActivate()" v-text="lang=='zh_CN'?'激活':'activation'">激活</button>
						<button class="login-container-btugobackw" @click="showpage=0" v-text="lang=='zh_CN'?'返回':'Back'"></button>					
					</div>
					<!-- 忘记密码重新设置 -->
					<div v-show='showpage==12'>
						<div class="login-container-forgetlog"><img src="../../../webapp/images/hook.png"></div>
						<div class="login-container-changpw" :class="{login_container_changpw_en:lang==''}" v-text="lang=='zh_CN'?'重设密码':'Reset the password'"></div>
						<input class='login-container-changeiniput' :placeholder="lang=='zh_CN'?'新密码(至少6位)':'New password (at least 6 bits)'" @keyup.enter="confirmPassword">
						<input class='login-comtainer-changeagain'  :placeholder="lang=='zh_CN'?'确认密码':'Confirm the password'" @keyup.enter="confirmPassword">
						<div class='login-container-changetxt' v-if="misspw" v-text="misspw" ></div>
						<button class="login-container-surepw" @click="confirmPassword" v-text="lang=='zh_CN'?'完成':'complete'"></button>
						<button class="login-container-callpw" @click="showpage=0" v-text="lang=='zh_CN'?'返回':'Back'"></button>			
					</div>
                 </div>
			</div>
			<!-- introduction-description -->
			<div class="introduction-description introduction-banner swiper-slide">
				<div class="item-1180 introduction-description-container">
					<h2 class="description-title" v-text="lang=='zh_CN'?'- 关于我们 -':'- About Us -'">
					</h2>
					<div class="description-text">
					<!-- <div class="description-text-img">
						<h3>Eversight.AI</h3>
						<h4 v-text="lang=='zh_CN'?'人工智能股权研究公司':'Artificial intelligence equity research company'"></h4>
					</div> -->
						<div class="text">
						    <div class="description-text-img">
								<h3>Eversight.AI</h3>
								<h4 v-text="lang=='zh_CN'?'人工智能股权研究公司':'Artificial intelligence equity research company'"></h4>
							</div>
							<p class="container" :class="{container_:lang==''}" v-html="lang=='zh_CN'?'Eversight.AI是一家人工智能股权研究公司，将公共数据转化为领先于市场的商业智能。<br/><br/>我们主要涵盖在许多其他全球公司中在美国和香港公开交易的中国公司。 凭借尖端的人工智能技术，我们为客户提供商业智能，处于市场的最前沿。 我们提供的关键信息非常宝贵，可以明智地做出您的投资决策。':'Eversight.AI is an artificial intelligence equity research company that transforms public data into market business intelligence that is ahead of the curve.<br/><br/>We primarily cover Chinese companies that publicly trade in the United States and Hong Kong among many other global companies. With cutting-edge Artificial Intelligence technologies, we deliver our clients business intelligence at the forefront of the market. The crucial information we provide is invaluable and for making your investment decisions wisely.'">
							</p>
						</div>
					</div>
				</div>
			</div>
			<!-- introduction-product-list -->
			<div class="introduction-product-list introduction-banner swiper-slide">
				<div class="item-1180 introduction-product-list-container">
					<div class="production-list-title section-title" v-text="lang=='zh_CN'?'- 我 们 覆 盖 的 股 票 -':'- Stocks We Cover -'"></div>
					<ul class="introduction-products clearfix">
						<li class="introduction-products-item">
							<i class="abcdata-icon tencent"></i>
						</li>
						<li class="introduction-products-item">
							<i class="abcdata-icon htht"></i>
						</li>
						<li class="introduction-products-item odd">
							<i class="abcdata-icon momo"></i>
						</li>
						<li class="introduction-products-item odd">
							<i class="abcdata-icon east-money"></i>
						</li>
						<li class="introduction-products-item last">
							<i class="abcdata-icon xdf"></i>
						</li>
						<li class="introduction-products-item">
							<i class="abcdata-icon rcl"></i>
						</li>
						<li class="introduction-products-item odd">
							<i class="abcdata-icon tal"></i>
						</li>
						<li class="introduction-products-item odd">
							<i class="abcdata-icon club"></i>
						</li>
						<li class="introduction-products-item">
							<i class="abcdata-icon ccl"></i>
						</li>
						<li class="introduction-products-item last">
							<i class="abcdata-icon tuhuashun"></i>
						</li>
						<li class="introduction-products-item">
							<i class="abcdata-icon vip"></i>
						</li>
						<li class="introduction-products-item last">
							<i class="abcdata-icon cheetah"></i>
						</li>
						<!-- add -->
					</ul>
				</div>
			</div>
			<!-- introduction-price -->
			<div class="introduction-price introduction-banner swiper-slide">
				<div class="item-1180 clearfix">
					<div class="price-title section-title" v-text="lang=='zh_CN'?'- 我 们 的 预 测 -':'- Our predictions -'"></div>
					<!--3d-->
					<ul id="box">
					<li class="threedslide" v-for="item in product">
						<img class="img" src="../../../webapp/images/_old/lunbo-bg.jpg">
						<img class="dashboard_img" :src="item.img_url"/>
						<h3 v-text="lang=='zh_CN'?'我们的误差':'Our error'"></h3>
						<!-- <h4 v-text="(100-item.correct).toFixed(2) + '%'"></h4> -->
						<h4 v-text="item.our_err"></h4>
						<div class="div">
							<p><span class="left-span" v-text="lang=='zh_CN'?'我们的预测(USD)':'Our prediction(USD)'"></span><!-- <span class="right-span">{{item.revenue_unit + item.revenue_expected | currency}}</span> -->
							<span class="right-span">{{item.our_exp}}</span>
							</p>
							<p><span class="left-span" v-text="lang=='zh_CN'?'实际营收(USD)':'Actual revenue(USD)'"></span>
							<!-- <span class="right-span">{{item.revenue_unit + item.revenue_actual | currency}}</span> -->
							<span class="right-span">{{item.real_rev}}</span>
							</p>
						    <p class="p"><span class="left-span" v-text="lang=='zh_CN'?'分析师预测(USD)':'Analyst forecast(USD)'"></span>
						    <!-- <span class="right-span">{{item.date_release_ours}}</span> -->
						    <span class="right-span">{{item.analyst_exp}}</span>
						    </p>
						    <p><span class="left-span" v-text="lang=='zh_CN'?'分析师误差':'Analyst error'"></span>
						    <!-- <span class="right-span">{{item.date_release_official}}</span> -->
						    <span class="right-span">{{item.analyst_err}}</span>
						    </p>
						</div>
					</li>
				    </ul>
				</div>
			</div>
			<!-- introduction-contact-us -->
			<div class="introduction-contact-us introduction-banner swiper-slide">
				<div class="item-1180 introduction-contact-us-container" style="width:100%;">
					<div class="contact-us-title section-title" v-text="lang=='zh_CN'?'- 联 系 我 们 -':'- Contact Us -'"></div>
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
			isLogin: false,
			product:[],
			email:"",
			pwd:"",
			showpage:0,
			wrongTip:"",
			wrongTooltip:'',
			wrongTooltipwechat:'',
			tipTimer:null,
			tooltipTimer:null,
			tooltipTimerwechat:null,
			forgetps: '',
            forgetm:null,
            misspw: '',
			missChange:null,
		}
	},
	computed:{
		 lang(){
	        return this.$store.state.introduction.lang;
	     },
	     useremail(){
	     	return this.$store.state.introduction.email;
	     }
	},
	mounted(){
		    GaHelper.sendPageView();
			var url_ = location.origin + "/view/bind/excessive.html";
			   if(location.hostname == "www.eversight.ai"){
				var obj = new WxLogin({
		        id:"login_container_really",
		        appid: "wx4ff7c9952429c23c",
		        scope: "snsapi_login",
		        redirect_uri: encodeURIComponent(url_),
		        state: Math.round(Math.random()*10000000),
		        style: "",
		        href:'https://' + location.hostname + "/view/bind/css/bind.css"
	         });
			  }else if(location.hostname == "data-dev.modeling.ai"){
				var obj = new WxLogin({
	            id:"login_container_really",
	            appid: "wx6e4329dacb96437c",
	            scope: "snsapi_login",
	            redirect_uri: encodeURIComponent(url_),
		        state: Math.round(Math.random()*10000000),
		        style: "",
		        href:'https://' + location.hostname + "/view/bind/css/bind.css"
	         });
		}
		var url_ = location.origin + "/view/bind/excessive.html";
			   if(location.hostname == "www.eversight.ai"){
				var obj = new WxLogin({
		        id:"login_container_really_register",
		        appid: "wx4ff7c9952429c23c",
		        scope: "snsapi_login",
		        redirect_uri: encodeURIComponent(url_),
		        state: Math.round(Math.random()*10000000),
		        style: "",
		        href:'https://' + location.hostname + "/view/bind/css/bind.css"
	         });
			  }else if(location.hostname == "data-dev.modeling.ai"){
				var obj = new WxLogin({
	            id:"login_container_really_register",
	            appid: "wx6e4329dacb96437c",
	            scope: "snsapi_login",
	            redirect_uri: encodeURIComponent(url_),
		        state: Math.round(Math.random()*10000000),
		        style: "",
		        href:'https://' + location.hostname + "/view/bind/css/bind.css"
	         });
		}
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
			url:'/api/v1/products/predictions2',
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
		        clickable: true
		    }
		})
		$('.gonext').click(function(event){
			event.stopPropagation();
		    mySwiper.slideNext();
		  })
		self.$nextTick(function(){
			if(localStorage.getItem("showpage")){
		 	self.showpage = localStorage.getItem('showpage');
            localStorage.removeItem('showpage');
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
		   }
    },
	methods:{
		direct(target) {
			this.$router.push({
				name:target
			})
		},
		purchase(num){
			this.$store.commit('zk_setInitPlanIndex',num)
			this.$router.push({
				name:'order'
			})
		},
		changeLang(lang){
            this.$store.commit('hj_changlang',lang);
		},
		//邮件登录赋值email和password
		setValue(e){
           if(e.target.type == 'email'){
           	 this.email = e.target.value;
           }else{
           	 this.pwd = e.target.value;
           }
		},
		/*登录验证*/
		verify(){
			if(!$(".login-container-Logemail").val().trim()) {
				this.wrongTip= "E-mail can\'t be empty";
				return false;
			}

			if($(".login-container-Logemail").val().trim().indexOf("@") == -1){
				this.wrongTip = "illegal enail";
				return false;
			}
			if(!$(".login-container-Logpwd").val().trim()){
				this.wrongTip = "Password can\'t be empty";
				return false;
			}
			if($(".login-container-Logpwd").val().trim().length<6){
				this.wrongTip="Password can\'t be shorter than six";
				return false;
			}
			return true;
		},
		// 邮件登录
		enter(){
			    var self = this;
				var lang = localStorage.getItem("lang");
				window.localStorage.clear();
				window.sessionStorage.clear();
				localStorage.setItem("lang",lang);
				if(lang == null){
					localStorage.setItem("lang","zh_CN");
				}
				if(!self.verify()){
					return false;
				}
				localStorage.setItem("huangjian",true);
				$.post({
					url: U.getApiPre() + '/api/v1/account/login',
					data:{
						email:this.email,
						password:$.md5(this.pwd),
						lang:lang
					},
					timeout:5000,
					success:(resp)=>{
						if(resp.success){
							if(resp.status == 0){
							//判断新老用户
							var day = new Date(resp.data.token.expiry.slice(0,-4)).getTime()-new Date();
							$.cookie("token",resp.data.token.auth_token,{
								expires:parseInt(day/1000/3600/24),
								path:'/'
							});
							localStorage.setItem("status",resp.status);
							localStorage.setItem("token",resp.data.token.auth_token);
							localStorage.setItem("uid",resp.data.user.id);
							localStorage.setItem("useremail",resp.data.user.email);
							this.$store.commit('hj_saveEmail',resp.data.user.email);
							//用于growingIO
							localStorage.setItem("username",resp.data.user.username);
							localStorage.setItem("usercompany",resp.data.company);
							localStorage.setItem("plan_name",resp.data.plan_name);
							localStorage.setItem("plan_name_zh_CN",resp.data.plan_name_zh_CN);
							self.savePass(resp.data.user.id,resp.data.user.username,resp.data.user.head_img);
							// 判断客户管理权限
							if(typeof resp.data.manager === 'number' ){
								localStorage.setItem("manager",resp.data.manager);
							}
							//标注权限和导出权限
							if(resp.data.user.right){
								sessionStorage.setItem('marker',resp.data.user.right.marker);
							}
							if(resp.data.user.right){
								sessionStorage.setItem('exports',resp.data.user.right.export);
							}
							//验证是否是用户还是分析师
							if(resp.data.user.right){
								sessionStorage.setItem('common',resp.data.user.right.common);
							}
							if (typeof resp.data.company != 'undefined') {
								self.saveCompany(resp.data.company.id, resp.data.company.role);
							}    
								 self.direct('my');
							}else if(resp.status == 1){
								//微信注册成功之后首次邮箱登陆,需要邮箱验证
								//即邮箱登录的邮箱是微信填写的 但是没激活
								localStorage.setItem("status",resp.status);
							    localStorage.setItem("uid",resp.data.user.id);
								localStorage.setItem("useremail",resp.data.user.email);
								this.$store.commit('hj_saveEmail',resp.data.user.email);
								localStorage.setItem("usercompany",resp.data.company);
								localStorage.setItem("username",resp.data.user.username);
								self.showpage = 10;
							 }
						}else if(!resp.success && resp.status == 2){
							// 账号密码失败 提示未注册邮箱
							self.wrongTip = 'Wrong E-mail or password';
							self.showpage = 2;
						}else if(!resp.success && (resp.status == 1 || resp.status == 3)){
                            self.wrongTip = 'Server exception';
						}
					},
					error:()=>{
						self.wrongTip = "Network anomaly";
					},
					complete:()=>{
                    
					}
				})
			},
			savePass:function(uid,name,headimg){
				if(!window.sessionStorage){return false;}
				sessionStorage.setItem('email',this.email);
				if(uid && name){
					sessionStorage.setItem('uid',uid);
					sessionStorage.setItem('username',name);
					//老用户默认头像
					sessionStorage.setItem('headimg',headimg);
				}
			},
			saveCompany:function (cid,role) {
				if(!window.sessionStorage){return false;}
				if(cid && role){
					sessionStorage.setItem('cid',cid);
					sessionStorage.setItem('role',role);
				}
			},
		/*邮箱完善信息验证*/
		  verifyInfo(){
		      if (!$(".login-container-Informationo").val().trim()){
		        this.wrongTooltip = "Your Full Name can't be empty";
		        return false;
		      }
		      if (!/^[^\s][A-Za-z0-9_\-\u4e00-\u9fa5\s]*[^\s]*$/.test($(".login-container-Informationo").val())){
		        this.wrongTooltip = "Your Full Name is illegal";
		        return false;
		      }
		      if (!$(".login-container-Informationt").val().trim()){
		        this.wrongTooltip = "Your Institution Name can't be empty";
		        return false;
		      }
		      if (!/^[^\s][A-Za-z0-9_\-\u4e00-\u9fa5\s]*[^\s]*$/.test( $(".login-container-Informationt").val())
		      ) {
		        this.wrongTooltip = "Your Institution Name is illegal";
		        return false;
		      }
		      if (!$(".login-container-Informationh").val().trim()){
		        this.wrongTooltip = "Your Business Email can't be empty";
		        return false;
		      }
		      if($(".login-container-Informationh").val().trim().indexOf("@") >= 0){
		      } else {
		        this.wrongTooltip = "illegal email";
		        return false;
		      }
		      if (!$(".login-container-Informationf").val().trim()){
		        this.wrongTooltip = "Your password can't be empty";
		        return false;
		      }
		      if ($(".login-container-Informationf").val().trim().length < 6){
		        this.wrongTooltip = "Password can't be shorter than six";
		        return false;
		      }
		      return true;
	    },
	    // 微信完善信息验证
	    verifyInfowechat(){
		      if (!$(".login-container-Informationo-wechat").val().trim()){
		        this.wrongTooltipwechat = "Your Full Name can't be empty";
		        return false;
		      }
		      if (!/^[^\s][A-Za-z0-9_\-\u4e00-\u9fa5\s]*[^\s]*$/.test($(".login-container-Informationo-wechat").val())){
		        this.wrongTooltipwechat = "Your Full Name is illegal";
		        return false;
		      }
		      if (!$(".login-container-Informationt-wechat").val().trim()){
		        this.wrongTooltipwechat = "Your Institution Name can't be empty";
		        return false;
		      }
		      if (!/^[^\s][A-Za-z0-9_\-\u4e00-\u9fa5\s]*[^\s]*$/.test( $(".login-container-Informationt-wechat").val())
		      ) {
		        this.wrongTooltipwechat = "Your Institution Name is illegal";
		        return false;
		      }
		      if (!$(".login-container-Informationh-wechat").val().trim()){
		        this.wrongTooltipwechat = "Your Business Email can't be empty";
		        return false;
		      }
		      if($(".login-container-Informationh-wechat").val().trim().indexOf("@") >= 0){
		      } else {
		        this.wrongTooltipwechat = "illegal email";
		        return false;
		      }
		      if (!$(".login-container-Informationf-wechat").val().trim()){
		        this.wrongTooltipwechat = "Your password can't be empty";
		        return false;
		      }
		      if ($(".login-container-Informationf-wechat").val().trim().length < 6){
		        this.wrongTooltipwechat = "Password can't be shorter than six";
		        return false;
		      }
		      return true;
	    },
	    //邮箱注册
	    register(){
	      var self = this;
	      if (!this.verifyInfo()) {
	        return false;
	      }
	      localStorage.setItem("huangjian", false);
	      localStorage.setItem("黄健", true);
	      var lang = localStorage.getItem("lang");
	      $.post({
	        url: U.getApiPre() + "/api/v1/account/register",
	        data: {
	          email:$(".login-container-Informationh").val(),
	          username: $(".login-container-Informationo").val(),
	          company: $(".login-container-Informationt").val(),
	          password: $.md5($(".login-container-Informationf").val()),
	          lang:lang
	        },
	        timeout: 1000,
	        success: resp => {
	          if (resp.success && resp.status ==1) {
	            localStorage.setItem("status", resp.status);
	            localStorage.setItem("uid", resp.data.user.id);
	            localStorage.setItem("useremail", resp.data.user.email);
	            this.$store.commit('hj_saveEmail',resp.data.user.email);
	            localStorage.setItem("usercompany", resp.data.company);
	            localStorage.setItem("username", resp.data.user.username);
	            localStorage.setItem("huang", true);
	            //注册成功，请检查邮件
                self.showpage = 9;
	         } else if(!resp.success && resp.status == 1){
	            this.wrongTooltip = "This mailbox has been registered";
	          }else if(!resp.success && resp.status == 2){
                this.wrongTooltip = "User registration failed, please contact customer service";
	          }else if(!resp.success && resp.status == 4){
	          	this.wrongTooltip = "send verify email failed";
	          }else if(!resp.success && resp.status == 3){
                this.wrongTooltip = "Server exception";
	          }
	        },
	        complete:() => {

	        }
	      });
	    },
	    registerWechat(){
           var self = this;
           var openid = localStorage.getItem('openid');
           var _name = $('.login-container-Informationo-wechat');
           var _company = $('.login-container-Informationt-wechat');
           var _email = $('.login-container-Informationh-wechat');
           var _password = $('.login-container-Informationf-wechat');
           var pwd = _password.val();
           var str = pwd.substring(0, 2) + "******" + pwd.substring(pwd.length - 2, pwd.length);
	       if(localStorage.getItem("status") == 1){
	       	//微信注册绑定的邮箱的第一次登录
	       	   localStorage.removeItem('status');
			   if(!self.verifyInfowechat()){
			        return false;
			     }
	       	  $.ajax({
				url: "/api/v1/account/first-login-bind",
				type: "post",
				contentType: 'application/x-www-form-urlencoded;charset=utf-8',
				data: {
					email: _email.val(),
					name: _name.val(),
					company: _company.val(),
					password: _password.val() == "********" ? "" : $.md5(_password.val()),
					pwd_ends: _password.val() == "********" ? "**********" : str
				},
				success:function (res){
					if (res.success == false && res.status == 2) {
						//提示用户邮箱已经绑定微信
						self.wrongTooltipwechat = "该邮箱已经绑定微信";
					} else if (res.success == true) {
						var day = new Date(res.data.token.expiry.slice(0, -4)).getTime() - new Date();
						$.cookie("token", res.data.token.auth_token, {
							expires: parseInt(day / 1000 / 3600 / 24),
							path: '/'
						});
						//保留token用于重置密码
						localStorage.setItem("token", res.data.token.auth_token);
						localStorage.setItem("uid", res.data.user.id);
						localStorage.setItem("username", res.data.user.username);
						self.savePass(res.data.user.id, res.data.user.username, res.data.user.email);
						var userid = res.data.user.id;
						$.ajax({
							url: "/api/v1/account/" + userid,
							type: "get",
							contentType: 'application/json;chartset=utf-8',
							success: function (res) {
								localStorage.setItem("useremail", res.data.email);
								localStorage.setItem("usercompany", res.data.company);
								localStorage.setItem("userpassword", res.data.pwd_ends);
								localStorage.setItem("userheadimg", res.data.head_img);
								if (document.location.protocol == "https:") {
									location.href = 'https://' + location.hostname + '/#/my';
								} else {
									location.href = 'http://' + location.host + '/#/my';
								}
							},
							error: function (error) {
							}
						})
						sessionStorage.setItem('openid', res.data.openid);
					}
				},
				error: function (error) {
					if (error.status == 401) {
						self.wrongTooltipwechat = "请您到邮箱确认登录后再点击即可修改";
					}
				}
			  })
			 return false;
	       }
	       //正常微信扫码绑定
	       if(!self.verifyInfowechat()){
			      return false;
			 }
		    $.ajax({
			url: "/api/wechat/email/bind",
			type: "post",
			contentType: 'application/x-www-form-urlencoded;charset=utf-8',
			data: {
				openid:openid,
				email: _email.val(),
				name: _name.val(),
				company: _company.val(),
				password: _password.val() == "********" ? "" : $.md5(_password.val()),
				pwd_ends: _password.val() == "********" ? "**********" : str,
				invite_code:''
			},
			success:function(res){
				if (res.success == false && res.status == 2) {
					//提示用户邮箱已经绑定微信
					self.wrongTooltipwechat = "该邮箱已经绑定微信";
				} else if (res.success == true) {
					var day = new Date(res.data.token.expiry.slice(0, -4)).getTime() - new Date();
					$.cookie("token", res.data.token.auth_token, {
						expires: parseInt(day / 1000 / 3600 / 24),
						path: '/'
					});
					//保留token用于重置密码
					localStorage.setItem("token", res.data.token.auth_token);
					localStorage.setItem("uid", res.data.user.id);
					localStorage.setItem("username", res.data.user.username);
					localStorage.setItem("plan_name", res.data.plan_name);
					localStorage.setItem("plan_name_zh_CN", res.data.plan_name_zh_CN);
					// 判断客户管理权限
					if (typeof res.data.manager === 'number') {
						localStorage.setItem("manager", res.data.manager);
					}
					self.savePass(res.data.user.id, res.data.user.username, res.data.user.email);
					var userid = res.data.user.id;
					$.ajax({
						url: "/api/v1/account/" + userid,
						type: "get",
						contentType: 'application/json;chartset=utf-8',
						success: function (res) {
							localStorage.setItem("useremail", res.data.email);
							localStorage.setItem("usercompany", res.data.company);
							localStorage.setItem("userpassword", res.data.pwd_ends);
							localStorage.setItem("userheadimg", res.data.head_img);
							if (document.location.protocol == "https:") {
								location.href = 'https://' + location.hostname + '/#/my';
							} else {
								location.href = 'http://' + location.host + '/#/my';
							}
						},
						error: function (error){

						}
					})
					sessionStorage.setItem('openid', res.data.openid);
				}
			},
			error: function (error){

			}
		  })
	    },
	    //忘记密码
	    forgetEmail(){
	    	var self = this;
	    	if(!$('.login-container-forgetInfor').val().trim()){
	    		self.forgetps = "输入不可为空";
	    		return false;
	    	}
	    	if($('.login-container-forgetInfor').val().trim().indexOf('@') == -1){
	    		self.forgetps = "输入邮箱不合法";
	    		return false;
	    	}
	    	$(".login-container-pb").text($('.login-container-forgetInfor').val());
	         $.ajax({
				url: "/api/v1/account/forget-pass?email=" + $(".login-container-pb").text() + "&lang=" + self.lang,
				type: "post",
				contentType: 'application/x-www-form-urlencoded;charset=utf-8',
				success:function(res){
					if(!res.success && res.status == 1){
                          layer.msg(res.message);
					}else if(res.success){
                          self.showpage = 8;
					}
				},
				error:function(error){
					console.log(error);
				}
			 });
	    },
	    openNewPageWx(){
	      var self = this;
	      var _url = location.origin + "/view/bind/excessive.html";
	      var appid = "wx4ff7c9952429c23c";
	      if (location.hostname == "data-dev.modeling.ai") {
	        appid = "wx6e4329dacb96437c";
	      }
	      window.location.href =
	        "https://open.weixin.qq.com/connect/qrconnect?appid=" +
	        appid +
	        "&redirect_uri=" +
	        encodeURIComponent(_url) +
	        "&response_type=code&scope=snsapi_login&state=" +
	        Math.round(Math.random() * 10000000) +
	        "#wechat_redirect";
	    },
	    // 获取URL的参数
	    getQueryString:function(name) {
	      var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
	      var r = window.location.search.substr(1).match(reg);
	      if (r != null) {
	        return decodeURI(r[2]);
	      }
	      return "";
	    },
	    //忘记密码身份验证
	    identityVerify(){
            //跳转邮箱官网
            var mailAccount =  $(".login-container-pb").text();
            var start = mailAccount.indexOf("@") + 1;
			var end = mailAccount.indexOf(".");
			var mailType = mailAccount.substring(start, end);
			var _link = "http://www.mail." + mailType + ".com";
			window.open(_link);
	    },
	    //邮箱激活button
        emailActivate(){
            var mailAccount =  $(".login-container-emailnum").text();
            var start = mailAccount.indexOf("@") + 1;
			var end = mailAccount.indexOf(".");
			var mailType = mailAccount.substring(start, end);
			var _link = "http://www.mail." + mailType + ".com";
			window.open(_link);
        },
        wechatActivate(){
        	var mailAccount =  $(".login-container-emailnumw").text();
            var start = mailAccount.indexOf("@") + 1;
			var end = mailAccount.indexOf(".");
			var mailType = mailAccount.substring(start, end);
			var _link = "http://www.mail." + mailType + ".com";
			window.open(_link);
        },
        confirmPassword(){
        	var self = this;
        	var _id = localStorage.getItem('_id');
        	var _token = localStorage.getItem('_token');
        	if(!$('.login-container-changeiniput').val().trim()){
        		this.misspw = "输入新密码不可为空";
        		return false;
        	}
        	if($('.login-container-changeiniput').val().trim().length < 6){
        		this.misspw = "输入新密码长度不可小于六位";
        		return false;
        	}
        	if(!$('.login-comtainer-changeagain').val().trim()){
        		this.misspw = "输入确认密码不可为空";
        		return false;
        	}
        	if($('.login-comtainer-changeagain').val().trim().length < 6){
        		this.misspw = "输入确认密码长度不可小于六位";
        		return false;
        	}
        	if($('.login-comtainer-changeagain').val().trim() != $('.login-container-changeiniput').val().trim()){
        		this.misspw = "新密码与确认密码不一致";
        		return false;
        	}
        	// 
            $.ajax({
			url: "/api/v1/account/forget-resetpasswd",
			type: "post",
			contentType: 'application/x-www-form-urlencoded;charset=utf-8',
			data: {
				id: _id,
				token: _token,
				password: $.md5($(".login-container-changeiniput").val()),
				confirm_password: $.md5($(".login-comtainer-changeagain").val())
			},
			success:function (resp) {
					//跳转到看板
					if (resp.success) {
						var day = new Date(resp.data.token.expiry.slice(0, -4)).getTime() - new Date();
						$.cookie("token", resp.data.token.auth_token, {
							expires: parseInt(day / 1000 / 3600 / 24),
							path: '/'
						});
						window.localStorage.clear();
						window.sessionStorage.clear();
						localStorage.setItem("token", resp.data.token.auth_token);
						localStorage.setItem("uid", resp.data.user.id);
						localStorage.setItem("username", resp.data.user.username);
						self.savePass(resp.data.user.id, resp.data.user.username, resp.data.user.email);
						var userid = resp.data.user.id;
						$.ajax({
							url: "/api/v1/account/" + userid,
							type: "get",
							contentType: 'application/json;chartset=utf-8',
							success:function (res) {
								// console.log(res);
								localStorage.setItem("useremail", res.data.email);
								localStorage.setItem("usercompany", res.data.company);
								localStorage.setItem("userpassword", res.data.pwd_ends);
								localStorage.setItem("userheadimg", res.data.head_img);
								if (document.location.protocol == "https:") {
									location.href = 'https://' + location.hostname + '/#/my';
								} else {
									location.href = 'http://' + location.host + '/#/my';
								}
							},
							error:function (error) {
								console.log(error);
							}
						})
					}else if(!resp.success && resp.status == 1){
                           self.misspw = resp.message;
					}else if(!resp.success && resp.status == 2){
						    self.misspw = resp.message;
					}
			},
			error:function (error) {
				console.log(error);
			}
		})

        }
	},
	watch:{
    	lang(newValue){
    		if(newValue == 'zh_CN'){
    			localStorage.setItem('lang','zh_CN')
    		}else if(newValue == ''){
    			localStorage.setItem('lang','')
    		}
    	},
    	wrongTip(val){
    		if(this.tipTimer){
    			clearTimeout(this.tipTimer);
    		}
    		this.tipTimer = setTimeout(()=>{
    			this.wrongTip = '';
    		},1800);
    	},
    	wrongTooltip(val){
            if(this.tooltipTimer){
            	clearTimeout(this.tooltipTimer);
            }
            this.tooltipTimer = setTimeout(()=>{
    			this.wrongTooltip = '';
    		},1800);
    	},
    	wrongTooltipwechat(val){
             if(this.tooltipTimerwechat){
            	clearTimeout(this.tooltipTimerwechat);
            }
            this.tooltipTimerwechat = setTimeout(()=>{
    			this.wrongTooltipwechat = '';
    		},1800);
    	},
    	forgetps(val){
            if(this.forgetm){
            	clearTimeout(this.forgetm);
            }
            this.forgetm = setTimeout(()=>{
    			this.forgetps = '';
    		},1800);
    	},
    	misspw(val){
            if(this.missChange){
            	clearTimeout(this.missChange);
            }
            this.missChange = setTimeout(()=>{
    			this.misspw = '';
    		},1800);
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
		background-size:cover;
		height:100%;
	}
	.introduction-nav-bar{
		height: 110px;
	    width:90%;
		margin:20px auto;
	}
	.introduction-nav-bar .lang-tab{
		width: 120px;
		height: 30px;
		line-height: 30px;
		float:right;
		margin-top: 50px;
	}
	.introduction-nav-bar .lang-tab .langtab{
		width: 49.5%;
		height:30px;
		line-height: 30px;
		float: left;
		color:deepskyblue;
		cursor: pointer;
	}
	.introduction-nav-bar .lang-tab .line{
		width:1%;
		height:30px;
		background:#FFFFFF;
		float:left;
	}
	.langtabActive{
         color:#FFFFFF !important;
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
		width:90%;
		height: 150px;
		position: absolute;
		top: 48%;
		left: 50%;
		transform: translateX(-50%);
		-ms-transform:translateX(-50%); 	/* IE 9 */
		-moz-transform:translateX(-50%); 	/* Firefox */
		-webkit-transform:translateX(-50%); /* Safari 和 Chrome */
		-o-transform:translateX(-50%); 	/* Opera */
		color: #fff;
	}
	.introduction-title{
		font-size: 35px;
		text-align: left;
		margin-bottom: 35px;
		font-weight: 100;
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	}
	.introduction-subtitle{
		font-size: 18px;
		text-align:left;
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	}
	.introduction-description-container{
		width: 1440px;
	}
	.introduction-description{
		/*background: url(/images/img-description.jpg) no-repeat center;*/
		/*background-size: cover;*/
		/*padding-top:50px;*/
		height: 100%;
		text-align: center;
		background:#FFFFFF;
	}
	.description-title{
		margin:10px auto;
		line-height: 1;
		font-size: 30px;
		color: #3E3E3E;
		text-align: center;
		font-weight: 100;
        font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	}
	.description-text{
		width:100%;
		height: 500px;
		margin:100px auto;
		position: relative;
	}
	.description-text .description-text-img{
        width:500px;
	    height:300px;
	    position:absolute;
	    left:-300px;
	    top: 100px;
	    -webkit-border-radius: 10px;
	    border-radius: 10px;
	    background: url(/images/description-img.jpg) no-repeat;
	    background-size: 660px 300px;
	    color: #fff;
	}
	.description-text .description-text-img h3{
		margin-top:95px;
		margin-bottom: 30px;
		font-family: PingFangSC-Medium;
		font-size:18px;
		color: #FFFFFF;
		line-height: 30px;
		text-shadow: 0 2px 2px rgba(0,0,0,0.10);
	}
	.description-text .description-text-img h4{
		font-family: PingFangSC-Regular;
		font-size: 15px;
		color: #FFFFFF;
		line-height: 30px;
		text-shadow: 0 2px 2px rgba(0,0,0,0.10);
	}
	.description-text .text{
         width:750px;
         height: 500px;
         line-height: 500px;
         margin:0 auto;
         margin-right:200px;
         float:right;
         background: #FFFFFF;
         box-shadow:0 0 20px gray;
         -webkit-box-shadow:0 0 20px gray;
         -moz-box-shadow:0 0 20px gray;
         position: relative;
	}
	.description-text .text .container{
		 display: block;
         width: 500px;
         height:200px;
         margin: 150px auto;
         float: right;
         text-align: left;
         white-space: normal;
         word-break:break-all;
         font-family: PingFangSC-Regular;
		 font-size: 16px;
		 color: #404040;
		 letter-spacing: 1px;
		 line-height:25px;
	}
	.description-text .text .container_{
		height: 350px;
		margin:100px auto !important; 
		word-break:normal !important;
		word-wrap: break-word;
		text-align:justify;
	}
	.introduction-footer{
		width: 80%;
		height: 50px;
		line-height: 50px;
		position: absolute;
		bottom:5%;
		left: 10%;
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
	    animation: imgmove 1s infinite;
	}
	@keyframes imgmove{
		from{
		    top:0; 
		    }
	     to{
		    top:18px;
	   }
	}
	/*login*/
	.login-container-reset{
		width:360px;
		height: 560px;
		opacity: 0.95;
		background: #FFFFFF;
		border-radius: 6px;
		position: absolute;
		top:120px;
		right:5%;
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	}
	.login-container-wx{
		width:50px;
		height:48px;
		position: absolute;
	    top: 5px;
	    right: 15px;
	    background: rgb(247, 248, 249);
	    cursor:pointer;
	}
	.login-container-Logtitle{
		/*width:128px;*/
		height:45px;
		line-height:45px;
		font-size:24px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		/*border: 1px solid #333;*/
		position:absolute;
		top:50px;
		left:32px;
		text-align: left;
	}
	.login-container-Logemail{
		width: 300px;
	    height: 48px;
	    display:block;
	    position:absolute;
	    top:145px;
	    left:35px;
	    line-height:48px;
	   	font-size:16px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
	}
	.login-container-Logemail-text{
		display:block;
		position:absolute;
		width: 300px;
		height:17px;
		/*border: 1px solid #333;*/
		margin-top:196px;
	    left:35px;
	    line-height:48px;
	   	font-size:16px;
	}
	.login-container-Logemail:-webkit-autofill {
	  -webkit-box-shadow: 0 0 0 1000px white inset !important;
	}
	.login-container-Logpwd{
		width: 300px;
	    height: 48px;
	    display:block;
	    position:absolute;
	    top:209px;
	    left:35px;
	    line-height:48px;
	   	font-size:16px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
	}
	.login-container-Logpwd:-webkit-autofill {
	  -webkit-box-shadow: 0 0 0 1000px white inset !important;
	}
	.login-container-Logpwd-text{
		display:block;
		position:absolute;
		width: 300px;
		height:17px;
		line-height:17px;
		color:red;
		margin-top:260px;
	    left:35px;
	   	font-size:16px;
	   	text-align:center;

	}
	.login-container-forget{
	    /*width: 70px;*/
	    height: 20px;
	    font-size: 14px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgba(133,144,166,1);
	    line-height: 20px;
	    display: block;
	    position: absolute;
	    margin-top: 291px;
	    margin-left: 31px;
	    cursor:pointer;
	}
	.login-container-loginup{
		width:302px;
		height:40px;
		background:rgba(0,119,209,1);
		border-radius:4px;
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(255,255,255,1);
	 	margin-top: 341px;
	 	cursor:pointer;
	}
	.login-container-create{
		/*width:64px;*/
		height:22px;
		font-size:16px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:22px;
	    text-align: center;
	    margin-top: 100px;
	    cursor:pointer;
	}
	/*wx*/
	.login-container-wxlog{
		width:50px;
		height:48px;
		position: absolute;
	    top: 5px;
	    right: 15px;
	    background: rgb(247, 248, 249);
	    cursor:pointer;
	}
	.login-container-wxtitle{
		/*width:128px;*/
		height:45px;
		line-height:45px;
		font-size:24px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		/*border:1px solid #333;*/
		position:absolute;
		top:50px;
		left:32px;
		text-align: left;
	}
	.login-container-wximg{
		width:300px;
		height:210px;
		margin-top:120px;
		margin-left:35px;
	}
	.login-container-wxtext{
		width:259px;
		height:17px;
		font-size:12px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:17px;
		margin-top:45px;
		margin-left:54px;
	}
	.login-container-create-fill{
		/*width: 70px;*/
	    height: 22px;
	    font-size: 1px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgba(133,144,166,1);
	    line-height: 22px;
	    text-align: center;
	    margin-top:10px;
	    cursor: pointer;
	}
	/*warn*/
	.login-container-warnlog{
		width:48px;
		height:48px;
		margin-top:48px;
		margin-left:31px;
	}
	.login-container-warntext{
		width:288px;
		height:45px;
		font-size:32px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:45px;
		margin-top:30px;
		margin-left:32px;
	}
	.login_container_warntext_en{
		font-size:20px;
	}
	.login-container-warntxt{
		width:297px;
		height:28px;
		font-size:13px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:28px;
		margin-top:20px;
		margin-left:33px;
	}
	.login-container-warnfill{
		width:100px;
		height:40px;
		line-height:40px;
		background:rgba(3,111,190,1);
		border-radius:25px;
		margin-top:116px;
		margin-left:33px;
		font-size:16px;
		/*font-family:PingFangSC-Light;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(255,255,255,1);
		cursor: pointer;
	}
	.login-container-warnback{
		width:100px;
		height:40px;
		line-height:40px;
		border-radius:25px;
		border:1px solid rgba(133,144,166,1);
		margin-top:24px;
		margin-left:33px;
		font-size:16px;
		/*font-family:PingFangSC-Light;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		cursor:pointer;
	}
	/*join*/
	.login-container-join{
		width:200px;
		height:45px;
		font-size:32px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:45px;
		margin-top:50px;
	}
	.login_container_join_en{
		width: 100%;
	}
	.login-container-joinwx{
		width:300px;
		height:120px;
		background:rgba(255,255,255,1);
		box-shadow:0px 2px 4px 0px rgba(0,0,0,0.1);
		opacity:0.8009000000000001;
		border:1px solid rgba(255,255,255,1);
		margin-top:50px;
		margin-left:33px;
	}
	.login-container-joinwx:hover>.login-container-joinwx-text,.login-container-joinwx:hover>.login-container-joinwx-txt{
		color:rgba(0,119,209,1);
	}
	.login-container-joinwx-text{
		width:80px;
		height:28px;
		font-size:20px;
		/*font-family:PingFangSC-Medium;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(102,102,102,1);
		line-height:28px;
		margin-top:32px;
		margin-left:29px;
	}
	.login_container_joinwx_text_en{
		    width: 100%;
    	text-align: left;
	}
	.login-container-joinwx-img{
		float: right;
	}
	.login-container-joinwx-img img{
		width:24px;
		height:24px;
		position: absolute;
		top:181px;
		right:56px;
		cursor: pointer;
	}
	.login-container-joinwx-txt{
		width:112px;
		height:20px;
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(153,153,153,1);
		line-height:20px;
		margin-top:8px;
		margin-left:29px;
	}
	.login_container_joinwx__txt_en{
		width: 100%;
		text-align: left;
	}
	.login-container-joinemail{
		width:300px;
		height:120px;
		background:rgba(255,255,255,1);
		box-shadow:0px 2px 4px 0px rgba(0,0,0,0.1);
		opacity:0.8009000000000001;
		border:1px solid rgba(255,255,255,1);
		margin-top:30px;
		margin-left:33px;
	}
	.login-container-joinemail:hover>.login-container-joinemail-text,.login-container-joinemail:hover>.login-container-joinemail-txt{
		color:rgba(0,119,209,1);
	}
	.login-container-joinemail-text{
		width:80px;
		height:28px;
		font-size:20px;
		/*font-family:PingFangSC-Medium;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(102,102,102,1);
		line-height:28px;
		margin-top:32px;
		margin-left:29px;
	}
	.login_container_joinemail_text_en{
		width: 100%;
		text-align: left;
	}
	.login-container-joinemail-img{
		float: right;
	}
	.login-container-joinemail-img img{
		width:24px;
		height:24px;
		position: absolute;
		top:331px;
		right:56px;
		cursor:pointer;
	}
	.login-container-joinemail-txt{
		width:112px;
		height:20px;
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(153,153,153,1);
		line-height:20px;
		margin-top:8px;
		margin-left:29px;
	}
	.login_container_joinemail_txt_en{
		width: 100%;
		text-align: left;
	}
	.login-container-joinemail-local{
		width:80px;
		height:22px;
		font-size:16px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:22px;
		margin-top:91px;
		margin-left:143px;
		cursor: pointer;
	}
	.login-container-joinemail-local_{
		margin-top: 50px !important;
	}
	.login_container_joinemail_local_en{
		width: 100%;
		text-align: left;
		margin-left: 100px;
	}
	.login_container_joinemail_local_en{
		width: 100%;
		text-align: left;
		margin-left: 100px;
	}
	/*邮件个人信息*/
	.login-container-Information{
		width:200px;
		height:45px;
		font-size:32px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:45px;	
		margin-top:50px;
		margin-left:32px
	}
	.login_container_Information_en{
		width: 100%;
		font-size: 22px;
		text-align: left;
	}
	.login-container-Informationt{
		display: block;
	    width: 300px;
	    height: 48px;
	    font-size: 16px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgb(155, 157, 159);
	    line-height: 24px;
	    margin-top: 16px;
	    margin-left: 40px;
	}
	.login-container-Informationh{
		display: block;
	    width: 300px;
	    height: 48px;
	    font-size: 16px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgb(155, 157, 159);
	    line-height: 24px;
	    margin-top: 16px;
	    margin-left: 40px;
	}
	.login-container-Informationh:-webkit-autofill {
	  -webkit-box-shadow: 0 0 0 1000px white inset !important;
	}
	.login-container-Informationf{
		display: block;
	    width: 300px;
	    height: 48px;
	    font-size: 16px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgb(155, 157, 159);
	    line-height: 24px;
	    margin-top: 16px;
	    margin-left: 40px;
	}
	.login-container-Informationf:-webkit-autofill {
	  -webkit-box-shadow: 0 0 0 1000px white inset !important;
	}
	.login-container-Information-tooltip{
		position:absolute;
	    width: 300px;
	    height: 15px;
	    color:red;
	    line-height: 15px;
	    margin-top: 5px;
	    margin-left: 40px;
	    text-align:center;

	}
	.login-container-Informationo{
		display: block;
	    width: 300px;
	    height: 48px;
	    font-size: 16px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgb(155, 157, 159);
	    line-height: 24px;
	    margin-top: 50px;
	    margin-left: 40px;
	}
	.login-container-Information-back{
		width:80px;
		height:36px;
		border-radius:20px;
		border:1px solid rgba(133,144,166,1);
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:36px;
		margin-top: 30px;
	    margin-left: 50px;
	    float: left;
	}
	.login-container-Information-sure{
		width:80px;
		height:36px;
		border-radius:20px;
		border:1px solid #FFFFFF;
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:#f6f5f5;
		line-height:36px;
		margin-top: 30px;
	    margin-right: 34px;
	    float: right;
	    background:rgba(0,119,209,1)
	}
	/*微信个人信息*/
    .login-container-Information-wechat{
		width:200px;
		height:45px;
		font-size:32px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:45px;	
		margin-top:50px;
		margin-left:32px
	}
	.login_container_Information_wechat_en{
		width: 100%;
		font-size: 22px;
		text-align: left;
	}
	.login-container-Informationt-wechat{
		display: block;
	    width: 300px;
	    height: 48px;
	    font-size: 16px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgb(155, 157, 159);
	    line-height: 24px;
	    margin-top: 16px;
	    margin-left: 40px;
	}
	.login-container-Informationh-wechat{
		display: block;
	    width: 300px;
	    height: 48px;
	    font-size: 16px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgb(155, 157, 159);
	    line-height: 24px;
	    margin-top: 16px;
	    margin-left: 40px;
	}
	.login-container-Informationf-wechat{
		display: block;
	    width: 300px;
	    height: 48px;
	    font-size: 16px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgb(155, 157, 159);
	    line-height: 24px;
	    margin-top: 16px;
	    margin-left: 40px;
	}
	.login-container-Information-tooltip-wechat{
		position:absolute;
	    width: 300px;
	    height: 15px;
	    color:red;
	    line-height: 15px;
	    margin-top: 5px;
	    margin-left: 40px;
	    text-align:center;

	}
	.login-container-Informationo-wechat{
		display: block;
	    width: 300px;
	    height: 48px;
	    font-size: 16px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgb(155, 157, 159);
	    line-height: 24px;
	    margin-top: 50px;
	    margin-left: 40px;
	}
	.login-container-Information-back-wechat{
		width:80px;
		height:36px;
		border-radius:20px;
		border:1px solid rgba(133,144,166,1);
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:36px;
		margin-top: 30px;
	    margin-left: 50px;
	    float: left;
	}
	.login-container-Information-sure-wechat{
		width:80px;
		height:36px;
		border-radius:20px;
		border:1px solid #FFFFFF;
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:#f6f5f5;
		line-height:36px;
		margin-top: 30px;
	    margin-right: 34px;
	    float: right;
	    background:rgba(0,119,209,1)
	}
	/*忘记密码*/
	.login-container-forgetlog{
		width:48px;
		height:48px;
		margin-top:48px;
		margin-left:31px;
	}
	.login-container-forgetext{
		width:288px;
		height:45px;
		font-size:32px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:45px;
		margin-top:30px;
		margin-left:32px;
		text-align: left;
	}
	.login_container_forgetext_en{
		width: 100%;
	}
	.login-container-forgetInfor{
		display: block;
	    width: 300px;
	    height: 48px;
	    font-size: 16px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgb(155, 157, 159);
	    line-height: 24px;
	    margin-top: 30px;
	    margin-left: 40px;
	}
	.login-container-forgetInfortxt{
		width: 300px;
	    height: 20px;
	    color:red;
	    margin-top: 5px;
	    margin-left: 40px;
	}
	.login-container-forgetsure{
		display: block;
		width:100px;
		height:40px;
		position: absolute;
		background:rgba(3,111,190,1);
		border-radius:25px;
		font-size:16px;
		/*font-family:PingFangSC-Light;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(255,255,255,1);
		line-height:22px;	
		top: 300px;
	    margin-left: 40px;
	    cursor: pointer;	
	}
	.login-container-forgetback{
		display: block;
		position: absolute;
		width:100px;
		height:40px;
		border:1px solid rgba(133,144,166,1);
		border-radius:25px;
		font-size:16px;
		/*font-family:PingFangSC-Light;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:22px;	
		top: 380px;
	    margin-left: 40px;
	    cursor: pointer;
	}
	/*密码身份认证*/
	.login-container-pw{
		width:48px;
		height:48px;
		margin-top:48px;
		margin-left:31px;	
	}
	.login-container-pwtext{
		width:128px;
		height:45px;
		font-size:32px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:45px;
		margin-top: 30px;
	    margin-left: 40px;
	}
	.login-container-pwInfor{
		width:228px;
		height:28px;
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:28px;
		margin-top: 20px;
	    margin-left: 40px;
	}
	.login-container-pb{
		width:228px;
		height:28px;
		line-height: 28px;
		margin-top: 15px;
	    margin-left: 40px;
	    color:rgba(3,111,190,1) 
	}
	.login-container-pwtsure{
		display: block;
		width:100px;
		height:40px;
		background:rgba(3,111,190,1);
		border-radius:25px;
		font-size:16px;
		/*font-family:PingFangSC-Light;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(255,255,255,1);
		line-height:22px;
		margin-top: 40px;
	    margin-left: 40px;
	}
	.login-container-pwback{
		display: block;
		width:100px;
		height:40px;
		border-radius:25px;
		border:1px solid rgba(133,144,166,1);
		font-size:16px;
		/*font-family:PingFangSC-Light;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:22px;
		margin-top: 24px;
	    margin-left: 40px;
	}
    .login-container-welcometxt{
	    width: 301px;
	    height: 45px;
	    font-size: 24px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgba(133,144,166,1);
	    margin-top: 30px;
	    line-height: 45px;
	}
	.login_container_welcometxt_en{
		width: 100%;
	}
	.login-container-gisrion{
	    width: 196px;
	    height: 28px;
	    font-size: 14px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgba(133,144,166,1);
	    line-height: 28px;
	    margin-top: 20px;
	    margin-left: 15px;
	}
	.login_container_gisrion_en{
		width: 100%;
		text-indent: 28px;
    	text-align: left
	}
	.login-container-cheek{
		width: 228px;
		height: 28px;
		font-size: 14px;
		/*font-family: PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgba(133,144,166,1);
	    line-height: 28px;
		margin-top: 15px;
   		margin-left: 37px;
	}
	.login-container-emailnum{
		padding: 0 10px;
		height:28px;
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(0,119,209,1);
		line-height:28px;
		margin-top: 15px;
   		margin-left: 37px;
   		text-align: left;
	}
	.login-container-btuboom{
		    background: rgba(3,111,190,1);
		    -webkit-border-radius: 25px;
		    border-radius: 25px;
		    font-size: 16px;
		    /*font-family: PingFangSC-Light;*/
		    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		    color: rgba(255,255,255,1);
		    line-height: 22px;
		    width: 100px;
		    height: 50px;
		    margin-top: 40px;
		    float: left;
		    margin-left: 30px;
	}
	.login-container-btugoback{
			width: 95px;
    		height: 48px;
		    -webkit-border-radius: 25px;
		    border-radius: 25px;
		    border: 1px solid rgba(133,144,166,1);
		    font-size: 16px;
		    /*font-family: PingFangSC-Light;*/
		    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		    color: rgba(133,144,166,1);
		    line-height: 22px;
		    position: absolute;
		    top: 430px;
		    left: 30px;
	}
	/*微信注册验证*/
    .login-container-welcometxtw{
	    height: 45px;
	    font-size: 24px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgba(133,144,166,1);
	    margin-top: 30px;
	    line-height: 45px;
	    height: 45px;
	    text-align: left;
	    margin-left: 38px;
	}
	.login-container-gisrionw{
	    width: 196px;
	    height: 28px;
	    font-size: 14px;
	    /*font-family: PingFangSC-Regular;*/
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgba(133,144,166,1);
	    line-height: 28px;
	    margin-top: 20px;
	    margin-left: 40px;
	}
	.login_container_gisrionw_en{
		width:280px;
	}
	.login-container-cheekw{
		width: 228px;
		height: 28px;
		font-size: 14px;
		/*font-family: PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgba(133,144,166,1);
	    line-height: 28px;
		margin-top: 15px;
   		margin-left: 37px;
	}
	.login_container_cheekw_en{
		width:85%;
	}
	.login-container-emailnumw{
		width:116px;
		height:28px;
		font-size:14px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(0,119,209,1);
		line-height:28px;
		margin-top: 15px;
   		margin-left: 37px;
	}
	.login-container-btuboomw{
		    background: rgba(3,111,190,1);
		    -webkit-border-radius: 25px;
		    border-radius: 25px;
		    font-size: 16px;
		    /*font-family: PingFangSC-Light;*/
		    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		    color: rgba(255,255,255,1);
		    line-height: 22px;
		    width: 100px;
		    height: 50px;
		    margin-top: 40px;
		    float: left;
		    margin-left: 30px;
	}
	.login-container-btugobackw{
			width: 95px;
    		height: 48px;
		    -webkit-border-radius: 25px;
		    border-radius: 25px;
		    border: 1px solid rgba(133,144,166,1);
		    font-size: 16px;
		    /*font-family: PingFangSC-Light;*/
		    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		    color: rgba(133,144,166,1);
		    line-height: 22px;
		    position: absolute;
		    top: 430px;
		    left: 30px;
	}
	.login-container-changpw{
		width:128px;
		height:45px;
		font-size:32px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:45px;
		margin-top: 30px;
		margin-left: 34px;
	}
	.login_container_changpw_en{
		width:85%;
	}
	.login-container-changeiniput{
		width:300px;
		height:48px;
		font-size:16px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:24px;
		margin-top: 35px;
		margin-left: 34px;
	}
	.login-comtainer-changeagain{
		width:300px;
		height:48px;
		font-size:16px;
		/*font-family:PingFangSC-Regular;*/
		font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
		color:rgba(133,144,166,1);
		line-height:24px;
		margin-top: 22px;
		margin-left: 34px;
	}
	.login-container-changetxt{
		height:15px;
		line-height:15px;
	    color:red;
	    margin-top:10px;
	    text-align: center;
	}
	.login-container-surepw{
	    display: block;
	    width: 100px;
	    height: 40px;
	    position: absolute;
	    background: rgba(3,111,190,1);
	    -webkit-border-radius: 25px;
	    border-radius: 25px;
	    font-size: 16px;
	    /* font-family: PingFangSC-Light; */
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color: rgba(255,255,255,1);
	    line-height: 22px;
	    top: 378px;
	    margin-left: 40px;
	    cursor: pointer;
	}
	.login-container-callpw{
 		display: block;
	    width: 100px;
	    height: 40px;
	    position: absolute;
	    border:1px solid rgba(133,144,166,1);
	    -webkit-border-radius: 25px;
	    border-radius: 25px;
	    font-size: 16px;
	    /* font-family: PingFangSC-Light; */
	    font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	    color:rgba(133,144,166,1);
	    line-height: 22px;
	    top: 440px;
	    margin-left: 40px;
	    cursor: pointer;
	}
	/*产品列表*/
	.production-list-title{
		margin: 10px auto;
	    line-height: 1;
	    font-size: 30px;
	    color: #3E3E3E;
	    text-align: center;
	    font-weight: 100;
        font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
	}
	.introduction-product-list{
		padding-top:60px;
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
		/*width: 235px;*/
		width: 295px;
		height: 140px;
	}
	.introduction-products-item .abcdata-icon{
		position: absolute;
		width: 185px;
		height: 85px;
		left: 50%;
		top: 50%;
		margin-left: -83px;
		margin-top: -40px;
		border: 1px solid #DFDFDF;
		border-radius:5px;
	}
	.introduction-products-item .abcdata-icon.tencent{
		background:url(/images/tencent_01.png) no-repeat;
		background-position:7px 15px;
	}
	.introduction-products-item .abcdata-icon.htht{
		background:url(/images/logo-htht.png) no-repeat;
		background-position:7px 10px;
	}
	.introduction-products-item .abcdata-icon.momo{
		background:url(/images/momo_01.png) no-repeat;
		background-position:8px 15px;
	}
	.introduction-products-item .abcdata-icon.east-money{
		background:url(/images/logo_east_money.png) no-repeat;
		background-position:8px 15px;
	}
	.introduction-products-item .abcdata-icon.xdf{
		background:url(/images/logo-xdf.png) no-repeat;
		background-position:8px 15px;
	}
	.introduction-products-item .abcdata-icon.rcl{
		background:url(/images/logo_rcl.png) no-repeat;
		background-position:8px 20px;
	}
	.introduction-products-item .abcdata-icon.tal{
		background:url(/images/logo_tal_01.png) no-repeat;
		background-position:8px 15px;
	}
	.introduction-products-item .abcdata-icon.club{
		background:url(/images/logo_lending_club.png) no-repeat;
		background-position:8px 15px;
	}
	.introduction-products-item .abcdata-icon.ccl{
		background:url(/images/logo_ccl.png) no-repeat;
		background-position:8px 15px;
	}
	.introduction-products-item .abcdata-icon.tuhuashun{
		background:url(/images/logo_tonghuashun.png) no-repeat;
		background-position:8px 15px;
	}
	.introduction-products-item .abcdata-icon.vip{
		background:url(/images/vip_01.png) no-repeat;
		background-position:8px 15px;
	}
	.introduction-products-item .abcdata-icon.cheetah{
		background:url(/images/cheetah_01.png) no-repeat;
		background-position:8px 15px;
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
		margin: 10px auto;
	    line-height: 1;
	    font-size: 30px;
	    text-align: center;
	    font-weight: 100;
        font-family: PingFangSC-Thin,"PingFang SC", -apple-system, Roboto, "Microsoft YaHei";
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
		width:100%;
		height:100px;
		font-size: 12px;
		line-height: 100px;
		border-top: 1px solid #eee;
		background: #333333;
		color: #FFFFFF;
		position: absolute;
		left:0;
		bottom:0;
	}
	/*重写userbar*/
	.introduction-nav-bar .userBar{
		margin-top: 45px;
	}
	/*3d轮播*/
	.introduction-price #box{
		width: 481px;
		height: 500px;
		margin: 20px auto;
	}
	.introduction-price #box .img{
		width: 451px;
		height: 540px;
	}
	.introduction-price #box ._img{
		position: absolute;
		left: 100px;
		bottom:60px;
		width:276px;
		height:39px;
	}
	.introduction-price #box .dashboard_img{
		position: absolute;
		left:230px;
		top:60px;
		/*height:80px;*/
		width:230px;
	}
	.introduction-price #box h3{
		position: absolute;
		top:160px;
		left: 290px;
		width: 120px;
		height: 33px;
		line-height: 33px;
		font-family: PingFangSC-Regular;
		font-size: 24px;
		color: #333333;
		text-align: left;
	}
	.introduction-price #box h4{
		position: absolute;
		top:220px;
		left: 290px;
		width: 120px;
		font-family: PingFangSC-Regular;
		font-size: 40px;
		font-weight: bold;
		color: #00518E;
		letter-spacing: 0;
		text-align:center;
	}	
	.introduction-price #box .div{
		width: 350px;
		height: 250px;
		position: absolute;
		top: 270px;
		left:180px;
		margin:0 auto;
	}
	.introduction-price #box .div p{
		width: 100%;
		height:40px;
		line-height: 40px;
		margin-top:15px;
	}
	.introduction-price #box .div p .left-span{
		display: block;
		float: left;
		font-family: PingFangSC-Regular;
		font-size: 18px;
		color: #777777;
	}
	.introduction-price #box .div p .right-span{
		display: block;
		float: right;
		font-family: PingFangSC-Medium;
		font-size:20px;
		color: #333333;
		font-weight: bold;
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
		margin:0 auto;
		margin-top: 40px !important;
	}
	.language-tab .btn-lang:first-child{
		border-right: 0.5px solid #7F899C;
	}
	.language-tab .btn-lang:last-child{
		border-left: 0.5px solid #FFFFFF ;
	}
	.language-tab .btn-lang{
		width: 69px;
		height: 30px;
		line-height:30px;
		text-align:center;
		float: left;
		cursor: pointer;
		font-family: PingFangSC-Semibold;
		font-size: 16px;
		color: #7F899C;
	}
	.btn_active{
		color:#00A1E9 !important;
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
	/*introduction PC页面修改*/
    .introduction-description-container,.introduction-contact-us-container,.introduction-product-list-container{
    	margin:80px auto;
    }
    .contact-us-title.section-title{
    	margin-top: -100px;
    	margin-bottom:150px;
	    line-height: 1;
	    font-size: 30px;
	    color: #3E3E3E;
	    text-align: center;
    }
    .contact-methods-wrap{
    	width: 1180px;
    	margin: 0 auto;
    }
</style>
