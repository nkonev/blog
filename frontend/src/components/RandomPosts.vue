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
        display: inline-block;
        position fixed
        right 10px
        top 220px
        width 250px
        margin 0 0
        padding 0 0

        &-h1{
            margin 0.2em 0
        }

        ul{
            margin 0 0
            padding 0em 1em 0em 1.0em
        }
    }

    @media screen and (max-width: $contentWidth+400px) {
        .random {
            display none
        }

    }

</style>