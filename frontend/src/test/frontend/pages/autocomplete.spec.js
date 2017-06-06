
describe("autocomplete", function(){
    var $el, request;

    beforeEach(function() {
        jasmine.Ajax.install();
        jasmine.clock().install();

        jasmine.getJSONFixtures().clearCache();

        setFixtures(`<div id="app"/>`);

        // draw autocomplete widget
        require("../../../main/frontend/pages/autocomplete/autocomplete");
        $el = $('#countries-list');
    });

    afterEach(function() {
        jasmine.clock().uninstall();
        jasmine.Ajax.uninstall();
    });

    it("Ввод буквы U", function() {
        expect($el).toBeInDOM();

        $el.val("U").keydown();

        jasmine.clock().tick(2000);

        request = jasmine.Ajax.requests.mostRecent();
        expect(request.url).toBe('/api/autocomplete?prefix=U');
        expect(request.method).toBe('GET');

        request.respondWith({
            "status": 200,
            "contentType": 'application/json;charset=UTF-8',
            "responseText": '["Uganda", "United States"]'
        });

        expect($('li:contains("Uganda")')).toBeInDOM();
        expect($('li:contains("United States")')).toBeInDOM();
    });

});