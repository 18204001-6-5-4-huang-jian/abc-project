/*
	kzhang
	2017-5-10
 */
(function(W,$,doc){
	var utils = {
		getApiPre: function(){
			// 线上测试
			if(location.host === 'static.modeling.ai'){
				return location.protocol + '//data-dev.modeling.ai';
			// 本地dev
			}else if(/localhost/ig.test(location.host)){
				return ''
			}else{
				// 正式环境
				return location.protocol + '//' + location.host;
			}
		},
		loadImg:function(imgs,fn){
			if(!imgs){
				return false;
			}
			let loadNum = 0;
			let loadEle = $('#load-progress');
			if(typeof imgs === 'string'){
				let imgEl = new Image();
				imgEl.onload = ()=>{
					fn && fn();
				};
				imgEl.src = imgs;
			}else if(imgs instanceof Array){
				let totalNum = imgs.length;
				imgs.map((url)=>{
					let imgEl = document.createElement('img');
					imgEl.onload = ()=>{
						loadEle.html(parseInt(loadNum/totalNum*100)+"%");
						if(++loadNum === totalNum){
							fn && fn();
							$('.load-mask').hide()
						}
					};
					imgEl.src = url;
				})
			}
		},
		init: function(cb) {
			if(!$ || typeof $.cookie !== 'function'){console.log('jQuery and jQuery cookie is required');return false;}
			var token = $.cookie()['token'];
			if(!token || token == 'null' || !document.cookie){
				$.cookie('token',null,{path:'/'});
			}
			if(!window.sessionStorage){
				layer.msg('您的浏览器版本太低');
			}
			$.ajax({
				type: 'get',
				url: U.getApiPre() + "/api/v1/account/valid",
				success:function(resp){
					if(resp.success){
						cb && cb(true);
					}else{
						// layer.msg(resp.message || 'please login');
						layer.msg(resp.message);
						cb && cb(false);
					}
				},
				timeout:5000,
				error:function(err){
					cb && cb(false);
				}
			})
		},
		logout: function() {
			$.ajax({
				type: 'post',
				url:U.getApiPre() + '/api/v1/account/logout',
				data: {
					id:sessionStorage.getItem('uid')
				},
				success:function(resp){
					if(resp.success){
						sessionStorage.clear();
						$.cookie('token',null,{path:"/"});
						window.location.href = '/#/login';
					}else{
						layer.msg(resp.message || '登出失败');
					}
				},
				timeout: 5000,
				error:function(e){
					layer.msg('网络异常');
				}
			})
		},
		rem: function() {
			var docEl = doc.documentElement,    
			resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize',    
			recalc = function () {    
				var clientWidth = docEl.clientWidth;    
				if (!clientWidth) return;
				clientWidth == 320 ? clientWidth * 1.2 : clientWidth;
				docEl.style.fontSize = 10 * (clientWidth / 320) + 'px';    
			};    
			if (!doc.addEventListener) return;    
			W.addEventListener(resizeEvt, recalc, false);    
			doc.addEventListener('DOMContentLoaded', recalc, false); 
		}
	};
	if(typeof Object.create === 'function'){
		W.U = Object.create(utils)
	}else{
		var so = {};
		for (var i in utils) {
		　　if (so[i])
			continue; 
		　　so[i] = utils[i]; 
		} 
		W.U = so;
	}
})(window, jQuery, document)