<template>
  <div id="app">
    <img src="./assets/logo.png">
    <nav>
        <router-link to="/login">Login</router-link>
        <router-link to="/">Index</router-link>
        <router-link to="/user/2">User 2</router-link>
        <router-link to="/user/3">User 3</router-link>
        <router-link to="/helloween">helloween</router-link>
        <router-link to="/not/found">Not found</router-link>
    </nav>
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'app'
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
