import VueRouter from 'vue-router'


const introduction = r => require.ensure([], () => r(require('./views/introduction/app')), 'introduction');
const order = r => require.ensure([], () => r(require('./views/order/app')), 'order');
const orderFinish = r => require.ensure([], () => r(require('./views/order-finish/app')), 'order-finish');
const orderhistory = r => require.ensure([], () => r(require('./views/orderhistory/app')), 'orderhistory');
const orderInformation = r => require.ensure([], () => r(require('./views/orderInformation/app')), 'orderInformation');
const waitingconfirmation = r => require.ensure([], () => r(require('./views/waitingconfirmation/app')), 'waitingconfirmation');
const waitingconfirmationdown = r => require.ensure([], () => r(require('./views/waitingconfirmationdown/app')), 'waitingconfirmationdown');
const orderfailed = r => require.ensure([], () => r(require('./views/orderfailed/app')), 'orderfailed');
const login = r => require.ensure([], () => r(require('./views/login/app')), 'login');
const registration = r => require.ensure([], () => r(require('./views/registration/app')), 'registration');
const register = r => require.ensure([], () => r(require('./views/register/app')), 'register');
const my = r => require.ensure([], () => r(require('./views/my/app')), 'my');
const dashboard = r => require.ensure([], () => r(require('./views/dashboard/app')), 'dashboard');
const fullscreen = r => require.ensure([], () => r(require('./views/fullscreen/app')), 'fullscreen');
const management = r => require.ensure([], () => r(require('./views/management/app')), 'management');
const manager = r => require.ensure([], () => r(require('./views/manager/app')), 'manager');
const managerinfo = r => require.ensure([], () => r(require('./views/managerinfo/app')), 'managerinfo');

//配置路由规则
const routes = [
	{
		path: '',
		name: 'introduction',
		component: introduction
	},
	{
		path: '/orderhistory/:id?',
		name: 'orderhistory',
		component: orderhistory
	},
	{
		path: '/orderInformation/:id?',
		name: 'orderInformation',
		component: orderInformation
	},
	{
		path: '/waitingconfirmation/:id?',
		name: 'waitingconfirmation',
		component: waitingconfirmation
	},
	{
		path: '/waitingconfirmationdown/:id?',
		name: 'waitingconfirmationdown',
		component: waitingconfirmationdown
	},
	{
		path: '/orderfailed/:id?',
		name: 'orderfailed',
		component: orderfailed
	},
	{
		path: '/order/:id?',
		name: 'order',
		component: order
	},
	{
		path: '/order-succeed/:id?',
		name: 'order-finish',
		component: orderFinish
	},
	{
		path: '/manager/:id?',
		name: 'manager',
		component: manager
	},
	{
		path: '/managerinfo/:id?',
		name: 'managerinfo',
		component: managerinfo
	},
	{
		path: '/login/:id?',
		name: 'login',
		component: login
	},
	{
		path: '/registration/:id?',
		name: 'registration',
		component: registration
	},
	{
		path: '/register/:email?',
		name: 'register',
		component: register
	},
	{
		path: '/dashboard/:bid?',
		name: 'dashboard',
		component: dashboard
	},
	{
		path: '/fullscreen/:bid?',
		name: 'fullscreen',
		component: fullscreen
	},
	{
		path: '/my/:id?',
		name: 'my',
		component: my
	},
	{
		path: '/backend/:id?',
		name: 'backend',
		component: management
	},
	{
		path: '*',
		name: 'not found',
		component: introduction
	}
]

//将路由规则交给router实例
const router =  new VueRouter({
	 routes
})

export default router
