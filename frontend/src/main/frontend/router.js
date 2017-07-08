import Vue from 'vue'
import Router from 'vue-router'
import Hello from './components/Hello.vue'
import UserProfile from './components/UserProfile.vue'
import NotFoundComponent from './components/NotFoundComponent.vue'
import Helloween from './components/Helloween.vue'
import UserList from './components/UserList.vue'
import Autocomplete from './components/Autocomplete.vue'
import Registration from './components/Registration.vue'
import PostList from './components/PostList.vue'
import Lorem from './components/Lorem.vue'

// This installs <router-view> and <router-link>,
// and injects $router and $route to all router-enabled child components
Vue.use(Router);

const root = '/';
const users = '/users';

const router = new Router({
    mode: 'history',
    routes: [
        {
            path: root,
            name: 'Hello',
            component: Hello
        },
        { name: 'user-profile', path: '/user/:id', component: UserProfile, props: true },
        { path: users, component: UserList},
        { path: '/autocomplete', component: Autocomplete},
        { path: '/helloween', component: Helloween },
        { path: '/registration', component: Registration },
        { path: '/lorem', component: Lorem },
        { path: '/posts', component: PostList },
        { path: '*', component: NotFoundComponent },
    ]
});


export  {
    router as default,
    root,
    users
}