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

    it("email fail", function() {
        const RegistrationComponent = mount(Registration);// set input value

        // set input value
        RegistrationComponent.setData({
            profile: {
                email: "wrong mail no",
                password: '1234',
                login: 'lol'
            },
        });

        // simulate event
        const emailInput = RegistrationComponent.find('input#email')[0];
        emailInput.trigger('blur'); // for appropriate event see in vee-validate sources or documentation

        expect(RegistrationComponent.text()).toContain('The email field must be a valid email.');
        expect(RegistrationComponent.data().profile.email).toBe('wrong mail no');
    });

});