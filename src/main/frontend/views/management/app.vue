<template lang="html">
    <div class="management-wrapper container-fluid">
        <div class="row">
            <div class="col-md-3">
                <mangement-left></mangement-left>
            </div>
            <div class="col-md-9">
                <mangement-right></mangement-right>
            </div>
        </div>
    </div>
</template>

<script>
import mangementLeft from './views/manger-left.vue'
import mangementRight from './views/manger-right.vue'
export default {
    name:'app',
    components: {
        mangementLeft,
        mangementRight
    },
    mounted(){
        this.$store.dispatch('zk_getComponentData',{
            curList: this.$store.state.backend.curList,
            activeTabIndex: 0
        });
    },
    beforeRouteLeave(){
        $('.abcdata-wrapper').css({
            'min-width': 1180
        })
    }
}
</script>
<style lang="css">
    .abcdata-wrapper{
        min-width: auto;
    }
    .management-wrapper {
        height: 100%;
        background: #f0f0f0;
        padding: 0;
    }
    .management-wrapper .row{
        height: 100%;
        margin: 0;
    }
    .management-wrapper .row>div{
        height: 100%;
        padding: 0;
    }
</style>
