<template>
    <modal :name="modalName" transition="pop-out" @before-open="beforeOpen" :width="380" :height="240">
        <div class="box">
            <div class="box-part">
                <div class="login-title">Please login</div>
                <form autocomplete="off">
                    <input id="username" type="text" placeholder="Username" v-model="formUsername">
                    <input id="password" type="password" placeholder="Password" v-model="formPassword">

                    <div class="button-set">
                        <button id="btn-submit" class="blog-btn ok-btn login-btn" type="submit" @click.prevent="doLogin">Login!</button>
                    </div>

                    <div class="errors">
                        <div v-show="formError" class="box-error-message">{{formError}}</div>
                    </div>
                </form>
            </div>
        </div>
    </modal>
</template>

<script>
//    import router from '../router'
    import Vue from 'vue'
    import {FETCH_USER_PROFILE} from '../store'
    import bus from '../bus'
    import {LOGIN} from '../bus'

    import {LOGIN_MODAL} from '../constants';

    export default {
        name: 'LoginModal',

        data () {
            return {
                formUsername: '',
                formPassword: '',
                formError: null,

                modalName: LOGIN_MODAL
            }
        },
        props: [
            'onSuccessCallback',
            'onFailCallback'
        ],
        methods:{
            beforeOpen (event) {
                this.formError = null;
            },
            doLogin() {
                const self = this;
                const options = { emulateJSON: true }; // for pass as form data
                this.$http.post('/api/login', {username: this.formUsername, password: this.formPassword}, options).then(response => {
                    // get body data
                    this.$modal.hide(LOGIN_MODAL);

                    this.$store.dispatch(FETCH_USER_PROFILE);
                    bus.$emit(LOGIN, null);
                    if(this.$props.onSuccessCallback){
                        this.$props.onSuccessCallback();
                    }
                }, response => {
                    // error callback
                    // alert('Booh! Wrong credentials, try again!');
                    this.formError = response.body.message;
                    if(this.$props.onFailCallback){
                        this.$props.onFailCallback();
                    }
                });

            }
        }
    }
</script>

<style lang="stylus" scoped>
    $background_color=#404142;
    @import "../buttons.styl"

    .box {
        background: white;
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: stretch;
        border-radius: 2px;
        box-sizing: border-box;
        box-shadow: 0 0 40px black;
        color: #8b8c8d;
        width: 100%;
        height: 100%;

        .box-part {
            height: 100%;
            width: 100%;
            margin-left 5%;
            margin-right 5%;
        }

        .errors {
            display: block
            margin-top 0.5em;
            margin-bottom 0.5em;

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
