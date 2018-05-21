var testLayout = [
    {"x":0,"y":0,"w":2,"h":2,"i":"0"},
    {"x":2,"y":0,"w":2,"h":4,"i":"1"},
    {"x":4,"y":0,"w":2,"h":5,"i":"2"},
    {"x":6,"y":0,"w":2,"h":3,"i":"3"},
    {"x":8,"y":0,"w":2,"h":3,"i":"4"},
    {"x":10,"y":0,"w":2,"h":3,"i":"5"},
    {"x":0,"y":5,"w":2,"h":5,"i":"6"},
    {"x":2,"y":5,"w":2,"h":5,"i":"7"},
    {"x":4,"y":5,"w":2,"h":5,"i":"8"},
    {"x":6,"y":4,"w":2,"h":4,"i":"9"},
    {"x":8,"y":4,"w":2,"h":4,"i":"10"},
    {"x":10,"y":4,"w":2,"h":4,"i":"11"},
    {"x":0,"y":10,"w":2,"h":5,"i":"12"},
    {"x":2,"y":10,"w":2,"h":5,"i":"13"},
    {"x":4,"y":8,"w":2,"h":4,"i":"14"},
    {"x":6,"y":8,"w":2,"h":4,"i":"15"},
    {"x":8,"y":10,"w":2,"h":5,"i":"16"},
    {"x":10,"y":4,"w":2,"h":2,"i":"17"},
    {"x":0,"y":9,"w":2,"h":3,"i":"18"},
    {"x":2,"y":6,"w":2,"h":2,"i":"19"},
    {"x":0,"y":0,"w":2,"h":2,"i":"20"},
    {"x":2,"y":0,"w":2,"h":4,"i":"21"},
    {"x":4,"y":0,"w":2,"h":5,"i":"22"},
    {"x":6,"y":0,"w":2,"h":3,"i":"23"},
    {"x":8,"y":0,"w":2,"h":3,"i":"24"},
    {"x":10,"y":0,"w":2,"h":3,"i":"25"},
    {"x":0,"y":5,"w":2,"h":5,"i":"26"},
    {"x":2,"y":5,"w":2,"h":5,"i":"27"},
    {"x":4,"y":5,"w":2,"h":5,"i":"28"},
    {"x":6,"y":4,"w":2,"h":4,"i":"29"},
    {"x":8,"y":4,"w":2,"h":4,"i":"30"},
    {"x":10,"y":4,"w":2,"h":4,"i":"31"},
    {"x":0,"y":10,"w":2,"h":5,"i":"32"},
    {"x":2,"y":10,"w":2,"h":5,"i":"33"},
    {"x":4,"y":8,"w":2,"h":4,"i":"34"},
    {"x":6,"y":8,"w":2,"h":4,"i":"35"},
    {"x":8,"y":10,"w":2,"h":5,"i":"36"},
    {"x":10,"y":4,"w":2,"h":2,"i":"37"},
    {"x":0,"y":9,"w":2,"h":3,"i":"38"},
    {"x":2,"y":6,"w":2,"h":2,"i":"39"},
    {"x":0,"y":0,"w":2,"h":2,"i":"40"},
    {"x":2,"y":0,"w":2,"h":4,"i":"41"},
    {"x":4,"y":0,"w":2,"h":5,"i":"42"},
    {"x":6,"y":0,"w":2,"h":3,"i":"43"},
    {"x":8,"y":0,"w":2,"h":3,"i":"44"},
    {"x":10,"y":0,"w":2,"h":3,"i":"45"},
    {"x":0,"y":5,"w":2,"h":5,"i":"46"},
    {"x":2,"y":5,"w":2,"h":5,"i":"47"},
    {"x":4,"y":5,"w":2,"h":5,"i":"48"},
    {"x":6,"y":4,"w":2,"h":4,"i":"49"},
    {"x":8,"y":4,"w":2,"h":4,"i":"50"},
    {"x":10,"y":4,"w":2,"h":4,"i":"51"},
    {"x":0,"y":10,"w":2,"h":5,"i":"52"},
    {"x":2,"y":10,"w":2,"h":5,"i":"53"},
    {"x":4,"y":8,"w":2,"h":4,"i":"54"},
    {"x":6,"y":8,"w":2,"h":4,"i":"55"},
    {"x":8,"y":10,"w":2,"h":5,"i":"56"},
    {"x":10,"y":4,"w":2,"h":2,"i":"57"},
    {"x":0,"y":9,"w":2,"h":3,"i":"58"},
    {"x":2,"y":6,"w":2,"h":2,"i":"59"},
    {"x":0,"y":0,"w":2,"h":2,"i":"60"},
    {"x":2,"y":0,"w":2,"h":4,"i":"61"},
    {"x":4,"y":0,"w":2,"h":5,"i":"62"},
    {"x":6,"y":0,"w":2,"h":3,"i":"63"},
    {"x":8,"y":0,"w":2,"h":3,"i":"64"},
    {"x":10,"y":0,"w":2,"h":3,"i":"65"},
    {"x":0,"y":5,"w":2,"h":5,"i":"66"},
    {"x":2,"y":5,"w":2,"h":5,"i":"67"},
    {"x":4,"y":5,"w":2,"h":5,"i":"68"},
    {"x":6,"y":4,"w":2,"h":4,"i":"69"},
    {"x":8,"y":4,"w":2,"h":4,"i":"70"},
    {"x":10,"y":4,"w":2,"h":4,"i":"71"},
    {"x":0,"y":10,"w":2,"h":5,"i":"72"},
    {"x":2,"y":10,"w":2,"h":5,"i":"73"},
    {"x":4,"y":8,"w":2,"h":4,"i":"74"},
    {"x":6,"y":8,"w":2,"h":4,"i":"75"},
    {"x":8,"y":10,"w":2,"h":5,"i":"76"},
    {"x":10,"y":4,"w":2,"h":2,"i":"77"},
    {"x":0,"y":9,"w":2,"h":3,"i":"78"},
    {"x":2,"y":6,"w":2,"h":2,"i":"79"}
];

//var Vue = require('vue');

Vue.config.debug = true;
Vue.config.devtools = true;

var GridLayout = VueGridLayout.GridLayout;
var GridItem = VueGridLayout.GridItem;

new Vue({
    el: '#app',
    components: {
        "GridLayout": GridLayout,
        "GridItem": GridItem
    },
    data: {
        layout: testLayout,
        index: 0,
        eventLog: []
    },
    watch: {
        eventLog: function() {
            var eventsDiv = this.$refs.eventsDiv;
            eventsDiv.scrollTop = eventsDiv.scrollHeight;
        }
    },
    methods: {
        moveEvent: function(i, newX, newY){
            var msg = "MOVE i=" + i + ", X=" + newX + ", Y=" + newY;
            this.eventLog.push(msg);
            // console.log(msg);

        },
        resizeEvent: function(i, newH, newW){
            var msg = "RESIZE i=" + i + ", H=" + newH + ", W=" + newW;
            this.eventLog.push(msg);
            // console.log(msg);
        },
        movedEvent: function(i, newX, newY){
            var msg = "MOVED i=" + i + ", X=" + newX + ", Y=" + newY;
            this.eventLog.push(msg);
            // console.log(msg);

        },
        /**
         *
         * @param i the item id/index
         * @param newH new height in grid rows
         * @param newW new width in grid columns
         * @param newHPx new height in pixels
         * @param newWPx new width in pixels
         *
         */
        resizedEvent: function(i, newH, newW, newHPx, newWPx){
            var msg = "RESIZED i=" + i + ", H=" + newH + ", W=" + newW + ", H(px)=" + newHPx + ", W(px)=" + newWPx;
            this.eventLog.push(msg);
            // console.log(msg);
        },
    }
});

