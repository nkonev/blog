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
        <input class="title" placeholder="Title of your megapost" type="text" autofocus v-model="editPostDTO.title"/>

        <div class="post-content">
            <medium-editor :text='editPostDTO.text' :options='options' custom-tag='div' v-on:edit='processEditOperation'/>
        </div>

        <div class="post-command-buttons">
            <div class="send">
                <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
                <button v-if="!submitting" class="blog-btn ok-btn" @click="onBtnSave" v-bind:disabled="!isPostValid()">Save</button>
            </div>
            <button v-if="!submitting" class="blog-btn cancel-btn" @click="onBtnCancel">Cancel</button>
        </div>
    </div>
</template>

<script>
    import 'vue-croppa/dist/vue-croppa.css'
    import Vue from 'vue'
    import BlogSpinner from './BlogSpinner.vue'
    import {API_POST} from '../constants'
    import Croppa from 'vue-croppa'
    import editor from 'vue2-medium-editor'
    import ImageUploadExtension from './image-upload'

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

    const MediumEditor = editor.MediumEditor;

    const uploadFunction = file => {
        console.log("insertImageFile");

        const formData = new FormData();
        formData.append('image', file);

        Vue.http.post('/api/image/post/content', formData)
            .then((result) => {
                const addImageElement = document.createElement('img');

                const url = result.data.relativeUrl; // Get url from response
                console.log("got url", url);
                addImageElement.src = url;

                MediumEditor.util.insertHTMLCommand(document, addImageElement.outerHTML);
            })
            .catch((err) => {
                console.log(err);
            })
    };

    const options = {
        toolbar: {
            buttons: [
                'bold', 'italic', 'underline', 'anchor', 'quote', 'h1', 'h2', 'h3', 'strikethrough',
                'pre', 'orderedlist', 'unorderedlist',
                'justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull',
                'removeFormat'
            ],
        },
        spellcheck: false,
        imageDragging: false,
        extensions: {
            'imageDragging': {},
            'fileDragging': {},
            'image-upload': new (ImageUploadExtension(MediumEditor, uploadFunction)),
        },
    };

    export default {
        props : [
            'postDTO', 'onAfterSubmit', 'onCancel', 'onError'
        ],
        data () {
            return {
                submitting: false,
                editPostDTO: {}, // will be overriden below in created()
                myCroppa: {},
                chosenFile: null,
                options: options
            }
        },
        mounted() {
//            this.quillInstance = this.$refs.myQuillEditor.quill;


        },
        beforeDestroy(){
//            this.quillInstance = null;
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
            startSending() {
                this.submitting = true;
            },
            finishSending() {
                this.submitting = false;
            },
            processEditOperation: function (operation) {
                this.editPostDTO.text = operation.api.origElements.innerHTML
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
            handleCroppaInit(e){
                document.querySelector(".post-edit-cropper canvas").style.border="dashed"
            },
            handleCroppaImageRemove() {
                console.debug('image removed');
                this.$data.chosenFile = null;
            },
            computePlaceholder(){
                return screen.width > 969 ? "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi." : "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat."
            },
        },
        components: {
            BlogSpinner,
            'croppa': Croppa.component,
            'medium-editor': editor
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

            font-size 2em
            font-weight $postTitleFontWeight
            font-family $postTitleFontFamily
            color $titleColor
            background $titleBackground
        }
    }

    .post-command-buttons {
        .send {
            display inline
        }
    }
</style>

<style lang="stylus">
    @import './post-content.styl'
</style>

<style lang="css" scoped>
    @import "~medium-editor/dist/css/medium-editor.css";
    @import "~medium-editor/dist/css/themes/default.css";
</style>