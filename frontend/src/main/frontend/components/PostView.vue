<template>
    <div class="post">
        <template v-if="postDTO">
            <div class="img-container">
                <img class="title-img" :src="postDTO.titleImg"/>
            </div>
            <aside class="left" > << </aside>
            <aside class="right"> >> </aside>

            <template v-if="isEditing">
                <PostEdit/>
            </template>
            <template v-else>
                <div class="post-head" v-if="postDTO.canEdit" @click="setEdit()">
                    <h2>{{postDTO.title}}</h2>
                    <img class="edit-container-pen" src="../assets/pen.png"/>
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
            this.$http.get(API_POST+'/'+this.$route.params.id, { }).then((res) => {
                this.postDTO = res.body; // add data from server's response
            });
        },
        components: {
            'PostEdit': PostEdit
        },
        methods: {
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
            /**
             * method with which child editor component will update html
             * @param html
             */
            setText(html) {
                this.postDTO.text = html;
                console.debug("PostView", this.postDTO.text);
            }
        }
    }
</script>

<style lang="stylus" scoped>
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
        .post-head {
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