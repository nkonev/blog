import Vue from 'vue'
import PostView from "../../../main/frontend/components/PostView.vue"
import postFactory from "../../../main/frontend/factories/PostDtoFactory"
import { mount, shallow } from 'avoriaz';


describe("PostView.vue", () => {
    let PostViewWrapper;
    beforeEach(function() {
        jasmine.Ajax.install();
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
        PostViewWrapper = null;
    });

    it("tap right", (done) => {

        const postDto = postFactory();
        postDto.id = 1234;
        postDto.left = 1233;
        postDto.right = 1235;

        const instance = Vue.extend();

        let routeId = 1234;
        const $route = {
            params: {
                id: routeId
            }
        };
        const $router = {
            push(o) {
                console.log("router push ", o);
                routeId = o.params.id;
            }
        };

        PostViewWrapper = shallow(PostView, {
            globals: { $router,  $route },
            instance
        });
        expect(PostViewWrapper).toBeDefined();


        PostViewWrapper.setData({
            isLoading: false,
            postDTO: postDto,
        });

        PostViewWrapper.setProps({
            onGetPostSuccess: (pd)=>{
                expect(pd.id).toBe(1235);
                done();
            }
        });

        PostViewWrapper.vm.goRight();
        const requestR = jasmine.Ajax.requests.mostRecent();
        expect(requestR.url).toBe('/api/post/1235');
        expect(requestR.method).toBe('GET');
        requestR.respondWith({
            "status": 200,
            "contentType": 'application/json;charset=UTF-8',
            "responseText": `{
                "id": 1235,
                "title": "Title original",
                "text": "Text with html",
                "titleImg": "data-png",
                "canEdit": false,
                "canDelete": false,
                "left": 1234,
                "right": 1236
            }`
        });

    });

});