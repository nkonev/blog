import Vue from 'vue'
import Router from 'vue-router'
import UserProfile from './components/UserProfile.vue'
import NotFoundComponent from './components/NotFoundComponent.vue'
import UserList from './components/UserList.vue'
import Autocomplete from './components/Autocomplete.vue'
import Registration from './components/Registration.vue'
import PostList from './components/PostList.vue'
import PostView from './components/PostView.vue'
import createPostDto from './factories/PostDtoFactory'
const PostEdit = () => import('./components/PostEdit.vue');

// This installs <router-view> and <router-link>,
// and injects $router and $route to all router-enabled child components
Vue.use(Router);

const root = '/';
const root_name = 'root';
const users = '/users';
const useProfileName = 'user-profile';
const post = 'post';

const router = new Router({
    mode: 'history',
    routes: [
        { name: root_name, path: root, component: PostList},
        { name: useProfileName, path: '/user/:id?', component: UserProfile, props: true, },
        { path: users, component: UserList},
        { path: '/autocomplete', component: Autocomplete},
        { path: '/registration', component: Registration },
        { path: '/post/add', component: PostEdit, props: {
            postDTO: createPostDto(),
            onAfterSubmit: (savedOnServerPostDto)=>{
                router.push({ name: post, params: { id: savedOnServerPostDto.id }})
            },
            onCancel: () => {
                router.push({ name: root_name })
            }
        }
        },
        { name: post, path: '/post/:id', component: PostView},
        { path: '*', component: NotFoundComponent },
    ]
});


export  {
    router as default,
    root,
    users,
    useProfileName,
    post
}