<template>
    <div id="app">
        <div id="content">
            <LoginModal></LoginModal>
            <auto-progress :includedUrls="['/api/post\\?.*', '/api/post/\\d+/comment.*', '/api/user\\?.*']"/>

            <h1 class="logo">{{config.header}}</h1>
            <nav id="header-nav">
                <router-link class="router-link" to="/" exact>Posts</router-link>
                <router-link class="router-link" to="/users">Users</router-link>
                <a class="router-link" id="a-doc" href="/docs/index.html">API</a>
                <router-link class="router-link" to="/help">Help</router-link>
                <user-profile-nav v-bind:currentUser="currentUser"/>
            </nav>
            <!--<random-posts v-if="showRandom"/>-->
            <router-view>
            </router-view>
        </div>
    </div>
</template>

<script>
    import Vue from 'vue';
    import LoginModal from './components/LoginModal.vue';
    import vmodal from 'vue-js-modal'
    import autoProgress from './lib/auto-progress.vue'
    import userProfileNav from './components/UserProfileNav.vue'
    import RandomPosts from "./components/RandomPosts.vue";
    import store, {GET_USER, FETCH_USER_PROFILE} from './store'
    import {mapGetters} from 'vuex'
    import {API_CONFIG} from './constants'
    import bus, {LOGIN, LOGOUT} from './bus'
    import Notifications from './notifications'
    import {isLargeScreen} from './utils'
    // https://www.endpoint.com/blog/2018/07/12/vue-fontawesome-facebook-twitter
    import { library } from '@fortawesome/fontawesome-svg-core'
    import { faFacebook } from '@fortawesome/free-brands-svg-icons'
    import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

    library.add(faFacebook);

    Vue.component('font-awesome-icon', FontAwesomeIcon);
    Vue.use(vmodal, { dialog: true });

    export default {
        name: 'app',
        methods: {
            onSuccessLogin(){
                Notifications.successfulLogin()
            },
            onSuccessLogout(){
                Notifications.successfulLogout()
            },
        },
        store,
        computed: {
            showRandom(){
                return isLargeScreen();
            },
            ...mapGetters({currentUser: GET_USER}), // currentUser is here, 'getUser' -- in store.js
        },
        // used components for provide custom tags
        components: {
            LoginModal,
            'auto-progress':autoProgress,
            userProfileNav,
            RandomPosts,
        },
        mounted() {
            // attempt to initialize LoginModal
            store.dispatch(FETCH_USER_PROFILE);
            this.$http.get(API_CONFIG).then((response) => {
                this.$data.config = response.body
            }, response => {
                console.error("Error on get config", response);
            });
        },
        created() {
            bus.$on(LOGIN, this.onSuccessLogin);
            bus.$on(LOGOUT, this.onSuccessLogout);
        },
        destroyed() {
            bus.$off(LOGIN, this.onSuccessLogin);
            bus.$off(LOGOUT, this.onSuccessLogout);
        },
        data(){
            return {
                config: {}
            }
        },
        metaInfo(){
            return Vue.util.extend(
                {
                    title: 'Welcome to'
                },
                {
                    titleTemplate: this.$data.config.titleTemplate
                }
            );
        }
    }
</script>

<style lang="stylus">
    @import "./constants.styl"
    @import "./buttons.styl"

    body {
        background-color: $strongblack
    }

    #app {
        top: 0px
        border-radius 2px

        font-family: 'Avenir', Helvetica, Arial, sans-serif;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;

        // center content
        width $contentWidth
        background-color: white;
        position: absolute;
        left: 0;
        right: 0;
        margin: auto;

        color: #2c3e50;
        margin-top: 10px;

        #content {
            padding: 4px;
        }

        .logo {
            padding 0.4em 0 0.4em 0.7em
            margin 0 0 0 0
            background-color dimgrey
            color white
            border-radius 2px
        }

        nav {
            background white
            font-size 22px
            font-family 'DejaVu Sans Mono', monospace
            font-weight bold
            a.router-link,
            span.router-link {
                text-decoration none
                color blue
                padding 12px
                display flex
                flex-direction column
                justify-content center
                align-self: center
                margin-top 2px
                margin-bottom 2px
            }
            span.router-link {
                color black
            }

            // :not(.router-link-active)
            a.router-link:hover {
                color white
                background-color #003eff
                border-radius 2px
                opacity: 0.8
                z-index 1000
                filter brightness(2)
                box-shadow: 0 0 3em #003eff;
            }

            a.router-link-active,
            span.router-link-active {
                color white
                background-color #003eff
                border-radius 2px
            }

            display flex
            flex-direction row
            flex-wrap wrap
            justify-content space-between
        }
    }

    @media screen and (max-width: $contentWidth) {
        #app {
            width: 100%;
            margin-top: 0px;

            img#logo {
                display: block;
                margin: 0 auto;
            }

            .logo {
                margin 0
                padding 0
                text-align center
            }

            nav {
                a.router-link:hover {
                    box-shadow: none
                }
            }
        }

    }

    div.offline-indicator {
        background: #ff6762;
        color: white;
    }
</style>
