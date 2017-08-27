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
                  {{ noMoreMessage }}
                </span>
                <span slot="spinner">
                  <blog-spinner message="Receiving..."></blog-spinner>
                </span>
            </infinite-loading>
        </div>
        <div class="fab"><!--<button>+</button>--><router-link to="/post/add">+</router-link></div>
    </div>
</template>

<script>
    import PostItem from './PostItem.vue'
    import InfiniteLoading from 'vue-infinite-loading';
    import debounce from "lodash/debounce"
    import {API_POST} from '../constants'
    import BlogSpinner from './BlogSpinner.vue'
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
            BlogSpinner
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
            onChangeSearchString() {
                console.debug('onChangeSearchString');
                this.posts = [];
                this.$nextTick(() => {
                    this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset');
                });
            },
            onClearButton() {
                if (this.searchString !== '') {
                    this.searchString = '';
                    this.onChangeSearchString();
                }
            }
        },
        created() {
            // https://forum-archive.vuejs.org/topic/5174/debounce-replacement-in-vue-2-0
            this.onChangeSearchString = debounce(this.onChangeSearchString, 500);
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
    $fabSize=40px
    .search {
        margin-top 4px;
    }
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