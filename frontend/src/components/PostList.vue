<template>
    <div>
        <Search @searchEvent="onChangeSearchString" placeholder="Fulltext search by posts" :initialString="query" v-sticky="{stickyTop: 0, zIndex: 2}"></Search>

        <div class="post-list">
            <div v-if="posts.length>0">
                <post-item v-for="post in posts" v-bind:postDTO="post" :key="post.id"></post-item>
            </div>
            <div v-else>
                No data
            </div>

            <infinite-loading @infinite="infiniteHandler" ref="infiniteLoading" :identifier="infiniteId">
                <span slot="no-more">
                  {{ noMoreMessage }}
                </span>
                <span slot="spinner">
                  <blog-spinner message="Receiving..."></blog-spinner>
                </span>
            </infinite-loading>
        </div>
        <vm-back-top v-if="isLargeScreen" :bottom="80" :right="18"></vm-back-top>
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
    import {updateById, cutPost, initStompClient, closeStompClient, infinitePostsHandlerWithSearch} from '../utils'
    import VueSticky from 'vue-sticky'
    import Notifications from "../notifications"
    import VmBackTop from 'vue-multiple-back-top'
    import {root} from '../routes';
    import {isLargeScreen} from '../utils'
    import bus, {LOGIN, LOGOUT} from '../bus'

    // https://peachscript.github.io/vue-infinite-loading/#!/getting-started/with-filter

    let stompObj;
    let subscriptionInsert;
    let subscriptionUpdate;
    let subscriptionDelete;

    const searchQueryParameter = 'q';

    export default {
        name: 'posts',
        data() {
            return {
                posts: [ ],
                searchString: '',
                noMoreMessage: 'There is no more posts :(',
                infiniteId: new Date(),

            }
        },
        components: {
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
                infinitePostsHandlerWithSearch(this, API_POST, res => res.data, res => {}, $state);
            },

            onChangeSearchString(str) {
                console.log('onChangeSearchString', str);
                this.searchString = str;
                this.posts = [];
                this.$nextTick(() => {
                    this.infiniteId+=1;
                });

                const routerNewState = {path: root};
                if (str){
                    routerNewState.query = {[searchQueryParameter]: str};
                }
                this.$router.push(routerNewState);
            },

            reload(){
                const oldSearchStr = this.searchString;
                this.onChangeSearchString(oldSearchStr);
            },
        },
        computed:{
            query(){
                return this.$route.query[searchQueryParameter];
            },
            isLargeScreen(){
                return isLargeScreen();
            },
        },
        mounted(){
            this.searchString = this.query;

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
                        this.infiniteId+=1;
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
        created() {
            bus.$on(LOGIN, this.reload);
            bus.$on(LOGOUT, this.reload);
        },
        destroyed() {
            //console.log("destroyed");
            bus.$off(LOGIN, this.reload);
            bus.$off(LOGOUT, this.reload);
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