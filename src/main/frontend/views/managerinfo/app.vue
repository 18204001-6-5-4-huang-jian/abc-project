<template>
    <transition name="component-fade">
        <div class="order-wrap">
            <div class="order-head">
                <div class="order-nav-bar clearfix">
                  客 户 信 息 管 理
                </div>
                <div class="order-head-exit" @click="exit()">退出</div>
                <div class="order-head-name" v-text="'管理员 : ' + email"></div>
             </div>
             <div class="item-box form-wrapper">
                <div class="switch_bar" style="font-size:14px;">
                <div class="switch_btn" @click="loginway=0" :class="{active:loginway==0}" v-show="manager == 0 || manager == 1">试用账号</div>
                <div class="switch_btn" @click="loginway=1" :class="{active:loginway==1}" v-text="manager == 2?'我邀请的用户':'免费账号'"></div>
                <div class="switch_btn" @click="loginway=2" :class="{active:loginway==2}" v-show="manager == 0 || manager == 1">付费账号</div>
                <span class="btn_underline" :class="{atLeft:loginway==0,atCenter:loginway==1,atRight:loginway==2}"
                 v-show="manager == 0 || manager == 1"></span>
                </div>
                <div class="item-title" v-show="huang==0">
                    <div class="left" v-text="msg"></div>
                    <div class="right" @click="productaccount()" v-show="loginway==0">生成新账号</div>
                </div>
                <!-- 表头 -->
                <table cellpadding="1" cellspacing="1"  v-show="huang==0">
                    <tr>
                        <td>账号</td>
                        <td v-text="loginway==0?'激活日期':'注册日期'"></td>
                        <td>账号到期时间</td>
                        <td>产品剩余时间</td>
                        <td v-text="loginway==0?'密码':'邀请码'"></td>
                        <td>用户姓名</td>
                        <td>公司名称</td>
                        <td>操作</td>
                    </tr>
                </table>
                <!-- 表格 -->
                <table cellpadding="1" cellspacing="1"  v-show="huang==0" id="itemContainer">
                    <!--数据 -->
                     <tr v-for="(item,index) in arrlist">
                        <td v-text="item.email"></td>
                        <td v-text="item.create_at.substring(0, 10)"></td>
                        <td v-text="item.expiry.substring(0, 10)"></td>
                        <td v-text="item.remaining"></td>
                        <td v-text="item.invite_code"></td>
                        <td v-text="item.username"></td>
                        <td v-text="typeof item.company == 'string'?item.company:item.company.name"></td>
                        <td>
                        <div @click="edit(item.id,item.email)" :class="{huang:loginway!=0}" v-text="loginway==0?'查看':'编辑'"></div>
                        <div @click="dele(item.email,item.id)" v-show="loginway==0">删除</div>
                        </td>
                    </tr>
                </table>
                <!--分页-->
                <div class="holder" v-show="huang==0">
                    
                </div> 
                  <!-- 删除框 -->
                <div class="delete-confirm" v-show="confirm == 1">
                    <div class="delete-title" v-text="'确认删除账号' + deleteemail + '?'"></div>
                    <div class="left-button" @click="confirmdelete()">确认</div>
                    <div class="right-button" @click="cancle()">取消</div>
                </div>
                <!-- 用户信息详情 -->
                <div class="userparticulars" v-if="huang==1">
                    <div class="returnback" @click="returnback()"><返回上一级</div>
                    <h5>&nbsp;用户信息 : </h5>
                    <div class="user-box">
                        <ul>
                            <li>
                                <label>&nbsp;User_ID : </label>
                                <input type="text" class="userid" readonly="readonly" v-model="user_obj.id">
                            </li>
                            <li>
                                <label>&nbsp;部门 : </label>
                                <input type="text" class="deparment"  v-model="user_obj.department == '未填写'?'':user_obj.department"  :placeholder="user_obj.department == '未填写'?'未填写':''">
                            </li>
                             <li>
                                <label>&nbsp;微信账号 : </label>
                                <input type="text" class="code" readonly="readonly" v-model="user_obj.nickname == '未填写'?'':user_obj.nickname"  :placeholder="user_obj.nickname == '未填写'?'未填写':''">
                            </li>
                             <li>
                                <label>&nbsp;职位 : </label>
                                <input type="text" class="position"  v-model="user_obj.job_title == '未填写'?'':user_obj.job_title"  :placeholder="user_obj.job_title == '未填写'?'未填写':''">
                            </li>
                            <li>
                                <label>&nbsp;邮箱账号 : </label>
                                <input type="text" class="account" readonly="readonly" v-model="user_obj.email">
                            </li>
                             <li>
                                <label>&nbsp;电话 : </label>
                                <input type="text" class="telphone"  v-model="user_obj.telephone == '未填写'?'':user_obj.telephone"  :placeholder="user_obj.telephone == '未填写'?'未填写':''">
                            </li>
                            <li>
                                <label>&nbsp;密码 : </label>
                                <input type="text" class="password" readonly="readonly" v-model="user_obj.pwd_ends">
                                <label class="labell" @click="changepassword()" v-text="loginway==0?'修改密码':'重置密码'" v-show="loginway==0"></label >
                            </li>
                             <li>
                                <label>&nbsp;管理资产规模 : </label>
                                <select @change="managementassetgn($event)">
                                <option v-for="item in user_obj.asset_size" v-text="item.name" :value="item.id"></option>
                                </select>
                            </li>
                             <li>
                                <label>&nbsp;姓名 : </label>
                                <input type="text" class="name" :readonly="loginway==0?null:'readonly'" v-model="user_obj.username">
                            </li>
                           <li>
                                <label>&nbsp;投资(研究)市场 : </label>
                                 <select @change="bazaargn($event)">
                                <option v-for="item in user_obj.i_m" v-text="item.name" :value="item.id"></option>
                                </select>
                            </li>

                            <li>
                                <label>&nbsp;性别 : </label>
                                 <select @change="sexgn($event)" class="sex">
                                <option v-for="item in user_obj.gender" v-text="item.name" :value="item.id"></option>
                                </select>
                            </li>
                             <li>
                                <label>&nbsp;投资(研究)行业 : </label>
                                <select @change="Investmentareagn($event)">
                                  <option v-for="item in user_obj.i_f" v-text="item.name" :value="item.id"></option>
                                </select>
                            </li>
                            <li>
                                <label>&nbsp;商业机构简称 : </label>
                                <input type="text" class="organization" :readonly="loginway==0?null:'readonly'" v-model="user_obj.company == '未填写'?'':user_obj.company"  :placeholder="user_obj.company == '未填写'?'未填写':''">
                            </li>
                            <li>
                                <label>&nbsp;投资(研究)风格 : </label>
                                 <select @change="stylegn($event)">
                                <option v-for="item in user_obj.i_s" v-text="item.name" :value="item.id"></option>
                                </select>
                            </li>
                             <li>
                                <label>&nbsp;商业机构全称 : </label>
                                <input type="text" class="organization-full"  v-model="user_obj.company_full == '未填写'?'':user_obj.company_full"  :placeholder="user_obj.company_full == '未填写'?'未填写':''">
                            </li>
                        </ul>
                    </div>
                    <h5>&nbsp;试用权限 : </h5><br/>
                    <div class="user-box user-box_">
                           &nbsp;方案&nbsp;&nbsp;&nbsp;&nbsp;
                           <select @change="plan($event)" :disabled="loginway==2 ||  pay == true?'disabled':null">
                               <option selected>Enterprise</option>
                               <option>Standard</option>
                               <option>Basic</option>
                           </select><br/><br/><br/>
                            &nbsp;天数&nbsp;&nbsp;&nbsp;&nbsp;
                           <select @change="getday($event)" :disabled="loginway==2 || pay == true?'disabled':null">
                               <option selected>1</option>
                               <option>3</option>
                               <option>5</option>
                               <option>7</option>
                               <option>14</option>
                               <option>21</option>
                               <option>28</option>
                               <option>30</option>
                           </select>&nbsp;&nbsp;天<br/><br/><br/>
                    </div>
                    <div class="user-dashboard">
                    <div style="width:3.5%;float:left;">&nbsp;看板</div>
                         <ul id="inputsParent">
                            <li v-for="item in dashboard_array">
                            <input type="checkbox" name="test"  :value="item.id"  :checked="item.check==true?true:false" :disabled="loginway==2 || pay == true?'disabled':null"  @click="checkclick($event)"><span v-text="item.name" :style="item.name == 'TAL' || item.name == 'MOMO'?{fontWeight:'bold'}:{fontWeight:'normal'}"></span>
                            </li>
                            <div v-show=" manager == 2 && pay == false">注：推荐开通MOMO和TAL，除MOMO和TAL之外，其他新开通看板不能超过2个。</div>
                         </ul>
                         <button @click="sure()">确定</button>
                    </div>
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
            	email:'',
                huang:0,
                confirm:0,
                loginway: 0,
                msg:'已激活账号列表 : ',
                lang:'zh_CN',
                sort:"create_at",
                order:"desc",
                offset:0,
                limit:99999,
                trail:null,
                paid:0,
                deleted:0,
                arrlist:[],
                deleteemail:'',
                userid:'',
                user_obj:{},
                dashboard_array:[],
                amendpassword:0,
                plans:2,//方案id,Basic: 0, Standard: 1, Enterprise: 2
                term_unit:"day",
                day:1,
                sex:null,
                Investmentarea:null,
                managementasset:null,
                bazaar:null,
                style:null,
                pay:null,
                p_tag:null,
                inputs:[],
                selectInputs:[]
            }
        },
        mounted() {
          GaHelper.sendPageView();
        	var self = this;
            self.email = self.useremail;
            if(self.manager == 0 || self.manager == 1){
                //默认显示试用账号
                self.trail = 1;
                 //请求数据渲染表格(分析师)
                $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&trail=' + self.trail + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        console.log(error);
                    }
                })
                }else if(self.manager == 2){
                    //销售不需要传trail,需要传paid默认显示免费账号,只可以编辑
                    self.loginway = 1;
                    $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                })
            }
        },
        watch:{
            loginway(newval){
               if(newval==1 && this.manager != 2){
                //免费账号
                var self = this;
                self.paid = 0;
                self.huang=0;
                $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&paid=' + self.paid + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                })
               }else if(newval==2){
                //付费账号
                var self = this;
                self.paid = 1;
                self.huang=0;
                $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&paid=' + self.paid + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                })
               }else if(newval==0){
                //试用账号
                var self = this;
                self.trail = 1;
                self.huang=0;
                $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&trail=' + self.trail + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                })
               }
            }
        },
        computed: {
        	useremail(){
                return localStorage.useremail_;
            },
            manager(){
                 return localStorage.manager;
            }
        },
        methods:{
           exit(){
              var self = this;
              self.$router.push({
                name:"manager"
              })
           },
           returnback(){
              var self = this;
              self.huang = 0;
              //返回刷新页面
              if(self.manager == 0 || self.manager == 1){
                 if(self.loginway == 0){
                self.trail = 1;
                $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&trail=' + self.trail + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        console.log(error);
                    }
                })
              }else if(self.loginway == 1){
                 self.paid = 0;
                 $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&paid=' + self.paid + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                })
              }else if(self.loginway == 2){
                 self.paid = 1;
                 $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&paid=' + self.paid + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                })
              }
              }else if(self.manager == 2){
                 $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                })
              }
           },
           productaccount(){
             var self = this;
             self.huang = 1; 
             var email;
             //随机生成账号密码。渲染页面
             $.get({
             url:'/api/v1/account/create-trail',
             contentType:'application/json;charset=utf-8',
             cache:false,
             success:function(res){
              $.get({
                  url:' /api/v1/account/' + res.data.id,
                  contentType:'application/json;charset=utf-8',
                  cache:false,
                  success:function(resp){
                      self.user_obj = resp.data;
                      self.user_obj.pwd_ends = res.data.password;
                      email = self.user_obj.email;
                       //获取看板列表
                      $.get({
                         url:'/api/v1/user-products/detail?email=' + email,
                         contentType:'application/json;charset=utf-8',
                         cache:false,
                         success:function(resp){
                                self.dashboard_array = resp.data.recommend_board.concat(resp.data.user_board.concat(resp.data.unpaid_board)); 
                                //self.dashboard_array = resp.data.user_board.concat(resp.data.unpaid_board);
                                var arr = resp.data.user_board;
                                for (var i = 0; i < arr.length; i++) {
                                    arr[i].check = true;
                                };
                            },
                            error:function(error){
                                layer.msg("Network anomaly")
                            }
                             })
                          },
                          error:function(error){
                              layer.msg("Network anomaly")
                          }
                      })
                      },
                      error:function(error){
                          layer.msg("Network anomaly")
                      }
                     })
           },
           edit(id,email){
             var self = this;
             //显示用户详情
             self.huang = 1; 
                 //console.log(id);
                $.get({
                    url:' /api/v1/account/' + id,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.user_obj = resp.data;
                        if(self.manager==2){
                             self.pay = resp.data.paid;
                        }
                        //$("html,body").animate({scrollTop:0},100);
                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                })
                // 获取看板
                // console.log(email);
                $.get({
                    url:"/api/v1/user-products/detail?email=" + email,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.dashboard_array = resp.data.recommend_board.concat(resp.data.user_board.concat(resp.data.unpaid_board)); 
                        //self.dashboard_array = resp.data.user_board.concat(resp.data.unpaid_board);
                        //推荐开通账号
                        // var arr = resp.data.recommend_board;
                        // for (var i = 0; i < arr.length; i++) {
                        //     arr[i].check = true;
                        // };
                        //已购买账号
                        var arr1 = resp.data.user_board;
                        for (var i = 0; i < arr1.length; i++) {
                            arr1[i].check = true;
                        }; 
                       
                    },
                    complete:function(){
                        // self.$nextTick(function(){
                        // //用于限制checkbox
                        // self.p_tag = document.getElementById("inputsParent");
                        // self.inputs = self.p_tag.getElementsByTagName("input");
                        // for(var i=0; i<self.inputs.length; i++){
                        //     if(self.inputs[i].checked == true){
                        //         self.selectInputs.push(self.inputs[i]);
                        //     }
                        // }
                        // })

                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                })
           },
           dele(email,id){
               var self = this;
               self.deleteemail = email;
               self.userid = id;
               self.confirm = 1;
           },
           confirmdelete(){
              //删除数据再次渲染页面
              var self = this;
              $.post({
                url:'/api/v1/account/delete-trail/' + self.userid,
                contentType:'application/x-www-form-urlencoded;charset=utf-8',
                data:{
                    id:self.userid
                },
                success:function(resp){
                self.confirm = 0;
                $.get({
                    url:'/api/v1/accounts?sort=' + self.sort + '&order=' + self.order + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang + '&trail=' + self.trail + '&deleted=' + self.deleted,
                    contentType:'application/json;charset=utf-8',
                    success:function(resp){
                        self.arrlist = resp.data.list;
                        self.$nextTick(function(){
                        $('div.holder').jPages({  
                        containerID: 'itemContainer',  
                        first:false,//false为不显示  
                        previous: '前一页',//false为不显示  
                        next: '下一页',//false为不显示 自定义按钮  
                        last:false,//false为不显示  
                        perPage:100,//每页最多显示几个
                        //keyBrowse: true,//键盘切换  
                        //scrollBrowse: true,//滚轮切换  
                        callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                        },   
                    });  
                    //跳转到某一页  
                    $('.goPage').on('click', function(){
                         $(".holder").jPages(5);
                        });
                      })
                    },
                    error:function(error){
                        layer.msg("Network anomaly")
                    }
                  })
                },
                error:function(error){
                    layer.msg("Network anomaly")
                }
              })
           },
           cancle(){
            var self = this;
            self.confirm = 0;
           },
           changepassword(){
            var self = this;
            self.amendpassword = 1;
            self.user_obj.pwd_ends = null;
            $(".password").removeAttr("readonly");
            $(".password").focus();
           },
           //Basic: 0, Standard: 1, Enterprise: 2
           plan(e){
            var self = this;
            if(e.target.value == "Standard"){
              self.plans = 1;
            }else if(e.target.value == "Basic"){
              self.plans = 0;
            }else if(e.target.value == "Enterprise"){
               self.plans = 2;
            };
           },
           getday(e){
             var self = this;
             self.day = e.target.value;
           },
           sure(){
            var self = this;
            var pwd_ends =$(".password").val().substring(0,2) + "****" + $(".password").val().substring($(".password").val().length-2,$(".password").val().length);
            //判断是否修改密码,调用修改密码接口
            if(self.amendpassword == 1){
                 $.post({
                  url:'/api/v1/account/set-password',
                  contentType:'application/x-www-form-urlencoded;charset=utf-8',
                  data:{
                      email:self.user_obj.email,
                      pass:$.md5($(".password").val()),
                      pwd_ends:pwd_ends
                  },
                  success:function(resp){
              self.amendpassword = 0;
              if(self.loginway == 0){
              //console.log("试用账号");
              var obj = document.getElementsByName("test");
              var check_val = [];
              for(var k in obj){
                  if(obj[k].checked)
                      check_val.push(obj[k].value);
              }
              //console.log(check_val);
              $.post({
              url:'/api/v1/account/edit-trail',
              contentType:'application/x-www-form-urlencoded;charset=utf-8',
              data:{
                   id:self.user_obj.id,
                   password:self.amendpassword == 0?null:$.md5($(".password").val()),
                   department:$(".deparment").val(),
                   job_title:$(".position").val(),
                   telephone:$(".telphone").val(),
                   username:$(".name").val(),
                   company:$(".organization").val(),
                   plan:self.plans,
                   term_unit:self.term_unit,
                   term_long:self.day,
                   pids:check_val,
                   lang:"zh_CN",
                   gender:self.sex,
                   i_f:self.Investmentarea,
                   i_m:self.bazaar,
                   i_s:self.style,
                   asset_size:self.managementasset,
                   company_full:$(".organization-full").val()
              },
              success:function(resp){
                if(resp.success){
                  // self.returnback();
                  $("html,body").animate({scrollTop:0},100);
                  layer.msg("试用权限开通成功");
                }
              },
               error:function(error){
                layer.msg("Network anomaly")
                }
             })
             }else if (self.loginway == 1 && self.manager != 2){
             //console.log("免费账号");
              var obj = document.getElementsByName("test");
              var check_val = [];
              for(var k in obj){
                  if(obj[k].checked)
                      check_val.push(obj[k].value);
              }
              $.post({
              url:'/api/v1/account/edit-unpaid',
              contentType:'application/x-www-form-urlencoded;charset=utf-8',
              data:{
                   id:self.user_obj.id,
                   password:self.amendpassword == 0?null:$.md5($(".password").val()),
                   department:$(".deparment").val(),
                   job_title:$(".position").val(),
                   telephone:$(".telphone").val(),
                   plan:self.plans,
                   term_unit:self.term_unit,
                   term_long:self.day,
                   pids:check_val,
                   lang:"zh_CN",
                   gender:self.sex,
                   i_f:self.Investmentarea,
                   i_m:self.bazaar,
                   i_s:self.style,
                   asset_size:self.managementasset,
                   company_full:$(".organization-full").val()
              },
              success:function(resp){
                if(resp.success){
                  // self.returnback();
                  $("html,body").animate({scrollTop:0},100);
                   layer.msg("试用权限开通成功");
                }
              },
               error:function(error){
                layer.msg("Network anomaly")
                }
             })
             }else if (self.loginway == 1 && self.manager == 2) {
              if(self.pay == true){
              var obj = document.getElementsByName("test");
              var check_val = [];
              for(var k in obj){
                  if(obj[k].checked)
                      check_val.push(obj[k].value);
              }
              $.post({
              url:'/api/v1/account/edit-paid',
              contentType:'application/x-www-form-urlencoded;charset=utf-8',
              data:{
                   id:self.user_obj.id,
                   password:self.amendpassword == 0?null:$.md5($(".password").val()),
                   department:$(".deparment").val(),
                   job_title:$(".position").val(),
                   telephone:$(".telphone").val(),
                   plan:self.plans,
                   term_unit:self.term_unit,
                   term_long:self.day,
                   pids:check_val,
                   lang:"zh_CN",
                   gender:self.sex,
                   i_f:self.Investmentarea,
                   i_m:self.bazaar,
                   i_s:self.style,
                   asset_size:self.managementasset,
                   company_full:$(".organization-full").val()
              },
              success:function(resp){
                if(resp.success){
                  // self.returnback();
                  $("html,body").animate({scrollTop:0},100);
                   layer.msg("账户资料修改成功");
                }
              },
               error:function(error){
                layer.msg("Network anomaly")
                }
             })
              }else if(self.pay == false){
              var obj = document.getElementsByName("test");
              var check_val = [];
              for(var k in obj){
                  if(obj[k].checked)
                      check_val.push(obj[k].value);
              }
              $.post({
              url:'/api/v1/account/edit-unpaid',
              contentType:'application/x-www-form-urlencoded;charset=utf-8',
              data:{
                   id:self.user_obj.id,
                   password:self.amendpassword == 0?null:$.md5($(".password").val()),
                   department:$(".deparment").val(),
                   job_title:$(".position").val(),
                   telephone:$(".telphone").val(),
                   plan:self.plans,
                   term_unit:self.term_unit,
                   term_long:self.day,
                   pids:check_val,
                   lang:"zh_CN",
                   gender:self.sex,
                   i_f:self.Investmentarea,
                   i_m:self.bazaar,
                   i_s:self.style,
                   asset_size:self.managementasset,
                   company_full:$(".organization-full").val()
              },
              success:function(resp){
                if(resp.success){
                  // self.returnback();
                  $("html,body").animate({scrollTop:0},100);
                   layer.msg("账户资料修改成功");
                }else if(resp.success == false && resp.status == 2){
                   layer.msg("除MOMO和TAL之外，其他新开通看板不能超过2个。");
                }
              },
               error:function(error){
                layer.msg("Network anomaly")
                }
             })
                }
             }else if (self.loginway == 2){
                 $.post({
                    url:"api/v1/account/edit",
                    contentType:'application/x-www-form-urlencoded;charset=utf-8',
                    data:{
                       id:self.user_obj.id,
                       password:self.amendpassword == 0?null:$.md5($(".password").val()),
                       department:$(".deparment").val(),
                       job_title:$(".position").val(),
                       telephone:$(".telphone").val(),
                       gender:self.sex,
                       i_f:self.Investmentarea,
                       i_m:self.bazaar,
                       i_s:self.style, 
                       company_full:$(".organization-full").val(),
                    },
                    success:function(resp){
                        if(resp.success){
                          $("html,body").animate({scrollTop:0},100);
                           layer.msg("账户资料修改成功");
                        }
                    },
                    error:function(error){
                         layer.msg("Network anomaly")
                    }
                  })
             };
              },
              error:function(error){
                    layer.msg("Network anomaly")
              }
             })
            }else{
            	//判断试用账号还是免费账号,调用接口修改
              if(self.loginway == 0){
              //console.log("试用账号");
              var obj = document.getElementsByName("test");
              var check_val = [];
              for(var k in obj){
                  if(obj[k].checked)
                      check_val.push(obj[k].value);
              }
              //console.log(check_val);
              $.post({
              url:'/api/v1/account/edit-trail',
              contentType:'application/x-www-form-urlencoded;charset=utf-8',
              data:{
                   id:self.user_obj.id,
                   password:self.amendpassword == 0?null:$.md5($(".password").val()),
                   department:$(".deparment").val(),
                   job_title:$(".position").val(),
                   telephone:$(".telphone").val(),
                   username:$(".name").val(),
                   company:$(".organization").val(),
                   plan:self.plans,
                   term_unit:self.term_unit,
                   term_long:self.day,
                   pids:check_val,
                   lang:"zh_CN",
                   gender:self.sex,
                   i_f:self.Investmentarea,
                   i_m:self.bazaar,
                   i_s:self.style,
                   asset_size:self.managementasset,
                   company_full:$(".organization-full").val()
              },
              success:function(resp){
                if(resp.success){
                  // self.returnback();
                  $("html,body").animate({scrollTop:0},100);
                   layer.msg("试用权限开通成功");
                }
              },
               error:function(error){
                layer.msg("Network anomaly")
                }
             })
             }else if (self.loginway == 1 && self.manager != 2){
             //console.log("免费账号");
              var obj = document.getElementsByName("test");
              var check_val = [];
              for(var k in obj){
                  if(obj[k].checked)
                      check_val.push(obj[k].value);
              }
              $.post({
              url:'/api/v1/account/edit-unpaid',
              contentType:'application/x-www-form-urlencoded;charset=utf-8',
              data:{
                   id:self.user_obj.id,
                   password:self.amendpassword == 0?null:$.md5($(".password").val()),
                   department:$(".deparment").val(),
                   job_title:$(".position").val(),
                   telephone:$(".telphone").val(),
                   plan:self.plans,
                   term_unit:self.term_unit,
                   term_long:self.day,
                   pids:check_val,
                   lang:"zh_CN",
                   gender:self.sex,
                   i_f:self.Investmentarea,
                   i_m:self.bazaar,
                   i_s:self.style,
                   asset_size:self.managementasset,
                   company_full:$(".organization-full").val()
              },
              success:function(resp){
                if(resp.success){
                  // self.returnback();
                  $("html,body").animate({scrollTop:0},100);
                   layer.msg("试用权限开通成功");
                }
              },
               error:function(error){
                layer.msg("Network anomaly")
                }
             })
             }else if (self.loginway == 1 && self.manager == 2) {
                if(self.pay == true){
              var obj = document.getElementsByName("test");
              var check_val = [];
              for(var k in obj){
                  if(obj[k].checked)
                      check_val.push(obj[k].value);
              }
              $.post({
              url:'/api/v1/account/edit-paid',
              contentType:'application/x-www-form-urlencoded;charset=utf-8',
              data:{
                   id:self.user_obj.id,
                   password:self.amendpassword == 0?null:$.md5($(".password").val()),
                   department:$(".deparment").val(),
                   job_title:$(".position").val(),
                   telephone:$(".telphone").val(),
                   plan:self.plans,
                   term_unit:self.term_unit,
                   term_long:self.day,
                   pids:check_val,
                   lang:"zh_CN",
                   gender:self.sex,
                   i_f:self.Investmentarea,
                   i_m:self.bazaar,
                   i_s:self.style,
                   asset_size:self.managementasset,
                   company_full:$(".organization-full").val()
              },
              success:function(resp){
                if(resp.success){
                  // self.returnback();
                  $("html,body").animate({scrollTop:0},100);
                   layer.msg("账户资料修改成功");
                }
              },
               error:function(error){
                layer.msg("Network anomaly")
                }
             })
                }else if(self.pay == false){
              var obj = document.getElementsByName("test");
              var check_val = [];
              for(var k in obj){
                  if(obj[k].checked)
                      check_val.push(obj[k].value);
              }
              $.post({
              url:'/api/v1/account/edit-unpaid',
              contentType:'application/x-www-form-urlencoded;charset=utf-8',
              data:{
                   id:self.user_obj.id,
                   password:self.amendpassword == 0?null:$.md5($(".password").val()),
                   department:$(".deparment").val(),
                   job_title:$(".position").val(),
                   telephone:$(".telphone").val(),
                   plan:self.plans,
                   term_unit:self.term_unit,
                   term_long:self.day,
                   pids:check_val,
                   lang:"zh_CN",
                   gender:self.sex,
                   i_f:self.Investmentarea,
                   i_m:self.bazaar,
                   i_s:self.style,
                   asset_size:self.managementasset,
                   company_full:$(".organization-full").val()
              },
              success:function(resp){
                if(resp.success){
                  // self.returnback();
                  $("html,body").animate({scrollTop:0},100);
                   layer.msg("账户资料修改成功");
                }else if(resp.success == false && resp.status == 2){
                   layer.msg("除MOMO和TAL，其他新开通看板不能超过2个。");
                }
              },
               error:function(error){
                layer.msg("Network anomaly")
                }
             })
                }
             
             }else if(self.loginway == 2){
                  $.post({
                    url:"api/v1/account/edit-paid",
                    contentType:'application/x-www-form-urlencoded;charset=utf-8',
                    data:{
                       id:self.user_obj.id,
                       password:self.amendpassword == 0?null:$.md5($(".password").val()),
                       department:$(".deparment").val(),
                       job_title:$(".position").val(),
                       telephone:$(".telphone").val(),
                       gender:self.sex,
                       i_f:self.Investmentarea,
                       i_m:self.bazaar,
                       i_s:self.style, 
                       company_full:$(".organization-full").val(),
                    },
                    success:function(resp){
                        if(resp.success){
                          $("html,body").animate({scrollTop:0},100);
                           layer.msg("账户资料修改成功");
                        }
                    },
                    error:function(error){
                         layer.msg("Network anomaly")
                    }
                  })
             };
            }
           },
           sexgn(e){
             var self = this;
             self.sex = e.target.value;
           },
           Investmentareagn(e){
            var self = this;
            self.Investmentarea = e.target.value;
           },
           managementassetgn(e){
            var self = this;
            self.managementasset = e.target.value;
           },
           bazaargn(e){
            var self = this;
            self.bazaar = e.target.value;
           },
           stylegn(e){
            var self = this;
            self.style = e.target.value;
           },
           checkclick(e){
            var self = this;
            //判断是销售视角的免费账号
            // if(self.manager == 2){
            //    // console.log("判断是销售视角的免费账号");
            //     var i = 0;
            //     var n = 0;
            //     if(e.target.checked == true)
            //     {
            //         self.selectInputs.push(e.target);
            //         if(self.selectInputs.length > 4){
            //             self.selectInputs[0].checked = false;
            //             self.selectInputs.shift();
            //         }
            //     }else{
            //         if(self.selectInputs.length>1){
            //             for(var i=0; i<self.selectInputs.length; i++){
            //                 if(e.target == self.selectInputs[i]) self.selectInputs.splice(i,1);
            //             }
            //         }else{
            //             e.target.checked = true;
            //             return false;
            //         }
            //     }
            // }
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
    .order-head .order-head-name{
        min-width: 200px;
        height: 30px;
        line-height: 30px;
        float: right;
        margin-right:20px;
        display: table;
        color: #ffffff;
        margin-top: 100px;
    }
    .order-head .order-head-exit{
        width: 50px;
        height: 30px;
        line-height: 30px;
        float:right;
        margin-right:10px;
        display: table;
        color: #ffffff;
        margin-top: 100px;
        text-decoration:underline;
        cursor: pointer;
    }
    .order-nav-bar {
        width:400px;
        height: 70px;
        line-height: 70px;
        float: left;
        margin:35px 120px;
        font-size: 35px;
        margin-top: 35px !import;
    }
    .item-box{
    	width:100%;
    	height: 600px;
    	margin:10px auto;
    }
    .switch_bar {
        height: 80px;
        width:360px;
        float: left;
        margin-left: 5%;
        position: relative;
        margin-top: 10px;
    }
    .switch_bar .switch_btn {
        float: left;
        cursor: pointer;
        width: 33.3%;
        height: 80px;
        line-height: 80px;
        text-align: center;
        font-size: 16px;
    }
    .btn_underline {
        height: 2px;
        display: block;
        width: 120px;
        background-color:#8F8F8F;
        position: absolute;
        bottom: 10px;
        transition: left 0.3s;
    }
     .atLeft {
        left:0px;
    }
     .atCenter {
        left: 120px;
    }
     .atRight {
        left:240px;
    }
    .active {
        font-weight: bold;
    }
    .huang{
        width: 100% !important;
    }
    .item-box .item-title{
        width:100%;
        height: 50px;
        float: left;
        line-height: 50px;
        margin:0 auto;
    }
    .item-box .item-title .left{
        float: left;
        margin-left: 80px;
    }
    .item-box .item-title .right{
       float: right;
       margin-right: 130px;
       width: 120px;
       height:25px;
       line-height: 25px;
       text-align: center;
       border:1px solid gray;
       border-radius:5px;
       cursor: pointer;
       margin-top: 11.5px;
    }
    .form-wrapper table{
        font-size: 14px;
        margin:0 auto;
        width: 90%;
        table-layout: fixed;  
      }
    .form-wrapper table tr{
        height: 50px;
        line-height:50px;
       }
    .form-wrapper table tr td{
        text-align: center;
     }
     .form-wrapper table td:first-child{
        text-align: left;
    }
    .form-wrapper table td:last-child>div{
        width: 50%;
        float: left;
        text-decoration:underline;
        cursor: pointer;
    }
    .form-wrapper .delete-confirm{
        width: 300px;
        height: 120px;
        position: fixed;
        background: #ffffff;
        z-index:2;
        left:180px;
        bottom:200px;
        display: table;
        border:1px solid gray;
    }
     .form-wrapper .delete-confirm .delete-title{
        width: 80%;
        height:70px;
        display: table;
        white-space: nowrap;
        line-height: 70px;
        margin: 0 auto;
     }
     .form-wrapper .delete-confirm  .left-button{
        width: 70px;
        height: 30px;
        line-height: 30px;
        text-align: center;
        float: left;
        margin-left: 30px;
        cursor: pointer;
        background: gray;
        color: #ffffff;
        border-radius: 5px;
        border:1px solid gray;
     } 
     .form-wrapper .delete-confirm  .right-button{
        width: 70px;
        height: 30px;
        line-height: 30px;
        text-align: center;
        float:right;
        margin-right: 30px;
        cursor: pointer;
        border-radius: 5px;
        border:1px solid gray;
     }
    .item-box .userparticulars{
        width: 90%;
        min-height:800px;
        clear: both;
        display: block;
        margin:0 auto;
        /*border:1px solid red;*/
    }
    .item-box .userparticulars .returnback{
        width: 100%;
        height: 30px;
        line-height: 30px;
        text-align: left;
        text-decoration: underline;
        cursor: pointer;
    }
    .item-box .userparticulars h5{
         font-weight: bold;
         width: 200px;
         height:50px;
         display: block;
         line-height:50px;
    }
    .item-box .userparticulars .user-box{
        width:100%;
        height:460px;
    }
    .item-box .userparticulars .user-box_{
        height: 120px !important;
    }
    .item-box .userparticulars .user-box .labell{
        width:80px;
        cursor: pointer;
        text-decoration: underline;
    }
    .item-box .userparticulars .user-box ul{
        width: 100%;
        height: 100%;
    }
    .item-box .userparticulars .user-box ul li{
        display: block;
        width: 50%;
        height: 55px;
        line-height: 55px;
        float: left;
    }
    .item-box .userparticulars .user-box ul li label{
       width: 120px;
       text-align: right;
    }
    .item-box .userparticulars .user-box ul li select{
      width:300px;
      height:35px;
    }
    .item-box .userparticulars .user-box ul li input{
         width:300px;
         height:35px;
         line-height:35px;
         border:1px solid gray;
         text-indent: 10px;
    }
    .user-box select{
        width: 100px;
        border: solid 1px #000;
    }
    .item-box .userparticulars .user-dashboard{
        width: 100%;
        height: 300px;
    }
    .item-box .userparticulars .user-dashboard ul{
        width:92%;
        height:200px;
        border:1px solid gray;
        display:block;float:left;
    }
    .item-box .userparticulars .user-dashboard ul li{
        display: block;
        float: left;
        margin:10px 18px;
    }
    .item-box .userparticulars .user-dashboard ul li input[type=checkbox]{
        margin:0 !important;
        visibility: visible;
        margin-right: 10px !important;
        z-index:0 !important;
    }
    .item-box .userparticulars .user-dashboard button{
        display: block;
        float: left;
        width: 200px;
        height: 30px;
        line-height: 30px;
        margin-top: 20px;
        margin-left: 50px;
        border-radius: 5px;
        background: gray;
        color: #ffffff;
        text-align: center;
    }
    #inputsParent{
        position: relative;
    }
    #inputsParent  div{
        width: 560px;
        height: 30px;
        line-height: 30px;
        position: absolute;
        bottom: 0;
        left: 0;
        text-align: center; 
        color: gray;
    }
    .holder{
        padding-top: 15px;
        width:50%;
        margin:15px auto;
        text-align: center;
    }
</style>
