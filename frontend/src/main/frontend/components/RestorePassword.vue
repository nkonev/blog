<template>
    <div class="restore-password" v-if="!isPasswordResetTokenSent">
        <h1>Restore your password</h1>
        <input id="email" v-model="email" placeholder="Your email"/>
        <button id="send" @click="requestPasswordResetToken()">Send password reset token</button>
    </div>
    <div class="check-your-email" v-else>
        <span>check your email</span>
    </div>

</template>

<script>
    const reqPasswordResetPrefix = '/api/request-password-reset?email=';

    export default {
        data(){
            return {
                email: null,
                passwordResetToken: null,
                isPasswordResetTokenSent: false,
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
            }
        },
    }
</script>