import Vue from 'vue'
import Registration from "../../../main/frontend/components/Registration.vue"
import { mount } from 'avoriaz';

describe("Registration.vue", function(){

    let RegistrationWrapper;

    beforeEach(function() {
        RegistrationWrapper = mount(Registration, { attachToDocument: false });
        expect(RegistrationWrapper).toBeDefined();
        expect(RegistrationWrapper.data().submitEnabled).toBe(false);

        jasmine.Ajax.install();
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
    });

    // https://scotch.io/tutorials/how-to-write-a-unit-test-for-vuejs
    it("email ok", function(done) {

        RegistrationWrapper.find('input#login')[0].element.value = 'lol';
        RegistrationWrapper.find('input#login')[0].trigger('input');

        RegistrationWrapper.find('input#email')[0].element.value = 'good@mail.co';
        RegistrationWrapper.find('input#email')[0].trigger('input');

        RegistrationWrapper.find('input#password')[0].element.value = '123456';
        RegistrationWrapper.find('input#password')[0].trigger('input');



        Vue.nextTick(() => {
            const submit = RegistrationWrapper.find('button#submit')[0];

            // expect(submit.getAttribute('disabled')).not.toBe("disabled");

            expect(RegistrationWrapper.data().submitEnabled).toBe(true);

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

            expect(RegistrationWrapper.data().profile.email).toBe('good@mail.co');
            expect(RegistrationWrapper.data().profile.password).toBe('123456');
            expect(RegistrationWrapper.data().profile.login).toBe('lol');

            expect(RegistrationWrapper.data().submitEnabled).toBe(false);

            done();
        });
    });

    it("email and password fail", function(done) {

        // set input value
        RegistrationWrapper.find('input#login')[0].element.value = 'lol';
        RegistrationWrapper.find('input#login')[0].trigger('input');

        RegistrationWrapper.find('input#email')[0].element.value = 'wrong mail no';
        RegistrationWrapper.find('input#email')[0].trigger('input');

        RegistrationWrapper.find('input#password')[0].element.value = '12345';
        RegistrationWrapper.find('input#password')[0].trigger('input');


        // Since Vue.js bindings update asynchronously, you should use Vue.nextTick() when asserting DOM updates after changing the data.
        Vue.nextTick(() => {
            // console.log(">>>", RegistrationWrapper.text());
            expect(RegistrationWrapper.text()).toContain('Invalid e-mail address');
            expect(RegistrationWrapper.text()).toContain('Minimum 6 characters');
            expect(RegistrationWrapper.data().profile.email).toBe('wrong mail no');
            expect(RegistrationWrapper.data().submitEnabled).toBe(false);
            done();
        });
    });

});