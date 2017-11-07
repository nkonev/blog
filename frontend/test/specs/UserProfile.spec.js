import UserProfile from "../../src/components/UserProfile.vue"
import { mount } from 'avoriaz';
import Vue from 'vue'

describe("UserProfile.vue", function(){

    it("render", function(done) {
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
                globals: { $http },
                propsData: {
                    id: 2,
                    onFetchSuccess: () => {
                        UserProfileWrapper.update();// force update ui
                        expect(UserProfileWrapper.text()).toContain('Viewing profile #2');
                        expect(UserProfileWrapper.text()).toContain('testor');
                        done();
                    }
                },
            }
        );

    });

});