<template>
    <div class="comment-edit">
        <textarea
                v-bind:value="commentDTO.text"
                v-on:input="editContent = $event.target.value"
        />
        <div class="command-buttons">
            <div class="send">
                <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
                <button v-if="!submitting" class="save-btn" @click="onBtnSave">Save</button>
            </div>
            <button v-if="!submitting" class="save-btn" @click="onBtnCancel">Отмена</button>
        </div>
    </div>
</template>


<script>
    import Vue from 'vue'
    import BlogSpinner from './BlogSpinner.vue'
    import bus, {COMMENT_SAVED, COMMENT_CANCELED} from '../bus'

    export default {
        data() {
            return {
                editContent: '',
                submitting: false,
            }
        },
        props: ['commentDTO'],
        components:{
            BlogSpinner
        },
        methods:{
            onBtnSave() {
                this.submitting = true;

                const newComment = Vue.util.extend({}, this.$props.commentDTO);
                newComment.text = this.editContent;

                this.$http.put('/api/post/0/comment', newComment)
                    .then(successResp => {
                        bus.$emit(COMMENT_SAVED, successResp.body);
                        this.submitting = false;
                    }, failResp => {
                        this.submitting = false;
                    })
            },
            onBtnCancel() {
                bus.$emit(COMMENT_CANCELED);
            },
        }
    }
</script>


<style lang="stylus">
    .comment-edit {


    }
</style>
