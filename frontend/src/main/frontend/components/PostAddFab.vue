<template>
    <div class="fab"><!--<button>+</button>--><router-link to="/post/add">+</router-link></div>
</template>

<script>
    export default {
        name: 'post-add-fab',
    }
</script>

<style lang="stylus" scoped>
    $fabSize=40px

    .fab {
        position fixed
        right 20px
        bottom 20px
        line-height: $fabSize - 5px
        a {
            display block
            text-align center
            vertical-align: middle;
            text-decoration none
            cursor pointer
            background-color #0086B3
            color white
            height $fabSize
            width $fabSize
            border-radius $fabSize
            border-width 0px
            transition: 0.1s all;
            opacity 0.8
            &:hover {
                opacity 1
                color: mix(#8b8c8d, black, 80%);
                box-shadow: 0 0 40px black;
            }
        }
    }
</style>