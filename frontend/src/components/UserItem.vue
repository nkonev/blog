<template>
    <div class="user" :id="'user-id-'+model.id">
        <div class="user-avatar"><avatar :src="model.avatar" :username="model.login"/></div>
        <div class="user-info">
            <router-link :to="{ name: 'user-profile', params: { id: model.id } }">{{ model.login }}</router-link>
        </div>
        <div class="user-role" v-if="model.managementData">{{model.managementData.role}}</div>
        <div class="user-management" v-if="model.managementData">
            <template v-if="model.canLock">
                <button v-if="model.managementData.locked" id="unlock" @click="requestUnlock()" class="blog-btn unlock-btn">Unlock</button>
                <button v-else id="lock" @click="requestLock()" class="blog-btn lock-btn">Lock</button>
            </template>
            <template v-if="model.canDelete">
                <button id="delete" @click="openDeleteConfirmation(model.login)" class="blog-btn lock-btn">Delete</button>
            </template>
            <template v-if="model.canChangeRole">
                <button @click="openChangeRole(model)" class="blog-btn ok-btn" id="change-role">Change role</button>
            </template>
        </div>
    </div>
</template>

<script>
    import Vue from "vue"
    import Avatar from 'vue-avatar'
    import {DIALOG, CHANGE_ROLE_MODAL} from '../constants'

    export const USER_DELETED = 'USER_DELETED';

    export default {
        name: 'user-item', // это имя компонента, которое м. б. тегом в другом компоненте
        props: ['userDTO', 'currentUser'], // it may be an object, for ability to set default values
        data(){
            return {
                model: {}
            }
        },
        methods: {
            lock(locked)  {
                console.log("set lock", locked);
                this.$http.post('/api/user/lock', {userId: this.model.id, lock: locked}).then(
                    response => {
                        this.model = response.body;
                    },
                    failResponse => {
                        alert("Error on try lock")
                    }
                )
            },
            requestLock(){
                this.lock(true)
            },
            requestUnlock(){
                this.lock(false)
            },
            requestDelete(){
                this.$http.delete('/api/user?userId='+this.model.id).then(
                    response => {
                        console.log("Successfully deleted");
                        this.$emit(USER_DELETED, response.body);
                    },
                    failResponse => {
                        alert("Error on delete")
                    }
                )
            },
            openChangeRole(model){
                this.$modal.show(CHANGE_ROLE_MODAL, { login: model.login, currentRole: model.managementData.role, userId: model.id });
            },
            openDeleteConfirmation(login){
                this.$modal.show(DIALOG, {
                    title: 'User delete confirmation',
                    text: 'Do you want to delete this user ' + login +'?',
                    buttons: [
                        {
                            title: 'No',
                            default: true,
                            handler: () => {
                                this.$modal.hide(DIALOG)
                            }
                        },
                        {
                            title: 'Yes',
                            handler: () => {
                                this.requestDelete();
                                this.$modal.hide(DIALOG)
                            }
                        },
                    ]
                })
            },
        },
        created(){
            this.model = Vue.util.extend({}, this.userDTO);
        },
        components:{
            Avatar
        }
    };
</script>

<style lang="stylus">
    @import "../common.styl"
    $lockBtnMargin=4px 8px
    $lockBtnWidth=80px
    .user {
        border-color rgba(0, 176, 88, 0.56)
        border-width 1px;
        border-style dashed
        margin 2px;

        display flex
        flex-direction row
        &-avatar {
            display: flex;
            flex-direction: column;
            justify-content: center;

            width 48px;
            height 48px;
            //margin 2px;
            padding 4px 4px 4px 4px
        }

        &-info {
            display: flex;
            flex-direction: column;
            justify-content: center;
            margin-left 6px
            margin-right 6px
        }

        &-role {
            display: flex;
            flex-direction: column;
            justify-content: center;
            margin-left 6px
            margin-right 6px
            color grey
        }

        &-management {
            width 100%
            display: flex;
            flex-direction: row;
            justify-content: flex-end;
            align-items center
        }

        button.blog-btn.lock-btn {
            background: white;
            border-color: $red_color
            color: $red_color
            margin $lockBtnMargin
            min-width $lockBtnWidth
            width $lockBtnWidth
            &:hover {
                border-color: $red_color;
                background: $red_color;
                color: white;
            }
        }
        button.blog-btn.unlock-btn {
            background: white;
            border-color: $ok_color
            color: $ok_color
            margin $lockBtnMargin
            min-width $lockBtnWidth
            width $lockBtnWidth
            &:hover {
                border-color: $ok_color;
                background: $ok_color;
                color: white;
            }
        }

        button.blog-btn.ok-btn {
            margin $lockBtnMargin
        }

    }
</style>

