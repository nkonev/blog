<template>

    <div class="profile-edit">
        <div class="profile-edit-msg">
            Editing profile #{{dto.id}}
        </div>

        <BlogSpinner v-if="binding" class="send-spinner" :speed="0.3" :size="72"></BlogSpinner>
        <template v-else>
        <div class="profile-edit-info">

            <CropperWrapper
                    ref="cropperInstance"
                    :initialImage="model.avatar"
                    :width="cropperWidth"
                    :height="cropperHeight"
                    placeholder="Choose avatar image"
                    :placeholder-font-size="20"
            />

            <div class="profile-edit-info-form">
                <vue-form-generator :schema="schema" :model="model" :options="formOptions" @validated="onValidated"></vue-form-generator>

                <div class="profile-edit-info-form-binding">
                    <template>
                        <form v-if="!model.oauthIdentifiers.facebookId" class="social form-fb bind" @submit.prevent="submitOauthFb">
                            <ButtonFacebook>Bind Facebook</ButtonFacebook>
                        </form>
                        <form v-else class="social unbind" @submit.prevent="unbindFacebook">
                            <ButtonFacebook>Unbind Facebook</ButtonFacebook>
                        </form>
                    </template>
                    <template>
                        <form v-if="!model.oauthIdentifiers.vkontakteId" class="social form-vk bind" @submit.prevent="submitOauthVk">
                            <ButtonVkontakte>Bind Vkontakte</ButtonVkontakte>
                        </form>
                        <form v-else class="social unbind" @submit.prevent="unbindVkontakte">
                            <ButtonVkontakte>Unbind Vkontakte</ButtonVkontakte>
                        </form>
                    </template>
                </div>
            </div>

        </div>

        <div class="profile-edit-buttons">
            <button class="save blog-btn ok-btn" @click="save" v-bind:disabled="!submitEnabled">Save</button>
            <button @click="requestDelete()" class="delete blog-btn delete-btn">Delete account</button>
            <button @click="cancel" class="blog-btn">Cancel</button>
        </div>
        </template>
        <error v-if="errorMessage" :message="errorMessage"></error>
    </div>
</template>

<script>
    import Vue from 'vue'
    import Error from './Error.vue'
    import store, {FETCH_USER_PROFILE, UNSET_USER} from '../store'
    import {root_name} from '../routes'
    import {PROFILE_URL, PASSWORD_MIN_LENGTH, DIALOG} from '../constants'
    import VueFormGenerator from "vue-form-generator";
    import "vue-form-generator/dist/vfg.css";  // optional full css additions
    import ButtonFacebook from "./ButtonFacebook.vue"
    import ButtonVkontakte from "./ButtonVkontakte.vue"
    import BlogSpinner from "./BlogSpinner.vue"
    import {submitOauthVkontakte, submitOauthFacebook} from '../utils'
    import CropperWrapper from "./CropperWrapper";

    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['dto'],
        data(){
            return {
                errorMessage: '',
                submitting: false,
                submitEnabled: true,
                binding: false,

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
            setSpinner(){
                this.binding = true;
            },
            unbindFacebook(){
                this.$http.delete('/api/profile/facebook')
                    .then(value => {
                            //store.commit(SET_USER, value.data);
                            store.dispatch(FETCH_USER_PROFILE);
                            this.$emit('SAVED');
                        },
                        reason => {
                            console.error("Error unbind fb", reason);
                        }
                    )
            },
            unbindVkontakte(){
                this.$http.delete('/api/profile/vkontakte')
                    .then(value => {
                            //store.commit(SET_USER, value.data);
                            store.dispatch(FETCH_USER_PROFILE);
                            this.$emit('SAVED');
                        },
                        reason => {
                            console.error("Error unbind fb", reason);
                        }
                    )
            },
            save(){
                this.startSending();
                const sendProfile = () => {
                    this.errorMessage = null;
                    this.$http.post(PROFILE_URL, {...this.model, removeAvatar: this.$refs.cropperInstance.isRemoveImage()}).then(
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
                const sendAvatarImage = (maybeBlob) => {
                    if (maybeBlob) {
                        const formData = new FormData();
                        formData.append('image', maybeBlob); // multipart part with name 'image'
                        return this.$http.post('/api/image/user/avatar', formData)
                    } else {
                        return Promise.resolve(true); // empty promise
                    }
                };
                const composition = (maybeBlob) => {
                    sendAvatarImage(maybeBlob)
                        .then(successResp => {
                            if(successResp.body) {
                                this.model.avatar = successResp.body.relativeUrl;
                            }
                        })
                        .then(sendProfile)
                        .catch(e => {
                            console.log("Catch error in sending post with image", e);
                            this.finishSending();
                        })
                };
                this.$refs.cropperInstance.onCreateBlob(composition);
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
            submitOauthVk(){
                this.setSpinner();
                submitOauthVkontakte();
            },
            submitOauthFb(){
                this.setSpinner();
                submitOauthFacebook();
            }
        },
        created(){
            // deep copy
            this.model = Vue.util.extend({}, this.dto);
        },
        components:{
            Error,
            CropperWrapper,
            "vue-form-generator": VueFormGenerator.component,
            ButtonFacebook, ButtonVkontakte,
            BlogSpinner
        },
        computed: {
            cropperWidth() {
                return 300
            },
            cropperHeight() {
                return 300
            },
        }
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

            &-form {
                height 100%

                &-binding {
                    width 100%
                    display flex
                    flex-direction row
                    .social {
                        width 100%
                        margin 0.5em
                    }
                }
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
