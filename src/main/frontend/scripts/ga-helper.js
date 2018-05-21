(function($, Ga){
	this.GaHelper = {
		Dimension: {
			NAVIGATION: 2,
			TOOLBAR: 3,
			DIMENSION3: 4,
			DIMENSION4: 5,
			DIMENSION5: 6,
			DIMENSION6: 7,
		},
		curDimIndex: 2,
		curMetricIndex: 0,
		// event defind
		Usercenter:{
			category: 'usercenter',
			//action
			read: 'usercenter-read',
			go:'daily-to-dashboard',
			request: 'usercenter-requestDemo',
			buyDashboard: 'usercenter-purchase'
		},
		Dashboard: {
			category: 'dashboard',
			// action
			fullwatch: 'dashboard-fullscreen',
			changeLanguage: 'dashboard-language',
			upgrade: 'dashboard-upgrade',
			renewal: 'dashboard-renewal'
		},
		FullScreen: {
			category: 'fullscreen',
			// action
			export: 'fullscreen-export',
			viewData: 'fullscreen-viewData',
			viewChart: 'fullscreen-viewChart'
		},
		addDimension(index, value){
			if (typeof ga == 'undefined') {return false;}
			//清空后面的维度信息
			// if (index < this.curDimIndex) {
			// 	for (var i = index + 1; i <= this.curDimIndex; i++) {
			// 		let key = 'dimension' + i;
			// 		ga('set', key, '');
			// 	}
			// }

			this.curDimIndex = index;
			let dimension = 'dimension'+index;
			ga('set', dimension, value);
			this.sendPageView();
		},
		addMetric(index, value){

		},
		sendEvent(action, labels){
			if (typeof ga == 'undefined') {return false;}
			ga('send', 'event', sessionStorage.getItem('uid'), action, labels);
		},
		sendPageView(){
			if (typeof ga == 'undefined') {return false;}
			ga('send', 'pageview', {
				title: document.title,
				// location: location.pathname,
				page: location.hash
			});
		}
	}
}(jQuery, window.ga));