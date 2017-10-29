<template>
    <div class="user-info" >
        <router-link :to="`/user/${owner.id}`">
            <img :src="owner.avatar"/>
        </router-link>
        <router-link :to="`/user/${owner.id}`">
            <span>{{owner.login}}</span>
        </router-link>
        <span class="time"> at {{time}}</span>
    </div>
</template>

<script>
    export default {
        props: ['owner', 'time']
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

        span.time {
            margin 0 0.4em
        }
    }

</style>