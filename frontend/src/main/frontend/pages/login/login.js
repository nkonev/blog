import $ from 'jquery';
import 'jquery.cookie';

$(document).ready(function ($) {
    $('#loginform').submit(function (event) {
        event.preventDefault();
        var data = 'username=' + $('#username').val() + '&password=' + $('#password').val();
        $.ajax({
            data: data,
            timeout: 1000,
            type: 'POST',
            url: '/login'

        }).done(function(data, textStatus, jqXHR) {
            var preLoginInfo = $.cookie('dashboard.pre.login.request');
            console.log("success", preLoginInfo);
            if (preLoginInfo) {
                window.location = preLoginInfo;
            }
        }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Booh! Wrong credentials, try again!');
        });
    });
});