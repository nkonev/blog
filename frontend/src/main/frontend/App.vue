<template>
  <div id="app">
    <img src="./assets/logo.png">
    <nav>
        <router-link to="/login">Login</router-link>
        <router-link to="/">Index</router-link>
        <router-link to="/users">Users</router-link>
        <router-link id="a-api" to="/helloween">helloween</router-link>
        <router-link to="/not/found">Not found</router-link>
        <a id="a-doc" href="/docs/index.html">Docs</a>
    </nav>
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'app'
}
</script>

<style lang="stylus">
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;

  nav {
      a.router-link-exact-active {
          color white
          background-color #003eff
      }
  }
}
</style>
