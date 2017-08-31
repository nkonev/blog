<template>
    <div>
        Enter email for resend confirmation
        <input v-model="email"/>
        <span class="help-block" v-show="errors.email">{{ errors.email }}</span>
        <button @click="resend()" v-bind:disabled="!submitEnabled">Resend</button>
        <span v-if="emailSuccessfullySent">Sent</span>
        <span class="help-block" v-show="errors.server">{{ errors.server }}</span>
    </div>
</template>

<script>
    import required from 'vuelidate/lib/validators/required'
    import email from 'vuelidate/lib/validators/email'

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
        methods: {
            resend() {
                this.errors.message = null;

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