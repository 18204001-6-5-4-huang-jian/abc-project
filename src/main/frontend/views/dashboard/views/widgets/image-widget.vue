<template lang="html">
    <img class="image-widget-wrapper" :src="image_url" v-on:error="onError" :style="style" :id="id">
</template>

<script>
export default {
    name:'image-widget',
    props: {
    	id: {
    		type: String,
    		default: ''
    	}
    },
    data(){
    	return {
    		image_url: '/images/img-bad-data.png',
            index: -1,
            chartWrapper: null,
            style: {}
    	}
    },
    methods: {
    	render(element, index, chartWrapper) {
            this.style = $.extend(true, {}, this.style, {
                width: element.width()+'px',
                height: element.height()+'px'
            });
            if (this.image_url != chartWrapper.image_url) {
                this.image_url = chartWrapper.image_url;
                this.index = index;
                this.chartWrapper = chartWrapper;
                this.$emit('onSuccess', {
                    index: this.index,
                    chartWrapper: this.chartWrapper
                });
            }
    	},
    	onError(){
            console.warn('image-widget onerror');
            this.style = {
                width: '0px',
                height: '0px',
                display: 'none'
            };
    		this.$emit('onError', {
        		index: this.index,
        		error: this.chartWrapper
        	});
    	}
    }
}
</script>

<style lang="css">
    .image-widget-wrapper {
        max-width: 100%;
        max-height: 100%;
        vertical-align: middle;
    }
</style>
