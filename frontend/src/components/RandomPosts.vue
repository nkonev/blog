<template>
    <div class="random">
        <h1 class="random-h1">Random posts</h1>
        <ul>
            <li v-for="post in posts"><a :href="'/post/' + post.id">{{post.title}}</a></li>
        </ul>
    </div>
</template>

<script>
    import {API_POST_RANDOM} from "../constants";

    export default {
        name: 'random-posts',
        mounted(){
            this.fetchData();
            this.t = setInterval(()=>this.fetchData(), 20*1000);
        },
        destroyed(){
            clearInterval(this.t);
        },
        data(){
            return {
                posts: [],
                t: null
            }
        },
        methods: {
            fetchData() {
                this.$http.get(API_POST_RANDOM).then((response) => {
                    this.posts = response.body;
                }, response => {
                    console.error("Error on fetch random posts", response);
                });
            },
        }
    }
</script>

<style lang="stylus" scoped>
    @import "../constants.styl"

    .random {
        /*display: inline-block;
        border-radius 2px
        background-color whitesmoke
        position fixed
        right 10px
        top 220px
        width 250px*/
        margin 0 0
        padding 0.7em 1em 1em 1em
        top 2em
        right 2em
        position fixed

        &-h1{
            margin 0 0 0.2em 0
            font-family 'DejaVu Sans Mono', monospace
            color $strongblack
        }

        ul{
            margin 0 0
            padding 0em 1em 0em 0em
            list-style-type: none;
        }

        li {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            width 250px
        }

        a {
            text-decoration: none;
        }
    }

    @media screen and (max-width: $contentWidth+400px) {
        .random {
            display none
        }

    }

</style>