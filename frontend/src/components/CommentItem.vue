<template>
    <div class="comment">
        <div class="comment-head">
            <owner :owner="commentDTO.owner" :time="createDateTime"></owner>
            <span v-if="!isEditing" class="comment-manage-buttons">
                <img class="edit-container-pen" src="../assets/pen.png" v-if="commentDTO.canEdit" @click="setEdit()"/>
                <img class="remove-container-x" src="../assets/remove.png" v-if="commentDTO.canDelete" @click="doDelete()"/>
            </span>
        </div>

        <div v-if="!isEditing" class="comment-content">
            {{commentDTO.text}}
        </div>
        <comment-edit v-else :commentDTO="commentDTO"></comment-edit>
        <hr/>
    </div>
</template>

<script>
    import CommentEdit from './CommentEdit.vue'
    import bus, {COMMENT_CANCELED, COMMENT_UPDATED, COMMENT_DELETED} from '../bus'
    import {getPostId, getTimestampFromUtc} from '../utils'
    import Owner from './Owner.vue'

    export default {
        name: 'comment-item',
        props: ['commentDTO'], // it may be an object, for ability to set default values
        data() {
            return {
                isEditing: false,
            }
        },
        computed:{
            createDateTime(){
                return getTimestampFromUtc(this.commentDTO.createDateTime);
            }
        },
        methods: {
            setEdit(){
                this.isEditing = true;
            },
            resetEdit(){
                this.isEditing = false;
            },
            doDelete(){
                const commentId = this.commentDTO.id;
                this.$http.delete(`/api/post/${getPostId(this)}/comment/${commentId}`)
                    .then(successResponse => {
                        bus.$emit(COMMENT_DELETED, successResponse.body);
                    }, failResponce => {
                        console.error(failResponce);
                    })
            }
        },
        components:{
            CommentEdit,
            Owner
        },
        created(){
            bus.$on(COMMENT_CANCELED, this.resetEdit);
            bus.$on(COMMENT_UPDATED, this.resetEdit);
        },
        destroyed(){
            bus.$off(COMMENT_CANCELED, this.resetEdit);
            bus.$off(COMMENT_UPDATED, this.resetEdit);
        },
    };
</script>

<style lang="stylus">
    .comment {
        // border-width 1px
        // border-color black
        // border-style solid
        margin 2px;

        &-head{
            display flex
            flex-direction row
            justify-content space-between
            flex-wrap wrap

        }

        .comment-manage-buttons {
            margin-left: auto
            display flex
            flex-direction row
            align-items center

            img {
                margin 0 0.25em
            }

            img.edit-container-pen {
                height 16px;
                cursor pointer
            }
            img.remove-container-x {
                height 16px;
                cursor pointer
            }
            img.remove-container-x:hover {
                transition: 0.2s all;
                box-shadow: 0 0 2em red;
            }
        }
    }
</style>