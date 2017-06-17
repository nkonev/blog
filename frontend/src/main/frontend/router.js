import Vue from 'vue'
import Router from 'vue-router'
import Hello from './components/Hello.vue'
import User from './components/User.vue'
import NotFoundComponent from './components/NotFoundComponent.vue'
import Login from './components/Login.vue'
import Helloween from './components/Helloween.vue'

// This installs <router-view> and <router-link>,
// and injects $router and $route to all router-enabled child components
Vue.use(Router);

const root = '/';
const login = '/login';


const router = new Router({
    mode: 'history',
    routes: [
        {
            path: root,
            name: 'Hello',
            component: Hello
        },
        { path: '/user/:id', component: User, props: true },
        { path: login, component: Login },
        { path: '/helloween', component: Helloween },
        { path: '*', component: NotFoundComponent },
    ]
});


export  {
    router as default,
    login,
    root
}