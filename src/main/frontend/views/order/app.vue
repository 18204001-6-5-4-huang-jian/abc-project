<template>
<transition name="component-fade">
	<div class="order-wrap">
		<div class="order-head">
			<div class="order-nav-bar clearfix">
				<!-- <div class="logo abcdata-icon pointer" @click="direct('introduction')"></div> -->
				<div class="logo abcdata-icon pointer" @click="direct('my')"></div>
				<div class="login" @click="direct('login')" v-if="!isLogin">
					<i class="abcdata-icon icon-login"></i>
					<span>LOGIN</span>
				</div>
				<user-bar v-if="isLogin" :showDashboradEnter="true"></user-bar>	
			</div>
			<div class="item-960">
				<h1 class="order-title">Order Information</h1>
				<!--<p class="order-tips">If you are an old user, please login first and then place the order</p>-->
			</div>
		</div>
		<div class="form-wrapper">
			<div class="item-960 clearfix">
				<div class="steps solution-plan">
					<div class="step-name-container">
						<!-- <i class="steps-num abcdata-icon"></i> -->
						<span>Choose Solution Plan</span>
					</div>
					<div class="subname-container">
						Choose Solution Plan
					</div>
					<ul class="serve-category-list clearfix">
						<li class="serve-category" v-for="(plan, index) in planList" :class="{active:plan.name === curPlan.name,last:!((index+1)%3)}" @click="changePlan(plan,index)">
							<i class="abcdata-icon icon-yellow-checked-20"></i>
							<p v-text="plan.name" class="p"></p>
							<p v-text="plan.note" style="font-weight: bold;color: #000000;"></p>
						</li>
					</ul>
					<div class="production-title subname-container"  id="products">Add Companies</div>
					<ul class="company-list clearfix">
						<li class="selected-company company-capsule" v-if="company.select" v-for="(company, index) in productList">
							<span class="company-name" v-text="company.name" :title="company.name"></span>
							<i  class="abcdata-icon icon-delect" @click="deleteProduct(company,index)"></i>
						</li>
						<li class="btn-add-company company-capsule pointer"  @click="showAddMask">
							<i class="abcdata-icon icon-add"></i>
							Add
						</li>
					</ul>
					<div class="subname-container">
						Price
					</div>
					<ul class="price-list clearfix">
						<li class="price-list-card" v-for="(level,index) in curPlan.levels" :class="{last:!((index+1)%2),first:!index,active:curLevel.id === level.id}" @click="changeDuring(level)">
							<i class="abcdata-icon icon-yellow-checked-20"></i>
							<div class="amount">{{'￥'+level.price | currency}}</div>
							<div class="previously-amount" v-if="level.long !==0">{{'￥'+(level.price1) | currency}}</div>
							<div class="duration">
								<span v-text="level.long == 0?'Half a ':level.long == 1?' One ':' Two '">&nbsp;&nbsp;</span><span v-text="level.long>1?level.long_unit + 's':level.long_unit"></span>
							</div>
						</li>
					</ul>
					<div class="payment">支付方式 : </div>
					<div class="payment-style">
					<label><input name="payment" type="radio" value="" checked="checked"/>银行转账 </label> 
					<!--<label><input name="payment" type="radio" value="" />支付宝</label> 
					<label><input name="payment" type="radio" value="" />微信支付</label> -->
					</div>
				</div>
				<!--<div class="steps account-settings">
					<div class="step-name-container">
						<span>Account settings</span>
					</div>
					<div class="email-form account-form" id="email">
						<label class="account-form-label subname-container required">Work Email</label>
						<input type="email" class="email-input account-settings-input" v-model="form.email" :disabled="isLogin" />
					</div>
					<div class="name-form account-form" id="name">
						<label class="account-form-label subname-container required">Name</label>
						<input type="text" class="name-input account-settings-input" v-model="form.username" :disabled="isLogin"/>

					</div>
					<div class="company-form account-form" id="company">
						<label class="account-form-label subname-container required">Company</label>
						<input type="text" class="company-input account-settings-input" v-model="form.company.name" />
					</div>
					<div class="telephone-form account-form" id="telephone">
						<label class="account-form-label subname-container">Telephone</label>
						<input type="number" class="phone-input account-settings-input" v-model="form.phoneNumber" />
					</div>
					<div class="address-form account-form clearfix">
						<label class="account-form-label subname-container">Address</label>
						<my-select :optionsList="countrys" :fixed="false" :curSelect="curCountry" selectStyle="height:45px;float:left; width:30%;line-height:45px;border-top-left-radius:3px;border-bottom-left-radius:3px;" :optionsStyle="{width:selectListWidth}" @onSelect="selectOption"></my-select>
						<input type="text" class="company-address-input account-settings-input" v-model="form.company.addressDetail" />
					</div>
				</div>-->
<!-- 				<div class="steps terms-of-service">
					<div class="panel-left">
						<div class="step-name-container">
							<i class="steps-num abcdata-icon"></i>
							<span>Terms of service</span>
						</div>
					</div>
					<div class="panel-right">
						<div class="service-container">
							<div class="abc-service service-name">
								<i class="abcdata-icon"></i>
								<span>ABC terms of service</span>
							</div>
							<div class="abc-agreement service-name">
								<i class="abcdata-icon"></i>
								<span>ABC service agreement</span>
							</div>
						</div>
						<i class="abcdata-icon icon-warn"></i>
						<span>The agreements and terms will be sent via email after confirm of order. The full instruction will be attached as well.</span>
					</div>
				</div>
				<div class="steps terms-of-payment">
					<div class="panel-left">
						<div class="step-name-container">
							<i class="steps-num abcdata-icon"></i>
							<span>Terms of payment</span>
						</div>
					</div>
					<div class="panel-right">
						<div class="bank-remittance">
							<span>Bank remittance</span>
							<i class="abcdata-icon icon-warn"></i>
							<span class="bank-warn-text">Only support bank remittance way</span>
						</div>
					</div>
				</div> -->
			</div>
			<div class="form-footer item-960 clearfix" id="code">
				<div class="form-footer-right">
					<button class="btn-confirm" @click="confirmOrder">Confirm</button>
				</div>
			</div>
			<div class="order-footer clearfix item-960">
				<div class="copyright">
					Copyright &copy 2016 - 2017 Eversight.AI. All Rights Reserved. 
				</div>
			</div>
		</div>
		<transition name="component-fade">
			<div class="mask" v-show="showCompanys">
				<div class="mask-inner">
					<div class="dialog-head">
						<i class="abcdata-icon icon-close" @click="hideCompanys"></i>
						<span>Company</span>
						<span style="float:right;margin-right:6px;font-size:14px;cursor: pointer;margin-right:30px;" @click="allSelect()">
							<i></i>&nbsp;Select all</span>
					</div>
					<ul class="company-list dialog-body clearfix">
						<li class="company-capsule" v-for="(company, index) in tmp_productList" @click="addProduct(company)">
							<i class="abcdata-icon icon-yellow-checked" :class="{show:company.select}"></i>
							<span class="company-name" v-text="company.name" :title="company.name"></span>
						</li>
					</ul>
					<div class="dialog-foot">
						<button class="btn-confirm" @click="confirmAdd()">confirm</button>
					</div>
				</div>
			</div>
		</transition>
	</div>
</transition>
</template>
<script>
import userBar from '../common/userbar.vue'
import mySelect from '../common/select.vue'
import countrys from '../../scripts/world'
export default {
	name: 'order',
	data() {
		return {
			showCompanys: false,
			form:{
				email: '',
				username: '',
				phoneNumber: '',
				company: {
					name: '',
					addressCountry: '',
					addressDetail: ''
				},
			},
			tmp_productList:[],
			// 0为无动作 1和2代表请求方案和产品成功总个数 3、邀请码认证中 4、邀请码认证成功 5、邀请码认证失败
			loadState: 0,
			inviteCode:'',
			invitePrice: 0,
			isLogin: false,
			selectListWidth: '200px',
			selectList_arr:[],
			plan:2
		}
	},
	components: {
		userBar,
		mySelect
	},
	created(){
		if(sessionStorage.getItem('email') && sessionStorage.getItem('username')){
			this.form.email = sessionStorage.getItem('email');
			this.form.username = sessionStorage.getItem('username');
		}
	},
	computed:{
		countrys(){
			return countrys.map((c)=>{
				return [c.split('-')[0],c.split('-')[0],true]
			})
		},
		curCountry() {
			return this.form.company.addressCountry || this.countrys[182][0];
		},
		curPlan(){
			return this.$store.state.order.curPlan;
		},
		planList(){
			return this.$store.state.order.planList;
		},
		productList() {
			return this.$store.state.order.productList.map(r=>{
				return{
					name: r.name,
					id: r.id,
//					select: this.curPlan.name === 'Enterprise' ? true : r.select
                    select:r.select
				}
			});
		},
		selectList() {
			return this.productList.filter((r)=>{
				return r.select;
			})
		},
		curLevel() {
			return this.$store.state.order.curLevel;
		},
		totalMoney() {
			if(this.curPlan.name === 'Enterprise'){
				return this.curLevel.price;
			}
			return this.curLevel.price || 0;
		}
	},
	filters:{
		currency(value){
			if(typeof value !== "string" || value.length < 4){
				return value
			}else if(value.length > 4 && value.length <= 7){
				return value.slice(0, -3) + ',' + value.slice(-3)
			}else if(value.length > 7){
				return value.slice(0, -6) + ',' + value.slice(-6, -3) + ',' + value.slice(-3);
			}
		},
		unit(value){
			if(typeof value !== "string")return value;
			return value.charAt(0) > 1? value + 's' : value;
		}
	},
	mounted(){
		GaHelper.sendPageView();
		var self = this;
		$(window).scrollTop(0);
		this.getPlans();
		this.getProducts();
		U.init((bol)=>{
			if(bol && sessionStorage.getItem('uid') && $.cookie()['token']){
				self.isLogin = true;
			};
		})
		this.selectListWidth = $('.select-wrap').width()+'px';
	},
	methods:{
		direct(target) {
			this.$router.push({
				name: target
			})
		},
		hideCompanys() {
			this.showCompanys = false;
		},
		showAddMask(){
			this.tmp_productList = _.cloneDeep(this.productList);
			this.showCompanys = true;
            if(localStorage.getItem("plan_name") == "Standard"){
            	this.plan=1;
            }else if (localStorage.getItem("plan_name") == "Enterprise") {
            	this.plan=2;
            }else if(localStorage.getItem("plan_name") == "Basic"){
            	this.plan=0;
            }
			$.get({
				url:"/api/v1/products?type=" + this.plan,
				contentType: 'application/json;charset=utf-8',
				success:(resp)=>{
					console.log(resp);
				},
				error:(error)=>{

				}

			})
		},
		confirmAdd(){
			//再次调用plans接口
			if(this.curPlan.name == 'Standard'){
				this.$store.state.order.initPlanIndex=1;
			}else if(this.curPlan.name == 'Basic'){
				this.$store.state.order.initPlanIndex=2;
			}else if(this.curPlan.name == 'Enterprise'){
				this.$store.state.order.initPlanIndex=0;
			}
			this.selectList_arr = [];
			this.$store.commit('zk_setProductList',this.tmp_productList);
			this.showCompanys = false;
			console.log(this.selectList);
			this.selectList.map((product)=>{
				this.selectList_arr.push(product.id);
			});
			$.get({
				url: U.getApiPre() + '/api/v1/plans2',
				data:{
					pid:this.selectList_arr
				},
				traditional: true,//必须指定为true
				success:(resp)=>{
					if(resp.success){
						console.log(resp);
						this.$store.commit('zk_setPlanList',resp.data.list);
						this.$store.commit('zk_changePlan',resp.data.list[this.$store.state.order.initPlanIndex]);
						this.$store.commit('zk_setCurLevel',resp.data.list[this.$store.state.order.initPlanIndex].levels[0]);
					}else{
						new PNotify({
							text: resp.message || '获取列表失败',
							type: 'error'
						})
					}
				},
				complete:()=>{
					this.loadState++;
					if(this.loadState === 2){
						layer.closeAll();
					}
				}
			})
		},
		allSelect(){
			if($(".dialog-head>span>i").hasClass("abcdata-icon")){
				this.tmp_productList.map(function(item){
				item.select = false;
			    })
				$(".dialog-head>span>i").removeClass("abcdata-icon");
			}else{
				this.tmp_productList.map(function(item){
				item.select = true;
			     })
			$(".dialog-head>span>i").addClass("abcdata-icon");
			}
		},
		changePlan(plan, index){
			this.$store.commit('zk_changePlan',plan);
			this.$store.commit('zk_setCurLevel', plan.levels[0]);
			localStorage.setItem("plan_name", plan.name);
		},
		addProduct(company,index){
			company.select = !company.select;
            let show = true;
            for(let i=0;i<this.tmp_productList.length;i++){
            	if(this.tmp_productList[i].select == false || typeof this.tmp_productList[i].select == 'undefined'){
            		show = false;
            	}
            }
            if(show == true){
            	$(".dialog-head>span>i").addClass("abcdata-icon");
            }else{
            	$(".dialog-head>span>i").removeClass("abcdata-icon");
            }
		},
		deleteProduct(company,index){
			this.selectList_arr = [];
			company.select = false;
			this.$store.commit('zk_setProductList',this.productList);
//			console.log(this.selectList);
			this.selectList.map((product)=>{
				this.selectList_arr.push(product.id);
			});
		    $.get({
				url: U.getApiPre() + '/api/v1/plans2',
				data:{
					pid:this.selectList_arr
				},
				traditional: true,//必须指定为true
				success:(resp)=>{
					if(resp.success){
						console.log(resp);
						this.$store.commit('zk_setPlanList',resp.data.list);
						this.$store.commit('zk_changePlan',resp.data.list[this.$store.state.order.initPlanIndex]);
						this.$store.commit('zk_setCurLevel',resp.data.list[this.$store.state.order.initPlanIndex].levels[0]);
					}else{
						new PNotify({
							text: resp.message || '获取列表失败',
							type: 'error'
						})
					}
				},
				complete:()=>{
					this.loadState++;
					if(this.loadState === 2){
						layer.closeAll();
					}
				}
			})
		},
		changeDuring( level ){
//			console.log(level);
			this.$store.commit('zk_setCurLevel', level);
		},
		getPlans(){
			layer.load(2);
			$.get({
				url: U.getApiPre() + '/api/v1/plans2',
				data:{
					pid:[]
				},
				traditional: true,//必须指定为true
				success:(resp)=>{
					
					if(resp.success){
						this.$store.commit('zk_setPlanList',resp.data.list);
						this.$store.commit('zk_changePlan',resp.data.list[this.$store.state.order.initPlanIndex]);
						this.$store.commit('zk_setCurLevel',resp.data.list[this.$store.state.order.initPlanIndex].levels[0]);
					}else{
						new PNotify({
							text: resp.message || '获取方案失败',
							type: 'error'
						})
					}
				},
				complete:()=>{
					this.loadState++;
					if(this.loadState === 2){
						layer.closeAll();
					}
				}
			})
		},
		getProducts(){
			$.get({
				url: U.getApiPre() + '/api/v1/products',
				contentType: 'application/json;charset=utf-8',
				success:(resp)=>{
					if(resp.success){
						this.$store.commit('zk_setProductList',resp.data.list);
					}else{
						new PNotify({
							text: resp.message || '获取方案失败',
							type: 'error'
						})
					}
				},
				complete:()=>{
					this.loadState++;
					if(this.loadState === 2){
						layer.closeAll();
					}
				}
			})
		},
		verifyCode(){
			if(!this.inviteCode.trim()){
				new PNotify({
					text: 'empty code',
					type: 'error'
				});
				this.toggleBorder($('#code'),'code');
				return false;
			}
			if(!this.verify()){
				return false;
			};
			this.loadState = 3;
			layer.load(2);
			$.post({
				url:"/api/v1/invite-code/verify",
				data:{
					email: this.form.email,
					price: this.totalMoney,
					code: this.inviteCode
				},
				timeout: 10000,
				success:(resp)=>{
					if(resp.success){
						this.loadState = 4;
						new PNotify({
							text: "认证通过，已使用优惠价格",
							type: 'success'
						})
						this.invitePrice = resp.data.price;
						setTimeout(()=>{this.loadState = 0},2000);
					}else{
						new PNotify({
							text: resp.message || '错误邀请码',
							type: 'error'
						})
						this.loadState = 5;
						setTimeout(()=>{this.loadState = 0},2000);
					}
				},
				complete(){
					layer.closeAll();
				}
			})
		},
		verify() {
			if(!this.curPlan.id){
				new PNotify({
					text:'no plan has been choosed',
					type:'error',
				})
				return false;
			}
			if(!this.selectList.length){
				new PNotify({
					text:'no products has been choosed',
					type:'error',
				})
				this.scrollTo('products');
				return false;
			}
			if(!this.form.email.trim()){
				new PNotify({
					text:'email can\'t be empty',
					type:'error',
				})
				this.scrollTo('email');
				return false;
			};
			if(!/^([0-9A-Za-z\-_\.]+)@([0-9a-z]+\.[a-z]{2,3}(\.[a-z]{2})?)$/g.test(this.form.email)){
				new PNotify({
					text:'illegal email',
					type:'error',
				})
				this.scrollTo('email');
				return false;
			}
			if(!this.form.username.trim()){
				new PNotify({
					text:'name can\'t be empty',
					type:'error',
				})
				this.scrollTo('name');
				return false;
			};
			if(!this.form.company.name.trim()){
				new PNotify({
					text:'company can\'t be empty',
					type:'error',
				})
				this.scrollTo('company');
				return false;
			};
			if(!this.totalMoney){
				new PNotify({
					text:'unknow money',
					type:'error',
				})
				return false;
			}
			return true;
		},
		scrollTo(target){
			let targetEl = $('#'+target);
			$('body,html').animate({scrollTop: targetEl.offset().top },'fast');
			this.toggleBorder(targetEl,target);
		},
		toggleBorder(targetEl,target){
			let elInput = targetEl.find('.'+target+'-input');
			elInput.addClass('red-border');
			setTimeout(()=>{
				elInput.removeClass('red-border');
			},2000);
		},
		selectOption(array){
			this.form.company.addressCountry = array[0];
		},
		confirmOrder() {
//			if(!this.verify()){
//				return false;
//			};
             console.log(this.selectList);
            if(!this.selectList.length){
				new PNotify({
					text:'no products has been choosed',
					type:'error',
				})
				this.scrollTo('products');
				return false;
			}
			let index = layer.load(2);
			$.post({
				url: U.getApiPre() + '/api/v1/orders/create2',
				contentType: 'application/json;chartset=utf-8',
				data: JSON.stringify({
					plan_id: this.curPlan.id,
					level_id: this.curLevel.id,
					term_long: this.curLevel.long,
					term_unit: this.curLevel.long_unit,
					products: this.selectList.map((product)=>{
						return {
							id: product.id
						}
					}),
					account:{
						type: 0,
						email: this.form.email,
						username: this.form.username,
						company: {
							address:this.curCountry + this.form.company.addressDetail,
							name: this.form.company.name
						},
						phoneNumber: this.form.phoneNumber
					},
					payment:{
						type: 0
					},
					price_total: this.invitePrice || this.totalMoney
				}),
				timeout: 10000,
				success:(resp)=>{
					console.log(resp);
					localStorage.setItem("order_id", resp.data.id);	
					if(resp.success){
						this.resetState();
						this.$router.push({
                            name: 'orderInformation',
//                          name: 'order-finish',
//                          name:'orderfailed',
							params:{
								id: resp.data.id
							}
						})
					}else{
						new PNotify({
							text: resp.message || '订单提交失败',
							type: 'error'
						})
					}
				},
				complete(){
					layer.close(index);
				}
			})
		},
		resetState(){
			this.invitePrice = 0;
			this.form = {
				email: '',
				username: '',
				phoneNumber: '',
				company: {
					name: '',
					address: ''
				}
			};
			this.loadState =  0;
		}
	},
}
</script>
<style type="text/css" scoped>
	/*头部*/
	.order-head{
		height: 270px;
		color: #fff;
		background: #091f27;
		border-top: 1px solid transparent;
	}
	.order-head .item-960 .order-title{
		font-size: 40px;
		font-weight: normal;
	}
	.order-head .item-960 .order-tips{
		margin-top: 10px;
	}
	.order-nav-bar{
		height: 70px;
		margin: 24px 60px 35px;
	}
	.logo{
		width: 70px;
		height: 70px;
		background-position: 0 -546px;
		float: left;
	}
	.login{
		float: right;
		width: 110px;
		height: 35px;
		line-height: 33px;
		text-align: center;
		cursor: pointer;
		border: 1px solid #fff;
		color: #fff;
		border-radius: 5px;
		transition: background 0.2s linear;
		font-weight: bold;
	}
	.icon-login{
		width: 10px;
		height: 12px;
		display: inline-block;
		vertical-align: 0px;
		margin-right: 10px;
		background-position: 0px -445px;
	}
	.login:hover{
		color: #0a1e27;
		background: #fff;
	}
	.login:hover .icon-login{
		background-position: -11px -444px;
	}
	/*表单区*/
	.form-wrapper .item-960{
		padding: 0 25px;
	}
	.steps{
		margin-bottom: 40px;
	}
/*	.panel-left{
		float: left;
	}*/
	.step-name-container{
		height: 45px;
		line-height: 43px;
		font-size: 26px;
		color: #0a1e27;
	}
	.subname-container{
		height: 55px;
		line-height: 53px;
		font-size: 16px;
		color: #78909c;
	}
/*	.step-name-container .steps-num{
		display: inline-block;
		width: 26px;
		height: 26px;
		vertical-align: -7px;
		margin-right: 15px;
	}
	.solution-plan .steps-num{
		background-position: 0 -645px;
	}
	.account-settings .steps-num{
		background-position: -26px -645px;
	}
	.terms-of-service .steps-num{
		background-position: -52px -645px;
	}
	.terms-of-payment .steps-num{
		background-position: -78px -645px;
	}
	.panel-right{
		width: 650px;
		margin-left: 300px;
	}*/
	/*solution plan*/
	.steps .payment{
		margin-top: 40px;
	}
	.steps .payment-style{
		margin-top: 40px;
		width: 500px;
		height:28px;
		line-height: 28px;
	}
	.steps .payment-style label{
		font-weight:normal;
		padding: 0 15px;
		cursor: pointer;
	}
	.steps .payment-style input{
		visibility: visible;
		cursor: pointer;
	}
	.solution-plan{
		margin-top: 40px;
	}
	.serve-category{
		float: left;
		width: 33%;
		height: 130px;
		line-height: 45px;
		font-size: 14px;
		text-align: center;
		background: #f6fbfc;
		margin-right: 0.5%;
		margin-bottom: 1%;
		cursor: pointer;
		border-radius: 5px;
		position: relative;
	}
	.serve-category .p{
		color:#35af4a;
		font-weight: bold;
		margin-top:20px;
	}
	.solution-plan .icon-yellow-checked-20{
		display: none;
		position: absolute;
		right: 8px;
		top: 8px;
		width: 19px;
		height: 19px;
		background-position: -20px -526px;
	}
	.price-list-card.active .icon-yellow-checked-20,
	.serve-category.active .icon-yellow-checked-20{
		display: block;
	}
	.serve-category:hover,
	.serve-category.active{
		background: #e7f2f6;
	}
	.price-list{
		padding-top: 10px;
	}
	.price-list-card{
		float: left;
		width: 33%;
		height: 130px;
		margin-right: 0.5%;
		margin-bottom: 1%;
		background: #f6fbfc;
		border-radius: 5px;
		cursor: pointer;
		text-align: center;
		position: relative;
	}
	.price-list-card:hover,
	.price-list-card.active{
		background: #e7f2f6;
	}
	.amount{
		font-size: 20px;
		color: #35af4a;
		margin: 24px 0 10px;
	}
	.price-list-card.first .amount{
		margin: 24px 0 33px;
	}
	.previously-amount{
		text-decoration: line-through;
		margin-bottom: 8px;
		color: #999;
	}
	.price-list-card .duration{
		font-size: 16px;
		font-weight: 600;
	}
	.production-title{
	}
	.production-title:before{
		content: '*';
		color: red;
		font-size: 18px;
		margin-right: 3px;
	}
	.company-list{
		margin-top:14px;
		margin-bottom: 45px;
		border: 1px solid #dbe6ea;
		height: 170px;
		padding-bottom: 13px;
		overflow-y: scroll;
	}
	.company-capsule{
		min-width: 119px;
		height: 32px;
		line-height: 31px;
		float: left;
		background: #e7f2f6;
		border-radius: 3px;
		margin-left: 10px;
		margin-top: 13px;
		text-align: center;
	}
	.company-name{
		display: inline-block;
		overflow: hidden;
		white-space: nowrap;
		text-overflow: ellipsis;
		width: 70px;
		vertical-align:top;
	}
	.icon-delect{
		display: inline-block;
		width: 13px;
		height: 13px;
		vertical-align:-2px;
		margin-left: 15px;
		background-position: -13px -632px;
		cursor: pointer;
	}
	.icon-delect:hover{
		background-position: -26px -632px;
	}
	.icon-add{
		display: inline-block;
		width: 20px;
		height: 20px;
		margin-left: -40px;
		margin-top: 5px;
		vertical-align: -5px;
		margin-right: 20px;
		background-position: -57px -526px;
		cursor: pointer;
	}
	.btn-add-company:hover .icon-add{
		background-position: -76px -526px;
	}
	/*account settings*/
	.account-form{
		color: #78909c;
	}
	.account-form-label{
		display: block;
		font-weight: normal;
	}
/*	.icon-warn{
		display: inline-block;
		width: 13px;
		height: 13px;
		vertical-align: -2px;
		background-position: 0 -632px;
	}
	.email-tips{
		font-size: 12px;
	}*/
	.account-form-label.required:before{
		content: '*';
		color: red;
		font-size: 18px;
		margin-right: 3px;
	}

	.account-settings-input{
		height: 45px;
		width: 100%;
		border: 1px solid #dbe6ea;
		color: #000;
		padding: 0 15px;
		border-radius: 3px;
	}
	.company-address-input{
		width: 70%;
		float: left;
		border-left: none;
		border-top-left-radius: 0;
		border-bottom-left-radius: 0;
	}
	/*terms-of-service*/
/*	.terms-of-service{
		font-size: 12px;
		color: #78909c;
	}
	.service-container{
		line-height: 44px;
		margin-bottom: 32px;
		height: 45px;
		color: #000;
	}
	.service-name{
		float: left;
		width: 325px;
	}
	.service-name .abcdata-icon{
		display: inline-block;
		width: 16px;
		height: 16px;
		vertical-align: -2px;
		margin-right: 15px;
		background-position: 0 -615px;
	}
	.terms-of-service .icon-warn{
		margin-right: 15px;
	}*/
	/*payment*/
/*	.bank-remittance{
		height: 45px;
		line-height: 44px;
		margin-bottom: 32px;
	}
	.bank-remittance .icon-warn{
		margin-left: 40px;
		vertical-align: -2px;
		margin-right: 8px;
	}
	.bank-warn-text{
		font-size: 12px;
		color: #78909c;
	}*/
	/*表单底部确认*/
	.form-footer{
		margin-top: 60px;
	}
/*	.form-footer-left{
		width: 50%;
		height: 100px;
		position: relative;
		float: left;
	}
	.form-footer-left .verify{
		margin-top: 13px;
		line-height: 45px;
		color: #547989;
		margin-bottom: 15px;
	}
	.form-footer-left .code-input{
		width: 190px;
		margin-left: 15px;
		margin-right: 15px;
		line-height: 45px;
		border-bottom: 1px solid #DBE6EA;
	}
	.form-footer-left .btn-verify{
		position: relative;
		text-decoration: underline;
		color: #547989;
		margin-right: 15px;
	}
	.form-footer-left .btn-verify.passed:before{
		content: '';
		position: absolute;
		width: 20px;
		height: 20px;
		background: red;
		left: -40px;
		top: 15px;
		background: url(/images/img-icon.png) no-repeat;
		background-position: 0 -526px;
	}
	.form-footer-left .check-tips{
		font-size: 12px;
		color: #f62828;
	}
	.form-footer-label{
		font-weight: normal;
	}*/
	.form-footer-right{
		/*width: 50%;*/
		width: 100%;
		padding: 10px 50px;
		text-align: center;
	}
/*	.form-footer-right .amount{
		font-size: 30px;
	}
	.form-footer-right .previously-amount{
		margin-left: 15px;
		font-size: 16px;
		color: #999;
	}*/
	.form-footer-right .btn-confirm{
		height: 38px;
		width: 300px;
		color: #fff;
		background:#355867;
		border-radius: 5px;
		font-size: 18px;
		/*vertical-align: 6px;*/
	}
	.order-footer{
		line-height: 100px;
		color: #797979;
		border-top: 1px solid #dfe6ea;
		font-size: 12px;
	}
	.order-footer .copyright{
		float: left;
	}
	.order-footer .contact{
		float: right;
		padding-right: 10px;
	}
	.order-footer .contact .contact-email{
		color: #797979;
	}
	/*mask*/
	.mask{
		position: fixed;
		top: 0;
		z-index: 3;
		width: 100%;
		height: 100%;
		background: rgba(0,0,0,.3);
	}
	.mask .mask-inner{
		position: absolute;
		left: 50%;
		top: 50%;
		transform: translateX(-50%) translateY(-50%);
		width: 500px;
		height: 260px;
		border-radius: 5px;
		background: #fff;
		padding: 16px;
		box-shadow: 3px 0 6px rgba(10,30,39,.25);
	}
	.mask .mask-inner .dialog-head{
		font-size: 16px;
	}
	.mask .mask-inner .icon-close{
		float: right;
		width: 16px;
		height: 16px;
		cursor: pointer;
		background-position: -18px -616px;
	}
	.mask .mask-inner .icon-close:hover{
		background-position: -34px -616px;
	}
	.mask .mask-inner .company-list{
		border: none;
		min-height: 140px;
	}
	.mask .mask-inner .company-capsule{
		height: 30px;
		line-height: 30px;
		margin: 1% 1.5% 0;
		width: 30%;
		cursor: pointer;
	}
	.mask .mask-inner .company-name{
		width: 53px;
	}
	.mask .mask-inner .icon-yellow-checked{
		visibility: hidden;
		display: inline-block !important;
		width: 13px;
		height: 13px;
		background-position: -39px -632px;
		vertical-align: -2px;
	}
	.mask .mask-inner .icon-yellow-checked.show{
		visibility: visible;
	}
	.mask .mask-inner .dialog-foot{
		text-align: right;
	}
	.mask .mask-inner .btn-confirm{
		height: 36px;
		width: 95px;
		background: #537b88;
		color: #fff;
		font-size: 16px;
		border-radius: 3px;
	}
	.red-border{
		border-bottom: 1px solid red !important;
	}
	.dialog-head span i{
		display: inline-block !important;
		width: 13px;
		height: 13px;
		background-position: -40px -634px;
		vertical-align: -2px;
		border: 1px solid gray;
		border-radius: 50%;
	}
</style>
