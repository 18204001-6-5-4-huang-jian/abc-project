/**
 *	author: jhuang
 *	date: 2018-5-1
 *	desc: 实现SVG、HTML元素的导出功能
 */
(function($, H) {
	this.Workbook = function() {
		if(!(this instanceof Workbook)) return new Workbook();
		this.SheetNames = [];
		this.Sheets = {};
	}

	this.FileExport = {
		// 支持的导出类型
		support_status: {
			'excel': !(!!window.ActiveXObject && document.all && !window.atob),
			'png': !(!!window.ActiveXObject && document.all && !window.atob),
			'png-base64': !(!!window.ActiveXObject && document.all && !window.atob)
		},
		browserSupportDownload: false,
		browserSupportBlob: window.Blob && window.navigator.msSaveOrOpenBlob,
		_init: function() {
		    var a = document.createElement('a');
		    if (typeof window.btoa != "undefined" && typeof a.download != "undefined") {
		        this.browserSupportDownload = true;
		    }
		},
		_validate: function(config) {
			var self = this;

			self._init();

			if(Object.keys(config || {}).length > 0
				&& config.hasOwnProperty('type')
				&& config.hasOwnProperty('data')
				&& config.hasOwnProperty('fname')) {

				if(self._support(config.type)){
					return true;
				} else {
					console.error('不支持此导出类型');
				}
			} else {
				console.error('缺少导出参数');
			}
			return false;
		},
	    _download: function(context, data) {
	        if (!data || (!data.content && !(data.datauri || data.blob))) {
	            throw new Error("Something went wrong while exporting the chart");
	        }

	        if (this.browserSupportDownload && (data.datauri || data.content)) {
	            a = document.createElement('a');
	            a.href = data.datauri || ('data:' + context.type + ';base64,' + window.btoa(unescape(encodeURIComponent(data.content))));
	            a.download = context.filename;
	            document.body.appendChild(a);
	            a.click();
	            a.remove();
	        } else if (this.browserSupportBlob && (data.blob || data.content)) {
	            blobObject = data.blob || new Blob([data.content], { type: context.type });
	            window.navigator.msSaveOrOpenBlob(blobObject, context.filename);
	        } else {
	            window.open(data);
	        }
	    },
	    _getScaleSize: function(size) {
	    	var MIN_LEN = 600;
	    	var radio   = size.width * 1.0 / size.height;

	    	while(size.width < MIN_LEN || size.height < MIN_LEN){
	    		if(size.width < MIN_LEN){
	    			size.width  = MIN_LEN;
	    			size.height = MIN_LEN / radio;
		    	}
		    	if(size.height < MIN_LEN){
		    		size.width  = MIN_LEN * radio;
	    			size.height = MIN_LEN;
		    	}
	    	}
	    	return size;
	    },
	    _svgToCanvas: function(svg, width, height, callback) {
	        var canvas = document.createElement('canvas');
	        var size   = this._getScaleSize({
							width : width,
							height: height
						});

	        canvas.setAttribute('width', size.width);
	        canvas.setAttribute('height', size.height);

	        canvg(canvas, svg, {
	            ignoreMouse: true,
	    		ignoreAnimation: true,
	    		ignoreDimensions: true,
	    		ignoreClear: true,
	            offsetX: 0,
	            offsetY: 0,
	            scaleWidth: size.width,
	            scaleHeight: size.height,
	            renderCallback: function() { callback(canvas); }
	        });

	        return canvas;
	    },
	    _getSafeFname: function(fname) {
	    	// Excel 名称限制在31个字符且不能包含[]\/?
	    	return (fname || '').trim().substring(0, 31).replace(/[\\\/\?\[\]]/ig, '_');
	    },
	    _getDataUri: function(canvas) {
	    	var self = this;
	    	return {
        		datauri: self.browserSupportDownload && canvas.toDataURL && canvas.toDataURL("image/png"),
        		blob: !self.browserSupportDownload && canvas.msToBlob && canvas.msToBlob()
        	}
	    },
	    _convertImgToBase64: function(url, callback, outputFormat){
	    	var self = this;
			var canvas = document.createElement('CANVAS'),
			ctx = canvas.getContext('2d'),
			img = new Image;
			img.crossOrigin = 'Anonymous';
			img.onload = function(){
				canvas.height = img.height;
				canvas.width  = img.width;

				ctx.drawImage(img, 0, 0);
				callback.call(this, self._getDataUri(canvas));
				canvas = null;
			};
			img.src = url;
		},
		isArray: function(obj) {
			return Object.prototype.toString.call(obj)=='[object Array]';
		},
		// 导出文件
		export: function(config) {
			var self = this;

			if(this._validate(config)){
				if(config.type == 'excel'){
		            if(config.data && self.isArray(config.data) && self.isArray(config.data[0])){
						if(config.data[0][0] == 'date' || config.data[0][0] == 'datetime'){
			                config.data[0][0] = '日期';
			            }

						// 导出Excel
						this._exportExcel(config.data, this._getSafeFname(config.fname));
		            } else {
		            	console.error('Excel 导出数据格式错误');
		            }
				} else if(config.type == 'png' || config.type == 'png-base64') {

					// 导出PNG
					if(config.data.sId){
						if(config.data.stype == 'highchart'){
							// Highcharts
							setTimeout(function(){
								var chart = $('#' + config.data.sId).highcharts();
	    						var svg = chart.getSVG({
							        exporting: {
							            sourceWidth: chart.chartWidth,
							            sourceHeight: chart.chartHeight + 0
							        },
							        title: {
							        	text: config.fname,
							        	align: 'left',
							        	style: {
							        		fontSize: '14px',
	    									fontWeight: 600
							        	}
							        }
							    });
	    						if(config.type == 'png'){
	    							self._svgToCanvas(svg, chart.chartWidth, chart.chartHeight + 0, function(canvas){
				                    	self._download({
							        		type: 'image/png',
							        		filename: config.fname + '.png',
							        	}, self._getDataUri(canvas));
				                    })
	    						}else if(config.type == 'png-base64'){
	    							//导出word支持
	    							self._svgToCanvas(svg, chart.chartWidth, chart.chartHeight + 0, function(canvas){
	    								config.callback(canvas.toDataURL("image/png"));
				                    })
	    						}
			                    
							}, 10);
						} else if(config.data.stype == 'img') {
							setTimeout(function(){
								self._convertImgToBase64(config.data.sId, function(data){
									self._download({
						        		type: 'image/png',
						        		filename: config.fname + '.png',
						        	}, data);
								})
							}, 10);
						} else {
							setTimeout(function(){
								// 普通Dom导出
								html2canvas($('#' + config.data.sId), {
							        onrendered: function (canvas) {
							        	self._download({
							        		type: 'image/png',
							        		filename: config.fname + '.png',
							        	}, self._getDataUri(canvas));
							        }
							   	});
							}, 10);
						}
					}
				}
			}
		},
		_support: function(type){
			return this.support_status[type];
		},
		_datenum: function(v, date1904) {
			if(date1904) v+=1462;
			var epoch = Date.parse(v);
			return (epoch - new Date(Date.UTC(1899, 11, 30))) / (24 * 60 * 60 * 1000);
		},
		_sheetFromArrayOfArrays: function(data, opts) {
			// 通过二维数组创建WorkSheet
			var ws = {};
			var range = {s: {c:10000000, r:10000000}, e: {c:0, r:0 }};
			for(var R = 0; R != data.length; ++R) {
				for(var C = 0; C != data[R].length; ++C) {
					if(range.s.r > R) range.s.r = R;
					if(range.s.c > C) range.s.c = C;
					if(range.e.r < R) range.e.r = R;
					if(range.e.c < C) range.e.c = C;
					var cell = {v: data[R][C] };
					if(cell.v == null) continue;
					var cell_ref = XLSX.utils.encode_cell({c:C,r:R});

					if(typeof cell.v === 'number') cell.t = 'n';
					else if(typeof cell.v === 'boolean') cell.t = 'b';
					else if(cell.v instanceof Date) {
						cell.t = 'n'; cell.z = XLSX.SSF._table[14];
						cell.v = datenum(cell.v);
					}
					else cell.t = 's';

					ws[cell_ref] = cell;
				}
			}
			if(range.s.c < 10000000) ws['!ref'] = XLSX.utils.encode_range(range);
			return ws;
		},

		_s2ab: function(s) {
			var buf = new ArrayBuffer(s.length);
			var view = new Uint8Array(buf);
			for (var i=0; i!=s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF;
			return buf;
		},
		_exportExcel: function(data, fname) {
			/* original data */
			data  = data  || [];
			fname = fname || '未命名工作表.xlsx';

			var wb = new Workbook(),
				ws = this._sheetFromArrayOfArrays(data);

			/* add worksheet to workbook */
			wb.SheetNames.push(fname);
			wb.Sheets[fname] = ws;

			var wbout = XLSX.write(wb, {bookType: 'xlsx', bookSST: true, type: 'binary'});

			saveAs(new Blob([this._s2ab(wbout)],
				{type: "application/octet-stream"}),
				fname + '.xlsx')
		}
	}
}(jQuery, Highcharts));
