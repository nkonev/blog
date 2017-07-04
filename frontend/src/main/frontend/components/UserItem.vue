<template>
    <div class="user">
        <div class="user-avatar"><img src="/build/assets/logo.png"/></div>
        <div class="user-info">
            <router-link :to="{ name: 'user-profile', params: { id: userDTO.id } }">{{ userDTO.login }}</router-link>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'user-item', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['userDTO'], // it may be an object, for ability to set default values
    };
</script>

<style lang="stylus">
    .user {
        border-color rgba(0, 176, 88, 0.56)
        border-width 2px;
        border-style dashed
        margin 2px;

        display flex
        flex-direction row
        &-avatar {
            width 32px;
            height 32px;

            img {
                width 100%
                height 100%
            }
        }

        &-info {
            display: flex;
            flex-direction: column;
            justify-content: center;
        }
    }
</style>

