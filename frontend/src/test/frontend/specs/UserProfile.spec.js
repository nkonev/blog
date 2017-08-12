import Vue from 'vue';
import UserProfile from "../../../main/frontend/components/UserProfile.vue"
import { mount } from 'avoriaz';
const should = require('chai').should();

describe("UserProfile.vue", function(){

    it("should render correct contents", function() {

        should.exist(UserProfile);
        const UserProfileComponent = mount(UserProfile);
        UserProfileComponent.setProps({
            id: 2,
        });

        assert.include(UserProfileComponent.text(), 'Пользователь 2', 'rendering contain text');
    });

});