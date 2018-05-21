/**
 *	author: kygeng
 *	date: 2017-5-2
 *	desc: 图表数据更新定时器
 */
(function($) {
	this.ChartTimer = {
        _chartTimer: {},
        get: function(key){
            let valWrapper = this._chartTimer[key];
            if(typeof valWrapper == 'undefined' && valWrapper != null){
                return null;
            }
            return valWrapper;
        },
        set: function(key, value){
            this._chartTimer[key] = {
                callback: value.callback,
                timer: value.timer,
                index: value.index,
                chartWrapper: value.chartWrapper
            };
        },
        clear: function(){
            var keys = Object.keys(this._chartTimer);
            for (var i = 0; i < keys.length; i++) {
                clearTimeout(this.get(keys[i]).timer);
            }
            this._chartTimer = {};
        },
        startTimer(chartWrapper){
            var self = this;

			var valWrapper = self.get(chartWrapper.id);
			if (valWrapper.timer != null) {
				clearTimeout(valWrapper.timer);
			}

			valWrapper.timer = setTimeout(function(){
                $.when(self.loadChartData(chartWrapper.dataSourceUrl)).done(function(resp){
                    let value = self.get(chartWrapper.id);
                    if (value != null) {
                        value.callback(value.index, value.chartWrapper, resp.table);
                        self.startTimer(value.chartWrapper);
                    }
                }).fail(function(resp){
                    let value = self.get(chartWrapper.id);
                    if (value != null) {
                        value.callback(value.index, value.chartWrapper, resp.table);
                        self.startTimer(value.chartWrapper);
                    }
                });
            }, chartWrapper.updateFreq*1000 || 60*1000);

            self.set(chartWrapper.id, {
                callback: valWrapper.callback,
                timer: valWrapper.timer,
                index: valWrapper.index,
                chartWrapper: valWrapper.chartWrapper
            });
        },
        loadChartData(url){
            var defer = $.Deferred();
            $.get({
                url:url,
                success:function(resp){
                    defer.resolve(resp);
                }
            });
            return defer.promise();
        }
	}
}(jQuery));
