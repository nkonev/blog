<template>
    <div class="registration" id="registration">
        <form action="/api/register" method="post">
            <div class="field">
                <label for="login">Login</label>
                <input id="login" v-model="profile.login" name="login" v-validate="'required'"/>
                <span class="help-block" v-show="errors.has('login')">{{ errors.first('login') }}</span>
            </div>
            <div class="field">
                <label for="email">Email</label>
                <input id="email" v-model="profile.email" name="email" v-validate="'required|email'"/>
                <span class="help-block" v-show="errors.has('email')">{{ errors.first('email') }}</span>
            </div>
            <div class="field">
                <label for="password">password</label>
                <input id="password" type="password" v-model="profile.password" name="password" v-validate="'required'"/>
                <span class="help-block" v-show="errors.has('password')">{{ errors.first('password') }}</span>
            </div>
            <button id="submit" type="submit" @click="onSubmit" v-bind:disabled="hasFormErrors()">Submit</button>
        </form>
    </div>
</template>

<script>
    import Vue from 'vue'
    import VeeValidate from 'vee-validate';
    Vue.use(VeeValidate);

    export default {
        name: 'registration', // component name
        data(){
            return {
                profile: {
                    login: '',
                    email: '',
                    password: ''
                },
            }
        },
        methods: {
            onSubmit() {
                console.log(this.profile.login, this.profile.email, this.profile.password);
                if (this.hasFormErrors()) {
                    return false;
                }
            },
            hasFormErrors(){
                const isErrs = this.errors.any();
                console.log('hasFormErrors', isErrs);
                return isErrs;
            }
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

        div {
            .help-block {
                margin-left 4px;
                position absolute
                color $errorColor
            }
        }
    }
</style>