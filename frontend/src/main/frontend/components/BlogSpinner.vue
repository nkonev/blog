<template>
    <spinner class="send-spinner" :line-size="10" :spacing="20" :speed="0.4" size="55" :font-size="20" :message="message"></spinner>
</template>

<script>
    import Spinner from 'vue-simple-spinner'  // https://github.com/dzwillia/vue-simple-spinner/blob/master/examples-src/App.vue

    export default {
        name: 'blog-spinner',
        props: ['message'],
        components: {
            Spinner
        },
    }
</script>

<style lang="stylus" scoped>
    .send-spinner {
        margin: 2px;
    }
</style>