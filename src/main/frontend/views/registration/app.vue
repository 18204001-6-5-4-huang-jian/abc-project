<template>
	<div class="login-wrap">
		<div class="login-head">
			<div class="logo pointer" @click="direct('introduction')"></div>
		</div>
		<!-- 登录 -->
		<div class="login-container" v-if="loginStatus==0">
			<div class="login-welcome">WELCOME</div>
		<div class="switch_bar" style="font-size: 14px;">
			<div class="switch_btn" @click="loginway=0" :class="{active:loginway==0}" v-text="lang=='zh_CN'?'微信注册':'Wechat Register'"></div>
			<div class="switch_btn" @click="loginway=1" :class="{active:loginway==1}" v-text="lang=='zh_CN'?'邮箱注册':'Email Register'"></div>
			<span class="btn_underline" :class="{atLeft:loginway==0,atRight:loginway==1}"></span>
		</div>
		<!--二维码注册-->
		<div id="login_container" class="loginway wechart" v-show="loginway==0">
			<div id="login_container_really"></div>
			<span class="wxlogintext"><i v-text="lang=='zh_CN'?'微信扫一扫即可注册，':'Scan here to register,'"></i><i class="i" @click="loginway=1" v-text="lang=='zh_CN'?'海外版微信用户建议使用邮箱注册':' Email registration is HIGHLY RECOMMENDED for users overseas.'"></i></span>
			<span class="wxloginErrortext" @click="openNewPageWx" v-text="lang=='zh_CN'?'二维码异常？':'Having troubles with QR code?'"></span>
		</div>
		<!--邮箱注册-->
		<div class="login-form" v-show="loginway==1">
				<div class="login-form-control">
					<input type="text" :placeholder="lang=='zh_CN'?'您的姓名':'Your Full Name'" class="login-full-name" @keyup.enter="register()"/>
				</div>
				<div class="login-form-control">
					<input type="text" :placeholder="lang=='zh_CN'?'机构名称':'Institution Name'" class="login-institution-name" @keyup.enter="register()"/>
				</div>
				<div class="login-form-control">
					<input type="text" :placeholder="lang=='zh_CN'?'公司邮箱':'Business Email'" class="login-business-email" @keyup.enter="register()"/>
				</div>
				<div class="login-form-control login-form-control-huang">
					<input type="password" :placeholder="lang=='zh_CN'?'设置密码':'Password'" class="login-password" @keyup.enter="register()"/>
				</div>
				<div class="login-form-control wrong-tip">
					<transition name="component-fade">
						<span v-if="wrongTip" v-text="wrongTip"></span>
					</transition>
				</div>
			</div>
			<button class="btn-login" @click="register()" v-show="loginway == 1">
				<span v-show="loadState === 0"  v-text="lang=='zh_CN'?'注册':'Create an Account'">
				</span>
				<span class="Logging" v-show="loadState === 1">
					<i class="loader"></i>
					Registering...
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
  name: "login",
  data() {
    return {
      loginStatus: 0, //0登录 1忘记密码
      wrongTip: "",
      loadState: 0, //0完成 1传输中
      tipTimer: null,
      loginway: 0
    };
  },
  directives: {
    focus: {
      inserted(el) {
        el.focus();
      }
    }
  },
  mounted() {
    GaHelper.sendPageView();
    // console.log(this.lang);
    if (localStorage.getItem("huang")) {
      localStorage.removeItem("huang");
      $(".login-full-name").val(localStorage.getItem("username"));
      $(".login-institution-name").val(localStorage.getItem("usercompany"));
      $(".login-business-email").val(localStorage.getItem("useremail"));
      this.loginway = 1;
    } else {
      this.loginway = 0;
    }
    var url_ = location.origin + "/view/bind/excessive.html";
    if (location.hostname == "www.eversight.ai") {
      var obj = new WxLogin({
        id: "login_container_really",
        appid: "wx4ff7c9952429c23c",
        scope: "snsapi_login",
        redirect_uri: encodeURIComponent(url_),
        state: Math.round(Math.random() * 10000000),
        style: "",
        href: "https://" + location.hostname + "/view/bind/css/bind.css"
      });
    } else if (location.hostname == "data-dev.modeling.ai") {
      var obj = new WxLogin({
        id: "login_container_really",
        appid: "wx6e4329dacb96437c",
        scope: "snsapi_login",
        redirect_uri: encodeURIComponent(url_),
        state: Math.round(Math.random() * 10000000),
        style: "",
        href: "https://" + location.hostname + "/view/bind/css/bind.css"
      });
    }
  },
  computed: {
    lang() {
      return this.$store.state.introduction.lang;
    }
  },
  methods: {
    direct(target) {
      this.$router.push({
        name: target
      });
    },
    //验证
    verify() {
      if (
        !$(".login-full-name")
          .val()
          .trim()
      ) {
        this.wrongTip = "Your Full Name can't be empty";
        return false;
      }
      //              if(!/^[^\s][A-Za-z0-9_\-\u4e00-\u9fa5\s]+[^\s]$/.test($(".login-full-name").val())){
      //              	this.wrongTip = 'Your Full Name is illegal';
      //					return false;
      //              }
      if (
        !/^[^\s][A-Za-z0-9_\-\u4e00-\u9fa5\s]*[^\s]*$/.test(
          $(".login-full-name").val()
        )
      ) {
        this.wrongTip = "Your Full Name is illegal";
        return false;
      }
      if (
        !$(".login-institution-name")
          .val()
          .trim()
      ) {
        this.wrongTip = "Your Institution Name can't be empty";
        return false;
      }
      //				if(!/^[^\s][A-Za-z0-9_\-\u4e00-\u9fa5\s]+[^\s]$/.test($(".login-institution-name").val())){
      //              	this.wrongTip = 'Your Institution Name is illegal';
      //					return false;
      //              }
      if (
        !/^[^\s][A-Za-z0-9_\-\u4e00-\u9fa5\s]*[^\s]*$/.test(
          $(".login-institution-name").val()
        )
      ) {
        this.wrongTip = "Your Institution Name is illegal";
        return false;
      }
      if (
        !$(".login-business-email")
          .val()
          .trim()
      ) {
        this.wrongTip = "Your Business Email can't be empty";
        return false;
      }
      //				if(!/^([0-9A-Za-z\-_\.]+)@([0-9a-zA-Z\-]+\.[a-z]{2,3}(\.[a-z]{2})?)$/g.test($(".login-business-email").val())){
      //					this.wrongTip = 'illegal email';
      //					return false;
      //				}
      if (
        $(".login-business-email")
          .val()
          .trim()
          .indexOf("@") >= 0
      ) {
      } else {
        this.wrongTip = "illegal email";
        return false;
      }
      if (
        !$(".login-password")
          .val()
          .trim()
      ) {
        this.wrongTip = "Your password can't be empty";
        return false;
      }
      if (
        $(".login-password")
          .val()
          .trim().length < 6
      ) {
        this.wrongTip = "Password can't be shorter than six";
        return false;
      }
      return true;
    },
    //只是邮箱注册
    register() {
      if (!this.verify()) {
        return false;
      }
      localStorage.setItem("huangjian", false);
      localStorage.setItem("黄健", true);
      var lang = localStorage.getItem("lang");
      $.post({
        url: U.getApiPre() + "/api/v1/account/register",
        data: {
          email: $(".login-business-email").val(),
          username: $(".login-full-name").val(),
          company: $(".login-institution-name").val(),
          password: $.md5($(".login-password").val()),
          lang: lang
        },
        timeout: 1000,
        success: resp => {
          if (resp.success) {
            console.log(resp);
            localStorage.setItem("status", resp.status);
            localStorage.setItem("uid", resp.data.user.id);
            localStorage.setItem("useremail", resp.data.user.email);
            localStorage.setItem("usercompany", resp.data.company);
            localStorage.setItem("username", resp.data.user.username);
            localStorage.setItem("huang", true);
            location.href = location.origin + "/view/bind/email-bind.html";
          } else {
            this.wrongTip = resp.message || "Wrong E-mail or password";
          }
        },
        complete: () => {
          this.loadState = 0;
        }
      });
    },
    openNewPageWx() {
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
    getQueryString: function(name) {
      var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
      var r = window.location.search.substr(1).match(reg);
      if (r != null) {
        return decodeURI(r[2]);
      }
      return "";
    }
  },
  watch: {
    wrongTip(val) {
      if (this.tipTimer) {
        clearTimeout(this.tipTimer);
      }
      this.tipTimer = setTimeout(() => {
        this.wrongTip = "";
      }, 1800);
    }
  }
};
</script>
<style type="text/css" scoped>
.login-wrap {
  position: relative;
  height: 100%;
  width: 100%;
  background: url(/images/login-bg.jpg) no-repeat center;
  background-size: cover;
}
.login-head {
  padding: 20px 80px;
}
.login-head .logo {
  width: 110px;
  height: 110px;
  background: url(/images/img-icon.png) no-repeat;
  background-position: 0 0;
}
.login-container {
  color: #fff;
  width: 500px;
  min-height: 400px;
  position: absolute;
  left: 50%;
  top: 45%;
  margin-left: -200px;
  margin-top: -200px;
}
.login-welcome {
  font-family: "goodTimes";
  margin-bottom: 45px;
  text-align: center;
  font-size: 30px;
  color: #fff;
}
.login-form {
  width: 400px;
  margin: 0 auto;
}
.login-form-control {
  position: relative;
  height: 55px;
  margin-bottom: 10px;
}
.login-form-control:first-child {
  margin-top: 20px;
}
.login-form-control-huang {
  margin-bottom: 0 !important;
}
.login-form-control .login-label {
  transition: all 0.1s linear;
  position: absolute;
  color: #5a7c8e;
  top: 20px;
  left: 0;
  font-size: 16px;
  /*css穿透属性*/
  pointer-events: none;
}
.login-form-control .login-label.up {
  top: 3px;
  font-size: 14px;
}
.login-form-control input {
  height: 30px;
  line-height: 30px;
  width: 400px;
  margin: 0 auto;
  font-size: 14px;
  border-bottom: 1px solid gray;
}
.login-form-control input::-webkit-input-placeholder {
  color: #456d7f;
  font-size: 14px;
  font-weight: bold;
}
.login-input {
  width: 400px;
  height: 30px;
  margin-top: 20px;
  font-size: 16px;
  color: #92bdd2;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.login-form-control.wrong-tip {
  height: 15px;
  font-size: 12px;
  color: #fb5252;
  margin-bottom: 18px;
}
.btn-login {
  width: 400px;
  margin-left: 50px;
  background: #355867;
  font-size: 16px;
  line-height: 50px;
  border-radius: 3px;
  color: #92bdd2;
  transition: all 0.3s linear;
}
.btn-login:hover {
  background: #456d7f;
}
.Logging {
  margin-left: -30px;
}
.Logging .loader {
  font-size: 12px;
  margin-right: 30px;
  vertical-align: middle;
}
.login-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  position: absolute;
  bottom: 0;
  left: 50%;
  margin-left: -590px;
  line-height: 100px;
  color: #5a7c8e;
  font-size: 12px;
}
.login-footer .copyright {
  float: left;
}
.login-footer .contact {
  float: right;
  padding-right: 10px;
}
.login-footer .contact .contact-email {
  color: #5a7c8e;
}
.forgot_password {
  color: #5a7c8e;
  text-align: center;
  padding-top: 20px;
  user-select: none;
}
.forgot_password span {
  cursor: pointer;
}
.login {
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
.icon-login {
  width: 10px;
  height: 12px;
  display: inline-block;
  vertical-align: 0px;
  margin-right: 10px;
  background-position: 0px -445px;
}
.login:hover {
  color: #0a1e27;
  background: #fff;
}
.login:hover .icon-login {
  background-position: -11px -444px;
}
.switch_bar {
  height: 60px;
  width: 80%;
  margin: 0 auto;
  position: relative;
}
.switch_btn {
  height: 67px;
  float: left;
  width: 50%;
  cursor: pointer;
  line-height: 67px;
  color: gray;
  text-align: center;
}
.active {
  color: white;
  font-weight: bold;
}
.btn_underline {
  height: 2px;
  display: block;
  width: 100px;
  background-color: white;
  position: absolute;
  bottom: 0;
  transition: left 0.3s;
}
.atLeft {
  left: 52px;
}
.atRight {
  left: 251px;
}
.account {
  display: block;
  color: #92bdd2;
  padding-top: 15px;
  cursor: pointer;
}
.loginway {
  height: 360px;
  width: 100%;
}
.loginway form {
  width: 300px;
  margin: 0 auto;
}
.wechart {
  text-align: center;
}
.wechart p {
  margin-top: 25px;
  line-height: 25px;
  color: #666;
}
#login_container {
  overflow: hidden;
  position: relative;
}
#login_container_really {
  overflow: hidden;
  height: 100%;
}
.wxlogintext {
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
.wxlogintext i {
  font-style: normal;
}
.wxlogintext .i {
  text-decoration: underline;
  cursor: pointer;
  white-space: nowrap;
}
.wxloginErrortext {
  font-size: 12px;
  color: #666;
  position: absolute;
  left: 0;
  bottom: 30px;
  width: 100%;
  text-align: center;
  cursor: pointer;
}
.wxloginErrortext:hover {
  color: white;
}
</style>