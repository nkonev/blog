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

        LoginModalWrapper.vm.$modal = {
            show() {
                console.log('[login fake modal] I am opened!!!');
            },
            hide() {
                console.log('[login fake modal] I am hided!!!');
            }
        };

        spyOn(LoginModalWrapper.vm.$modal, 'hide').and.callThrough();

        LoginModalWrapper.setProps({
            onSuccessCallback: ()=> {
                LoginModalWrapper.update();

                expect(LoginModalWrapper.vm.$modal.hide).toHaveBeenCalled();

                done();
            },
        });

        expect(LoginModalWrapper.data().formError).toBe(null);

        LoginModalWrapper.setData({
            formUsername: 'lol',
            formPassword: '123456',
        });


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

        // done();
    });

    it("login with incorrect credentials", function(done) {
        const LoginModalWrapper = mount(LoginModal, { attachToDocument: false });
        expect(LoginModalWrapper).toBeDefined();

        expect(LoginModalWrapper.data().formError).toBe(null);

        LoginModalWrapper.setData({
            formUsername: 'lol',
            formPassword: '123456',
        });
        expect(LoginModalWrapper.data().formError).toBe(null);

        spyOn(Vue.http, 'post').and.callThrough();

        LoginModalWrapper.setProps({
            onFailCallback: ()=> {
                LoginModalWrapper.update();

                expect(LoginModalWrapper.data().formUsername).toBe('lol');
                expect(LoginModalWrapper.data().formPassword).toBe('123456');
                expect(LoginModalWrapper.data().formError).toBe('bad credentialz');

                done();
            },
        });

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
            "status": 401,
            "contentType": 'application/json;charset=UTF-8',
            "responseText": '{ "message": "bad credentialz"} ' // Firefox requires this
        });

        expect(Vue.http.post).toHaveBeenCalled();
    });

});