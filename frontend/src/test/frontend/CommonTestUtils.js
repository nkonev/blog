import Vue from 'vue'

export default {
    /**
     * Монтирует Vue-компонент Component в css-селектор selector, и русует на странице результат, так что он становится виден jQuery
     * @see https://vuejs.org/v2/api/
     * @param Component
     * @param selector
     */
    mountToPageAndDraw: function (Component, selector) {
        const Ctor = Vue.extend(Component);
        const vm = new Ctor();
        return vm.$mount(selector);
    },

    /**
     * вспомогательная функция, выполняющая монтирование и
     * возвращающая строку с результатами рендеринга
     * @param Component
     * @param propsData
     * @return {string}
     */
    getRenderedText: function (Component, propsData) {
        const Ctor = Vue.extend(Component);
        const vm = new Ctor({propsData: propsData}).$mount();
        return vm.$el.textContent;
    }
}