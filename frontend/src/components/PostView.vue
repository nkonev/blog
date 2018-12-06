<template>
    <div class="post">
        <template v-if="postDTO">
            <template v-if="isEditing">
                <PostEdit :postDTO="postDTO" :onAfterSubmit="afterSubmit" :onCancel="cancel" />
            </template>
            <template v-else>
                <template v-if="!isLoading && !errorMessage">
                    <div v-if="postDTO.titleImg" class="img-container post-title">
                        <img class="title-img" :src="postDTO.titleImg"/>
                        <h1>{{postDTO.title}}</h1>
                    </div>
                    <div v-else class="post-title"><h1>{{postDTO.title}}</h1></div>
                    <div class="post-head">
                        <owner v-if="postDTO.owner" prepend="written by" :owner="postDTO.owner" :time="createTime"></owner>

                        <div class="manage-buttons" v-if="postDTO.canEdit || postDTO.canDelete">
                            <img class="edit-container-pen" src="../assets/pen.png" v-if="postDTO.canEdit" @click="setEdit()"/>
                            <img class="remove-container-x" src="../assets/remove.png" v-if="postDTO.canDelete" @click="openDeleteConfirmation()"/>
                        </div>
                    </div>
                    <div class="ql-container ql-bubble">
                        <div class="post-content ql-editor" v-html="postDTO.text"></div>
                    </div>
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

            <div class="sides">
            <aside class="left" @click="goLeft()" v-if="postDTO.id && !isEditing && postDTO.left"><font-awesome-icon icon="arrow-left" /></aside>
            <aside class="right" @click="goRight()" v-if="postDTO.id && !isEditing && postDTO.right"><font-awesome-icon icon="arrow-right" /></aside>
            </div>

            <CommentList v-show="!(isLoading || errorMessage || isEditing)"/>
            <vm-back-top v-if="!isEditing && isLargeScreen" :bottom="80" :right="18"></vm-back-top>
        </template>
        <template v-else>
            Error
        </template>
    </div>

</template>

<script>
    import Vue from 'vue'
    import {API_POST, DIALOG} from '../constants'
    import bus, {LOGIN, LOGOUT, POST_SWITCHED} from '../bus'
    import {root, post} from '../routes'
    import postFactory from "../factories/PostDtoFactory"
    import BlogSpinner from "./BlogSpinner.vue"
    import PostAddFab from './PostAddFab.vue'
    import CommentList from './CommentList.vue'
    import Owner from './Owner.vue'
    import {getPostId, getTimestampFromUtc} from '../utils'
    import VmBackTop from 'vue-multiple-back-top'
    import {isLargeScreen} from '../utils'
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
            this.fetchData();
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
            CommentList,
            Owner,
            'vm-back-top': VmBackTop
        },
        computed:{
            createTime(){
                return getTimestampFromUtc(this.postDTO.createDateTime);
            },
            isLargeScreen(){
                return isLargeScreen();
            },
        },
        methods: {
            getId(){
                return getPostId(this);
            },
            fetchData() {
                // console.log("fetching post...");
                this.isLoading = true;
                this.errorMessage = null;
                this.$http.get(API_POST+'/'+this.getId()).then((response) => {
                    this.postDTO = response.body; // add data from server's response
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
            openDeleteConfirmation(){
                this.$modal.show(DIALOG, {
                    title: 'Post delete confirmation',
                    text: 'Do you want to delete this post?',
                    buttons: [
                        {
                            title: 'No',
                            handler: () => {
                                this.$modal.hide(DIALOG)
                            }
                        },
                        {
                            title: 'Yes',
                            default: true,
                            handler: () => {
                                this.doDelete();
                                this.$modal.hide(DIALOG)
                            }
                        },
                    ]
                })
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
        metaInfo () {
            return {
                title: this.postDTO.title,
            }
        }
    }
</script>

<style lang="stylus">
    @import "../constants.styl"
    $leftAfterTransform=30px
    $rightAfterTransform=30px

    .sides {
        display flex
        flex-direction row
        justify-content space-between
    }

    aside {
        position fixed
        cursor pointer
        background green
        top 50%
        opacity 0.6
        padding 8px 8px 2px 8px
        border-radius 2px
        color white
        margin 2px

        font-size 1.8em
        filter none

        &.left {
            left $leftAfterTransform
        }
        &.right {
            right $rightAfterTransform
        }
        &:hover {
            opacity 1.0
            color white
            transition: 0.1s all;
            box-shadow: 0 0 40px green;
            filter brightness(1.5)
            &.left {
                left $leftAfterTransform
            }
            &.right {
                right $rightAfterTransform
            }

        }
    }

    .post {
        &-title {
            h1 {
                padding-top 0.2em
                padding-bottom 0.2em
                padding-left 0.4em
                padding-right 0
            }
        }
        &-head {
            display flex
            flex-direction row
            justify-content space-between
            flex-wrap wrap
            //align-items: baseline

            img.edit-container-pen {
                height 32px;
                cursor pointer
            }
            img.remove-container-x {
                height 32px;
                cursor pointer
            }
            img.remove-container-x:hover {
                transition: 0.2s all;
                box-shadow: 0 0 2em red;
            }

            .manage-buttons {
                margin-left: auto
                display flex
                flex-direction row
                align-items center

                img {
                    margin 0 0.5em
                }
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

        .ql-editor {
            font-size $postBodyFontSize
            font-family $postBodyFontFamily

            margin-top $postBodyMarginTop
            margin-bottom $postBodyMarginBottom
        }

        &-error {
            font-size 4em
        }


        // https://css-tricks.com/text-blocks-over-image/
        .img-container {
            //display flex
            //flex-direction row
            justify-content center
            position: relative;

            .title-img {
                object-fit: cover;
                object-position: center;
                height: 300px;
                width 100%
            }

            h1 {
                position: absolute;
                bottom: 10px;
                left: 0;
                right 0;
                //width: 90%;
                color $titleColor
                background $titleBackground
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
                transition: none
                box-shadow: none
                transform none
            }
        }
    }
</style>

<style lang="stylus">
    @import "~quill/dist/quill.bubble.css"
</style>