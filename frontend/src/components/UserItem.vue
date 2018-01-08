<template>
    <div class="user">
        <div class="user-avatar"><img :src="model.avatar"/></div>
        <div class="user-info">
            <router-link :to="{ name: 'user-profile', params: { id: model.id } }">{{ model.login }}</router-link>
        </div>
        <div class="user-management" v-if="model.managementData">
            <template v-if="!(currentUser && currentUser.id==model.id)">
                <button v-if="model.managementData.locked" id="unlock" @click="requestUnlock()" class="blog-btn unlock-btn">Unlock</button>
                <button v-else id="lock" @click="requestLock()" class="blog-btn lock-btn">Lock</button>
            </template>
        </div>
    </div>
</template>

<script>
    import Vue from "vue"
    
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
            }
        },
        created(){
            this.model = Vue.util.extend({}, this.userDTO);
        }
    };
</script>

<style lang="stylus">
    @import "../buttons.styl"
    $lockBtnMargin=4px 8px
    $lockBtnWidth=80px
    .user {
        border-color rgba(0, 176, 88, 0.56)
        border-width 2px;
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
            margin 2px;

            // needs for correct display in Chrome
            img {
                //width 100%;
                height 100%;
            }
        }

        &-info {
            display: flex;
            flex-direction: column;
            justify-content: center;
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

    }
</style>

