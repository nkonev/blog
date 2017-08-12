import Vue from 'vue'
import Registration from "../../../main/frontend/components/Registration.vue"
import { mount } from 'avoriaz';

describe("Registration.vue", function(){

    beforeEach(function() {
        jasmine.Ajax.install();
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
    });

    // https://scotch.io/tutorials/how-to-write-a-unit-test-for-vuejs
    it("email ok", function(done) {
        const RegistrationComponent = mount(Registration, { attachToDocument: true });
        expect(RegistrationComponent).toBeDefined();

        expect(RegistrationComponent.data().submitEnabled).toBe(false);

        RegistrationComponent.setData({
            profile: {
                email: "good@mail.co",
                password: '123456',
                login: 'lol'
            },
        });

        // simulate event
        const submit = RegistrationComponent.find('button#submit')[0];
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


        Vue.nextTick(() => {
            expect(RegistrationComponent.data().profile.email).toBe('good@mail.co');
            expect(RegistrationComponent.data().profile.password).toBe('123456');
            expect(RegistrationComponent.data().profile.login).toBe('lol');

            expect(RegistrationComponent.data().submitEnabled).toBe(true);

            done();
        });
    });

    it("email and password fail", function(done) {
        const RegistrationComponent = mount(Registration);
        expect(RegistrationComponent).toBeDefined();

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
            expect(RegistrationComponent.data().submitEnabled).toBe(false);
            done();
        });
    });

});