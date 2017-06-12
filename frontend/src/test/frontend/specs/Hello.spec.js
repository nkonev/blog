import Vue from 'vue'
import User from "../../../main/frontend/components/User.vue"

// вспомогательная функция, выполняющая монтирование и
// возвращающая строку с результатами рендеринга
function getRenderedText (Component, propsData) {
    const Ctor = Vue.extend(Component);
    const vm = new Ctor({ propsData: propsData }).$mount();
    return vm.$el.textContent;
}

describe("autocomplete", function(){

    beforeEach(function() {
        setFixtures(`<div id="app"/>`);
    });


    it("Отрисовка пользователя", function() {
        expect(getRenderedText(User, {
            id: 2
        })).toBe('Пользователь ' + 2)
    });

});