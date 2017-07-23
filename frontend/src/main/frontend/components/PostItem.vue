<template>
    <div class="post">
        <img :src="postDTO.titleImg"/>
        <router-link :to="{name: post, params: { id: postDTO.id} }"><h2>{{postDTO.title}}</h2></router-link>
        <div class="post-content">
            {{postDTO.text}}
            <hr/>
        </div>
    </div>
</template>

<script>
    import {post} from '../router';

    export default {
        name: 'post-item',
        props: ['postDTO'], // it may be an object, for ability to set default values
        data() {
            return {
                post: post
            }
        }
    }
</script>

<style lang="stylus" scoped>
    @import "../constants.styl"
    $borderColor=red;
    .post {
        display block
    // border-style solid
        border-width 1px;
        border-color $borderColor
        padding 2px;
        margin 2px;

        h2 {
            padding 2px;
            margin 2px;
        }

        img {
            float: left
            margin 4px;
            width: 250px;
            height: auto;
            max-height 400px;
        }

        &-content {
            text-align justify
        }
    }
</style>