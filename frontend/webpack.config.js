'use strict';

const path = require('path');
const srcDir = path.join(__dirname, 'src');
const buildDir = path.join(__dirname, '../backend/src/main/resources/static/build');

const DEVELOPMENT_ENV = 'development';
const NODE_ENV = process.env.NODE_ENV || DEVELOPMENT_ENV;
const publicPath = "/build/";

const webpack = require('webpack');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const WebpackOnBuildPlugin = require('on-build-webpack');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const LiveReloadPlugin = require('webpack-livereload-plugin');
const { VueLoaderPlugin } = require('vue-loader')

module.exports = {
    cache: true,

    context: srcDir,

    entry: {
        main: "./main.js", // vue.js
    },

    output: {
        path: buildDir,
        publicPath: publicPath, // url prefix
        filename: "[name].js",
    },

    optimization: {
        splitChunks: {
            chunks: 'async',
            minSize: 30000,
            minChunks: 1,
            maxAsyncRequests: 5,
            maxInitialRequests: 3,
            automaticNameDelimiter: '~',
            name: true,
            cacheGroups: {
                default: false,
                vendors: {
                    test: /[\\/]node_modules[\\/]/,
                    name: "vendor",
                    priority: 1,
                    chunks: "all",
                    reuseExistingChunk: true
                }
            }
        }
    },

    watch: NODE_ENV == DEVELOPMENT_ENV,
    watchOptions: {
        aggregateTimeout: 100,
        poll: 250
    },

    devtool: NODE_ENV == DEVELOPMENT_ENV ? "source-map" : false,

    plugins: [
        new VueLoaderPlugin(),
        new webpack.NoEmitOnErrorsPlugin(),
        new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /^$/),
        new CleanWebpackPlugin([buildDir], {
            verbose: false,
            dry: false
        }),
        new WebpackOnBuildPlugin(function (stats) {
            var currentdate = new Date();
            console.log("Built with environment:", NODE_ENV, "at", currentdate.toLocaleString());
        }),
        new MiniCssExtractPlugin({
            filename: '[name].css',
            chunkFilename: "[id].css"
        }),
        new webpack.ProvidePlugin({
            // "$":"jquery",
            // "jQuery":"jquery",
            // "window.jQuery":"jquery"
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
                //use: ["style-loader", "css-loader?sourceMap"]
                use: [
                    MiniCssExtractPlugin.loader,
                    "css-loader"
                ]
            },
            {
                test: /\.styl|stylus$/,
                use: ["style-loader", "css-loader?sourceMap", 'stylus-loader']
                /*use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    'stylus-loader'
                ]*/
            },
            {
                test: /\.(ttf|eot|woff|woff2)$/,
                // for fix MemoryFs error with Karma -- remove limit
                use: [
                    {
                        loader: 'url-loader',
                        options: {
                            name: '[path][name].[ext]',
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
                        }
                    }
                ]
            },
            {
                test: /\.vue$/,
                loader: 'vue-loader',
                // options: {
                //     extractCSS: true
                // }
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
