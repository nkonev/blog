<template>
    <modal :name="modalName" transition="pop-out" :width="modalWidth" @before-open="beforeOpen" :height="180">
        <div class="spinner" v-if="isLoading">
            <BlogSpinner message="Loading roles..."/>
        </div>
        <div class="box" v-else>
            <div class="title">Change role for {{login}}</div>

            <div class="box-part">
                <select v-model="role">
                    <option v-for="option in getRoles" :value="option">
                        {{ option }}
                    </option>
                </select>

                <div class="button-set">
                    <button id="btn-submit" class="blog-btn ok-btn" type="button" @click="updateRole()">Apply</button>
                    <button id="btn-cancel" class="blog-btn" type="reset" @click="closeModal()">Cancel</button>
                </div>

            </div>
        </div>
    </modal>
</template>

<script>
    import {CHANGE_ROLE_MODAL} from '../constants';
    import BlogSpinner from "./BlogSpinner.vue"
    import store, {GET_ROLES} from '../store'
    const USER_UPDATED = 'USER_UPDATED';

    export default {
        name: 'ChangeRoleModal',

        data () {
            return {
                login: '',
                formError: null,

                modalName: CHANGE_ROLE_MODAL,
                currentRole: null,
                userId: null,

                loading: false
            }
        },
        store,
        computed: {
            modalWidth(){
                return screen.width > 380 ? 380 : screen.width;
            },
            role:{
                get(){
                    return this.$data.currentRole;
                },
                set(value){
                    this.$data.currentRole = value;
                }
            },
            getRoles(){
                return this.$store.getters[GET_ROLES];
            },
            isLoading(){
                return !this.$data.currentRole || this.getRoles == [] || this.loading
            }
        },
        methods:{
            beforeOpen(event) {
                this.login = event.params.login;
                this.role = event.params.currentRole;
                this.userId = event.params.userId;
            },
            closeModal(){
                this.$modal.hide(CHANGE_ROLE_MODAL);
            },
            updateRole(){
                this.loading = true;
                this.$http.post('/api/user/role?userId='+this.userId+'&role='+this.role).then(value => {
                    this.loading = false;
                    this.$modal.hide(CHANGE_ROLE_MODAL);
                    this.$emit(USER_UPDATED, value.body);
                },reason => {
                    this.loading = false;
                    console.error("Error during change role", reason);
                })
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

    .spinner {
        display: flex;
        flex-direction: column;
        justify-content: center
        height: 100%;
    }

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

        .title {
            box-sizing: border-box;
            padding: 22px;
            width: 100%;
            text-align: center;
            letter-spacing: 1px;
            font-size: 22px;
            font-weight: 300;
        }


        .box-part {
            height: 100%;
            padding-left 5%;
            padding-right 5%;
            padding-bottom 5%

            display: flex;
            flex-direction: column;
            justify-content: space-between

            .button-set {
                width 100%
                display: flex
                flex-direction row
                justify-content space-between

                button {
                    width 49%
                }
            }
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
