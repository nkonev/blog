<template>
    <div v-if="currentUser" class="user-profile-nav">
        <div class="user-profile-nav-login">
            <multiselect :placeholder="currentUser.login" :show-labels="false" :options="actions" :searchable="false" :reset-after="true" @select="dispatchAction"></multiselect>
        </div>
        <img :src="currentUser.avatar"/>
    </div>
    <span class="user-profile-nav-anonymous" v-else>
        <a class="router-link" href="#" @click.prevent="login">login</a>
        <span class="router-link">or</span>
        <router-link class="router-link" to="/registration">Registration</router-link>
    </span>
</template>

<script>
    import Multiselect from 'vue-multiselect'
    import store from '../store'
    import {UNSET_USER} from '../store'
    import "vue-multiselect/dist/vue-multiselect.min.css"
    import {LOGIN_MODAL} from '../constants'
    import bus from '../bus'
    import {LOGOUT} from '../bus'

    import router from  '../router'
    import {useProfileName} from  '../router'

    export default {
        name: 'user-profile-nav',
        props: ['currentUser'],
        mounted() {
            // console.debug('user-profile-nav loaded', this.$props);
        },
        data () {
            return {
                actions: ['exit', 'profile'],
            }
        },
        components: {
            Multiselect
        },
        methods: {
            dispatchAction (actionName) {
                switch (actionName) {
                    case 'exit':
                        this.$http.post('/api/logout').then(
                            response => {
                                // ok
                                store.commit(UNSET_USER);
                                bus.$emit(LOGOUT, null);
                            },
                            response => {
                                // fail
                                console.error("Can't to logout");
                            }
                        );
                        break;
                    case 'profile':
                        router.push({name: useProfileName, params: {id: this.$props.currentUser.id}});
                        break;
                }
            },
            login() {
                this.$modal.show(LOGIN_MODAL);
            }
        },
    }
</script>

<style lang="stylus" scoped>
    .user-profile-nav{

        display flex
        flex-direction row
        justify-content flex-end

        &-login,&-not-registegred {
            display flex
            flex-direction column
            justify-content center
        }
        img {
            height 48px;
            width 48px;
        }
    }

    .user-profile-nav-anonymous {
        display flex
        flex-direction row
        justify-content flex-end
    }
</style>