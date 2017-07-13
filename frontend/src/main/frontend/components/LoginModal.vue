<template>
    <modal :name="modalName" transition="pop-out" @before-open="beforeOpen" :width="380" :height="250">
        <div class="box">
            <div class="box-part">
                <div class="login-title">Пожалуйста, представьтесь</div>
                <form autocomplete="off">
                    <input id="username" type="text" placeholder="Username" v-model="formUsername">
                    <input id="password" type="password" placeholder="Password" v-model="formPassword">

                    <div class="errors">
                        <div v-show="formError" class="box-error-message">Wrong login or password</div>
                    </div>
                    <div class="button-set">
                        <button id="btn-submit" class="large-btn login-btn" type="submit" @click.prevent="doLogin">Login!</button>
                    </div>
                </form>
            </div>
        </div>
    </modal>
</template>

<script>
//    import router from '../router'
//    import {root} from '../router'

    import store from  '../store';
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

                    store.dispatch(FETCH_USER_PROFILE);
                    bus.$emit(LOGIN, null);
                }, response => {
                    // error callback
                    // alert('Booh! Wrong credentials, try again!');
                    this.formError = response;
                });

            }
        }
    }
</script>

<style lang="stylus" scoped>
    $background_color=#404142;
    $signin_color= #00cf6b;

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
            height 20px;
            width: 334px;
            margin-top 4px;
            margin-bottom 4px;

            .box-error-message {
                //display: inline
                padding 4px;
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

        button {
            background: white;
            border-radius: 4px;
            box-sizing: border-box;
            padding: 10px;
            letter-spacing: 1px;
            font-family: "Open Sans", sans-serif;
            font-weight: 400;
            min-width: 140px;
            margin-top: 8px;
            color: #8b8c8d;
            cursor: pointer;
            border: 1px solid #DDDEDF;
            text-transform: uppercase;
            transition: 0.1s all;
            font-size: 10px;
            outline: none;
            &:hover {
                border-color: mix(#DDDEDF, black, 90%);
                color: mix(#8b8c8d, black, 80%);
            }
        }

        .large-btn {
            width: 100%;
            background: white;

            span {
                font-weight: 600;
            }
            &:hover {
                color: white !important;
            }
        }

        .button-set {
            margin-bottom: 8px;
        }

        #signin-btn {
            margin-left: 8px;
        }

        .login-btn {
            border-color: $signin_color;
            color: $signin_color;
            &:hover {
                border-color: $signin_color;
                background: $signin_color;
            }
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
