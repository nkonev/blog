import Vue from 'vue'
import Router from 'vue-router'
import Hello from '../components/Hello.vue'

// This installs <router-view> and <router-link>,
// and injects $router and $route to all router-enabled child components
Vue.use(Router);

export default new Router({
    //mode: 'history',
    routes: [
        {
            path: '/',
            name: 'Hello',
            component: Hello
        }
    ]
})
