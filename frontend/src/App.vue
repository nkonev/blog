<template>
    <div id="app">
        <img class="bg" :src="config.imageBackground"/>

        <div id="content">
            <LoginModal></LoginModal>
            <auto-progress :includedUrls="['/api/post\\?.*', '/api/post/\\d+/comment.*', '/api/user\\?.*']"/>

            <h1 class="logo">{{config.header}}</h1>
            <nav id="header-nav">
                <router-link class="router-link" to="/" exact>Posts</router-link>
                <router-link class="router-link" to="/users">Users</router-link>
                <a class="router-link" id="a-doc" href="/docs/index.html">API</a>
                <router-link class="router-link" to="/help">Help</router-link>
                <router-link v-show="config.showSettings" class="router-link" to="/settings">Settings</router-link>
                <user-profile-nav v-bind:currentUser="currentUser"/>
            </nav>
            <!--<random-posts v-if="showRandom"/>-->
            <v-dialog/>
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
    import store, {GET_USER, FETCH_USER_PROFILE, FETCH_CONFIG, GET_CONFIG} from './store'
    import {mapGetters} from 'vuex'
    import bus, {LOGIN, LOGOUT} from './bus'
    import Notifications from './notifications'
    import {isLargeScreen} from './utils'
    // https://www.endpoint.com/blog/2018/07/12/vue-fontawesome-facebook-twitter
    import { library } from '@fortawesome/fontawesome-svg-core'
    import { faFacebook } from '@fortawesome/free-brands-svg-icons'
    import { faArrowLeft, faArrowRight } from '@fortawesome/free-solid-svg-icons'
    import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

    library.add(faFacebook, faArrowLeft, faArrowRight);

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
            ...mapGetters({currentUser: GET_USER, config: GET_CONFIG}), // currentUser is here, 'getUser' -- in store.js
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
            store.dispatch(FETCH_CONFIG);
        },
        created() {
            bus.$on(LOGIN, this.onSuccessLogin);
            bus.$on(LOGOUT, this.onSuccessLogout);
        },
        destroyed() {
            bus.$off(LOGIN, this.onSuccessLogin);
            bus.$off(LOGOUT, this.onSuccessLogout);
        },
        metaInfo(){
            return Vue.util.extend(
                {
                    title: 'Welcome to'
                },
                {
                    titleTemplate: this.config.titleTemplate
                }
            );
        }
    }
</script>

<style lang="stylus">
    @import "./constants.styl"
    @import "common.styl"

    body {
        padding 0
        margin 0

        background-color #343434
    }

    html{
        padding 0
        margin 0
    }

    // https://css-tricks.com/perfect-full-page-background-image/
    img.bg {
        /* Set rules to fill background */
        min-height: 100%;
        min-width: 1024px;

        /* Set up proportionate scaling */
        width: 100%;
        height: auto;

        /* Set up positioning */
        position: fixed;
        top: 0;
        left: 0;
    }

    #app {

        #content {
            padding: 4px;

            top: 0px
            border-radius 2px

            font-family: 'Avenir', Helvetica, Arial, sans-serif;
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;

            // center content
            width $contentWidth
            background-color: white;
            position: absolute
            left: 0;
            right: 0;
            margin: auto;

            color: #2c3e50;
            margin-top: 10px;

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
        img.bg {
            min-width auto
            width auto
            min-height 100%
            height 100%
        }

        #app {
            #content {
                padding: 0px;
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
    }

    div.offline-indicator {
        background: #ff6762;
        color: white;
    }
</style>
