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
                        @file-choose="handleCroppaFileChoose"
                        @image-remove="handleCroppaImageRemove"
                >
            </croppa>
            </div>

            <div class="profile-edit-info-form">
                <vue-form-generator :schema="schema" :model="model" :options="formOptions" @validated="onValidated"></vue-form-generator>
            </div>

        </div>

        <div class="profile-edit-buttons">
            <button class="save blog-btn ok-btn" @click="save" v-bind:disabled="!submitEnabled">Save</button>
            <button @click="requestDelete()" class="delete blog-btn delete-btn">Delete account</button>
            <button @click="cancel" class="blog-btn">Cancel</button>
        </div>
        <error v-if="errorMessage" :message="errorMessage"></error>
    </div>
</template>

<script>
    import Vue from 'vue'
    import Error from './Error.vue'
    import Croppa from 'vue-croppa'
    import store, {FETCH_USER_PROFILE, UNSET_USER} from '../store'
    import {root_name} from '../routes'
    import {PROFILE_URL, PASSWORD_MIN_LENGTH, DIALOG} from '../constants'
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
                    removeAvatar: false,
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
                        required: false,
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
            requestDelete(){
                this.$modal.show(DIALOG, {
                    title: 'Account delete confirmation',
                    text: 'Do you want to delete your account ?',
                    buttons: [
                        {
                            title: 'No',
                            default: true,
                            handler: () => {
                                this.$modal.hide(DIALOG)
                            }
                        },
                        {
                            title: 'Yes',
                            handler: () => {
                                this.doDelete();
                                store.commit(UNSET_USER);
                                this.$modal.hide(DIALOG)
                            }
                        },
                    ]
                })
            },
            doDelete(){
                this.$http.delete("/api/profile").then(value => {
                    this.$router.push({ name: root_name });
                }, reason => {
                    console.error("Error during delete account", reason);
                });
            },
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
            handleCroppaFileChoose(e){
                console.debug('image chosen', e);
                this.model.removeAvatar = false;
                this.$data.chosenFile = e;
            },
            handleCroppaImageRemove(){
                this.model.removeAvatar = true;
                this.$data.chosenFile = null;
            },
            save(){
                this.startSending();
                const sendProfile = () => {
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
                if (this.$data.chosenFile){
                    this.profileAvatarCroppa.promisedBlob(this.$data.chosenFile.type).then(blob => {
                        const formData = new FormData();
                        formData.append('image', blob); // multipart part with name 'image'
                        this.$http.post('/api/image/user/avatar', formData)
                            .then(successResp => {
                                this.model.avatar = successResp.body.relativeUrl;
                                return;
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
                    sendProfile();
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
    @import "../constants.styl"

    .profile-edit {
        display flex
        flex-direction column

        &-msg{
            text-align center
            font-size xx-large
            font-family monospace
            margin-bottom: 1em;
            margin-top: 0.5em;
        }

        &-info {
            display flex
            flex-direction row
            //flex-wrap wrap
            justify-content space-around
            align-items stretch
            height 100%
            width: 100%

            &-avatar-container {
                text-align center
            }

            &-form {
                height 100%
            }
        }

        @media screen and (max-width: $contentWidth - 300) {
            &-info {
                flex-wrap wrap
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
