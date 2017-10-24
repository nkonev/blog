<template>
    <div class="post">
        <template v-if="postDTO">
            <template v-if="isEditing">
                <PostEdit :postDTO="postDTO" :onAfterSubmit="afterSubmit" :onCancel="cancel" />
            </template>
            <template v-else>
                <template v-if="!isLoading && !errorMessage">
                    <div class="img-container">
                        <img class="title-img" :src="postDTO.titleImg"/>
                    </div>
                    <div class="post-head">
                        <h2>{{postDTO.title}}</h2>
                        <div class="user-info">
                            <router-link :to="`/user/${postDTO.owner.id}`" v-if="postDTO.owner">
                                <span>{{postDTO.owner.login}}</span>
                                <img :src="postDTO.owner.avatar"/>
                            </router-link>
                        </div>
                        <span class="manage-buttons" v-if="postDTO.canEdit || postDTO.canDelete">
                            <img class="edit-container-pen" src="../assets/pen.png" v-if="postDTO.canEdit" @click="setEdit()"/>
                            <img class="remove-container-x" src="../assets/remove.png" v-if="postDTO.canDelete" @click="doDelete()"/>
                        </span>
                    </div>
                    <div class="post-content" v-html="postDTO.text"></div>
                </template>
                <template v-if="errorMessage">
                    <div class="post-error">
                        {{errorMessage}}
                    </div>
                </template>
                <template v-if="isLoading">
                    <blog-spinner message="Loading post"></blog-spinner>
                </template>
                <post-add-fab/>
            </template>
            <!-- draw only if id == true (!=0) -->
            <aside class="left" @click="goLeft()" v-if="postDTO.id && !isEditing && postDTO.left"><span><< left</span></aside>
            <aside class="right" @click="goRight()" v-if="postDTO.id && !isEditing && postDTO.right"><span>right >></span></aside>

            <CommentList v-if="!isLoading && !errorMessage"/>
        </template>
        <template v-else>
            Error
        </template>
    </div>

</template>

<script>
    import {API_POST} from '../constants'
    import bus, {LOGIN, LOGOUT, POST_SWITCHED} from '../bus'
    import {root, post} from '../routes'
    import postFactory from "../factories/PostDtoFactory"
    import BlogSpinner from "./BlogSpinner.vue"
    import PostAddFab from './PostAddFab.vue'
    import CommentList from './CommentList.vue'
    import {getPostId} from '../utils'

    // Lazy load heavy component https://router.vuejs.org/en/advanced/lazy-loading.html. see also in .babelrc
    const PostEdit = () => import('./PostEdit.vue');

    export default {
        name: 'post-view',
        props: [
            'onGetPostSuccess',
            'onGetPostFail'
        ],
        data(){
            return{
                postDTO: postFactory(),
                isEditing: false,
                isLoading: true,
                errorMessage: null,
            }
        },
        created(){
            // console.log("PostView created");
            if (!process.env.KARMA_ENV) {
                this.fetchData();
            }
            bus.$on(LOGIN, this.onLogin);
            bus.$on(LOGOUT, this.onLogout);
        },
        destroyed(){
            bus.$off(LOGIN, this.onLogin);
            bus.$off(LOGOUT, this.onLogout);
        },
        components: {
            'PostEdit': PostEdit,
            'blog-spinner': BlogSpinner,
            PostAddFab,
            CommentList
        },
        methods: {
            getId(){
                return getPostId(this);
            },
            fetchData() {
                // console.log("fetching post...");
                this.isLoading = true;
                this.errorMessage = null;
                this.$http.get(API_POST+'/'+this.getId(), { }).then((response) => {
                    this.postDTO = JSON.parse(response.bodyText); // add data from server's response
                    this.isLoading = false;
                    bus.$emit(POST_SWITCHED, this.postDTO.id);
                    if (this.$props.onGetPostSuccess) {
                        this.$props.onGetPostSuccess(this.postDTO);
                    }
                }, response => {
                    console.error("Error on fetch post", response);
                    this.isLoading = false;
                    if (this.$props.onGetPostFail) {
                        this.$props.onGetPostFail(this.postDTO);
                    }
                    this.errorMessage = response.body.message;
                });
            },
            onLogin() {
                this.fetchData();
            },
            onLogout() {
                console.log("onLogout");
                this.fetchData();
            },

            setEdit() {
                this.isEditing = true;
            },
            /**
             * method with which child editorcancel editing -- e. g. hide edit input
             * @param html
             */
            cancel() {
                this.isEditing = false;
            },
            afterSubmit() {
                this.cancel();
                this.fetchData();
            },
            goto(postId){
                this.$router.push({ name: post, params: { id: postId }});
                this.fetchData();
            },
            goLeft() {
                this.goto(this.postDTO.left);
            },
            goRight() {
                this.goto(this.postDTO.right);
            },
            doDelete() {
                this.isLoading = true;
                console.log("Deleting");
                this.$http.delete(API_POST+'/'+this.getId(), { }).then((response) => {
                    this.isLoading = false;
                    if (this.postDTO.right) {
                        this.goto(this.postDTO.right);
                    } else if (this.postDTO.left) {
                        this.goto(this.postDTO.left);
                    } else {
                        this.$router.push({ name: root });
                    }
                }, response => {
                    console.error("Error on delete post", response);
                    this.isLoading = false;
                });
            }
        },
        watch: {
            'postDTO': {
                handler: function (val, oldVal) {
                    // console.log("PostView changing postDTO", val.title, val.text)
                },
                deep: true
            }
        },
    }
</script>

<style lang="stylus">
    @import "../constants.styl"
    $leftAfterTransform=30px
    $rightAfterTransform=30px


    aside {
        position fixed
        cursor pointer
        background green
        top 50%
        opacity 0.6
        padding 4px 6px
        border-radius 2px
        color white
        margin 2px

        font-size 1.8em

        &.left {
            float left
            left $leftAfterTransform
        }
        &.right {
            float right
            right $rightAfterTransform
        }
        &:hover {
            opacity 1.0
            color white
            transition: 0.1s all;
            transform: scale(1.5, 1.5);
            &.left {
                left $leftAfterTransform
            }
            &.right {
                right $rightAfterTransform
            }

        }
    }

    @media screen and (max-width: $contentWidth+300px) {
        aside {
            position static
            transform none
            font-size 1.8em
            &.left {
                top inherit
            }
            &.right {
                top inherit
            }
            &:hover {
                transform none
            }
        }

    }


    .post {
        &-head {
            display flex
            flex-direction row
            justify-content space-between
            // flex-wrap wrap
            align-items: baseline

            h1 {
            }
            img.edit-container-pen {
                height 32px;
                cursor pointer
            }
            img.remove-container-x {
                height 32px;
                cursor pointer
            }

            .user-info img {
                height 32px;
            }
        }
        &-content {
            blockquote {
                background: #f9f9f9;
                border-left: 10px solid #ccc;
                margin: 1.5em 10px;
                padding: 0.5em 10px;
                quotes: "\201C""\201D""\2018""\2019";
            }
            blockquote:before {
                color: #ccc;
                content: open-quote;
                font-size: 4em;
                line-height: 0.1em;
                margin-right: 0.25em;
                vertical-align: -0.4em;
            }
            blockquote p {
                display: inline
            }
        }
        &-error {
            font-size 4em
        }

        .img-container {
            display flex
            flex-direction row
            justify-content center

            .title-img {
                max-width 100%
            }
        }
    }
</style>