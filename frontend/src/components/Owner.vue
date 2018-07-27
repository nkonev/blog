<template>
    <div class="user-info" >
        <span v-if="prepend" class="prepend">{{prepend}}</span>

        <router-link :to="`/user/${owner.id}`">
            <avatar :src="owner.avatar" :username="owner.login" :size="40"/>
        </router-link>
        <router-link :to="`/user/${owner.id}`">
            <span>{{owner.login}}</span>
        </router-link>
        <span class="time"> at {{time}}</span>
    </div>
</template>

<script>
    import Avatar from 'vue-avatar'

    export default {
        props: ['prepend', 'owner', 'time'],
        components:{
            Avatar
        }
    }
</script>

<style lang="stylus" scoped>
    @import "../constants.styl"

    .user-info {
        display flex
        flex-direction row
        align-items center
        flex-wrap wrap
        color grey

        .prepend {
            margin-left 1em
            margin-right 0.8em
            //display flex
            //align-items center
        }

        span {
            margin-left 6px
            margin-right 6px
        }

        span.time {
            margin 0 0.4em
        }
    }


    @media screen and (max-width: $contentWidth) {
        .user-info {
            justify-content flex-start
            .prepend {
                margin-left 0.6em
                margin-right 0
            }

        }

    }
</style>