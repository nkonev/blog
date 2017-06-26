<template>
    <div class="registration">
        <form action="/api/register" method="post">
            <div class="field">
                <label for="login">Login</label>
                <input id="login" v-model="profile.login" @blur="onLeave"/>
                <span class="help-block" v-show="hasError('login')">{{ errors.login }}</span>
            </div>
            <div class="field">
                <label for="email">Email</label>
                <input id="email" v-model="profile.email" @blur="onLeave"/>
                <span class="help-block" v-show="hasError('email')">{{ errors.email }}</span>
            </div>
            <div class="field">
                <label for="password">password</label>
                <input id="password" type="password" v-model="profile.password" @blur="onLeave"/>
                <span class="help-block" v-show="hasError('password')">{{ errors.password }}</span>
            </div>
            <button type="submit" @click="onSubmit" v-bind:disabled="hasFormErrors()">Submit</button>
        </form>
    </div>
</template>

<script>
    import Validator from 'easiest-js-validator';
    export default {
        name: 'registration', // component name
        data(){
            return {
                profile: {
                    login: '',
                    email: '',
                    password: ''
                },
                rules: {
                    login: 'required,alpha',
                    email: 'required,email',
                    password: 'required',
                },
                messages: {
                    'url': 'The field format is invalid.',
                    'numeric': 'The field must be a number.',
                    'integer': 'The field must be an integer.',
                    'required': 'The field field is required.',
                    'alpha': 'The field may only contain letters.',
                    'email': 'The field must be a valid email address.',
                    'dateISO': 'Please enter a valid date with a ISO format.',
                    'alphaNum': 'The field may only contain letters and numbers.',
                    'phone': 'The field does not have a valid phone number format.',
                    'blank': 'The field is required and does not allow blank spaces.'
                },
                errors: {} // result object with validation errors
            }
        },
        methods: {
            onSubmit() {
                console.log(this.profile.login, this.profile.email, this.profile.password);

                this.errors = Validator.make(this.profile, this.rules, this.messages);
                if (this.hasFormErrors()) {
                    return false;
                }

                this.errors = {};
            },
            onLeave() {
                return this.onSubmit();
            },
            //look up possibles validation errors.
            hasError(key){
                return typeof this.errors[key] !== 'undefined';
            },
            hasFormErrors(){
                console.log('hasFormErrors');
                return Object.keys(this.errors).length>0;
            }
        }
    }
</script>

<style lang="stylus" scoped>
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
                color red
            }
        }
    }
</style>