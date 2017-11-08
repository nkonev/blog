import Vue from 'vue'
import Registration from "../../src/components/Registration.vue"
import { mount } from 'vue-test-utils';

describe("Registration.vue", () => {

    let RegistrationWrapper;

    beforeEach(() => {
        RegistrationWrapper = mount(Registration, { attachToDocument: false });
        expect(RegistrationWrapper).toBeDefined();
        expect(RegistrationWrapper.vm.submitEnabled).toBe(false);

        jasmine.Ajax.install();
    });

    afterEach(() => {
        jasmine.Ajax.uninstall();
    });

    // https://scotch.io/tutorials/how-to-write-a-unit-test-for-vuejs
    it("email ok", (done) => {

        RegistrationWrapper.find('input#login').element.value = 'lol';
        RegistrationWrapper.find('input#login').trigger('input');

        RegistrationWrapper.find('input#email').element.value = 'good@mail.co';
        RegistrationWrapper.find('input#email').trigger('input');

        RegistrationWrapper.find('input#password').element.value = '123456';
        RegistrationWrapper.find('input#password').trigger('input');



        Vue.nextTick(() => {
            const submit = RegistrationWrapper.find('button#submit');

            // expect(submit.getAttribute('disabled')).not.toBe("disabled");

            expect(RegistrationWrapper.vm.submitEnabled).toBe(true);

            // simulate event

            submit.trigger('click');

            const request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/register');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({
                login: 'lol',
                password: '123456',
                email: 'good@mail.co'
            });
            request.respondWith({
                "status": 200,
                "contentType": 'application/json;charset=UTF-8',
                "responseText": '{}' // Firefox requires this
            });

            expect(RegistrationWrapper.vm.profile.email).toBe('good@mail.co');
            expect(RegistrationWrapper.vm.profile.password).toBe('123456');
            expect(RegistrationWrapper.vm.profile.login).toBe('lol');

            expect(RegistrationWrapper.vm.submitEnabled).toBe(false);

            done();
        });
    });

    it("email and password fail", (done) => {

        // set input value
        RegistrationWrapper.find('input#login').element.value = 'lol';
        RegistrationWrapper.find('input#login').trigger('input');

        RegistrationWrapper.find('input#email').element.value = 'wrong mail no';
        RegistrationWrapper.find('input#email').trigger('input');

        RegistrationWrapper.find('input#password').element.value = '12345';
        RegistrationWrapper.find('input#password').trigger('input');


        // Since Vue.js bindings update asynchronously, you should use Vue.nextTick() when asserting DOM updates after changing the data.
        Vue.nextTick(() => {
            // console.log(">>>", RegistrationWrapper.text());
            expect(RegistrationWrapper.text()).toContain('Invalid e-mail address');
            expect(RegistrationWrapper.text()).toContain('Minimum 6 characters');
            expect(RegistrationWrapper.vm.profile.email).toBe('wrong mail no');
            expect(RegistrationWrapper.vm.submitEnabled).toBe(false);
            done();
        });
    });

});