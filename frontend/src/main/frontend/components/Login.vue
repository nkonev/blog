<template>
    <div class="login">
        <h1>Пожалуйста, представьтесь</h1>
        <form id="loginform" name="loginform" action="/login" method="post">
            <label for="username">Username: </label><input id="username" name="username"><br/>
            <label for="password">Password:</label><input id="password" name="password"><br/>
            <button type="submit" @click.prevent="doLogin()">Login!</button>
        </form>
    </div>

</template>

<script>
    import $ from 'jquery';
    import 'jquery.cookie';

    export default {
        methods:{
            doLogin() {
                console.debug('doLogin');
                const post_url = $(this).attr("action");
                const request_method = $(this).attr("method");
                const data = $(this).serialize();

                $.ajax({
                    data: data,
                    url: post_url,
                    type: request_method,

                }).done(function(data, textStatus, jqXHR) {
                    const preLoginInfo = $.cookie('dashboard.pre.login.request');
                    console.log("success", preLoginInfo);
                    if (preLoginInfo) {
                        window.location = preLoginInfo;
                    }
                }).fail(function(jqXHR, textStatus, errorThrown) {
                    alert('Booh! Wrong credentials, try again!');
                });
            }
        }
    }
</script>