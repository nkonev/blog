import Vue from 'vue'
Vue.config.devtools = false;
Vue.config.productionTip = false;

import UserProfile from "../../../main/frontend/components/UserProfile.vue"
import CommonTestUtils from "../CommonTestUtils"


describe("UserProfile.vue", function(){

    beforeEach(function() {
        setFixtures(`<div id="app"/>`);
    });


    it("Отрисовка профиля пользователя", function() {
        expect(CommonTestUtils.getRenderedText(UserProfile, {
            id: 2
        })).toBe('Пользователь ' + 2)
    });

});