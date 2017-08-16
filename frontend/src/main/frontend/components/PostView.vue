<template>
    <div class="post">
        <template v-if="postDTO">
            <div class="img-container">
                <img class="title-img" :src="postDTO.titleImg"/>
            </div>
            <div class="post-head">
                <h2>{{postDTO.title}}</h2>
                <img class="edit-container-pen" src="../assets/pen.png"/>
            </div>
            <div class="post-content" v-html="postDTO.text">
                <hr/>
            </div>
        </template>
        <template v-else>
            Error
        </template>
    </div>

</template>

<script>

    const api_prefix = '/api/post/';

    export default {
        name: 'post-view',
        data(){
            return{
                postDTO: {
                    id: 0,
                    title: 'Заголовок загружается',
                    titleImg: 'https://vuejs.org/images/components.png',
                    text: `<b>Lorem Ipsum</b> - это текст-"рыба", часто используемый в печати и вэб-дизайне`
                }
            }
        },
        created(){
            console.log("PostView created");
            this.$http.get(api_prefix+this.$route.params.id, { }).then((res) => {
                this.postDTO = res.body; // add data from server's response
            });


        }
    }
</script>

<style lang="stylus" scoped>
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