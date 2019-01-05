<template>
    <button id="btn-submit-vkontakte" class="blog-btn login-btn-social login-btn-social-vk" type="submit" @click="onClick">
        <font-awesome-icon :icon="{ prefix: 'fab', iconName: 'vk' }" style="width: 28px; height:28px; margin-right: 1em" ></font-awesome-icon>
        <span><slot/></span>
    </button>
</template>

<script>
    export default {
        methods:{
            onClick(){
                this.$emit('click');
            }
        }
    }
</script>

<style lang="stylus" scoped>
    @import "../constants.styl"
    @import "../common.styl"
    @import "./ButtonOauth2.styl"
</style>