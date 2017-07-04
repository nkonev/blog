import '../common'
import Vue from 'vue'
import Autocomplete from "../../../main/frontend/components/Autocomplete.vue"
import CommonTestUtils from "../CommonTestUtils"


describe("autocomplete.js", function(){
    var $el, request, vm;

    beforeEach(function() {
        jasmine.Ajax.install();
        jasmine.clock().install();

        jasmine.getJSONFixtures().clearCache();

        setFixtures(`<div id="app"/>`);

        vm = CommonTestUtils.mountToPageAndDraw(Autocomplete, "#app");

        // draw autocomplete widget
        $el = $('.v-autocomplete-input');
    });

    afterEach(function() {
        jasmine.clock().uninstall();
        jasmine.Ajax.uninstall();
    });

    xit("Ввод префикса Unit", function(done) {
        expect($el).toBeInDOM();

        $el.val("Unit").change().keyup();
        vm.$emit('input');
        vm.$emit('change', ["Unit"]);
        vm.$emit('update-items', ["Unit"]);



        Vue.nextTick(() => {
            jasmine.clock().tick(2000);


            request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/public/autocomplete?prefix=Unit');
            expect(request.method).toBe('GET');

            request.respondWith({
                "status": 200,
                "contentType": 'application/json;charset=UTF-8',
                "responseText": '["United Arab Emirates", "United States"]'
            });

            expect($('li:contains("United Arab Emirates")')).toBeInDOM();
            expect($('li:contains("United States")')).toBeInDOM();

            done();
        });
    });

});