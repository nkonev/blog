<template>
    <div class="comment-edit">
        <textarea v-model="editContent" @input="clearErrorMessage()"/>
        <error v-show="errorMessage" :message="errorMessage"></error>
        <div class="comment-command-buttons">
            <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
            <button v-if="!submitting" class="save-btn" @click="onBtnSave">Save</button>
            <button v-if="!submitting && !isAdd" class="cancel-btn" @click="onBtnCancel">Отмена</button>
        </div>
    </div>
</template>


<script>
    import Vue from 'vue'
    import BlogSpinner from './BlogSpinner.vue'
    import bus, {COMMENT_UPDATED, COMMENT_ADD, COMMENT_CANCELED, POST_SWITCHED, LOGIN} from '../bus'
    import {getPostId} from '../utils'
    import Error from './Error.vue'

    export default {
        data() {
            return {
                editContent: '',
                submitting: false,
                errorMessage: ''
            }
        },
        props: ['commentDTO', 'isAdd'],
        components:{
            BlogSpinner, Error
        },
        created(){
            this.editContent = this.commentDTO.text;
            bus.$on(LOGIN, this.clearErrorMessage);
        },
        destroyed(){
            bus.$off(LOGIN, this.clearErrorMessage);
        },
        methods:{
            clearData(){
                this.editContent = '';
            },
            clearErrorMessage(){
                this.errorMessage = '';
            },
            onBtnSave() {
                this.submitting = true;

                const postId = getPostId(this);
                const newComment = Vue.util.extend({}, this.$props.commentDTO);
                newComment.text = this.editContent;
                if (this.$props.isAdd) {
                    this.$http.post(`/api/post/${postId}/comment`, newComment)
                        .then(successResp => {
                            bus.$emit(COMMENT_ADD, successResp.body);
                            this.clearData();
                            this.submitting = false;
                        }, failResp => {
                            this.submitting = false;
                            this.errorMessage = failResp.body.message;
                        });
                } else {
                    this.$http.put(`/api/post/${postId}/comment`, newComment)
                        .then(successResp => {
                            bus.$emit(COMMENT_UPDATED, successResp.body);
                            this.clearData();
                            this.submitting = false;
                        }, failResp => {
                            this.submitting = false;
                            this.errorMessage = failResp.body.message;
                        });
                }
            },
            onBtnCancel() {
                this.clearData();
                bus.$emit(COMMENT_CANCELED);
            },
        },
    }
</script>


<style lang="stylus">
    .comment-edit {

        textarea {
            width 99%
            min-height 60px
        }
    }
</style>
