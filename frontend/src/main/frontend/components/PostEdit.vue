<template>
    <div>
        <input v-model="postDTO.title"/>

        <!-- bidirectional data binding -->
        <quill-editor v-model="postDTO.text"
                      ref="myQuillEditor"
                      :options="editorOption"
                      @blur="onEditorBlur($event)"
                      @focus="onEditorFocus($event)"
                      @ready="onEditorReady($event)">
        </quill-editor>

        <div class="command-buttons">
            <div class="send">
                <spinner v-if="submitting" class="send-spinner" :line-size="10" :spacing="20" :speed="0.4" size="55" :font-size="20" message="Sending..."></spinner>
                <button v-if="!submitting" class="save-btn" @click="onBtnSave" v-bind:disabled="hasInvalidText()">Сохранить</button>
            </div>
            <button v-if="!submitting" class="save-btn" @click="onBtnCancel">Отмена</button>
        </div>
    </div>
</template>

<script>
    import Vue from 'vue'
    import { quillEditor } from 'vue-quill-editor'
    import Spinner from 'vue-simple-spinner'  // https://github.com/dzwillia/vue-simple-spinner/blob/master/examples-src/App.vue

    const MIN_LENGTH = 10;

    /**
     * removes html tags
     * @param html
     * @return {string|string}
     */
    function strip(html) {
        const tmp = document.implementation.createHTMLDocument("New").body;
        tmp.innerHTML = html;
        return tmp.textContent || tmp.innerText || "";
    }

    // https://quilljs.com/docs/modules/toolbar/
    const toolbarOptions = [
        ['bold', 'italic', 'underline', 'strike'],        // toggled buttons
        ['link', 'image'],
        ['blockquote', 'code-block'],

        [{ 'header': 1 }, { 'header': 2 }],               // custom button values
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
        [{ 'script': 'sub'}, { 'script': 'super' }],      // superscript/subscript
        [{ 'direction': 'rtl' }],                         // text direction

        [{ 'header': [1, 2, 3, 4, false] }],

        [{ 'color': [] }, { 'background': [] }],          // dropdown with defaults from theme
        [{ 'font': [] }],
        [{ 'align': [] }],

        ['clean']                                         // remove formatting button
    ];

    export default {
        props : [
            'postDTO'
        ],
        data () {
            return {
                submitting: false,
                // content: 'Input something',
                editorOption: {
                    modules: {
                        toolbar: toolbarOptions
                    }
                }
            }
        },
        methods: {
            onEditorBlur(editor) {
                console.debug('editor blur!')
            },
            onEditorFocus(editor) {
                console.debug('editor focus!')
            },
            onEditorReady(editor) {
                console.debug('editor ready!')
            },
            onBtnSave() {
                const self = this;
                console.log('start submitting');
                this.submitting = true;
                const quillInstance = self.$refs.myQuillEditor.quill;
                quillInstance.enable(false);

//                setTimeout(()=>{
//                    self.submitting = false;
//                    quillInstance.enable(true);
//                    console.log('end submitting');
//                }, 4000);

                this.$http.put('/api/post', this.postDTO, {}).then(response => {
                    self.submitting = false;
                    quillInstance.enable(true);
                    console.log('end submitting');
                    this.$parent.afterSubmit();
                }, response => {
                    // error callback
                    // alert('Booh! Wrong credentials, try again!');
                    console.error("Error on send post", response);
                });
            },
            onBtnCancel() {
                this.$parent.cancel();
            },
            hasInvalidText() {
                return strip(this.postDTO.text).length < MIN_LENGTH;
            }
        },
        components: {
            quillEditor,
            Spinner
        },
        watch: {
            //content(html) {
            //    console.debug("PostEdit", html);
            //    this.$parent.setText(html);
            //},
        }
    }
</script>

<style lang="stylus" scoped>
    $signin_color= #00cf6b;
    @import "../buttons.styl"

    .command-buttons {

        .send {
            display inline

            .send-spinner {
                margin: 2px;
            }

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