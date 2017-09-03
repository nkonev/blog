import Vue from 'vue'
import Router from 'vue-router'
import UserProfile from './components/UserProfile.vue'
import NotFoundComponent from './components/NotFoundComponent.vue'
import UserList from './components/UserList.vue'
import Registration from './components/Registration.vue'
import PostList from './components/PostList.vue'
import PostView from './components/PostView.vue'
import createPostDto from './factories/PostDtoFactory'
import Confirm from './components/Confirm.vue'
import ConfirmTokenNotFound from './components/ConfirmTokenNotFound.vue'
import ConfirmUserNotFound from './components/ConfirmUserNotFound.vue'
import ResendRegistrationConfirmationToken from './components/ResendRegistrationConfirmationToken.vue'
import RestorePassword from './components/RestorePassword.vue'
import PasswordReset from './components/PasswordReset.vue'
import Help from './components/Help.vue'
import {root, root_name, users, useProfileName, post} from './routes'
const PostEdit = () => import('./components/PostEdit.vue');

// This installs <router-view> and <router-link>,
// and injects $router and $route to all router-enabled child components
// WARNING You shouldn't include it in tests, else avoriaz's glogals won't works (https://github.com/eddyerburgh/avoriaz/issues/124)
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
        { path: '/restore-password', component: RestorePassword },
        { path: '/password-reset', component: PasswordReset },
        { path: '*', component: NotFoundComponent },
    ]
});


export default router;