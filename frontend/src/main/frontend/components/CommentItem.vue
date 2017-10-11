<template>
    <div class="comment">
        <div class="user-info">
            <img :src="commentDTO.owner.avatar">
            <span>{{commentDTO.owner.login}}</span>
        </div>

        <div v-if="!isEditing">
            <span class="manage-buttons">
                <img class="edit-container-pen" src="../assets/pen.png" v-if="commentDTO.canEdit" @click="setEdit()"/>
                <img class="remove-container-x" src="../assets/remove.png" v-if="commentDTO.canDelete" @click="doDelete()"/>
            </span>

            <div class="comment-content">
                {{commentDTO.text}}
            </div>
        </div>
        <div v-else>
            <input
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
        <hr/>
    </div>
</template>

<script>
    import Vue from 'vue'
    import BlogSpinner from './BlogSpinner.vue'
    import bus, {COMMENT_SAVED} from '../bus'

    export default {
        name: 'comment-item',
        props: ['commentDTO'], // it may be an object, for ability to set default values
        data() {
            return {
                isEditing: false,
                editContent: '',
                submitting: false,
            }
        },
        methods: {
            setEdit(){
                this.isEditing = true;
            },
            onBtnSave() {
                this.submitting = true;

                const newComment = Vue.util.extend({}, this.$props.commentDTO);
                newComment.text = this.editContent;

                this.$http.put('/api/post/0/comment', newComment)
                    .then(successResp => {
                        bus.$emit(COMMENT_SAVED, successResp.body);

                        this.submitting = false;
                        this.isEditing = false;
                    }, failResp => {
                        this.submitting = false;
                    })
            },
            onBtnCancel() {
                this.isEditing = false;
            },
        },
        components:{
            BlogSpinner
        }
    };
</script>

<style lang="stylus">
    .comment {
        // border-width 1px
        // border-color black
        // border-style solid
        margin 2px;

        .user-info {
            img {
                max-width 48px;
                max-height 48px;
            }
        }

        img.edit-container-pen {
            height 16px;
            cursor pointer
        }
        img.remove-container-x {
            height 16px;
            cursor pointer
        }

    }
</style>