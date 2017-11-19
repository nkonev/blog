import Vue from 'vue'
import PostView from "../../src/components/PostView.vue"
import postFactory from "../../src/factories/PostDtoFactory"
import { mount, shallow } from 'vue-test-utils';

describe("PostView.vue", () => {
    let PostViewWrapper, $router, $route, checkGetPost;
    const $httpLeft = {
        get(url){
            if (checkGetPost) {
                expect(url).toBe('/api/post/1233');
            }
            return Promise.resolve({body: {
                "id": 1233,
                "title": "Title lefter",
                "text": "Text with html",
                "titleImg": "data-png",
                "canEdit": false,
                "canDelete": false,
                "left": 1232,
                "right": 1234,
                "owner": {
                    "id": 42
                }
            }});
        }
    };

    const $httpRight = {
        get(url){
            if (checkGetPost) {
                expect(url).toBe('/api/post/1235');
            }
            return Promise.resolve({body: {
                "id": 1235,
                "title": "Title righter",
                "text": "Text with html",
                "titleImg": "data-png",
                "canEdit": false,
                "canDelete": false,
                "left": 1234,
                "right": 1236,
                "owner": {
                    "id": 42
                }
            }});
        }
    };

    beforeEach(() => {
        let routeId = 1234;
        checkGetPost=false;
        $route = {
            params: {
                id: routeId
            }
        };
        $router = {
            push(o) {
                console.log("router push ", o);
                $route.params.id = o.params.id;
            }
        };

    });

    afterEach(() => {
        PostViewWrapper = null;
    });


    it("tap left", (done) => {
        const instance = Vue.extend();
        PostViewWrapper = shallow(PostView, {
            mocks: { $router,  $route, $http: $httpLeft },
            instance
        });
        expect(PostViewWrapper).toBeDefined();

        const postDto = postFactory();
        postDto.owner = {id: 42};
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
        checkGetPost = true;
    });

    it("tap right", (done) => {
        const instance = Vue.extend();
        PostViewWrapper = shallow(PostView, {
            mocks: { $router,  $route, $http: $httpRight },
            instance
        });
        expect(PostViewWrapper).toBeDefined();

        const postDto = postFactory();
        postDto.owner = {id: 42};
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
        checkGetPost = true;
    });

});