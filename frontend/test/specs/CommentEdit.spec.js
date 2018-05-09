import Vue from 'vue'
import CommentEdit from "../../src/components/CommentEdit.vue"
import { mount, shallow } from '@vue/test-utils';

describe("CommentEdit.vue", () => {
    let CommentEditWrapper, $router, $route, checkGetPost;
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
        CommentEditWrapper = null;
    });

    it("update", (done) => {
        const instance = Vue.extend();
        CommentEditWrapper = shallow(CommentEdit, {
            mocks: { $http: $http, $route },
            propsData: {
                commentDTO: {
                    text: "Initial comment text"
                }
            },
            instance
        });
        expect(CommentEditWrapper).toBeDefined();
        $http.put = (url)=>{
            expect(url).toBe(`/api/post/${postId}/comment`);
            done();
            return Promise.resolve(true);
        };
        CommentEditWrapper.setData({
            editContent: "New comment text",
        });
        CommentEditWrapper.vm.onBtnSave();
    });

    it("cancel", () => {
        const instance = Vue.extend();
        CommentEditWrapper = shallow(CommentEdit, {
            mocks: { $http: $http, $route },
            propsData: {
                commentDTO: {
                    text: "Initial comment text"
                }
            },
            instance
        });
        expect(CommentEditWrapper).toBeDefined();
        CommentEditWrapper.setData({
            editContent: "New comment text",
        });
        CommentEditWrapper.vm.onBtnCancel();
        expect(CommentEditWrapper.vm.editContent).toBe("");
    });
});