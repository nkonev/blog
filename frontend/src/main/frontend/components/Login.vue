<template>
    <div class="login">
        <h1>Пожалуйста, представьтесь</h1>
        <form id="form-login" action="/login" method="post" >
            <label for="username">Username: </label><input id="username" name="username"><br/>
            <label for="password">Password:</label><input id="password" name="password"><br/>
            <button type="submit" @click.prevent="doLogin">Login!</button>
        </form>
    </div>

</template>

<script>
    import $ from 'jquery';
    import 'jquery.cookie';
    import router from '../router'

    export default {
        methods:{
            doLogin() {
                const $el = this.$el;
                const $form = $($el).find('#form-login');
                const post_url = $form.attr("action");
                const request_method = $form.attr("method");
                const data = $form.serialize();
                const self = this;
                $.ajax({
                    data: data,
                    url: post_url,
                    type: request_method,

                }).done(function(data, textStatus, jqXHR) {
                    const preLoginInfo = $.cookie('dashboard.pre.login.request');
                    // console.log("success", data);
                    if (preLoginInfo) {
                        window.location = preLoginInfo;
                    } else {
                        router.push('/');
                    }
                }).fail(function(jqXHR, textStatus, errorThrown) {
                    alert('Booh! Wrong credentials, try again!');
                });
            }
        }
    }
</script>