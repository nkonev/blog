import UserProfile from "../../../main/frontend/components/UserProfile.vue"
import { mount } from 'avoriaz';

describe("UserProfile.vue", function(){

    it("render", function() {
        expect(UserProfile).toBeDefined();
        const $http = {
            get(){
                return new Promise(success => {
                    console.log('call success promise (/user/get)');
                    return {
                        body: {

                        }
                    }
                }, fail => {
                    console.error('call fail promise (/user/get)');
                })
            }
        };
        const UserProfileWrapper = mount(UserProfile, { attachToDocument: false, globals: { $http } });
        UserProfileWrapper.setProps({
            id: 2,
        });
        expect(UserProfileWrapper.text()).toContain('# 2');
    });

});