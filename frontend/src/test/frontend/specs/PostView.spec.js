import Vue from 'vue'
import PostView from "../../../main/frontend/components/PostView.vue"
import postFactory from "../../../main/frontend/factories/PostDtoFactory"
import { mount, shallow } from 'avoriaz';
import VueResource from 'vue-resource'
Vue.use(VueResource);

describe("PostView.vue", () => {
    let PostViewWrapper;
    beforeEach(function() {
        jasmine.Ajax.install();

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
                //routeId = o.params.id;
                $route.params.id = o.params.id;
            }
        };

        PostViewWrapper = shallow(PostView, {
            globals: { $router,  $route },
            instance
        });
        expect(PostViewWrapper).toBeDefined();
    });

    afterEach(function() {
        jasmine.Ajax.uninstall();
        PostViewWrapper = null;
    });


    it("tap left", (done) => {
        const postDto = postFactory();
        postDto.id = 1234;
        postDto.left = 1233;
        postDto.right = 1235;

        PostViewWrapper.setData({
            isLoading: false,
            postDTO: postDto,
        });

        PostViewWrapper.setProps({
            onGetPostSuccess: (pd)=>{
                expect(pd.id).toBe(1233);
                done();
            }
        });

        PostViewWrapper.vm.goLeft();
        const requestL = jasmine.Ajax.requests.mostRecent();
        expect(requestL.url).toBe('/api/post/1233');
        expect(requestL.method).toBe('GET');
        requestL.respondWith({
            "status": 200,
            "contentType": 'application/json;charset=UTF-8',
            "responseText": `{
                "id": 1233,
                "title": "Title lefter",
                "text": "Text with html",
                "titleImg": "data-png",
                "canEdit": false,
                "canDelete": false,
                "left": 1232,
                "right": 1234
            }`
        });

    });

    it("tap right", (done) => {
        const postDto = postFactory();
        postDto.id = 1234;
        postDto.left = 1233;
        postDto.right = 1235;

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