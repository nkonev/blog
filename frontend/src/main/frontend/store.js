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

        setUser(state, payload) {
            state.currentUser = {login: payload.login, avatar: payload.avatar}
        },
        unsetUser(state) {
            state.currentUser = null;
        },
    },
    getters: {
        getUser(state) {
            return state.currentUser;
        }
    },
    actions: {
        fetchUserProfile(context) {
            Vue.http.get('/api/profile').then(response => {
                console.info('User Profile:', response.body);
                context.commit('setUser', {login: response.body.login, avatar: response.body.avatar});
            }, response => {
                // error callback
                console.error('Can\'t get user profile!', response);
            });
        }
    }
});

export default store;