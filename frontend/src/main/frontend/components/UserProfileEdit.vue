<template>
    <div class="profile">
        <div>
            Editing profile
        </div>
        <div class="user-info">
            <croppa v-model="myCroppa"
                    :width="200"
                    :height="200"
                    :file-size-limit="1 * 1024 * 1024"
                    placeholder="Choose avatar image"
                    :initial-image="model.avatar"
                    :placeholder-font-size="0"
                    :disabled="false"
                    :prevent-white-space="true"
                    :show-remove-button="true"
                    @file-size-exceed="handleCroppaFileSizeExceed"
                    @file-type-mismatch="handleCroppaFileTypeMismatch"
            >
            </croppa >

            <div class="panel-body">
                <vue-form-generator :schema="schema" :model="model" :options="formOptions"></vue-form-generator>
            </div>

            <button class="save" @click="save">Save</button>
            <button @click="cancel">Cancel</button>
        </div>
        <error v-if="errorMessage" :message="errorMessage"></error>
    </div>
</template>

<script>
    import Vue from 'vue'
    import Error from './Error.vue'
    import Croppa from 'vue-croppa'
    import store, {FETCH_USER_PROFILE} from '../store'
    import {PROFILE_URL} from '../constants'
    import VueFormGenerator from "vue-form-generator";
    import "vue-form-generator/dist/vfg.css";  // optional full css additions

    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['dto'],
        data(){
            return {
                errorMessage: '',
                submitting: false,
                myCroppa: {},
                chosenFile: null,

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
                        min: 6,
                        required: false,
                        hint: "Minimum 6 characters",
                        validator: VueFormGenerator.validators.string
                    },{
                        type: "input",
                        inputType: "email",
                        label: "E-mail",
                        model: "email",
                        placeholder: "User's e-mail address"
                    }]
                },

                formOptions: {
                    validateAfterLoad: true,
                    validateAfterChanged: true
                }
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
            this.model = Vue.util.extend({}, this.dto);
        },
        components:{
            Error,
            'croppa': Croppa.component,
            "vue-form-generator": VueFormGenerator.component,
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
