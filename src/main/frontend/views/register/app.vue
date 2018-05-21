<template>
	<div class="register-wrap">
		<div class="register-head">
			<div class="logo pointer" @click="direct('introduction')"></div>
		</div>
		<!-- 登录 -->
		<div class="register-container">
			<div class="register-welcome">REGISTERED</div>
			<div class="register-form">
				<div class="register-form-control">
					<label class="register-label up">E-mail</label>
					<input type="email" :value="email" class="email-label register-input" disabled />
				</div>
				<div class="register-form-control">
					<label class="register-label" :class="{up:nameFill}">Name</label>
					<input type="text" @focus="onfocus('name')" @blur="onblur($event,'name')" @change="onblur($event,'name')" v-model="name" v-focus class="name-label register-input" @keyup.enter="register"/>
				</div>
				<div class="register-form-control">
					<label class="register-label" :class="{up:passFill}">Password</label>
					<input type="password" @focus="onfocus('pwd')" @blur="onblur($event,'pwd')" @change="onblur($event,'password')" class="input-password register-input" v-model="pwd" @keyup.enter="register"/>
				</div>
				<div class="register-form-control wrong-tip">
					<transition name="component-fade">
						<span v-if="wrongTip" v-text="wrongTip"></span>
					</transition>
				</div>
			</div>
			<button class="btn-register" @click="register">
				<span v-show="loadState === 0">
					Registered
				</span>
				<span class="Logging" v-show="loadState === 1">
					<i class="loader"></i>
					Registering...
				</span>
			</button>
		</div>
		<div class="register-footer item-1180">
			<div class="copyright">
				Copyright &copy 2016 - 2017 Eversight.AI All Rights Reserved. 
			</div>
			<div class="contact">
				<a class="contact-email" href="mailto:service@modeling.ai">Email:service@Eversight.ai</a>
			</div>
		</div>
	</div>
</template>
<script type="text/javascript">
	export default {
		name: 'register',
		data(){
			return{
				nameFill: false,
				passFill: false,
				wrongTip: '',
				pwd: '',
				email: '',
				name: '',
				loadState: 0, //0完成 1传输中
				tipTimer: null
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
			var email = this.$route.params.email;
			if(email){
				this.email = email;
			}else{
				this.direct('introduction');
			}
		},
		methods:{
			direct(target){
				this.$router.push({
					name: target
				})
			},
			onfocus(type){
				if(type === 'name'){
					this.nameFill = true;
				}else{
					this.passFill = true;
				}
			},
			onblur(ev,type){
				if(ev.target.value.trim()){
					if(type === 'name'){
						this.nameFill = true;
					}else{
						this.passFill = true;
					}
				}else{
					if(type === 'name'){
						this.nameFill = false;
					}else{
						this.passFill = false;
					}
				}
			},
			verify(){
				if(!/^([0-9A-Za-z\-_\.]+)@([0-9a-z]+\.[a-z]{2,3}(\.[a-z]{2})?)$/g.test(this.email)){
					this.wrongTip = 'Bad format email';
					return false;
				}
				if(!this.name.trim()){
					this.wrongTip = 'Name can\'t be empty';
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
			register(){
				if(!this.verify()){
					return false;
				}
				this.loadState = 1;
				$.post({
					url: U.getApiPre() + '/api/v1/account/register',
					data:{
						email: this.email,
						username: this.name,
						password: $.md5(this.pwd)
					},
					timeout: 5000,
					success:(resp)=>{
						if(resp.success){
							var day = new Date(resp.data.token.expiry.slice(0,-4)).getTime()-new Date();
							$.cookie("token",resp.data.token.auth_token,{
								expires:parseInt(day/1000/3600/24),
								path:'/'
							});
							window.sessionStorage.clear();
							this.savePass(resp.data.user.id,resp.data.user.username);
							if (typeof resp.data.company != 'undefined') {
								this.saveCompany(resp.data.company.id, resp.data.company.role);
							}
							this.direct('my');
						}else{
							this.wrongTip = resp.message || 'Wrong E-mail or password'
						}
					},
					complete:()=>{
						this.loadState = 0;
					}
				})
			},
			savePass:function(uid,name){
				if(!window.sessionStorage){return false;}
				sessionStorage.setItem('email',this.email);
				if(uid && name){
					sessionStorage.setItem('uid',uid);
					sessionStorage.setItem('username',name);
				}
			},
			saveCompany:function (cid,role) {
				if(!window.sessionStorage){return false;}
				if(cid && role){
					sessionStorage.setItem('cid',cid);
					sessionStorage.setItem('role',role);
				}
			},
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
	.register-wrap{
		position: absolute;
		left: 0;
		bottom: 0;
		right: 0;
		top: 0;
		background: url(/images/login-bg.jpg) no-repeat center;
		background-size: cover;
	}
	.register-head{
		padding: 20px 80px;
	}
	.register-head .logo{
		width: 110px;
		height: 110px;
		background: url(/images/img-icon.png) no-repeat;
		background-position: 0 0;
	}
	.register-container{
		color: #fff;
		width: 400px;
		min-height: 400px;
		position: absolute;
		left: 50%;
		top: 45%;
		margin-left: -200px;
		margin-top: -200px;
	}
	.register-welcome{
		font-family: 'goodTimes';
		margin-bottom: 45px;
		text-align: center;
		font-size: 30px;
		color: #fff;
	}
	.register-form-control{
		position: relative;
		height: 55px;
		margin-bottom: 20px;
	}
	.register-form-control .register-label{
		transition: all .1s linear;
		position: absolute;
		color: #5a7c8e;
		top: 20px;
		font-size: 16px;
	}
	.register-form-control .register-label.up{
		top: 3px;
		font-size: 14px;
	}
	.register-input{
		width: 400px;
		height: 30px;
		margin-top: 20px;
		font-size: 16px;
		color: #92bdd2;
		border-bottom: 1px solid rgba(255,255,255,.06);
	}
	.email-label{
		color: #5a7c8e;
	}
	.register-form-control.wrong-tip{
		height: 35px;
		font-size: 12px;
		color: #fb5252;
		margin-bottom: 0;
	}
	.btn-register{
		width: 400px;
		background: #355867;
		font-size: 16px;
		line-height: 50px;
		border-radius: 3px;
		color: #92bdd2;
		transition: all .3s linear;
	}
	.btn-register:hover{
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
	.register-footer{
		border-top: 1px solid rgba(255,255,255,.06);
		position: absolute;
		bottom: 0;
		left: 50%;
		margin-left: -590px;
		line-height: 100px;
		color: #5a7c8e;
		font-size: 12px;
	}
	.register-footer .copyright{
		float: left;
	}
	.register-footer .contact{
		float: right;
		padding-right: 10px;
	}
	.register-footer .contact .contact-email{
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
	.register{
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
	.icon-register{
		width: 10px;
		height: 12px;
		display: inline-block;
		vertical-align: 0px;
		margin-right: 10px;
		background-position: 0px -445px;
	}
	.register:hover{
		color: #0a1e27;
		background: #fff;
	}
	.register:hover .icon-register{
		background-position: -11px -444px;
	}
</style>