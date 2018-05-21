import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

import introduction from './introduction/'
import order from './order/'
import common from './common/'
import dashboard from './dashboard/'
import fullscreen from './fullscreen/'
import backend from './backend/'
import login from './login/'
import my from './my/'

export default new Vuex.Store({
	modules:{
		introduction,
		order,
		common,
		dashboard,
		fullscreen,
		backend,
		login,
		my
	}
})