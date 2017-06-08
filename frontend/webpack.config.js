'use strict';

const path = require('path');
const srcDir = path.join(__dirname, 'src/main/frontend');
const buildDir = path.join(__dirname, 'src/main/resources/static/build');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const DEVELOPMENT_ENV = 'dev';
const NODE_ENV = process.env.NODE_ENV || DEVELOPMENT_ENV;

const webpack = require('webpack');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const WebpackOnBuildPlugin = require('on-build-webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
    context: srcDir,

    entry: {
        vendor: ["jquery", "underscore"],
        autocomplete: "./pages/autocomplete/autocomplete.js",
        login: "./pages/login/login.js",
        helloween: "./pages/helloween/helloween.js",
    },

    output: {
        path: buildDir,
        filename: "[name].js",
        library: "[name]"
    },

    watch: NODE_ENV == DEVELOPMENT_ENV,

    watchOptions: {
        aggregateTimeout: 100
    },

    devtool: NODE_ENV == DEVELOPMENT_ENV ? "source-map" : false,

    plugins: [
        new webpack.NoEmitOnErrorsPlugin(),
        new webpack.DefinePlugin({
            NODE_ENV: JSON.stringify(NODE_ENV)
        }),
        new webpack.optimize.CommonsChunkPlugin({
            name: ['vendor']
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
        }),
        new webpack.LoaderOptionsPlugin({
            debug: NODE_ENV == DEVELOPMENT_ENV
        })

    ],

    resolve: {
        alias: {
            'jquery': require.resolve('jquery'), // for uniform.js
        }
    },

    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['es2015']
                    }
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
                            name: '[path][name].[ext]'
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
            }
        ]

    }
};

if (NODE_ENV !== DEVELOPMENT_ENV) {
    module.exports.plugins.push(
        new webpack.optimize.UglifyJsPlugin({
            compress: {
                // don't show unreachable variables etc
                warnings: false,
                drop_console: true,
                unsafe: true
            }
        })
    );
}
