import Vue from 'vue'
import Search from "../../src/components/Search.vue"
import { mount } from '@vue/test-utils';

describe('Search.vue', ()=>{
    let wrapper;

    beforeEach(()=>{
        wrapper = mount(Search);
        expect(wrapper).toBeDefined();
    });
    afterEach(()=>{

    });

    it('emit event', (done)=>{
        wrapper.vm.$on('SEARCH_EVENT', (str)=>{
            expect(str).toBe('good day for die');
            done();
        });

        const el = wrapper.find("input.search-input");
        el.element.value = 'good day for die';
        el.trigger('input');
    });

    it('emit event clear', (done)=>{
        wrapper.setData({
            searchString: 'dirty due previous search'
        });

        wrapper.vm.$on('SEARCH_EVENT', (str)=>{
            expect(str).toBe('');
            done();
        });

        const el = wrapper.find(".search-clear");
        el.trigger('click');
    });
});