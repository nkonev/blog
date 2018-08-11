<template>

    <div class="profile-edit">
        <div class="profile-edit-msg">
            Editing profile #{{dto.id}}
        </div>

        <div class="profile-edit-info">
            <div class="profile-edit-info-avatar-container">
                <croppa v-model="profileAvatarCroppa"
                        :width="300"
                        :height="300"
                        :file-size-limit="5 * 1024 * 1024"
                        placeholder="Choose avatar image"
                        :initial-image="model.avatar"
                        :placeholder-font-size="20"
                        :disabled="false"
                        :prevent-white-space="false"
                        :show-remove-button="true"
                        accept="image/*"
                        @init="handleCroppaInit"
                        @file-size-exceed="handleCroppaFileSizeExceed"
                        @file-type-mismatch="handleCroppaFileTypeMismatch"
                >
            </croppa>
            </div>

            <div class="profile-edit-info-form">
                <vue-form-generator :schema="schema" :model="model" :options="formOptions" @validated="onValidated"></vue-form-generator>
            </div>

        </div>

        <div class="profile-edit-buttons">
            <button class="save blog-btn ok-btn" @click="save" v-bind:disabled="!submitEnabled">Save</button>
            <button @click="cancel" class="blog-btn">Cancel</button>
        </div>
        <error v-if="errorMessage" :message="errorMessage"></error>
    </div>
</template>

<script>
    import Vue from 'vue'
    import Error from './Error.vue'
    import Croppa from 'vue-croppa'
    import store, {FETCH_USER_PROFILE} from '../store'
    import {PROFILE_URL, PASSWORD_MIN_LENGTH} from '../constants'
    import VueFormGenerator from "vue-form-generator";
    import "vue-form-generator/dist/vfg.css";  // optional full css additions
    import 'vue-croppa/dist/vue-croppa.css'

    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['dto'],
        data(){
            return {
                errorMessage: '',
                submitting: false,
                profileAvatarCroppa: {},
                chosenFile: null,
                submitEnabled: true,

                model: {
                    login: "",
                    password: "",
                    email: "",
                },

                schema: {
                    fields: [{
                        type: "input",
                        inputType: "text",
                        label: "Login",
                        model: "login",
                        placeholder: "Your login for enter",
                        featured: true,
                        required: true
                    },{
                        type: "input",
                        inputType: "password",
                        label: "Password",
                        model: "password",
                        min: PASSWORD_MIN_LENGTH,
                        required: false, // We don't fore user to remember his or her password
                        hint: `Minimum ${PASSWORD_MIN_LENGTH} characters`,
                        validator: VueFormGenerator.validators.string
                    },{
                        type: "input",
                        inputType: "email",
                        label: "E-mail",
                        model: "email",
                        required: true,
                        placeholder: "User's e-mail address",
                        validator: VueFormGenerator.validators.email
                    }]
                },

                formOptions: {
                    validateAfterLoad: true,
                    validateAfterChanged: true
                }
            }
        },
        methods:{
            handleCroppaInit(e){
                document.querySelector(".profile-edit-info-avatar-container canvas").style.border="dashed"
            },
            handleCroppaFileTypeMismatch() {
                alert('Image wrong type');
            },
            handleCroppaFileSizeExceed() {
                // see :file-size-limit
                alert('Image size must be < than 5 Mb');
            },
            save(){
                this.startSending();
                const sendProfile = (url) => {
                    if (url) {
                        this.model.avatar = url;
                    }
                    this.errorMessage = null;
                    this.$http.post(PROFILE_URL, this.model).then(
                        successResp => {
                            this.finishSending();
                            store.dispatch(FETCH_USER_PROFILE);
                            this.$emit('SAVED');
                        },
                        failResp => {
                            this.errorMessage = failResp.body;
                            this.finishSending();
                        }
                    )
                };
                if (this.profileAvatarCroppa.getChosenFile()){
                    this.profileAvatarCroppa.promisedBlob(this.profileAvatarCroppa.getChosenFile().type).then(blob => {
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
            onValidated(isValid, errors) {
                console.log("Validation result: ", isValid, ", Errors:", errors);
                this.submitEnabled = isValid;
                this.errorMessage = null;
            },
        },
        created(){
            // deep copy
            this.model = Vue.util.extend({}, this.dto);
        },
        components:{
            Error,
            'croppa': Croppa.component,
            "vue-form-generator": VueFormGenerator.component,
        },
    };
</script>

<style lang="stylus" scoped>
    .profile-edit {
        &-msg{
            text-align center
            font-size xx-large
            font-family monospace
            margin-bottom: 1em;
            margin-top: 0.5em;
        }

        &-info {
            //display flex
            //flex-direction row
            //flex-wrap wrap
            //justify-content space-around
            display block

            &-avatar-container {
                text-align center
                float left
                //width 100%
            }

            &-form {
            }
        }

        &-buttons{
            display flex
            flex-direction row

            clear both
        }
    }

    img.avatar {
        max-height 200px
        max-width 400px
    }

    img.edit-container-pen {
        height 32px;
        cursor pointer
    }
</style>
