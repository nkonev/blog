<template>
    <div class="registration" id="registration">
        <form action="/api/register" method="post">
            <div class="field">
                <label for="login">Login <span class="required-mark">*</span></label>
                <input id="login" v-model="profile.login" name="login" autofocus/>
                <span class="help-block" v-show="errors.login">{{ errors.login }}</span>
            </div>
            <div class="field">
                <label for="email">Email <span class="required-mark">*</span></label>
                <input id="email" v-model="profile.email" name="email" />
                <span class="help-block" v-show="errors.email">{{ errors.email }}</span>
            </div>
            <div class="field">
                <label for="password">password <span class="required-mark">*</span></label>
                <input id="password" type="password" v-model="profile.password" name="password" />
                <span class="help-block" v-show="errors.password">{{ errors.password }}</span>
            </div>
            <button id="submit" type="submit" @click.prevent="onSubmit" v-bind:disabled="!submitEnabled">Submit</button>
        </form>
    </div>
</template>

<script>
    import Vue from 'vue'
    // https://monterail.github.io/vuelidate/
    // https://github.com/monterail/vuelidate/tree/master/src/validators
    // import {required, email} from 'vuelidate'
    import required from 'vuelidate/lib/validators/required'
    import email from 'vuelidate/lib/validators/email'

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
                errors: {
                    login: false, // false or 'error message'
                    email: false,
                    password :false
                },
                submitEnabled: false
            }
        },
        watch: {
            'profile.login': function (p) {
                this.hasFormErrors();
            },
            'profile.email': function (p) {
                this.hasFormErrors();
            },
            'profile.password': function (p) {
                this.hasFormErrors();
            },
        },
        methods: {
            onSubmit() {
                if (this.hasFormErrors()) {
                    return false;
                }

                const self = this;
                console.log('start submitting');
                this.submitEnabled = false;

                this.$http.post('/api/register', this.profile).then(response => {
                    self.submitEnabled = true;
                    console.log('end submitting');
                }, response => {
                    console.error(response);
                    // alert(response);
                });
            },
            hasFormErrors(){
                let hasErrors = this.validate();
                this.submitEnabled = !hasErrors;
                return hasErrors;
            },
            validate() {
                this.errors = {
                    login: required(this.profile.login) ? false : 'login is required',
                    email: required(this.profile.email) && email(this.profile.email) ? false : 'Valid email is required',
                    password: required(this.profile.password)  ? false : 'valid password is required',
                };
                console.debug("validated", Object.keys(this.errors));
                let hasErrors = false;
                Object.keys(this.errors).forEach(item => {
                    console.debug(item, this.errors[item]);
                    hasErrors = hasErrors || ((this.errors[item] === false) ? false : true);
                });
                console.debug("validated, hasErrors=", hasErrors);
                return hasErrors
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

        .required-mark {
            color red
        }

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