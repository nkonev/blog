<template>
    <span>
        <template v-if="isEditing">
            <h1>Editing, here will be slot</h1>
            <div class="command-buttons">
                <div class="send">
                    <blog-spinner v-if="submitting" message="Sending..."/>
                    <button v-if="!submitting" class="save-btn" @click="onBtnSave" v-bind:disabled="!isValid">Save</button>
                </div>
                <button v-if="!submitting" class="cancel-btn" @click="onBtnCancel">Cancel</button>
            </div>
        </template>
        <template v-else>
            <div class="vec-head">
                <h2>Slot head</h2>
                <span class="manage-buttons" v-if="!isLoading">
                    <img class="edit-container-pen" src="../assets/pen.png" v-if="canEdit" @click="setEdit()"/>
                    <img class="remove-container-x" src="../assets/remove.png" v-if="canDelete" @click="doDelete()"/>
                </span>
            </div>
            <template v-if="!isLoading">
                <h1>Normal content slot</h1>
            </template>
            <template v-else>
                <blog-spinner :message="loadingMessage"></blog-spinner>
            </template>

        </template>
    </span>
</template>

<script>
    import BlogSpinner from "./BlogSpinner.vue"
    export default {
        name: 'view-edit-cancel',
        data() {
            return {
                isEditing: false,
                isLoading: false,
                submitting: false,
            }
        },
        props:['loadingMessage', 'canEdit', 'canDelete', 'onDelete', 'onCancel', 'onSave', 'isValid'],
        components:{
            BlogSpinner
        },
        methods:{
            setEdit(){
                this.isEditing = true;
            },
            doDelete() {
                if (this.$props.onDelete){
                    this.$props.onDelete();
                }
            },
            cancel() {
                this.isEditing = false;
                this.submitting = false;
            },
            onBtnSave(){
                this.submitting = true;
                this.$props.onSave()
                    .then(success => {
                        this.cancel();
                    }).catch(error => {
                        this.submitting = false;
                    })
                ;
            },
            onBtnCancel(){
                this.cancel();
                if (this.$props.onCancel){
                    this.$props.onCancel();
                }
            }
        }
    }
</script>

<style lang="stylus">
    .vec-head {
        display flex
        flex-direction row
        justify-content space-between
        // flex-wrap wrap
        align-items: baseline

        .manage-buttons {
            img.edit-container-pen {
                height 32px;
                cursor pointer
            }
            img.remove-container-x {
                height 32px;
                cursor pointer
            }
        }
    }

    .command-buttons {
        .send {
            display inline

            .save-btn {
                height 32px
                min-width 64px
                border-radius 2px;
                border-width 1px;
                background: white;
                color: $signin_color;
                &:hover:enabled {
                    border-color: $signin_color;
                    background: cornflowerblue;
                    color: white;
                }
                &:disabled{
                    color: red
                }
                &:hover:disabled {
                    background: red;
                    color: white;
                }
            }
        }
    }

</style>