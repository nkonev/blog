import Vue from 'vue'
import Vuex from 'vuex'
import {PROFILE_URL, API_CONFIG} from './constants'

Vue.use(Vuex);

export const GET_USER = 'getUser';
export const SET_USER = 'setUser';
export const UNSET_USER = 'unsetUser';
export const FETCH_USER_PROFILE = 'fetchUserProfile';

export const GET_CONFIG = 'getConfig';
export const SET_CONFIG = 'setConfig';
export const UNSET_CONFIG = 'unsetConfig';
export const FETCH_CONFIG = 'fetchConfig';

export const GET_HEADER = 'getHeader';
export const SET_HEADER = 'setHeader';

export const GET_SUBHEADER = 'getSubHeader';
export const SET_SUBHEADER = 'setSubHeader';

export const GET_TITLE_TEMPLATE = 'getTitleTemplate';
export const SET_TITLE_TEMPLATE = 'setTitleTemplate';

export const GET_IMAGE_BACKGROUND = "getImageBackground";
export const SET_IMAGE_BACKGROUND = "setImageBackground";

export const GET_CONFIG_LOADING = "getConfigLoading";
export const SET_CONFIG_LOADING = "setConfigLoading";

export const GET_ROLES = 'getRoles';

const store = new Vuex.Store({
    state: {
        currentUser: null,
        config: {
            header: null,
            subHeader: null,
            titleTemplate: null,
            imageBackground: null
        },
        configLoading: true,
        showSettings: false
    },
    mutations: {
        [SET_USER](state, payload) {
            state.currentUser = payload;
        },
        [UNSET_USER](state) {
            state.currentUser = null;
        },

        [SET_CONFIG](state, payload) {
            state.config = payload;
        },
        [UNSET_CONFIG](state) {
            state.config = {};
        },

        [SET_HEADER](state, payload) {
            state.config.header = payload;
        },
        [SET_SUBHEADER](state, payload) {
            state.config.subHeader = payload;
        },

        [SET_TITLE_TEMPLATE](state, payload) {
            state.config.titleTemplate = payload;
        },
        [SET_CONFIG_LOADING](state, payload) {
            state.configLoading = payload;
        },
        [SET_IMAGE_BACKGROUND](state, payload) {
            state.config.imageBackground = payload;
        }
    },
    getters: {
        [GET_USER](state) {
            return state.currentUser;
        },

        [GET_CONFIG](state) {
            return state.config;
        },

        [GET_HEADER](state) {
            return state.config.header;
        },
        [GET_SUBHEADER](state) {
            return state.config.subHeader;
        },
        [GET_TITLE_TEMPLATE](state) {
            return state.config.titleTemplate;
        },
        [GET_IMAGE_BACKGROUND](state) {
            return state.config.imageBackground;
        },
        [GET_CONFIG_LOADING](state) {
            return state.configLoading;
        },
        [GET_ROLES](state) {
            return state.config.availableRoles;
        },
    },
    actions: {
        [FETCH_USER_PROFILE](context) {
            Vue.http.get(PROFILE_URL).then(response => {
                context.commit(SET_USER, response.body);
            }, response => {
                // error callback
                // console.error("Can\'t get user profile!", response);
            });
        },

        [FETCH_CONFIG](context) {
            context.commit(SET_CONFIG_LOADING, true);
            Vue.http.get(API_CONFIG).then(response => {
                context.commit(SET_CONFIG, response.body);
                context.commit(SET_CONFIG_LOADING, false);
            }, response => {
                // error callback
                context.commit(SET_CONFIG_LOADING, false);
                console.error("Can\'t get config", response);
            });
        }
    }
});

export default store;