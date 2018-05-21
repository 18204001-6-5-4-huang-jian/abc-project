<template>
    <transition name="component-fade">
        <div class="order-wrap">
            <div class="order-head">
                <div class="order-nav-bar clearfix">
                    <div class="logo abcdata-icon pointer" @click="direct('introduction')"></div>
                   <div style="width: 30%;height:60px;position: absolute;left: 13%;" class="huangjian-box">
                   	 <div class="switch_bar" style="font-size:14px;">
                        <div class="switch_btn" @click="loginway=0" :class="{active:loginway==0,}" v-text="lang=='zh_CN'?'大数据日报':'Data daily'"></div>
                        <div class="switch_btn" @click="loginway=1" :class="{active:loginway==1}" v-text="lang=='zh_CN'?'上市公司透视':'Dashboard'"></div>
                        <span class="btn_underline" :class="{atLeft:loginway==0,atRight:loginway==1}"></span>
                    </div>
                   </div>
                    <user-bar></user-bar>
                     <ul class="langchange">
                        <li @click="li_langchange=0" :class="{active:li_langchange==0}">中文</li>
                        <li @click="li_langchange=1" :class="{active:li_langchange==1}">English</li>
                    </ul>
                    <!-- @click.stop来阻止冒泡 -->
                    <!-- 消息提醒 -->
                     <div class="notification-bell" @click.stop="quarterlygn()">
                         <p  v-if="quarterly==0 && quarterlyNumber != 0" v-text="quarterlyNumber" @click.stop="quarterlygn()"></p>
                         <img src="../../../webapp/images/bell-icon.png" @click.stop="quarterlygn()"/>
                         <div v-show="isnotification" class="isnotification">
                        <ul>
                        <h3 v-if="quarterlyArray.length == 0 " style="text-align:center;color:black;margin:0 auto;width:80%;margin-top:60px;">&nbsp;&nbsp;&nbsp;您暂时没有未通知消息</h3>
                        <img src="../../../webapp/images/regret.png" v-if="quarterlyArray.length == 0 " style="width:128px;height:128px;margin:20px auto;display:block;" />
                        <li v-for="item in quarterlyArray">
                           <div class="content">
                           <span v-text="item.company" class="company" @click="joindashboard_(item,item.up_id)"></span>&nbsp;
                           <span v-text="item.title" class="span _span"></span>&nbsp;
                           <span class="span" v-show="item.analyst_notify == ''" v-text="lang=='zh_CN'?'将于':'will be released on'"></span>
                           <span v-text="item.date" class="span _span" v-show="item.analyst_notify == ''"></span>&nbsp;
                           <span class="span" v-show="item.analyst_notify == ''" v-text="lang=='zh_CN'?'公布':''"></span>
                           <span class="span" v-text="item.analyst_notify"></span>
                           <span class="span" v-text="lang=='zh_CN'?'。':'.'"></span>
                           </div>
                           <div class="time" v-text="item.update_time" :class="{timemargin:item.analyst_notify != ''}"></div>
                        </li>
                        </ul>
                    </div>
                   </div> 
                </div>
            </div>
            <div class="form-wrapper">
                <div class="item-1180">
                    <div class="tooltip_daily" v-show="loginway == 0" :class="{height:lang=='zh_CN',height_:lang=='en'}">
                        <h5 v-text="lang=='zh_CN'?'Data Daily是什么?':'What is Data Daily?'"></h5>
                        <p v-text="lang=='zh_CN'?'data日报是EVERSIGHT.AI基于全网大数据分析，对目标公司的日常经营数据进行重点监测，当目标公司经营数据发生显著变化时第一时间发布，以帮助投资者掌握最新的投资机会。':'Data Daily is built to deliver the latest investment opportunities to our clients. Utilizing big data analysis, Eversight.AI monitors target companies’ daily operating data and generates alerts whenever there are significant changes.'">
                        </p>
                    </div>
                     <!--季报日历 -->
                    <div class="tooltip_quarterly" v-show="loginway == 0">
                        <table class="first-table">
                            <tr>
                                <td>
                                <select @change="changTime($event)">
                                    <option value="" v-text="lang=='zh_CN'?'最新季报':'Latest'"></option>
                                    <option v-for="item in pulldownOne" v-text="item" :value="item"></option>
                                </select>
                                </td>
                                <td>
                                <select class="select" @change="changeCompany($event)">
                                     <option value="" v-text="lang=='zh_CN'?'全部公司':'Ticker'"></option>
                                     <option v-for="item in pulldownTwo" v-text="item.name" :value="item.id"></option>
                                </select>
                                </td> 
                                <td style="text-align:center;font-size:12px;color:#ffffff;" v-text="lang=='zh_CN'?' 盘前/盘后':'Time'">
                                </td>   
                            </tr>
                        </table>
                        <div :class="{divborder:quarterlyData.length>=5}">
                            <table class="second-table" :class="{tableheight:quarterlyData.length>=0 && quarterlyData.length<5}">
                                <h4 v-show="quarterlyData.length==0">暂 无 季 报 日 历 信 息</h4>
                                    <tr v-for="item in quarterlyData">
                                        <td v-text="item.date"></td>
                                        <td v-text="item.company"></td> 
                                        <td v-text="item.type"></td>    
                                    </tr>
                            <!--  <tr>
                                <td>1</td>
                                <td>2</td> 
                                <td>3</td> 
                             </tr> -->
                            </table> 
                        </div>
                    </div>
                    <!--日报-->
                    <div class="daily" v-show="loginway == 0">
                        <div class="tab_bar">
                            <div class="huang_btn" @click="huang=0" v-text="lang=='zh_CN'?'大数据日报':'Data daily'"></div>
                            <!--<div class="huang_btn" @click="huang=1" :class="{active:huang==1}">港 股</div>
                            <div class="huang_btn" @click="huang=2" :class="{active:huang==2}">A 股</div>-->
                            <!--<span class="huang_underline"
                                  :class="{at_Left:huang==0,at_Center:huang==1,at_Right:huang==2}"></span>-->
                        </div>
                        <div class="search_div">
                            <input class="stock_search" type="search" :placeholder="lang=='zh_CN'?'搜索相关股票信息':'Search stock info'" @keyup.enter="img_click()"/>
                            <img src="../../../webapp/images/icon_delete.png" class="icon_delete" @click="imgclick()"/>
                            <img src="../../../webapp/images/search.png" class="icon_search" @click="img_click()"/>
                        </div>
                        <div class="daily_content">
                                <p class="daily-title" v-show="expired_?false:expired">
                                您收看Data Daily的权限已过期，如需收看最新日报请<i @click="joinorder()">点击订阅</i><span @click="gn()"> X </span>
                                </p>
                            <ul id="itemContainer" class="content_ul">
                                    <li v-for="(item,index) in dailyArr">
                                    <div class="left_div">
                                        <p v-text="item.update_time"></p>
                                    </div>
                                    <div class="right_div">
                                        <span class="myspan" v-show="item.update_time != ''"></span>
                                        <p :class="{huangjian_p:item.update_time != '',huangjian_p_:item.update_time == ''}" style="font-size: 14px;"> 
                                            <span class="span" v-text="item.product_name" @click="joindashboard(item,item.up_id)"></span>
                                            <span class="span" v-text="item.product_code?'('+item.product_code+') : ':''" @click="joindashboard(item,item.up_id)"></span>
                                            <i v-text="item.report"></i>
                                        </p>
                                    </div>
                                    </li>
                            </ul>
                            <!--分页-->
                            <div class="holder">
                                
                            </div> 
                        </div>
                    </div>
                    <!-- 提示框 -->
                   
                    <div class="hini-box" v-if="status==1">
                        <h4 v-show="buy==1">
                        <img src="../../../webapp/images/tishigantan.png" style="width:15px;height:15px;margin-top:-3px;" />&nbsp;
                        该公司看板已经过期，如需查看请<span @click="directorder()">点击购买</span></h4>
                        <h4 v-show="nobuy==1">
                        <img src="../../../webapp/images/tishigantan.png" style="width:15px;height:15px;margin-top:-3px;" />&nbsp;您尚未购买该公司看板，如需查看请<span @click="directorder()">点击购买</span></h4>
                    </div>
                         <!-- 日报提示 -->
                    
                    <!--看板-->
                    <div class="huang-dashboard" v-show="loginway == 1">
                            <!--<div class="item_box">-->
                            	  <!--<p class="item-title" v-show="jhuang">&nbsp;&nbsp;&nbsp;&nbsp;您暂未开通EVERSIGHT看板权限，如需使用请&nbsp;<i
                                    @click="joinorder()">点击订阅</i>，或发送邮件至&nbsp;service@Eversight.ai&nbsp;申请试用版权限。<span
                                    @click="fn()">X</span></p>
                                  <p class="item-title_" v-show="pastdue">&nbsp;&nbsp;&nbsp;&nbsp;您查看Data Dashboard的权限已到期，如需查看最新Data请&nbsp;<i @click="joinorder()">点击订阅</i>
                            	&nbsp;&nbsp;&nbsp;&nbsp;<span @click="wn()">X</span></p>-->
                            <!--</div>-->
                        <!--tab切换看板-->
                        <div class="dashboard_tab_bar">
                        	<div class="dashboard_switch_btn" @click="dashboard=0" :class="{active_dashboard:dashboard==0,active_dashboard_:dashboard!==0}" v-text="lang=='zh_CN'?'A股市场':'Chinese Stock Market'"></div>
                            <div class="dashboard_switch_btn" @click="dashboard=1" :class="{active_dashboard:dashboard==1,active_dashboard_:dashboard!==1}" v-text="lang=='zh_CN'?'美股市场':'US Stock Market'">美股市场</div>
                        </div>
                        <p class="item-title" v-show="jhuang">&nbsp;&nbsp;&nbsp;&nbsp;您暂未开通EVERSIGHT看板权限，如需使用请&nbsp;<i
                                    @click="joinorder()">点击订阅</i>，或发送邮件至&nbsp;service@Eversight.ai&nbsp;申请试用版权限。<span
                                    @click="fn()">X</span></p>
                                  <p class="item-title_" v-show="pastdue">&nbsp;&nbsp;&nbsp;&nbsp;您查看Data Dashboard的权限已到期，如需查看最新Data请&nbsp;<i @click="joinorder()">点击订阅</i>
                            	&nbsp;&nbsp;&nbsp;&nbsp;<span @click="wn()">X</span></p>
                        <ul class="proList">
                            <li v-for="(pro,index) in products"
                                :class="pro.status==0?'nobuyli':(pro.status==4?'nohave':'')"
                                @click="pro.status==2?showDashborad(pro):''"
                                :style="pro.status==3?'cursor:default;':''">

                                <img v-show="pro.status == 0||pro.status == 4"
                                     :src="pro.image_url_nocolor.replace('/logo/','/images/')">
                                <img v-show="pro.status ==2 " 
                                	 :src="pro.image_url.replace('/logo/','/images/')">
                                <span class="product_status" @click.stop v-if="pro.remaining<=7&&pro.status==2">Remaining {{pro.remaining}} days, please <a
                                        href="/#/order">renew</a></span>
                                <span class="product_status" @click.stop v-if="pro.status==3">Expired,please <a
                                        href="/#/order">renew</a></span>
                                <div class="buyShadow" v-if="pro.status==0||pro.status==4">
                                    <span class="btn-link" @click="direct('order',pro)"
                                          v-if="pro.status==0">BUY NOW</span>
                                    <span class="btn-link" @click="requestDemo(pro)"
                                          v-if="pro.status==4">REQUEST A DEMO</span>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="order-footer clearfix item-1180" v-show="loginway == 1">
                    <div class="copyright">
                        Copyright &copy 2016 - 2017 Eversight.AI. All Rights Reserved.
                    </div>
                    <div class="contact">
                        <a class="contact-email" href="mailto:service@modeling.ai">Email:service@Eversight.ai</a>
                        <!-- <span class="contact-num">Phone number:010-85565</span> -->
                    </div>
                </div>
            </div>
        </div>
    </transition>
</template>
<script>
    import UserBar from '../common/userbar.vue'

    export default {
        name: 'order',
        components: {
            UserBar
        },
        data() {
            return {
                isShowUserBar: false,
                products: [],
                jhuang: false,
                loginway: 0,
                li_langchange: 0,
                huang: 0,
                sort:"date",
                sort_:"quarterly_report.pub_date",//用于季报接口
                order:"desc",
                order_:"asc",
                offset:0,
                limit:99999,
                search_val:"",
                dailyArr:[],
                expired:false,
                pastdue:false,
                huangj:0,
                orderlist:[],
                dashboard:0,
                a:"A",
                m:"M",
                allproductlist:[],
                category:"",
                quarterly:null,
                quarterlyArray:[],
                quarterlyNumber:null,
                isnotification:false,
                pulldownOne:[],
                pulldownTwo:[],
                date:'',
                pid:'',
                quarterlyData:[],
                status:0,
                buy:0,
                nobuy:0
            }
        },
        mounted(){
            GaHelper.sendPageView();
        	var self = this;
            //console.log(self.lang);
            //console.log(self.manager);
            //判断登录状态回到首页之前的lang类型保持一致
            if(self.lang == 'en'){
               self.li_langchange = 1; 
            }
            U.init((bol) => {
                if (bol) {
                    $(window).scrollTop(0);
                    self.getMyproducts();
                } else {
                    // self.$router.push({name: 'login'});
                    self.$router.push({name: 'my'});
                }
            })
            //获取中概股份日报信息
            $.get({
            	url:"/api/v1/reports"+"?sort="+self.sort+"&order="+self.order + "&offset="+self.offset+"&limit="+self.limit + "&lang="+self.lang,
            	contentType:'application/json;charset=utf-8',
            	success:function(res){
//          		console.log(res);
            		if(res.data.expired == true){
            			self.expired = true;
            		}
            		self.dailyArr = res.data.list;
            		self.$nextTick(function(){
            		$('div.holder').jPages({  
		            containerID: 'itemContainer',  
		            first:false,//false为不显示  
		            previous: '前一页',//false为不显示  
		            next: '下一页',//false为不显示 自定义按钮  
		            last:false,//false为不显示  
		            perPage:100,//每页最多显示几个  
		            //keyBrowse: true,键盘切换  
		            //scrollBrowse: true滚轮切换  
		            callback: function(pages, items) {  
//		                console.log(pages);  
//		                console.log(items);
		                $("html,body").animate({scrollTop:0},500);
		            },   
		        });  
	            //跳转到某一页  
	            $('.goPage').on('click', function(){
	                 $(".holder").jPages(5);
	                });
                if(self.lang == "en"){
                    $(".jp-previous").html("Previous page");
                    $(".jp-next").html("Next page")
                    }
	              })
            	},
            	error:function(error){
            	  console.log(error)
            	}
            	
            })
            //判断status
            if(localStorage.getItem("status") && localStorage.getItem("dashboard")){
            	if(localStorage.getItem("dashboard") == 1){
            		localStorage.removeItem("dashboard");
	            	self.loginway = 1;
	            	self.dashboard = 1;
            	}else if(localStorage.getItem("dashboard") == 0){
                   localStorage.removeItem("dashboard");
	            	self.loginway = 1;
	            	self.dashboard = 0;
            	}
            }else{
            	self.loginway = 0;
            }
            //获取最新未读订单通知
            $.get({
            	url:'api/v1/orders/lastest',
            	contentType:'application/json;charset=utf-8',
            	cache:false,
            	success:function(res){
            		if(res.data.list.length !== 0){
            			localStorage.setItem("price",res.data.list[0].price);
            			localStorage.setItem("plan",res.data.list[0].plan);
            		}
            		if(res.total !== 0){
            			self.orderlist = res.data.list;
            			for(let i= 0;i<self.orderlist.length;i++){
            				if(self.orderlist[i].status == 1){
            					self.$router.push({name: 'order-finish'});
            				}else if(self.orderlist[i].status == 2){
            					self.$router.push({name: 'orderfailed'});
            				}
            			}
            		}
            	}
            })
            
            //监听input搜索框
            $('.stock_search').bind('input propertychange', function() {  
            	if($(".stock_search").val().trim() == ''){
            		$(".icon_delete").hide();
            		self.img_click();
            	}
		    });
               //获取所有看板list
             setTimeout(function(){
             $.get({
                    url: U.getApiPre() + '/api/v1/user-products',
                    contentType: 'application/json;charset=utf-8',
                    cache:false,
                    success: (resp) => {
                        if (resp.success) {
                            self.allproductlist = resp.data.list;
                        }else{
                            new PNotify({
                                text: resp.message || '获取产品列表失败',
                                type: 'error'
                            })
                        }
                    },
                    error: () => {
                        new PNotify({
                            text: '获取产品列表失败',
                            type: 'error'
                        })
                    }
                })
            },300)
             //获取季报消息提示信息
                 if(localStorage.getItem("quarterly")==1){
                    localStorage.setItem("quarterly",1);
                 }else{
                    localStorage.setItem("quarterly",0);
                 }
                 $.get({
                        url:'/api/v1/reports/calendar-notify?lang=' + self.lang + '&quarterly=' + localStorage.getItem("quarterly"),
                        contentType: 'application/json;charset=utf-8',
                        success: (resp) => {
                            if (resp.success){
                                if(resp.data.list.length != 0){
                                     self.quarterly = 0;
                                     self.quarterlyNumber = resp.data.total;
                                     self.quarterlyArray = resp.data.list;
                                }else if(resp.data.list.length == 0){
                                     self.quarterly = 1;
                                }
                            }else{
                                new PNotify({
                                    text:'获取季报消息提醒失败',
                                    type: 'error'
                                })
                            }
                        },
                        error: () => {
                            new PNotify({
                                text: '获取季报消息提醒失败',
                                type: 'error'
                            })
                        }
                })
            //获取季报下拉单
            $.get({
                url:'/api/v1/reports/calendar-head?lang=' + self.lang,
                contentType: 'application/json;charset=utf-8',
                success:function(resp){
                   self.pulldownOne = resp.data.dates;
                   self.pulldownTwo = resp.data.names;
                },
                error:function(error){
                    console.log(error);
                }
            })
            //获取季报表格数据
            $.get({
                url:'/api/v1/reports/calendar?date=' + self.date + '&pid=' + self.pid + '&sort=' + self.sort_ + '&order='+ self.order_ + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
                contentType: 'application/json;charset=utf-8',
                success:function(resp){
                    if(resp.success){
                        self.quarterlyData = resp.data.list;
                    }else{
                        self.quarterlyData = new Array;
                    }
                },
                error:function(error){
                    console.log(error);
                }

            })
        },
        watch:{
        	dashboard(newValue,old){
        		this.category = newValue;
        		if(newValue == 1){
        		$.get({
                    url: U.getApiPre() + '/api/v1/user-products?category=' + this.m,
                    contentType: 'application/json;charset=utf-8',
                    cache:false,
                    success: (resp) => {
                        if (resp.success) {
                            this.products = resp.data.list;
                            this.getcategory_m();
                        }else{
                            new PNotify({
                                text: resp.message || '获取产品列表失败',
                                type: 'error'
                            })
                        }
                    },
                    error: () => {
                        new PNotify({
                            text: '获取产品列表失败',
                            type: 'error'
                        })
                    }
                })
        }else if(newValue == 0){
        		$.get({
                    url: U.getApiPre() + '/api/v1/user-products?category=' + this.a,
                    contentType: 'application/json;charset=utf-8',
                    cache:false,
                    success: (resp) => {
                        if (resp.success) {
                            this.products = resp.data.list;
                            this.getcategory_a();
                        }else{
                            new PNotify({
                                text: resp.message || '获取产品列表失败',
                                type: 'error'
                            })
                        }
                    },
                    error: () => {
                        new PNotify({
                            text: '获取产品列表失败',
                            type: 'error'
                        })
                    }
                })
        		}
        	},
            li_langchange(newvalue){
                if(newvalue == 1){
                var self = this;
                this.$store.commit("hj_changelang","en");
                $.get({
                url:"/api/v1/reports"+"?sort="+self.sort+"&order="+self.order + "&offset="+self.offset+"&limit="+self.limit + "&lang="+self.lang,
                contentType:'application/json;charset=utf-8',
                success:function(res){
//                  console.log(res);
                    if(res.data.expired == true){
                        self.expired = true;
                    }
                    self.dailyArr = res.data.list;
                    self.$nextTick(function(){
                    $('div.holder').jPages({  
                    containerID: 'itemContainer',  
                    first:false,//false为不显示  
                    previous: '前一页',//false为不显示  
                    next: '下一页',//false为不显示 自定义按钮  
                    last:false,//false为不显示  
                    perPage:100,//每页最多显示几个  
                    callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                    },   
                });  
                //跳转到某一页  
                $('.goPage').on('click', function(){
                     $(".holder").jPages(5);
                    });
                if(self.lang == "en"){
                    $(".jp-previous").html("Previous page");
                    $(".jp-next").html("Next page")
                    }
                  })
                },
                error:function(error){
                  console.log(error)
                }
                
                })
                 }else if(newvalue == 0){
                var self = this;
                this.$store.commit("hj_changelang","zh_CN");
                $.get({
                url:"/api/v1/reports"+"?sort="+self.sort+"&order="+self.order + "&offset="+self.offset+"&limit="+self.limit + "&lang="+self.lang,
                contentType:'application/json;charset=utf-8',
                success:function(res){
                    if(res.data.expired == true){
                        self.expired = true;
                    }
                    self.dailyArr = res.data.list;
                    self.$nextTick(function(){
                    $('div.holder').jPages({  
                    containerID: 'itemContainer',  
                    first:false,//false为不显示  
                    previous: '前一页',//false为不显示  
                    next: '下一页',//false为不显示 自定义按钮  
                    last:false,//false为不显示  
                    perPage:100,//每页最多显示几个  
                    callback: function(pages, items) {  
                        $("html,body").animate({scrollTop:0},500);
                    },   
                });  
                $('.goPage').on('click', function(){
                     $(".holder").jPages(5);
                    });
                if(self.lang == "en"){
                    $(".jp-previous").html("Previous page");
                    $(".jp-next").html("Next page")
                    }
                  })
                },
                error:function(error){
                  console.log(error)
                }
                
                })
              }
            },
            lang(newvalue){
                 var self = this;
            //获取季报消息提示信息
               if(localStorage.getItem("quarterly")==1){
                    localStorage.setItem("quarterly",1);
                 }else{
                    localStorage.setItem("quarterly",0);
                 }
                 $.get({
                        url:'/api/v1/reports/calendar-notify?lang=' + newvalue + '&quarterly=' + localStorage.getItem("quarterly"),
                        contentType: 'application/json;charset=utf-8',
                        success: (resp) => {
                            if (resp.success){
                                if(resp.data.list.length != 0){
                                     self.quarterly = 0;
                                     self.quarterlyNumber = resp.data.total;
                                     self.quarterlyArray = resp.data.list;
                                }else if(resp.data.list.length == 0){
                                     self.quarterly = 1;
                                }
                            }else{
                                new PNotify({
                                    text:'获取季报消息提醒失败',
                                    type: 'error'
                                })
                            }
                        },
                        error: () => {
                            new PNotify({
                                text: '获取季报消息提醒失败',
                                type: 'error'
                            })
                        }
                })
            //获取季报下拉单
            $.get({
                url:'/api/v1/reports/calendar-head?lang=' + newvalue,
                contentType: 'application/json;charset=utf-8',
                success:function(resp){
                   self.pulldownOne = resp.data.dates;
                   self.pulldownTwo = resp.data.names;
                },
                error:function(error){
                    console.log(error);
                }
            })
            //获取季报表格数据
            $.get({
                url:'/api/v1/reports/calendar?date=' + self.date + '&pid=' + self.pid + '&sort=' + self.sort_ + '&order='+ self.order_ + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + newvalue,
                contentType: 'application/json;charset=utf-8',
                success:function(resp){
                  if(resp.success){
                     self.quarterlyData = resp.data.list;
                 }else{
                     self.quarterlyData = new Array;
                 }
                },
                error:function(error){
                    console.log(error);
                }

            })
            }
        },
        computed:{
            hj() {
                return this.$store.state.dashboard.hj;
            },
            h_j() {
                return this.$store.state.dashboard.h_j;
            },
            expired_(){
            	return localStorage.expired;
            },
            pastdue_(){
            	return localStorage.pastdue;
            },
            status_(){
            	return localStorage.status;
            },
            lang(){
	        	return this.$store.state.my.lang;
	        },
            isnotificationstate(){
                return this.$store.state.my.isnotificationstate;
            },
            manager(){
                return localStorage.manager;
            }
        },
        methods: {
            showDashborad(pro){
            	if(this.dashboard == 1){
            		localStorage.setItem("dashboard",1);
            	}else{
            		localStorage.setItem("dashboard",0);
            	}
                localStorage.setItem("dashboard-name",pro.name);
                //GA监测进入哪些看板，pro.name为看板名字
                GaHelper.sendEvent(GaHelper.Usercenter.read, pro.name);
                // 重置看板时间
                this.$store.commit('zk_setCurDay', '');
                // 存储看板语种
                sessionStorage.setItem('_l', JSON.stringify(pro.lang || []));
                localStorage.setItem("status",true);
                let lang = pro.lang && pro.lang instanceof Array && pro.lang.length === 1 && pro.lang[0];
                if (lang) {
                    this.$store.commit("zk_setLanguage", lang);
                }
                this.$router.push({
                    name: 'dashboard',
                    params: {
                        bid: pro.id
                    }
                })
            },
            direct(target, pro){
            	if(target == 'order'){
            		localStorage.setItem("status",true);
            	}
                if (pro) {
                    //GA监测购买哪些看板（BUY NOW），pro.name为看板名字
                    GaHelper.sendEvent(GaHelper.Usercenter.buyDashboard, pro.name);
                }
                this.$router.push({
                    name: target
                })
            },
            getMyproducts() {
                var self = this;
                let _load = layer.load(2);
                if(this.category == 0 || this.category == ""){
                	$.get({
                    url: U.getApiPre() + '/api/v1/user-products?category=' + self.a,
                    contentType: 'application/json;charset=utf-8',
                    cache:false,
                    success: (resp) => {
                        if (resp.success) {
                            self.products = resp.data.list;
                        }else{
                            new PNotify({
                                text: resp.message || '获取产品列表失败',
                                type: 'error'
                            })
                        }
                    },
                    error: () => {
                        new PNotify({
                            text: '获取产品列表失败',
                            type: 'error'
                        })
                    },
                    complete: () => {
                        layer.closeAll();
                        layer.close(_load);
                    }
                })
                }else if(this.category == 1){
                	$.get({
                    url: U.getApiPre() + '/api/v1/user-products?category=' + self.m,
                    contentType: 'application/json;charset=utf-8',
                    cache:false,
                    success: (resp) => {
                        if (resp.success) {
                            self.products = resp.data.list;
                        }else{
                            new PNotify({
                                text: resp.message || '获取产品列表失败',
                                type: 'error'
                            })
                        }
                    },
                    error: () => {
                        new PNotify({
                            text: '获取产品列表失败',
                            type: 'error'
                        })
                    },
                    complete: () => {
                        layer.closeAll();
                        layer.close(_load);
                    }
                })
                }
            },
            showUserBar() {
                var self = this;
                this.isShowUserBar = true;
                setTimeout(function () {
                    $("html").click(function () {
                        if (!self.isShowUserBar) {
                            return;
                        }
                        self.isShowUserBar = false;
                        $("html").unbind('click');
                    })
                }, 0)
            },
            logOut() {
                U.logout();
            },
            requestDemo(pro) {
                 //GA监测REQUEST DEMO，pro.name为看板名字
                GaHelper.sendEvent(GaHelper.Usercenter.request, pro.name);
                this.$router.push({
                    name: 'dashboard',
                    params: {
                        bid: 'demo'
                    }
                })
            },
            joinorder(){
                 this.$router.push({
                 	name:"order"
                 })
            },
            //删除搜索框
            imgclick(){
            	$(".load-mask").show();
            	$(".loader").show();
            	var self = this;
            	$(".icon_delete").hide();
            	$(".stock_search").val("");
            	$.get({
            	url:"/api/v1/reports?sort="+self.sort+"&order="+self.order+"&offset="+self.offset+"&limit="+self.limit + "&lang="+self.lang,
            	contentType:'application/json;charset=utf-8',
            	success:function(res){
            		if(res.data.expired == true){
            			self.expired = true;
            		}
            		$(".holder").show();
            		self.dailyArr = res.data.list;
            		self.$nextTick(function(){
            		$(".load-mask").hide();
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
		                console.log(pages);  
		                console.log(items);
		                $("html,body").animate({scrollTop:0},500);
		            },   
		        });  
	            //跳转到某一页  
	            $('.goPage').on('click', function(){
	                 $(".holder").jPages(5);
	                });
                if(self.lang == "en"){
                    $(".jp-previous").html("Previous page");
                    $(".jp-next").html("Next page")
                    }
	              })
            	},
            	error:function(error){
            	  console.log(error)
            	}
            	
            })
            },
            //搜索
            img_click(){
            	var self = this;
            	self.search_val = $('.stock_search').val();
            	$.get({
            	url:"/api/v1/reports?sort="+self.sort+"&order="+self.order+"&q="+self.search_val+"&offset="+self.offset+"&limit="+self.limit + "&lang="+self.lang,
            	contentType:'application/json;charset=utf-8',
            	success:function(res){
            		$(".holder").show();
            		if(self.search_val != ""){
            			$(".icon_delete").show();
            		}
            		if(res.data.expired == true){
            			self.expired = true;
            		}
            		if(res.data.total == 0){
            			layer.msg("暂时没有您搜索的相关内容");
            			$(".holder").hide();
            		}
            		self.dailyArr = res.data.list;
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
		            callback: function(pages, items){
		                console.log(pages); 
		                console.log(items);
		                $("html,body").animate({scrollTop:0},500);
		            },   
		            });  
	                //跳转到某一页  
	                $('.goPage').on('click', function(){
	                 $(".holder").jPages(5);
	                });
                    if(self.lang == "en"){
                    $(".jp-previous").html("Previous page");
                    $(".jp-next").html("Next page")
                    }
	              })
            	},
            	error:function(error){
            	  console.log(error)
            	}
              })
            },
            joindashboard(pro,dashboard_id){
                // console.log(dashboard_id);
                for(var i=0;i<this.allproductlist.length;i++){
                  if(dashboard_id == this.allproductlist[i].id && this.allproductlist[i].remaining !== 0){
                  	this.huangj = 1;
                    break;
                	}else{
                	this.huangj = 2;
                	}
                }
                if(this.huangj == 1){
                     localStorage.setItem("dashboard-name",pro.product_name);
                      //从日报进入
                     localStorage.setItem("dailyjoin",pro.chart_id);
                     //GA监测进入哪些看板，pro.name为看板名字
                	 GaHelper.sendEvent(GaHelper.Usercenter.go, pro.product_name);
	                // 重置看板时间
	                this.$store.commit('zk_setCurDay', '');
	                // 存储看板语种
	                sessionStorage.setItem('_l', JSON.stringify(pro.lang || []));
	                let lang = pro.lang && pro.lang instanceof Array && pro.lang.length === 1 && pro.lang[0];
	                if (lang) {
	                    this.$store.commit("zk_setLanguage", lang);
	                }
                    this.$router.push({
                    name: 'dashboard',
                    params: {
                        bid: dashboard_id
                    }
                    })
                }else if(this.huangj == 2){
                	this.loginway = 1;
                    //判断看板类型是A股美股
                    if(pro.type == "A"){
                        this.dashboard = 0;
                    }else if(pro.type == "M"){
                        this.dashboard = 1;
                    }
                	$("html,body").animate({scrollTop:0},100);
                }
            },
            joindashboard_(pro,id){
                    var self = this;
                    sessionStorage.setItem('_l', JSON.stringify(pro.lang || []));
                    let lang = pro.lang && pro.lang instanceof Array && pro.lang.length === 1 && pro.lang[0];
                    if (lang) {
                        self.$store.commit("zk_setLanguage", lang);
                    }
                    //季报跳转看板
                    if(pro.remaining != 0){
                       self.$router.push({
                       name: 'dashboard',
                       params: {
                        bid: id
                       }
                    })
                    }else if(pro.remaining==0){
                       if(pro.status==0){
                        self.status=1;
                        self.displayself();
                        self.nobuy=1
                        }else if(pro.status==3){
                        self.status=1;
                        self.displayself();
                        self.buy=1;
                        }
                        
                    }
            },
            displayself(){
               var self = this;
               setTimeout(function(){
                self.status=0;
               },5000)
            },
            gn(){
            	var self = this;
            	self.expired = false;
            	localStorage.setItem("expired", self.expired);
            },
            fn(){
                var self = this;
                self.jhuang = false;
                self.$store.commit('hj_setcategory',1);
            },
            wn(){
            	var self = this;
                self.pastdue = false;
                self.$store.commit('hj_setcategory_1',1);
            },
            getcategory_m(){
            	var self = this;
            	$.get({
            	url:"/api/v1/user-products/expiry?category=M",
            	contentType:'application/json;charset=utf-8',
            	cache:false,
            	success:function(res){
            		console.log(res);
            		if(res.status == 0){
	        			if(self.hj == 1){
	    			        self.jhuang = false;
	    			     }else{
	    			     	self.jhuang = true;
	    			     }
            		}else if(res.status == 1){
            			if(self.h_j == 1){
	    			        self.pastdue = false;
	    			     }else{
	    			     	self.pastdue = true;
	    			     }
            		}else if(res.status == 2){
            			self.jhuang = false;
            			self.pastdue = false;
            		}
	            	},
	            error:function(error){
	            		console.log(error)
	            	}
	                })
            },
            getcategory_a(){
            	var self = this;
            	$.get({
            	url:"/api/v1/user-products/expiry?category=A",
            	contentType:'application/json;charset=utf-8',
            	cache:false,
            	success:function(res){
            		console.log(res);
            		if(res.status == 0){
            			if(self.hj == 1){
	    			        self.jhuang = false;
	    			     }else{
	    			     	self.jhuang = true;
	    			     }
            		}else if(res.status == 1){
            			if(self.h_j == 1){
	    			        self.pastdue = false;
	    			     }else{
	    			     	self.pastdue = true;
	    			     }
            		}else if(res.status == 2){
            			self.jhuang = false;
            			self.pastdue = false;
            		}
	            	},
	            error:function(error){
	            		console.log(error)
	            	}
	                })
            },
            quarterlygn(){
                localStorage.setItem("quarterly",1);
                var self = this;
                self.quarterly = 1;
                self.isnotification = true;
                self.$nextTick(function(){
                    $("html").click(function(){
                        if(!self.isnotification){return;}
                        self.isnotification = false;
                        $("html").unbind('click');
                    })
                })
            },
            changTime(e){
                var self = this;
                self.date = e.target.value;
                $.get({
                url:'/api/v1/reports/calendar?date=' + self.date + '&pid=' + self.pid + '&sort=' + self.sort_ + '&order='+ self.order_ + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
                contentType: 'application/json;charset=utf-8',
                success:function(resp){
                    if(resp.success){
                        self.quarterlyData = resp.data.list;
                    }else{
                        self.quarterlyData = new Array();
                    }
                },
                error:function(error){
                    console.log(error);
                }

            })
            },
            changeCompany(e){
                var self = this;
                self.pid = e.target.value;
                $.get({
                url:'/api/v1/reports/calendar?date=' + self.date + '&pid=' + self.pid + '&sort=' + self.sort_ + '&order='+ self.order_ + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
                contentType: 'application/json;charset=utf-8',
                success:function(resp){
                    if(resp.success){
                        self.quarterlyData = resp.data.list;
                    }else{
                        self.quarterlyData = new Array();
                    }
                },
                error:function(error){
                    console.log(error);
                }
              })
            },
            directorder(){
                var self = this;
                self.$router.push({
                    name:'order'
                })
            }
        },
    }
</script>
<style type="text/css" scoped>
    /*头部*/
    .order-head {
        height: 100px;
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

   .item-1180 .item_box {
        width: 100%;
        height: 60px;
        line-height: 60px;
        margin-top: 15px;
        text-align: center;
    }
    .item-1180 .item-title {
        font-size: 14px;
        font-weight: bold;
        display: table;
        width: auto;
        height: 36px;
        line-height: 36px;
        text-align: center;
        margin: 0 auto;
        background: #10A7D8;
        color: #FFFFFF;
        margin-top: 20px;
    }

    .item-1180 .item-title i {
        font-style: normal;
        color: #ffffff;
        cursor: pointer;
        text-decoration: underline;
    }
    .item-1180 .item-title span {
        cursor: pointer;
        color: #FFFFFF;
        display: block;
        float: right;
        width: 10px;
        height: 36px;
        line-height: 36px;
        text-align: center;
        margin-right: 10px;
    }
    .item-1180 .item-title_ {
        font-size: 14px;
        font-weight: bold;
        display: table;
        width: auto;
        height: 36px;
        line-height: 36px;
        text-indent: 5px;
        margin: 0 auto;
        background: #10A7D8;
        color: #FFFFFF;
        margin-top: 20px;
    }

    .item-1180 .item-title_ i {
        font-style: normal;
        color: #ffffff;
        cursor: pointer;
        text-decoration: underline;
    }
    .item-1180 .item-title_ span {
        cursor: pointer;
        color: #FFFFFF;
        display: block;
        float: right;
        width: 10px;
        height: 36px;
        line-height: 36px;
        text-align: center;
        margin-right: 10px;
    }
    .order-nav-bar {
        height: 70px;
        margin: 0 60px;
    }
    .logo{
        width: 70px;
        height: 70px;
        background-position: 0 -546px;
        float: left;
    }

    .icon-login {
        width: 10px;
        height: 12px;
        display: inline-block;
        vertical-align: 0px;
        margin-right: 10px;
        background-position: 0px -445px;
    }

    .order-footer {
        line-height: 100px;
        color: #797979;
        border-top: 1px solid #eee;
        font-size: 12px;
    }

    .order-footer .copyright {
        float: left;
    }

    .order-footer .contact {
        float: right;
        padding-right: 10px;
    }

    .order-footer .contact .contact-email {
        color: #797979;
    }

    .order-footer .contact .contact-num {
        color: #797979;
        margin-left: 20px;
    }
    .hini-box{
        width:26%;
        height: 40px;
        line-height: 40px;
        position: fixed;
        top: 110px;
        left: 35%;
        background:#ffffff;
        z-index: 999;
        -webkit-box-shadow:10px 10px 10px gray,0px 10px 10px gray;  
        -moz-box-shadow:10px 10px 10px gray,0px 10px 10px gray;  
        box-shadow:10px 10px 10px gray,0px 10px 10px gray;

    }
    .hini-box h4{
        width: 100%;
        display: block;
        height: 30px;
        line-height: 30px;
        margin: 5px 0;
        text-align: center;
        font-size: 15px !important;
    }
    .hini-box h4 span{
        cursor: pointer;
        text-decoration: underline;
        color: deepskyblue;
    }
    .tooltip_daily{
    	-webkit-user-select: none;
    	-moz-user-select: none;
    	-ms-user-select: none;
    	width: 300px;
        margin-right: -70px;
    	float: right;
    	margin-top: 70px;
    	border:1px solid #C0C0C0;
    }
    .height{
       height: 160px !important;
    }
    .height_{
       height: 200px !important; 
    }
    .tooltip_daily h5{
    	width: 80%;
    	color:gray;
    	font-weight:bold;
    	font-size: 14px;
    	margin: 10px auto;
    }
    .tooltip_daily p{
    	width:80%;
    	color:gray;
    	display: block;
    	margin: 10px auto;
    	font-size: 14px;
        word-break:break-all;
    }
    .tooltip_quarterly{
        width: 300px;
        height: 258px;
        overflow-y:auto; 
        float: right;
        margin-top: 30px;
        margin-right: -70px;
        /*liu改*/
        clear: both;
        /*border:2px solid #C0C0C0;*/
    }
    .tooltip_quarterly .first-table{
         width: 100%;
         height: 40px;
         line-height: 40px;
         border-collapse: collapse;
    }
    .tooltip_quarterly .first-table tr{
        height: 40px;
        line-height: 40px;
        width: 100%;
        background: deepskyblue;
        border: 1px solid #C0C0C0;
    }
    .tooltip_quarterly .first-table  tr td{
         width:33.33%; 
         border: 1px solid #C0C0C0;
    }
    .tooltip_quarterly .first-table  tr  select{
        width:100%; 
        height:40px;
        line-height:40px;
        font-size:12px;
        padding: 0 4%;
        color: #ffffff;
    }
    .tooltip_quarterly .first-table  tr  select option{
        color: #000000;
    }
    .tooltip_quarterly div{
        width: 100%;
        height: 211px;
        overflow-y:auto;
    }
    .divborder{
        border-bottom: 1px solid #C0C0C0;
    }
    .tooltip_quarterly .second-table{
         width: 100%;
         height: 202px;
         min-height:202px;
         border-collapse: collapse;
         table-layout: fixed;
    }
    .tableheight{
        min-height:0 !important;
        height: 0 !important;
    }
    .tooltip_quarterly .second-table h4{
        width: 100%;
        height: 50px;
        line-height: 50px;
        display: table;
        text-align: center;
        border:1px solid #C0C0C0; 
        margin:0 auto;
        color: gray;
    }
    .tooltip_quarterly .second-table tr{
        height: 40px;
        line-height: 40px;
        width: 100%;
        border: 1px solid #C0C0C0;
        display:table;
    }
    .tooltip_quarterly .second-table tr td{
        width:33.33%; 
        height: 40px;
        line-height: 40px;
        border: 1px solid #C0C0C0;
    }
    .tooltip_quarterly .second-table tr td:first-child{
        text-indent: 8px;
    }
    .tooltip_quarterly .second-table tr td:nth-child(2){
        text-indent: 12px;
    }
    .tooltip_quarterly .second-table tr td:last-child{
        text-align: center;
    }
    .daily {
        display: block;
        width: 78%;
        height: 500px;
        min-height:500px;
        float: left;
        /*liu改*/
        margin-top: -235px;
    }
    .daily .tab_bar {
        width:39%;
        height: 40px;
        font-weight: bold;
        font-size:18px;
        float: left;
        margin-top: 20px;
        position: relative;
    }
    .daily .tab_bar .huang_btn {
        float: left;
        cursor: pointer;
        width: 33.33%;
        height: 40px;
        line-height: 40px;
        text-indent: 5px;
        color: #000000;
    }
    .daily .tab_bar .huang_underline {
        height: 2px;
        display: block;
        width: 120px;
        background-color: deepskyblue;
        position: absolute;
        bottom: 0.5px;
        transition: left 0.3s;
    }
    .active {
        color: deepskyblue;
    }
    .active_dashboard{
    	background: deepskyblue;
    	color: #FFFFFF;
    }
    .active_dashboard_{
    	color: #000000;
    	background: #F2F2F2;
    }
    .at_Left {
        left: 0;
    }
    .at_Center {
        left: 120px;
    }
    .at_Right {
        left: 240px;
    }
    .huang-dashboard {
        width: 1200px;
        margin:0 auto;
    }
    .huang-dashboard .dashboard_tab_bar{
    	width:49%;
    	height: 50px;
    	margin-top: 20px;
    }
    .huang-dashboard .dashboard_tab_bar .dashboard_switch_btn{
    	width: 50%;
    	height: 50px;
    	line-height: 50px;
    	float: left;
    	text-align: center;
    	cursor: pointer;
    	font-size: 16px;
    	font-weight: bold;
    }
    .huangjian_p{
    	line-height: 30px;
    }
    .huangjian_p_{
    	line-height: 20px;
    }
    .daily .search_div {
        float: right;
        margin-top: 30px;
        width: 200px;
        height: 30px;
        line-height: 30px;
        position: relative;
        border: 1px solid gray;
        margin-bottom: 5px;
    }
    .daily .search_div .stock_search {
        width: 160px;
        height: 30px;
        line-height: 30px;
        text-indent: 8px;
        font-size: 14px;
        border: none;
        outline: none;
    }
    .daily .search_div .icon_delete{
    	display: none;
    	width: 18px;
    	height: 18px;
    	position:absolute;
    	cursor:pointer;
    	right:30px;
    	top:6px;
    }
    .daily .search_div .icon_search{
    	width: 15px;
    	height: 15px;
        position: absolute;
        right: 10px;
        top: 8px;
        cursor:pointer;
    }
    .daily .daily_content {
        clear: both;
        width: 100%;
        border-top: 1px solid gray;
    }
    /*.daily .daily_content .title-box{
    	width: 100%;
        height: 40px;
        line-height: 40px;
        margin-top: 15px;
        margin-left: 100px;
        text-align: center;
    }*/
    .daily .daily_content .daily-title{
    	font-size: 14px;
        font-weight: bold;
        display: table;
        /*width: auto;*/
        height: 36px;
        line-height: 36px; 
        margin:10px auto;
        background: #10A7D8;
        color: #FFFFFF;
        /*liu改*/
        width: 800px;
        text-align: center;
    }
    .daily .daily_content .daily-title i{
    	text-decoration: underline;
    	font-style:normal;
    	cursor: pointer;
    }
    .daily .daily_content .daily-title span{
    	cursor: pointer;
        color: #FFFFFF;
        display: block;
        float: right;
        width: 10px;
        height: 36px;
        line-height: 36px;
        margin-left: 100px;
        margin-right: 20px;
    }
    .daily .daily_content .content_ul{
    	margin-top: 20px;
    	width: 100%;
    }
    .daily .daily_content  .holder{
    	padding-top: 15px;
    	width:50%;
    	margin:15px auto;
    	text-align: center;
    }
    .daily .daily_content .content_ul li{
    	overflow:auto;
    	min-height: 40px;
    	height:auto !important;
    }
    .daily .daily_content .content_ul .left_div{
    	float: left;
    	width:10%;
    	min-height: 40px;
    	height:auto !important;
    	position: relative;
    }
    .daily .daily_content .content_ul li:first-child .left_div p{
    	margin-top:-3px;
    }
    .daily .daily_content .content_ul .left_div p{
    	display: block;
    	width:100%;
    	margin-top: 5px;
    }
    .daily .daily_content .content_ul li:first-child .right_div p{
    	margin-top:-7.5px;
    	line-height: 0;
    }
    .daily .daily_content .content_ul li:first-child .right_div .myspan{
    	top:0px;
    }
    .daily .daily_content .content_ul .right_div{
    	float: right;
    	width:90%;
    	min-height: 40px;
    	height:auto !important;
    	border-left:1px solid darkgrey;
    	position: relative;
    }
    .daily .daily_content .content_ul .right_div .myspan{
    	width: 10px;
    	height:10px;
    	border-radius:50%;
    	background:gray;
    	position: absolute;
    	left:-6px;
    	top:9px;
    }
    .daily .daily_content .content_ul .right_div p{
    	display: block;
    	word-wrap:break-word;
    	width:95%;
    	margin-left: 30px;
    }
    .daily .daily_content .content_ul .right_div .span{
    	color:deepskyblue;
    	cursor: pointer;
    	font-weight:bold;
    }
    .daily .daily_content .content_ul .right_div i{
    	font-style:normal;
    	line-height:30px;
    }
    .proList {
    	padding-top: 20px;
        padding-bottom: 50px;
        overflow: hidden;
        width: 1200px;
        margin: 0 auto;
    }
    .proList li {
        height: 200px;
        width: 280px;
        background-color: #fff;
        box-shadow: 1px 0 2px rgba(0, 0, 0, .05);
        margin: 0 17px 20px 3px;
        float: left;
        cursor: pointer;
        position: relative;
        transition: all 0.2s;
        user-select: none;
        border: 1px solid #eee;
    }

    .proList li.nohave {
        cursor: default;
    }

    .proList li:hover {
        border: 1px solid #98b5c3;
    }

    .nohave:hover {
        box-shadow: none;
        border: 1px solid #98b5c3;
    }

    .nobuyli:hover .buyShadow {
        display: block;
    }

    .nohave:hover .buyShadow {
        display: block;
    }

    .proList li img {
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
    }

    /*.blackImg{*/
    /*-webkit-filter: grayscale(100%); */
    /*-moz-filter: grayscale(100%); */
    /*-ms-filter: grayscale(100%); */
    /*-o-filter: grayscale(100%); */
    /*filter: grayscale(100%); */
    /*filter: gray;*/
    /*filter: progid:DXImageTransform.Microsoft.BasicImage(grayscale=1);*/
    /*}*/
    .product_status {
        height: 26px;
        width: 100%;
        position: absolute;
        bottom: 0;
        left: 0;
        line-height: 26px;
        font-size: 12px;
        color: #fff;
        text-align: center;
        background-color: deepskyblue;
        /*background-color: #f43e3d;*/
        cursor: default;
    }

    .product_status a {
        color: #fff;
    }

    .buyShadow {
        position: absolute;
        left: 0;
        top: 0;
        height: 100%;
        width: 100%;
        /*background: rgba(54,87,104,.2);*/
        cursor: default;
        display: none;
    }

    .buyShadow .btn-link {
        height: 36px;
        padding: 0 10px;
        border-radius: 4px;
        background-color: #365768;
        color: #fff;
        line-height: 36px;
        text-align: center;
        display: block;
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        bottom: 25px;
        text-decoration: none;
        cursor: pointer;
        white-space: nowrap;
    }

    .buyShadow a:hover {
        background-color: #537b88;
    }

    .switch_bar {
        height: 60px;
        width:260px;
        float: left;
        margin-left: 5%;
        position: relative;
        margin-top: 10px;
    }
    .switch_bar .switch_btn {
        float: left;
        cursor: pointer;
        width: 50%;
        height: 60px;
        line-height: 60px;
        text-align: center;
    }
    .btn_underline {
        height: 2px;
        display: block;
        width: 80px;
        background-color: deepskyblue;
        position: absolute;
        bottom: 10px;
        transition: left 0.3s;
    }

    .atLeft {
        left: 25px;
    }

    .atRight {
        width: 83px;
        left: 155px;
    }

    .langchange {
        height: 30px;
        width: 150px;
        float: right;
        margin-top: 22px;
        margin-right: 10px;
    }

    .langchange li {
        cursor: pointer;
        display: block;
        width: 50%;
        height: 30px;
        line-height: 30px;
        text-align: center;
        float: left;
    }

    .langchange li:nth-child(1) {
        border-right: 1px solid #FFFFFF;
    }

    .langchangecolor {
        color: deepskyblue;
    }
    .notification-bell{
        height: 34px;
        width: 34px;
        float: right;
        margin-top: 22px;
        margin-right: 10px;
        position: relative;
    }
    .notification-bell p{
        display: block;
        position: absolute;
        width: 16px;
        height: 16px;
        line-height: 16px;
        border-radius: 50%;
        right:0;
        top: 0;
        background: deepskyblue;
        text-align: center;
        vertical-align:middle;
        font-size: 8px;
        cursor: pointer;
    }
    .notification-bell img{
        width: 32px;
        height: 32px;
        cursor: pointer;
    }
    .isnotification{
        width:500px;
        height: 300px;
        background-color: #fff;
        border-radius: 3px;
        position: fixed;
        top: 100px;
        right:10px;
        clear: both;
        z-index: 999;
        font-size: 14px;
        -webkit-box-shadow:10px 0px 20px gray,0px 10px 20px gray;  
        -moz-box-shadow:10px 0px 20px gray,0px 10px 20px gray;  
        box-shadow:10px 0px 20px gray,0px 10px 20px gray;
        overflow-y:auto;
    }
    .isnotification ul{
        display: block;
        width:500px;
        height: auto;
    }
    .isnotification ul li{
         display: block;
         width:500px;
         height: 100px;
         padding-top:1px; /*消除自容器的margin-top给父容器的合并margin影响*/
         position:relative;
    }
    .isnotification ul li .content{
        clear: both;
        width:80%;
        display: block;
        margin: 0 auto;
        margin-top: 10px;
        word-break: break-all;
    }
    .isnotification ul li div .company{
        color: deepskyblue !important;
        font-weight: bold;
        cursor: pointer;
    }
    .isnotification ul li div .span{
        color:#333 !important;
    }
     .isnotification ul li div ._span{
        text-decoration: underline;
    }
    .isnotification ul li .time{
        clear: both;
        width:80%;
        height:20px;
        line-height:20px;
        color:black !important;
        font-size: 10px;
        margin-left: 50px;
        margin-top: 20px;
    }
    .timemargin{
        bottom:8px !important;
        margin-top:0 !important;
        margin-left: 0 !important;
        position: absolute !important;
        left: 50px;
        bottom:10px;
    }
    /*设置滚动条样式*/
    ::-webkit-scrollbar{
    /*width: 4px;
    height:4px;*/ 
    width: 1.5px !important;
    height:1.5px !important;
    }
    ::-webkit-scrollbar-thumb{
        border-radius: 2px;
        -webkit-box-shadow: inset 0 0 6px rgba(0,0,0,.3);
        background-color: #7985a3;
    }
    ::-webkit-scrollbar-thumb:hover{  
       background-color:#19bdec;  
    }
    /*---滚动框背景样式--*/  
    ::-webkit-scrollbar-track-piece{  
       -webkit-border-radius: 0;
       /*background: #fff;*/
    }
</style>
