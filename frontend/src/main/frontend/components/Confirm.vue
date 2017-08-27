<template>
    <span class="successfully-confirmed">
        You successfully confirmed, now you can <a href="#" @click.prevent="login">login</a>
    </span>
</template>

<script>
    import {LOGIN_MODAL} from '../constants'
    import {root_name} from '../router'
    import bus, {LOGIN} from '../bus'

    export default {
        methods: {
            login() {
                this.$modal.show(LOGIN_MODAL);
            },
            onSuccessLogin() {
                this.$router.push({ name: root_name });
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