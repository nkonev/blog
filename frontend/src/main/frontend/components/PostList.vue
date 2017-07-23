<template>
    <div>
        <label for="search">Search</label>
        <input id="search"/>
        <div class="post-list">
            <div v-if="posts.length>0">
                <post-item v-for="post in posts" v-bind:postDTO="post" :key="post.id"></post-item>
            </div>
            <div v-else>
                No data
            </div>

            <infinite-loading :on-infinite="onInfinite" ref="infiniteLoading">
                <span slot="no-more">
                  There is no more posts :(
                </span>
            </infinite-loading>
        </div>
    </div>
</template>

<script>
    import PostItem from './PostItem.vue'
    import InfiniteLoading from 'vue-infinite-loading';

    // https://peachscript.github.io/vue-infinite-loading/#!/getting-started/with-filter
    const api = '/api/public/posts';

    export default {
        name: 'posts',
        data() {
            return {
                posts: [ ]
            }
        },
        components: {
            PostItem,
            InfiniteLoading
        },
        methods: {
            onInfinite() {
                this.$http.get(api, {
                    params: {
                        page: this.posts.length / 20,
                    },
                }).then((res) => {
                    if (res.data.length) {
                        this.posts = this.posts.concat(res.data); // add data from server's response
                        this.$refs.infiniteLoading.$emit('$InfiniteLoading:loaded');
                        if (this.posts.length / 20 === 10) {
                            this.$refs.infiniteLoading.$emit('$InfiniteLoading:complete');
                        }
                    } else {
                        this.$refs.infiniteLoading.$emit('$InfiniteLoading:complete');
                    }
                });
            },
            changeFilter() {
                this.posts = [];
                this.$nextTick(() => {
                    this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset');
                });
            },
        }
    }
</script>

<style lang="stylus" scoped>

</style>