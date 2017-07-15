import '../common'
import UserProfile from "../../../main/frontend/components/UserProfile.vue"
import CommonTestUtils from "../CommonTestUtils"


describe("UserProfile.vue", function(){

    beforeEach(function() {
        setFixtures(`<div id="app"/>`);
    });


    xit("Отрисовка профиля пользователя", function() {
        expect(CommonTestUtils.getRenderedText(UserProfile, {
            id: 2
        })).toContainText(' Пользователь ' + 2)
    });

});