<template>
	<div class="chart-header-wrapper">
		<div class="chart-header-wrapper-fullscreen">
       <!-- <div class="chart-header-chart-menu" @click="showChartList()">
				<div class="icon"></div><span v-if="false">图表</span>
			</div> -->
			<div class="btn-back" @click="exitFullScreen()">{{lang?'返回':'back'}}</div>
       <div class="chart-header-caption-tab" v-if="!isMobile" :class="{'active':showtype=='caption','marginRight':exports ==1}" @click="toCaption()">
        <div class="icon"></div><span>{{lang?'说明':'Caption'}}</span>
      </div>
        <div class="chart-header-table-tab" v-if="!isMobile && !isTableChart" :class="{'active':showtype=='table'}" @click="setFullScreenWatchTable(true)">
        <div class="icon"></div><span>{{lang?'数据':'Data'}}</span>
      </div>
			<div class="chart-header-chart-tab" v-if="!isMobile" :class="{'active':showtype=='chart'}" @click="setFullScreenWatchTable(false)">
				<div class="icon"></div><span>{{lang?'图表':'Chart'}}</span>
			</div>
			<div class="chart-header-chart-title" v-text="curWatchChart_copy.title"></div>
			<div class="chart-header-change-tab" v-if="isMobile && !isTableChart" @click="changeFullScreenWatchTable">
				<div class="icon"></div>
			</div>
			 <div class="data-export" v-if="!isMobile" v-show=" exports == 1 ">
				<a href="javascript:void(0)" class="dropdown-toggle icon-wrapper" @click="fileExport('excel')">
					<i class="icon-export"></i> 
					<span>{{lang?'导出':'Export Data'}}</span>
				</a>
			</div> 
		</div>
	</div>
</template>
<style type="text/css" scoped>
.fullscreen-wrapper .chart-header-wrapper {
  position: absolute;
  top: 0px;
  left: 0px;
  width: 100%;
  height: 50px;
  z-index: 2000;
}
.fullscreen-wrapper .chart-header-wrapper-fullscreen {
  display: -webkit-flex;
  display: flex;
  /*flex-direction: row;*/
  flex-direction: row-reverse;
  align-items: center;
  width: 100%;
  height: 100%;
}
.fullscreen-wrapper .chart-header-wrapper-fullscreen > div {
  display: -webkit-flex;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  font-size: 12px;
  line-height: 16px;
  cursor: pointer;
}
.fullscreen-wrapper .chart-header-wrapper-fullscreen .chart-header-chart-title {
  cursor: default;
}
.fullscreen-wrapper .chart-header-wrapper-fullscreen .btn-back {
  width: 120px;
  font-size: 14px;
  color: #7985a3;
  position: absolute;
  left: 0;
  top: 0;
}
.fullscreen-wrapper .chart-header-wrapper-fullscreen .btn-back:hover {
  color: #92bdd2;
}
.fullscreen-wrapper .chart-header-wrapper-fullscreen .btn-back:before {
  content: " <";
  vertical-align: "top";
  margin-right: 10px;
}
.fullscreen-wrapper .chart-header-wrapper-fullscreen > div > div.icon {
  padding: 0px;
  margin: 0px 10px 0px 0px;
  width: 16px;
  height: 16px;
  background-repeat: no-repeat;
  background-size: initial;
}
.fullscreen-wrapper .chart-header-chart-menu {
  flex-basis: 49px;
}
.fullscreen-wrapper .chart-header-chart-menu .icon {
  margin: 0px !important;
  background-position: -64px 0px;
}
.fullscreen-wrapper .chart-header-chart-menu:hover .icon {
  background-position: -80px 0px;
}
.fullscreen-wrapper .chart-header-caption-tab.marginRight{
  margin-right: 90px;
}
.fullscreen-wrapper .chart-header-chart-tab,
.fullscreen-wrapper .chart-header-table-tab,
.fullscreen-wrapper .chart-header-caption-tab {
  flex-basis: 160px;
}
.fullscreen-wrapper .chart-header-chart-tab .icon {
  background-position: 0px 0px;
}
.fullscreen-wrapper .chart-header-chart-tab:hover .icon,
.fullscreen-wrapper .chart-header-chart-tab.active .icon {
  background-position: -16px 0px;
}
.fullscreen-wrapper .chart-header-table-tab .icon {
  background-position: -32px 0px;
}
.fullscreen-wrapper .chart-header-table-tab:hover .icon,
.fullscreen-wrapper .chart-header-table-tab.active .icon {
  background-position: -48px 0px;
}
.fullscreen-wrapper .chart-header-caption-tab .icon {
  background-position: -64px 0px;
}
.fullscreen-wrapper .chart-header-caption-tab:hover .icon,
.fullscreen-wrapper .chart-header-caption-tab.active .icon {
  background-position: -80px 0px;
}
.fullscreen-wrapper .chart-header-chart-title {
  position: absolute;
  /*left: 50%;*/
  /*top: 0px;*/
  left:50%;
  top: 50px;
  transform: translateX(-50%);
  font-size: 16px !important;
  font-weight: 600;
  color: #19bdec;
}
.fullscreen-wrapper .chart-header-exit-fullscreen {
  flex-basis: 110px;
  height: 32px !important;
  line-height: 32px;
  margin: 0px 15px 0px 0px;
  transition: all 0.1s linear;
}
.fullscreen-wrapper .chart-header-exit-fullscreen .icon {
  background-position: -128px 0px;
}
.data-export {
  position: absolute;
  right: 0;
  top: 0;
}
.data-export .dropdown-toggle {
  height: 32px;
  line-height: 32px;
  padding: 0px 20px;
  margin-right: 15px;
  color: inherit;
  text-decoration: none;
}
.data-export .dropdown-toggle:hover {
  color: #19bdec;
}
.data-export .dropdown-toggle:hover .icon-export {
  background-position: -192px -7px;
}
.data-export .icon-export {
  display: inline-block;
  margin-right: 7.5px;
  width: 16px;
  height: 16px;
  vertical-align: text-bottom;
  background: url(/images/sprite-fullscreen.png) no-repeat;
  background-position: -165px -7px;
}
</style>
<script type="text/javascript">
export default {
  name: "navigation-panel",
  data() {
    return {
      exports: 0,
      dashboardName: ""
    };
  },
  mounted() {
    //导出数据权限
    this.exports = parseInt(sessionStorage.exports);
    this.dashboardName = localStorage.getItem("dashboard-name");
  },
  computed: {
    isFullScreenWatch() {
      return this.$store.state.fullscreen.isFullScreenWatch;
    },
    isFullScreenWatchTable() {
      return this.$store.state.fullscreen.isFullScreenWatchTable;
    },
    dashBoard() {
      return this.$store.state.dashboard.curDashBoard;
    },
    curWatchChartIdx() {
      return this.$store.state.fullscreen.curWatchChartIdx;
    },
    isTableChart() {
      if (this.curChart) {
        return (
          this.curChart.chartType === "table" ||
          this.curChart.type === "research_data_list" ||
          this.curChart.type === "research_stock_card"
        );
      }
      return false;
    },
    forceShowChartTab() {
      // 强制显示图表Tab
      if (this.curChart) {
        return (
          this.curChart.type === "research_data_list" ||
          this.curChart.type === "research_stock_card"
        );
      }
      return false;
    },
    curWatchChart_copy() {
      return this.$store.state.fullscreen.curWatchChart_copy || {};
    },
    isShowChartList() {
      return this.$store.state.fullscreen.isShowChartList;
    },
    curFullChart() {
      return this.$store.state.fullscreen.curFullChart;
    },
    curChart() {
      if (this.dashBoard && this.isFullScreenWatch) {
        return this.dashBoard.charts[this.curWatchChartIdx];
      } else {
        return null;
      }
    },
    watchDbId() {
      return this.$store.state.dashboard.dashBoardId;
    },
    fromPath() {
      return this.$store.state.fullscreen.fromPath;
    },
    lang() {
      return this.$store.state.dashboard.lang;
    },
    isMobile() {
      return this.$store.state.common.isMobile;
    },
    curDay() {
      return this.$store.state.dashboard.curDay;
    },
    showtype() {
      return this.$store.state.fullscreen.showtype;
    }
  },
  methods: {
    // showChartList(){
    // 	this.$store.commit('setShowChartList',{isShow:!this.isShowChartList});
    // },
    setFullScreenWatchTable(isWatch) {
      if (isWatch) {
        this.$store.commit("zb_changeShowType", "table");
        GaHelper.sendEvent(
          GaHelper.FullScreen.viewData,
          this.dashboardName + "看板-" + this.curWatchChart_copy.title + "-data"
        );
      } else {
        this.$store.commit("zb_changeShowType", "chart");
        GaHelper.sendEvent(
          GaHelper.FullScreen.viewChart,
          this.dashboardName + "看板-" + this.curWatchChart_copy.title + "chart"
        );
      }
      if (this.forceShowChartTab && !isWatch) {
        return false;
      }
      this.$store.commit("setFullScreenWatchTable", { isWatch: isWatch });
    },
    changeFullScreenWatchTable() {
      this.$store.commit("setFullScreenWatchTable", {
        isWatch: !this.isFullScreenWatchTable
      });
    },
    toCaption() {
      this.$store.commit("zb_changeShowType", "caption");
    },
    destroyChartOrTable() {
      //退出全屏清空图表内容
      if ($("#cc-chart-container").highcharts()) {
        $("#cc-chart-container")
          .highcharts()
          .destroy();
      }
      if ($("#cc-table-container").handsontable("getInstance")) {
        $("#cc-table-container")
          .handsontable("getInstance")
          .destroy();
      }
      $("#cc-table-container").empty();
    },
    exitFullScreen() {
      var self = this;
      self.destroyChartOrTable();
      self.$store.commit("setFullScreenWatch", { isWatch: false });
      self.$store.commit("setShowChartList", { isShow: false });
      self.$router.push({
        name: "dashboard",
        params: {
          bid: self.watchDbId
        }
      });
    },
    fileExport(type) {
      GaHelper.sendEvent(
        GaHelper.FullScreen.export,
        this.dashboardName + "看板-" + this.curWatchChart_copy.title + "-export"
      );
      //console.log(this.curWatchChart_copy.title);
      if (this.curChart) {
        if (type == "excel") {
          FileExport.export({
            type: "excel",
            data: this.curFullChart.data,
            fname: (this.curWatchChart_copy.title || "").trim().substring(0, 31)
          });
        } else if (type == "png") {
          return false;
          FileExport.export({
            type: "png",
            data: {
              sId: this.isTableChart
                ? "cc-table-container"
                : "cc-chart-container",
              stype: this.isTableChart ? "html" : "highchart"
            },
            fname: (this.curWatchChart_copy.title || "").trim().substring(0, 31)
          });
        }
      }
    }
  }
};
</script>
