<template>
    <div class="post">
        <template v-if="postDTO">
            <div class="img-container">
                <img class="title-img" :src="postDTO.titleImg"/>
            </div>
            <template v-if="postDTO.id"> <!-- draw only if id == true (!=0) -->
                <aside class="left" @click="goLeft()"> << </aside>
                <aside class="right" @click="goRight()"> >> </aside>
            </template>
            <template v-if="isEditing">
                <PostEdit :postDTO="postDTO" :onAfterSubmit="afterSubmit" :onCancel="cancel" />
            </template>
            <template v-else>
                <div class="post-head">
                    <h2>{{postDTO.title}}</h2>
                    <img class="edit-container-pen" src="../assets/pen.png" v-if="postDTO.canEdit" @click="setEdit()"/>
                </div>
                <div class="post-content" v-html="postDTO.text"></div>
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
    import bus from '../bus'
    import {LOGIN, LOGOUT} from '../bus'
    import {post} from '../router'

    // Lazy load heavy component https://router.vuejs.org/en/advanced/lazy-loading.html. see also in .babelrc
    const PostEdit = () => import('./PostEdit.vue');

    export default {
        name: 'post-view',
        data(){
            return{
                postDTO: {
                    id: 0,
                    title: 'Заголовок загружается',
                    titleImg: 'https://vuejs.org/images/components.png',
                    text: `<b>Lorem Ipsum</b> - это текст-"рыба", часто используемый в печати и вэб-дизайне`,
                    canEdit: false,
                    canDelete: false
                },
                isEditing: false
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
            'PostEdit': PostEdit
        },
        methods: {
            fetchData() {
                this.$http.get(API_POST+'/'+this.$route.params.id, { }).then((res) => {
                    this.postDTO = res.body; // add data from server's response
                });
            },
            onLogin() {
                this.fetchData();
            },
            onLogout() {
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
            /**
             * method with which child editor component will update html
             * @param html
             */
            setText(html) {
                this.postDTO.text = html;
                console.debug("PostView", this.postDTO.text);
            },
            goLeft() {
                this.postDTO.id--;
                this.$router.push({ name: post, params: { id: this.postDTO.id }});
                this.fetchData();
            },
            goRight() {
                this.postDTO.id++;
                this.$router.push({ name: post, params: { id: this.postDTO.id }});
                this.fetchData();
            }
        },
        watch: {
            'postDTO': {
                handler: function (val, oldVal) {
                    console.log("PostView changing postDTO", val.title, val.text)
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