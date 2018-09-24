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
            <random-posts v-if="showRandom"/>

            <infinite-loading @infinite="infiniteHandler" ref="infiniteLoading">
                <span slot="no-more">
                  {{ noMoreMessage }}
                </span>
                <span slot="spinner">
                  <blog-spinner message="Receiving..."></blog-spinner>
                </span>
            </infinite-loading>
        </div>
        <vm-back-top :bottom="80" :right="18"></vm-back-top>
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
    import {updateById, cutPost, initStompClient, closeStompClient, infinitePostsHandler, isLargeScreen} from '../utils'
    import VueSticky from 'vue-sticky'
    import RandomPosts from "./RandomPosts.vue";
    import Notifications from "../notifications"
    import VmBackTop from 'vue-multiple-back-top'

    // https://peachscript.github.io/vue-infinite-loading/#!/getting-started/with-filter

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
            Search,
            'vm-back-top': VmBackTop
        },
        directives: {
            'sticky': VueSticky,
        },
        methods: {
            infiniteHandler($state) {
                infinitePostsHandler(this, API_POST, res => res, res => {}, $state);
            },

            onChangeSearchString(str) {
                console.log('onChangeSearchString', str);
                this.searchString = str;
                this.posts = [];
                this.$nextTick(() => {
                    this.$refs.infiniteLoading.$emit('$InfiniteLoading:reset');
                });
            },
        },
        computed: {
            showRandom(){
                return isLargeScreen();
            }
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
                    Notifications.postCreated(obj.title);
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