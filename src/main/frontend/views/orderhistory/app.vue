<template>
    <transition name="component-fade">
        <div class="order-wrap">
            <div class="order-head">
                <div class="order-nav-bar clearfix">
                    <div class="logo abcdata-icon pointer" @click="direct('introduction')"></div>
                    <user-bar></user-bar>
                </div>
            </div>
            <div class="form-wrapper">
            	<h2>Order History</h2>
            	<table cellpadding="1" cellspacing="1">
            		<tr class="first_tr" style="background:#E4E4E4;">
            			<td>Time</td>
            			<td>Plan</td>
            			<td>Company</td>
            			<td>Deadline</td>
            			<td>Price</td>
            			<td>Status</td>
            		</tr>
            		<tr v-for="item in listArray" style="border-bottom:1px solid #BCBCBC;">
            			<td v-text="item.time"></td>
            			<td v-text="item.plan"></td>
            			<td>
            				<div class="huang"
            		         :title="item.company.join(',')" 
            		         v-text="item.company.join(',')">
            				</div>
            			</td>
            			<td v-text="item.deadline"></td>
            			<td v-text="'￥' + item.price"></td>
            			<td v-text="item.status" @click="gostatus(item.status,item.price,item.plan,item.id)" class="last_td"></td>
            		</tr>
            	</table>
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
            	sort:"create_at",
                order:"desc",
                limit:9999,
                offset:0,
                lang:"",
                listArray:[]
            }
        },
        mounted() {
          GaHelper.sendPageView();
        	var self = this;
        	$.get({
            	url:"api/v1/orders/history?lang="+self.lang+"&sort="+self.sort+"&order="+self.order + "&limit="+self.limit + "&offset=" + self.offset,
            	contentType:'application/json;charset=utf-8',
            	cache:false,
            	success:function(res){
            		console.log(res);
            		self.listArray = res.data.list;
            	},
            	error:function(error){
            	  console.log(error)
            	}
            	
            })
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
        },
        computed: {
        	
        },
        methods:{
            direct(target, pro) {
                if (pro) {
                    GaHelper.sendEvent(GaHelper.Usercenter.buyDashboard, pro.name);
                }
                this.$router.push({
                    name: target
                })
            },
            gostatus(target,price,plan,id){
            	if(target == "Succeed"){
            		this.$router.push({
                    name: 'order-finish'
                    })
            		localStorage.setItem("price",price);
            		localStorage.setItem("plan",plan);
            	}else if(target == "Failed"){
            		this.$router.push({
                    name: 'orderfailed'
                    })
            	}else if(target == "Unconfirmed"){
            		this.$router.push({
                    name: 'waitingconfirmationdown'
                    })
            	}else if(target == "Unpaid"){
            		this.$router.push({
                    name: 'orderInformation'
                    })
            		localStorage.setItem("order_id",id);	
            	}
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
        text-indent: 5px;
        margin: 0 auto;
        background: #10A7D8;
        color: #FFFFFF;
    }
    .order-nav-bar {
        height: 70px;
        margin: 0 60px;
    }
    .logo {
        width: 70px;
        height: 70px;
        background-position: 0 -546px;
        float: left;
    }
    .form-wrapper{
    	width:80%;
    	margin:0 auto;
    	margin-top:50px;
    }
     .form-wrapper h2{
     	width: 300px;
     	height: 50px;
     	line-height:50px;
     	font-weight:bold;
     	font-size:32px;
     	color: #000000;
     }
      .form-wrapper table{
      	font-size: 14px;
      	margin:20px 0;
      	width: 80%;
      	table-layout: fixed;  
      }
      .form-wrapper .first_tr{
      	border-bottom:none;
      	-webkit-user-select: none;
      	-ms-user-select: none;
      	-moz-user-select: none;
      }
      .form-wrapper .first_tr td{
      	font-weight: bold;
      	color: #000000;
      	border: 1px solid #FFFFFF;
      }
      .form-wrapper table tr{
      	height: 50px;
      	line-height: 50px;
      	/*border-bottom: 1px solid gray;*/
      }
      .form-wrapper table tr td{
      	width:16.66%;
      	height: 50px;
      	line-height:50px;
      	text-align: center;
      	text-overflow: ellipsis;
      	-moz-text-overflow: ellipsis;
      	white-space: nowrap;
      	overflow: hidden;
      }
      .form-wrapper .last_td{
      	cursor: pointer;
      }
      .huang{
      	text-overflow: ellipsis;
      	-moz-text-overflow: ellipsis;
      	white-space: nowrap;
      	overflow: hidden;
      }
</style>
