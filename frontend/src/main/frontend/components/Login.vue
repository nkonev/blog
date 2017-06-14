<template>
    <div class="login">
        <h1>Пожалуйста, представьтесь</h1>
        <form id="form-login" action="/login" method="post" >
            <label for="username">Username: </label><input id="username" name="username" v-model="formUsername"><br/>
            <label for="password">Password:</label><input id="password" name="password" v-model="formPassword"><br/>
            <button type="submit" @click.prevent="doLogin">Login!</button>
        </form>
    </div>

</template>

<script>
    import router from '../router'
    import {root} from '../router'

    export default {
        data() {
            return {
                formUsername: '',
                formPassword: ''
            }
        },
        methods:{
            doLogin() {
                const options = { emulateJSON: true }; // for pass as form data
                this.$http.post('/login', {username: this.formUsername, password: this.formPassword}, options).then(response => {
                    // get body data
                    router.push(root);
                }, response => {
                    // error callback
                    alert('Booh! Wrong credentials, try again!');
                });

            }
        }
    }
</script>