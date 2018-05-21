var webpack = require('webpack');
var path    = require('path');
var entries = require('./entries');
// var ExtractTextPlugin = require("extract-text-webpack-plugin");
var HtmlWebpackPlugin = require('html-webpack-plugin');
var WebpackMd5Hash = require('webpack-md5-hash');
var FriendlyErrorsPlugin = require('friendly-errors-webpack-plugin');
module.exports = {
	entry: entries.config,
	output: {
		path: path.resolve(__dirname, './src/main/webapp/scripts/components'),
		publicPath: '/scripts/components/',
		chunkFilename: '[name]/[name].js?v=[chunkhash]',
		filename: '[name]/[name].js',
	},
	module: {
		rules: [
			// {
			// 	enforce: 'pre',
			// 	test: /.vue$/,
			// 	loader: 'eslint-loader',
			// 	exclude: /node_modules/,
			// 	options: {
			// 		emitWarning: true,
			// 		formatter: require('eslint-friendly-formatter')
			// 	}
			// },
			{
				test: /\.vue$/,
				loader: "vue-loader",
				options: {
					// loaders: {
					// 	css: ExtractTextPlugin.extract({
					// 			use: ['css-loader','postcss-loader'],
					// 			fallback:"vue-style-loader"
					// 	}),
					// 	stylus: ExtractTextPlugin.extract({
					// 			use: ["css-loader", "stylus-loader"],
					// 			fallback:"vue-style-loader"
					// 	}),
					// 	less: ExtractTextPlugin.extract({
					// 			use: ["css-loader", "less-loader"],
					// 			fallback:"vue-style-loader"
					// 	}),
					// },
					postcss: [
						require('autoprefixer')({
							browsers: ['last 20 versions']
						})
					]
				}
			},
            {
                test: /\.less$/,
                use: ["vue-style-loader", "css-loader", "less-loader"]
            },
			{
				test: /\.js$/,
				loader: 'babel-loader',
				exclude: /node_modules/
			},
			{
				test: /\.(png|jpg|gif|svg)$/,
				loader: 'file-loader',
				options: {
					name: '[name].[ext]?[hash]'
				}
			}
		]
	},
	resolve: {
		extensions:[".js",".vue"],
		alias: {
			'vue$': 'vue/dist/vue.common.js'
		}
	},
	devServer: {
		historyApiFallback: true,
		noInfo: true,
		proxy:{
			'/api/*':{
				// target: "http://10.12.10.10:9280",
				target: "http://10.12.0.30",
				secure: false
			}
		}
	},
	devtool: '#eval-source-map',
}

if (process.env.NODE_ENV === 'production') {
	module.exports.devtool = '#source-map'
	// http://vue-loader.vuejs.org/en/workflow/production.html
	module.exports.plugins = (module.exports.plugins || []).concat([
		new webpack.DefinePlugin({
			'process.env': {
				NODE_ENV: '"production"'
			}
		}),
		new webpack.optimize.UglifyJsPlugin({
			sourceMap: false,
			compress: {
				warnings: false
			}
		}),
		new webpack.LoaderOptionsPlugin({
			minimize: true,
			sourceMap: false
		}),
		new WebpackMd5Hash(),
		new HtmlWebpackPlugin({
			template: './src/main/webapp/index_tmp.html', 
			hash: true,
			filename: path.resolve(__dirname, './src/main/webapp/index.html'),
			minify: {
				removeComments: true,
				collapseWhitespace: false
			}
		}),
		// new ExtractTextPlugin("styles.css"),
	])
}
// else{
	// module.exports.plugins = (module.exports.plugins || []).concat([
		// new webpack.NoEmitOnErrorsPlugin(),
		// new FriendlyErrorsPlugin()
	// ])
// }