const path = require('path');

module.exports = {
    alias: {
        // 'jquery': require.resolve('jquery'), // for uniform.js
        // 'vue': path.resolve(path.join(__dirname, 'node_modules', 'vue/dist/vue.js')), // fix "Vue is not constructor" in vue-online
        'vue$': path.resolve(path.join(__dirname, '..', 'node_modules', 'vue/dist/vue.esm.js')), // it's important, else you will get "You are using the runtime-only build of Vue where the template compiler is not available. Either pre-compile the templates into render functions, or use the compiler-included build."
    }
}