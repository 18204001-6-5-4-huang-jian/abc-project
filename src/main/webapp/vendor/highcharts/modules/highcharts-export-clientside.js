(function(H) {
    if (!H.exporting) {
        H.exporting = function(){};
    }

    // This will be redefined later;
    var oldExport = H.Chart.prototype.exportChart;
    H.Chart.prototype.exportChart = function(){};

    // Set the URL of the export server to a non-existant one, just to be sure.
    var defaultHighChartsOptions = H.getOptions() || {};
    defaultHighChartsOptions.exporting     = defaultHighChartsOptions.exporting || {};
    defaultHighChartsOptions.exporting.url = "http://127.0.0.1:666/";

    defaultHighChartsOptions.exporting.csv = defaultHighChartsOptions.exporting.csv || {};
    defaultHighChartsOptions.exporting.csv.url = "http://127.0.0.1:666/";

    H.setOptions(defaultHighChartsOptions);

    var MIME_TYPES = {
        "PDF": "application/pdf",
        "PNG": "image/png",
        "JPEG": "image/jpeg",
        "SVG": "image/svg+xml",
        "CSV": "text/csv",
        "XLS": "application/vnd.ms-excel"
    };

    var MIME_TYPE_TO_EXTENSION = {
        "application/pdf": ".pdf",
        "image/png": ".png",
        "image/jpeg": ".jpeg",
        "image/svg+xml": ".svg",
        "text/csv": ".csv",
        "application/vnd.ms-excel": ".xlsx"
    };

    // This var indicates if the browser supports HTML5 download feature
    var browserSupportDownload = false;
    var a = document.createElement('a');
    if (typeof window.btoa != "undefined" && typeof a.download != "undefined") {
        browserSupportDownload = true;
    }

    // This is for IE support of Blob
    var browserSupportBlob = window.Blob && window.navigator.msSaveOrOpenBlob;

    /**
    * Describes the MIME types that this module supports.
    * Additionnally, you can call `support(mimeType)` to check
    * that this type is available on the current platform.
    */
    H.exporting.MIME_TYPES = MIME_TYPES;

    /**
    * Little helper function that you can set to the `filename` configuration
    * option to use the chart title for the filename when downloaded.
    */
    H.exporting.USE_TITLE_FOR_FILENAME = function(options, chartOptions) {
        var title = this.title ? this.title.textStr.replace(/ /g, '-').toLowerCase() : 'chart';
            return title;
        };

        var supportStatus = {};
        var buildSupportStatus = function() {
        var hasDownloadOrBlob  = browserSupportDownload || browserSupportBlob;

        var svgSupport = (H.Chart.prototype.getSVG !== undefined);

        // Canvg uses a function named RGBColor, but it's also a not widely known standard object
        // http://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/css.html#CSS-RGBColor
        // Fugly, but heh.
        var rbgColorSupport = false;
        try {
            rbgColorSupport = (new RGBColor("").ok) !== undefined;
        }
        catch(e) {}
        // We also check that a canvas element can be created.
        var canvas = document.createElement('canvas');
        var canvgSupport = typeof canvg !== "undefined" && typeof RGBColor != "undefined" &&
        rbgColorSupport && canvas.getContext && canvas.getContext('2d');

        supportStatus[MIME_TYPES.PNG]  = hasDownloadOrBlob && svgSupport && canvgSupport;
        // On IE, it relies on canvas.msToBlob() which always returns PNG
        supportStatus[MIME_TYPES.JPEG] =  svgSupport && canvgSupport && browserSupportDownload;
    };
    buildSupportStatus();

    /**
    * Checks if the supplied MIME type is available on the
    * current platform for a chart to be exported in.
    * @param mimeType {String} The MIME type.
    * @returns {boolean} <code>true</code> if the MIME type is available on the
    *    current platform.
    */
    H.exporting.supports = function(mimeType) {
        if(supportStatus[mimeType]) {
            return supportStatus[mimeType];
        }
        else {
            return false;
        }
    };

    /*
    * Converts a SVG string to a canvas element
    * thanks to canvg.
    * @param svg {String} A SVG string.
    * @param width {Integer} The rasterized width.
    * @param height {Integer} The rasterized height.
    * @return {DOMNode} a canvas element.
    */
    var svgToCanvas = function(svg, width, height, callback) {
        var canvas = document.createElement('canvas');

        canvas.setAttribute('width', width);
        canvas.setAttribute('height', height);

        canvg(canvas, svg, {
            ignoreMouse: true,
    		ignoreAnimation: true,
    		ignoreDimensions: false,
    		ignoreClear: false,
            offsetX: 0,
            offsetY: 0,
            scaleWidth: width,
            scaleHeight: height,
            renderCallback: function() { callback(canvas); }
        });

        return canvas;
    };

    /**
    * An object to simplifies the retrieval of options in
    * multiple bundles.
    * @param opts {Object} Multiple, an object containing options.
    */
    var Opt = function(opts1, opt2, dotdotdot) {
        this.bundles = arguments;
    };

    /**
    * Fetch the value associated with the specified key in the bundles.
    * First one defined is the one returned.
    * @param key {String} The key.
    * @param value {mixed} The first defined value in the bundles or
    *    <code>undefined</code> if none is found.
    */
    Opt.prototype.get = function(key) {
        for(var i = 0; i < this.bundles.length; i++) {
            if(this.bundles[i] && this.bundles[i][key] !== undefined) {
                return this.bundles[i][key];
            }
        }
        return undefined;
    };

    // Default options.
    var defaultExportOptions = {
        type: MIME_TYPES.PNG,
        scale: 1,
        filename: "chart",
        csv: {
            useBOM: MIME_TYPES.XLS,
            useLocalDecimalPoint: true
        }
    };

    var preRenderImage = function (highChartsObject, options, chartOptions) {
        var opt = new Opt(options, false, defaultExportOptions);

        var scale = opt.get("scale"),
            sourceWidth  = highChartsObject.options.width || opt.get("sourceWidth") || highChartsObject.chartWidth,
            sourceHeight = highChartsObject.options.height || opt.get("sourceHeight") || highChartsObject.chartHeight,
            destWidth  = sourceWidth * scale,
            destHeight = sourceHeight * scale;

        var cChartOptions = chartOptions || false && highChartsObject.options.exporting.chartOptions || {};
        if (!cChartOptions.chart) {
            cChartOptions.chart = { width: destWidth, height: destHeight };
        }
        else {
            cChartOptions.chart.width = destWidth;
            cChartOptions.chart.height = destHeight;
        }

        var svg = highChartsObject.getSVG(cChartOptions);

        return {
            svg: svg,
            destWidth: destWidth,
            destHeight: destHeight
        };
    };

    var renderSvg = function(highChartsObject, context, callback) {
        var data = {
            content: undefined,
            datauri: undefined,
            blob: undefined
        };
        data.content = context.svg;
        callback(data);
    };

    var renderPngJpeg = function(highChartsObject, context, callback) {
        var data = {
            content: undefined,
            datauri: undefined,
            blob: undefined
        };

        svgToCanvas(context.svg, context.destWidth, context.destHeight, function(canvas) {
            data.datauri = context.browserSupportDownload && canvas.toDataURL && canvas.toDataURL(context.type);
            data.blob = (context.type == MIME_TYPES.PNG) && !context.browserSupportDownload && canvas.msToBlob && canvas.msToBlob();

            callback(data);
        });
    };

    var download = function(highChartsObject, context, data) {
        if (!data || (!data.content && !(data.datauri || data.blob))) {
            throw new Error("Something went wrong while exporting the chart");
        }

        if (context.browserSupportDownload && (data.datauri || data.content)) {
            a = document.createElement('a');
            a.href = data.datauri || ('data:' + context.type + ';base64,' + window.btoa(unescape(encodeURIComponent(data.content))));
            a.download = context.filename;
            document.body.appendChild(a);
            a.click();
            a.remove();
        }
        else if (context.browserSupportBlob && (data.blob || data.content)) {
            blobObject = data.blob || new Blob([data.content], { type: context.type });
            window.navigator.msSaveOrOpenBlob(blobObject, context.filename);
        }
        else {
            window.open(data);
        }
    };

    /**
    * Redefines the export function of the official exporting module.
    * @param options {Object} Overload the export options defined in the chart.
    * @param chartOptions {Object} Additionnal chart options.
    */
    H.Chart.prototype.exportChartLocal = function(options, chartOptions) {
        var opt = new Opt(options, false, defaultExportOptions);

        var type = opt.get("type");
        if (!H.exporting.supports(type)) {
            throw new Error("Unsupported export format on this platform: " + type);
        }

        var steps = {
            rendering: {},
            download: download
        };

        steps.rendering[MIME_TYPES.PNG] = {
            preRender: preRenderImage,
            render: renderPngJpeg
        };

        var highChartsObject = this;

        var context;
        if(steps.rendering[type].preRender) {
            context = steps.rendering[type].preRender(highChartsObject, options, chartOptions);
        } else {
            context = {};
        }

        context.type = type;
        context.browserSupportDownload = browserSupportDownload;
        context.browserSupportBlob     = browserSupportBlob;

        var filename = opt.get("filename");
        if(typeof filename === "function") {
            context.filename = filename.bind(this)(options, chartOptions) + MIME_TYPE_TO_EXTENSION[type];
        } else {
            context.filename = opt.get("filename") + MIME_TYPE_TO_EXTENSION[type];
        }

        steps.rendering[type].render(highChartsObject, context, function(data) {
            if(steps.rendering[type].postRender) {
                steps.rendering[type].postRender(highChartsObject, context);
            }

            steps.download(highChartsObject, context, data);
        });
    }

    // Forces method from export module to use the local version
    H.Chart.prototype.exportChart = H.Chart.prototype.exportChartLocal;
} (Highcharts));