import Vue from 'vue'
import Vuex from 'vuex'
import LoginModal from "../../src/components/LoginModal.vue"
import { mount, shallow, createLocalVue } from 'vue-test-utils';

const localVue = createLocalVue();
localVue.use(Vuex);

// https://jasmine.github.io/2.0/introduction.html
describe("LoginModal.vue", () => {

    let LoginModalWrapper;

    const $httpSuccess = {
        post(){
            // TODO assert method args
            /*
            expect(request.url).toBe('/api/login');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({
            username: ['lol'],
            password: ['123456'],
             */
            return Promise.resolve({body: { message: "success" }});
        }
    };

    const $httpFail = {
        post(){
            return Promise.reject({
                body: { message: "bad credentialz"}
            });
        }
    };

    const prepareWrapper = (LoginModalWrapper) => {
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
    };

    let actions;
    let store;
    beforeEach(() => {
        actions = {
            fetchUserProfile: () => {
                console.log('Mocked fetchUserProfile');
            },
        };
        store = new Vuex.Store({
            state: {},
            actions
        });
    });

    afterEach(() => {
    });

    it("login ok", (done) => {
        LoginModalWrapper = shallow(
            LoginModal,
            {
                attachToDocument: false,
                mocks: {
                    $http: $httpSuccess
                },
                store, localVue
            }
        );
        prepareWrapper(LoginModalWrapper);

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

        expect(LoginModalWrapper.vm.formUsername).toBe('lol');
        expect(LoginModalWrapper.vm.formPassword).toBe('123456');
        expect(LoginModalWrapper.vm.formError).toBe(null);

        // done();
    });

    it("login with incorrect credentials", (done) => {
        LoginModalWrapper = shallow(
            LoginModal,
            {
                attachToDocument: false,
                mocks: {
                    $http: $httpFail
                },
                store, localVue
            }
        );
        prepareWrapper(LoginModalWrapper);

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
    });

});