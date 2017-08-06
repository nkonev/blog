const ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = function(extract){
    return {
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
                use: extract ?
                    ExtractTextPlugin.extract({
                        fallback: "style-loader",
                        use: "css-loader?sourceMap"
                    }) :
                    ['style-loader', 'css-loader?sourceMap']
            },
            {
                test: /\.styl$/,
                use: extract ?
                    ExtractTextPlugin.extract({
                        fallback: "style-loader",
                        use: ["css-loader?sourceMap", 'stylus-loader']
                    }) :
                    [
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
                        minimize: extract
                    }
                }],
            },
            {
                test: /\.vue$/,
                loader: 'vue-loader',
                options: {
                    extractCSS: extract
                }
            }
        ]

    }
}