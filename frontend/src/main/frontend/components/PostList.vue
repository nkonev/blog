<template>
    <div>
        <div class="search">
            <label for="search">Search</label>
            <input id="search" v-model="searchString" @input="onChangeSearchString()"/> <button @click="onClearButton()">clear</button>
        </div>
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
    const Stomp = require("@stomp/stompjs/lib/stomp.js").Stomp; // https://github.com/jmesnil/stomp-websocket/issues/119 https://stomp-js.github.io/stomp-websocket/codo/extra/docs-src/Usage.md.html

    // https://peachscript.github.io/vue-infinite-loading/#!/getting-started/with-filter
    const api = '/api/public/post';
    const POSTS_PAGE_SIZE = 20;
    const MAX_PAGES = 10;

    let stompClient;

    export default {
        name: 'posts',
        data() {
            return {
                posts: [ ],
                searchString: ''
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
                        searchString: this.searchString,
                        page: Math.floor(this.posts.length / POSTS_PAGE_SIZE),
                    },
                }).then((res) => {
                    if (res.data.length) {
                        this.posts = this.posts.concat(res.data); // add data from server's response
                        this.$refs.infiniteLoading.$emit('$InfiniteLoading:loaded');
                        if (Math.floor(this.posts.length / POSTS_PAGE_SIZE) === MAX_PAGES) {
                            console.log("Overwhelming prevention");
                            this.$refs.infiniteLoading.$emit('$InfiniteLoading:complete');
                        }
                    } else {
                        this.$refs.infiniteLoading.$emit('$InfiniteLoading:complete');
                    }
                });
            },
            onChangeSearchString() {
                console.debug('onChangeSearchString');
                this.posts = [];
                this.$nextTick(() => {
                    this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset');
                });
            },
            onClearButton() {
                this.searchString = '';
                this.onChangeSearchString();
            }
        },
        mounted(){
            const url = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/stomp";
            stompClient = Stomp.client(url);
            stompClient.reconnect_delay = 5000; // reconnect after 5 sec

            stompClient.connect({ }, (frame) => {
                // subscribe
                stompClient.subscribe("/topic/posts", (data) => {
                    const message = data.body;
                    const obj = JSON.parse(message);
                    console.log(message);
                    this.posts.unshift(obj);
                });
            });
        },
        beforeDestroy() {
            console.debug('closing stompClient');
            if (stompClient) {
                stompClient.disconnect();
            }
        }
    }
</script>

<style lang="stylus" scoped>
    .search {
        margin-top 4px;
    }
</style>