// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import VueResource from 'vue-resource'
import App from './App.vue'
import router from './router.js'
// import {login} from './router.js'

Vue.use(VueResource);


Vue.config.devtools = false;

function getCookie(name) {

    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
}

Vue.http.interceptors.push((request, next)  => {

    const csrfCookieValue = getCookie('XSRF-TOKEN');
    console.log('csrfCookieValue', csrfCookieValue);

    // https://docs.spring.io/spring-security/site/docs/current/reference/html/csrf.html#csrf-cookie
    request.headers.set('X-XSRF-TOKEN', csrfCookieValue);

    next((response) => {
        if(response.status === 401 ) {
            // logout();

            // не думал что такое будет работать ;)
            vm.$modal.show('demo-login');
        }
    });
});


/* eslint-disable no-new */
const vm = new Vue({
  el: '#app-container',
  router,
  template: '<App/>',
  components: { App }
});

