<template>
    <div id="app">
        <div id="content">
            <LoginModal></LoginModal>
            <auto-progress/>

            <h1 class="logo">Blog</h1>
            <nav>
                <router-link class="router-link" to="/" exact>Posts</router-link>
                <router-link class="router-link" to="/users">Users</router-link>
                <a class="router-link" id="a-doc" href="/docs/index.html">API</a>
                <router-link class="router-link" to="/help">Help</router-link>
                <user-profile-nav v-bind:currentUser="currentUser"/>
            </nav>
            <router-view></router-view>
        </div>
    </div>
</template>

<script>
    import Vue from 'vue';
    import LoginModal from './components/LoginModal.vue';
    import vmodal from 'vue-js-modal'
    import autoProgress from 'vue-auto-progress'
    import userProfileNav from './components/UserProfileNav.vue'
    import store, {GET_USER, FETCH_USER_PROFILE} from './store'
    import {mapGetters} from 'vuex'

    Vue.use(vmodal);

    export default {
        name: 'app',
        methods: {},
        store,
        computed: mapGetters({currentUser: GET_USER}), // currentUser is here, 'getUser' -- in store.js
        // used components for provide custom tags
        components: {
            LoginModal,
            autoProgress,
            userProfileNav
        },
        mounted() {
            // attempt to initialize LoginModal
            store.dispatch(FETCH_USER_PROFILE);
        }
    }
</script>

<style lang="stylus">
    @import "./constants.styl"

    body {
        //background-color: $bgColor;
        background: repeating-linear-gradient(
                45deg,
                #ffffff 0px,
                #ffffff 2px,
                rgba(255, 209, 128, 0.3) 2px,
                rgba(255, 209, 128, 0.2) 6px
        ),
        url(./assets/bg.png);
    }

    #app {
        top: 0px

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
            padding-left 1em;
        }

        nav {
            a.router-link,
            span.router-link {
                text-decoration none
                color blue
                padding 12px
                display flex
                flex-direction column
                justify-content center
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
                transform: scale(1.5, 1.5);
                transition: 0.1s all;
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
        }

    }

    div.offline-indicator {
        background: #ff6762;
        color: white;
    }
</style>
