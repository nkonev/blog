<template>
    <div class="password-reset-enter-new" v-if="!isPasswordSuccessfullyReset">
        Please enter new password
        <input id="new-password" v-model="newPassword"/>
        <button id="set-password" @click="resetPassword()">Set new password</button>
        <!-- TODO display validation errors -->
        <span class="help-block" v-show="errors.server">{{ errors.server }}</span>
    </div>
    <div v-else>
        Now you can login with new password
    </div>
</template>

<script>
    import bus, {LOGIN} from '../bus'
    import {root_name} from '../routes'

    const setNewPassword = '/api/password-reset-set-new';

    export default {
        data() {
            return {
                newPassword: null,
                isPasswordSuccessfullyReset: false,
                errors: {
                    server: null
                }
            }
        },
        methods: {
            resetPassword() {
                this.$http.post(setNewPassword, {passwordResetToken: this.$route.query.uuid, newPassword: this.$data.newPassword}).then(
                    goodResponse => {
                        this.$data.isPasswordSuccessfullyReset = true;
                    },
                    badResponse => {
                        console.log(badResponse);
                        this.$data.errors.server = badResponse.body.message;
                    }
                )
            },
            onSuccessLogin() {
                this.$router.push({ name: root_name });
            }
        },
        created() {
            bus.$on(LOGIN, this.onSuccessLogin);
        },
        destroyed() {
            bus.$off(LOGIN, this.onSuccessLogin);
        }
    }
</script>