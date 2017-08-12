// Karma configuration
// Generated on Mon Dec 12 2016 23:49:18 GMT+0300 (MSK)
const path = require('path');
const webpack = require('webpack');
const webpackModule = require('./webpack/module');
const webpackResolve = require('./webpack/resolve');

const DEVELOPMENT_ENV = 'dev';
const KARMA_ENV = 'karma';
const NODE_ENV = process.env.NODE_ENV || DEVELOPMENT_ENV;

let ENV_BROWSERS = process.env.KARMA_BROWSERS;
ENV_BROWSERS = ENV_BROWSERS ? ENV_BROWSERS.split(",") : ['PhantomJS'];
const browsers = ENV_BROWSERS;
// console.log(browsers);

module.exports = function (config) {
    config.set({

        basePath: '.',
        browsers: ['PhantomJS'],
        frameworks: ['mocha', 'sinon-chai', 'phantomjs-shim'],
        reporters: ['spec', 'coverage'],
        files: ["./src/test/frontend/index.js"],

        proxies: {
            '/static/': '/base/src/main/resources/static/'
        },

        /*preprocessors: {
            "src/test/frontend/index.js": ["webpack", 'sourcemap'],
        },*/
        preprocessors: {
            "src/test/frontend/index.js": ["webpack", 'sourcemap'],
            '../../main/frontend': ['coverage'],
            'src/test/frontend/specs/**/*spec.js': ['webpack', 'sourcemap']
        },
        port: 9876,
        colors: true,
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,
        autoWatch: true,
        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: true,
        webpackMiddleware: {
            noInfo: true
        },
        coverageReporter: {
            dir: './coverage',
            reporters: [
                {type: 'lcov', subdir: '.'},
                {type: 'text-summary'}
            ]
        },
        client: {
            useIframe: true,
            captureConsole: true // append browser console to output or not
        },
        customLaunchers: {
            'PhantomJS_debug': {
                base: 'PhantomJS',
                options: {
                    windowName: 'my-window',
                    settings: {
                        webSecurityEnabled: false
                    },
                },
                flags: ['--remote-debugger-port=9000'],
                debug: true
            }
        },

        webpack: {
            cache: false,
            devtool: 'inline-source-map',
            // entry: {
            //       index: "./src/test/frontend/index.js" // 3/3
            // },
            // disable extract-test-plugin spam
            stats: {
                children: false
            },

            plugins: [
                new webpack.ProvidePlugin({
                    // jQuery: "jquery", // for uniform.js
                    // $: 'jquery', // for usage in tests
                }),
                new webpack.DefinePlugin({
                    'process.env': {
                        KARMA_ENV: JSON.stringify(KARMA_ENV),  // определяем окружение для того чтобы менять поведение компонентов. когда невозмножно сделать jasmine spy
                        NODE_ENV: JSON.stringify('production') // must be 'production' (with single quotes) for disable Vue warnings, which you can see it if drop_console: false
                    }
                }),
            ],
            resolve: webpackResolve,
            module: webpackModule(false),
        }
    })
};