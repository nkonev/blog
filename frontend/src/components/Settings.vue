<template>
    <div>
        <template v-if="configLoading">
            <blog-spinner message="Loading config..."></blog-spinner>
        </template>
        <template v-else>
            <h1>Header</h1>
            <input class="title-edit" placeholder="Header title" type="text" autofocus v-model="header"/>

            <h1>Title template</h1>
            <input class="title-edit" placeholder="Title Template" type="text" v-model="titleTemplate"/>

            <h1>Background image</h1>
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
                        @init="handleCroppaInit"
                        @file-choose="handleCroppaFileChoose"
                        @image-remove="handleCroppaImageRemove"
                        @file-size-exceed="handleCroppaFileSizeExceed"
                        @file-type-mismatch="handleCroppaFileTypeMismatch"
                >
                </croppa >
            </div>

            <button v-if="!submitting" class="blog-btn ok-btn" @click="onBtnSave">Save</button>
            <blog-spinner v-if="submitting" message="Sending..."></blog-spinner>
        </template>
    </div>
</template>

<script>
    import BlogSpinner from './BlogSpinner.vue'
    import 'vue-croppa/dist/vue-croppa.css'
    import Croppa from 'vue-croppa'
    import {computedCropper} from "../utils";
    import {mapGetters} from 'vuex'
    import store, {GET_CONFIG, GET_HEADER, SET_HEADER, GET_TITLE_TEMPLATE, SET_TITLE_TEMPLATE, SET_CONFIG, GET_IMAGE_BACKGROUND, GET_CONFIG_LOADING, FETCH_CONFIG} from '../store'

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
            handleCroppaInit(){
                document.querySelector(".image-background-cropper canvas").style.border="dashed"
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

                const dtoContent = JSON.stringify({...this.storedConfigDto, removeImageBackground: this.removeImageBackground}); // содержимое нового файла...
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

<style lang="stylus">
    @import "../constants.styl"
    @import "../common.styl"

    .image-background-cropper {
        text-align center
    }

</style>