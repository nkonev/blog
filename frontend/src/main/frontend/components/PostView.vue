<template>
    <div class="post">
        <template v-if="postDTO">
            <img class="title-img" :src="postDTO.titleImg"/>
            <h2>{{postDTO.title}}</h2>
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

    const api_prefix = '/api/public/post/';

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
        .title-img {
            max-width 100%
        }
    }
</style>