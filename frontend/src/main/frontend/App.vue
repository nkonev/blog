<template>
    <div id="app">
        <OfflineIndicator message="Oh no, you're offline :("></OfflineIndicator>
        <LoginModal></LoginModal>

        <img src="./assets/logo.png">
        <nav>
            <router-link to="/">Index</router-link>
            <router-link to="/users">Users</router-link>
            <router-link id="a-api" to="/helloween">helloween</router-link>
            <router-link to="/not/found">Not found</router-link>
            <router-link to="/autocomplete">Autocomplete</router-link>
            <a id="a-doc" href="/docs/index.html">Docs</a>
            <router-link to="/registration">Registration</router-link>
        </nav>
        <router-view></router-view>
  </div>
</template>

<script>
    import Vue from 'vue';
    import { OfflineIndicator } from 'vue-online';
    import LoginModal       from './components/LoginModal.vue';
    import vmodal from 'vue-js-modal'

    Vue.use(vmodal);

    export default {
        name: 'app',
        methods: {

        },
        // used components for provide custom tags
        components: {
            OfflineIndicator,
            LoginModal
        }
    }
</script>

<style lang="stylus">
    #app {
      font-family: 'Avenir', Helvetica, Arial, sans-serif;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;

      // center content
      width 969px
      background-color: rgba(255, 0, 0, 0.27);
      position: absolute;
      left: 0;
      right: 0;
      margin: auto;

      color: #2c3e50;
      margin-top: 60px;

      nav {
          a.router-link-exact-active {
              color white
              background-color #003eff
          }
      }
    }

    div.offline-indicator {
        background: #ff6762;
        color: white;
    }
</style>
