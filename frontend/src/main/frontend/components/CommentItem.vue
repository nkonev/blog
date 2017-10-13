<template>
    <div class="comment">
        <div class="user-info">
            <img :src="commentDTO.owner.avatar">
            <span>{{commentDTO.owner.login}}</span>
        </div>

        <div v-if="!isEditing">
            <span class="comment-manage-buttons">
                <img class="edit-container-pen" src="../assets/pen.png" v-if="commentDTO.canEdit" @click="setEdit()"/>
                <img class="remove-container-x" src="../assets/remove.png" v-if="commentDTO.canDelete" @click="doDelete()"/>
            </span>

            <div class="comment-content">
                {{commentDTO.text}}
            </div>
        </div>
        <comment-edit v-else :commentDTO="commentDTO"></comment-edit>
        <hr/>
    </div>
</template>

<script>
    import CommentEdit from './CommentEdit.vue'
    import bus, {COMMENT_CANCELED, COMMENT_UPDATED} from '../bus'

    export default {
        name: 'comment-item',
        props: ['commentDTO'], // it may be an object, for ability to set default values
        data() {
            return {
                isEditing: false,
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
                console.log('delete');
            }
        },
        components:{
            CommentEdit
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

        .user-info {
            img {
                max-width 48px;
                max-height 48px;
            }
        }

        .comment-manage-buttons {
            img.edit-container-pen {
                height 16px;
                cursor pointer
            }
            img.remove-container-x {
                height 16px;
                cursor pointer
            }
        }
    }
</style>
