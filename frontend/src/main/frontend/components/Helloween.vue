<template>
    <div>
        <h1>Тут будут данные</h1>
        {{someData}}
    </div>

</template>

<script>
    export default {
        data() {
            return {
                someData: 'No data :(',
            }
        },
        created(){
            this.$http.get('/rest/hello').then(response => {

                // get body data
                this.someData = response.body;

            }, response => {
                // error callback
            });
        }
    };
</script>
