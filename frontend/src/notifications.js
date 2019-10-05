import Vue from 'vue'
import VueNotifications from 'vue-notifications'
import miniToastr from 'mini-toastr'

// Here we setup messages output to `mini-toastr`
function toast ({title, message, type, timeout, cb}) {
    return miniToastr[type](message, title, timeout, cb)
}
VueNotifications.config.timeout = 4000;
// Activate plugin
Vue.use(VueNotifications, {
    success: toast,
    error: toast,
    info: toast,
    warn: toast
}); // VueNotifications have auto install but if we want to specify options we've got to do it manually.
miniToastr.init({types: {
    success: 'success',
    error: 'error',
    info: 'info',
    warn: 'warn'
}});


export default {
    unexpectedError(m, b, s) {
        miniToastr['error']('Unexpected server error occurred on '+m+' '+b + ' ' + s, 'Unexpected server error')
    },
    successfulLogin(){
        miniToastr['info']('You are successfully logged in')
    },
    successfulLogout(){
        miniToastr['info']('You are successfully logged out')
    },
    postCreated(postTitleAsMessage){
        miniToastr['info'](postTitleAsMessage, 'Created new post')
    }
}