<template>
    <div class="applications">
        <ApplicationItem v-for="application in applications" v-bind:application="application" :key="application.id"></ApplicationItem>
    </div>
</template>

<script>
    import ApplicationItem from "./ApplicationItem.vue"

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
