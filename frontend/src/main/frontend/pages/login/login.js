import $ from 'jquery';
import CookieService from '../../services/CookieService';

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
            CookieService.getLocationAnd(
                function(loc){
                    window.location = loc;
                }
            )
        }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Booh! Wrong credentials, try again!');
        });
    });
});