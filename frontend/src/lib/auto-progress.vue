<template>
    <div><vue-progress ref="progressbar"></vue-progress></div>
</template>

<script>
    import vueProgress from 'vue-top-progress'
    import Vue from 'vue'

    export default {
        name: "Progressbar",
        components: { vueProgress },
        props:['includedUrls'],
        mounted() {
            var progress = this.$refs.progressbar;

            var listener = {
                tempOpen: XMLHttpRequest.prototype.open,
                tempSend: XMLHttpRequest.prototype.send,
                callback(){ }
            };

            var includedVal = this.$props.includedUrls;
            var includedRegexp = includedVal.map(currentVal => new RegExp(currentVal, "i"));

            var canUseProgress = function(url){
                if (includedVal) {
                    return includedRegexp.find(el => el.test(url))
                } else {
                    return true
                }
            };

            var startProgress = function(){
                progress.start();
            };

            XMLHttpRequest.prototype.open = function(a='',b='') {
                if (canUseProgress(b)) {
                    console.log('starting autoprogress for ', b)
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
                if (canUseProgress(listener.url)) {
                    doneProgress();
                }

                listener.tempSend.apply(this, arguments);
                if(listener.method.toLowerCase() == 'post') { listener.data = a };
                listener.callback();
            };
        }
    }
</script>
