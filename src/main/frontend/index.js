import Vue from "vue"
import VueRouter from 'vue-router'
import App  from './views/app'

import './scripts/utils'
import './scripts/chart-helper'
import './scripts/chart-timer'
import './scripts/file-export'
import './scripts/ga-helper'

Vue.use(VueRouter);

import store from './store/'
import router from './router'

//自定义时间戳过过滤器
//value为13位的时间戳
Vue.filter('h_timechange', function (value) {
            function add0(m) {
                return m < 10 ? '0' + m : m
            }
            var time = new Date(parseInt(value));
            var y = time.getFullYear();
            var m = time.getMonth() + 1;
            var d = time.getDate();
            return y + '.' + add0(m) + '.' + add0(d);
        });

Vue.filter('hj_timechange', function (value) {
            function add0(m) {
                return m < 10 ? '0' + m : m
            }
            var time = new Date(parseInt(value));
            var y = time.getFullYear();
            var m = time.getMonth() + 1;
            var d = time.getDate();
            var H = time.getHours();
            var i = time.getMinutes();
            return y + '-' + add0(m) + '-' + add0(d) + '' + add(H) + ':' + add(i);
        });
//当Vue实例没有el属性时，则该实例尚没有挂载到某个dom中；
//假如需要延迟挂载，可以在之后手动调用vm.$mount()方法来挂载
// 这里的render: h => h(App)是es6的写法
// 转换过来就是：  暂且可理解为是渲染App组件
// render:(function(h){
//  return h(App);
// });
//将store和router交给vue实例
let vm = new Vue({
	store,
	router,
	render: h => h(App)
}).$mount('#app')

//自适应
U.rem();

// 预加载
U.loadImg(
	[
		'/images/img-banner.jpg',
		'/images/img-description.jpg',
		'/images/img-icon.png',
		'/images/icon-order.png',
		'/images/login-bg.jpg',
		'/images/price-bg.jpg',
		'/images/ctrip_01.png',
		'/images/baidu_01.png',
		'/images/vip_01.png',
		'/images/ctrip_01_nocolor.png',
		'/images/netease_01.png',
		'/images/weibo_01.png',
		'/images/jd_01.png',
		'/images/vip_01_nocolor.png',
		'/images/icon-order.png',
		'/images/cheetah_01.png',
		'/images/weibo_01_nocolor.png',
		'/images/netease_01_nocolor.png',
		'/images/baidu_01_nocolor.png',
		'/images/ali_01.png',
		'/images/jd_01_nocolor.png',
		'/images/icon-fullscreen-list.png',
		'/images/cheetah_01_nocolor.png',
		'/images/momo_01.png',
		'/images/ali_01_nocolor.png',
	]
)
//路由页面的权限验证
router.beforeEach((to, from, next) =>{
	if(/my/.test(to.fullPath) && !sessionStorage.getItem('uid')){
		next({name: 'introduction'})
	}else if(from.fullPath.match('/fullscreen')){
		store.commit('setFullScreenWatch',{isWatch:false});
	}
	next();
})