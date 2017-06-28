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
      // { pattern: 'src/test/frontend/fixtures/*.html', included: false}, // not in script tag
      // { pattern: 'src/test/frontend/responses/*.json', included: false}, // not in script tag
      // { pattern: 'src/main/resources/static/img/*.*', watched: false, included: false, served: true},

      { pattern: 'node_modules/babel-polyfill/dist/polyfill.js', watched: false, included: true, served: true}, // for old WebKit in PhantomJS

      "src/test/frontend/index.js", // 1/3

      'src/test/frontend/specs/**/*spec.js' // Test specifications
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
      'src/test/frontend/specs/**/*spec.js': ['webpack']
    },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    // 'mocha' в данном случае -- это просто репортер, не тестовый фреймворк
    reporters: ['mocha', 'html'], // two reporters may duplicate output (https://github.com/karma-runner/karma/issues/2342)

      // the default configuration
    htmlReporter: {
          outputDir: path.join(__dirname, 'target/karma_html'), // where to put the reports
          templatePath: null, // set if you moved jasmine_template.html
          focusOnFailures: true, // reports show failures on start
          namedFiles: false, // name files instead of creating sub-directories
          pageTitle: null, // page title for reports; browser info by default
          urlFriendlyName: false, // simply replaces spaces with _ for files/dirs
          reportName: 'report-summary-filename', // report summary filename; browser info by default


          // experimental
          preserveDescribeNesting: false, // folded suites stay folded
          foldAll: false, // reports start folded (only with preserveDescribeNesting)
    },

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
        // disable extract-test-plugin spam
      stats: {
            children: false
      },

      plugins: [
          new webpack.ProvidePlugin({
              // jQuery: "jquery", // for uniform.js
              $: 'jquery', // for usage in tests
          }),
          new webpack.DefinePlugin({
              'process.env': {
                  KARMA_ENV:  JSON.stringify(KARMA_ENV),  // определяем окружение для того чтобы менять поведение компонентов. когда невозмножно сделать jasmine spy
                  NODE_ENV:  JSON.stringify('production') // must be 'production' (with single quotes) for disable Vue warnings, which you can see it if drop_console: false
              }
          }),
      ],
      resolve: {
          alias: {
              // 'jquery': require.resolve('jquery'), // for uniform.js
              'vue': 'vue/dist/vue.js', // fix "Vue is not constructor" in vue-online
              'vue$': 'vue/dist/vue.esm.js', // it's important, else you will get "You are using the runtime-only build of Vue where the template compiler is not available. Either pre-compile the templates into render functions, or use the compiler-included build."
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
            {
                test: /\.vue$/,
                loader: 'vue-loader',
                options: {
                    extractCSS: false
                }
            },
        ],// end loaders
      }
    }
  })
};