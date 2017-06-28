import Vue from 'vue'
Vue.config.devtools = false;
Vue.config.productionTip = false;

import Registration from "../../../main/frontend/components/Registration.vue"
import CommonTestUtils from "../CommonTestUtils"
import VeeValidate from 'vee-validate';


describe("Registration2.vue", function(){
    var $el, request, $login, $email, $password, $submit, vm;

    beforeEach(function() {
        jasmine.Ajax.install();

        jasmine.getJSONFixtures().clearCache();

        setFixtures(`<div id="app">This is app</div>`);

        vm = CommonTestUtils.mountToPageAndDraw(Registration, "#app");
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
    });



    it("all ok", function(done) {
        vm.$data.profile.email='good@mail.co';
        vm.$data.profile.password='1234';
        vm.$data.profile.login='lol';

        Vue.nextTick(() => {
            expect($("input#login").val()).toBe('lol');
            expect($("input#password").val()).toBe('1234');
            expect($("input#email").val()).toBe('good@mail.co');

            expect($("button#submit")).not.toHaveAttr('disabled');
            done();
        });
    });

    it("email fail", function(done) {
        vm.$data.profile.email='wrong mail no';
        vm.$data.profile.password='1234';
        vm.$data.profile.login='lol';

        Vue.nextTick(() => {
            //expect(vm.$el.querySelector('input#login').value).toBe('lol');
            expect($("input#login").val()).toBe('lol');
            expect($("input#password").val()).toBe('1234');
            expect($("input#email").val()).toBe('wrong mail no');
            expect($("button#submit")).toHaveAttr('disabled');
            done();
        });
    });

});