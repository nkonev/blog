<template>
    <div v-if="currentUser" class="user-profile-nav">
        <div class="dropdown">
            <span class="dropobj">
                <span class="login">{{currentUser.login}}</span>
                <img class="avatar" :src="currentUser.avatar"/>
            </span>
            <div class="dropdown-content">
                <span class="blue" @click.prevent="dispatchAction('profile')">Profile</span>
                <span class="red" @click.prevent="dispatchAction('exit')">Logout</span>
            </div>
        </div>
    </div>
    <span class="user-profile-nav-anonymous" v-else>
        <a class="router-link" href="#" @click.prevent="login">login</a>
        <span class="router-link">or</span>
        <router-link class="router-link" to="/registration">Registration</router-link>
    </span>
</template>

<script>
    import Multiselect from 'vue-multiselect'
    import store from '../store'
    import {UNSET_USER} from '../store'
    import "vue-multiselect/dist/vue-multiselect.min.css"
    import {LOGIN_MODAL} from '../constants'
    import bus from '../bus'
    import {LOGOUT} from '../bus'
    import {useProfileName} from  '../routes'

    export default {
        name: 'user-profile-nav',
        props: ['currentUser'],
        mounted() {
            // console.debug('user-profile-nav loaded', this.$props);
        },
        data () {
            return {
                actions: ['exit', 'profile'],
            }
        },
        methods: {
            dispatchAction (actionName) {
                switch (actionName) {
                    case 'exit':
                        this.$http.post('/api/logout').then(
                            response => {
                                // ok
                                store.commit(UNSET_USER);
                                bus.$emit(LOGOUT, null);
                            },
                            response => {
                                // fail
                                console.error("Can't to logout");
                            }
                        );
                        break;
                    case 'profile':
                        this.$router.push({name: useProfileName, params: {id: this.$props.currentUser.id}});
                        break;
                }
            },
            login() {
                this.$modal.show(LOGIN_MODAL);
            }
        },
    }
</script>

<style lang="stylus" scoped>
    $h=48px

    .user-profile-nav{

        display flex
        flex-direction row
        justify-content flex-end

        &-login,&-not-registegred {
            display flex
            flex-direction column
            justify-content center
        }
        img {
            height $h
            width $h
        }
    }

    .user-profile-nav-anonymous {
        display flex
        flex-direction row
        justify-content flex-end
    }


    /* Dropdown Button */
    .dropobj {
        // display: inline-block;
        // background-color: white;
        color: black;
        padding: 10px;
        font-size: 16px;
        border: none;
        cursor: pointer;
        //background-color blue

        display flex
        flex-direction row

        .login {
            //background-color red
            height $h
            line-height $h
            margin-right 10px
        }
    }

    /* The container <div> - needed to position the dropdown content */
    .dropdown {
        position: relative;
        display: inline-block;
        //float:left;

        /* Show the dropdown menu on hover */
        &:hover .dropdown-content {
            display: block;
        }

        /* Change the background color of the dropdown button when the dropdown content is shown */
        &:hover .dropobj {
            // background-color: #65f769;
            //box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.8);
        }
    }

    /* Dropdown Content (Hidden by Default) */
    .dropdown-content {
        display: none;
        position: absolute;
        right: 0;
        background-color: #f9f9f9;
        min-width: 100px;
        width 100%
        box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.8);
        z-index: 1;

        /* Links inside the dropdown */
        span {
            color: black;
            padding: 10px 10px;
            text-decoration: none;
            display: block;
            text-align center
            cursor pointer
        }

        /* Change color of dropdown links on hover */
        span:hover {
            background-color: #f1f1f1
            cursor pointer
        }

        span.blue:hover {
            color blue
            text-shadow 0 0 1em blue;
        }

        span.red:hover {
            color red
            text-shadow 0 0 1em red;
        }
    }

</style>