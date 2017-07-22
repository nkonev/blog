<template>
    <div class="post">
        <img :src="postDTO.titleImg"/>
        <h2>{{postDTO.title}}</h2>
        <div class="post-content">
            {{postDTO.text}}
            <hr/>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'post-item',
        props: ['postDTO'], // it may be an object, for ability to set default values
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