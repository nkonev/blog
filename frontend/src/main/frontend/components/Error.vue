<template>
    <span class="help-block">{{ message }}</span>
</template>

<script>
    export default {
        props: ['message']
    }
</script>

<style lang="stylus">
    @import "../constants.styl"

    .help-block {
        margin-left 4px;
        // position absolute
        color $errorColor
    }
</style>