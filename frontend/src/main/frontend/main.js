// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import VueResource from 'vue-resource'
import App from './App.vue'
import router from './router.js'

Vue.use(VueResource);

Vue.config.devtools = false;

Vue.http.interceptors.push((request, next)  => {
    next((response) => {
        if(response.status == 401 ) {
            // logout();
            router.push('/login?unauthorized=1');
        }
    });
});

/* eslint-disable no-new */
new Vue({
  el: '#app-container',
  router,
  template: '<App/>',
  components: { App }
});
