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
    import SockJS from 'sockjs-client' // https://www.npmjs.com/package/sockjs-client

    // https://peachscript.github.io/vue-infinite-loading/#!/getting-started/with-filter
    const api = '/api/public/post';
    const POSTS_PAGE_SIZE = 20;
    const MAX_PAGES = 10;

    let socket;

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
            socket = new SockJS('/stomp');
            let stompClient = Stomp.over(socket);
            stompClient.connect({ }, function(frame) {
                // subscribe
                stompClient.subscribe("/topic/greetings", function(data) {
                    var message = data.body;
                    console.log(message);
                });

            });
        },
        beforeDestroy() {
            console.debug('closing SockJs');
            socket.close();
        }
    }
</script>

<style lang="stylus" scoped>
    .search {
        margin-top 4px;
    }
</style>