import Vue from 'vue'
import Vuex from 'vuex'
import {GET_PROFILE_URL, PAGE_SIZE, PAGE} from './constants'
import {users} from './router'
import router from './router'

Vue.use(Vuex);

export const GET_USER = 'getUser';
export const SET_USER = 'setUser';
export const UNSET_USER = 'unsetUser';
export const FETCH_USER_PROFILE = 'fetchUserProfile';
export const GET_ADMIN_USERS = 'getAdminUsers';
export const SET_ADMIN_USERS = 'setAdminUsers';
export const GET_ADMIN_USERS_PAGE_COUNT = 'getAdminUsersPageCount';
export const SET_ADMIN_USERS_PAGE_COUNT = 'setAdminUsersPageCount';
export const GET_ADMIN_USERS_INITIAL_PAGE_INDEX = 'getAdminUsersPage'; // текущая траница страницы с пользователями
export const FETCH_ADMIN_USERS = 'fetchAdminUsers';
export const SETUP_AFTER_LOGIN = 'setupAfterLogin'; // инициализаиия после логина
export const TEARDOWN_AFTER_LOGOUT = 'tearDownAfterLogout';

const getRoute = () => router.app.$route;


const store = new Vuex.Store({
    state: {
        count: 0, // REMOVE this dummy var

        currentUser: null,

        // adminUsersPage: 1,
        adminUsers: [],
        adminUsersPageCount: 0,
    },
    mutations: {
        increment (state) {
            state.count++
        },
        decrement (state) {
            state.count--
        },

        // залогиненный пользователь, отображается логин и аватарка вверху в навбаре
        [SET_USER](state, payload) {
            state.currentUser = {login: payload.login, avatar: payload.avatar}
        },
        [UNSET_USER](state) {
            state.currentUser = null;
        },

        // пользователи админки
        [SET_ADMIN_USERS](state, payload) {
            state.adminUsers = payload;
        },
        [SET_ADMIN_USERS_PAGE_COUNT](state, payload) {
            state.adminUsersPageCount = payload;
        },

    },
    getters: {
        [GET_USER](state) {
            return state.currentUser;
        },

        [GET_ADMIN_USERS](state) {
            return state.adminUsers;
        },

        [GET_ADMIN_USERS_PAGE_COUNT](state) {
            return state.adminUsersPageCount;
        },

        /**
         * returns initial page index for admin users page
         * @param state
         * @return {*}
         */
        [GET_ADMIN_USERS_INITIAL_PAGE_INDEX](state) {
            return getRoute().query[PAGE] ? parseInt(getRoute().query[PAGE]-1) : 0;
        }
    },

    // only it may contain async operations
    actions: {
        [SETUP_AFTER_LOGIN](context) {
            console.log('After login setup');
            context.dispatch(FETCH_USER_PROFILE);

            if ((getRoute().path)===(users)){
                const page = context.getters[GET_ADMIN_USERS_INITIAL_PAGE_INDEX]+1;
                context.dispatch(FETCH_ADMIN_USERS, page);
            }
        },
        [TEARDOWN_AFTER_LOGOUT](context) {
            context.commit(UNSET_USER);
            context.commit(SET_ADMIN_USERS_PAGE_COUNT, 0);
            context.commit(SET_ADMIN_USERS, []);
        },
        [FETCH_USER_PROFILE](context) {
            Vue.http.get(GET_PROFILE_URL).then(response => {
                const userProfile = response.body;
                console.info('User Profile:', userProfile);
                context.commit(SET_USER, {login: userProfile.login, avatar: userProfile.avatar});
            }, response => {
                // error callback
                console.error("Can\'t get user profile!", response);
            });
        },
        
        [FETCH_ADMIN_USERS](context, pageNum) {
            console.log("will fetching adminUsers for page", pageNum);

            // get page count
            Vue.http.get('/api/user-count').then(response => {
                const userCount = response.body;
                const adminUsersPageCount = Math.ceil(userCount / PAGE_SIZE);
                context.commit(SET_ADMIN_USERS_PAGE_COUNT, adminUsersPageCount);
            }, response => {
                console.error(response);
                // alert(response);
            });


            // API request
            Vue.http.get('/api/user?page='+(pageNum-1)+'&size='+PAGE_SIZE).then(response => {
                context.commit(SET_ADMIN_USERS, response.body);
            }, response => {
                console.error(response);
                // alert(response);
            });

        },

    }
});

export default store;