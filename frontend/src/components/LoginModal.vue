<template>
    <modal :name="modalName" transition="pop-out" @before-open="beforeOpen" :width="loginModalWidth" :height="240">
        <div class="box">
            <div class="login-title">Please login</div>

            <div class="box-part">
                <form autocomplete="off" v-if="!loading">
                    <input id="username" type="text" placeholder="Username" v-model="formUsername">
                    <input id="password" type="password" placeholder="Password" v-model="formPassword">

                    <div class="button-set">
                        <button id="btn-submit" class="blog-btn login-btn" type="submit" :disabled="loading" @click.prevent="doLogin">Login!</button>
                    </div>
                </form>
                <form action="/api/login/facebook" v-if="!loading && !formError" @click="setSpinner">
                    <button id="btn-submit-facebook" class="blog-btn login-btn-facebook" type="submit">
                        <font-awesome-icon :icon="{ prefix: 'fab', iconName: 'facebook' }" style="width: 28px; height:28px; margin-right: 1em" ></font-awesome-icon>
                        <span>Facebook</span>
                    </button>
                </form>

                <BlogSpinner v-show="loading" class="send-spinner" :speed="0.3" :size="72"></BlogSpinner>

                <div class="errors">
                    <div v-show="formError" class="box-error-message">{{formError}}</div>
                </div>

            </div>
        </div>
    </modal>
</template>

<script>
    import Vue from 'vue'
    import {FETCH_USER_PROFILE, FETCH_CONFIG} from '../store'
    import bus from '../bus'
    import {LOGIN} from '../bus'
    //import Spinner from 'vue-simple-spinner'
    import {LOGIN_MODAL} from '../constants';
    import BlogSpinner from "./BlogSpinner.vue"

    export default {
        name: 'LoginModal',

        data () {
            return {
                formUsername: '',
                formPassword: '',
                formError: null,

                modalName: LOGIN_MODAL,
                loading: false
            }
        },
        props: [
            'onSuccessCallback',
            'onFailCallback'
        ],
        computed: {
            loginModalWidth(){
                return screen.width > 380 ? 380 : screen.width;
            },
        },
        methods:{
            beforeOpen (event) {
                this.clearError();
            },
            clearError(){
                this.formError = null;
            },
            doLogin() {
                this.setSpinner();
                this.formError = null;
                const options = { emulateJSON: true }; // for pass as form data
                this.$http.post('/api/login', {username: this.formUsername, password: this.formPassword}, options).then(response => {
                    // get body data
                    this.$modal.hide(LOGIN_MODAL);

                    this.$store.dispatch(FETCH_USER_PROFILE);
                    this.$store.dispatch(FETCH_CONFIG);
                    bus.$emit(LOGIN, null);
                    this.loading = false;
                    if(this.$props.onSuccessCallback){
                        this.$props.onSuccessCallback();
                    }
                }, response => {
                    // error callback
                    // alert('Booh! Wrong credentials, try again!');
                    this.loading = false;
                    this.formError = response.body.message;
                    if(this.$props.onFailCallback){
                        this.$props.onFailCallback();
                    }
                });

            },
            setSpinner(){
                this.loading = true;
            }
        },
        watch: {
            formUsername() {
                this.clearError();
            },
            formPassword() {
                this.clearError();
            },
        },
        components: {
            BlogSpinner
        }
    }
</script>

<style lang="stylus" scoped>
    $background_color=#404142;
    $fbColor=#3B5998
    @import "../common.styl"

    .box {
        background: white;
        display: flex;
        flex-direction: column;
        //justify-content: center;
        //align-items: stretch;
        border-radius: 2px;
        box-sizing: border-box;
        box-shadow: 0 0 40px black;
        color: #8b8c8d;
        width: 100%;
        height: 100%;

        .box-part {
            height: 100%;
            padding-left 5%;
            padding-right 5%;
            padding-bottom 5%

            display: flex;
            flex-direction: column;
            justify-content: center;
        }

        .errors {
            display: block

            .box-error-message {
                padding 0.4em;
                box-sizing: border-box;
                //width 100%;
                //height 100%
                //margin-left auto;
                //margin-right auto;
                border-radius: 2px;
                text-align: center;
                width: 100%;
                color: white;
                background: #f3504d;
            }

        }


        .login-title {
            box-sizing: border-box;
            padding: 22px;
            width: 100%;
            text-align: center;
            letter-spacing: 1px;
            font-size: 22px;
            font-weight: 300;
        }

        input[type=password],
        input[type=text] {
            display: block;
            box-sizing: border-box;
            margin-bottom: 4px;
            width: 100%;
            font-size: 12px;
            line-height: 2;
            border: 0;
            border-bottom: 1px solid #DDDEDF;
            padding: 4px 8px;
            font-family: inherit;
            transition: 0.5s all;
            outline: none;
        }

        .login-btn{
            width 100%
            background: white;
            color: $ok_color;
            border-color: $ok_color;
            &:hover:enabled {
                border-color: $ok_color;
                background: #00cf6b;
                color: white;
            }
            &:disabled{
                border-color: $grey_color
                color: $grey_color
            }
        }

        .login-btn-facebook{
            width 100%
            height: 36px
            background: white;
            padding 3px
            color: $ok_color;
            border-color: $ok_color;
            display flex
            justify-content center
            align-items center
            span {
                align-items center
            }
            &:hover:enabled {
                border-color: $fbColor
                background: $fbColor
                color: white;
            }
            &:disabled{
                border-color: $grey_color
                color: $grey_color
            }
        }

        .button-set {
            margin-top: 0.5em;
            margin-bottom: 0.5em;
        }
    }

    .pop-out-enter-active,
    .pop-out-leave-active {
        transition: all 0.5s;
    }

    .pop-out-enter,
    .pop-out-leave-active {
        opacity: 0;
        transform: translateY(24px);
    }
</style>
