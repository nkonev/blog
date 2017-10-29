<template>
    <div class="user-info" >
        <span v-if="prepend" class="prepend">{{prepend}}</span>

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
        props: ['prepend', 'owner', 'time']
    }
</script>

<style lang="stylus" scoped>
    .user-info {
        display flex
        flex-direction row
        align-items center
        flex-wrap wrap

        .prepend {
            margin-left 2em
            margin-right 0.8em
            //display flex
            //align-items center
        }

        img {
            margin-right 5px
            height 40px;
        }

        span.time {
            margin 0 0.4em
        }
    }

</style>