'use strict';

const path = require('path');
const srcDir = path.join(__dirname, 'src/main/frontend');
const buildDir = path.join(__dirname, 'src/main/resources/static/build');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const DEVELOPMENT_ENV = 'development';
const NODE_ENV = process.env.NODE_ENV || DEVELOPMENT_ENV;

const webpack = require('webpack');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const WebpackOnBuildPlugin = require('on-build-webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const LiveReloadPlugin = require('webpack-livereload-plugin');

const webpackModule = require('./webpack/module');
const webpackResolve = require('./webpack/resolve');

module.exports = {
    cache: true,

    context: srcDir,

    entry: {
        vendor: ["vue"],
        highlight: ['highlight.js'],
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
        new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /en|ru/),
        new webpack.optimize.CommonsChunkPlugin({
            // names: ['highlight', 'vendor'] // (choose the chunks, or omit for all chunks)
            children: true,
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

    resolve: webpackResolve,

    // disable extract-test-plugin spam
    stats: {
        children: false
    },

    module: webpackModule(true)
};

if (NODE_ENV === DEVELOPMENT_ENV) {
    module.exports.plugins.push(
        new LiveReloadPlugin({
            appendScriptTag: true
        })
    );
}


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
