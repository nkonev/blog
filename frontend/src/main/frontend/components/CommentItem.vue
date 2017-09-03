<template>
    <div class="comment">
        <div class="user-info">
            <img :src="commentDTO.owner.avatar">
            <span>{{commentDTO.owner.login}}</span>
        </div>
        <div class="comment-content">
            {{commentDTO.text}}
        </div>
        <hr/>
    </div>
</template>

<script>
    export default {
        name: 'comment-item',
        props: ['commentDTO'], // it may be an object, for ability to set default values
    };
</script>

<style lang="stylus">
    .comment {
        // border-width 1px
        // border-color black
        // border-style solid
        margin 2px;

        .user-info {
            img {
                max-width 48px;
                max-height 48px;
            }
        }
    }
</style>