<template>
	<div class="dashboard-header clearfix">
		<div class="header-left left-item">
			<div class="btn-back" @click="goToUserCenter">{{lang?'返回':'back'}}</div>
			<div class="language-tab clearfix" v-if="isShowLangTool">
				<div class="btn-cn btn-lang" :class="{active:lang === 'zh_CN'}" @click="changeLang('zh_CN')">中文</div>
				<div class="btn-en btn-lang" :class="{active:lang !== 'zh_CN'}" @click="changeLang('')">English</div>
			</div>
		</div>
		<time-line :lang="lang"></time-line>
		<div class="header-right right-item" v-if="!isMobile && dashboardId !== 'demo'">
			<!--评论-->
			<div class="discuss" @click="getDiscuss($event)"><span v-text="lang == 'zh_CN'?'观点':'Discuss'"></span><span class="radius_span"></span></div>
			<div class="discussDiv">
				<div class="discuss_shape"></div>
				<div class="huang_tooltip">
					<h5>输入昵称不可为空并且不可重复</h5>
				</div>
				<h1 @click="getDiscuss($event)">X</h1>
				<ul class="tab_bar">
					<li class="liDefault" @click="tabSelect($event)" v-text="lang == 'zh_CN'?'全部':'All Posts'"></li>
					<li @click="tabSelect($event)" v-text="lang == 'zh_CN'?'我的':'Me'"><span class="my_span"></span></li>
				</ul>
				<!--全部-->
				<div class="comment_div">
				  <ul class="content_header">
				  	<li class="li_Default" @click="contentSelect($event)" v-text="lang == 'zh_CN'?'最热':'Hot'"><span class="hot_span"></span></li>
				  	<li @click="contentSelect($event)" v-text="lang == 'zh_CN'?'最新':'Latest'"><span class="lasted_span"></span></li>
				  	<li @click="contentSelect($event)" v-text="lang == 'zh_CN'?'常见问题':'FAQ'"><span class="common_span"></span></li>
				  	<li>
				  		<input type="search" placeholder="Search related comments" class="search_input"/>
				  		<img src="../../../../webapp/images/search.png" @click="img_click()"/>
				  	</li>
				  	<button  class="return_look" @click="return_look" v-text="lang == 'zh_CN'?'《返回查看全部':'Return view all'"></button>
				  </ul>
				  <ul class="content_discuss">
				  	<!--设置昵称-->
				  	<div class="nickName">
				  			<input type="text" placeholder="Please enter your nickname"/>
				  			<button class="sure" @click="sure()" v-text="lang == 'zh_CN'?'确认':'Confirm'"></button>
				  			<button class="unsure" @click="unsure()">X</button>
				  	</div>
				  	<!--设置昵称-->
				  	<div class="nickName_">
				  			<input type="text" placeholder="Please enter your nickname" />
				  			<button class="sure_" @click="sure_()" v-text="lang == 'zh_CN'?'确认':'Confirm'"></button>
				  			<button class="unsure_" @click="unsure_()">X</button>
				  	</div>
				  	<li class="content_discuss_li" v-for="(item,index) in discuss_Array">
				  		<span class="blue_radius"></span>
				  		<div class="_div">
				  			<h5 v-text="item.nickname"></h5>
				  			<h5 v-html="item.comment" class="comment_h5"></h5>
				  			<div class="huang_div">
				  				<div class="left"><span  v-text="item.create_time"></span></div>
				  				<div class="right">
				  			    <span @click="discuss_delete($event,index)" v-text="lang == 'zh_CN'?'删除':'Del'" v-show="uid == item.user_id"></span>&nbsp;
				  				<span><i @click="discuss_praise($event,index)" v-text="lang == 'zh_CN'?'赞':'Like'"></i><b>({{item.like_count}})</b></span>&nbsp;
				  				<span><i @click="discuss_pulish($event,index)" v-text="lang == 'zh_CN'?'评论':'Comment'"></i><b @click="discuss_pulish($event,index)">({{item.comment_count}})</b></span></div>
				  			</div>
				  			<textarea class="discuss_textarea"></textarea>
				  			<h6 class="h6"><input type="checkbox" class="discuss_checkbox" checked="checked"/><span class="_anonymity_span" v-text="lang == 'zh_CN'?'匿名':'Anonymous'"></span></h6>
				  			<button class="discuss_button" @click="button_fn($event,index)" v-text="lang == 'zh_CN'?'发表':'Post'"></button>
				  			<h5 class="my_all_total_count"><p  class="all_total_count" v-text="lang == 'zh_CN'?'全部评论':'All Com'"></p><span>({{all_total_count}})</span></h5>
				  			<!--删除框-->
				  			<div class="prompt_box">
				  				<h5 v-text="lang == 'zh_CN'?'确认删除么?':'Confirm？'">?</h5>
				  				<button class="confirm" @click="confirm($event,index)" v-text="lang == 'zh_CN'?'确认':'Confirm'"></button>
				  				<button class="unconfirm" @click="unconfirm($event,index)" v-text="lang == 'zh_CN'?'取消':'Cancle'"></button>
				  			</div>
				  		   <!--子评论回复框-->
				  			<div class="recovery_zone" v-for="(sub_item,index) in sondiscuss_Array" v-show="item.showComment">
				  				<span class="blue_radius"></span>
				  				<h5 class="h5" v-text="sub_item.nickname"></h5>
				  				<h5 class="_p" v-html="sub_item.comment"></h5>
				  				<h6><p class="time_p" v-text="sub_item.create_time"></p>
				  				<button @click="hj_reply($event,index)" class="_reply" v-text="lang == 'zh_CN'?'回复':'Reply'"></button>
				  				<button class="del" v-text="lang == 'zh_CN'?'删除':'Del'" v-show ="uid == sub_item.user_id" @click="btn_del($event,index)"></button>
				  				</h6>
				  				<textarea class="reply_textarea"></textarea>
				  				<input type="checkbox" class="huang_checkbox" checked="checked"/><span class="huang_span" v-text="lang == 'zh_CN'?'匿名':'Anonymous'"></span>
				  		        <button class="huang_button" @click="huang_publish($event,index)" v-text="lang == 'zh_CN'?'发表':'Post'"></button>
				  		        <div class="prompt_box">
				  				<h5 v-text="lang == 'zh_CN'?'确认删除么?':'Confirm？'">?</h5>
				  				<button class="confirm"  v-text="lang == 'zh_CN'?'确认':'Confirm'" @click="btn_confirm($event,index)"></button>
				  				<button class="unconfirm"  v-text="lang == 'zh_CN'?'取消':'Cancle'" @click="btn_unconfirm($event,index)"></button>
				  			    </div>
				  			</div>
				  		</div>
				  	</li>
				  	<li class="paging_li">
				  		<div class="paging">
				  		<a class="start">首页</a>
						<a class="prev"><<上一页</a>
						<a class="next">下一页>></a>
						<a class="end">尾页</a>
				  	    </div>
				  	</li>
				  	<li class="published_box">
				  		<textarea class="_textarea" @click="textarea_click($event)" placeholder=" 发表观点..."></textarea>
				  		<button class="Button" @click="create_publish()" v-text="lang == 'zh_CN'?'发表':'Post'"></button>
				  		<input type="checkbox"  class="anonymity" checked="checked"/><span class="anonymity_span" v-text="lang == 'zh_CN'?'匿名':'Anonymous'"></span>
				  	</li>
				  </ul>
				</div>
				<!--我的-->
				<div class="comment_my">
				   <ul class="content_header">
				  	<li class="li_Default" @click="contentSelect($event)" v-text="lang == 'zh_CN'?'我发表的':'My Posts'"></li>
				  	<li @click="contentSelect($event)" v-text="lang == 'zh_CN'?'我评论的':'My Comments'"></li>
				  	<li @click="contentSelect($event)" v-text="lang == 'zh_CN'?'评论我的':'Comments Received'"><span class="tome_span"></span></li>
				  	<li @click="contentSelect($event)" v-text="lang == 'zh_CN'?'赞':'liked'"><span class="like_span"></span></li>
				  </ul>
				  	<!--我发表的-->
				  <ul class="content_discuss">
				  	<li class="content_discuss_my_li" v-for="(item,index) in my_Array">
				  		<span class="blue_radius"></span>
				  		<div class="my_div">
				  			<h5 v-text="item.nickname" class="my_div_h5"></h5>
				  			<h5 v-html="item.comment" class="my_div_h5"></h5>
				  			<p>
				  				<div class="left"><span v-text="item.create_time"></span></div>
				  				<div class="right">
				  				<span @click="discuss_deletemy($event,index)" v-text="lang == 'zh_CN'?'删除':'Del'"></span>&nbsp;
				  				<span><i  @click="discuss_praisemy($event,index)"  v-text="lang == 'zh_CN'?'赞':'Like'"></i><b @click="discuss_praisemy($event,index)">({{item.like_count}})</b></span>&nbsp;
				  				<span><i  @click="discuss_pulishmy($event,index)" v-text="lang == 'zh_CN'?'评论':'Comment'"></i><b @click="discuss_pulishmy($event,index)">({{item.comment_count}})</b></span>
				  				</div>
				  			</p>
				  			<textarea class="my_textarea"></textarea>
				  			<input type="checkbox" class="my_checkbox" checked="checked"/><span class="_anonymity_span" v-text="lang == 'zh_CN'?'匿名':'Anonymous'"></span>
				  		    <button class="_my_button" @click="_my_publish($event,index)" v-text="lang == 'zh_CN'?'发表':'Post'"></button>
				  		    <div class="prompt_box">
				  				<h5 v-text="lang == 'zh_CN'?'确认删除么？':'Confirm?'"></h5>
				  				<button class="confirm" @click="confirmmy($event,index)" v-text="lang == 'zh_CN'?'确认':'Confirm'"></button>
				  				<button class="unconfirm" @click="unconfirmmy($event,index)" v-text="lang == 'zh_CN'?'取消':'Cancle'"></button>
				  			</div>
				  			<h5 class="huang_all_total_count">
				  			<p  class="total_count" v-text="lang == 'zh_CN'?'全部评论':'All Com'"></p>
				  			<span>({{total_count}})</span>
				  			</h5>
				  		    <div class="_recovery_zone" v-for="(huang,index) in huang_Array" v-show="item.huang">
				  				<span class="blue_radius"></span>
				  				<h5 class="h5" v-text="huang.nickname"></h5>
				  				<h5 class="_h5" v-html="huang.comment"></h5>
				  				<h6><p v-text="huang.create_time"></p>
				  				<button  class="del" v-text="lang == 'zh_CN'?'删除':'Del'" v-show ="uid == huang.user_id" @click="huang_btn_del($event,index)"></button>
				  				<button class="_my_reply" @click="_my_reply($event,index)" v-text="lang == 'zh_CN'?'回复':'Reply'"></button></h6>
				  				<textarea class="_reply_textarea"></textarea>
				  				<input type="checkbox" class="huang_checkbox" checked="checked"/><span class="huang_span" v-text="lang == 'zh_CN'?'匿名':'Anonymous'"></span>
				  		        <button class="huang_button" @click="huang_publishmy($event,index)" v-text="lang == 'zh_CN'?'发表':'Post'"></button>
				  		        <div class="prompt_box">
				  				<h5 v-text="lang == 'zh_CN'?'确认删除么?':'Confirm？'">?</h5>
				  				<button class="confirm"  v-text="lang == 'zh_CN'?'确认':'Confirm'" @click="huang_btn_confirm($event,index)"></button>
				  				<button class="unconfirm"  v-text="lang == 'zh_CN'?'取消':'Cancle'" @click="huang_btn_unconfirm($event,index)"></button>
				  			     </div>
				  		    </div>
				  		</div>
				  	</li>
				  	<li class="published_boxmy">
				  		<textarea class="_textareamy" @click="textarea_clickmy($event)" placeholder=" 发表观点...">
				  		</textarea>
				  		<button class="Button_my" @click="create_publishmy()" v-text="lang == 'zh_CN'?'发表':'Post'"></button>
				  		<input type="checkbox" class="anonymity_my"checked="checked"/>
				  		<span class="anonymity_spanmy" v-text="lang == 'zh_CN'?'匿名':'Anonymous'"></span>
				  	</li>
				  </ul>
				  <!--我评论的-->
				  <ul class="content_discuss_pinglun">
				  	<li v-for="(item,index) in append_Array">
				  		<span class="pinglun_radius"></span>
				  		<div class="huang_div">
				  			<h5 v-text="item.nickname"></h5>
				  			<h5 v-html="item.comment" class="pinglun-h5"></h5>
				  			<h5><p class="_p"></p><span v-text="item.super_comment.nickname"></span><span v-show="item.super_comment.nickname">:&nbsp;</span><i v-text="item.super_comment.comment" class="huangjian_span"></i></h5>
				  			<h6><p class="time_p" v-text="item.create_time"></p><button @click="delete_append($event,index)" v-text="lang == 'zh_CN'?'删除':'Del'"></button></h6>
				  		    <div class="prompt_box">
				  				<h5 v-text="lang == 'zh_CN'?'确认删除么？':'Confirm?'"></h5>
				  				<button class="confirm" @click="sure_append($event,index)" v-text="lang == 'zh_CN'?'确认':'Confirm'"></button>
				  				<button class="unconfirm" @click="unsure_append($event,index)" v-text="lang == 'zh_CN'?'取消':'Cancle'"></button>
				  			</div>
				  		</div>
				  	</li>
				  </ul>
				  <!--评论我的-->
				  <ul class="content_discuss_pingluntome">
				  	<li v-for="(item,index) in tome_Array">
				  		<span class="pinglun_radius"></span>
				  		<div>
				  		 <h5 v-text="item.nickname"></h5>
				         <h5 v-html="item.comment"></h5>
				         <h5><p class="_p"></p><span v-text="item.super_comment.nickname"></span><span v-show="item.super_comment.nickname">:</span><span v-html="item.super_comment.comment" class="to_me_span"></span></h5>
				         <h6><p class="time_p" v-text="item.create_time"></p><button @click="tome_reply($event,index)" v-text="lang == 'zh_CN'?'回复':'Reply'"></button></h6>
				  		 <textarea class="_textarea"></textarea>
				  		 <input type="checkbox" class="_checkbox" checked="checked"/><span class="_span" v-text="lang == 'zh_CN'?'匿名':'Anonymous'"></span>
				  		 <button class="_button" @click="hj_replyPost($event,index)" v-text="lang == 'zh_CN'?'发表':'Post'"></button>
				  		</div>
				  	</li>
				  </ul>
				  <!--赞我的-->
				  <ul class="content_discuss_praise">
				  	<li v-for="item in praise_Array">
				  		<span class="pinglun_radius"></span>
				  		<div>
				          <h5>{{item.like_nickname}}<span v-text="lang == 'zh_CN'?'赞了我':'liked me'"></span></h5>
				          <h5><p class="_p"></p><span>{{item.nickname}}&nbsp;:&nbsp;</span>{{item.comment}}</h5>
				          <h6><p class="time_p" v-text="item.create_time"></p></h6>
				  		</div>
				  	</li>
				  </ul>
				</div>
			</div>
			<div class="enhance-due-time left-item">
				<span class="due-time rights-text">{{remaining | todate}} {{lang?'到期':'due'}}</span>
				<span class="btn-add-time rights-handle" @click="changeDue">{{lang?'续期':'Renewal'}}</span>
			</div>
			<span class="mid_line"></span>
			<div class="plan-category left-item">
				<span class="curent-plan rights-text">{{plan_name}}</span>
				<span class="btn-upgrade rights-handle" @click="changePlan">{{lang?'升级':'Upgrade'}}</span>
			</div>
		</div>
	</div>
</template>
<script type="text/javascript">
	import timeLine from './time-line'
	export default{
		name: 'dashboard-header',
		data(){
			return{
				creat_at:"",
				asc:"",
				hot:"hot",
				time:"time",
				common:"common",
				create:"create",
				append:"append",
				to_me:"to_me",
				like:"like",
				offset:0,
				limit:20,
				anon:0,
				discuss_Array:[],
				sondiscuss_Array:[],
				my_Array:[],
				huang_Array:[],
				search_val:"",
				product_id:"",
				append_Array:[],
				tome_Array:[],
				praise_Array:[],
				total_count:0,
				all_total_count:0,
			    reply:false,
			    huangj:false,
			    huangj_:false,
			    huangjian:false,
			    huang_jian:false,
			    index_:0,
			    li:null,
			    uid:"",
			    change_Array:[],
			    change_huang_Array:[],
			    commentId:""
			}
		},
		components:{
			timeLine
		},
		watch:{
			pid(val){
				var self = this;
					self.product_id = val;
//					console.log(self.product_id);
					$.get({
		      		url:"/api/v1/comment/count/" + self.product_id,
		      		contentType:'application/json;chartset=utf-8',
                    success:function(res){
                    	if(res.data.total != 0){
                    		$(".radius_span").show();
                    	}else if(res.data.total == 0){
                    		$(".radius_span").hide();
                    	}
                    	if(res.data.all != 0){
                    		$(".my_span").show();
                    	}else if(res.data.all == 0){
                    		$(".my_span").hide();
                    	}
                    	if(res.data.hot != 0){
                    		$(".hot_span").show();
                    	}else if(res.data.hot == 0){
                    		$(".hot_span").hide();
                    	}
                    	if(res.data.latest != 0){
                    		$(".lasted_span").show();
                    	}else if(res.data.latest == 0){
                    		$(".lasted_span").hide();
                    	}
                    	if(res.data.common != 0){
                    		$(".common_span").show();
                    	}else if(res.data.common == 0){
                    		$(".common_span").hide();
                    	}
                    	if(res.data.like != 0){
                    		$(".like_span").show();
                    	}else if(res.data.like == 0){
                    		$(".like_span").hide();
                    	}
                    	if(res.data.to_me != 0){
                    		$(".tome_span").show();
                    	}else if(res.data.to_me == 0){
                    		$(".tome_span").hide();
                    	}
                    },
                    error:function(res){
                    	console.log(res);
                    	if(res.status === 401){
                        self.$router.push({
                            name: 'login'
                        })
                      }
                    }
		      	})
		 },
		 lang(val,Oldval){
		 	if(val){
		 		var self = this;
		 		if($(".tab_bar .liDefault").text() == "All Posts"){
		 		     if($(".comment_div .li_Default").text() == "Latest"){
	            $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + "&lang=" + val,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                	  for(let i = 0;i<self.discuss_Array.length;i++){
                		for(let j=0;j<self.change_Array.length;j++){
                			if(self.change_Array[j] == self.discuss_Array[i].id){
                				let item = self.discuss_Array[i];
		      		            item.showComment = true;
		      		    $.get({
				  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.discuss_Array[i].id + '&lang=' + val,
				      	contentType:'application/json;chartset=utf-8',
				      	success:function(res){
				      		self.sondiscuss_Array = res.data.list;
				      	},
				      	error:function(error){
				      		console.log(error);
				      	}
				  	    })
                			}
                		}
                	}
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}else if($(".comment_div .li_Default").text()=="Hot"){
	            $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + "&lang=" + val,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                	for(let i = 0;i<self.discuss_Array.length;i++){
                		for(let j=0;j<self.change_Array.length;j++){
                			if(self.change_Array[j] == self.discuss_Array[i].id){
                				let item = self.discuss_Array[i];
		      		            item.showComment = true;
		      		    $.get({
				  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.discuss_Array[i].id + '&lang=' + val,
				      	contentType:'application/json;chartset=utf-8',
				      	success:function(res){
				      		self.sondiscuss_Array = res.data.list;
				      	},
				      	error:function(error){
				      		console.log(error);
				      	}
				  	    })
                			}
                		}
                	}
                },
                error:function(error){
                   console.log(error);
                 }
		        })
	          }
		 	}else if($(".tab_bar .liDefault").text() == "Me"){
		 		if($(".comment_my .li_Default").text() == "My Posts"){
		 		$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.create +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + val,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.my_Array = res.data.list;
                	for(let i=0;i<self.my_Array.length;i++){
                		for(let j=0;j<self.change_huang_Array.length;j++){
                			if(self.my_Array[i].id == self.change_huang_Array[j]){
                				let item = self.my_Array[i];
		      		            item.huang = true;
			                $.get({
					  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.my_Array[i].id + '&lang=' + val,
					      	contentType:'application/json;chartset=utf-8',
					      	success:function(res){
					      		self.huang_Array = res.data.list;
					      	},
					      	error:function(error){
					      		console.log(error);
					      	}
					  	    });
                			}
                		}
                	}
                },
                error:function(error){
                   console.log(error);
                 }
		        })
		 			
		 		}else if($(".comment_my .li_Default").text() == "My Comments"){
		 		$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.append +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + val,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	console.log(res);
                	self.append_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                 }
		        })
		 		}else if($(".comment_my .li_Default").text() == "Comments Received"){
		 		$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.to_me +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + val,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	console.log(res);
                	self.tome_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                  }
		        })
		 		}else if($(".comment_my .li_Default").text() == "liked"){
		 		$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.like +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + val,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                   self.praise_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                  }
		         })
		 		}
		 	}
		 }else{
		 	    var self = this;
		 	    if($(".tab_bar .liDefault").text() == "全部"){
		 	        if($(".comment_div .li_Default").text() == "最新"){
	            $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                    for(let i = 0;i<self.discuss_Array.length;i++){
                		for(let j=0;j<self.change_Array.length;j++){
                			if(self.change_Array[j] == self.discuss_Array[i].id){
                				let item = self.discuss_Array[i];
		      		            item.showComment = true;
		      		    $.get({
				  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.discuss_Array[i].id,
				      	contentType:'application/json;chartset=utf-8',
				      	success:function(res){
				      		self.sondiscuss_Array = res.data.list;
				      	},
				      	error:function(error){
				      		console.log(error);
				      	}
				  	    })
                			}
                		}
                	}
                	
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}else if($(".comment_div .li_Default").text()=="最热"){
	            $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                	for(let i = 0;i<self.discuss_Array.length;i++){
                		for(let j=0;j<self.change_Array.length;j++){
                			if(self.change_Array[j] == self.discuss_Array[i].id){
                				let item = self.discuss_Array[i];
		      		            item.showComment = true;
		      		    $.get({
				  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.discuss_Array[i].id,
				      	contentType:'application/json;chartset=utf-8',
				      	success:function(res){
				      		self.sondiscuss_Array = res.data.list;
				      	},
				      	error:function(error){
				      		console.log(error);
				      	}
				  	    })
                			}
                		}
                	}
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}	
		 	    }else if($(".tab_bar .liDefault").text() == "我的"){
		 	    if($(".comment_my .li_Default").text() == "我发表的"){
		 		$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.create +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.my_Array = res.data.list;
                	for(let i=0;i<self.my_Array.length;i++){
                		for(let j=0;j<self.change_huang_Array.length;j++){
                			if(self.my_Array[i].id == self.change_huang_Array[j]){
                				let item = self.my_Array[i];
		      		            item.huang = true;
			                $.get({
					  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.my_Array[i].id + '&lang=' + val,
					      	contentType:'application/json;chartset=utf-8',
					      	success:function(res){
					      		self.huang_Array = res.data.list;
					      	},
					      	error:function(error){
					      		console.log(error);
					      	}
					  	    });
                			}
                		}
                	}
                },
                error:function(error){
                   console.log(error);
                 }
		        })
		 			
		 		}else if($(".comment_my .li_Default").text() == "我评论的"){
		 		$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.append +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.append_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                 }
		        })
		 		}else if($(".comment_my .li_Default").text() == "评论我的"){
		 		$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.to_me +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.tome_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                  }
		        })
		 		}else if($(".comment_my .li_Default").text() == "赞"){
		 		$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.like +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                   self.praise_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                  }
		         })
		 		}
		 	    }
		     }
		   }
		},
		mounted(){
	      //判断删除权限
		  var self = this;
          self.uid =sessionStorage.uid;
          if(self.lang == ""){
		          self.to_en();
		        }else if(self.lang == "zh_CN"){
		          self.to_cn();
		        }
		},
		computed:{
			pid(){
	            return this.$store.state.dashboard.productId;
	        },
	        remaining(){
	            return this.$store.state.dashboard.remaining;
	        },
	        plan_name(){
	            return this.$store.state.dashboard.plan_name;
	        },
	        lang(){
	        	return this.$store.state.dashboard.lang;
	        },
	        isShowLangTool(){
	        	if(this.dashboardId === 'demo') return true;
	        	var langArr = JSON.parse(sessionStorage.getItem("_l") || []);
	        	if(langArr instanceof Array){
	        		if(langArr.length === 2 || !langArr.length)
		        		return true;
	        		else
	        			return false
	        	}
	        	return true;
	        },
	        //当前看板ID
	        dashboardId(){
	        	return this.$route.params.bid;
	        },
	        isMobile(){
				return this.$store.state.common.isMobile;
			}
	    },
	    filters:{
			todate(value){
				return	new Date(value*3600*24*1000+new Date().getTime()).toLocaleDateString().replace(/\//g,'.');
			},
		},
		methods:{
			to_en(){
		          	  $(".search_input").attr("placeholder","Search related comments");
		          	  $(".nickName input").attr("placeholder","Please enter your nickname");
		          	  $(".nickName_ input").attr("placeholder","Please enter your nickname");
			},
			to_cn(){
		          	  $(".search_input").attr("placeholder","搜索相关问题和观点");
		          	  $(".nickName input").attr("placeholder","请输入昵称");
		          	  $(".nickName_ input").attr("placeholder","请输入昵称");
			},
			goToUserCenter(){
				this.$router.push({
					name: 'my'
				})
			},
			changeDue(){
	            GaHelper.sendEvent(GaHelper.Dashboard.renewal, this.dashboardId);
				this.$router.push({
					name: 'order',
					params:{

					}
				})
			},
			changePlan(){
	            GaHelper.sendEvent(GaHelper.Dashboard.upgrade, this.dashboardId);
				this.$router.push({
					name: 'order',
					params:{
						
					}
				})
			},
			changeLang(lang){
				if(!lang){
					var self = this;
		            GaHelper.sendEvent(GaHelper.Dashboard.changeLanguage, 'en');
		            $(".discuss").text("Discuss");
		            $(".search_input").attr("placeholder","Search related comments");
		          	$(".nickName input").attr("placeholder","Please enter your nickname");
		          	$(".nickName_ input").attr("placeholder","Please enter your nickname");
				}else{
					var self = this;
		            GaHelper.sendEvent(GaHelper.Dashboard.changeLanguage, 'cn');
		            $(".discuss").text("观点");
		            $(".search_input").attr("placeholder","搜索相关问题和观点");
		          	$(".nickName input").attr("placeholder","请输入昵称");
		          	$(".nickName_ input").attr("placeholder","请输入昵称");
				}
				this.$store.commit('zk_setLanguage',lang);
			},
			//搜索框返回查看全部
			return_look(){
				var self = this;
				$('.search_input').val("");
				if(self.lang == ""){
				if($(".comment_div .li_Default").text() == "Latest"){
	            $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}else if($(".comment_div .li_Default").text() == "Hot"){
	            $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}
				}else if(self.lang =="zh_CN"){
				if($(".comment_div .li_Default").text() == "最新"){
	            $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                	
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}else if($(".comment_div .li_Default").text()=="最热"){
	            $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}
				}
			},
			//拖拽
			discussDiv_drag(){
            var box = document.querySelector(".discussDiv");
            var drop = document.querySelector(".tab_bar");
			//鼠标再按下之后，同时移动
			    drop.onmousedown=function(e){
			        //e的兼容性（短路运算）
			        e=e||window.event;
			        //鼠标在盒子之中的距离=鼠标在页面之中的位置-盒子距离页面边界的距离
			        var offsetX=e.pageX-box.offsetLeft;
			        var offsetY= e.pageY-box.offsetTop;
			        document.onmousemove=function(e){
			        //盒子在移动后的位置=移动后的鼠标的在页面中的位置-鼠标在盒子之中的距离
			            var x= e.pageX-offsetX;
			            var y= e.pageY-offsetY;
			            if(x<0){
			                x=0;
			            }
			            if(y<0){
			                y=0;
			            }
			            //window.innerHeight浏览器可是区域的高度
			            if(y>window.innerHeight-box.offsetHeight){
			                y=window.innerHeight-box.offsetHeight;
			            }
			            if(x>window.innerWidth-box.offsetWidth){
			                x=window.innerWidth-box.offsetWidth;
			            }
			            box.style.left=x+"px";
			            box.style.top=y+"px";
			        }
			    }
			    //当鼠标抬起的时候，盒子不再改变位置
			    drop.onmouseup=function(){
			        document.onmousemove=null;
			    }
		},
			//评论框是否显示
			getDiscuss(e){
				var self = this;
				self.product_id = self.pid;
//				console.log(self.product_id);
				$(".discuss .radius_span").hide();
				$(".hot_span").hide();
				if($(".discussDiv").is(":hidden")){
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                	$(".discussDiv").show();
                	//评论框显示，给html绑定事件
                	// self.$nextTick(function(){
                	// 	$('html').click(function(){
                	// 		if($(".discussDiv").is(":hidden")){return;}
                 //                $(".discussDiv").hide();
                	// 			$("html").unbind('click');
                	// 	})
                	// })
                	self.discussDiv_drag();
				if(self.lang == ""){
		          	  self.to_en();
		        }else if(self.lang == "zh_CN"){
		        	  self.to_cn();
		         }
                },
                error:function(error){
                   console.log(error);
                 }
		      }) 
			//搜索功能
			$('.search_input').bind('keypress',function(event){
	          if(event.keyCode == "13"){ 
	            self.search_val = $('.search_input').val();
	            if(self.lang == ""){
	            	 if($(".comment_div .li_Default").text() == "Latest"){
	            		$.get({
				      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
				      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&q=' + self.search_val,
				      	contentType:'application/json;chartset=utf-8',
		                success:function(res){
		                	self.discuss_Array = res.data.list;
		                },
		                error:function(error){
		                   console.log(error);
		                 }
				       })
	            	}else if($(".comment_div .li_Default").text() == "Hot"){
	            		 $.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&q=' + self.search_val,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	self.discuss_Array = res.data.list;
			                },
			                error:function(error){
			                   console.log(error);
			                 }
					       })
	            	}
	            }else if(self.lang == "zh_CN"){
	            	if($(".comment_div .li_Default").text() == "最新"){
	            		$.get({
				      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
				      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&q=' + self.search_val + '&lang=' + self.lang,
				      	contentType:'application/json;chartset=utf-8',
		                success:function(res){
		                	self.discuss_Array = res.data.list;
		                },
		                error:function(error){
		                   console.log(error);
		                 }
				       })
	            	}else if($(".comment_div .li_Default").text() == "最热"){
	            		 $.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&q=' + self.search_val + '&lang=' + self.lang,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	self.discuss_Array = res.data.list;
			                },
			                error:function(error){
			                   console.log(error);
			                 }
					       })
	            	}
	            }
	           
	            
	          }  
	        }); 
				}else{
					$(".discussDiv").hide();
				}
				//隐藏分页
				var lis = $(".content_discuss_li");
				if(lis.length <= 20){
					$(".paging_li").hide();
				}
			},
			//图片搜索
			img_click(){
				var self = this;
				var img_textareaval = $('.search_input').val();
				if(self.lang == ""){
					 	 if($(".comment_div .li_Default").text() == "Latest"){
	            		$.get({
				      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
				      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&q=' + img_textareaval,
				      	contentType:'application/json;chartset=utf-8',
		                success:function(res){
		                	self.discuss_Array = res.data.list;
		                },
		                error:function(error){
		                   console.log(error);
		                 }
				       })
	            	}else if($(".comment_div .li_Default").text() == "Hot"){
	            		 $.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&q=' + img_textareaval,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	self.discuss_Array = res.data.list;
			                },
			                error:function(error){
			                   console.log(error);
			                 }
					       })
	            	}
				}else if(self.lang == "zh_CN"){
					if($(".comment_div .li_Default").text() == "最新"){
	            		$.get({
				      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
				      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&q=' + img_textareaval + '&lang=' + self.lang,
				      	contentType:'application/json;chartset=utf-8',
		                success:function(res){
		                	self.discuss_Array = res.data.list;
		                },
		                error:function(error){
		                   console.log(error);
		                 }
				       })
	            	}else if($(".comment_div .li_Default").text() == "最热"){
	            		 $.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&q=' + img_textareaval + '&lang=' + self.lang,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	self.discuss_Array = res.data.list;
			                },
			                error:function(error){
			                   console.log(error);
			                 }
					       })
	            	}
				}
				
			},
			//"我的"和"全部"菜单切换
			tabSelect(e){
				var self = this;
				$(e.target).addClass("liDefault");
				$(e.target).siblings().removeClass("liDefault");
				if(self.lang == ""){
				  self.to_en();
				  if($(e.target).text() == "Me"){
				   $(".comment_div").hide();
				   $(".comment_my").show();
				   $(".my_span").hide();
				   $(".comment_div").find(".published_box").css("display","block");
	            	$(".discuss_textarea").hide();
	            	$(".h6").hide();
	            	$(".discuss_button").hide();
	            	$(".my_all_total_count").hide();
	            	$(".content_discuss_my_li .my_textarea").css("display","none");
	            	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
	            	$(".content_discuss_my_li ._anonymity_span").css("display","none");
	            	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
	            	$(".content_discuss_my_li ._my_button").css("display","none");
			          $.get({
				      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.create +'&sort=' + self.creat_at
				      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
				      	contentType:'application/json;chartset=utf-8',
		                success:function(res){
		                	console.log(res);
		                	self.my_Array = res.data.list;
		                },
		                error:function(error){
		                   console.log(error);
		                }
		        })
				}else if($(e.target).text() == "All Posts"){
					$(".comment_div").show();
					$(".comment_my").hide();
					$(".discuss_textarea").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$(".my_all_total_count").hide();
					$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
					if($(".comment_div .li_Default").text() == "Hot"){
					$.get({
				      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
				      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
				      	contentType:'application/json;chartset=utf-8',
		                success:function(res){
		                	self.discuss_Array = res.data.list;
		                	$(".discuss_textarea").hide();
		                	$(".h6").hide();
		                	$(".discuss_button").hide();
		                	$(".my_all_total_count").hide();
							$(".content_discuss_my_li .my_textarea").css("display","none");
		                	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
		                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
		                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
		                	$(".content_discuss_my_li ._my_button").css("display","none");
		                },
		                error:function(error){
		                   console.log(error);
		                    }
			        })
				}else if($(".comment_div .li_Default").text() == "Latest"){
						    $.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	self.discuss_Array = res.data.list;
			                	$(".discuss_textarea").hide();
			                	$(".h6").hide();
			                	$(".discuss_button").hide();
			                	$(".my_all_total_count").hide();
								$(".content_discuss_my_li .my_textarea").css("display","none");
			                	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
			                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
			                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
			                	$(".content_discuss_my_li ._my_button").css("display","none");
			                },
			                error:function(error){
			                   console.log(error);
			                    }
					          })
				         }else if($(".comment_div .li_Default").text() == "FAQ"){
				         	$(".content_discuss_my_li .my_textarea").css("display","none");
		                	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
		                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
		                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
		                	$(".content_discuss_my_li ._my_button").css("display","none");
						    $.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.common +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	self.discuss_Array = null;
			                	$(".discuss_textarea").hide();
			                	$(".h6").hide();
			                	$(".discuss_button").hide();
			                	$(".my_all_total_count").hide();
								$(".content_discuss_my_li .my_textarea").css("display","none");
			                	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
			                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
			                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
			                	$(".content_discuss_my_li ._my_button").css("display","none");
			                },
			                error:function(error){
			                   console.log(error);
			                    }
					          })
				         }
			       }	
				}else if(self.lang == "zh_CN"){
				self.to_cn();
				if($(e.target).text() == "我的"){
				   $(".comment_div").hide();
				   $(".comment_my").show();
				   $(".my_span").hide();
				   $(".discuss_textarea").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$(".my_all_total_count").hide();
			          $.get({
				      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.create +'&sort=' + self.creat_at
				      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
				      	contentType:'application/json;chartset=utf-8',
		                success:function(res){
		                	console.log(res);
		                	self.my_Array = res.data.list;
		                	$(".discuss_textarea").hide();
		                	$(".h6").hide();
		                	$(".discuss_button").hide();
		                	$(".my_all_total_count").hide();
							$(".content_discuss_my_li .my_textarea").css("display","none");
		                	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
		                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
		                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
		                	$(".content_discuss_my_li ._my_button").css("display","none");
		                },
		                error:function(error){
		                   console.log(error);
		                }
		        })
				}else if($(e.target).text() == "全部"){
					$(".comment_div").show();
					$(".comment_my").hide();
					$(".discuss_textarea").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$(".my_all_total_count").hide();
					if($(".comment_div .li_Default").text() == "最热"){
							$.get({
						      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
						      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
						      	contentType:'application/json;chartset=utf-8',
				                success:function(res){
				                	self.discuss_Array = res.data.list;
				                	$(".discuss_textarea").hide();
				                	$(".h6").hide();
				                	$(".discuss_button").hide();
				                	$(".my_all_total_count").hide();
									$(".content_discuss_my_li .my_textarea").css("display","none");
				                	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
				                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
				                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
				                	$(".content_discuss_my_li ._my_button").css("display","none");
				                },
				                error:function(error){
				                   console.log(error);
				                    }
					        })
				}else if($(".comment_div .li_Default").text() == "最新"){
						    $.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	self.discuss_Array = res.data.list;
			                	$(".discuss_textarea").hide();
			                	$(".h6").hide();
			                	$(".discuss_button").hide();
			                	$(".my_all_total_count").hide();
								$(".content_discuss_my_li .my_textarea").css("display","none");
			                	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
			                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
			                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
			                	$(".content_discuss_my_li ._my_button").css("display","none");
			                },
			                error:function(error){
			                   console.log(error);
			                    }
					          })
				         }else if($(".comment_div .li_Default").text() == "常见问题"){
						    $.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.common +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	self.discuss_Array = null;
			                	$(".discuss_textarea").hide();
			                	$(".h6").hide();
			                	$(".discuss_button").hide();
			                	$(".my_all_total_count").hide();
								$(".content_discuss_my_li .my_textarea").css("display","none");
			                	$(".content_discuss_my_li .my_checkbox").css("visibility","hidden");
			                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
			                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
			                	$(".content_discuss_my_li ._my_button").css("display","none");
			                },
			                error:function(error){
			                   console.log(error);
			                    }
					          })
				         }
			       }
				}
			},
			//全部内容切换
			contentSelect(e){
				var self = this;
				$(e.target).addClass("li_Default");
				$(e.target).siblings().removeClass("li_Default");
				if(self.lang == ""){
				   if($(e.target).text() == "Latest"){
				$(".lasted_span").hide();
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}
				else if($(e.target).text() == "Hot"){
				$(".hot_span").hide();
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}
				else if($(e.target).text() == "FAQ"){
					console.log("----常见问题----");
					self.discuss_Array =null;
					$(".comment_div").find(".published_box").css({
			              "display":"none"
			        });
//				$.get({
//		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.common +'&sort=' + self.creat_at
//		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
//		      	contentType:'application/json;chartset=utf-8',
//              success:function(res){
//              	self.discuss_Array = res.data.list;
//                  $(".discuss_textarea").hide();
//              	$(".discuss_checkbox").hide();
//              	$(".discuss_button").hide();
//              	$("._anonymity_span").hide()；
//              	$(".my_all_total_count").hide()；
//              },
//              error:function(error){
//                 console.log(error);
//               }
//		        })

			  }else if($(e.target).text() == "Comments Received"){
			  	$(".tome_span").hide();
				$(".comment_my .content_discuss_pinglun").hide()
				$(".comment_my .content_discuss").hide();
				$(".comment_my .content_discuss_praise").hide()
				$(".comment_my .content_discuss_pingluntome").show()
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.to_me +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.tome_Array = res.data.list;
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                	$(".comment_my .content_discuss_pingluntome").css({
					"height":"600px"
				})
                },
                error:function(error){
                   console.log(error);
                  }
		        })
			  }else if($(e.target).text() == "liked"){
			  	$(".like_span").hide();
				$(".comment_my .content_discuss_pingluntome").hide()
				$(".comment_my .content_discuss_pinglun").hide()
				$(".comment_my .content_discuss").hide();
				$(".comment_my .content_discuss_praise").show()
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.like +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                   self.praise_Array = res.data.list;
                  $(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                   $(".comment_my .content_discuss_praise").css({
					"height":"600px"
				})
                },
                error:function(error){
                   console.log(error);
                  }
		         })
			  }else if($(e.target).text() == "My Posts"){
			  	$(".comment_my .content_discuss_pingluntome").hide()
				$(".comment_my .content_discuss_pinglun").hide()
				$(".comment_my .content_discuss_praise").hide()
				$(".comment_my .content_discuss").show();
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.create +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.my_Array = res.data.list;
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                },
                error:function(error){
                   console.log(error);
                 }
		        })
			  }else if($(e.target).text() == "My Comments"){
			  	$(".comment_my .content_discuss").hide();
				$(".comment_my .content_discuss_pingluntome").hide();
				$(".comment_my .content_discuss_praise").hide()
				$(".comment_my .content_discuss_pinglun").show();
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.append +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.append_Array = res.data.list;
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                	$(".comment_my .content_discuss_pinglun").css({
					"height":"600px"
				})
                },
                error:function(error){
                   console.log(error);
                 }
		        })
			  }	
				}else if(self.lang == "zh_CN"){
					if($(e.target).text() == "最新"){
				$(".lasted_span").hide();
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}
				else if($(e.target).text() == "最热"){
				$(".hot_span").hide();
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.discuss_Array = res.data.list;
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                },
                error:function(error){
                   console.log(error);
                 }
		        })
				}
				else if($(e.target).text() == "常见问题"){
					console.log("----常见问题----")
					self.discuss_Array = null;
//				$.get({
//		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.common +'&sort=' + self.creat_at
//		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit,
//		      	contentType:'application/json;chartset=utf-8',
//              success:function(res){
//              	self.discuss_Array = res.data.list;
//              },
//              error:function(error){
//                 console.log(error);
//               }
//		        })
			  }else if($(e.target).text() == "评论我的"){
			  	$(".tome_span").hide();
				$(".comment_my .content_discuss_pinglun").hide()
				$(".comment_my .content_discuss").hide();
				$(".comment_my .content_discuss_praise").hide()
				$(".comment_my .content_discuss_pingluntome").show()
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.to_me +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.tome_Array = res.data.list;
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                },
                error:function(error){
                   console.log(error);
                  }
		        })
			  }else if($(e.target).text() == "赞"){
			  	$(".like_span").hide();
				$(".comment_my .content_discuss_pingluntome").hide()
				$(".comment_my .content_discuss_pinglun").hide()
				$(".comment_my .content_discuss").hide();
				$(".comment_my .content_discuss_praise").show()
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.like +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                   self.praise_Array = res.data.list;
                   $(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                },
                error:function(error){
                   console.log(error);
                  }
		         })
			  }else if($(e.target).text() == "我发表的"){
			  	$(".comment_my .content_discuss_pingluntome").hide()
				$(".comment_my .content_discuss_pinglun").hide()
				$(".comment_my .content_discuss_praise").hide()
				$(".comment_my .content_discuss").show();
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.create +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.my_Array = res.data.list;
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                },
                error:function(error){
                   console.log(error);
                 }
		        })
			  }else if($(e.target).text() == "我评论的"){
			  	$(".comment_my .content_discuss").hide();
				$(".comment_my .content_discuss_pingluntome").hide();
				$(".comment_my .content_discuss_praise").hide()
				$(".comment_my .content_discuss_pinglun").show();
				$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.append +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit+ '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.append_Array = res.data.list;
                	$(".content_discuss_my_li .my_textarea").css("display","none");
                	$(".content_discuss_my_li .my_checkbox").css("visibility", "hidden");
                	$(".content_discuss_my_li ._anonymity_span").css("display","none");
                	$(".content_discuss_my_li .huang_all_total_count").css("display","none");
                	$(".content_discuss_my_li ._my_button").css("display","none");
                	$(".comment_div").find(".published_box").css({
			              "display":"block"
			        });
                	$(".discuss_textarea").hide();
                	$(".discuss_checkbox").hide();
                	$(".h6").hide();
                	$(".discuss_button").hide();
                	$("._anonymity_span").hide();
                	$(".my_all_total_count").hide();
                },
                error:function(error){
                   console.log(error);
                 }
		        })
			  }
			}
			},
			//“全部”的输入框点击事件
			textarea_click(e){
				var self = this;
				$(".comment_div .content_discuss").css({
					"height":"480px"
				})
				$(".published_box").css({
					"height":"110px"
				})
				$(e.target).css({
					"width":"290px",
					"height":"60px"
				})
				$(".Button").css({
					"width":"140px",
					"height":"30px",
					"lineHeight":"25px",
					"marginLeft":"200px",
					"marginTop":"6px",
					'border-radius':'5px'
				})
				$(".anonymity").css({
					"visibility": 'visible'
				})
				$(".anonymity_span").css({
					"display": 'block',
					'color':'#7985A3'
				});
			},
			//“我的”输入框点击事件
			textarea_clickmy(e){
				$(".comment_my .content_discuss").css({
					"height":"480px"
				})
				$(".published_boxmy").css({
					"height":"110px"
				})
				$(e.target).css({
					"width":"290px",
					"height":"60px"
				})
				$(".Button_my").css({
					"width":"140px",
					"height":"30px",
					"lineHeight":"25px",
					"marginLeft":"200px",
					"marginTop":"10px",
					'border-radius':'5px'
				})
				$(".anonymity_my").css({
					"visibility": 'visible'
				})
				$(".anonymity_spanmy").css({
					"display":"block"
				})
			},
			//删除评论
			discuss_delete(e,index){
				var self = this;
				$(e.target.parentNode.parentNode.parentNode).find(".prompt_box").css({
					'visibility': 'visible',
					"background":"#151922"
				})
		  },
		  //“我的”删除
		  discuss_deletemy(e,index){
				var self = this;
				$(e.target.parentNode.parentNode.parentNode).find(".prompt_box").css({
					'visibility': 'visible',
					"background":"#151922"
				})
		  },
		  //确认删除
		  confirm(e,index){
		  	var self = this;
		  	$.post({
              	url:"/api/v1/comment/delete/" + self.discuss_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                       cid:self.discuss_Array[index].id
                }),
                success:function(res){
                	self.discuss_Array.splice(index,1);
		  	        $(e.target.parentNode.parentNode.parentNode).find(".prompt_box").css({
					'visibility': 'hidden'
		        })
                },
                error:function(error){
                	console.log(error);
                }
          })
		  },
		  confirmmy(e,index){
		  	var self = this;
		  	$.post({
              	url:"/api/v1/comment/delete/" + self.my_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                       cid:self.my_Array[index].id
                }),
                success:function(res){
                	console.log(res);
                	self.my_Array.splice(index,1);
		  	       $(e.target.parentNode.parentNode.parentNode).find(".prompt_box").css({
					'visibility': 'hidden'
		        })
                },
                error:function(error){
                	console.log(error);
                }
           })
		  },
		  //取消删除
		  unconfirm(e,index){
		  	$(e.target.parentNode.parentNode.parentNode).find(".prompt_box").css({
					'visibility': 'hidden'
		    })
		  },
		  //取消我的删除
		  unconfirmmy(e,index){
		  	$(e.target.parentNode.parentNode.parentNode).find(".prompt_box").css({
					'visibility': 'hidden'
		    })
		  },
		  //点赞功能
		  discuss_praise(e,index){
		  	var self = this;
		  	if(self.lang == ""){
		  		if($(e.target).html() == "Like"){
		  		$(e.target).html("Liked");
		  		$.post({
              	url:"/api/v1/comment/like/" + self.discuss_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                    cid:self.discuss_Array[index].id
                }),
                success:function(res){
                	self.discuss_Array[index].like_count++;
                },
                error:function(error){
                	console.log(error);
                }
            })
		  	}else if($(e.target).html() == "Liked"){
		  		$(e.target).html("Like");
		  		$.post({
              	url:"/api/v1/comment/like/" + self.discuss_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                       cid:self.discuss_Array[index].id
                }),
                success:function(res){
                	self.discuss_Array[index].like_count--;
		  		    if(self.discuss_Array[index].like_count <= 0){
		  			self.discuss_Array[index].like_count = 0;
		  		}
                },
                error:function(error){
                	console.log(error);
                }
            })
		  	}
		  	}else if(self.lang == "zh_CN"){
		  	   	if($(e.target).html() == "赞"){
		  		$(e.target).html("已赞");
		  		$.post({
              	url:"/api/v1/comment/like/" + self.discuss_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                    cid:self.discuss_Array[index].id
                }),
                success:function(res){
                	self.discuss_Array[index].like_count++;
                },
                error:function(error){
                	console.log(error);
                }
            })
		  	}else if($(e.target).html() == "已赞"){
		  		$(e.target).html("赞");
		  		$.post({
              	url:"/api/v1/comment/like/" + self.discuss_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                       cid:self.discuss_Array[index].id
                }),
                success:function(res){
                	self.discuss_Array[index].like_count--;
		  		    if(self.discuss_Array[index].like_count <= 0){
		  			self.discuss_Array[index].like_count = 0;
		  		}
                },
                error:function(error){
                	console.log(error);
                }
            })
		  	}	
		  	}
		  },
		  //我的点赞
		   discuss_praisemy(e,index){
		  	var self = this;
		  	if(self.lang == "zh_CN"){
		  		  	if($(e.target).html() == "赞"){
		  		$(e.target).html("已赞");
		  		$.post({
              	url:"/api/v1/comment/like/" + self.my_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                       cid:self.my_Array[index].id
                }),
                success:function(res){
                	 self.my_Array[index].like_count++;
                },
                error:function(error){
                	console.log(error);
                }
            })
		  	}else if($(e.target).html() == "已赞"){
		  		$(e.target).html("赞");
		  		$.post({
              	url:"/api/v1/comment/like/" + self.my_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                       cid:self.my_Array[index].id
                }),
                success:function(res){
                	self.my_Array[index].like_count--;
		  		    if(self.my_Array[index].like_count <= 0){
		  			self.my_Array[index].like_count = 0;
		  		}
                },
                error:function(error){
                	console.log(error);
                }
            })
		  	}
		  	}else if(self.lang == ""){
		  		  	if($(e.target).html() == "Like"){
		  		$(e.target).html("Liked");
		  		$.post({
              	url:"/api/v1/comment/like/" + self.my_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                       cid:self.my_Array[index].id
                }),
                success:function(res){
                	 self.my_Array[index].like_count++;
                },
                error:function(error){
                	console.log(error);
                }
            })
		  	}else if($(e.target).html() == "Liked"){
		  		$(e.target).html("Like");
		  		$.post({
              	url:"/api/v1/comment/like/" + self.my_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                       cid:self.my_Array[index].id
                }),
                success:function(res){
                	self.my_Array[index].like_count--;
		  		    if(self.my_Array[index].like_count <= 0){
		  			self.my_Array[index].like_count = 0;
		  		}
                },
                error:function(error){
                	console.log(error);
                }
              })
		  	}
		  	}
		  },
		  //全部点击子评论
		  discuss_pulish(e,index){
		  	var self = this;
		  	var $li = $(e.target.parentNode.parentNode.parentNode.parentNode);
		  	if(self.huangj == false){
		  	     //获取子评论列表
		     	$.get({
		  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.discuss_Array[index].id + '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
		      	success:function(res){
		      		self.sondiscuss_Array = res.data.list;
		      		self.all_total_count = res.data.total;
		      		let item = self.discuss_Array[index];
		      		item.showComment = true;
		      		self.discuss_Array.splice(index, 1, item);
		      		self.change_Array.push(self.discuss_Array[index].id);
		      		$li.find(".h6").show();
		      		$li.find("._anonymity_span").show();
		      		if(self.discuss_Array[index].comment_count == 0){
		      			$li.find(".my_all_total_count").css({
		      				"display":"none"
		      			})
		      		}else{
		      		    $li.find(".my_all_total_count").css({
		      				"display":"block"
		      			})
		      		}
				  	$li.find(".discuss_textarea").show();
				  	$li.find(".discuss_checkbox").css({
				  		'visibility':"visible",
				  		"display":"block"
				  	});
				  	$li.find(".discuss_button").show();
				  	$li.find(".discuss_button").css({
				  		"border-radius":"5px",
				  		"display":"block"
				  	});
				  	self.huangj = true;
		      	},
		      	error:function(error){
		      		console.log(error);
		      	}
		  	});
		  	}else if(self.huangj == true){
		  	let item = self.discuss_Array[index];
		    item.showComment = false;
		    self.discuss_Array.splice(index, 1,item);
		    self.change_Array.splice(0,1);
		    $(e.target.parentNode.parentNode.parentNode.parentNode).find(".discuss_checkbox").css({
		  		'visibility':"hidden"
		  	});
		  	$(e.target.parentNode.parentNode.parentNode.parentNode).find("._anonymity_span").css({
		  		'display':"none"
		  	});
		  	$(e.target.parentNode.parentNode.parentNode.parentNode).find(".h6").css({
		  		'display':"none"
		  	});
		  	$(e.target.parentNode.parentNode.parentNode.parentNode).find(".discuss_button").hide();
		  	$(e.target.parentNode.parentNode.parentNode.parentNode).find(".my_all_total_count").css({
		  		"display":"none"
		  	});
		  	$(e.target.parentNode.parentNode.parentNode.parentNode).find(".discuss_textarea").hide()
            $(e.target.parentNode.parentNode.parentNode.parentNode).find(".content_discuss_li").css({
                'width':"398px"
             });
             self.huangj = false;
		  	}
		  },
		  //“全部”子评论发表
		  button_fn(e,index){
		  	var self = this;
		  	var $li = $(e.target.parentNode.parentNode);
		  	self.li = $li;
		  	var discuss_textareaval = $li.find(".discuss_textarea").val();
             if($li.find(".discuss_checkbox").is(":checked")){
             self.anon = 1;
              if(discuss_textareaval != ""){
		  	   $.post({
		  		url:" /api/v1/comment/create/" + self.product_id +"?anon="+self.anon +"&cid=" + self.discuss_Array[index].id + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                        comment:discuss_textareaval
                }),
                success:function(res){
                	self.discuss_Array[index].comment_count++;
                	self.all_total_count++;
                	$li.find(".my_all_total_count").css({
                		"display":"block"
                	});
                	$li.find(".discuss_textarea").val("");
                	 //全部发表子评论请求子评论列表
		            $.get({
				  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.discuss_Array[index].id + '&lang=' + self.lang,
				      	contentType:'application/json;chartset=utf-8',
				      	success:function(res){
				      		self.sondiscuss_Array = res.data.list;
				      	},
				      	error:function(error){
				      		console.log(error);
				      	}
				  	})
                },
                error:function(error){
                	console.log(error)
                }
		  	 })	
		  	}
		  	}else{
		  	  self.anon = 0;
		  	  $.get({
		 		url:"api/v1/account/have-nickname",
		 		contentType:'application/json;chartset=utf-8',
		 		success:function(res){
		 			//没有设置过昵称
		 			if(res.success == false){
		 			   $(".discuss_shape").show();
		 			   $(".nickName_").show();
		  	           $(".nickName_").css({
		  	  	       "background":"#151922"
		  	          })
		  	         self.index_=index;
		 			}
		 			else if(res.success == true){
		 		 if(discuss_textareaval != ""){
		  	   $.post({
		  		url:" /api/v1/comment/create/" + self.product_id +"?anon="+self.anon +"&cid=" + self.discuss_Array[index].id+ '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                        comment:discuss_textareaval
                }),
                success:function(res){
                	console.log(res);
                	self.discuss_Array[index].comment_count++;
                	self.all_total_count++;
                	$li.find(".discuss_textarea").val("");
                	 //全部发表子评论请求子评论列表
		            $.get({
				  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.discuss_Array[index].id + '&lang=' + self.lang,
				      	contentType:'application/json;chartset=utf-8',
				      	success:function(res){
				      		self.sondiscuss_Array = res.data.list;
				      	},
				      	error:function(error){
				      		console.log(error);
				      	}
				  	})
                },
                error:function(error){
                	console.log(error)
                }
		  	 })	
		  	   }
		     }
		 		}
		 		})
		  	}
		  },
		  //“全部”子评论回复
		 hj_reply(e,index){
		 	var self = this;
		 	if(self.huangj_ == false){
		 	 $(e.target.parentNode.parentNode).find(".reply_textarea").show();
		 	 $(e.target.parentNode.parentNode).css({
		 	 	"height":"170px"
		 	 })
		 	 $(e.target.parentNode.parentNode).find(".huang_checkbox").css({
		 	 	'visibility': 'visible',
		 	 	'display': 'block'
		 	 })
		 	 $(e.target.parentNode.parentNode).find(".huang_span").css({
		 	 	"display":"block"
		 	 })
		 	 $(e.target.parentNode.parentNode).find(".huang_button").css({
		 	 	"display":"block"
		 	 })
		 	 self.huangj_ = true;
		 	}else if(self.huangj_ == true){
		 		$(e.target.parentNode.parentNode).css({
		 	 	"min-height":"70px"
		 	    })
		 		$(e.target.parentNode.parentNode).find(".huang_checkbox").css({
		 	 	'visibility': 'hidden',
		 	 	'display': 'none'
			 	 })
			 	 $(e.target.parentNode.parentNode).find(".huang_span").hide()
			 	 $(e.target.parentNode.parentNode).find(".huang_button").hide();
		 		$(e.target.parentNode.parentNode).find(".reply_textarea").hide();
		 	    self.huangj_ = false;
		 	}
		 },
		 //全部子评论回复发表
		 huang_publish(e,index){
		 	var self = this;
		 	console.log(e.target.parentNode);
		 	var $reply_textarea = $(e.target.parentNode).find(".reply_textarea");
		 	if($(e.target.parentNode).find(".huang_checkbox").is(":checked")){
		 		self.anon = 1;
		 	    if($reply_textarea.val() != ""){
		 	   	$.post({
		  		url:" /api/v1/comment/create/"+ self.product_id +"?anon="+ self.anon + "&sid=" + self.sondiscuss_Array[index].id + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                    comment:$reply_textarea.val(),
                }),
                success:function(res){
                	console.log(res);
                	self.sondiscuss_Array = res.data.list;
                	for(let i=0;i<self.discuss_Array.length;i++){
                		if(self.discuss_Array[i].id == res.data.comment_id){
                			self.discuss_Array[i].comment_count++;
                		}
                	}
                	self.all_total_count++;
                	self.huangj_ = false
                 	$reply_textarea.val("");
                 	$reply_textarea.hide();
                 $(e.target.parentNode.parentNode).find(".huang_checkbox").css({
		 	 	'visibility': 'hidden'
		 	     })
                 $(e.target.parentNode.parentNode).find(".huang_span").css({
		 	 	'display': 'none'
		 	    })
                $(e.target.parentNode.parentNode.parentNode).find(".recovery_zone").css({
		 	 	"height":"70px"
		 	    })
			 	 $(e.target.parentNode.parentNode).find(".huang_button").hide();
                },
                error:function(error){
                	console.log(error)
                }
		  	   })	
		 	  }
		 	}else{
		 		self.anon = 0;
		 	  if($reply_textarea.val() != ""){
		 	   	$.post({
		  		url:" /api/v1/comment/create/"+ self.product_id +"?anon="+ self.anon + "&sid=" + self.sondiscuss_Array[index].id + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                    comment:$reply_textarea.val(),
                }),
                success:function(res){
                	console.log(res);
                	self.sondiscuss_Array = res.data.list;
                	for(let i=0;i<self.discuss_Array.length;i++){
                		if(self.discuss_Array[i].id == res.data.comment_id){
                			self.discuss_Array[i].comment_count++;
                		}
                	}
                	self.all_total_count++;
                 	$reply_textarea.val("");
                 	$reply_textarea.hide();
                 $(e.target.parentNode.parentNode).find(".huang_checkbox").css({
		 	 	'visibility': 'hidden'
		 	     })
                 $(e.target.parentNode.parentNode).find(".huang_span").css({
		 	 	'display': 'none'
		 	    })
                $(e.target.parentNode.parentNode.parentNode).find(".recovery_zone").css({
		 	 	"height":"70px"
		 	    })
			 	 $(e.target.parentNode.parentNode).find(".huang_button").hide();
                },
                error:function(error){
                	console.log(error)
                }
		  	   })	
		 	  }
		 	}
		 },
		  //全部发表
		  create_publish(){
		  	var self = this;
		  	if($(".anonymity").is(":checked")){
		  	  self.anon = 1;
		  	  if($("._textarea").val() != ""){
		  	  	$.post({
		  		url:" /api/v1/comment/create/"+ self.product_id+"?anon="+self.anon + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                        bid:self.product_id,
                        anon:self.anon,
                        comment:$("._textarea").val()
                }),
                success:function(res){
                $("._textarea").val("");
                if($(".comment_div .li_Default").text() == "Hot" || $(".comment_div .li_Default").text() == "最热"){
                $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	if(self.lang == ""){
                	self.to_en();
                }else if(self.lang == "zh_CN"){
                	self.to_cn();
                }
                	self.discuss_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                 }
		        })	
                }else if($(".comment_div .li_Default").text() == "Latest" || $(".comment_div .li_Default").text() == "最新"){
                    	$.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	if(self.lang == ""){
                	self.to_en();
                }else if(self.lang == "zh_CN"){
                	self.to_cn();
                }
                	self.discuss_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                 }
		        })	
                }
                },
                error:function(error){
                	console.log(error)
                 }
		  	   })
		  	  }
		  	}else if($(".anonymity").is(":checked") == false){
		  	//设置昵称
		  	  self.anon = 0;
		  	  $.get({
		 		url:"api/v1/account/have-nickname",
		 		contentType:'application/json;chartset=utf-8',
		 		success:function(res){
		 			//判断是否设置过昵称
		 			if(res.success == false){
		 			  $(".discuss_shape").show();
		 			   $(".nickName").show();
		  	           $(".nickName").css({
		  	  	       "background":"#151922"
		  	          })
		 			}else if(res.success == true){
		 				$.post({
					  		url:" /api/v1/comment/create/"+ self.product_id+"?anon="+self.anon + '&lang=' + self.lang,
					  		contentType:'application/json;chartset=utf-8',
					  		data:JSON.stringify({
			                        bid:self.product_id,
			                        anon:self.anon,
			                        comment:$("._textarea").val()
			                }),
			                success:function(res){
			                	$(".nickName input").val("");
							 	$(".nickName").hide();
							 	$("._textarea").val("");
							$.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	console.log(res.data.list);
			                	self.discuss_Array = res.data.list;
			                },
			                error:function(error){
			                   console.log(error);
			                 }
					        })
							$.get({
					      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.time +'&sort=' + self.creat_at
					      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
					      	contentType:'application/json;chartset=utf-8',
			                success:function(res){
			                	console.log(res.data.list);
			                	self.discuss_Array = res.data.list;
			                },
			                error:function(error){
			                   console.log(error);
			                 }
					        })
			                },
			                error:function(error){
			                	console.log(error)
			                }
					  	})
		 			}
		 		}
		 	 })
		  	}
		 },
		 //设置昵称
		 sure(){
		 	var self = this;
		 	if($(".nickName input").val() == ""){
		 		$(".huang_tooltip").show();
		 		$(".discuss_shape").show();
		 	}else{
		 	  if($("._textarea").val() !=""){
		 	  $.ajax({
		      	url:"/api/v1/account/set-nickname",
		      	type:"post",
		      	data:{
					nickname:$(".nickName input").val(),
				},
                success:function(res){
                	if(res.success == true){
                	    //创建评论
				 	  	$.post({
				  		url:" /api/v1/comment/create/"+ self.product_id+"?anon="+self.anon + '&lang=' + self.lang,
				  		contentType:'application/json;chartset=utf-8' ,
				  		data:JSON.stringify({
		                        bid:self.product_id,
		                        anon:self.anon,
		                        comment:$("._textarea").val()
		                }),
		                success:function(res){
						$("._textarea").val("");
						$(".nickName input").val("");
                	    $(".nickName").hide();
                	    $(".discuss_shape").hide();
                	    $(".huang_tooltip").hide();
						$.get({
				      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.hot +'&sort=' + self.creat_at
				      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
				      	contentType:'application/json;chartset=utf-8',
		                success:function(res){
		                	self.discuss_Array = res.data.list;
		                },
		                error:function(error){
		                   console.log(error);
		                 }
				        })
		                },
		                error:function(error){
		                	console.log(error)
		                }
				  	    })
                	}else if(res.success == false){
                		$(".huang_tooltip").show();
                	}
                },
                error:function(error){
                   console.log(error);
                 }
		        })
		 	}
		 	}
		 },
		 //取消设置
		 unsure(){
		 	$(".nickName").hide();
		 	$(".discuss_shape").hide();
		 	$(".huang_tooltip").hide();
		 },
		 sure_(){
		 	var self = this;
		 	var myli = self.li;
		 	var discuss_textareaval = myli.find(".discuss_textarea").val();
		 	if($(".nickName_ input").val() == ""){
		 		$(".huang_tooltip").show();
		 		$(".discuss_shape").show();
		 	}else{
		 		if(discuss_textareaval != ""){
		 		$.ajax({
		      	url:"/api/v1/account/set-nickname",
		      	type:"post",
		      	data:{
					nickname:$(".nickName_ input").val(),
				},
                success:function(res){
                   if(res.success == true){
                	$.post({
			  		url:" /api/v1/comment/create/" + self.product_id +"?anon="+self.anon +"&cid=" + self.discuss_Array[self.index_].id + '&lang=' + self.lang,
			  		contentType:'application/json;chartset=utf-8',
			  		data:JSON.stringify({
	                        comment:discuss_textareaval
	                }),
	                success:function(res){
	                	$(".nickName_ input").val("");
                	    $(".nickName_").hide();
                	    $(".discuss_shape").hide();
                	    $(".huang_tooltip").hide();
                	    myli.find(".discuss_textarea").val("");
	                	self.discuss_Array[self.index_].comment_count++;
	                	self.all_total_count++;
	                	 //全部发表子评论请求子评论列表
			            $.get({
					  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.discuss_Array[self.index_].id + '&lang=' + self.lang,
					      	contentType:'application/json;chartset=utf-8',
					      	success:function(res){
					      		self.sondiscuss_Array = res.data.list;
					      	},
					      	error:function(error){
					      		console.log(error);
					      	}
					  	})
	                },
	                error:function(error){
	                	console.log(error)
	                }
			  	   })
                	}else if(res.success == false){
                		$(".huang_tooltip").show()
                	}
                },
                error:function(error){
                   console.log(error);
                 }
		        })
		 		}
		 	}
		 },
		 unsure_(){
		 	$(".nickName_").hide();
		 	$(".discuss_shape").hide();
		 	$(".huang_tooltip").hide();
		 },
		//我的发表
		create_publishmy(){
			var self = this;
		  	if($(".anonymity_my").is(":checked")){
		  	  self.anon = 1;
		  	  if($("._textareamy").val() != ""){
		  	  	$.post({
		  		url:" /api/v1/comment/create/"+ self.product_id+"?anon="+self.anon + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                        bid:self.product_id,
                        anon:self.anon,
                        comment:$("._textareamy").val()
                }),
                success:function(res){
                $("._textareamy").val("");
                $.get({
		      	url: '/api/v1/comments/'+ self.product_id +'?c='+ self.create +'&sort=' + self.creat_at
		      	+'&order='+self.asc + '&offset=' + self.offset + '&limit=' + self.limit + '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.my_Array = res.data.list;
                },
                error:function(error){
                   console.log(error);
                 }
		        })
                },
                error:function(error){
                	console.log(error)
                }
		  	   })
		  	 }  	 
           }
	    },
		 //删除我评论的
		delete_append(e,index){
		 	console.log(index);
		 	$(e.target.parentNode.parentNode.parentNode).find(".prompt_box").show();
		 	$(e.target.parentNode.parentNode.parentNode).find(".prompt_box").css({
		 		"background":"#151922"
		 	})
		},
		//确定
		sure_append(e,index){
		var self = this;
		$.post({
              	url:"/api/v1/comment/delete/" + self.append_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                       cid:self.append_Array[index].id
                }),
                success:function(res){
                	self.append_Array.splice(index,1);
		  	        $(e.target.parentNode.parentNode.parentNode).find(".prompt_box").hide();
                },
                error:function(error){
                	console.log(error);
                }
           })
		},
		//取消
		unsure_append(e,index){
			$(e.target.parentNode.parentNode.parentNode).find(".prompt_box").hide();
		},
		//“我发表的”评论
		  discuss_pulishmy(e,index){
		  	 var self = this;
		  	 var $li = $(e.target.parentNode.parentNode.parentNode.parentNode);
		  	 if(self.huangjian == false){
		  	    $.get({
		  		url:'/api/v1/comments/'+ self.product_id +'?cid='+ self.my_Array[index].id + '&lang=' + self.lang,
		      	contentType:'application/json;chartset=utf-8',
		      	success:function(res){
		      		self.huang_Array = res.data.list;
		      		let item = self.my_Array[index];
		      		item.huang = true;
		      		self.my_Array.splice(index, 1, item);
		      		self.total_count = res.data.total;
		      		self.$nextTick(function(){
		      		$li.find(".my_textarea").show();	
                    $li.find(".my_checkbox").css("visibility","visible");
	                $li.find("._anonymity_span").show();
	                $li.find("._my_button").show();
		          })
		      	self.change_huang_Array.push(self.my_Array[index].id);
		      		if(self.my_Array[index].comment_count == 0){
		      			$li.find(".huang_all_total_count").css({
		      				"display":"none"
		      			})
		      		}else{
		      		    $li.find(".huang_all_total_count").css({
                	     "display":"block"
                        })
		      		}
		      	},
		      	error:function(error){
		      		console.log(error);
		      	}
		  	 });
		  	 self.huangjian = true;
		  	 }else if(self.huangjian == true){
		  	 	self.change_huang_Array.splice(0,1);
		  	 	    $li.find(".my_textarea").hide();	
                    $li.find(".my_checkbox").css({
              	     "visibility":"hidden"
                    });
                    $li.find(".huang_all_total_count").css({
		      				"display":"none"
		      	     })
	              $li.find("._anonymity_span").hide();
	              $li.find("._my_button").hide();
                    let item = self.my_Array[index];
		      		item.huang = false;
		      		self.my_Array.splice(index, 1, item);
	                self.huangjian = false;
		  	 }
		  },
		  //“我发表的” 发表子评论
		  _my_publish(e,index){
		  	var self = this;
		  	var $my_li = $(e.target.parentNode.parentNode);
		  	var my_textareaval = $my_li.find(".my_textarea").val();
		  	if($my_li.find(".my_checkbox").is(":checked")){
             self.anon = 1;
             if(my_textareaval != ""){
		  	   $.post({
		  		url:" /api/v1/comment/create/" + self.product_id +"?anon="+self.anon +"&cid=" + self.my_Array[index].id + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                        comment:my_textareaval
                }),
                success:function(res){
                	$my_li.find(".huang_all_total_count").show();
                	$my_li.find("._recovery_zone").show();
                $.get({
		  		url:"/api/v1/comments/"+ self.product_id +"?cid="+ self.my_Array[index].id + "&lang=" + self.lang,
		      	contentType:'application/json;chartset=utf-8',
		      	success:function(res){
		      		console.log(res)
		      		self.huang_Array = res.data.list;
		      		$my_li.find(".my_textarea").val("");
                	self.my_Array[index].comment_count++;
                	self.total_count++;
		      	},
		      	error:function(error){
		      		console.log(error);
		      	}
		  	    })
                },
                error:function(error){
                	console.log(error)
                }
		  	 })	
		  	}
		  	}else{
		  	  self.anon = 0;
		  	  	  	if(my_textareaval != ""){
		  	   $.post({
		  		url:" /api/v1/comment/create/" + self.product_id +"?anon="+self.anon +"&cid=" + self.my_Array[index].id + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                        comment:my_textareaval
                }),
                success:function(res){
                $.get({
		  		url:"/api/v1/comments/"+ self.product_id +"?cid="+ self.my_Array[index].id + "&lang=" + self.lang,
		      	contentType:'application/json;chartset=utf-8',
		      	success:function(res){
		      		self.huang_Array = res.data.list;
		      		$my_li.find(".my_textarea").val("");
                	self.my_Array[index].comment_count++;
                	self.total_count++;
		      	},
		      	error:function(error){
		      		console.log(error);
		      	}
		  	    })
                },
                error:function(error){
                	console.log(error)
                }
		  	 })	
		  	}
		  	}
		  },
		  //我的子评论回复
		  _my_reply(e,index){
		  	var self = this;
		  	if(self.reply == false){
		  		$(e.target.parentNode.parentNode).find("._reply_textarea").show();
		  		$(e.target.parentNode.parentNode).css({
		  			"min-height":"170px"
		  		})
		  		 $(e.target.parentNode.parentNode).find(".huang_checkbox").css({
		 	 	'visibility': 'visible',
		 	 	'display': 'block'
		 	 })
		 	 $(e.target.parentNode.parentNode).find(".huang_span").css({
		 	 	"display":"block"
		 	 })
		 	 $(e.target.parentNode.parentNode).find(".huang_button").css({
		 	 	"display":"block"
		 	 })
		  		self.reply = true;
		  	}else if(self.reply == true){
                $(e.target.parentNode.parentNode).css({
		 	 	"min-height":"90px"
		 	    })
		 		$(e.target.parentNode.parentNode).find(".huang_checkbox").css({
		 	 	'visibility': 'hidden',
		 	 	'display': 'none'
			 	 })
			 	 $(e.target.parentNode.parentNode).find(".huang_span").hide()
			 	 $(e.target.parentNode.parentNode).find(".huang_button").hide();
		  		$(e.target.parentNode.parentNode).find("._reply_textarea").hide();
		  		self.reply = false;
		  	}
		  },
		  //我的子评论回复发表
		  huang_publishmy(e,index){
		  	    var self = this;
		  	    var $recovery_zone = e.target.parentNode;
		  	    var $textarea_ = $($recovery_zone).find("._reply_textarea");
		  	    if($($recovery_zone).find(".huang_checkbox").is(":checked")){
		  	    self.anon = 1;
		  	    if($textarea_.val() != ""){
		  		$.post({
		  		url:" /api/v1/comment/create/"+ self.product_id +"?anon="+ self.anon + "&sid=" + self.huang_Array[index].id + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                    comment:$textarea_.val()
                }),
                success:function(res){
                console.log(res)
                self.huang_Array = res.data.list;
                self.total_count++;
                for(let i=0;i<self.my_Array.length;i++){
                		if(self.my_Array[i].id == res.data.comment_id){
                			self.my_Array[i].comment_count++;
                		}
                }
                self.reply = false;
                $textarea_.val("");
                $($recovery_zone).find(".huang_checkbox").hide();
                $($recovery_zone).find(".huang_button").hide();
                $($recovery_zone).find(".huang_span").hide();
                $($recovery_zone).find("._reply_textarea").hide();
                $($recovery_zone).css({
                	"min-height":"90px"
                });
                },
                error:function(error){
                	console.log(error)
                }
		  	   })	
		  		}
		  	    }else{
		  	    self.anon = 0;
		  	    if($textarea_.val() != ""){
		  		$.post({
		  		url:" /api/v1/comment/create/"+ self.product_id +"?anon="+ self.anon + "&sid=" + self.huang_Array[index].id + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                    comment:$textarea_.val()
                }),
                success:function(res){
                console.log(res)
                self.huang_Array = res.data.list;
                self.total_count++;
                for(let i=0;i<self.my_Array.length;i++){
                		if(self.my_Array[i].id == res.data.comment_id){
                			self.my_Array[i].comment_count++;
                		}
                }
                $textarea_.val("");
                $($recovery_zone).find(".huang_checkbox").hide();
                $($recovery_zone).find(".huang_button").hide();
                $($recovery_zone).find(".huang_span").hide();
                $($recovery_zone).find("._reply_textarea").hide();
                $($recovery_zone).css({
                	"min-height":"90px"
                });
                },
                error:function(error){
                	console.log(error)
                }
		  	   })	
		  	}
		}
		  	 },
		//回复“评论我的”
		tome_reply(e,index){
			var self = this;
			var $huang_li = $(e.target.parentNode.parentNode.parentNode);
			console.log($huang_li);
			if(self.huang_jian == false){
				$huang_li.css({
				"min-height":"180px"
			})
				$huang_li.find("._textarea").show();
				$huang_li.find("._button").show();
				$huang_li.find("._span").show();
				$huang_li.find("._checkbox").css({
					'display': 'block',
					'visibility': 'visible'
				})
				self.huang_jian = true;
			}else if(self.huang_jian == true){
				$huang_li.css({
				"min-height":"80px"
			    })
				$huang_li.find("._textarea").hide();
				$huang_li.find("._button").hide();
				$huang_li.find("._span").hide();
				$huang_li.find("._checkbox").css({
					'display': 'none'
				});
				self.huang_jian = false;
			}
		},
		hj_replyPost(e,index){
			var self = this;
			var $huangj_li = $(e.target.parentNode.parentNode);
			var $huang_textarea = $huangj_li.find("._textarea");
			var $huang_checkbox = $huangj_li.find("._checkbox");
			if($huang_checkbox.is(":checked")){
				self.anon = 1;
			}else{
				self.anon = 0;
			}
			if($huang_textarea.val() != ""){
			$.post({
		  		url:" /api/v1/comment/create/"+ self.product_id +"?anon="+ self.anon + "&cid=" + self.tome_Array[index].id + '&lang=' + self.lang,
		  		contentType:'application/json;chartset=utf-8',
		  		data:JSON.stringify({
                    comment:$huang_textarea.val()
                }),
                success:function(res){
                console.log(res);
                $huangj_li.css({
				       "min-height":"100px"
			    })
                $huangj_li.find("._textarea").hide();
				$huangj_li.find("._button").hide();
				$huangj_li.find("._span").hide();
				$huangj_li.find("._checkbox").css({
					'visibility': 'hidden'
				});
                },
                error:function(error){
                	console.log(error)
                }
		  	   })	
			}
		},
		btn_del(e,index){
			var $_recovery_zone = $(e.target.parentNode.parentNode);
			$_recovery_zone.find(".prompt_box").css('visibility','visible');
		},
		btn_confirm(e,index){
			var self = this;
			var $myrecovery_zone = $(e.target.parentNode.parentNode);
			$.post({
              	url:"/api/v1/comment/delete/" + self.sondiscuss_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	console.log(res);
                	self.sondiscuss_Array.splice(index,1);
                	$myrecovery_zone.find(".prompt_box").css('visibility','hidden');
                	self.all_total_count--;
                	if(self.all_total_count <=0){
                	   if(self.all_total_count == 0){
                		$(".my_all_total_count").css('display','none');
                	    }
                	}
                	for(let i=0;i<self.discuss_Array.length;i++){
                		if(self.discuss_Array[i].id == res.data.comment_id){
                			self.discuss_Array[i].comment_count--;
                		}
                	}
                },
                error:function(error){
                	console.log(error);
                }
            })
		},
		btn_unconfirm(e,index){
			var $huang_recovery_zone = $(e.target.parentNode.parentNode);
			$huang_recovery_zone.find(".prompt_box").css('visibility','hidden');
		},
		huang_btn_del(e,index){
			var $huang_recovery_zone = $(e.target.parentNode.parentNode);
			$huang_recovery_zone.find(".prompt_box").css('visibility','visible');
		},
		huang_btn_confirm(e,index){
			var self = this;
			var $huangrecovery_zone = $(e.target.parentNode.parentNode);
			$.post({
              	url:"/api/v1/comment/delete/" + self.huang_Array[index].id,
             	contentType:'application/json;chartset=utf-8',
                success:function(res){
                	self.huang_Array.splice(index,1);
                	$huangrecovery_zone.find(".prompt_box").css('visibility','hidden');
                	self.total_count--;
                	if(self.total_count <=0){
                	   if(self.total_count == 0){
                		$(".huang_all_total_count").css('display','none');
                	    }
                	}
                	for(let i=0;i<self.my_Array.length;i++){
                		if(self.my_Array[i].id == res.data.comment_id){
                			self.my_Array[i].comment_count--;
                		}
                	}
                },
                error:function(error){
                	console.log(error);
                }
            })
		},
		huang_btn_unconfirm(e,index){
			var $huangjian_recovery_zone = $(e.target.parentNode.parentNode);
			$huangjian_recovery_zone.find(".prompt_box").css('visibility','hidden');
		}
	  }
	}
</script>
<style type="text/css" scoped>
.dashboard-header{
		position: absolute;
		top: 0;
		width: 100%;
		padding: 18px 32px 0;
		height: 50px;
		background: #151922;
		color: #596175;
		border-bottom: 1px solid #1b1f29;
	}
	.left-item{
		float: left;
	}
	.header-left .btn-back{
		font-weight: bold;
		font-size: 12px;
		line-height: 15px;
		cursor: pointer;
		display: inline-block;
		vertical-align: top;
	}
	.header-left .btn-back:before{
		content: ' <';
		margin-right: 10px;
	}
	.header-left .btn-back:hover{
		color: #92bdd2;
	}
	.language-tab{
		display: inline-block;
		vertical-align: top;
		margin-top: -5px;
		margin-left: 45px;
		border-radius: 3px;
	}
	.language-tab .btn-lang{
		float: left;
		background: #1b1f29;
		width: 75px;
		height: 26px;
		line-height: 25px;
		text-align: center;
		cursor: pointer;
		color: #596175;
		font-size: 12px;
	}
	.language-tab .btn-lang.active{
		color: #19BDEC;
		background: #20242e;
	}
	.right-item{
		float: right;
	}
	.right-item .left-item{
		margin-left: 10px;
	}
	.header-right .rights-text,
	.header-right .rights-handle{
		display: inline-block;
		vertical-align: top;
		font-size: 12px;
		padding: 0 5px;
		line-height: 15px;
	}
	.header-right .rights-handle{
		cursor: pointer;
		text-decoration: underline;
	}
	.header-right .rights-handle:hover{
		color: #92bdd2;
	}
	.mid_line{
		display: block;
		height: 15px;
		width: 1px;
		float: left;
		background-color: #2e444c;
		margin-left: 15px;
	}
	/*评论*/
	.discuss{
		text-align: center;
		width: 50px;
		height: 30px;
		line-height: 30px;
		background:deepskyblue;
		color:white;
		clear: both;
		float: left;
		font-size: 12px;
		margin-top:-8px;
		position:relative;
		cursor: pointer;
	}
	.discuss .radius_span{
		width: 10px;
		height: 10px;
		border-radius: 50%;
		background:red;
		clear: both;
		float: right;
		position: absolute;
		top: 3px;
		right:3px;
		cursor: pointer;
		display: none;
	}
	.discussDiv{
		font-size: 12px;
		display: none;
		background: #151922;
		width: 400px;
		height: 700px;
		z-index: 999;
		position: absolute;
		top: 45px;
		right:5px;
	}
	.discussDiv .discuss_shape{
		display: none;
		z-index:999;
		opacity:0.8;
		position:absolute;
		left: 0;
		right: 0;
		top: 0;
		background: #151922;
		width: 400px;
		height: 700px;
	}
	.discussDiv .huang_tooltip{
		display: none;
		position: absolute;
		left: 60px;
		top: 260px;
		background:#151922;
		z-index: 9999;
		width: 260px;
		height:20px;
		text-align: center;
		line-height:20px;
		color:white;
	}
	.discussDiv .my_span{
		width: 10px;
		height: 10px;
		border-radius: 50%;
		background:red;
		clear: both;
		float: right;
		position: absolute;
		margin-right:5px;
		margin-top: 5px;
		display: none;
	}
	.discussDiv h1{
	    color:gray;
		font-size:16px;
		position: absolute;
		right:5px;
		top:5px;
	}
	.discussDiv .tab_bar{
		width: 400px;
		height: 35px;
		cursor: pointer;
	}
	.discussDiv .tab_bar li:first-child{
		margin-left: 0;
	}
	.discussDiv .tab_bar li{
		display: block;
		width: 60px;
		height: 35px;
		line-height: 35px;
		text-align: center;
		float: left;
		color: deepskyblue;
		cursor:pointer;
	}
	.liDefault{
		background: deepskyblue;
		color: white !important;
	}
	.comment_div{
		width: 398px;
		height: 650px;
		/*border: 1px solid gray;*/
		border: 1px solid #2E2F3B;
		color: #7985A3;
		overflow-y: auto;
		overflow-x: hidden;
	}
	.comment_div .hot_span,
	.comment_div .lasted_span,
	.comment_div .common_span{
		width: 10px;
		height: 10px;
		border-radius: 50%;
		background:red;
		clear: both;
		float: right;
		position:absolute;
		top:0;
		right:0;
		cursor: pointer;
		display: none;
	}
	.comment_div .content_header{
		width: 400px;
		height:30px;
	   margin-top: 20px;
	   cursor: pointer;
	}
	.comment_div .content_header button{
		float: right;
		margin-right:22px;
		text-indent:15px;
		width:110px;
		padding:3px;
	}
	.comment_div .content_header li{
		display: block;
		float: left;
		padding:0 10px;
		border-right:1px solid #7985A3;
		position: relative;
	}
	.comment_div .content_header li:nth-child(4){
		border-right: none;
		padding-left: 30px;
		position: relative;
	}
	.comment_div .content_header li img{
		position: absolute;
		right:15px;
		top: 2px;
	}
	.comment_div .content_header li:first-child{
		padding-left: 20px;
	}
	.comment_div .content_header li:nth-child(3){
		border-right:none;
	}
	.comment_div .content_header li .search_input:focus::-webkit-input-placeholder{
            text-indent: -999em;
            z-index: -20;
      }
	.comment_div .content_header li .search_input{
		float: right;
		text-indent:7px;
		color: white;
		width:180px;
		border: 1px solid #8B9BBF;
        border-radius: 3px 4px 4px 4px;
		/*border:1px solid gray;*/
	}
	.comment_div .content_header li .search_input::-webkit-input-placeholder{
	     color: #7985A3;
	     opacity:1;	
	}
	.li_Default{
		color: white;
	}
	.comment_div .content_discuss{
		width: 100%;
		height: 520px;
		overflow-y: auto;
		overflow-x: hidden;
	}
	/*确认框*/
	.comment_div .content_discuss .prompt_box{
		visibility: hidden;
		width: 130px;
		height:55px;
		border: 1px solid gray;
		position: absolute;
		right:10px;
		top:5px;
		z-index: 999;
		cursor: pointer;
	}
	.comment_div .content_discuss .prompt_box h5{
		padding-left:30px;
	}
	.comment_div .content_discuss .prompt_box .confirm{
		width: 47px;
		height: 22px;
		line-height: 22px;
		background: deepskyblue;
		color: black;
		border-radius:5px;
		margin-left: 15px;
		font-size: 8px;
	}
	.comment_div .content_discuss .prompt_box .unconfirm{
		width: 47px;
		height: 20px;
		line-height:20px;
		border-radius:5px;
		margin-left:10px;
		border: 1px solid gray;
		font-size: 8px;
	}
	.comment_div .content_discuss li{
		width:398px;
		/*border: 1px solid gray;*/
		border: 1px solid #2E2F3B;
		position: relative;
		border-top:none;
		border-left:none;
		overflow: hidden;
	}
	.comment_div .content_discuss li .comment_h5{
		word-wrap:break-word;
		width:330px;
		line-height: 20px;
		font-family: PingFangSC-Regular;
		font-size: 14px;
		color: #8B9BBF;
		letter-spacing: 0;
	} 
	.comment_div .content_discuss li .my_all_total_count{
		height:20px;
	 	width: 130px;
	 	line-height:20px;
	 	padding-top:4px;
	 	display: none;
	}
    .comment_div .content_discuss li .my_all_total_count .all_total_count{
    	width: 60px;
    	display: block;
    	float: left;
    }
    .comment_div .content_discuss li .my_all_total_count span{
    	height: 20px;
    }
	/*子评论框*/
	.comment_div .content_discuss li .recovery_zone{
		width:320px;
		min-height:70px;
		height: auto !important;
		overflow: hidden;
		margin-top:10px;
		border-top: 1px dashed gray;
		position: relative;
		cursor: pointer;
	}
	.comment_div .content_discuss li .recovery_zone .blue_radius{
		display: block;
		float: left;
		width: 18px;
		height:18px;
		border-radius: 50%;
		background: deepskyblue;
		margin-left:8px;
		margin-top: 15px;
	}
	.comment_div .content_discuss li .recovery_zone .h5{
		float: left;
		padding-left: 5px;
		height: 20px;
		line-height: 20px;
		width: 90%;
		padding-top:10px;
	}
.comment_div .content_discuss li .recovery_zone ._p{
		float:right;
		width: 90%;
		word-wrap:break-word;
		padding-top: 10px;
		line-height: 20px;
	}
	.comment_div .content_discuss li .recovery_zone h6{
		float: right;
		width:315px;
		height: 15px;
		margin-top:15px;
		margin-bottom:6px;
		position:relative;
	}
	.comment_div .content_discuss li .recovery_zone h6 .time_p{
		margin-left:25px;
		float: left;
		width: 100px;
		margin-top: 2px;
	}
	.comment_div .content_discuss li .recovery_zone h6 ._reply{
		 position: absolute;
		 right: 5px;
	}
	.comment_div .content_discuss li .recovery_zone h6 .del{
		 float: right;
		 margin-right: 45px;
	}
	.comment_div .content_discuss li .recovery_zone .reply_textarea{
		display: none;
		float: left;
		width:320px;
		height: 50px;
		margin-top: 13PX;
		 resize:none;
		 border: 1px solid gray;
	}
	.comment_div .content_discuss li .recovery_zone .huang_checkbox{
		display: none;
		visibility: hidden;
		z-index:1;
		position: absolute;
		left:5px;
		bottom:5px;
		width: 18px;
		height: 18px;
	}
	.comment_div .content_discuss li .recovery_zone .huang_span{
		display: none;
		float: left;
		font-size:12px;
		width: 100px;
		height: 15px;
		position: absolute;
		bottom:8px;
		left:30px;
	}
	.comment_div .content_discuss li .recovery_zone .huang_button{
		display: none;
		float: right;
		margin-top: 5px;
		width: 180px;
		height: 30px;
		line-height: 20px;
		color: black;
		border-radius: 5px;
		background: deepskyblue;
	}
	.comment_div .content_discuss li .recovery_zone .prompt_box{
		position:absolute;
		right: 0;
		top: 0;
		width: 135px;
		height: 60px;
		background: #151922;
	}
	.comment_div .content_discuss li .recovery_zone .prompt_box h5{
		
	}
	.comment_div .content_discuss li .recovery_zone .prompt_box .confirm{
	}
	.comment_div .content_discuss li .recovery_zone .prompt_box .unconfirm{
		
	}
	.comment_div .content_discuss i{
		font-style: normal;
	} 
	.comment_div .content_discuss .blue_radius{
		display: block;
		float: left;
		width: 30px;
		height: 30px;
		border-radius: 50%;
		background: deepskyblue;
		margin-left:20px;
		margin-top: 20px;
	}
	.comment_div .content_discuss ._div{
		width:330px;
		min-height:80px;
		float: left;
		margin-left: 10px;
		margin-top: 10px;
	}
	.comment_div .content_discuss ._div h5{
		margin: 7px 0;
	}
	.comment_div .content_discuss ._div .huang_div{
		display: block;
		width: 330px;
		min-height: 30px;
		cursor: pointer;
	}
	.comment_div .content_discuss ._div .left{
		float: left;
	}
	.comment_div .content_discuss ._div .right{
		float:right;
		padding-right: 5px;
	}
	.comment_div .content_discuss ._div .discuss_textarea{
       display: none;	
       color:white;
       width:320px;
       height:50px;
       resize:none;
       margin-top: 5px;
       border: 1px solid gray;
	}
	.comment_div .content_discuss ._div .discuss_checkbox{
		display: block;
		width: 18px;
		height: 18px;
		border: 1px solid gray;
		position: absolute;
		z-index: 999;
		top: -2px;
		left: 8px;
	}
	.comment_div .content_discuss ._div .discuss_button{
		display:none;
		width: 180px;
		height: 30px;
		line-height: 30px;
		color: black;
		background: deepskyblue;
		margin-left:130px;
		margin-top: 5px;
	}
	.comment_div .content_discuss li:last-child{
		display: block;
		position: absolute;
		bottom:0;
		left: 0;
		right: 0;
		z-index:998;
		background:#151922;
		/*border-left:1px solid gray;*/
		border-left: 1px solid #2E2F3B;
	}
	.comment_div .content_discuss li ._textarea{
		color:#7985A3;
		display: block;
		float: left;
		resize:none;
		width: 220px;
		height: 40px;
		/*border: 1px solid gray;*/
		border: 1px solid #525466;
        border-radius: 6px;
		margin-left: 55px;
		margin-bottom: 10px;
	}
	.comment_div .content_discuss li .Button{
		display: block;
		width: 50px;
		height:38px;
		line-height:38px;
		float: left;
		margin-left: 25px;
		margin-bottom: 10px;
		background: deepskyblue;
		color:#ffffff;
		border-radius:20%;
	}
	.comment_div .content_discuss li .anonymity{
		display: block;
		width: 16px;
		height: 16px;
		border: 1px solid gray;
		margin-left: 65px;
		margin-top:-30px;
		float:left;
		z-index: 999;
	}
	.comment_div .content_discuss li .anonymity_span{
		display:none;
		position: absolute;
		left: 90px;
		top: 85px;
		z-index: 999;
		color:#7985A3;
		font-size: 12px;
	}
	.comment_div .content_discuss li .h6{
		display: none;
		width: 80px;
		height:18px;
		line-height: 18px;
		text-align: center;
		vertical-align: middle;
		position: relative;
		float: left;
		padding-top: -25px;
		z-index: 998;
	}
	.comment_div .content_discuss li ._anonymity_span{
		display:none;
		position: absolute;
		z-index: 999;
		float: left;
		color:#7985A3;
	}
	.discussDiv .nickName{
		display: none;
		width: 400px;
		height: 700px;
		border: 1px solid gray;
		z-index:1200;
		position: absolute;
		left: 0;
	    top: 0;
	    opacity: 0.8;
	    }
	.discussDiv .nickName input{
		display: block;
		width: 180px;
		height: 40px;
		text-indent: 10px;
		margin-left:90px;
		margin-top: 300px;
		padding-bottom:4px;
		border: 1px solid gray;
		color: white;
		float: left;
	}
	.discussDiv .nickName input:focus::-webkit-input-placeholder{
            text-indent: -999em;
            z-index: -20;
     }
	.discussDiv .nickName .sure{
		float: left;
		display: block;
		width: 50px;
		height:40px;
		float:left;
		margin-left:280px;
		margin-top: -41px;
		border-radius:5px;
		font-size:10px;
		color: black;
		background: deepskyblue;
	}
	.discussDiv .nickName .unsure{
		display: block;
		width: 30px;
		height:18px;
		text-align: center;
		float:right;
		margin-right: -5px;
		margin-top:-335px;
		font-size:14px;
	}
	.discussDiv .nickName_{
		display: none;
		width: 400px;
		height: 700px;
		border: 1px solid gray;
		z-index:1200;
		position: absolute;
		left: 0;
		top:0;
	}
	.discussDiv .nickName_ input{
		display: block;
		width: 180px;
		height: 40px;
		text-indent: 10px;
		margin-left:90px;
		margin-top: 300px;
		padding-bottom:4px;
		border: 1px solid gray;
		color: white;
		float: left;
	}
	.discussDiv .nickName_ input:focus::-webkit-input-placeholder{
            text-indent: -999em;
            z-index: -20;
           }
	.discussDiv .nickName_ .sure_{
		float: left;
		display: block;
		width: 50px;
		height:40px;
		float:left;
		margin-left:280px;
		margin-top: -41px;
		border-radius:5px;
		font-size:10px;
		color: black;
		background: deepskyblue;
	}
	.discussDiv .nickName_ .unsure_{
		display: block;
		width: 30px;
		height:18px;
		text-align: center;
		float:right;
		margin-right: -5px;
		margin-top:-335px;
		font-size:14px;
	}
	/*分页*/
	.comment_div .content_discuss .paging{
	  margin: 0 auto;
	  display: flex;
	  width:100%;
	  height:30px;
	  line-height:30px;
	  text-align: center;
	  flex-wrap:wrap;
	 align-content:center;
	justify-content: center;	
	}
	.comment_div .content_discuss .paging a{
	display: inline-block;
	border:1px solid gray;
	color:#999;
	width:60px;
	}
	
	/*我的*/
	.comment_my{
		display: none;
		width: 398px;
		height: 650px;
		/*border: 1px solid gray;*/
		border: 1px solid #2E2F3B;
		color: #7985A3;
	}
	.comment_my .content_header{
		width: 400px;
		height: 30px;
	   margin-top: 20px;
	   cursor: pointer;
	}
	.comment_my .content_header .tome_span{
		width: 10px;
		height: 10px;
		border-radius: 50%;
		background:red;
		clear: both;
		float: right;
		margin-right:4px;
		margin-top: 2px;
		display: none;
		position: absolute;
	}
	.comment_my .content_header .like_span{
		width: 10px;
		height: 10px;
		border-radius: 50%;
		background:red;
		clear: both;
		float: right;
		margin-right:4px;
		margin-top: 2px;
		display: none;
		position: absolute;
	}
	.comment_my .content_header li{
		display: block;
		float: left;
		padding:0 10px;
		border-right:1px solid #7985A3;
		cursor: pointer;
	}
	.comment_my .content_header li:first-child{
		padding-left: 20px;
	}
	.comment_my .content_header li:last-child{
		border-right:none;
	}
	.comment_my .content_discuss{
		width: 100%;
		height:520px;
		overflow-y: auto;
		overflow-x: hidden;
	}
	.comment_my .content_discuss .my_div{
		width:330px;
		min-height:80px;
		height: auto !important;
		float: left;
		margin-left: 10px;
		margin-top: 10px;
	}
	.comment_my .content_discuss .my_div .my_div_h5{
		width: 330px;
		margin: 7px 0;
		word-wrap:break-word;
	}
	.comment_my .content_discuss li{
		width:398px;
		position: relative;
		/*border: 1px solid gray;*/
		border:1px solid #2E2F3B;
		overflow:hidden;
		border-bottom: none;
	}
	.comment_my .content_discuss li:first-child{
		border-top:none;
	}
	.comment_my .content_discuss li:last-child{
		height: 51px;
		border-top:none;
		position: absolute;
		bottom: 0;
		left: 0;
		right: 0;
		z-index:9999;
		background:#151922;
		/*border-bottom: 1px solid gray;*/
		border-bottom: 1px solid #2E2F3B;
	}
	.comment_my .content_discuss i{
		font-style: normal;
	}
	.comment_my .content_discuss .content_discuss_my_li{
		border-left: none;
	}
	.comment_my .content_discuss .blue_radius{
		display: block;
		float: left;
		width: 30px;
		height: 30px;
		border-radius: 50%;
		background: deepskyblue;
		margin-left:20px;
		margin-top: 20px;
	}
	.comment_my .content_discuss .my_div p{
		width: 300px;
		padding-top:4px;
	}
	.comment_my .content_discuss .my_div .left{
		float: left;
	}
	.comment_my .content_discuss .my_div .right{
		cursor: pointer;
		float: right;
	}
	.comment_my .content_discuss li ._textareamy{
		color:#7985A3;
		display: block;
		float: left;
		resize:none;
		width: 220px;
		height: 40px;
		/*border: 1px solid gray;*/
		border: 1px solid #525466;
        border-radius: 6px;
		margin-left: 55px;
	}
	.comment_my .content_discuss li .anonymity_my{
		display: block;
		float: left;
		width: 16px;
		height:16px;
		margin-left: 60px;
		margin-top: -30px;
		z-index: 999;
	}
	.comment_my .content_discuss li .Button_my{
		display: block;
		width: 50px;
		height:38px;
		line-height:38px;
		float: left;
		margin-left: 25px;
		margin-bottom: 10px;
		background:deepskyblue;
		color:#ffffff;
		border-radius:20%;
	}
	.comment_my .content_discuss li .anonymity_spanmy{
		display: none;
		position: absolute;
		left:85px;
		top: 80px;
		z-index: 999;
		color:#7985A3;
		font-size: 12px;
	}
	.comment_my .content_discuss .prompt_box{
		visibility: hidden;
		width: 130px;
		height:55px;
		border: 1px solid gray;
		position: absolute;
		right:10px;
		top:5px;
		z-index: 9999;
	}
	.comment_my .content_discuss .prompt_box h5{
		margin-top:7px;
		margin-left:30px;
	}
	.comment_my .content_discuss .prompt_box .confirm{
		width: 47px;
		height: 22px;
		line-height: 22px;
		background: deepskyblue;
		color: black;
		border-radius:5px;
		margin-left: 15px;
		margin-top: 5px;
		font-size: 8px;
	}
	.comment_my .content_discuss .prompt_box .unconfirm{
		width: 47px;
		height: 20px;
		line-height:20px;
		border-radius:5px;
		margin-top:5px;
		margin-left:10px;
		border: 1px solid gray;
		font-size: 8px;
	}
	.comment_my .content_discuss .my_textarea{
		display: none;
		resize:none;
		width: 320px;
		height: 50px;
		margin-top:10px;
		border: 1px solid gray; 
	}
	.comment_my .content_discuss .my_checkbox{
		float: left;
		margin-left: 15px;
		margin-top:10px;
	}
	.comment_my .content_discuss ._anonymity_span{
		display: none;
		float: left;
		padding-top:11px;
	}
	.comment_my .content_discuss ._my_button{
		display: none;
		float: right;
		margin-right:30px;
		margin-top: 5px;
		width: 180px;
		height: 30px;
		line-height: 30px;
		background: deepskyblue;
		color: black;
		border-radius:5px;
	}
	.comment_my .content_discuss li .huang_all_total_count{
		height:20px;
		line-height:20px;
	 	width: 130px;
	 	margin-top:45px;
	 	display: none;
	}
	 .comment_my .content_discuss li .huang_all_total_count .total_count{
    	width: 60px;
    	height: 20px;
    	display: block;
    	float: left;
    	padding-top: 0;
    }
    .comment_my .content_discuss li .huang_all_total_count span{
    	height:20px;
    	width: 20px;
    	float: left;
    }
	.comment_my .content_discuss li ._recovery_zone{
		width:320px;
		min-height:90px;
		height: auto !important;
		margin-top:5px;
		border-top: 1px dashed gray;
		position: relative;
		overflow: hidden;
	}
	.comment_my .content_discuss li ._recovery_zone .blue_radius{
		display: block;
		float: left;
		width: 18px;
		height:18px;
		border-radius:50%;
		background: deepskyblue;
		margin-left:8px;
		margin-top: 12px;
	}
	.comment_my .content_discuss li ._recovery_zone .h5{
		float: right;
		height: 20px;
		line-height:20px;
		width: 288px;
		padding-top:4px;
		padding-left:4px;
	}
	.comment_my .content_discuss li ._recovery_zone ._h5{
		float:right;
		line-height: 20px;
		width:288px;
		padding-top: 4px;
		word-wrap: break-word;
	}
	.comment_my .content_discuss li ._recovery_zone h6{
		display: block;
		margin-top: 10px;
		margin-bottom: 6px;
		float: right;
		width:288px;
	}
	.comment_my .content_discuss li ._recovery_zone h6 p{
		width:100px;
	}
	.comment_my .content_discuss li ._recovery_zone h6 .del{
		float: right;
		margin-top:-15px;
		margin-right:45px;
	}
	.comment_my .content_discuss li ._recovery_zone h6 ._my_reply{
		float: right;
		margin-top:-15px;
		margin-right: 5px;
	}
	.comment_my .content_discuss li ._recovery_zone ._reply_textarea{
		display: none;
		float: left;
		width:320px;
		resize:none;
		border: 1px solid gray;
	}
	.comment_my .content_discuss li ._recovery_zone .huang_checkbox{
		display: none;
		visibility: visible;
		z-index:1;
		position: absolute;
		left:5px;
		bottom:4px;
		width: 18px;
		height: 18px;
	}
	.comment_my .content_discuss li ._recovery_zone .huang_button{
		display: none;
		float: right;
		margin-top:8px;
		width: 180px;
		height: 30px;
		line-height: 20px;
		color: black;
		border-radius: 5px;
		background: deepskyblue;
	}
	.comment_my .content_discuss li ._recovery_zone .huang_span{
		display: none;
		float:left;
		position: absolute;
		left:30px;
		bottom: 5px;
	}
	.comment_my .content_discuss li ._recovery_zone .prompt_box{
		position:absolute;
		right: 0;
		top: 0;
		width: 135px;
		height: 60px;
		background: #151922;
	}
	.comment_my .content_discuss_pinglun{
		display:none;
		width: 100%;
		height:600px;
		overflow-y: auto;
		overflow-x: hidden;
	}
	.comment_my .content_discuss_pinglun .pinglun_radius{
		display: block;
		float: left;
		width: 30px;
		height: 30px;
		border-radius: 50%;
		background: deepskyblue;
		margin-left:20px;
		margin-top: 15px;
	}
	.comment_my .content_discuss_pinglun li:last-child{
		border-bottom: none;
	}
	.comment_my .content_discuss_pinglun li{
		width:398px;
		position: relative;
		border: 1px solid gray;
		overflow:hidden;
		border-top: none;
	}
	.comment_my .content_discuss_pinglun .huang_div{
		width:330px;
		min-height:80px;
		float: left;
		margin-left: 10px;
		margin-top: 10px;
	}
	.comment_my .content_discuss_pinglun .huang_div ._p{
		display: block;
		background: gray;
		height: 15px;
		width: 5px;
		float: left;
	}
	.comment_my .content_discuss_pinglun .huang_div h5{
		padding-top: 5px;
		word-wrap:break-word;
		width:330px;
	}
	.comment_my .content_discuss_pinglun .huang_div h5 .huangjian_span{
		font-style: normal;
		line-height: 20px;
	}
	.comment_my .content_discuss_pinglun .huang_div .pinglun-h5{
		line-height: 20px;
	}
	.comment_my .content_discuss_pinglun .huang_div span{
		padding-left: 5px;
	}
	.comment_my .content_discuss_pinglun .huang_div h6{
		height: 30px;
		width: 90%;
		padding-top: 10px;
	}
	.comment_my .content_discuss_pinglun .huang_div h6 .time_p{
		display: block;
		float: left;
	}
	.comment_my .content_discuss_pinglun .huang_div h6 button{
		float: right;
	}
	.comment_my .content_discuss_pinglun .huang_div .prompt_box{
		display: none;
		width: 130px;
		height:55px;
		border: 1px solid gray;
		position: absolute;
		right:10px;
		top:0;
		z-index: 9999;
	}
	.comment_my .content_discuss_pinglun .huang_div .prompt_box h5{
		padding-left:30px;
		padding-top: 3px;
	}
	.comment_my .content_discuss_pinglun .prompt_box .confirm{
		width: 42px;
		height: 20px;
		line-height: 20px;
		background: deepskyblue;
		color: black;
		font-size: 8px;
		border-radius:5px;
		margin-left: 20px;
	}
	.comment_my .content_discuss_pinglun .prompt_box .unconfirm{
		width: 40px;
		height: 20px;
		line-height:20px;
		font-size:8px;
		border-radius:5px;
		margin-left:10px;
		border: 1px solid gray;
		margin-top: 10px
	}
	/*我评论的*/
	.comment_my .content_discuss_pingluntome{
		display:none;
		width: 100%;
		height:580px;
		overflow-y: auto;
		overflow-x: hidden;
	}
	.comment_my .content_discuss_pingluntome li:last-child{
		border-bottom: none;
	}
	.comment_my .content_discuss_pingluntome li{
		width:398px;
		/*min-height:130px;
		height: auto !important;*/
		overflow: hidden;
		border: 1px solid gray;
		border-top: none;
	}
	.comment_my .content_discuss_pingluntome li .pinglun_radius{
		display: block;
		float: left;
		width: 30px;
		height: 30px;
		border-radius: 50%;
		background: deepskyblue;
		margin-left:20px;
		margin-top: 5px;
	}
	.comment_my .content_discuss_pingluntome li div{
		width:330px;
		min-height:80px;
		float: left;
		margin-left: 10px;
		position: relative;
	}
	.comment_my .content_discuss_pingluntome div ._p{
		display: block;
		background: gray;
		height: 15px;
		width: 5px;
		float: left;
	}
	.comment_my .content_discuss_pingluntome div span{
		padding-left: 5px;
	}
	.comment_my .content_discuss_pingluntome div h5{
		padding-top:5px;
		word-wrap:break-word;
		width:330px;
	}
	.comment_my .content_discuss_pingluntome div h5 .to_me_span{
		line-height: 20px;
	}
	.comment_my .content_discuss_pingluntome div h6{
		display: block;
		height: 30px;
		width:90%;
		padding-top:5px;
	}
	.comment_my .content_discuss_pingluntome div h6 .time_p{
		display: block;
		height: 18px;
		width: 100px;
		float: left;
	}
	.comment_my .content_discuss_pingluntome div h6 button{
		float: right;
		height: 18px;
		width: 50px;
	}
	.comment_my .content_discuss_pingluntome div  ._textarea{
		display: none;
	    resize: none;
	    width: 320px;
	    height: 50px;
	    margin-top: 10px;
	    border: 1px solid gray;
	}
	.comment_my .content_discuss_pingluntome div  ._button{
		display: none;
	    float: right;
	    margin-right:10px;
	    margin-top: -25px;
	    width: 180px;
	    height: 30px;
	    line-height: 20px;
	    color: black;
	    border-radius: 5px;
	    background: deepskyblue;
	}
	.comment_my .content_discuss_pingluntome div  ._span{
		display: none;
		position: absolute;
		left: 30px;
		bottom:5px;
	}
	.comment_my .content_discuss_pingluntome div  ._checkbox{
		/*visibility: hidden;*/
		display: none;
		width: 18px;
		height:18px;
		margin-left: 5px;
		margin-top: 13px;
	}
	/*赞我的*/
	.comment_my .content_discuss_praise{
		display:none;
		width: 100%;
		height:580px;
		overflow: hidden;
	}
	.comment_my .content_discuss_praise li:last-child{
		border-bottom: none;
	}
	.comment_my .content_discuss_praise li{
		width:398px;
		min-height: 90px;
		height: auto !important;
		border: 1px solid gray;
		border-top: none;
	}
	.comment_my .content_discuss_praise li .pinglun_radius{
		display: block;
		float: left;
		width: 30px;
		height: 30px;
		border-radius: 50%;
		background: deepskyblue;
		margin-left:20px;
		margin-top: 5px;
	}
	.comment_my .content_discuss_praise li div{
		width:330px;
		min-height:80px;
		float: left;
		margin-left: 10px;
		margin-top: 10px;
	}
	.comment_my .content_discuss_praise div ._p{
		display: block;
		background: gray;
		height: 15px;
		width: 5px;
		float: left;
	}
	.comment_my .content_discuss_praise div h5{
		padding-top: 5px;
		word-wrap:break-word;
		width:330px;
	}
	.comment_my .content_discuss_praise div span{
		padding-left: 5px;
	}
	.comment_my .content_discuss_praise div h6{
		height: 15px;
		line-height: 15px;
		width: 90%;
		margin-top:10px;
	}
	.comment_my .content_discuss_praise div h6 .time_p{
		display: block;
		float: left;
	}
</style>