import Vue from 'vue'
import UserProfile from "../../../main/frontend/components/UserProfile.vue"

// вспомогательная функция, выполняющая монтирование и
// возвращающая строку с результатами рендеринга
function getRenderedText (Component, propsData) {
    const Ctor = Vue.extend(Component);
    const vm = new Ctor({ propsData: propsData }).$mount();
    return vm.$el.textContent;
}

describe("UserProfile.vue", function(){

    beforeEach(function() {
        setFixtures(`<div id="app"/>`);
    });


    it("Отрисовка профиля пользователя", function() {
        expect(getRenderedText(UserProfile, {
            id: 2
        })).toBe('Пользователь ' + 2)
    });

});