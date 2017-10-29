<template>
    <div class="profile">
        <div v-if="me">
            <p>Это вы</p>
        </div>
        <div># {{ id }}</div>
        <div v-if="dto">
            <div v-if="!isEditing" class="user-profile">
                <span v-if="me" class="manage-buttons">
                    <img class="edit-container-pen" src="../assets/pen.png" @click="edit()"/>
                </span>
                <div><img class="avatar" :src="dto.avatar"></div>
                <div class="login">{{ dto.login }}</div>
                <div v-if="dto.email" class="email">{{dto.email}}</div>
            </div>
            <div v-else>
                <UserProfileEdit :dto="dto" @CANCELED="onCancel" @SAVED="onSaved"></UserProfileEdit>
            </div>
        </div>
        <error v-if="errorMessage"></error>
    </div>
</template>

<script>
    import Vue from 'vue'
    import store, {GET_USER} from  '../store'
    import Error from './Error.vue'
    import UserProfileEdit from './UserProfileEdit.vue'
    import bus, {LOGIN, LOGOUT} from '../bus'

    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['id'],
        data(){
            return {
                dto: null,
                errorMessage: '',
                isEditing: false
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
        methods:{
            edit(){
                this.isEditing = true;
            },
            onCancel(){
                this.isEditing = false;
            },
            onSaved(){
                this.isEditing = false;
                this.fetch();
            },
            fetch(){
                this.$http.get(`/api/user/${this.id}`).then(
                    successResp => {
                        this.dto = successResp.body;
                    },
                    failResp => {
                        this.errorMessage = failResp.body.message
                    }
                );
            },
            onLogin(){
                this.fetch();
            },
            onLogout(){

            },
        },
        components:{
            Error, UserProfileEdit
        },
        created(){
            this.fetch();
            bus.$on(LOGIN, this.onLogin);
        },
        destroyed(){
            bus.$off(LOGIN, this.onLogin);
        }
    };
</script>

<style lang="stylus" scoped>
    img.avatar {
        max-height 200px
        max-width 200px
    }

    img.edit-container-pen {
        height 32px;
        cursor pointer
    }
</style>
