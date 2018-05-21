<template>
	<div class="time-line-wrap">
		<div class="time-line-box">
			<ul class="time-point-list" id="time-line-list">
				<li class="time-point" @click="setCurDay(day.time, index)" v-for="(day, index) in datelist" :class="{active:index === 6,old: index < 6}" v-text="day.d"></li>
			</ul>
		</div>
		<div class="tool-tip" v-show="toolTip" @mouseenter = "showToolTip" @mouseleave = "hideToolTip">
			<span v-text="formatedCurDay"></span>
			<span id="datetimepicker" v-text="lang ? '选择时间':'select time'"></span>
		</div>
		<div class="tl-btn-left" @click="minusDay"></div>
		<div class="tl-btn-right" @click="plusDay"></div>
	</div>
</template>
<script type="text/javascript">
	export default{
		name: 'time-line',
		data() {
			return {
				datelist: [],
				timeLineBox: null,
				datePicker: null,
				toolTip: false,
				closeTimer: null
			}
		},
		computed: {
			formatedCurDay () {
				return moment(this.curDay).format('YY.MM.DD');
			},
			lang () {
				return this.$store.state.dashboard.lang;
			},
			curDay () {
				return this.$store.state.dashboard.curDay;
			}
		},
		methods: {
			formatDay (time, day){
				// @parames
				// time: 时间对象
				// day: 偏移天数

				if(!time instanceof Date)return false;
				let seconds = 1000 * 60 * 60 * 24,
					_date = time.getTime() + day * seconds;
				return {
							d:moment(_date).format("D"),
							time: new Date(_date)
						}
			},
			minusDay () {
				this.timeLineBox.addClass('add-trans').addClass('translate-minus-1');
				setTimeout(()=>{
					this.$store.commit('zk_setCurDay', this.formatDay(this.curDay, -1).time);
					this.timeLineBox.removeClass('add-trans').removeClass('translate-minus-1');
				}, 500);
			},
			plusDay () {
				if( new Date().getTime() - new Date(this.curDay).getTime() <= 24 * 60 * 60 * 1000 ){
					new PNotify({
						text: this.lang ? '超出最大日期范围' : 'Exceed the maximum date',
						type: 'error'
					})
					return false;
				}
				this.timeLineBox.addClass('add-trans').addClass('translate-plus-1');
				setTimeout(()=>{
					this.$store.commit('zk_setCurDay', this.formatDay(this.curDay, 1).time);
					this.timeLineBox.removeClass('add-trans').removeClass('translate-plus-1');
				}, 500);
			},
			setCurDay (day, index) {

				if (new Date(day).getTime() - new Date().getTime() > 0) {
					new PNotify({
						text: this.lang ? '超出最大日期范围' : 'Exceed the maximum date',
						type: 'error'
					})
					return false;
				}

				let diff = index - 6,
					className = 'translate-';
					
				if (diff === 0) return false;
				if (diff < 0) {
					className = className + "minus-" + Math.abs(diff);
				} else {
					className = className + "plus-" + Math.abs(diff);
				}
				this.timeLineBox.addClass('add-trans').addClass(className);
				setTimeout(()=>{
					this.$store.commit('zk_setCurDay', this.formatDay(this.curDay, diff).time);
					this.timeLineBox.removeClass('add-trans').removeClass(className);
				}, 500);
			},
			showToolTip () {
				clearTimeout(this.closeTimer);
				if(this.toolTip === true) return false;
				this.toolTip = true;
			},
			hideToolTip () {
				clearTimeout(this.closeTimer);

				if(!this.toolTip) return false;

				this.closeTimer = setTimeout(() => {
					this.toolTip = false;
				}, 800)
			},
			initToolTip () {

				this.timeLineBox.delegate('.time-point.active', 'mouseenter', () => {
					this.showToolTip();
				}).on('mouseleave', () => {
					this.hideToolTip();
				})

				$('.tool-tip').on('mouseenter', ()=>{
					this.showToolTip();
				}).on('mouseleave', ()=>{
					this.hideToolTip();
				})

				$(this.bindMouseEvent);
			},
			bindMouseEvent () {
				let pickDom = $('.datetimepicker')
				pickDom.on('mouseenter', ()=>{
					this.showToolTip();
				}).on('mouseleave', ()=>{
					this.hideToolTip();
					pickDom.hide();
				})
			},
			initDatePicker (lang,changelanguage) {
				// debugger
				if(!this.datePicker){
					this.datePicker = $("#datetimepicker");
					this.datePicker.datetimepicker({
						autoclose: true,
						todayBtn: true,
						pickerPosition: "bottom-right",
						minView: 2,
						todayHighlight: true,
						initialDate: this.curDay,
						endDate: new Date(),
						language: this.lang
					}).on('changeDate', (ev)=>{
						this.toolTip = false;
						this.$store.commit('zk_setCurDay', ev.date);
					});
				}else{
					if (changelanguage) {
						this.datePicker.datetimepicker('remove').datetimepicker({
							autoclose: true,
							todayBtn: true,
							pickerPosition: "bottom-right",
							minView: 2,
							todayHighlight: true,
							endDate: new Date(),
							language: lang
						})
					}else{
						this.datePicker.data('datetimepicker').update(this.curDay)
					}
				}
				this.bindMouseEvent();
			}
		},
		created () {
			this.$store.commit('zk_setCurDay', this.curDay ? new Date(new Date(this.curDay).getTime()+1) : new Date());
		},
		mounted () {
			this.$nextTick(()=>{
				this.timeLineBox = $('.time-point-list');
				this.initToolTip();
				this.initDatePicker();
			})
		},
		watch: {
			curDay (val) {
				// debugger
				this.datelist = [
					this.formatDay(val, -6),
					this.formatDay(val, -5),
					this.formatDay(val, -4),
					this.formatDay(val, -3),
					this.formatDay(val, -2),
					this.formatDay(val, -1),
					this.formatDay(val, 0),
					this.formatDay(val, 1),
					this.formatDay(val, 2),
					this.formatDay(val, 3),
					this.formatDay(val, 4),
					this.formatDay(val, 5),
					this.formatDay(val, 6)
				];

				if (!this.datePicker) return false;

				this.initDatePicker();
			},
			lang (val) {
				this.initDatePicker(val, true);
			}
		}
	}
</script>
<style lang="less" scoped>
	@import '../../../less/front.less';

	.time-line-wrap{
		position: absolute;
		left: 50%;
		width: 350px;
		height: 50px;
		top: 0;
		transform: translateX(-50%);
		border-bottom: 1px solid @textColor;
		z-index: 1;
		.time-line-box{
			overflow: hidden;
			.time-point-list{
				width: 650px;
				position: relative;
				overflow: hidden;
				transform: translateX(-150px);
				cursor: pointer;
				&.add-trans{
					transition: all .5s ease-out;
				}
				&.translate-minus-1{
					transform: translateX(-100px);
				}
				&.translate-minus-2{
					transform: translateX(-50px);
				}
				&.translate-minus-3{
					transform: translateX(0);
				}
				&.translate-plus-1{
					transform: translateX(-200px);
				}
				&.translate-plus-2{
					transform: translateX(-250px);
				}
				&.translate-plus-3{
					transform: translateX(-300px);
				}
				&:after{
					content: '';
					display: block;
					height: 0;
					clear: both;
					zoom: 1;
				}
				.time-point{
					position: relative;
					width: 50px;
					float: left;
					height: 35px;
					margin-top: 15px;
					text-align: center;
					color: #313a4a;
					&:after{
						position: absolute;
						content: '';
						height: 10px;
						width: 2px;
						bottom: 0;
						left: 25px;
						background: @borderColor;
					}
					&:before{
						position: absolute;
						content: '';
						height: 5px;
						border-left: 1px solid @borderColor;
						border-right: 1px solid @borderColor;
						bottom: 0;
						left: 0;
						width: 50px;
					}
					&.active{
						color: @baseColor;
						font-weight: bold;
						font-size: 25px;
						margin-top: 7px;
						height: 42px;
						&:after{
							background: @baseColor;
						}
					}
					&.old{
						color: #626f88;
						font-weight: bold;
						&:after{
							background: #626f88;
						}
					}
				}
			}
		}
		.tool-tip{
			// display: none;
			position: absolute;
			left: 50%;
			transform: translateX(-50%);
			color: @textColor;
			border: 1px solid @borderColor;
			top: 60px;
			padding: 5px 10px;
			border-radius: 4px;
			background: #20242e;
			&:before{
				content: '';
				position: absolute;
				top: -10px;
				border-left: 10px solid transparent;
				border-right: 10px solid transparent;
				border-bottom: 10px solid #20242e;
				left: 50%;
				transform: translateX(-50%);
			}
			&:after{
				content: '';
				position: absolute;
				top: -11px;
				border-left: 11px solid transparent;
				border-right: 11px solid transparent;
				border-bottom: 11px solid @borderColor;
				z-index: -1;
				left: 50%;
				transform: translateX(-50%);
			}
			#datetimepicker{
				margin-left: 20px;
				display: inline-block;
				cursor: pointer;
				color: @baseColor;
				text-decoration: underline;
			}
		}
		.tl-btn-left{
			position: absolute;
			width: 25px;
			height: 25px;
			left: -40px;
			top: 15px;
			cursor: pointer;
			background: url(/images/icon-arrow-up.png) no-repeat center;
			background-color: #20242e;
			transform: rotate(-90deg);
			border-radius: 50%;
		}
		.tl-btn-right{
			.tl-btn-left();
			left: auto;
			right: -40px;
			transform: rotate(90deg);
		}
	}
</style>