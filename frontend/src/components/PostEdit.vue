<template>
    <div class="post-edit">
        <!--
            You shouldn't change parent postDTO here, if you do it you will have bugs
            1) your changes will reset on server 401 response
            2) your changes corrupts parent's data when you click Cancel button
            https://vuejs.org/v2/guide/components.html#Composing-Components
            see also created() hook
        -->

        <!-- https://zhanziyang.github.io/vue-croppa/#/file-input -->
        <div class="post-edit-cropper">
            <croppa v-model="myCroppa"
                    :width="cropperWidth"
                    :height="cropperHeight"
                    :remove-button-size="cropperRemoveButtonSize"
                    :file-size-limit="5 * 1024 * 1024"
                    :show-loading="true"
                    placeholder="Choose title image"
                    :initial-image="editPostDTO.titleImg"
                    :placeholder-font-size="32"
                    :disabled="false"
                    :prevent-white-space="true"
                    :show-remove-button="true"
                    @file-choose="handleCroppaFileChoose"
                    @image-remove="handleCroppaImageRemove"
                    @file-size-exceed="handleCroppaFileSizeExceed"
                    @file-type-mismatch="handleCroppaFileTypeMismatch"
                    >
            </croppa >
        </div>
        <input class="title" placeholder="Title of your megapost" type="text" autofocus v-model="editPostDTO.title"/>
        <quill-editor v-model="editPostDTO.text"
                      ref="myQuillEditor"
                      :options="editorOption"
                      @blur="onEditorBlur($event)"
                      @focus="onEditorFocus($event)"
                      @ready="onEditorReady($event)">
        </quill-editor>

        <div class="post-command-buttons">
            <div class="send">
                <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
                <button v-if="!submitting" class="blog-btn ok-btn" @click="onBtnSave" v-bind:disabled="!isPostValid()">Сохранить</button>
            </div>
            <button v-if="!submitting" class="blog-btn cancel-btn" @click="onBtnCancel">Отмена</button>
        </div>
    </div>
</template>

<script>
    import 'highlight.js/styles/monokai.css'
    import 'vue-croppa/dist/vue-croppa.css'
    import Vue from 'vue'
    import { quillEditor } from 'vue-quill-editor'
    import BlogSpinner from './BlogSpinner.vue'
    import {API_POST} from '../constants'
    import Croppa from 'vue-croppa'

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
                    theme: 'bubble',
                    modules: {
                        syntax: true,              // Include syntax module
                        toolbar: toolbarOptions,
                    },
                },
                editPostDTO: {}, // will be overriden below in created()
                myCroppa: {},
                chosenFile: null,
            }
        },
        mounted() {
            this.quillInstance = this.$refs.myQuillEditor.quill;


            /////////////////////////////////////////////////////////////////////////////
            //// https://github.com/quilljs/quill/issues/1400#issuecomment-318260547 ////
            /////////////////////////////////////////////////////////////////////////////
            /**
             * Step1. select local image
             *
             */
            const selectLocalImage = () => {
                const input = document.createElement('input');
                input.setAttribute('type', 'file');
                input.click();

                // Listen upload local image and save to server
                input.onchange = () => {
                    const file = input.files[0];

                    // file type is only image.
                    if (/^image\//.test(file.type)) {
                        saveToServer(file);
                    } else {
                        console.warn('You could only upload images.');
                    }
                };
            }

            /**
             * Step2. save to server
             *
             * @param {File} file
             */
            const saveToServer = (file) => {
                const fd = new FormData();
                fd.append('image', file);

                Vue.http.post('/api/image/post/content', fd)
                    .then(successResp => {
                        const url =  successResp.body.relativeUrl;
                        insertToEditor(url);
                    }, failResp => {
                        throw "failed to upload title img"
                    })
                    .catch(e => {
                        console.log("Catch error in sending post content image", e);
                    })

            }

            /**
             * Step3. insert image url to rich editor.
             *
             * @param {string} url
             */
            const insertToEditor = (url) => {
                // push image url to rich editor.
                const range = this.quillInstance.getSelection();
                this.quillInstance.insertEmbed(range.index, 'image', url);
            }


            this.quillInstance.getModule('toolbar').addHandler('image', () => {
                selectLocalImage();
            });
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
        computed: {
            cropperWidth(){
                return screen.width > 969 ? 800 : screen.width - 25
            },
            cropperHeight(){
                return screen.width > 969 ? 600 : this.cropperWidth;
            },
            cropperRemoveButtonSize(){
                return screen.width > 969 ? 30 : 45;
            },
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

                const sendPost = url => {
                    if (this.editPostDTO.id) {
                        // edit / update
                        if (url) {
                            this.editPostDTO.titleImg = url;
                        }
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
                        this.editPostDTO.titleImg = url;
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
                };

                if (this.$data.chosenFile) {
                    this.myCroppa.promisedBlob(this.$data.chosenFile.type).then(blob => {
                        const formData = new FormData();
                        formData.append('image', blob); // multipart part with name 'image'
                        this.$http.post('/api/image/post/title', formData)
                            .then(successResp => {
                                return successResp.body.relativeUrl
                            }, failResp => {
                                throw "failed to upload title img"
                            })
                            .then(sendPost)
                            .catch(e => {
                                console.log("Catch error in sending post with image");
                                this.finishSending();
                            })
                    });
                } else {
                    sendPost('');
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
                alert('Image size must be < than 5 Mb');
            },
            handleCroppaFileChoose(e) {
                console.debug('image chosen', e);
                this.$data.chosenFile = e;
            },
            handleCroppaImageRemove() {
                console.debug('image removed');
                this.$data.chosenFile = null;
            }
        },
        components: {
            quillEditor,
            BlogSpinner,
            'croppa': Croppa.component
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
    @import "../constants.styl"

    .post-edit {
        &-cropper {
            text-align center
        }
        .title {
            width 100%
            margin-left 0px
            margin-right 0px
            padding-right 0px
            padding-left 0.3em
            border-style hidden
            border-width 0px
            box-sizing: border-box;
            font-size $postTitleFontSize
            font-weight $postTitleFontWeight
            font-family $postTitleFontFamily
            color $postTitleColor
        }
        div.quill-editor {
            margin-top: 0.3em
            margin-bottom: 0.5em
        }
    }

    .post-command-buttons {
        .send {
            display inline
        }
    }
</style>

<style lang="stylus">
    @import "../constants.styl"
    .ql-editor > * {
        font-size $postBodyFontSize
        font-family $postBodyFontFamily

        margin-top $postBodyMarginTop
        margin-bottom $postBodyMarginBottom
    }
</style>