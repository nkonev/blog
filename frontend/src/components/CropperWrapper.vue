<template>
    <div class="cropper-wrapper">
        <croppa v-model="myCroppa"
                :width="cropperWidth"
                :height="cropperHeight"
                :remove-button-size="cropperRemoveButtonSize"

                :file-size-limit="fileSizeLimit"
                :show-loading="true"
                :placeholder="internalPlaceholder"
                :initial-image="initialImageInternal()"
                :placeholder-font-size="internalPlaceholderTextSize"
                :disabled="false"
                :prevent-white-space="false"
                :show-remove-button="true"
                accept="image/*"
                @file-choose="handleCroppaFileChoose"
                @draw="handleDraw"
                @image-remove="handleCroppaImageRemove"
                @file-size-exceed="handleCroppaFileSizeExceed"
                @file-type-mismatch="handleCroppaFileTypeMismatch"

                @move="handleImageChanged"
                @zoom="handleImageChanged"
        >
        </croppa >
    </div>
</template>

<script>
    import 'vue-croppa/dist/vue-croppa.css'
    import Croppa from 'vue-croppa'
    import {UPLOAD_FILE_SIZE_LIMIT} from '../constants'
    import debounce from "lodash/debounce";
    import once from "lodash/once";

    const EVENT_IMAGE_CHANGED = 'imageChangedEvent';

    export default {
        props : [
            'initialImage',
            'width',
            'height',
            'removeButtonSize',
            'placeholder',
            'placeholderTextSize'
        ],
        components: {
            'croppa': Croppa.component,
        },
        data() {
            return {
                removeImage: false,
                imageChanged: false,
                myCroppa: {},
                imageContentType: null,
            }
        },
        computed: {
            fileSizeLimit(){
                return UPLOAD_FILE_SIZE_LIMIT;
            },
            cropperWidth(){
                return this.width
            },
            cropperHeight(){
                return this.height
            },
            cropperRemoveButtonSize(){
                return this.removeButtonSize
            },
            internalPlaceholder() {
                return this.placeholder
            },
            internalPlaceholderTextSize() {
                return this.placeholderTextSize
            }
        },
        created() {
            this.handleDraw = debounce(this.handleDraw, 400);
            this.initialImageInternal = once(this.initialImageInternal);
        },
        methods: {
            initialImageInternal(){
                let url;
                if (!this.initialImage) {
                    url = this.initialImage;
                } else if (typeof this.initialImage === "function") {
                    url = this.initialImage();
                } else if (typeof this.initialImage === "string") {
                    url = this.initialImage;
                } else {
                    throw "Allowed string or function for prop initialImage"
                }
                console.log("Initial image", url);
                if (url) {
                    this.$http.head(url).then(resp => {
                        // once set imageContentType for able to change image
                        let raw = resp.headers.get("content-type");
                        if (raw) {
                            let arr = raw.split(";");
                            raw = arr[0];
                        }
                        this.imageContentType = raw;
                        console.log("Initial image content-type", this.imageContentType);
                    });
                }
                return url;
            },
            handleDraw(){
                const dataUrl = document.querySelector(".cropper-wrapper canvas").toDataURL();
                this.$emit(EVENT_IMAGE_CHANGED, dataUrl);
            },
            handleImageChanged(){
                this.$data.imageChanged = true;
                console.debug('image changed', this.$data.imageChanged);
            },
            handleCroppaFileChoose(e){
                this.removeImage = false;
                this.imageContentType = e.type;
                this.handleImageChanged();
                console.debug('image chosen', e);
            },
            handleCroppaImageRemove(){
                console.debug('image removed');
                this.removeImage = true;
                this.imageContentType = null;
                this.$emit(EVENT_IMAGE_CHANGED, null);
                //this.handleImageChanged();
            },
            handleCroppaFileSizeExceed(){
                alert(`Image size must be < than ${UPLOAD_FILE_SIZE_LIMIT} bytes`);
            },
            handleCroppaFileTypeMismatch(){
                alert('Image wrong type');
            },
            /**
             *
             * @param next function to call after blob will created.
             */
            onCreateBlob(next){
                if (this.$data.imageChanged) {
                    console.debug("Invoking next() with blob of type", this.imageContentType);
                    this.myCroppa.promisedBlob(this.imageContentType).then(blob => {
                        next(blob);
                    });
                } else {
                    console.debug("Invoking next() without blob");
                    next();
                }
            },
            isRemoveImage(){
                return this.removeImage;
            }
        },
    }
</script>


<style lang="stylus" scoped>

    .cropper-wrapper {
        text-align center
    }

</style>