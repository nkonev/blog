<template>
    <div>
        <h1>hi there</h1>
        <tinymce id="lolol" v-model="data"></tinymce>
        <div>{{ data }}</div>
    </div>
</template>

<script>
    import Vue from 'vue'
    import tinymce from './TinymceVue'
    // Vue.component('tinymce', tinymce);

    export default{
        components: {
            tinymce
        },
        data(){
            return {
                data : '<p>Hello from tinymce</p>'
            };
        },
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
