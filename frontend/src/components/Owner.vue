<template>
    <div class="user-info" >

        <div class="time-container">
            <span v-if="!hideWrittenBy" class="prepend">written by </span>

            <router-link :to="`/user/${owner.id}`">
                <avatar :src="owner.avatar" :username="owner.login" :size="40"/>
            </router-link>
            <router-link :to="`/user/${owner.id}`">
                <span>{{owner.login}}</span>
            </router-link>
            <span class="time"> at {{createTime}}</span>
        </div>
        <span v-if="editTime" class="time-container">edited at {{editTime}}</span>
        <slot></slot>
    </div>
</template>

<script>
    import Avatar from 'vue-avatar'

    export default {
        props: ['owner', 'createTime', 'editTime', 'hideWrittenBy'],
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
        justify-content space-between
        flex-wrap wrap
        color grey

        .time-container {
            display flex
            flex-direction row
            align-items center
        }

        span.time {
            margin 0 0.4em
        }

        span.prepend {
            margin-right 0.8em
        }

        .vue-avatar--wrapper {
            margin-right 0.3em
        }
    }


    @media screen and (max-width: $contentWidth) {
        .user-info {
            justify-content flex-start
        }

        span:nth-child(even) {
            margin-top 0.4em
        }
    }
</style>