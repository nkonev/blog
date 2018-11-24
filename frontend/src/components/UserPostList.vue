<template>
    <div>
        <div class="post-list">
            <h1>{{total}} posts</h1>

            <div v-if="posts.length>0">
                <post-item v-for="post in posts" v-bind:postDTO="post" :key="post.id"></post-item>
            </div>

            <infinite-loading @infinite="infiniteHandler" ref="infiniteLoading">
                <span slot="no-more">
                  {{ noMoreMessage }}
                </span>
                <span slot="spinner">
                  <blog-spinner message="Receiving..."></blog-spinner>
                </span>
            </infinite-loading>
        </div>
        <post-add-fab/>
    </div>
</template>

<script>
    import PostItem from './PostItem.vue'
    import InfiniteLoading from 'vue-infinite-loading';
    import BlogSpinner from './BlogSpinner.vue'
    import PostAddFab from './PostAddFab.vue'
    import {infinitePostsHandlerWithSearch} from '../utils'


    // https://peachscript.github.io/vue-infinite-loading/#!/getting-started/with-filter

    export default {
        name: 'user-posts',
        props: ['id'],
        data() {
            return {
                posts: [ ],
                total: 0,
                noMoreMessage: 'There is no more posts :(',
            }
        },
        components: {
            PostItem,
            InfiniteLoading,
            BlogSpinner,
            PostAddFab,
        },
        methods: {
            infiniteHandler($state) {
                infinitePostsHandlerWithSearch(this, `/api/user/${this.id}/posts`, res => res.data, res => {this.total = res.data.totalCount;}, $state);
            },
        },
        mounted(){
        },
        beforeDestroy() {
        },
        metaInfo: {
            title: 'User posts',
        }
    }
</script>

<style lang="stylus" scoped>
    .post-list {
        display: block;
        clear: left;

        h1 {
            text-align center
            background-color dimgrey
            color: white
            margin-bottom 0
            border-radius 2px
        }
    }
</style>