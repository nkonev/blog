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
                    :prevent-white-space="false"
                    :show-remove-button="true"
                    accept="image/*"
                    @init="handleCroppaInit"
                    @file-choose="handleCroppaFileChoose"
                    @image-remove="handleCroppaImageRemove"
                    @file-size-exceed="handleCroppaFileSizeExceed"
                    @file-type-mismatch="handleCroppaFileTypeMismatch"
                    >
            </croppa >
        </div>
        <input class="title-edit" placeholder="Title of your megapost" type="text" autofocus v-model="editPostDTO.title"/>
        <vue-editor id="editor"
                    :editorOptions="editorOptions"
                    :placeholder="computePlaceholder()"
                    useCustomImageHandler
                    @imageAdded="handleImageAdded" v-model="editPostDTO.text"
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
        </div>
    </div>
</template>

<script>
    import VueEditor from '../lib/VueEditor.vue'
    import 'vue-croppa/dist/vue-croppa.css'
    import Vue from 'vue'
    import BlogSpinner from './BlogSpinner.vue'
    import {API_POST} from '../constants'
    import Croppa from 'vue-croppa'
    import {isLargeScreen, computedCropper} from "../utils";
    if (isLargeScreen()) {
        require("quill/dist/quill.bubble.css");
    } else {
        require("quill/dist/quill.snow.css");
    }

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

    const localStorageKey = 'postDto';

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
            'postDTO', 'onAfterSubmit', 'onCancel', 'onError'
        ],
        data () {
            return {
                submitting: false,
                editorOptions: {
                    theme: isLargeScreen() ? 'bubble' : 'snow',
                    modules: {
                        syntax: false,
                        toolbar: toolbarOptions,
                    }
                },
                editPostDTO: {}, // will be overriden below in created()
                myCroppa: {},
                chosenFile: null,
            }
        },
        computed: computedCropper,
        methods: {
            startSending() {
                this.submitting = true;
            },
            finishSending() {
                this.submitting = false;
                localStorage.removeItem(localStorageKey);
            },
            onBtnSave() {
                this.startSending();

                const sendPost = () => {
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
                };

                if (this.$data.chosenFile) {
                    this.myCroppa.promisedBlob(this.$data.chosenFile.type).then(blob => {
                        const formData = new FormData();
                        formData.append('image', blob); // multipart part with name 'image'
                        this.$http.post('/api/image/post/title', formData)
                            .then(successResp => {
                                this.editPostDTO.titleImg = successResp.body.relativeUrl;
                                return
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
                    sendPost();
                }
            },
            onBtnCancel() {
                localStorage.removeItem(localStorageKey);
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
                this.editPostDTO.removeTitleImage = false;
                console.debug('image chosen', e);
                this.$data.chosenFile = e;
            },
            handleCroppaInit(e){
                document.querySelector(".post-edit-cropper canvas").style.border="dashed"
            },
            handleCroppaImageRemove() {
                this.editPostDTO.removeTitleImage = true;
                console.debug('image removed');
                this.$data.chosenFile = null;
            },
            computePlaceholder(){
                return "Lorem ipsum dolor sit amet..."
            },
            handleImageAdded: function(file, Editor, cursorLocation) {
                // An example of using FormData
                // NOTE: Your key could be different such as:
                // formData.append('file', file)

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
            }

        },
        components: {
            VueEditor,
            BlogSpinner,
            'croppa': Croppa.component
        },
        watch: {
            'editPostDTO': {
                handler: function (val, oldVal) {
                    // console.log("PostEdit changing editPostDTO", val.title, val.text);
                    const parsed = JSON.stringify(this.editPostDTO);
                    localStorage.setItem(localStorageKey, parsed);
                },
                deep: true
            }
        },
        mounted(){
            if (localStorage.getItem(localStorageKey)) {
                try {
                    this.editPostDTO = JSON.parse(localStorage.getItem(localStorageKey));
                } catch(e) {
                    console.error("Exception during parsing localstorage value - will delete this value", e);
                    localStorage.removeItem(localStorageKey);
                }
            }
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

    .ql-editor {
        font-size $postBodyFontSize
        font-family $postBodyFontFamily

        margin-top $postBodyMarginTop
        margin-bottom $postBodyMarginBottom
        overflow: visible
    }
</style>


