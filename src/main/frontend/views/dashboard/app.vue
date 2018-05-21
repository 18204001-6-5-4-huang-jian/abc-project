<template lang="html">
    <div class="dashboard-wrapper">
        <dashboard-header></dashboard-header>
        <dashboard-detail v-bind:fullScreen="false"></dashboard-detail>
    </div>
</template>

<script>
import DashboardDetail from './views/dashboard-detail.vue'
import DashboardHeader from './views/dashboard-header.vue'
export default {
    name:'app',
    components: {
        DashboardDetail,
        DashboardHeader
    }
}
</script>
<style lang="css">
    .dashboard-wrapper {
        position: absolute;
        left: 0px;
        top: 0px;
        bottom: 0px;
        width: 100%;
    }
</style>
