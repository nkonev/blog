// Karma configuration
// Generated on Mon Dec 12 2016 23:49:18 GMT+0300 (MSK)
const path = require('path');
const webpack = require('webpack');
const DEVELOPMENT_ENV = 'dev';
const KARMA_ENV = 'karma';
const NODE_ENV = process.env.NODE_ENV || DEVELOPMENT_ENV;

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '.',

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine'],

    // list of files / patterns to load in the browser
    files: [
      { pattern: 'src/test/frontend/fixtures/*.html', included: false}, // not in script tag
      { pattern: 'src/test/frontend/responses/*.json', included: false}, // not in script tag

      { pattern: 'src/main/resources/static/img/*.*', watched: false, included: false, served: true},

      { pattern: 'node_modules/babel-polyfill/dist/polyfill.js', watched: false, included: true, served: true}, // for old WebKit in PhantomJS

      "src/test/frontend/index.js", // 1/3

      'src/test/frontend/pages/**/*spec.js' // Test specifications
    ],

    // list of files to exclude
    exclude: [],

    proxies : {
        '/static/': '/base/src/main/resources/static/'
    },

    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      "src/test/frontend/index.js": ["webpack"], // 2/3
      'src/test/frontend/pages/**/*spec.js': ['webpack']
    },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    // 'mocha' в данном случае -- это просто репортер, не тестовый фреймворк
    reporters: ['mocha', 'kjhtml'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    // logLevel: config.LOG_DEBUG,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: NODE_ENV==DEVELOPMENT_ENV ? ['Chrome'] : ['PhantomJS'],
    // browsers: ['PhantomJS_debug'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: NODE_ENV!=DEVELOPMENT_ENV,

    // Concurrency level
    // how many browser should be started simultaneous
    concurrency: Infinity,

    client: {
      useIframe: true
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
      cache: true,
      devtool: 'inline-source-map',
      // entry: {
      //       index: "./src/test/frontend/index.js" // 3/3
      // },
      plugins: [
          new webpack.ProvidePlugin({
              // jQuery: "jquery", // for uniform.js
              $: 'jquery', // for usage in tests
          }),
          new webpack.DefinePlugin({
              // определяем окружение
              // для того чтобы менять поведение компонентов. когда невозмножно сделать jasmine spy
              'process.env.NODE_ENV': JSON.stringify(KARMA_ENV)
          }),
      ],
      resolve: {
          alias: {
              // 'jquery': require.resolve('jquery'), // for uniform.js
          }
      },
      module: {
        loaders: [
          {
              test:   /\.js$/,
              exclude: /node_modules/,
              use: {
                  loader: 'babel-loader',
              }
          },
            {
                test: /\.css$/,
                use:[
                    'style-loader',
                    'css-loader?sourceMap'
                ]
            },

            {
              test: /\.styl$/,
                use:[
                    'style-loader',
                    'css-loader?sourceMap',
                    'stylus-loader'
                ]
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
                        minimize: false
                    }
                }],
            },
        ],// end loaders
      }
    }
  })
};