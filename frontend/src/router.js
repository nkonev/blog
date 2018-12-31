import Vue from 'vue'
import Router from 'vue-router'
import UserProfile from './components/UserProfile.vue'
import Error404 from './components/Error404.vue'
import Error401 from './components/Error401.vue'
import Error403 from './components/Error403.vue'
import UserList from './components/UserList.vue'
import Registration from './components/Registration.vue'
import PostList from './components/PostList.vue'
import createPostDto from './factories/PostDtoFactory'
import Confirm from './components/Confirm.vue'
import ConfirmTokenNotFound from './components/ConfirmTokenNotFound.vue'
import ConfirmUserNotFound from './components/ConfirmUserNotFound.vue'
import ResendRegistrationConfirmationToken from './components/ResendRegistrationConfirmationToken.vue'
import PasswordRestore from './components/PasswordRestore.vue'
import PasswordReset from './components/PasswordReset.vue'
import Help from './components/Help.vue'
import Settings from './components/Settings.vue'
import ApplicationList from './components/ApplicationList.vue'
import {root, root_name, users, useProfileName, post} from './routes'
const PostView = () => import('./components/PostView.vue');
const PostEdit = () => import('./components/PostEdit.vue');

// This installs <router-view> and <router-link>,
// and injects $router and $route to all router-enabled child components
// WARNING You shouldn't include it in tests, else avoriaz's globals won't works (https://github.com/eddyerburgh/avoriaz/issues/124)
Vue.use(Router);

const router = new Router({
    mode: 'history',
    // https://router.vuejs.org/en/api/options.html#routes
    routes: [
        { name: root_name, path: root, component: PostList},
        { name: useProfileName, path: '/user/:id?', component: UserProfile, props: true, },
        { path: users, component: UserList},
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
        { path: '/confirm', component: Confirm},
        { path: '/confirm/registration/token-not-found', component: ConfirmTokenNotFound},
        { path: '/confirm/registration/user-not-found', component: ConfirmUserNotFound},
        { path: '/registration-confirmation-resend', component: ResendRegistrationConfirmationToken},
        { path: '/help', component: Help},
        { path: '/password-restore', component: PasswordRestore },
        { path: '/password-reset', component: PasswordReset },
        { path: '/settings', component: Settings},
        { path: '/applications', component: ApplicationList},
        { path: '/unauthorized', component: Error401 },
        { path: '/forbidden', component: Error403 },
        { path: '*', component: Error404 },
    ]
});


export default router;