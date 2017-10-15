import Vue from 'vue'
import Vuex from 'vuex'
import {GET_PROFILE_URL} from './constants'

Vue.use(Vuex);

export const GET_USER = 'getUser';
export const SET_USER = 'setUser';
export const UNSET_USER = 'unsetUser';
export const FETCH_USER_PROFILE = 'fetchUserProfile';

const store = new Vuex.Store({
    state: {
        count: 0,
        currentUser: null
    },
    mutations: {
        increment (state) {
            state.count++
        },
        decrement (state) {
            state.count--
        },

        [SET_USER](state, payload) {
            state.currentUser = payload;
        },
        [UNSET_USER](state) {
            state.currentUser = null;
        },
    },
    getters: {
        [GET_USER](state) {
            return state.currentUser;
        }
    },
    actions: {
        [FETCH_USER_PROFILE](context) {
            Vue.http.get(GET_PROFILE_URL).then(response => {
                const userProfile = response.body;
                // console.info('User Profile:', userProfile);
                context.commit(SET_USER, userProfile);
            }, response => {
                // error callback
                // console.error("Can\'t get user profile!", response);
            });
        }
    }
});

export default store;