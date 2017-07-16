<template>
    <div id="app">
        <div id="content">
            <OfflineIndicator message="Oh no, you're offline :("></OfflineIndicator>
            <LoginModal></LoginModal>
            <auto-progress/>

            <img id="logo" src="./assets/logo.png">
            <nav>
                <router-link to="/" exact>Index</router-link>
                <router-link to="/users">Users</router-link>
                <router-link to="/not/found">Not found</router-link>
                <router-link to="/autocomplete">Autocomplete</router-link>
                <a id="a-doc" href="/docs/index.html">Docs</a>
                <router-link to="/registration">Registration</router-link>
                <router-link to="/lorem">Lorem ipsum</router-link>
                <router-link to="/posts">Posts</router-link>
                <router-link to="/post/edit">Edit</router-link>

                <user-profile-nav v-bind:currentUser="currentUser"/>
            </nav>
            <router-view></router-view>
        </div>
  </div>
</template>

<script>
    import Vue from 'vue';
    import { OfflineIndicator } from 'vue-online';
    import LoginModal       from './components/LoginModal.vue';
    import vmodal from 'vue-js-modal'
    import autoProgress from 'vue-auto-progress'
    import userProfileNav from './components/UserProfileNav.vue'
    import store from './store'
    import {GET_USER, FETCH_USER_PROFILE} from './store'
    import {mapGetters} from 'vuex'

    Vue.use(vmodal);

    export default {
        name: 'app',
        methods: {

        },
        store,
        computed: mapGetters({currentUser: GET_USER}), // currentUser is here, 'getUser' -- in store.js
        // used components for provide custom tags
        components: {
            OfflineIndicator,
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

    $contentWidth=969px
    body {
        background-color: $bgColor;
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
      margin-top: 60px;

      #content {
        padding: 4px;
      }

      nav {
          a {
              padding 12px
              display flex
              flex-direction column
              justify-content center
          }

          a.router-link-active {
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
