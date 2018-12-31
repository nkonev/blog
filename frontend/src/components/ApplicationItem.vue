<template>
    <div class="application">
        <h2 :id="this.application.id"><a :href="'#'+this.application.id">#</a> {{this.application.title}}</h2>
        <div class="application-container" >
            <iframe :src="this.application.srcUrl"/>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['application'],
    }
</script>

<style lang="stylus">
    .application {
        h2 {
            margin 1em 0 0.2em 0
        }


        &-container {
            position: relative;
            width: 100%;
            height: 0;
            padding-bottom: 56.25%;


            iframe {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
            }
        }
    }
</style>