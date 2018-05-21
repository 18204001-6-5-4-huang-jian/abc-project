<template>
	<!-- full-screen -->
	<div class="fullscreen-wrapper">
		<chart-header></chart-header>
		<chart-content></chart-content>
	</div>
</template>

<script type="text/javascript">

	import ChartHeader from './chart-header-panel.vue'
	import ChartList from './chart-list-panel.vue'
	import ChartContent from './chart-content-panel.vue'

	export default {
		name: 'full-screen',
		components:{
			ChartHeader,
			ChartContent
		},
	}
</script>

<style type="text/css">
	.fullscreen-wrapper{
		position: fixed;
		top: 0px;
		left: 0px;
		right: 0px;
		bottom: 0px;
		z-index: 100;
		color: #7985a3;
	}
</style>
