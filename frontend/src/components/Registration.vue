<template>
    <div class="registration" id="registration">
        <form v-if="!emailSuccessfullySent">
            <vue-form-generator :schema="schema" :model="profile" :options="formOptions" @validated="onValidated"></vue-form-generator>
            <button id="submit" type="submit" @click.prevent="onSubmit" v-bind:disabled="!submitEnabled" class="blog-btn ok-btn">Submit</button>
            <blog-spinner v-if="submitting" message="Sending data"/>

            <div class="field">
                <error v-if="errorMessage" :message="errorMessage"></error>
            </div>
        </form>
        <div v-else>
            Your confirmation email successfully sent. Open your mail '{{profile.email}}' and follow confirmation link.
            Didn't get email ? Click <router-link class="registration-confirmation-resend" :to="{ path: '/registration-confirmation-resend', query: { email: profile.email }}">here</router-link>
        </div>
    </div>
</template>

<script>
    import Vue from 'vue'
    import Error from './Error.vue'

    import BlogSpinner from './BlogSpinner.vue'
    import {PASSWORD_MIN_LENGTH} from '../constants'

    import VueFormGenerator from "vue-form-generator";
    import "vue-form-generator/dist/vfg.css";

    export default {
        name: 'registration', // component name
        data(){
            return {
                // CreateUserDTO
                profile: {
                    login: '',
                    email: '',
                    password: '',
                },
                submitEnabled: false, // is submit button enabled
                submitting: false,
                emailSuccessfullySent: false,

                errorMessage: '',

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
                        required: true,
                        hint: `Minimum ${PASSWORD_MIN_LENGTH} characters`,
                        validator: VueFormGenerator.validators.string
                    },{
                        type: "input",
                        inputType: "email",
                        label: "E-mail",
                        id: 'email',
                        model: "email",
                        required: true,
                        placeholder: "User's e-mail address",
                        validator: VueFormGenerator.validators.email
                    }]
                },
                formOptions: {
                    validateAfterLoad: true,
                    validateAfterChanged: true
                },
            }
        },
        components: {
            BlogSpinner, Error,
            "vue-form-generator": VueFormGenerator.component,
        },
        methods: {
            onSubmit() {
                // console.log('start submitting');
                this.submitEnabled = false;
                this.submitting = true;
                this.errorMessage = null;

                this.$http.post('/api/register', this.profile).then(response => {
                    this.submitEnabled = true;
                    // console.log('end submitting');
                    this.submitting = false;
                    this.emailSuccessfullySent = true;
                }, response => {
                    this.submitEnabled = true;
                    console.error(response);
                    // alert(response);
                    this.submitting = false;
                    this.errorMessage = response.body.message;
                });
            },
            onValidated(isValid, errors) {
                // console.log("Validation result: ", isValid, ", Errors:", errors);
                this.submitEnabled = isValid;
                this.errorMessage = null;
            },

        }
    }
</script>

<style lang="stylus" scoped>
    @import "../constants.styl"

    .registration {
        display block
        position relative
        margin 10px;

        .field {
            margin-bottom 4px;
        }

        #submit{
            margin-top 0.5em
        }
    }
</style>