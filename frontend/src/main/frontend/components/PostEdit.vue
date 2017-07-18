<template>
    <div>
        <h1>Post editing</h1>

        <!-- bidirectional data binding -->
        <quill-editor v-model="content"
                      ref="myQuillEditor"
                      :options="editorOption"
                      @blur="onEditorBlur($event)"
                      @focus="onEditorFocus($event)"
                      @ready="onEditorReady($event)">
        </quill-editor>

        <div class="command-buttons">
            <button class="save-btn">Сохранить</button>
        </div>
    </div>
</template>

<script>
    import Vue from 'vue'
    import VueQuillEditor from 'vue-quill-editor'

    import { quillEditor } from 'vue-quill-editor'

    export default {
        data () {
            return {
                content: '<h2>I am Example</h2>',
                editorOption: {
                    // some quill options
                }
            }
        },
        methods: {
            onEditorBlur(editor) {
                console.log('editor blur!', editor)
            },
            onEditorFocus(editor) {
                console.log('editor focus!', editor)
            },
            onEditorReady(editor) {
                console.log('editor ready!', editor)
            }
        },
        components: {
            quillEditor
        },
        watch: {
            content:  function (val) {
                console.log(val);
            },
        }
    }
</script>

<style lang="stylus" scoped>
    $signin_color= #00cf6b;
    @import "../buttons.styl"

    .command-buttons {

        .save-btn {
            height 32px
            min-width 64px
            border-radius 2px;
            border-width 1px;
            background: white;
            color: $signin_color;
            &:hover {
                border-color: $signin_color;
                background: cornflowerblue;
                color: white;
            }
        }
    }

</style>