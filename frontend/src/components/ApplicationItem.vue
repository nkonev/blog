<template>
    <div class="application">
        <h2 :id="this.application.id"><a :href="'#'+this.application.id">#</a> {{this.application.title}}</h2>
        <div class="application-container" >
            <iframe :id="getId()" scrolling="no" :src="this.application.srcUrl"/>
        </div>
    </div>
</template>

<script>
    import bus, {LOGIN, LOGOUT} from '../bus'

    export default {
        props: ['application'],
        methods: {
            getId(){
                return 'ifr_'+this.$props.application.id;
            },
            onSuccessLogin() {
                const iframeEl = document.getElementById(this.getId());
                iframeEl.contentWindow.postMessage(LOGIN, '*');
            },
            onSuccessLogout() {
                const iframeEl = document.getElementById(this.getId());
                iframeEl.contentWindow.postMessage(LOGOUT, '*');
            },
        },
        created(){
            bus.$on(LOGIN, this.onSuccessLogin);
            bus.$on(LOGOUT, this.onSuccessLogout);
        },
        destroyed() {
            bus.$off(LOGIN, this.onSuccessLogin);
            bus.$off(LOGOUT, this.onSuccessLogout);
        },
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