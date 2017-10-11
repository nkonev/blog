<template>
    <div>
        <Search @SEARCH_EVENT="onChangeSearchString"></Search>

        <div class="post-list">
            <div v-if="posts.length>0">
                <post-item v-for="post in posts" v-bind:postDTO="post" :key="post.id"></post-item>
            </div>
            <div v-else>
                No data
            </div>

            <infinite-loading :on-infinite="onInfinite" ref="infiniteLoading">
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
    import {API_POST} from '../constants'
    import BlogSpinner from './BlogSpinner.vue'
    import PostAddFab from './PostAddFab.vue'
    import Search from './Search.vue';
    import {updateById} from '../utils'

    const Stomp = require("@stomp/stompjs/lib/stomp.js").Stomp; // https://github.com/jmesnil/stomp-websocket/issues/119 https://stomp-js.github.io/stomp-websocket/codo/extra/docs-src/Usage.md.html

    // https://peachscript.github.io/vue-infinite-loading/#!/getting-started/with-filter
    const POSTS_PAGE_SIZE = 20;
    const MAX_PAGES = 10;

    let stompClient;

    export default {
        name: 'posts',
        data() {
            return {
                posts: [ ],
                searchString: '',
                noMoreMessage: 'There is no more posts :(',
            }
        },
        components: {
            PostItem,
            InfiniteLoading,
            BlogSpinner,
            PostAddFab,
            Search
        },
        methods: {
            onInfinite() {
                this.$http.get(API_POST, {
                    params: {
                        searchString: this.searchString,
                        page: Math.floor(this.posts.length / POSTS_PAGE_SIZE),
                    },
                }).then((res) => {
                    if (res.data.length) {
                        this.posts = this.posts.concat(res.data); // add data from server's response
                        this.$refs.infiniteLoading.$emit('$InfiniteLoading:loaded');

                        if (Math.floor(this.posts.length / POSTS_PAGE_SIZE) === MAX_PAGES) {
                            this.noMoreMessage = `You reached max pages limit (${MAX_PAGES}). We want to stop to overwhelming your RAM.`;
                            console.log("Overwhelming prevention");
                            this.$refs.infiniteLoading.$emit('$InfiniteLoading:complete');
                        }
                        // Prevent infinity loading bug when there server responds is less than POSTS_PAGE_SIZE elements
                        if (res.data.length < POSTS_PAGE_SIZE) {
                            console.log("Loaded less than page size");
                            this.$refs.infiniteLoading.$emit('$InfiniteLoading:complete');
                        }
                    } else {
                        this.$refs.infiniteLoading.$emit('$InfiniteLoading:complete');
                    }
                });
            },
            onChangeSearchString(str) {
                console.debug('onChangeSearchString', str);
                this.searchString = str;
                this.posts = [];
                this.$nextTick(() => {
                    this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset');
                });
            },
        },
        mounted(){
            const url = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/stomp";
            stompClient = Stomp.client(url);
            stompClient.reconnect_delay = 5000; // reconnect after 5 sec

            stompClient.connect({ }, (frame) => {
                // allowed prefixes here http://www.rabbitmq.com/stomp.html#d
                // subscribe
                stompClient.subscribe("/topic/posts.insert", (data) => {
                    const message = data.body;
                    const obj = JSON.parse(message);
                    // console.log(message);
                    this.posts.unshift(obj);
                });
                stompClient.subscribe("/topic/posts.update", (data) => {
                    const message = data.body;
                    const obj = JSON.parse(message);
                    // console.log(message);
                    const foundPost = updateById(this.posts, obj);
                    if (foundPost) {
                        console.debug("found and updated");
                    }
                });
                stompClient.subscribe("/topic/posts.delete", (data) => {
                    const message = data.body;
                    const id = parseInt(message);
                    const foundPost = this.posts.find((element, index, array)=>{
                        return element.id === id ? (this.posts.splice(index, 1), true) : false;
                    });
                    if (foundPost) {
                        console.debug("found and deleted");
                    }
                    if (this.posts.length === 0) {
                        this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset');
                    }
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
