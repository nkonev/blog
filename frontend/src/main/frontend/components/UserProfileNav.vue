<template>
    <div class="user-profile-nav">
        <div class="user-profile-nav-login">vasya</div>
        <img src="/build/assets/logo.png"/>
    </div>
</template>

<script>
    export default {
        name: 'user-profile-nav'
    }
</script>

<style lang="stylus" scoped>
    .user-profile-nav{

        display flex
        flex-direction row
        justify-content flex-end

        &-login {
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