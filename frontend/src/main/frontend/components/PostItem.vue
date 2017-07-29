<template>
    <div class="post">
        <div class="img-wrapper">
            <img :src="postDTO.titleImg"/>
        </div>
        <div class="text-wrapper">
            <router-link :to="{name: post, params: { id: postDTO.id} }"><h2>{{postDTO.title}}</h2></router-link>
            <div class="post-content">
                {{postDTO.text}}
            </div>
        </div>
        <hr/>
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
    $imgMaxHeight=200px
    .post {
        display block
        // border-style solid
        border-width 1px;
        border-color $borderColor
        padding 2px;
        margin 2px;
        //clear: both

        .img-wrapper {
            // max-height 400px;
            // clear both
            img {
                float: left
                margin 4px;
                width: 250px;
                // display inline
                // height: auto;
                //max-height 400px;
                max-height $imgMaxHeight
            }
        }

        .text-wrapper {
            //clear: both
            // height auto
            // float: right;
            min-height $imgMaxHeight
            max-height 600px;

            h2 {
                padding 2px;
                margin 2px;
            }
            &-content {
                height auto
                text-align justify
            }

        }
    }
</style>