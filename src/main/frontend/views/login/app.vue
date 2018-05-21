<template>
	<div class="login-wrap">
		<div class="login-head">
			<div class="logo pointer" @click="direct('introduction')"></div>
		</div>
		<!-- 登录 -->
		<div class="login-container" v-if="loginStatus==0">
			<div class="login-welcome">WELCOME</div>
		<div class="switch_bar" style="font-size: 14px;">
			<div class="switch_btn" @click="loginway=0" :class="{active:loginway==0}" v-text="lang=='zh_CN'?'微信登录':'Wechat Login'"></div>
			<div class="switch_btn" @click="loginway=1" :class="{active:loginway==1}" v-text="lang=='zh_CN'?'邮箱登录':'Email Login'"></div>
			<span class="btn_underline" :class="{atLeft:loginway==0,atRight:loginway==1}"></span>
		</div>
		<!--二维码登陆-->
		<div id="login_container" class="loginway wechart" v-show="loginway==0">
			<div id="login_container_really"></div>
			<span class="wxlogintext" v-text="lang=='zh_CN'?'微信扫一扫即可登录':'Scan here to login'"></span>
			<span class="wxloginErrortext" @click="openNewPageWx" v-text="lang=='zh_CN'?'二维码异常？':'Having troubles with QR code?'"></span>
		</div>
		<!--邮箱登录-->
		<div class="login-form" v-show="loginway==1">
				<div class="login-form-control">
					<label class="login-label" :class="{up:emailFill}" v-text="lang=='zh_CN'?'邮箱地址':'E-mail'"></label>
					<input type="email" @focus="onfocus('email')" @blur="onblur($event,'email')" @change="onblur($event,'email')" v-model="email" v-focus class="email-label login-input" @keyup.enter="login"/>
				</div>
				<div class="login-form-control">
					<label class="login-label" :class="{up:passFill}" v-text="lang=='zh_CN'?'密码':'Password'"></label>
					<input type="password" @focus="onfocus('pwd')" @blur="onblur($event,'pwd')" @change="onblur($event,'password')" class="input-password login-input" v-model="pwd" @keyup.enter="login"/>
				</div>
				<div class="login-form-control wrong-tip">
					<transition name="component-fade">
						<span v-if="wrongTip" v-text="wrongTip"></span>
					</transition>
				</div>
			</div>
			<button class="btn-login" @click="login" v-show="loginway == 1">
				<span v-show="loadState === 0" v-text="lang=='zh_CN'?'登录':'Login'">
				</span>
				<span class="Logging" v-show="loadState === 1">
					<i class="loader"></i>
					<i v-text="lang=='zh_CN'?'正在登录...':'Logging in...'"></i>
				</span>
			</button>
			<a class="account" v-show="loginway==1" @click="direct('registration')" v-text="lang=='zh_CN'?'没有账号？请点击这里注册':'No account?Click here to register'"></a>
			<a class="account_" v-show="loginway==1" @click="resetpassword()" v-text="lang=='zh_CN'?'忘记密码?':'Forget password?'"></a>
		</div>
		<!-- 忘记密码 -->
		<div class="login-container"  v-if="loginStatus==1">
			<div class="login" @click="loginStatus=0">
				<i class="abcdata-icon icon-login"></i>
				<span v-text="lang=='zh_CN'?'登录':'LOGIN'"></span>
			</div>
			<div class="login-welcome">RESET YOUR PASSWORD</div>
			<div class="login-form">
				<div class="login-form-control">
					<label class="login-label" :class="{up:passFill}">Password</label>
					<input type="password" @focus="onfocus('pwd')" @blur="onblur($event,'pwd')" @change="onblur($event,'password')" class="input-password login-input" v-model="pwd" @keyup.enter="login"/>
				</div>
				<div class="login-form-control wrong-tip">
					<transition name="component-fade">
						<span v-if="wrongTip" v-text="wrongTip"></span>
					</transition>
				</div>
			</div>
			<button class="btn-login" @click="confirm_forgot">
				<span v-show="loadState === 0">
					Confirm
				</span>
				<span class="Logging" v-show="loadState === 1">
					<i class="loader"></i>
					 Loading...
				</span>
			</button>
		</div>
		<div class="login-footer item-1180">
			<div class="copyright">
				Copyright &copy 2016 - 2017 Eversight.AI. All Rights Reserved. 
			</div>
			<div class="contact">
				<a class="contact-email" href="mailto:service@Eversight.ai">Email:service@Eversight.ai</a>
			</div>
		</div>
	</div>
</template>
<script type="text/javascript">
	export default {
		name: 'login',
		data(){
			return{
				loginStatus: 0,//0登录 1忘记密码
				emailFill: false,
				passFill: false,
				wrongTip: '',
				pwd: '',
				email: '',
				loadState: 0, //0完成 1传输中
				tipTimer: null,
				loginway:0
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
              //GA
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
		},
		computed:{
			lang(){
	        	return this.$store.state.introduction.lang;
	        }
		},
		methods:{
			direct(target){
				this.$router.push({
					name: target
				})
			},
			onfocus(type){
				if(type === 'email'){
					this.emailFill = true;
				}else{
					this.passFill = true;
				}
			},
			onblur(ev,type){
				if(ev.target.value.trim()){
					if(type === 'email'){
						this.emailFill = true;
					}else{
						this.passFill = true;
					}
				}else{
					if(type === 'email'){
						this.emailFill = false;
					}else{
						this.passFill = false;
					}
				}
			},
			//验证
			verify(){
				if(!this.email.trim()){
					this.wrongTip = 'E-mail can\'t be empty';
					return false;
				}
//				if(!/^([0-9A-Za-z\-_\.]+)@([0-9a-zA-Z\-]+\.[a-z]{2,3}(\.[a-z]{2})?)$/g.test(this.email)){
//					this.wrongTip = 'illegal email';
//					return false;
//				}
                if(this.email.trim().indexOf("@")>=0){
                	
                }else{
                	this.wrongTip = 'illegal email';
					return false;
                }
				if(!this.pwd.trim()){
					this.wrongTip = 'Password can\'t be empty';
					return false;
				}
				if(this.pwd.length < 6){
					this.wrongTip = 'Password can\'t be shorter than six';
					return false;
				}
				return true;
			},
			login(){
				var lang = localStorage.getItem("lang")
				window.localStorage.clear();
				window.sessionStorage.clear();
				localStorage.setItem("lang",lang);
				if(lang == null){
					localStorage.setItem("lang","zh_CN");
				}
				if(!this.verify()){
					return false;
				}
				localStorage.setItem("huangjian",true);
				this.loadState = 1;
				$.post({
					url: U.getApiPre() + '/api/v1/account/login',
					data:{
						email: this.email,
						password: $.md5(this.pwd),
						lang:lang
					},
					timeout:5000,
					success:(resp)=>{
						if(resp.success){
							console.log(resp);
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
							//用于growingIO
							localStorage.setItem("username",resp.data.user.username);
							localStorage.setItem("usercompany",resp.data.company);
							localStorage.setItem("plan_name",resp.data.plan_name);
							localStorage.setItem("plan_name_zh_CN",resp.data.plan_name_zh_CN);
							this.savePass(resp.data.user.id,resp.data.user.username,resp.data.user.head_img);
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
								this.saveCompany(resp.data.company.id, resp.data.company.role);
							}    
								 this.direct('my');
							}else if(resp.status == 1){
								//微信注册成功之后首次邮箱登陆
								localStorage.setItem("status",resp.status);
							    localStorage.setItem("uid",resp.data.user.id);
								localStorage.setItem("useremail",resp.data.user.email);
								localStorage.setItem("usercompany",resp.data.company);
								localStorage.setItem("username",resp.data.user.username);
								location.href = location.origin + "/view/bind/email-bind.html";
							 }
						}else{
							this.wrongTip = 'Wrong E-mail or password'
						}
					},
					error:()=>{
						this.wrongTip = "Network anomaly";
					},
					complete:()=>{
						this.loadState = 0;
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
			forgotPsd(){
				this.pwd = "";
				this.loginStatus = 1;
			},
			confirm_forgot(){
				if(!this.pwd.trim()){
					this.wrongTip = 'Password can\'t be empty';
					return false;
				}
				if(this.pwd.length < 6){
					this.wrongTip = 'Password can\'t be shorter than six';
					return false;
				}
			},
			openNewPageWx(){
				var self = this;
				var _url = location.origin + "/view/bind/excessive.html";
				var appid = 'wx4ff7c9952429c23c';
				if(location.hostname == "data-dev.modeling.ai"){
					appid = "wx6e4329dacb96437c";
				}
				window.location.href = "https://open.weixin.qq.com/connect/qrconnect?appid="+ appid +
				"&redirect_uri="+ encodeURIComponent(_url) +"&response_type=code&scope=snsapi_login&state="+ 
				Math.round(Math.random()*10000000) +"#wechat_redirect";
			},
			// 获取URL的参数
			getQueryString: function(name) {
			    var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)', 'i');
			    var r = window.location.search.substr(1).match(reg);
			    if (r != null) {
			      return decodeURI(r[2]);
			    }
			    return '';
			},
			resetpassword(){
				location.href = location.origin + "/view/bind/resetpassword.html"
			}
		},
		watch:{
			wrongTip(val){
				if(this.tipTimer){
					clearTimeout(this.tipTimer);
				}
				this.tipTimer = setTimeout(()=>{
					this.wrongTip = ''
				},1800);
			}
		}
	}
</script>
<style type="text/css" scoped>
	.login-wrap{
		position: relative;
		height: 100%;
		width: 100%;
		background: url(/images/login-bg.jpg) no-repeat center;
		background-size: cover;
	}
	.login-head{
		padding: 20px 80px;
	}
	.login-head .logo{
		width: 110px;
		height: 110px;
		background: url(/images/img-icon.png) no-repeat;
		background-position: 0 0;
	}
	.login-container{
		color: #fff;
		width: 400px;
		min-height: 400px;
		position: absolute;
		left: 50%;
		top: 45%;
		margin-left: -200px;
		margin-top: -200px;
	}
	.login-welcome{
		font-family: 'goodTimes';
		margin-bottom: 45px;
		text-align: center;
		font-size: 30px;
		color: #fff;
	}
	.login-form-control{
		position: relative;
		height: 55px;
		margin-bottom: 20px;
	}
	.login-form-control .login-label{
		transition: all .1s linear;
		position: absolute;
		color: #5a7c8e;
		top: 20px;
		left:0;
		font-size: 16px;
		/*css穿透属性*/
		pointer-events: none;
	}
	.login-form-control .login-label.up{
		top: 3px;
		font-size: 14px;
	}
	.login-input{
		width: 400px;
		height: 30px;
		margin-top: 20px;
		font-size: 16px;
		color: #92bdd2;
		border-bottom: 1px solid rgba(255,255,255,.06);
	}
	.login-form-control.wrong-tip{
		height: 35px;
		font-size: 12px;
		color: #fb5252;
		margin-bottom: 0;
	}
	.btn-login{
		width: 400px;
		background: #355867;
		font-size: 16px;
		line-height: 50px;
		border-radius: 3px;
		color: #92bdd2;
		transition: all .3s linear;
	}
	.btn-login:hover{
		background: #456D7F;
	}
	.Logging{
		margin-left: -30px;
	}
	.Logging .loader{
		font-size: 12px;
		margin-right: 30px;
		vertical-align: middle;
	}
	.btn-login i{
		font-style: normal;
	}
	.login-footer{
		border-top: 1px solid rgba(255,255,255,.06);
		position: absolute;
		bottom: 0;
		left: 50%;
		margin-left: -590px;
		line-height: 100px;
		color: #5a7c8e;
		font-size: 12px;
	}
	.login-footer .copyright{
		float: left;
	}
	.login-footer .contact{
		float: right;
		padding-right: 10px;
	}
	.login-footer .contact .contact-email{
		color: #5a7c8e;
	}
	.forgot_password{
		color: #5a7c8e;
		text-align: center;
		padding-top: 20px;
		user-select: none;
	}
	.forgot_password span{
		cursor: pointer;
	}
	.login{
		position: fixed;
		top: 35px;
		right: 80px;
		width: 110px;
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
	.switch_bar{
		height: 60px;
		width: 100%;
		position: relative;
		}
	.switch_btn{
		height: 67px;
		float: left;
		width: 50%;
		cursor: pointer;
		line-height: 67px;
		color:gray;
		text-align: center;
	}
	.active{
		color:white;
		font-weight: bold;
	}
	.btn_underline{
		height: 2px;
		display: block;
		width: 90px;
		background-color:white;
		position: absolute;
		bottom: 0;
		transition: left 0.3s;
	}
	.atLeft{
		left: 57px;
	}
	.atRight{
		left: 257px;
	}
	.account{
		display: block;
		color:#92bdd2;
		padding-top: 15px;
		cursor: pointer;
		font-size: 14px;
		float: left;
	}
	.account_{
		display: block;
		color:#92bdd2;
		padding-top: 15px;
		cursor: pointer;
		font-size: 14px;
		float: right;
	}
	.loginway{
		height: 360px;
		width: 100%;
		}
	.loginway form{
		width: 300px;
		margin: 0 auto;
	}
	.wechart{
		text-align: center;
		}
	.wechart p{
			margin-top: 25px;
			line-height: 25px;
			color: #666;
		}
	#login_container{
		overflow: hidden;
		position: relative;
	}
	#login_container_really{
		overflow: hidden;
		height: 100%;
	}
	.wxlogintext{
    	font-size: 12px;
    	color: #999;
    	position: absolute;
    	left: 0;
    	bottom: 95px;
    	width: 100%;
    	height: 15px;
    	line-height: 15px;
    	text-align: center;
    }
    .wxloginErrortext{
    	font-size: 12px;
    	color: #666;
    	position: absolute;
    	left: 0;
    	bottom: 30px;
    	width: 100%;
    	text-align: center;
    	cursor: pointer;
    }
    .wxloginErrortext:hover{
    	color:white;
    }
</style>