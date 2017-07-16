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