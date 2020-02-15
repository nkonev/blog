import Vue from 'vue'
import PostEdit from "../../src/components/PostEdit.vue"
import { mount, shallowMount } from '@vue/test-utils';

describe("PostEdit.vue", () => {
    let PostEditWrapper, $route, checkGetPost;
    const postId = 1234;
    const $http = { };
    beforeEach(() => {
        checkGetPost=false;
        $route = {
            params: {
                id: postId
            }
        };
    });
    afterEach(() => {
        PostEditWrapper = null;
    });

    it("displays", (done) => {
        const instance = Vue.extend();
        const testText = 'test text';
        PostEditWrapper = shallowMount(PostEdit, {
            mocks: { $http: $http, $route },
            propsData: {
                postDTO: {
                    text: "Initial post text",
                },
                hljs: {
                    highlightAuto(t) {
                        expect(t).toEqual(testText);
                        done();
                        return t
                    }
                }
            },
            instance
        });
        PostEditWrapper.vm.editorOptions.modules.syntax.highlight(testText);
    });

});