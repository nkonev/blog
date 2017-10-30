<template>
    <div class="resend-registration-confirmation-token">
        Enter email for resend confirmation
        <input v-model="email"/>
        <error v-show="errors.email" :message="errors.email"></error>
        <button @click="resend()" v-bind:disabled="!submitEnabled">Resend</button>
        <span class="email-successfully-sent" v-if="emailSuccessfullySent">Sent</span>
        <error v-show="errors.server" :message="errors.server"></error>
    </div>
</template>

<script>
    import required from 'vuelidate/lib/validators/required'
    import email from 'vuelidate/lib/validators/email'
    import Error from './Error.vue'

    export default {
        data() {
            return {
                errors: {

                },
                email: null,
                emailSuccessfullySent: false,
                submitEnabled: true,
            }
        },
        created() {
            this.email = this.$route.query.email
        },
        components: {Error},
        methods: {
            resend() {
                this.errors.message = null;
                this.emailSuccessfullySent = false;
                this.$http.post('/api/resend-confirmation-email?email=' + this.email).then(response => {
                    this.submitting = false;
                    this.emailSuccessfullySent = true;
                }, response => {
                    console.error(response);
                    // alert(response);
                    this.submitting = false;
                    this.errors.server = response.body.message;
                });
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
    }
</script>