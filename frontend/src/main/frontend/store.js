import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

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

        login(state, login, avatar) {
            state.currentUser = {login: login, avatar: avatar}
        },
        logout(state) {
            state.currentUser = null;
        }
    }
});

export default store;