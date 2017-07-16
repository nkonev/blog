import Vue from 'vue'
import Router from 'vue-router'
import Hello from './components/Hello.vue'
import UserProfile from './components/UserProfile.vue'
import NotFoundComponent from './components/NotFoundComponent.vue'
import UserList from './components/UserList.vue'
// import Autocomplete from './components/Autocomplete.vue'
import Registration from './components/Registration.vue'
import PostList from './components/PostList.vue'
import Lorem from './components/Lorem.vue'
// Lazy load heavy component https://router.vuejs.org/en/advanced/lazy-loading.html. see also in .babelrc
const PostEdit = () => import('./components/PostEdit.vue');

// This installs <router-view> and <router-link>,
// and injects $router and $route to all router-enabled child components
Vue.use(Router);

const root = '/';
const users = '/users';
const usersWithPage = users + '/:page?'; // '?' means not necessary as in RegExp
const useProfileName = 'user-profile';

const router = new Router({
    mode: 'history',
    routes: [
        {
            path: root,
            name: 'Hello',
            component: Hello
        },
        { name: useProfileName, path: '/user/:id?', component: UserProfile, props: true, },
        // { path: usersWithPage, component: UserList, name: "users", props: true },
        { path: users, component: UserList},
        //{ path: '/autocomplete', component: Autocomplete},
        { path: '/registration', component: Registration },
        { path: '/lorem', component: Lorem },
        { path: '/posts', component: PostList },
        { path: '/post/edit', component: PostEdit },
        { path: '*', component: NotFoundComponent },
    ]
});


export  {
    router as default,
    root,
    users,
    usersWithPage,
    useProfileName
}