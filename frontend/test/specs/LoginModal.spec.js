import Vue from 'vue'
import LoginModal from "../../src/components/LoginModal.vue"
import { mount } from 'vue-test-utils';

// https://jasmine.github.io/2.0/introduction.html
describe("LoginModal.vue", () => {

    let LoginModalWrapper;

    beforeEach(() => {
        LoginModalWrapper = mount(LoginModal, { attachToDocument: false });
        expect(LoginModalWrapper).toBeDefined();
        expect(LoginModalWrapper.vm.formError).toBe(null);

        LoginModalWrapper.vm.$modal = {
            show() {
                console.log('[login fake modal] I am opened!!!');
            },
            hide() {
                console.log('[login fake modal] I am hided!!!');
            }
        };

        spyOn(LoginModalWrapper.vm.$modal, 'hide').and.callThrough();
        spyOn(Vue.http, 'post').and.callThrough();

        jasmine.Ajax.install();
    });

    afterEach(() => {
        jasmine.Ajax.uninstall();
    });

    it("login ok", (done) => {
        LoginModalWrapper.setProps({
            onSuccessCallback: ()=> {
                expect(LoginModalWrapper.vm.$modal.hide).toHaveBeenCalled();
                done();
            },
        });
        expect(LoginModalWrapper.vm.formError).toBe(null);

        LoginModalWrapper.setData({
            formUsername: 'lol',
            formPassword: '123456',
        });

        expect(LoginModalWrapper.vm.formError).toBe(null);

        // simulate event
        const submit = LoginModalWrapper.find('button#btn-submit');
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

        expect(LoginModalWrapper.vm.formUsername).toBe('lol');
        expect(LoginModalWrapper.vm.formPassword).toBe('123456');
        expect(LoginModalWrapper.vm.formError).toBe(null);

        // done();
    });

    it("login with incorrect credentials", (done) => {
        LoginModalWrapper.setData({
            formUsername: 'lol',
            formPassword: '123456',
        });
        expect(LoginModalWrapper.vm.formError).toBe(null);

        LoginModalWrapper.setProps({
            onFailCallback: ()=> {
                expect(LoginModalWrapper.vm.formUsername).toBe('lol');
                expect(LoginModalWrapper.vm.formPassword).toBe('123456');
                expect(LoginModalWrapper.vm.formError).toBe('bad credentialz');
                done();
            },
        });

        // simulate event
        const submit = LoginModalWrapper.find('button#btn-submit');
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

        expect(Vue.http.post).toHaveBeenCalled(); // yes, it can be here
    });

});