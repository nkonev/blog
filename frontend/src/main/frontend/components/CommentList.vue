<template>
    <div class="comments">
        <template v-if="comments.length>0">
            <h1 class="comments-header">Comments:</h1>
            <div id="comments-list">
                <comment-item v-for="comment in comments" v-bind:commentDTO="comment" :key="comment.id"></comment-item>
            </div>
            <paginate
                    :page-count="pageCount"
                    :margin-pages="2"
                    :click-handler="reloadPage"
                    :page-range="4"
                    :initial-page="initialPageIndex"
                    ref="paginate"
                    :container-class="'pagination'"
                    :page-class="'page-item'"
                    :page-link-class="'page-link-item'"
                    :prev-class="'prev-item'"
                    :prev-link-class="'prev-link-item arrow-link-item'"
                    :next-class="'next-item'"
                    :next-link-class="'next-link-item arrow-link-item'"
            ></paginate>
        </template>
        <div v-else id="comments-list">
            No comments
        </div>
        <button @click="insertComment">Add comment</button>
    </div>

</template>

<script>
    import Vue from 'vue'
    import CommentItem from "./CommentItem.vue";
    import {PAGE_SIZE} from "../constants";
    import Paginate from 'vuejs-paginate';
    import bus, {POST_SWITCHED, COMMENT_SAVED} from '../bus'
    import {updateById} from '../utils'

    Vue.component('paginate', Paginate);

    export default {
        components: {CommentItem},
        data() {
            return {
                comments: [],
                pageCount: 0,
                postIdCache: null,
            }
        },
        methods: {
            reloadPage: function(pageNum) {
                this.$router.push({query: {page: pageNum}});
                console.log("opening comment page ", pageNum);

                this.fetchComments(this.postIdCache, pageNum);
            },
            /**
             *
             * @param postId goes from event
             * @param pageNum
             */
            fetchComments(postId, pageNum=(this.initialPageIndex+1)){
                this.postIdCache = postId;
                this.$http.get('/api/post/'+postId+'/comment?page='+(pageNum-1)+'&size='+PAGE_SIZE).then(
                    response => {
                        this.pageCount = Math.ceil(response.body.totalCount / PAGE_SIZE);
                        this.comments = response.body.data;
                    }, response => {
                        console.error(response);
                        // alert(response);
                    }
                );
            },
            updateComment(newComment){
                console.log('updateComment', newComment);
                updateById(this.comments, newComment);
            },
            insertComment() {
                this.reloadPage(this.pageCount);
                if (this.$refs.paginate) {
                    this.$refs.paginate.selected = this.pageCount - 1;
                }
            }
        },
        computed: {
            //  The index of initial page which selected. default: 0
            initialPageIndex() {
                return this.$route.query.page ? parseInt(this.$route.query.page-1) : 0;
            }
        },
        created() {
            // this.reloadPage(this.initialPageIndex+1);
            bus.$on(POST_SWITCHED, this.fetchComments);
            bus.$on(COMMENT_SAVED, this.updateComment);
        },
        destroyed(){
            bus.$off(POST_SWITCHED, this.fetchComments);
            bus.$off(COMMENT_SAVED, this.updateComment);
        },
    };
</script>

<style lang="stylus">
    @import "./pageable.styl"

    .comments {
        &-header{
            text-align center
        }
    }
</style>