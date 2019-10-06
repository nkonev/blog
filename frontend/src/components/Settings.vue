<template>
    <div>
        <template v-if="configLoading">
            <blog-spinner message="Loading config..."></blog-spinner>
        </template>
        <div class="settings" v-else>
            <h1>Header</h1>
            <input class="title-edit" placeholder="Header title" type="text" autofocus v-model="header"/>

            <h2>Sub header</h2>
            <input class="title-edit" placeholder="Sub header" type="text" v-model="subHeader"/>

            <h2>Title template</h2>
            <input class="title-edit" placeholder="Title Template" type="text" v-model="titleTemplate"/>

            <h2>Background image</h2>
            <!-- https://zhanziyang.github.io/vue-croppa/#/file-input -->
            <div class="image-background-cropper">
                <croppa v-model="myCroppa"
                        :width="cropperWidth"
                        :height="cropperHeight"
                        :remove-button-size="cropperRemoveButtonSize"

                        :file-size-limit="5 * 1024 * 1024"
                        :show-loading="true"
                        placeholder="Choose background image"
                        :initial-image="imageBackground"
                        :placeholder-font-size="32"
                        :disabled="false"
                        :prevent-white-space="false"
                        :show-remove-button="true"
                        accept="image/*"
                        @file-choose="handleCroppaFileChoose"
                        @loading-end="handleLoadingEnd"
                        @image-remove="handleCroppaImageRemove"
                        @file-size-exceed="handleCroppaFileSizeExceed"
                        @file-type-mismatch="handleCroppaFileTypeMismatch"
                >
                </croppa >
            </div>

            <button v-if="!submitting" class="blog-btn ok-btn" @click="onBtnSave">Save</button>
            <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
        </div>
    </div>
</template>

<script>
    import BlogSpinner from './BlogSpinner.vue'
    import 'vue-croppa/dist/vue-croppa.css'
    import Croppa from 'vue-croppa'
    import {computedCropper} from "../utils";
    import {mapGetters} from 'vuex'
    import store, {GET_CONFIG, GET_HEADER, SET_HEADER, GET_SUBHEADER, SET_SUBHEADER, GET_TITLE_TEMPLATE, SET_TITLE_TEMPLATE, SET_CONFIG, GET_IMAGE_BACKGROUND, SET_IMAGE_BACKGROUND, GET_CONFIG_LOADING, FETCH_CONFIG} from '../store'

    export default {
        components: {
            'croppa': Croppa.component,
            BlogSpinner,
        },
        data() {
            return {
                chosenFile: null,
                removeImageBackground: false,
                myCroppa: {},
                submitting: false,
            }
        },
        store,
        computed: {
            ...computedCropper,
            ...mapGetters({storedConfigDto: GET_CONFIG, configLoading: GET_CONFIG_LOADING}),
            header: {
                get () {
                    return this.$store.getters[GET_HEADER]
                },
                set (value) {
                    this.$store.commit(SET_HEADER, value)
                }
            },
            subHeader: {
                get () {
                    return this.$store.getters[GET_SUBHEADER]
                },
                set (value) {
                    this.$store.commit(SET_SUBHEADER, value)
                }
            },
            titleTemplate: {
                get () {
                    return this.$store.getters[GET_TITLE_TEMPLATE]
                },
                set (value) {
                    this.$store.commit(SET_TITLE_TEMPLATE, value)
                }
            },
            imageBackground(){
                return this.$store.getters[GET_IMAGE_BACKGROUND];
            }
        },
        methods: {
            handleLoadingEnd(){
                const dataUrl = document.querySelector(".image-background-cropper canvas").toDataURL();
                //console.debug('image chosen', dataUrl);
                this.$store.commit(SET_IMAGE_BACKGROUND, dataUrl);
            },
            handleCroppaFileChoose(e){
                this.removeImageBackground = false;
                console.debug('image chosen', e);
                this.$data.chosenFile = e;
            },
            handleCroppaImageRemove(){
                console.debug('image removed');
                this.removeImageBackground = true;
                this.$data.chosenFile = null;
                this.$store.commit(SET_IMAGE_BACKGROUND, null);
            },
            handleCroppaFileSizeExceed(){
                // see :file-size-limit
                alert('Image size must be < than 5 Mb');
            },
            handleCroppaFileTypeMismatch(){
                alert('Image wrong type');
            },
            onBtnSave(){
                console.debug('Start saving settings');
                this.startSending();

                const formData = new FormData();

                const objToPut = {...this.storedConfigDto, removeImageBackground: this.removeImageBackground};
                delete objToPut.imageBackground; //  don't pass unnecessary value
                const dtoContent = JSON.stringify(objToPut); // содержимое нового файла...
                const dtoBlob = new Blob([dtoContent], { type: "application/json"});
                formData.append('dto', dtoBlob);

                const putFunction = () => {
                    this.$http.put('/api/config', formData)
                        .then(successResp => {
                            console.log("successfully set config", successResp);
                            this.finishSending();
                            const newVal = successResp.body;
                            this.$store.commit(SET_CONFIG, newVal);
                            return newVal
                        }, failResp => {
                            console.error("Server fail", failResp);
                            throw "failed to upload title img"
                        })
                        .catch(e => {
                            console.log("Catch error in sending post with image", e);
                            this.finishSending();
                        })
                };

                if (this.$data.chosenFile) {
                    this.myCroppa.promisedBlob(this.$data.chosenFile.type).then(blob => {
                        formData.append('image', blob); // multipart part with name 'image'
                        putFunction();
                    });
                } else {
                    putFunction();
                }
            },
            startSending() {
                this.submitting = true;
            },
            finishSending() {
                this.submitting = false;
            },
        },
        mounted(){
            console.log("settings mounted");
        },
        beforeDestroy(){
            console.log("fetching new config on destroy settings");
            this.$store.dispatch(FETCH_CONFIG);
        }
    }
</script>

<style lang="stylus" scoped>
    @import "../constants.styl"
    @import "../common.styl"

    .image-background-cropper {
        text-align center
    }

    div.settings h1, h2 {
        margin-bottom 0
    }
</style>