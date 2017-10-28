<template>
    <div class="user-info" >
        <router-link :to="`/user/${owner.id}`">
            <img :src="owner.avatar"/>
        </router-link>
        <router-link :to="`/user/${owner.id}`">
            <span>{{owner.login}}</span>
        </router-link>
    </div>
</template>

<script>
    export default {
        props: ['owner']
    }
</script>

<style lang="stylus">
    .user-info {
        display flex
        flex-direction row
        align-items center

        img {
            margin-right 5px
            height 40px;
        }
    }

</style>