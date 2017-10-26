<template>
    <div class="fab"><!--<button>+</button>--><router-link tag="div" to="/post/add">+</router-link></div>
</template>

<script>
    export default {
        name: 'post-add-fab',
    }
</script>

<style lang="stylus" scoped>
    $fabSize=40px

    .fab {
        display: inline-block;
        position fixed
        right 20px
        bottom 20px
        height $fabSize
        width $fabSize
        margin 0 0
        padding 0 0
        div {
            display block
            height 100%
            width 100%
            //border: 2px solid
            line-height $fabSize
            text-align: center;
            font-size xx-large

            font-weight bold
            font-family monospace
            margin 0 0
            padding 0 0
            text-decoration none
            cursor pointer
            background-color #0086B3
            color white
            border-radius ($fabSize / 2)
            transition: 0.1s all;
            opacity 0.8
            &:hover {
                opacity 1
                transform: scale(1.3, 1.3);
                box-shadow: 0 0 40px #0086B3;
            }
        }
    }
</style>