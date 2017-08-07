import UserProfile from "../../../main/frontend/components/UserProfile.vue"
import { mount } from 'avoriaz';

describe("UserProfile.vue", function(){

    it("render", function() {
        expect(UserProfile).toBeDefined();
        const UserProfileComponent = mount(UserProfile);
        UserProfileComponent.setProps({
            id: 2,
        });
        expect(UserProfileComponent.text()).toContain('Пользователь 2');
    });

});