<template>
    <transition name="component-fade">
        <div class="order-wrap">
            <div class="order-head">
                <div class="order-nav-bar clearfix">
                  客 户 信 息 管 理
                </div>
            </div>
             <div class="item-box">
        	 <div class="item-title">Eversight.AI 客户管理系统</div>
             <div class="form-container">
                <ul>
                    <li>
                        <label class="first-label">管理员账号&nbsp; : </label>
                        <input type="text" class="email" @keyup.enter="btnconfirm()"/>
                    </li>
                    <li>
                        <label class="second-label">邀请码/密码&nbsp; : </label>
                        <input type="password" class="code" @keyup.enter="btnconfirm()"/>
                    </li>
                     <li>
                        <input type="button" class="button" value="登录"  @click="btnconfirm()"/>
                    </li>
                </ul>
                </div>
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
            	
            }
        },
        mounted() {
            GaHelper.sendPageView();
        	var self = this;
        },
        computed: {
        	
        },
        methods:{
            btnconfirm(){
            var self = this; 
            if($(".email").val().trim() == ""){
               layer.msg("管理员账号不可为空");
               return false;
            }
            if($(".email").val().trim().indexOf("@")<0){
                layer.msg("管理员账号不合法");
                return false;
            }
            if($(".code").val().trim() == ""){
               layer.msg("邀请码/密码不可为空");
               return false;
            }
            //验证成功之后发起请求
            $.post({
                url:"/api/v1/account/login-admin",
                contentType:'application/x-www-form-urlencoded;charset=utf-8',
                data:{
                    email:$(".email").val(),
                    pass:$.md5($(".code").val())
                },
                success:function(resp){
                    if(resp.success){
                    var day = new Date(resp.data.token.expiry.slice(0,-4)).getTime()-new Date();
                    $.cookie("token",resp.data.token.auth_token,{
                        expires:parseInt(day/1000/3600/24),
                        path:'/'
                    });
                    localStorage.setItem("manager",resp.data.manager);
                    localStorage.setItem("useremail_",resp.data.user.email);
                       self.$router.push({
                        name:"managerinfo"
                       })
                    }else if(resp.success==false){
                        layer.msg("您输入的账号密码不正确");
                    }
                },
                error:function(error){
                    layer.msg("Network anomaly")
                }
            })
        }
      }
    }
</script>
<style type="text/css" scoped>
    /*头部*/
    .order-head {
        height: 160px;
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
    .order-nav-bar {
        height: 70px;
        line-height:70px;
        margin: 35px 120px;
        font-size: 35px;
    }
    .item-box{
    	width: 600px;
    	height: 600px;
    	margin:50px auto;
    }
    .item-box .item-title{
    	width:85%;
    	height: 30px;
    	line-height: 30px;
    	text-align: right;
    	font-size: 22px;
    }
    .item-box .form-container{
        width: 100%;
        height: 50%;
        margin:70px auto;
    }
    .item-box .form-container ul{
        width:80%;
        height:300px;
        margin:0 auto;
    }
    .item-box .form-container ul li{
            width: 90%;
            height: 28px;
            line-height: 28px;
            margin:40px 0;
    }
    .item-box .form-container ul li:last-child{
            margin-top:60px; 
    }
    .item-box .form-container ul li label{
        width: 40%;
        height: 100%;
        font-size: 14px;
        color:gray;
        text-align: right;
        display: block;
        float: left;
    }
     .item-box .form-container ul li input{
        outline: none;
        border: none;
        border: 1px solid gray;
        display: block;
        float: right;
        width:55%;
     }
    .item-box .form-container ul li .button{
            text-align: center;
            background:#355867;
            color:#FFFFFF;
            border: none;
            cursor: pointer;
            font-size: 16px;
            border-radius:5px;
            height:35px;
            line-height:35px;
      }
</style>
