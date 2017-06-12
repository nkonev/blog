import Vue from 'vue'
import Router from 'vue-router'
import Hello from './components/Hello.vue'
import User from './components/User.vue'
import NotFoundComponent from './components/NotFoundComponent.vue'
import Login from './components/Login.vue'

// This installs <router-view> and <router-link>,
// and injects $router and $route to all router-enabled child components
Vue.use(Router);

export default new Router({
    // mode: 'history',
    routes: [
        {
            path: '/',
            name: 'Hello',
            component: Hello
        },
        { path: '/user/:id', component: User, props: true },
        { path: '/login', component: Login },
        { path: '*', component: NotFoundComponent },
    ]
})
