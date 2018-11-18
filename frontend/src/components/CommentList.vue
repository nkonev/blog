<template>
    <div class="comments">
        <v-dialog/>
        <template v-if="comments.length>0">
            <h3 class="comments-header">Comments:</h3>
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
        <div v-else id="comments-list" class="comments-no-comments">
            No comments. Write first comment:
        </div>
        <CommentEdit :commentDTO="{}" :isAdd="true"></CommentEdit>
    </div>

</template>

<script>
    import Vue from 'vue'
    import CommentItem from "./CommentItem.vue";
    import {PAGE_SIZE} from "../constants";
    import Paginate from 'vuejs-paginate';
    import bus, {POST_SWITCHED, COMMENT_UPDATED, COMMENT_ADD, COMMENT_DELETED} from '../bus'
    import {updateById, getPostId} from '../utils'
    import CommentEdit from './CommentEdit.vue'

    Vue.component('paginate', Paginate);

    export default {
        components: {CommentItem, CommentEdit},
        data() {
            return {
                comments: [],
                pageCount: 0,
            }
        },
        methods: {
            reloadPage: function(pageNum) {
                this.$router.push({query: {page: pageNum}});
                console.log("opening comment page ", pageNum);

                this.fetchComments(pageNum);
            },
            onPostSwitched(){
                this.fetchComments();
            },
            /**
             *
             * @param pageNum. decreases to 1 for compability with Spring Data
             */
            fetchComments(pageNum=(this.initialPageIndex+1)){
                const postId = getPostId(this);

                this.$http.get('/api/post/'+postId+'/comment?page='+(pageNum-1)+'&size='+PAGE_SIZE).then(
                    response => {
                        this.pageCount = this.getPageCount(response.body.totalCount);
                        this.comments = response.body.data;
                    }, response => {
                        console.error(response);
                        // alert(response);
                    }
                );
            },
            getPageCount(totalCommentCount){
                return Math.ceil(totalCommentCount / PAGE_SIZE);
            },
            shouldSwitch(totalCommentCount){
                return !(totalCommentCount % PAGE_SIZE);
            },
            updateComment(newComment){
                // console.log('updateComment', newComment);
                updateById(this.comments, newComment);
            },
            insertComment(newComment) {
                this.pageCount = this.getPageCount(newComment.commentsInPost);

                const nextPage = this.shouldSwitch(newComment.commentsInPost) ? this.pageCount+1 : this.pageCount;
                this.reloadPage(nextPage);
                if (this.$refs.paginate) {
                    this.$refs.paginate.selected = nextPage-1;
                }
            },
            deleteComment(newCommentCount){
                this.pageCount = this.getPageCount(newCommentCount);
                const nextPage = this.shouldSwitch(newCommentCount) ? this.initialPageIndex : this.initialPageIndex +1;
                this.reloadPage(nextPage);
                if (this.$refs.paginate) {
                    this.$refs.paginate.selected = nextPage-1;
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
            bus.$on(POST_SWITCHED, this.onPostSwitched);
            bus.$on(COMMENT_UPDATED, this.updateComment);
            bus.$on(COMMENT_ADD, this.insertComment);
            bus.$on(COMMENT_DELETED, this.deleteComment);
        },
        destroyed(){
            bus.$off(POST_SWITCHED, this.onPostSwitched);
            bus.$off(COMMENT_UPDATED, this.updateComment);
            bus.$off(COMMENT_ADD, this.insertComment);
            bus.$off(COMMENT_DELETED, this.deleteComment);
        },
    };
</script>

<style lang="stylus">
    @import "./pageable.styl"

    .comments {
        &-header{
            text-align center
            //font-size x-large
            margin 20px 0 0
        }

        &-no-comments {
            text-align center
            font-size large
        }
    }
</style>