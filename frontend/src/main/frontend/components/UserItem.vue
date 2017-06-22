<template>
    <div>{{ userDTO.id }} {{ userDTO.login }} <router-link :to="{ name: 'user-profile', params: { id: ''+userDTO.id } }">Ссылко</router-link></div>
</template>

<script>
    export default {
        name: 'user-item', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['userDTO']
    };
</script>
