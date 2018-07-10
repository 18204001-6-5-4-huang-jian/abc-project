<template>
    <div class="userBar" @click.stop="showUserBar()" v-if="isLogin">
    	 <img  class="headimg" v-show="isheadimg" :src="headimgpath"/>
        {{username}}
        <ul v-show="isShowUserBar" class="user-bar-dropdown">
            <div class="userInfo">
            	<img  class="_headimg" v-show="isheadimg" :src="headimgpath"/>
            	<div class="try_product" v-show="isheadimg" v-text="lang=='zh_CN'?plan_name_zh_CN:plan_name"></div>
                <p class="user-name">{{username}}</p>
                <span class="user-mail">{{userEmail}}</span>
            </div>
            <li v-if="showDashboradEnter" @click="direct('my')">Data日报</li>
            <li @click="selecthistory()" v-text="lang=='zh_CN'?'订单历史':'Order History'"></li>
            <li @click="setting()"  v-text="lang=='zh_CN'?'账户设置':'Account Setting'"></li>
            <li @click="direct('manager')" v-text="lang=='zh_CN'?'客户管理':'Client Management'" v-show="manager== 0 || manager == 1 || manager == 2"></li>
            <li @click="logOut" v-text="lang=='zh_CN'?'退出登录':'Log out'"></li>
        </ul>
    </div>
</template>
<script type="text/javascript">
    export default{
        name:'userBar',
        props:{
            showDashboradEnter: false
        },
        data(){
            return {
                username:"",
                isShowUserBar:false,
                isLogin:false,
                userEmail:'',
                isheadimg:false,
                headimgpath:""
            }
        },
        computed:{
        lang(){
            return this.$store.state.my.lang;
        },
        manager(){
            return localStorage.manager;
        },
        plan_name(){
            return localStorage.plan_name;
        },
        plan_name_zh_CN(){
            return localStorage.plan_name_zh_CN;
        }
        },
        mounted(){
            if($.cookie()['token'] && sessionStorage.getItem('uid')){
                this.isLogin = true;
                this.$nextTick(this.setUserName);
            }
        	if(localStorage.getItem("userheadimg")){
    		//二维码用户登录获取微信头像
    		   this.isheadimg = true;
    		   this.headimgpath = localStorage.getItem("userheadimg");
    	    }else if(sessionStorage.getItem("headimg")){
    		//老用户邮箱登录获取默认头像
    		   this.isheadimg = true;
    		   this.headimgpath = sessionStorage.getItem("headimg");
        	}
            //监听滚动事件
              window.addEventListener('scroll', this.handleScroll,true);
        },
        methods:{
            direct(target) {
                this.$router.push({
                    name: target
                })
            },
            showUserBar(){
                var self = this;
                if(self.isShowUserBar){
                    self.isShowUserBar = false;
                }else{
                    self.isShowUserBar = true;
                    self.$nextTick(function(){
                        $("html").click(function(){
                            if(!self.isShowUserBar){return;}
                            self.isShowUserBar = false;
                            $("html").unbind('click');
                        })
                    })
                }
            },
            logOut(){
                U.logout();
            },
            setUserName(){
                this.username = sessionStorage.getItem('username');
                this.userEmail = sessionStorage.getItem('email');
            },
            setting(){
            	if(document.location.protocol == "https:"){  
				        location.href = 'https://' + location.hostname + '/view/bind/resetting.html';  
				}else{
				    	location.href = 'http://' + location.host + '/view/bind/resetting.html';
				}
            },
            selecthistory(){
              this.$router.push({
              	name:"orderhistory"
              })
            },
            handleScroll(){
                // console.log('handleScroll');
                this.isShowUserBar = false;
            }
        },
    }
</script>

<style type="text/css" scoped>
   .userBar{
   	    text-align: center;
        z-index: 1;
        float: right;
        height: 20px;
        line-height:20px;
        text-align: center;
        cursor: pointer;
        color: #fff;
        font-weight: bold;
        margin-top: 25px;
        position: relative;
        padding-right: 15px;
        user-select: none;
    }
    .userBar .headimg{
    	width: 22px;
    	height:22px;
    	border-radius: 50%;
    }
    .userBar ._headimg{
    	width: 40px;
    	height:40px;
    	border-radius:50%;
    }
    .userBar:after{
        content: '';
        width: 0;
        height: 0;
        margin-left: 2px;
        vertical-align: middle;
        border-top: 4px solid #fff;
        border-right: 4px solid transparent;
        border-left: 4px solid transparent;
        position: absolute;
        top: 8px;
        right: 0;
    }
    .userBar .user-bar-dropdown{
        width:285px;
        background-color: #fff;
        border: 1px solid #e3e6e6;
        border-radius: 3px;
        position: fixed;
        top: 100px;
        right:5px;
        text-align: left;
        font-weight: normal;
        cursor: default;
        font-size: 14px;
    }
    .userBar .user-bar-dropdown li{
    	text-align: center;
        height: 36px;
        color: #000;
        line-height: 36px;
        /*margin:8px 0;*/
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        padding: 0 12px;
        cursor: pointer;
        border-bottom: 1px solid #ddd;
    }
    .userBar .user-bar-dropdown li:last-child{
        border-bottom:none;
    }
    .userBar .user-bar-dropdown li:hover{
        background-color: #d6dee1;
    }
    .userInfo{
    	height: auto !important; 
        min-height:85px;
        text-align: center;
        padding: 14px 0;
        border-bottom: 1px solid #ddd;
        cursor: default;
        position: relative;
    }
    .userInfo .user-name{
    	text-align: center;
        font-size: 14px;
        margin-top: 6px;
        margin-bottom: 6px;
        color: #000;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        padding: 0 12px;
        display: block;
        box-sizing: border-box;
    }
    .userInfo .user-mail{
    	text-align: center;
        color: #999;
        font-size: 12px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        padding: 0 12px;
        display: block;
        box-sizing: border-box;
    }
     .userInfo .try_product{
     	text-align: center;
     	height: 25px;
     	line-height: 25px;
     	font-size: 10px;
     	width: 45px;
     	border: 1px solid red;
     	color: red;
     	position: absolute;
     	top:20px;
     	right:30px;
        display: table;
     }
</style>