<template>
    <div class="user-profile">
        <template v-if="dto">
            <template v-if="!isEditing">
                <div class="user-profile-msg">Viewing profile #{{ id }}</div>

                <div class="user-profile-view">
                    <div class="user-profile-view-avatar"><img class="avatar" :src="dto.avatar"></div>

                    <div class="user-profile-view-info">
                        <div v-if="me" class="user-profile-view-info-me">
                            <span class="me">This is you</span>
                            <span class="manage-buttons">
                                <img class="edit-container-pen" src="../assets/pen.png" @click="edit()"/>
                            </span>
                        </div>
                        <div class="user-profile-view-info-data">
                            <div class="login">{{ dto.login }}</div>
                            <div v-if="dto.email" class="email">{{dto.email}}</div>
                            <div class="last-login">{{ lastLogin }}</div>
                        </div>
                    </div>
                </div>

                <UserPostList :id="id"/>
            </template>
            <div v-else>
                <UserProfileEdit :dto="dto" @CANCELED="onCancel" @SAVED="onSaved"></UserProfileEdit>
            </div>
        </template>
        <error v-if="errorMessage"></error>
    </div>
</template>

<script>
    import Vue from 'vue'
    import store, {GET_USER} from  '../store'
    import Error from './Error.vue'
    import UserProfileEdit from './UserProfileEdit.vue'
    import bus, {LOGIN, LOGOUT} from '../bus'
    import UserPostList from "./UserPostList.vue"
    import {getTimestampFromUtc} from '../utils.js'

    export default {
        name: 'user-profile', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['id', 'onFetchSuccess'],
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
            lastLogin(){
                if (this.dto.lastLoginDateTime) {
                    return 'Last login at ' + getTimestampFromUtc(this.dto.lastLoginDateTime)
                }
            }
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
                this.dto = null;
                this.$http.get(`/api/user/${this.id}`).then(
                    successResp => {
                        this.dto = successResp.body;
                        if (this.onFetchSuccess){
                            this.onFetchSuccess();
                        }
                        console.log('successfully fetched profile ' + this.id);
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
        watch: {
            // refetch data
            '$route': 'fetch'
        },
        components:{
            Error, UserProfileEdit, UserPostList
        },
        created(){
            this.fetch();
            bus.$on(LOGIN, this.onLogin);
        },
        destroyed(){
            bus.$off(LOGIN, this.onLogin);
        },
        metaInfo () {
            let login;
            if (this.dto) {
                login = this.dto.login;
            } else {
                login = this.id;
            }
            return {

                title: 'User ' + login,
            }
        }

    };
</script>

<style lang="stylus" scoped>
    @import "../constants.styl"

    img.avatar {
        max-height 300px
        max-width 300px
    }

    img.edit-container-pen {
        margin-left 1em
        height 32px;
        cursor pointer
    }

    .user-profile {
        &-msg{
            text-align center
            font-size xx-large
            font-family monospace
            margin-bottom: 1em;
            margin-top: 0.5em;
        }

        &-view {
            display flex
            flex-direction row

            &-info {
                &-me {
                    display flex
                    flex-direction row
                    align-items center

                    span.me {
                        margin-left 0.5em
                        font-family monospace
                        font-size 1.3em
                    }
                }

                &-data {
                    display flex
                    flex-direction column
                    flex-wrap wrap
                    justify-content space-around
                    font-size 1.5em
                    padding 0.3em

                    .last-login {
                        color: grey
                        font-size small
                    }
                }
            }
        }
    }

    @media screen and (max-width: $contentWidth - 200) {
        .user-profile {
            &-view {
                flex-direction column
                &-avatar {
                    text-align center
                }
            }
        }
    }

</style>
