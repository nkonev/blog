<template>
    <div class="restore-password" v-if="!isPasswordResetTokenSent">
        <h1>Restore your password</h1>
        <input id="email" v-model="email" placeholder="Your email"/>
        <button id="send" @click="requestPasswordResetToken()" v-bind:disabled="!submitEnabled" class="blog-btn ok-btn">Send password reset token</button>
        <error v-show="errors.email" :message="errors.email"></error>
    </div>
    <div class="check-your-email" v-else>
        <span>check your email</span>
    </div>

</template>

<script>
    import required from 'vuelidate/lib/validators/required'
    import email from 'vuelidate/lib/validators/email'

    import Error from './Error.vue'

    const reqPasswordResetPrefix = '/api/request-password-reset?email=';

    export default {
        components: {Error},
        data(){
            return {
                errors: {},
                email: null,
                passwordResetToken: null,
                isPasswordResetTokenSent: false,
                submitEnabled: true,
            }
        },
        methods: {
            requestPasswordResetToken(){
                this.$http.post(reqPasswordResetPrefix+this.$data.email).then(
                    goodResponse => {
                        this.$data.isPasswordResetTokenSent = true;
                    },
                    badResponse => {
                        console.log(badResponse);
                    }
                )
            },
            validate() {
                this.errors = {};
                this.errors.email = required(this.email) ? false : 'Email is required';
                if (!this.errors.email) { // if previous check is passed
                    this.errors.email = email(this.email) ? false : 'Email is invalid';
                }

                let hasErrors = false;
                Object.keys(this.errors).forEach(item => {
                    hasErrors = hasErrors || !!this.errors[item]; // !! - convert to boolean
                });
                return hasErrors
            },
            updateSubmitEnabled(){
                let hasErrors = this.validate();
                this.submitEnabled = !hasErrors;
            },

        },
        watch: {
            email() {
                this.emailSuccessfullySent = false;
                this.updateSubmitEnabled();
            }
        },
        created(){
            this.updateSubmitEnabled();
        },
        metaInfo: {
            title: 'Restoring password',
        }
    }
</script>

<style lang="stylus">
    .restore-password button {
        display unset
    }
</style>