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

        RegistrationWrapper.setData({
            profile: {
                email: "good@mail.co",
                password: '123456',
                login: 'lol'
            },
        });


        Vue.nextTick(() => {
            expect(RegistrationWrapper.data().submitEnabled).toBe(true);

            // simulate event
            const submit = RegistrationWrapper.find('button#submit')[0];
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
        RegistrationWrapper.setData({
            profile: {
                email: "wrong mail no",
                password: '12345',
                login: 'lol'
            },
        });

        // Since Vue.js bindings update asynchronously, you should use Vue.nextTick() when asserting DOM updates after changing the data.
        Vue.nextTick(() => {
            // console.log(">>>", RegistrationWrapper.text());
            expect(RegistrationWrapper.text()).toContain('Email is invalid');
            expect(RegistrationWrapper.text()).toContain('password must be logger than');
            expect(RegistrationWrapper.data().profile.email).toBe('wrong mail no');
            expect(RegistrationWrapper.data().submitEnabled).toBe(false);
            done();
        });
    });

});