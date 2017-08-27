<template>
    <div>
        <!--
            You shouldn't change parent postDTO here, if you do it you will have bugs
            1) your changes will reset on server 401 response
            2) your changes corrupts parent's data when you click Cancel button
            https://vuejs.org/v2/guide/components.html#Composing-Components
            see also created() hook
        -->

        <!-- https://zhanziyang.github.io/vue-croppa/#/file-input -->
        <div>
            <croppa v-model="myCroppa"
                    :width="400"
                    :height="250"
                    :file-size-limit="1 * 1024 * 1024"
                    placeholder="Choose title image"
                    :initial-image="editPostDTO.titleImg"
                    :placeholder-font-size="0"
                    :disabled="false"
                    :prevent-white-space="true"
                    :show-remove-button="true"
                    @file-choose="handleCroppaFileChoose"
                    @file-size-exceed="handleCroppaFileSizeExceed"
                    @file-type-mismatch="handleCroppaFileTypeMismatch"
                    >
            </croppa >
        </div>
        <input class="title" placeholder="Title of your megapost" v-model="editPostDTO.title"/>
        <quill-editor v-model="editPostDTO.text"
                      ref="myQuillEditor"
                      :options="editorOption"
                      @blur="onEditorBlur($event)"
                      @focus="onEditorFocus($event)"
                      @ready="onEditorReady($event)">
        </quill-editor>

        <div class="command-buttons">
            <div class="send">
                <spinner v-if="submitting" class="send-spinner" :line-size="10" :spacing="20" :speed="0.4" size="55" :font-size="20" message="Sending..."></spinner>
                <button v-if="!submitting" class="save-btn" @click="onBtnSave" v-bind:disabled="!isPostValid()">Сохранить</button>
            </div>
            <button v-if="!submitting" class="save-btn" @click="onBtnCancel">Отмена</button>
        </div>
    </div>
</template>

<script>
    import 'vue-croppa/dist/vue-croppa.css'
    import Vue from 'vue'
    import { quillEditor } from 'vue-quill-editor'
    import Spinner from 'vue-simple-spinner'  // https://github.com/dzwillia/vue-simple-spinner/blob/master/examples-src/App.vue
    import {API_POST} from '../constants'
    import Croppa from 'vue-croppa'

    Vue.use(Croppa);

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

        [{ 'header': [1, 2, 3, false] }],

        [{ 'color': [] }, { 'background': [] }],          // dropdown with defaults from theme

        ['clean']                                         // remove formatting button
    ];

    export default {
        props : [
            'postDTO', 'onAfterSubmit', 'onCancel', 'onError'
        ],
        data () {
            return {
                submitting: false,
                editorOption: {
                    modules: {
                        toolbar: toolbarOptions,
                    }
                },
                editPostDTO: {}, // will be overriden below in created()
                myCroppa: {}
            }
        },
        mounted() {
            this.quillInstance = this.$refs.myQuillEditor.quill;
            /**
             * https://quilljs.com/playground/#class-vs-inline-style
             *   Quill uses classes for most inline styles.
             The exception is background and color,
             where it uses inline styles.
             This demo shows how to change this.
             */
            // https://quilljs.com/guides/how-to-customize-quill/#content-and-formatting
            // warning: "// if you need register quill modules, you need to introduce and register before the vue program is instantiated" @ https://github.com/surmon-china/vue-quill-editor
        },
        beforeDestroy(){
            this.quillInstance = null;
        },
        methods: {
            onEditorBlur(editor) {
                // console.debug('editor blur!')
            },
            onEditorFocus(editor) {
                // console.debug('editor focus!')
            },
            onEditorReady(editor) {
                // console.debug('editor ready!')
            },
            startSending() {
                // console.log('start submitting', this.editPostDTO.title, this.editPostDTO.text);
                this.submitting = true;
                this.quillInstance.enable(false);
            },
            finishSending() {
                this.submitting = false;
                this.quillInstance.enable(true);
                // console.log('end submitting', this.editPostDTO.title, this.editPostDTO.text);
            },
            onBtnSave() {
                this.startSending();
                const dataUrl = this.myCroppa.generateDataUrl();
                this.editPostDTO.titleImg = dataUrl;
                // console.debug('titleImg=', this.editPostDTO.titleImg);

                if (this.editPostDTO.id) {
                    // edit / update
                    this.$http.put(API_POST, this.editPostDTO, {}).then(response => {
                        this.finishSending();
                        if (this.$props.onAfterSubmit){
                            this.$props.onAfterSubmit(response.body);
                        }
                    }, response => {
                        console.error("Error on edit post", response);
                        this.finishSending();
                    });
                } else {
                    // create
                    this.$http.post(API_POST, this.editPostDTO, {}).then(response => {
                        this.finishSending();
                        if (this.$props.onAfterSubmit){
                            this.$props.onAfterSubmit(response.body);
                        }
                    }, response => {
                        console.error("Error on add post", response);
                        this.finishSending();
                    });
                }
            },
            onBtnCancel() {
                if (this.$props.onCancel){
                    this.$props.onCancel();
                }
            },
            isPostValid() {
               return this.hasValidText() && this.editPostDTO.title
            },
            hasValidText() {
                return !(strip(this.editPostDTO.text).length < MIN_LENGTH);
            },
            handleCroppaFileTypeMismatch() {
                alert('Image wrong type');
            },
            handleCroppaFileSizeExceed() {
                // see :file-size-limit
                alert('Image size must be < than 1 Mb');
            },
            handleCroppaFileChoose(e) {
                console.debug('image chosen');
            }
        },
        components: {
            quillEditor,
            Spinner
        },
        watch: {
            'editPostDTO': {
                handler: function (val, oldVal) {
                    // console.log("PostEdit changing editPostDTO", val.title, val.text);
                },
                deep: true
            }
        },
        created(){
            this.editPostDTO = Vue.util.extend({}, this.postDTO); // deep copy prevent parent postDTO mutations
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