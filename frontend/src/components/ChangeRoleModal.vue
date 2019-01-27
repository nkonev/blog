<template>
    <modal :name="modalName" transition="pop-out" :width="modalWidth" @before-open="beforeOpen" :height="240">
        <div class="box">
            <div class="login-title">Change role for {{login}}</div>

            <div class="box-part">
                <select>
                    <option value="ROLE_USER">ROLE_USER</option>
                    <option value="ROLE_ADMIN">ROLE_ADMIN</option>
                </select>

            </div>
        </div>
    </modal>
</template>

<script>
    import {CHANGE_ROLE_MODAL} from '../constants';
    import BlogSpinner from "./BlogSpinner.vue"

    export default {
        name: 'ChangeRoleModal',

        data () {
            return {
                login: '',
                formError: null,

                modalName: CHANGE_ROLE_MODAL,
                loading: false
            }
        },
        computed: {
            modalWidth(){
                return screen.width > 380 ? 380 : screen.width;
            },
        },
        methods:{
            setSpinner(){
                this.loading = true;
            },
            beforeOpen (event) {
                console.log(event.params.login);
                this.login = event.params.login;
            }
        },
        components: {
            BlogSpinner,
        }
    }
</script>

<style lang="stylus" scoped>
    $background_color=#404142;
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
    }


</style>
