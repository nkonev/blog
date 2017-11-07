// Karma configuration
// Generated on Mon Dec 12 2016 23:49:18 GMT+0300 (MSK)
const path = require('path');
const webpack = require('webpack');
const webpackModule = require('./webpack/module');
const webpackResolve = require('./webpack/resolve');

const DEVELOPMENT_ENV = 'development';
const KARMA_ENV = 'karma';
const PRODUCTION_ENV = 'production';
const NODE_ENV = process.env.NODE_ENV || DEVELOPMENT_ENV;


let browsers;
if (process.env.KARMA_BROWSERS) {
    browsers = process.env.KARMA_BROWSERS.split(",")
} else {
    browsers = (NODE_ENV===PRODUCTION_ENV) ? ['PhantomJS'] : ['PhantomJS', 'Firefox', 'Chrome'];
}
console.log("browsers (might be overwritten by --browsers option):", browsers);


module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '.',

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine'],

    // list of files / patterns to load in the browser
    files: [
      { pattern: 'node_modules/babel-polyfill/dist/polyfill.js', watched: false, included: true, served: true}, // for old WebKit in PhantomJS
      "test/index.js", // 1/3
      'test/specs/**/*spec.js' // Test specifications
    ],

    // list of files to exclude
    exclude: [],

    proxies : {
        '/static/': '/base/../backend/src/main/resources/static/'
    },

    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      "test/index.js": ["webpack", 'sourcemap'], // 2/3
      'test/specs/**/*spec.js': ['webpack', 'sourcemap']
    },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    // 'mocha' this is reporter, not framework
    reporters: ['spec'], // two reporters may duplicate output (https://github.com/karma-runner/karma/issues/2342)

    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: browsers,
    // browsers: ['PhantomJS_debug'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: NODE_ENV!=DEVELOPMENT_ENV,

    // Concurrency level
    // how many browser should be started simultaneous
    concurrency: Infinity,

    client: {
      clearContext: true, // clear test page context.html
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
      },
      // 'ChromeHeadless' already defined in driver
      'FirefoxHeadless': {
          base: 'Firefox',
          flags: ['-headless'],
      },
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
                  KARMA_ENV:  JSON.stringify(KARMA_ENV),  // определяем окружение для того чтобы менять поведение компонентов. когда невозмножно сделать jasmine spy
                  NODE_ENV:  JSON.stringify(PRODUCTION_ENV) // must be 'production' (with single quotes) for disable Vue warnings, which you can see it if drop_console: false
              }
          }),
      ],
      resolve: webpackResolve,
      module: webpackModule(false),
    }
  })
};