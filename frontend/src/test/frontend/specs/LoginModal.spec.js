import Vue from 'vue'
import LoginModal from "../../../main/frontend/components/LoginModal.vue"
import { mount } from 'avoriaz';

describe("LoginModal.vue", function(){

    beforeEach(function() {
        jasmine.Ajax.install();
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
    });

    // https://scotch.io/tutorials/how-to-write-a-unit-test-for-vuejs
    it("login ok", function(done) {
        const LoginModalWrapper = mount(LoginModal, { attachToDocument: false });
        expect(LoginModalWrapper).toBeDefined();

        expect(LoginModalWrapper.data().formError).toBe(null);

        LoginModalWrapper.setData({
            formUsername: 'lol',
            formPassword: '123456',
        });


        Vue.nextTick(() => {
            expect(LoginModalWrapper.data().formError).toBe(null);

            // simulate event
            const submit = LoginModalWrapper.find('button#btn-submit')[0];
            submit.trigger('click');

            const request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/login');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({
                username: ['lol'],
                password: ['123456'],
            });
            request.respondWith({
                "status": 200,
                "contentType": 'application/json;charset=UTF-8',
                "responseText": '{}' // Firefox requires this
            });

            expect(LoginModalWrapper.data().formUsername).toBe('lol');
            expect(LoginModalWrapper.data().formPassword).toBe('123456');
            expect(LoginModalWrapper.data().formError).toBe(null);

            done();
        });
    });

    xit("login with incorrect credentials", function(done) {
        done();
    });

});