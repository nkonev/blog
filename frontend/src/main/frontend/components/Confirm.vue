<template>
    <span class="successfully-confirmed">
        You successfully confirmed, now you can <a href="#" @click.prevent="openLogin">login</a> as {{login}}
    </span>
</template>

<script>
    import {LOGIN_MODAL} from '../constants'
    import {root_name} from '../routes'
    import bus, {LOGIN} from '../bus'

    export default {
        methods: {
            openLogin() {
                this.$modal.show(LOGIN_MODAL);
            },
            onSuccessLogin() {
                this.$router.push({ name: root_name });
            }
        },
        computed: {
            login(){
                return this.$route.query.login;
            }
        },
        created() {
            bus.$on(LOGIN, this.onSuccessLogin);
        },
        destroyed() {
            bus.$off(LOGIN, this.onSuccessLogin);
        }
    }
</script>