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

            <h2>Background color</h2>
            <color-picker v-model="backgroundColor" @cancel="onColorCancel" cancelLabel="Reset color" />

            <h2>Background image</h2>

            <CropperWrapper
                    ref="cropperInstance"
                    :initialImage="imageBackground"
                    :width="cropperWidth"
                    :height="cropperHeight"
                    :removeButtonSize="cropperRemoveButtonSize"
                    placeholder="Choose background image"
                    :placeholderTextSize="32"
                    @imageChangedEvent="onImageChanged"
            />

            <button v-if="!submitting" class="blog-btn ok-btn" @click="onBtnSave">Save</button>
            <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
        </div>
    </div>
</template>

<script>
    import BlogSpinner from './BlogSpinner.vue'
    import {computedCropper} from "../utils";
    import {mapGetters} from 'vuex'
    import store, {GET_CONFIG, GET_HEADER, SET_HEADER, GET_SUBHEADER, SET_SUBHEADER, GET_TITLE_TEMPLATE, SET_TITLE_TEMPLATE, SET_CONFIG, GET_IMAGE_BACKGROUND, SET_IMAGE_BACKGROUND, GET_BACKGROUND_COLOR, SET_BACKGROUND_COLOR, GET_CONFIG_LOADING, FETCH_CONFIG} from '../store'
    import { Photoshop } from 'vue-color'
    import CropperWrapper from "./CropperWrapper";

    export default {
        components: {
            BlogSpinner,
            'color-picker': Photoshop,
            CropperWrapper
        },
        data() {
            return {
                submitting: false,
            }
        },
        store,
        computed: {
            ...computedCropper, // cropperWidth, cropperHeight, cropperRemoveButtonSize, ...
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
            backgroundColor: {
                get () {
                    return {hex: this.$store.getters[GET_BACKGROUND_COLOR]}
                },
                set (value) {
                    this.$store.commit(SET_BACKGROUND_COLOR, value.hex)
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
        },
        created() {
        },
        methods: {
            imageBackground(){
                return this.$store.getters[GET_IMAGE_BACKGROUND];
            },
            onColorCancel(){
                this.$store.commit(SET_BACKGROUND_COLOR, null);
            },
            onImageChanged(dataUrl){
                this.$store.commit(SET_IMAGE_BACKGROUND, dataUrl);
            },
            onBtnSave(){
                this.$Progress.start();
                console.debug('Start saving settings');
                this.startSending();

                const formData = new FormData();

                const objToPut = {...this.storedConfigDto, removeImageBackground: this.$refs.cropperInstance.isRemoveImage()};
                delete objToPut.imageBackground; //  don't pass unnecessary value
                const dtoContent = JSON.stringify(objToPut);
                const dtoBlob = new Blob([dtoContent], { type: "application/json"});
                formData.append('dto', dtoBlob);

                const putFunction = (maybeBlob) => {
                    if (maybeBlob) {
                        formData.append('image', maybeBlob); // multipart part with name 'image'
                    }
                    this.$http.post('/api/config', formData)
                        .then(successResp => {
                            console.log("successfully set config", successResp);
                            this.finishSending();
                            const newVal = successResp.body;
                            this.$store.commit(SET_CONFIG, newVal);
                            this.$Progress.finish();
                            return newVal
                        }, failResp => {
                            console.error("Server fail", failResp);
                            this.$Progress.fail();
                            throw "failed to upload title img"
                        })
                        .catch(e => {
                            console.log("Catch error in sending post with image", e);
                            this.finishSending();
                        })
                };

                this.$refs.cropperInstance.onCreateBlob(putFunction);
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