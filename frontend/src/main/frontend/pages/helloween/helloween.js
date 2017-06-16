import $ from 'jquery';
import CookieService from '../../services/CookieService';

$(document).ready(function($) {
    $.ajax({
        type: 'GET',
        url: '/api/hello'
    }).done(function (data, textStatus, jqXHR) {
        $('#helloweenMessage').html(data.message);

    }).fail(function (jqXHR, textStatus, errorThrown) {
        if (jqXHR.status === 401) { // HTTP Status 401: Unauthorized
            CookieService.storeCurrentLocation();
            window.location = '/api/login.html';
        } else {
            alert('Houston, we have a problem...');
        }
    });
});