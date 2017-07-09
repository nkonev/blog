<template>
    <div class="user-profile-nav">
        <template v-if="currentUser">
            <div class="user-profile-nav-login">
                <multiselect :placeholder="currentUser.login" :show-labels="false" :options="actions" :searchable="false" :reset-after="true" @select="dispatchAction"></multiselect>
            </div>
            <img :src="currentUser.avatar"/>
        </template>
        <div class="user-profile-nav-not-registegred" v-else>
            <a href="#" @click.prevent="login">login</a>
        </div>

    </div>
</template>

<script>
    import Multiselect from 'vue-multiselect'
    import store from '../store'
    import {UNSET_USER} from '../store'
    import "vue-multiselect/dist/vue-multiselect.min.css"
    import {LOGIN_MODAL} from '../constants'

    export default {
        name: 'user-profile-nav',
        props: ['currentUser'],
        mounted() {
            console.debug('user-profile-nav loaded', this.$props);
        },
        data () {
            return {
                actions: ['exit'],
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
                            },
                            response => {
                                // fail
                                console.error("Can't to logout");
                            }
                        );
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
</style>