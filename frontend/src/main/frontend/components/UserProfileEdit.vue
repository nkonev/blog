<template>
    <div class="profile">
        <div>
            Editing profile
        </div>
        <div class="user-info">
            <input v-model="editProfileDTO.login"/>
            <croppa v-model="myCroppa"
                    :width="200"
                    :height="200"
                    :file-size-limit="1 * 1024 * 1024"
                    placeholder="Choose avatar image"
                    :initial-image="editProfileDTO.avatar"
                    :placeholder-font-size="0"
                    :disabled="false"
                    :prevent-white-space="true"
                    :show-remove-button="true"
                    @file-size-exceed="handleCroppaFileSizeExceed"
                    @file-type-mismatch="handleCroppaFileTypeMismatch"
            >
            </croppa >

            <div class="email"><input v-model="editProfileDTO.email"/></div>
            <button @click="save">Save</button>
            <button @click="cancel">Cancel</button>
        </div>
        <error v-if="errorMessage"></error>
    </div>
</template>

<script>
    import Vue from 'vue'
    import Error from './Error.vue'
    import Croppa from 'vue-croppa'
    import store, {FETCH_USER_PROFILE} from '../store'

    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['dto'],
        data(){
            return {
                editProfileDTO: null,
                errorMessage: '',
                submitting: false,
                myCroppa: {}
            }
        },
        methods:{
            handleCroppaFileTypeMismatch() {
                alert('Image wrong type');
            },
            handleCroppaFileSizeExceed() {
                // see :file-size-limit
                alert('Image size must be < than 1 Mb');
            },
            save(){
                this.startSending();
                const sendProfile = (url) => {
                    if (url) {
                        this.editProfileDTO.avatar = url;
                    }
                    this.$http.post(`/api/profile`, this.editProfileDTO).then(
                        successResp => {
                            this.finishSending();
                            store.dispatch(FETCH_USER_PROFILE);
                            this.$emit('SAVED');
                        },
                        failResp => {
                            this.finishSending();
                        }
                    )
                };
                if (this.myCroppa.getChosenFile()){
                    this.myCroppa.promisedBlob(this.myCroppa.getChosenFile().type).then(blob => {
                        const formData = new FormData();
                        formData.append('image', blob); // multipart part with name 'image'
                        this.$http.post('/api/image/user/avatar', formData)
                            .then(successResp => {
                                return successResp.body.relativeUrl
                            }, failResp => {
                                throw "failed to upload title img"
                            })
                            .then(sendProfile)
                            .catch(e => {
                                console.log("Catch error in sending avatar with image");
                                this.finishSending();
                            })
                    });
                } else {
                    console.log('avatar not selected');
                    sendProfile('');
                }
            },
            cancel() {
                this.$emit('CANCELED');
            },
            startSending() {
                this.submitting = true;
            },
            finishSending() {
                this.submitting = false;
            },
        },
        created(){
            // deep copy
            this.editProfileDTO = Vue.util.extend({}, this.dto);
        },
        components:{
            Error,
            'croppa': Croppa.component
        },
    };
</script>

<style lang="stylus">
    img.avatar {
        max-height 200px
        max-width 400px
    }

    img.edit-container-pen {
        height 32px;
        cursor pointer
    }
</style>
