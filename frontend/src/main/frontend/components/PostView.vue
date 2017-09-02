<template>
    <div class="post">
        <template v-if="postDTO">
            <!-- draw only if id == true (!=0) -->
            <aside class="left" @click="goLeft()" v-if="postDTO.id && !isEditing && postDTO.left"> << </aside>
            <aside class="right" @click="goRight()" v-if="postDTO.id && !isEditing && postDTO.right"> >> </aside>

            <template v-if="isEditing">
                <PostEdit :postDTO="postDTO" :onAfterSubmit="afterSubmit" :onCancel="cancel" />
            </template>
            <template v-else>
                <template v-if="!isLoading">
                    <div class="img-container">
                        <img class="title-img" :src="postDTO.titleImg"/>
                    </div>
                    <div class="post-head">
                        <h2>{{postDTO.title}}</h2>

                        <span class="manage-buttons">
                            <img class="edit-container-pen" src="../assets/pen.png" v-if="postDTO.canEdit" @click="setEdit()"/>
                            <img class="remove-container-x" src="../assets/remove.png" v-if="postDTO.canDelete" @click="doDelete()"/>
                        </span>
                    </div>
                    <div class="post-content" v-html="postDTO.text"></div>
                </template>
                <template v-else>
                    <blog-spinner message="Loading post"></blog-spinner>
                </template>
                <post-add-fab/>
            </template>
            <hr/>

        </template>
        <template v-else>
            Error
        </template>
    </div>

</template>

<script>
    import {API_POST} from '../constants'
    import bus, {LOGIN, LOGOUT} from '../bus'
    import {root, post} from '../router'
    import postFactory from "../factories/PostDtoFactory"
    import BlogSpinner from "./BlogSpinner.vue"
    import PostAddFab from './PostAddFab.vue'

    // Lazy load heavy component https://router.vuejs.org/en/advanced/lazy-loading.html. see also in .babelrc
    const PostEdit = () => import('./PostEdit.vue');

    export default {
        name: 'post-view',
        data(){
            return{
                postDTO: postFactory(),
                isEditing: false,
                isLoading: true
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
            PostAddFab
        },
        methods: {
            fetchData() {
                // console.log("fetching post...");
                this.isLoading = true;
                this.$http.get(API_POST+'/'+this.$route.params.id, { }).then((response) => {
                    this.postDTO = JSON.parse(response.bodyText); // add data from server's response
                    this.isLoading = false;
                }, response => {
                    console.error("Error on fetch post", response);
                    this.isLoading = false;
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
                this.$http.delete(API_POST+'/'+this.$route.params.id, { }).then((response) => {
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
    aside {
        position fixed
        border-color #8135ff
        border-width 1px
        // border-style solid
        cursor pointer
        height 100%
        width: 15%
        top 0
        z-index -1

        font-family monospace
        text-shadow: 4px 4px 4px black
        opacity 0.1
        display: flex;
        flex-direction: column;
        justify-content: center;
        text-align center
        font-size 30ch
        transform: scale(1, 2.5); // scale text vertically
        &:hover {
            opacity 0.2
        }
        &.left {
            left 0
            float left
        }
        &.right {
            right 0
            float right
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