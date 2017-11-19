'use strict';

const path = require('path');
const srcDir = path.join(__dirname, 'src');
const buildDir = path.join(__dirname, '../backend/src/main/resources/static/build');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const DEVELOPMENT_ENV = 'development';
const NODE_ENV = process.env.NODE_ENV || DEVELOPMENT_ENV;

const webpack = require('webpack');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const WebpackOnBuildPlugin = require('on-build-webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const LiveReloadPlugin = require('webpack-livereload-plugin');


module.exports = {
    cache: true,

    context: srcDir,

    entry: {
        vendor: ["vue", 'highlight.js'],
        main: "./main.js", // vue.js
    },

    output: {
        path: buildDir,
        publicPath: "/build/", // url prefix
        filename: "[name].js",
        library: "[name]"
    },

    watch: NODE_ENV == DEVELOPMENT_ENV,
    watchOptions: {
        aggregateTimeout: 100,
        poll: 250
    },

    devtool: NODE_ENV == DEVELOPMENT_ENV ? "source-map" : false,

    plugins: [
        new webpack.NoEmitOnErrorsPlugin(),
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV:  JSON.stringify(NODE_ENV) // must be 'production' (with single quotes) for disable Vue warnings, which you can see it if drop_console: false
            }
        }),
        new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /^$/),
        new webpack.optimize.CommonsChunkPlugin({
            names: ['vendor'], // (choose the chunks, or omit for all chunks) https://webpack.js.org/plugins/commons-chunk-plugin/#move-common-modules-into-the-parent-chunk
            // children: true,
        }),
        new CleanWebpackPlugin([buildDir], {
            verbose: false,
            dry: false
        }),
        new CopyWebpackPlugin([
                { from: path.join(__dirname, 'node_modules/babel-polyfill/dist/polyfill.min.js') },
        ]),
        new WebpackOnBuildPlugin(function (stats) {
            var currentdate = new Date();
            console.log("Built with environment:", NODE_ENV, "at", currentdate.toLocaleString());
        }),
        new ExtractTextPlugin({
            filename: '[name].css'
        }),
        new webpack.ProvidePlugin({
            // "$":"jquery",
            // "jQuery":"jquery",
            // "window.jQuery":"jquery"
            "window.hljs": "highlight.js"
        }),
        new webpack.LoaderOptionsPlugin({
            debug: NODE_ENV == DEVELOPMENT_ENV
        }),
    ],

    resolve: {
        alias: {
            // 'jquery': require.resolve('jquery'), // for uniform.js
            // 'vue': path.resolve(path.join(__dirname, 'node_modules', 'vue/dist/vue.js')), // fix "Vue is not constructor" in vue-online
            'vue$': path.resolve(path.join(__dirname, 'node_modules', 'vue/dist/vue.esm.js')), // it's important, else you will get "You are using the runtime-only build of Vue where the template compiler is not available. Either pre-compile the templates into render functions, or use the compiler-included build."
            //'@': path.resolve(__dirname, 'src')
        }
    },

    // disable extract-test-plugin spam
    stats: {
        children: false
    },

    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                }
            },
            {
                test: /\.css$/,
                use: ExtractTextPlugin.extract({
                    fallback: "style-loader",
                    use: "css-loader?sourceMap"
                })
            },
            {
                test: /\.styl$/,
                use: ExtractTextPlugin.extract({
                    fallback: "style-loader",
                    use: ["css-loader?sourceMap", 'stylus-loader']
                })
            },
            {
                test: /\.(ttf|eot|woff|woff2)$/,
                // for fix MemoryFs error with Karma -- remove limit
                use: [
                    {
                        loader: 'url-loader',
                        options: {
                            name: '[path][name].[ext]',
                            limit: '4096'
                        }
                    }
                ],
            },
            {
                test: /\.(png|jpg|svg|gif)$/,
                use: [
                    {
                        loader: 'file-loader',
                        options: {
                            name: '[path][name].[ext]',
                            // publicPath: '/static/build/',
                        }
                    }
                ]
            },
            {
                test: /\.html$/,
                use: [{
                    loader: 'html-loader',
                    options: {
                        minimize: true
                    }
                }],
            },
            {
                test: /\.vue$/,
                loader: 'vue-loader',
                options: {
                    extractCSS: true
                }
            }
        ]

    }
};

if (NODE_ENV === DEVELOPMENT_ENV) {
    module.exports.plugins.push(
        new LiveReloadPlugin({
            appendScriptTag: true
        })
    );
}
