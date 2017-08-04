<template>
    <div class="profile">
        <div v-if="me">
            <p>Это вы</p>
        </div>
        <div>Пользователь {{ id }}</div>
    </div>
</template>

<script>
    import {GET_USER} from '../store'
    import store from  '../store'
    import bus from '../bus'
    import {LOGIN, LOGOUT} from '../bus'

    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['id'],
        data() {
            return {

            }
        },
        computed: {
            me() {
                const storeUser = store.getters[GET_USER];
                if (this.$props && storeUser) {
                    return storeUser ? storeUser.id == this.$props.id : false;
                } else {
                    return false;
                }
            },
        },
    };
</script>
