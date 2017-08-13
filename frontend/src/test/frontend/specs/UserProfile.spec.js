import UserProfile from "../../../main/frontend/components/UserProfile.vue"
import { mount } from 'avoriaz';

describe("UserProfile.vue", function(){

    it("render", function() {
        expect(UserProfile).toBeDefined();
        const UserProfileWrapper = mount(UserProfile, { attachToDocument: false });
        UserProfileWrapper.setProps({
            id: 2,
        });
        expect(UserProfileWrapper.text()).toContain('Пользователь 2');
    });

});