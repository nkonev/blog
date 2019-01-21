<template>
    <div class="comment-edit">
        <textarea v-model="editContent" @input="clearErrorMessage()"/>
        <error v-show="errorMessage" :message="errorMessage"></error>
        <div class="comment-command-buttons">
            <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
            <button v-if="!submitting" class="blog-btn ok-btn" @click="onBtnSave">Save comment</button>
            <button v-if="!submitting && !isAdd" class="blog-btn cancel-btn" @click="onBtnCancel">Cancel</button>
        </div>
    </div>
</template>


<script>
    import Vue from 'vue'
    import BlogSpinner from './BlogSpinner.vue'
    import bus, {COMMENT_UPDATED, COMMENT_ADD, COMMENT_CANCELED, LOGIN} from '../bus'
    import {getPostId} from '../utils'
    import Error from './Error.vue'

    const br = "<br />";
    const brRegexp = /\<br \/\>/g;

    export default {
        data() {
            return {
                editContent: '',
                submitting: false,
                errorMessage: '',
                localStorageKey: null
            }
        },
        props: ['commentDTO', 'isAdd'],
        components:{
            BlogSpinner, Error
        },
        created(){
            const prevText = this.commentDTO.text;
            this.editContent = prevText ? prevText.replace(brRegexp, '\n') : '';
            bus.$on(LOGIN, this.clearErrorMessage);
            this.localStorageKey = 'commentDto_' + (this.isAdd ? 'new' : this.commentDTO.id);
        },
        destroyed(){
            bus.$off(LOGIN, this.clearErrorMessage);
        },
        mounted(){
            if (localStorage.getItem(this.localStorageKey)) {
                try {
                    this.editContent = localStorage.getItem(this.localStorageKey);
                } catch(e) {
                    console.error("Exception during parsing localstorage value - will delete this value", e);
                    localStorage.removeItem(this.localStorageKey);
                }
            }
        },
        methods:{
            clearData(){
                this.editContent = '';
                localStorage.removeItem(this.localStorageKey);
            },
            clearErrorMessage(){
                this.errorMessage = '';
            },
            onBtnSave() {
                this.submitting = true;

                const postId = getPostId(this);
                const newComment = Vue.util.extend({}, this.$props.commentDTO);
                newComment.text = this.editContent.replace(/\n/g, br);
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
        watch:{
            'editContent':{
                handler: function (val, oldVal) {
                    localStorage.setItem(this.localStorageKey, this.editContent);
                },
                deep: true
            }
        }

    }
</script>


<style lang="stylus" scoped>
    .comment-edit {

        textarea {
            width 99%
            min-height 60px
        }

        .comment-command-buttons {
            margin-top 0.3em

            display flex
            flex-direction row
        }

    }
</style>
