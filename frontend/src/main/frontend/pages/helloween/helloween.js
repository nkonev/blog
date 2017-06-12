import $ from 'jquery';
import CookieService from '../../services/CookieService';

$(document).ready(function($) {
    $.ajax({
        type: 'GET',
        url: '/rest/hello'
    }).done(function (data, textStatus, jqXHR) {
        $('#helloweenMessage').html(data.message);

    }).fail(function (jqXHR, textStatus, errorThrown) {
        if (jqXHR.status === 401) { // HTTP Status 401: Unauthorized
            CookieService.storeCurrentLocation();
            window.location = '/login.html';
        } else {
            alert('Houston, we have a problem...');
        }
    });
});