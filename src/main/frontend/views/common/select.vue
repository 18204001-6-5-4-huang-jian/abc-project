<template>
    <div class="select-wrap" :class="{'negtive':disabled}" :style="selectStyle" @click.stop="showOptionsFn($event)">
        <div class="select cur-select" :title="curSelect" :style="selectStyle" v-if="!isInput" v-text='curSelect'></div>
        <input class="select cur-select" :title="curSelect" :style="selectStyle" :type="isText?'text':'number'" @change="inputVal($event)" v-if="isInput" :value='curSelect'></input>
        <ul class="options-wrap" :style="listPositon" v-show="showOptions">
            <li class="options" :class="{'disable':!option[2]}" v-for="option in optionsList" :style="optionsStyle" v-text="option[1]" :title="option[1]" @click.stop="choose(option)"></li>
        </ul>
    </div>
</template>
<style type="text/css" scoped>
    .select-wrap{
        display: inline-block;
        width: 100px;
        height: 30px;
        /*background-color: #f9f9f9;*/
        vertical-align: top;
        position: relative;
    }
    .cur-select{
        line-height: 20px;
        padding: 0 5px;
        height: 100%;
        padding-right: 10px;
        width: 100% !important;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        background-color: transparent;
        border: 1px solid #dbe6ea;
        cursor: pointer;
        appearance:none;
        -moz-appearance:none;
        -webkit-appearance:none;
        padding-right: 14px;
        background-repeat: no-repeat;
        background-color: transparent;
        background-position: 97% center;
        background-image: url(/images/select-icon.png) !important;
        background-size: 10px;
        outline: 0;
    }
    .options-wrap{
        overflow: auto;
        z-index: 1;
        list-style: none;
        margin: 0;
        padding: 0;
        background: #fff;
        border: 1px solid #dbe6ea;
    }
    .select-wrap.negtive{
        background-color: #f3f3f3 !important;
    }
    .options{
        padding: 3px 10px;
        line-height: 20px;
        cursor: pointer;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        width: 100px;
    }
    .options.disable{
        color: rgb(185,185,185);
    }
    .options:hover{
        background: #e5e5e5;
    }
</style>
<script type="text/javascript">
    export default{
        name:'select',
        props:{
            optionsList:{
                type: Array,
                required: true,
            },
            curSelect:{
                type: [String,Number],
                required :true,
            },
            isInput:{
                type: Boolean,
                default: false,
            },
            isText:{
                type: Boolean,
                default: false,
            },
            disabled:{
                type: Boolean,
                default: false,
            },
            selectStyle:{
                type: [Object , String],
            },
            optionsStyle:{
                type: [Object , String]
            },
            maxHeight:{
                type: String,
                default: '250px'
            },
            fixed:{
                type: Boolean,
                dafault: true
            }
        },
        data(){
            return {
                showOptions: false,
                listPositon:{
                    left: 0,
                    top:0,
                    maxHeight: this.maxHeight,
                    position: this.fixed ? "fixed" : 'absolute'

                },
            }
        },
        methods:{
            choose( array ){
                if(!array[2]){return};
                this.showOptions = false;
                this.$emit('onSelect',array);
            },
            showOptionsFn(ev){
                if(this.disabled)return;
                var parames = $(ev.target).offset(),
                    scrollTop = $(window).scrollTop(),
                    wrapHeight = $('.select-wrap').height(),
                    standardHeight = parames.top - scrollTop + wrapHeight,
                    ulHeight = $(ev.target).next().height(),
                    unable = $(window).height() - standardHeight < ulHeight;
                if(this.fixed){
                    if(unable){
                        standardHeight = parames.top - scrollTop - ulHeight;
                    }
                    this.listPositon.left = parames.left + 'px';
                }else{
                    if(unable){
                        standardHeight = - ulHeight;
                    }else{
                        standardHeight = wrapHeight;
                    }
                    this.listPositon.left = '0px';
                }
                this.listPositon.top = standardHeight + 'px';
                this.showOptions = !this.showOptions;

            },
            inputVal(ev){
                if(!this.isText){
                    var num = Math.abs(ev.target.value);
                    // if(num < 1 || num >= 100){
                    //     ev.target.value = 14;
                    //     return;
                    // }
                    this.$emit('selectOption',[num,num,true]);
                }else{
                    var text = ev.target.value.trim();
                    if(text){
                        this.$emit('selectOption',[text,text,true]);
                    }
                }
            }
        },
        mounted(){
            var self = this;
            document.addEventListener('click',function(){
                if(self.showOptions){
                    self.showOptions = false;
                }
            })
        },
    }
</script>