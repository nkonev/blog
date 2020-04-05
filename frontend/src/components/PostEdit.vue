<template>
    <div class="post-edit">
        <!--
            You shouldn't change parent postDTO here, if you do it you will have bugs
            1) your changes will reset on server 401 response
            2) your changes corrupts parent's data when you click Cancel button
            https://vuejs.org/v2/guide/components.html#Composing-Components
            see also created() hook
        -->

        <CropperWrapper
                ref="cropperInstance"
                :initialImage="editPostDTO.titleImg"
                :width="cropperWidth"
                :height="cropperHeight"
                :removeButtonSize="cropperRemoveButtonSize"
                placeholder="Choose title image"
                :placeholderTextSize="32"
        />

        <input class="title-edit" placeholder="Title of your megapost" type="text" autofocus v-model="editPostDTO.title"/>
        <vue-editor id="editor"
                    :editorOptions="editorOptions"
                    :placeholder="computePlaceholder()"
                    useCustomImageHandler
                    @imageAdded="handleImageAdded" v-model="editPostDTO.text"
                    @input="onInput"
        >
        </vue-editor>


        <div class="post-command-buttons">
            <div class="send">
                <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
                <button v-if="!submitting" class="blog-btn ok-btn" @click="onBtnSave" v-bind:disabled="!isPostValid()">Save</button>
            </div>
            <button v-if="!submitting" class="blog-btn cancel-btn" @click="onBtnCancel">Cancel</button>

            <div class="draft">
                <input type="checkbox" id="draft" name="draft" v-model="editPostDTO.draft">
                <label for="draft">[Draft]</label>
            </div>
            <error v-if="errorMessage" :message="errorMessage"></error>
        </div>
    </div>
</template>

<script>
    import VueEditor from '../lib/VueEditor.vue'
    import Vue from 'vue'
    import BlogSpinner from './BlogSpinner.vue'
    import {API_POST} from '../constants'
    import {computedCropper} from "../utils";
    import CropperWrapper from "./CropperWrapper";
    import Error from './Error.vue'
    import "quill/dist/quill.snow.css";

    const MIN_LENGTH = 1;

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
        ['link', 'image', 'video'],
        ['blockquote', 'code-block'],

        [{ 'header': 1 }, { 'header': 2 }],               // custom button values
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],

        [{ 'header': [1, 2, 3, false] }],

        [{ 'color': [] }, { 'background': [] }],          // dropdown with defaults from theme
        [{ 'align': [] }],
        ['clean']                                         // remove formatting button
    ];

    export default {
        props : [
            'postDTO', 'onAfterSubmit', 'onCancel', 'onError', 'hljs'
        ],
        data () {
            return {
                errorMessage: '',
                submitting: false,
                editorOptions: {
                    theme: 'snow',
                    modules: {
                        // https://github.com/surmon-china/vue-quill-editor/issues/39#issuecomment-353725408
                        syntax: {
                            highlight: text => this.$props.hljs.highlightAuto(text).value
                        },
                        toolbar: toolbarOptions,
                    }
                },
                editPostDTO: {}, // will be overriden below in created()
            }
        },
        computed: computedCropper,
        methods: {
            startSending() {
                this.submitting = true;
            },
            finishSending() {
                this.submitting = false;
            },
            onBtnSave() {
                this.startSending();

                const sendPost = () => {
                    if (this.editPostDTO.id) {
                        // edit / update
                        this.$http.put(API_POST, {...this.editPostDTO, removeTitleImage: this.$refs.cropperInstance.isRemoveImage()}).then(response => {
                            this.finishSending();
                            if (this.$props.onAfterSubmit){
                                this.$props.onAfterSubmit(response.body);
                            }
                        }, response => {
                            console.error("Error on edit post", response);
                            this.errorMessage = response.body;
                            this.finishSending();
                        });
                    } else {
                        // create
                        this.$http.post(API_POST, {...this.editPostDTO, removeTitleImage: this.$refs.cropperInstance.isRemoveImage()}).then(response => {
                            this.finishSending();
                            if (this.$props.onAfterSubmit){
                                this.$props.onAfterSubmit(response.body);
                            }
                        }, response => {
                            console.error("Error on add post", response);
                            this.errorMessage = response.body;
                            this.finishSending();
                        });
                    }
                };

                const sendTitleImage = (maybeBlob) => {
                    if(maybeBlob){
                        const formData = new FormData();
                        formData.append('image', maybeBlob); // multipart part with name 'image'
                        return Vue.http.post('/api/image/post/title', formData);
                    } else {
                        return Promise.resolve(true); // empty promise
                    }
                };

                const composition = (maybeBlob) => {
                    sendTitleImage(maybeBlob)
                        .then(successResp => {
                            if(successResp.body) {
                                this.editPostDTO.titleImg = successResp.body.relativeUrl;
                            }
                        })
                        .then(sendPost)
                        .catch(e => {
                            console.log("Catch error in sending post with image", e);
                            this.finishSending();
                        })
                };

                this.$refs.cropperInstance.onCreateBlob(composition);
            },
            onBtnCancel() {
                if (this.$props.onCancel){
                    this.$props.onCancel();
                }
            },
            isPostValid() {
               return this.hasValidText() && this.editPostDTO.title
            },
            onInput() {
                let v = this.isPostValid();
                if (v) {
                    this.errorMessage = null;
                }
            },
            hasValidText() {
                return !(strip(this.editPostDTO.text).length < MIN_LENGTH);
            },
            computePlaceholder(){
                return "Lorem ipsum dolor sit amet..."
            },
            handleImageAdded: function(file, Editor, cursorLocation) {
                var formData = new FormData();
                formData.append('image', file);

                this.$http.post('/api/image/post/content', formData)
                    .then((result) => {
                        let url = result.data.relativeUrl; // Get url from response
                        console.log("got url", url);
                        Editor.insertEmbed(cursorLocation, 'image', url);
                    })
                    .catch((err) => {
                        console.log(err);
                    })
            },
        },
        components: {
            VueEditor,
            BlogSpinner,
            CropperWrapper,
            Error,
        },
        created(){
            this.editPostDTO = Vue.util.extend({}, this.postDTO); // deep copy prevent parent postDTO mutations
        }
    }
</script>


<style lang="stylus" scoped>
    @import "../constants.styl"
    @import "../common.styl"
    .post-edit {
        &-cropper {
            text-align center
        }
        div.quill-editor {
            margin-top: 0.3em
            margin-bottom: 0.5em
        }
    }

    .post-command-buttons {
        display flex
        flex-direction row

        .send {
            display inline
        }

        div.draft {
            display flex
            align-items center
        }
    }
</style>

<style lang="stylus">
    @import "../constants.styl"
    @import "../lib/QuillVideo.styl"

    .post-edit {
        .ql-editor {
            font-size $postBodyFontSize
            font-family $postBodyFontFamily

            margin-top $postBodyMarginTop
            margin-bottom $postBodyMarginBottom

            height: 480px
        }
    }

</style>


