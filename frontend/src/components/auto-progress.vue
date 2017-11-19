<template>
    <div><vue-progress ref="progressbar"></vue-progress></div>
</template>

<script>
    import vueProgress from 'vue-top-progress'
    import Vue from 'vue'

    export default {
        name: "Progressbar",
        components: { vueProgress },
        props:['excludedUrls'],
        mounted() {
            var progress = this.$refs.progressbar;

            var listener = {
                tempOpen: XMLHttpRequest.prototype.open,
                tempSend: XMLHttpRequest.prototype.send,
                callback(){ }
            };

            var excludedVal = this.$props.excludedUrls;
            var excludedRegexp = excludedVal.map(currentVal => new RegExp(currentVal, "i"));

            var canUseProgress = function(url){
                return !excludedRegexp.find(el => el.test(url))
            };

            var startProgress = function(){
                progress.start();
            };

            XMLHttpRequest.prototype.open = function(a='',b='') {
                if (excludedVal) {
                    if (canUseProgress(b)) {
                        startProgress();
                    }
                } else {
                    startProgress();
                }
                listener.tempOpen.apply(this, arguments);
                listener.method = a;
                listener.url = b;
                if (a.toLowerCase() == 'get') {
                    listener.data = b.split('?');
                    listener.data = listener.data[1];
                }
            };

            var doneProgress = function(){
                setTimeout(() => {
                    progress.done()
                }, 500);
            };

            XMLHttpRequest.prototype.send = function(a='',b='') {
                if (excludedVal) {
                    if (canUseProgress(listener.url)) {
                        doneProgress();
                    }
                } else {
                    doneProgress();
                }

                listener.tempSend.apply(this, arguments);
                if(listener.method.toLowerCase() == 'post') { listener.data = a };
                listener.callback();
            };
        }
    }
</script>
