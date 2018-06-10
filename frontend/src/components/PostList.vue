<template>
    <div>
        <Search @SEARCH_EVENT="onChangeSearchString" placeholder="Fulltext search by posts" v-sticky="{stickyTop: 0, zIndex: 2}"></Search>

        <div class="post-list">
            <div v-if="posts.length>0">
                <post-item v-for="post in posts" v-bind:postDTO="post" :key="post.id"></post-item>
            </div>
            <div v-else>
                No data
            </div>
            <random-posts></random-posts>

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
    import {API_POST} from '../constants'
    import BlogSpinner from './BlogSpinner.vue'
    import PostAddFab from './PostAddFab.vue'
    import Search from './Search.vue';
    import {updateById, cutPost, initStompClient, closeStompClient} from '../utils'
    import VueSticky from 'vue-sticky'
    import RandomPosts from "./RandomPosts.vue";

    // https://peachscript.github.io/vue-infinite-loading/#!/getting-started/with-filter
    const POSTS_PAGE_SIZE = 20;
    const MAX_PAGES = 10;

    let stompObj;
    let subscriptionInsert;
    let subscriptionUpdate;
    let subscriptionDelete;
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
            RandomPosts,
            PostItem,
            InfiniteLoading,
            BlogSpinner,
            PostAddFab,
            Search
        },
        directives: {
            'sticky': VueSticky,
        },
        methods: {
            infiniteHandler($state) {
                this.$http.get(API_POST, {
                    params: {
                        searchString: this.searchString,
                        page: Math.floor(this.posts.length / POSTS_PAGE_SIZE),
                    },
                }).then((res) => {
                    if (res.data.length) {
                        var new_array = res.data.map((e) => {
                            cutPost(e);
                            return e;
                        });

                        this.posts = this.posts.concat(new_array); // add data from server's response
                        $state.loaded();

                        if (Math.floor(this.posts.length / POSTS_PAGE_SIZE) === MAX_PAGES) {
                            this.noMoreMessage = `You reached max pages limit (${MAX_PAGES}). We want to stop to overwhelming your RAM.`;
                            console.log("Overwhelming prevention");
                            $state.complete();
                        }
                        // Prevent infinity loading bug when there server responds is less than POSTS_PAGE_SIZE elements
                        if (res.data.length < POSTS_PAGE_SIZE) {
                            console.log("Loaded less than page size");
                            $state.complete();
                        }
                    } else {
                        $state.complete();
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
            stompObj = initStompClient((frame) => {
                // allowed prefixes here http://www.rabbitmq.com/stomp.html#d
                // subscribe
                subscriptionInsert=stompObj.stompClient.subscribe("/topic/posts.insert", (data) => {
                    const message = data.body;
                    const obj = JSON.parse(message);
                    cutPost(obj);
                    // console.log(message);
                    this.posts.unshift(obj);
                });
                subscriptionUpdate=stompObj.stompClient.subscribe("/topic/posts.update", (data) => {
                    const message = data.body;
                    const obj = JSON.parse(message);
                    cutPost(obj);
                    // console.log(message);
                    const foundPost = updateById(this.posts, obj);
                    if (foundPost) {
                        console.debug("found and updated");
                    }
                });
                subscriptionDelete=stompObj.stompClient.subscribe("/topic/posts.delete", (data) => {
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
            try {
                subscriptionInsert.unsubscribe();
                subscriptionUpdate.unsubscribe();
                subscriptionDelete.unsubscribe();
            } catch (ignored){}
            closeStompClient(stompObj);
        },
        metaInfo: {
            title: 'Posts',
        }
    }
</script>

<style lang="stylus">
    .search {
        margin-top 12px
        margin-bottom 10px
    }
</style>