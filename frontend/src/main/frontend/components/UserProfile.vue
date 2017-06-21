<template>
    <div>Пользователь {{ id }}</div>
</template>

<script>
    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['id']
    };
</script>
