<template>
    <div class="application-list">
        <ApplicationItem v-for="application in applications" v-bind:application="application" :key="application.id"></ApplicationItem>
    </div>
</template>

<script>
    import ApplicationItem from "./ApplicationItem.vue"
    import {fixScroll} from '../utils.js'
    export default {
        data(){
            return {
                applications:[]
            }
        },
        created(){
            this.$http.get('/api/application').then(
                response => {
                    this.applications = response.body;
                    fixScroll();
                },
                failResponse => {

                }
            )
        },
        components:{
            ApplicationItem: ApplicationItem
        }
    }
</script>
