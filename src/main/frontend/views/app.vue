<template>
	<div class="abcdata-wrapper" :class="{mobile:isMobile}">
		<router-view></router-view>
	</div>
</template>
<script type="text/javascript">
	export default {
		name: 'abcdata-wrapper',
		computed:{
			isMobile(){
				return this.$store.state.common.isMobile;
			}
		},
		mounted() {
			if(device.mobile() || device.ios()){
				this.$store.commit("zk_setUserAgent",true);
				$('body,html').height($(window).height());
			}
			this.$nextTick(()=>{
				let _PNotify = PNotify;
				let options = PNotify.prototype.options;
				let stack_center = {"dir1": "down", "dir2": "right", "firstpos1": this.isMobile?0:80, "firstpos2": this.isMobile?0:(( $(window).width() - parseInt(options.width )) / 2)};
				{
					let PNotify = (params) =>{
						if(!params){
							return new _PNotify();
						}else{
							return new _PNotify($.extend(params,{
								title: false,
								buttons:{
									closer: false,
									sticker: false
								},
								delay: 1500,
								stack: stack_center,
							}));
						}
					}
					window.PNotify = PNotify;
				}
				$(window).resize( () =>{
					stack_center.firstpos2 = ( $(window).width() - parseInt(options.width) )/ 2;
				})
			})
		}
	}
</script>