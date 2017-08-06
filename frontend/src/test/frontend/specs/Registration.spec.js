import '../common'
import Vue from 'vue'
import Registration from "../../../main/frontend/components/Registration.vue"
import CommonTestUtils from "../CommonTestUtils" // todo remove it
import { mount } from 'avoriaz';

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


    // https://scotch.io/tutorials/how-to-write-a-unit-test-for-vuejs
    it("email ok", function(done) {

        vm.$data.profile.email='good@mail.co';
        vm.$data.profile.password='123456';
        vm.$data.profile.login='lol';

        Vue.nextTick(() => {
            expect($("input#login").val()).toBe('lol');
            expect($("input#password").val()).toBe('123456');
            expect($("input#email").val()).toBe('good@mail.co');

            expect(vm.$data.submitEnabled).toBe(true);
            done();
        });
    });

    it("email and password fail", function(done) {
        const RegistrationComponent = mount(Registration);

        // set input value
        RegistrationComponent.setData({
            profile: {
                email: "wrong mail no",
                password: '12345',
                login: 'lol'
            },
        });

        Vue.nextTick(() => {
            // console.log(">>>", RegistrationComponent.text());
            expect(RegistrationComponent.text()).toContain('Email is invalid');
            expect(RegistrationComponent.text()).toContain('password must be logger than');
            expect(RegistrationComponent.data().profile.email).toBe('wrong mail no');
            expect(vm.$data.submitEnabled).toBe(false);
            done();
        });
    });

});