import '../common'
import Vue from 'vue'
import Registration from "../../../main/frontend/components/Registration.vue"
import CommonTestUtils from "../CommonTestUtils"

describe("Registration.vue", function(){
    var vm;

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
            expect(vm.$el.textContent).toContainText('The email field must be a valid email');
            // expect($("button#submit")).toHaveAttr('disabled');
            done();
        });
    });

});