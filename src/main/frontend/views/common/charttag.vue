<template>
    <div id="charttag" ref="tagbox" @click.stop>
        <div class="triangleout">
            <div class="trianglein"></div>
        </div>
        <div class="triangleoutright">
            <div class="triangleinright"></div>
        </div>
        <div class="ct-header" v-show="admin == 1 || admin == 3">
            <div class="color-bar">
                <div :class="{active:sc=='#5B9CE4'}" @click="changecolor('#5B9CE4')">
                    <i></i>
                </div>
                <div :class="{active:sc=='#C8CE2F'}" @click="changecolor('#C8CE2F')">
                    <i></i>
                </div>
                <div :class="{active:sc=='#F69733'}" @click="changecolor('#F69733')">
                    <i></i>
                </div>
                <div :class="{active:sc=='#ED6723'}" @click="changecolor('#ED6723')">
                    <i></i>
                </div>
            </div>
            <div class="writedel" v-show="!newtag">
                <i class="tag_write" @click="writenow" title="编辑"></i>
                <i class="tag_del" @click="deltag" title="删除"></i>
            </div>
        </div>
        <div class="ct-body">
            <span id="tagtextshow" v-if="!writeing && !newtag" @dblclick="writenow" v-text="spantext"></span>
            <textarea maxlength="140" v-show="writeing || newtag" id="tagtextinput" placeholder="按 Enter 保存" v-model="tagtext" 
                :class="{lineheight21:tagtext.length>0,height35px:tagtext.length==0,backcolor:admin}" @keydown.enter="saveText"
                @blur="textblur" 
                @focus="textfocus"></textarea>
            <transition name="fadebutton">    
                <div class="haveTextShow" v-show="tagtext.length>0">
                    <span>按 Enter 保存</span>
                    <button @click="saveText">确定</button>
                </div>
            </transition>
        </div>
        <!--haha-->
        <div class="ct-footer">
            <div class="yname">{{taglinename}}</div>
            <div class="xname">{{tagxname}}</div>
        </div>
    </div>
</template>

<script type="text/javascript">
    export default{
        name:'charttag',
        props: {
            _left: {
                type: Number,
                default: 10
            },
            _top:{
                type: Number,
                default: 10
            },
            tagpoint:{
                type: Object,
                default: null
            },
            admin:{
                type: Number,
                default:0
            },
            newtag: {
                type: Boolean,
                default: false
            },
            spantext:{
                type: String,
                default: ""
            },
            sc: {
                type: String,
                default: ""
            },
            cleantagtext: {
                type: Boolean,
                default: false
            },
            tagxname: {
                type: String,
                default: ""
            },
            taglinename: {
                type: String,
                default: ""
            },
        },
        watch:{
            _left(newVal){
                this.$refs.tagbox.style.left = newVal +'px';
            },
            _top(newVal){
                this.$refs.tagbox.style.top = newVal +'px';
            },
            cleantagtext(newVal){
                if(newVal)this.tagtext = "";
            },
        },
        data(){
            return {
                tagtext:"",
                writeing:false,
                newSaveText:"",
            }
        },
        methods:{
            saveText(){
                 //保存标注
                var self = this;
                if($.trim(self.tagtext).length == 0){
                    $("#charttag").hide();
                    //去掉marker
                    self.$emit("savepoint",self.tagpoint,undefined);
                }else{
                    //ajax请求成功success
                    // console.log(self.tagpoint);
                    self.$emit("savepoint",self.tagpoint,{
                        fillColor: self.sc,
                        text: self.tagtext,
                        enabled: true,
                        lineColor: "#fff",
                        radius: 8,
                        lineWidth: 2,
                    });
                    self.newSaveText = self.tagtext;
                }
                this.$emit("changespantext",self.tagtext);
                this.tagtext = "";
                this.writeing = false;
                this.$emit("changenewtag",false);
            },
            textblur(){
                 
            },
            textfocus(){
                
            },
            writenow(){
                var self = this;
                if(!this.admin){
                    return;
                }
                this.newSaveText = self.spantext;
                this.writeing = true;
                this.tagtext = self.spantext;
                setTimeout(function(){
                    $("#tagtextinput").focus();
                },0)
            },
            deltag(){
                var self = this;
                this.$emit("deletepoint",self.tagpoint,undefined);
                $("#charttag").hide();
            },
            changecolor(color){
                var self = this;
                this.$emit("changetagcolor",color);
                var _text = "";
                if(!this.writeing){
                    //非编辑状态
                    _text = self.spantext;
                    this.$emit("savepoint",self.tagpoint,{
                        fillColor: color,
                        text: _text,
                        enabled: true,
                        lineColor: "#fff",
                        radius: 8,
                        lineWidth: 2,
                    })
                }else{
                    _text = self.newSaveText;
                    //编辑状态只改变颜色 不保存到数据库
                    self.tagpoint.update({
                        marker: {
                            fillColor: color,
                            text: _text,
                            enabled: true,
                            lineColor: "#fff",
                            radius: 8,
                            lineWidth: 2,
                        }
                    })
                }
                $("#tagtextinput").focus();
            }
        },
        mounted(){
            var self = this;
            autosize(document.querySelector('.ct-body>textarea'));
            
        },
    }
</script>

<style type="text/css" lang="less" scoped>
    @color4: #ED6723;
    @color3: #F69733;
    @color2: #C8CE2F;
    @color1: #5B9CE4;
    .lineheight21{
        line-height: 21px!important;
    }
    .height35px{
        height: 35px!important;
    }
    .fadebutton-enter-active, .fadebutton-leave-active {
      transition: opacity .1s
    }
    .fadebutton-enter, .fadebutton-leave-active {
      opacity: 0
    }
    .triangleout{
        width: 0;
        height: 0;
        border-top: 10px solid transparent;
        border-right: 12px solid #373E50;
        border-bottom: 10px solid transparent;
        position: absolute;
        left: -12px;
        top: 15px;
    }
    .trianglein{
        width: 0;
        height: 0;
        border-top: 10px solid transparent;
        border-right: 12px solid #212632;
        border-bottom: 10px solid transparent;
        position: absolute;
        left: 1px;
        top: -10px;
    }
    .triangleoutright{
        width: 0;
        height: 0;
        border-top: 10px solid transparent;
        border-left: 12px solid #373E50;
        border-bottom: 10px solid transparent;
        position: absolute;
        right: -12px;
        top: 15px;
        display: none;
    }
    .triangleinright{
        width: 0;
        height: 0;
        border-top: 10px solid transparent;
        border-left: 12px solid #212632;
        border-bottom: 10px solid transparent;
        position: absolute;
        left: -13px;
        top: -10px;
    }
    #charttag{
        width: 300px;
        position: absolute; 
        z-index: 99999;
        background: #212632;
        border: 1px solid #373E50;
        box-shadow: 0 2px 10px 0 rgba(0,0,0,0.40);
        border-radius: 4px;
        display: none;
    }
    .ct-header{
        height: 30px;
        padding: 0 15px;
        display: flex;
        justify-content: space-between;
        .color-bar{
            width: 15px;
            height: 100%;
            position: relative;
            div{
                cursor: pointer;
                top: 8px;
                left: 0;
                position: absolute;
                border-radius: 50%;
                height: 14px;
                width: 14px;
                transition: left 0.2s linear;
                i{
                    display: inline-block;
                    height: 12px;
                    width: 12px;
                    border-radius: 50%;
                    border: 1px solid #212632;
                    position: absolute;
                    left: 0px;
                    top: 0px;
                }
            }
            div.active{
                z-index: 999999;
            }
            div:nth-child(4){
                i{
                    background-color: @color4;
                }
            }
            div:nth-child(4).active{
                border: 1px solid @color4;
            }
            div:nth-child(3){
                i{
                    background-color: @color3;
                }
            }
            div:nth-child(3).active{
                border: 1px solid @color3;
            }
            div:nth-child(2){
                i{
                    background-color: @color2;
                }
            }
            div:nth-child(2).active{
                border: 1px solid @color2;
            }
            div:nth-child(1){
                i{
                    background-color: @color1;
                }
            }
            div:nth-child(1).active{
                border: 1px solid @color1;
            }
        }
        .writedel{
            display: flex;
            align-items: center;
            i{
                display: inline-block;
                height: 16px;
                width: 16px;
                margin-left: 15px;
                cursor: pointer;
            }
            .tag_write{
                background-image: url(/images/tag/icon-write.png);
            }
            .tag_write:hover{
                background-image: url(/images/tag/icon-write-hover.png);
            }
            .tag_del{
                background-image: url(/images/tag/icon-del.png);
            }
            .tag_del:hover{
                background-image: url(/images/tag/icon-del-hover.png);
            }
        }
    }
    .color-bar:hover{
        width: 100px;
        div:nth-child(3){
            left: 25px;
        }
        div:nth-child(2){
            left: 50px;
        }
        div:nth-child(1){
            left: 75px;
        }
    }
    .ctbody.backcolor{
        background: #1B1F29;
    }
    .ct-body{
        min-height: 35px;
        padding: 10px 19px;
        #tagtextshow{
            font-size: 14px;
            color: #7985A3;
            line-height: 21px;
        }
        textarea{
            width: 100%;
            height: 35px;
            font-size: 14px;
            color: #7985A3;
            max-height: 250px;
            resize: none; 
            line-height: 35px;
        }
        .haveTextShow{
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 5px 0;
            span{
                color: #525E7B;
                font-size: 12px;
            }
            button{
                background: #0FACD9;
                borer:none;
                outline: none;
                border-radius: 4px;
                height: 22px;
                width: 55px;
                color: #fff;
            }
        }
    }
    .ct-footer{
        height: 35px;
        display: flex;
        justify-content: space-between;
        padding: 0 15px;
        .xname,.yname{
            height: 100%;
            line-height: 35px;
            font-size: 12px;
            color: #7985A3;
        }
    }
</style>