<template>
    <div>
        <h1>Пользователи</h1>
        <user-item v-for="user in users" v-bind:userDTO="user" :key="user.id"></user-item>
    </div>

</template>

<script>
    import UserItem from "./UserItem.vue";
    export default {
        components: {UserItem}, // говорим, из каких компонентов могут быть извлечены тэги
        data() {
            return {
                users: 'No users :(',
            }
        },
        created(){
            this.$http.get('/api/user').then(response => {
                this.users = response.body;
            }, response => {
                console.error(response);
                // alert(response);
            });
        }
    };
</script>
