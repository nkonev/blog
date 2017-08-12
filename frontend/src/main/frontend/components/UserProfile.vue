<template>
    <div class="profile">
        <div v-if="me">
            <p>Это вы</p>
        </div>
        <div>Пользователь {{ id }} {{testo}}</div>
    </div>
</template>

<script>
    import Vue from 'vue'
    import {GET_USER} from '../store'
    import store from  '../store'

    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['id'],
        data() {
            return {
                testo: true
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
        created() {
            if (global.ddd) {
                this.testo = false;
            }
        }
    };
</script>
