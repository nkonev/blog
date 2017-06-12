import $ from 'jquery';
import 'jquery.cookie';

const locCookieName = 'dashboard.pre.login.request';

export default {
    storeCurrentLocation() {
        $.cookie(locCookieName, window.location);
    },
    getLocationAnd(ok, notExists) {
        const loc = $.cookie(locCookieName);
        if (loc && ok) {
            ok(loc);
        } else if (notExists){
            notExists(loc);
        }
    }
}