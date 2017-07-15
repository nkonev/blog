<template>
    <div class="profile">
        <div v-if="me">
            <p>Это вы</p>
        </div>
        <div>Пользователь {{ id }}</div>
    </div>
</template>

<script>
    import {GET_USER} from '../store'
    import store from  '../store'
    import bus from '../bus'
    import {LOGIN, LOGOUT} from '../bus'

    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['id'],
        data() {
            return {

            }
        },
        computed: {
            me() {
                const storeUser = store.getters[GET_USER];
                if (this.$props && storeUser) {
                    return storeUser ? storeUser.id == this.$props.id : false;
                } else {
                    return false;
                }
            },
        },
        /*methods: {
            _me() {
                const storeUser = store.getters[GET_USER];
                if (this.$props && storeUser) {
                    return storeUser ? storeUser.id == this.$props.id : false;
                } else {
                    return false;
                }
            },
            fetchUserData() {
                const self = this;
                this.$http.get('/api/hello').then(response => {
                    self.userData = response.body;
                }, response => {
                    // error callback
                });
            },
            onLogin(ignore) {
                /* computed property me not produce update dom when store changed so if won't works *
                if (this._me()) {
                    this.fetchUserData();
                }
            },
            onLogout(ignore) {
                if (this._me()) {
                    this.userData = NO_DATA;
                }
            },
        },
        mounted(){
            if (this._me()) {
                this.fetchUserData();
             }

            bus.$on(LOGIN, this.onLogin);
            bus.$on(LOGOUT, this.onLogout);
        },
        beforeDestroy() {
            //console.log("destroyed");
            bus.$off(LOGIN, this.onLogin);
            bus.$off(LOGOUT, this.onLogout);
        }*/
    };
</script>
