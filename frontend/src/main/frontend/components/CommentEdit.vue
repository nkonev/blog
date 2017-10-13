<template>
    <div class="comment-edit">
        <textarea
                v-bind:value="commentDTO.text"
                v-on:input="editContent = $event.target.value"
        />
        <div class="comment-command-buttons">
            <div class="send">
                <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
                <button v-if="!submitting" class="save-btn" @click="onBtnSave">Save</button>
            </div>
            <button v-if="!submitting && !isAdd" class="save-btn" @click="onBtnCancel">Отмена</button>
        </div>
    </div>
</template>


<script>
    import Vue from 'vue'
    import BlogSpinner from './BlogSpinner.vue'
    import bus, {COMMENT_UPDATED, COMMENT_ADD, COMMENT_CANCELED, POST_SWITCHED} from '../bus'
    import {getPostId} from '../utils'

    export default {
        data() {
            return {
                editContent: '',
                submitting: false,
            }
        },
        props: ['commentDTO', 'isAdd'],
        components:{
            BlogSpinner
        },
        methods:{
            onBtnSave() {
                this.submitting = true;

                const postId = getPostId(this);
                const newComment = Vue.util.extend({}, this.$props.commentDTO);
                newComment.text = this.editContent;
                if (this.$props.isAdd) {
                    this.$http.post(`/api/post/${postId}/comment`, newComment)
                        .then(successResp => {
                            bus.$emit(COMMENT_ADD, successResp.body);
                            this.submitting = false;
                        }, failResp => {
                            this.submitting = false;
                        });
                } else {
                    this.$http.put(`/api/post/${postId}/comment`, newComment)
                        .then(successResp => {
                            bus.$emit(COMMENT_UPDATED, successResp.body);
                            this.submitting = false;
                        }, failResp => {
                            this.submitting = false;
                        });
                }
            },
            onBtnCancel() {
                this.editContent = '';
                bus.$emit(COMMENT_CANCELED);
            },
        },
    }
</script>


<style lang="stylus">
    .comment-edit {


    }
</style>
