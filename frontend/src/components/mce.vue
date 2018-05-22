<template>
    <div>
    <h1>hi there</h1>
    <tinymce id="d111" v-model="data"></tinymce>
    </div>
</template>

<script>
import Vue from 'vue'
import tinymce from 'vue-tinymce-editor'
Vue.component('tinymce', tinymce);

export default{
    data(){
        return {
            data : ''
        };
    },
}

</script>