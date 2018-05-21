<template>
	<div class="notify-wrapper" :class="type" v-show="show">
        <div class="notify-icon" :class="type"></div>
        <div class="notify-text">
            <span v-text="text"></span>
        </div>
        <div class="notify-handle" v-show="isShowHandle">
            <span @click="handle()">"{{handleText}}"</span>
        </div>
    </div>
</template>

<script type="text/javascript">
export default {
    name:'notify',
    props: {
        type: {
            type: String,
            default: 'info'
        },
        text: {
            type: String,
            default: ''
        },
        show: {
            type: Boolean,
            default: false
        },
        handleType: {
        	type: String,
        	default: ''
        },
        handleText: {
            type: String,
            default: ''
        },
        delay: {
        	type: Number,
        	default: 3000
        }
    },
    computed:{
        isShowHandle(){
            return this.handleText?true:false;
        }
    },
    watch:{
        show: function(isShow){
            var self = this;
            if(isShow){
                setTimeout(function(){
                    self.$emit('onHideNotify');
                },self.delay);
            }
        }
    },
    methods:{
        handle(){
            var self = this;
            self.$emit('onHandleNotify', self.handleType);
        }
    }
}	
</script>

<style type="text/css">
	.notify-wrapper {
	    position: fixed;
	    top: 80px;
	    left: 50%;
	    display: -webkit-flex;
	    display: flex;
	    justify-content: center;
	    align-items: center;
	    padding: 20px 0px;
	    margin: 0px 0px 0px -150px;
	    width: 300px;
	    min-height: 60px;
	    font-size: 12px;
	    color: #fff;
    	box-shadow: 0px 6px 20px rgba(0, 0, 0, 0.1);
	}
	.notify-wrapper.info {
	    background-color: #3f92ee;
	}
	.notify-wrapper.success {
	    background-color: #25c781;
	}
	.notify-wrapper.error {
	    background-color: #fe5757;
	}
	.notify-wrapper div {
	    height: 20px;
	    line-height: 20px;
	}
	.notify-wrapper .notify-icon {
	    flex-basis: 20px;
	    padding: 0px;
	    margin: 0px 10px 0px 0px;
	    width: 20px;
	    background-position: center;
	    background-repeat: no-repeat;
	    background-size: 18px 18px;
	}
	.notify-wrapper .notify-icon.info,
	.notify-wrapper .notify-icon.error {
	    background-image: url('/images/icon-recover-tip.png');
	}
	.notify-wrapper .notify-icon.success {
	    background-image: url('/images/icon-success.png');
	}
	.notify-wrapper .notify-text {
	    padding: 0px;
	    margin: 0px 10px 0px 0px;
	}
	.notify-wrapper .notify-handle {
	    cursor: pointer;
	    text-decoration: underline;
	}
</style>