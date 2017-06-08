import $ from 'jquery';
import 'jquery.cookie';

$(document).ready(function($) {
    $.ajax({
        type: 'GET',
        url: '/rest/hello'

    }).done(function (data, textStatus, jqXHR) {
        $('#helloweenMessage').html(data.message);

    }).fail(function (jqXHR, textStatus, errorThrown) {
        if (jqXHR.status === 401) { // HTTP Status 401: Unauthorized
            var preLoginInfo = window.location;
            console.log("storing", preLoginInfo);
            $.cookie('dashboard.pre.login.request', preLoginInfo);
            window.location = '/login.html';
        } else {
            alert('Houston, we have a problem...');
        }
    });
});