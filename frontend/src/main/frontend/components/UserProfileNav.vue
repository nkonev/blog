<template>
    <div class="user-profile-nav">
        <template v-if="currentUser">
            <div class="user-profile-nav-login">{{currentUser.login}}</div>
            <img :src="currentUser.avatar"/>
        </template>
        <div class="user-profile-nav-not-registegred" v-else>
            Not registered
        </div>
    </div>
</template>

<script>
    import store from '../store'

    export default {
        name: 'user-profile-nav',
        props: ['currentUser'],
        mounted() {
            console.debug('user-profile-nav loaded', this.$props);
        }
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