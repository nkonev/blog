import UserProfile from "../../src/components/UserProfile.vue"
import { mount, RouterLinkStub } from '@vue/test-utils';
import Vue from 'vue'

describe("UserProfile.vue", () => {

    it("render", (done) => {
        expect(UserProfile).toBeDefined();
        const $http = {
            get(){
                return new Promise(success => {
                    console.log('call success promise (/user/get)');
                    success( {
                        body: {
                            id: 2,
                            login: 'testor'
                        }
                    })
                }, fail => {
                    console.error('call fail promise (/user/get)');
                })
            }
        };
        const UserProfileWrapper = mount(
            UserProfile,
            {
                attachToDocument: false,
                mocks: { $http },
                propsData: {
                    id: 2,
                    onFetchSuccess: () => {
                        expect(UserProfileWrapper.text()).toContain('Viewing profile #2');
                        expect(UserProfileWrapper.text()).toContain('testor');
                        done();
                    }
                },
                stubs: ['RouterLink', 'UserPostList' ]
            }
        );

    });

});