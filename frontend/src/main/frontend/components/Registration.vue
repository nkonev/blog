<template>
    <div class="registration" id="registration">
        <form>
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
    import VueResource from 'vue-resource'
    Vue.use(VueResource);

    // https://monterail.github.io/vuelidate/
    // https://github.com/monterail/vuelidate/tree/master/src/validators
    import required from 'vuelidate/lib/validators/required'
    import email from 'vuelidate/lib/validators/email'

    const PASSWORD_MIN_LENGTH = 6;

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
                    // email:
                            // false if there isn't any error
                            // 'error message' if error present
                    // ...
                },
                submitEnabled: false
            }
        },
        watch: {
            'profile': {
                handler: function (val, oldVal) {
                    this.hasFormErrors();
                },
                deep: true
            }
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
                this.errors = { };
                this.errors.login = required(this.profile.login) ? false : 'login is required';
                this.errors.email = required(this.profile.email) ? false : 'Email is required';
                if (!this.errors.email) { // if previous check is passed
                    this.errors.email = email(this.profile.email) ? false : 'Email is invalid';
                }
                this.errors.password = required(this.profile.password)  ? false : 'password is required';
                if (!this.errors.password){ // if previous check is passed
                    this.errors.password = this.profile.password.length >= PASSWORD_MIN_LENGTH ? false : 'password must be logger than ' + PASSWORD_MIN_LENGTH;
                }

                console.debug("validated", Object.keys(this.errors));
                let hasErrors = false;
                Object.keys(this.errors).forEach(item => {
                    console.debug(item, this.errors[item]);
                    hasErrors = hasErrors || !!this.errors[item]; // !! - convert to boolean
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