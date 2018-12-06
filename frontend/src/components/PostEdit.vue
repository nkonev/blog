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
            <vueCropper
                    ref="cropper"
                    :img="option.img"
                    :outputSize="option.size"
                    :outputType="option.outputType"
                    :info="true"
                    :full="option.full"
                    :canMove="option.canMove"
                    :canMoveBox="option.canMoveBox"
                    :fixedBox="option.fixedBox"
                    :original="option.original"
                    :autoCrop="option.autoCrop"
                    :autoCropWidth="option.autoCropWidth"
                    :autoCropHeight="option.autoCropHeight"
                    :centerBox="option.centerBox"
                    :high="option.high"
                    :infoTrue="option.infoTrue"
            ></vueCropper>
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
        </div>
    </div>
</template>

<script>
    import VueEditor from '../lib/VueEditor.vue'
    import Vue from 'vue'
    import BlogSpinner from './BlogSpinner.vue'
    import {API_POST} from '../constants'
    import {VueCropper} from 'vue-cropper'
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

    // https://quilljs.com/docs/modules/toolbar/
    const toolbarOptions = [
        ['bold', 'italic', 'underline', 'strike'],        // toggled buttons
        ['link', 'image'],
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
                option: {
                    img: "https://nkonev.name/api/image/settings/000e3920-4e79-4424-9277-affc965050aa.png",
                    size: 1,
                    full: false,
                    outputType: "png",
                    canMove: true,
                    fixedBox: false,
                    original: false,
                    canMoveBox: true,
                    autoCrop: true,
                    // 只有自动截图开启 宽度高度才生效
                    autoCropWidth: 200,
                    autoCropHeight: 150,
                    centerBox: false,
                    high: true,
                    cropData: {},
                    enlarge: 1
                },
                chosenFile: null,
            }
        },
        methods: {
            startSending() {
                this.submitting = true;
            },
            finishSending() {
                this.submitting = false;
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
            VueCropper
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
    @import "../common.styl"
    .post-edit {
        &-cropper {
            text-align center
            height 300px
            width 300px
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
    }
</style>

<style lang="stylus">
    @import "../constants.styl"

    .ql-editor {
        font-size $postBodyFontSize
        font-family $postBodyFontFamily

        margin-top $postBodyMarginTop
        margin-bottom $postBodyMarginBottom
    }
</style>


