<template>
    <div>
        <h1>Пользователи</h1>
        <div class="search">
            <label for="search">Search</label>
            <input id="search" v-model="searchString" @input="onChangeSearchString()"/> <button @click="onClearButton()">clear</button>
        </div>

        <paginate
                :page-count="pageCount"
                :margin-pages="2"
                :click-handler="reloadPage"
                :page-range="4"
                :initial-page="initialPageIndex"
                ref="paginate"
                :container-class="'pagination'"
                :page-class="'page-item'"
                :page-link-class="'page-link-item'"
                :prev-class="'prev-item'"
                :prev-link-class="'prev-link-item arrow-link-item'"
                :next-class="'next-item'"
                :next-link-class="'next-link-item arrow-link-item'"
        ></paginate>

        <div v-if="users.length>0" id="user-list">
            <user-item v-for="user in users" v-bind:userDTO="user" :key="user.id"></user-item>
        </div>
        <div v-else id="user-list">
            No data
        </div>
    </div>

</template>

<script>
    import Vue from 'vue'
    import UserItem from "./UserItem.vue";
    import {PAGE_SIZE} from "../constants";
    import Paginate from 'vuejs-paginate';
    import {users} from '../routes';
    import bus from '../bus'
    import {LOGIN, LOGOUT, UNAUTHORIZED} from '../bus'
    import debounce from "lodash/debounce"

    Vue.component('paginate', Paginate);

    export default {
        components: {UserItem}, // говорим, из каких компонентов могут быть извлечены тэги
        data() {
            return {
                users: [],
                pageCount: 0,
                searchString: '',
            }
        },
        methods: {
            reloadPage: function(pageNum) {
                this.$router.push({path: users, query: {page: pageNum}});
                console.log("opening page ", pageNum);

                // API request
                this.$http.get('/api/user',
                    {
                        params: {
                            searchString: this.searchString,
                            page: (pageNum-1),
                            size: PAGE_SIZE
                        },
                    }).then(
                    response => {
                        this.pageCount = Math.ceil(response.body.totalCount / PAGE_SIZE);
                        this.users = response.body.data;
                    }, response => {
                        console.error(response);
                        // alert(response);
                    }
                );
            },
            onLogin(ignore) {
                this.reloadPage(this.initialPageIndex+1);
            },
            onLogout(ignore) {
                this.pageCount=0;
                this.users=[];
            },
            onChangeSearchString() {
                console.debug('onChangeSearchString');
                this.users = [];
                const initPage = 0;
                this.reloadPage(initPage+1);
                this.$refs.paginate.selected = initPage;
            },
            onClearButton() {
                if (this.searchString !== '') {
                    this.searchString = '';
                    this.onChangeSearchString();
                }
            },
        },
        computed: {
            // The index of initial page which selected. default: 0
            initialPageIndex() {
                return this.$route.query.page ? parseInt(this.$route.query.page-1) : 0;
            }
        },
        created() {
            //console.log("created");
            this.reloadPage(this.initialPageIndex+1);

            bus.$on(LOGIN, this.onLogin);
            bus.$on(LOGOUT, this.onLogout);
            bus.$on(UNAUTHORIZED, this.onLogout);
            this.onChangeSearchString = debounce(this.onChangeSearchString, 500);
        },
        destroyed() {
            //console.log("destroyed");
            bus.$off(LOGIN, this.onLogin);
            bus.$off(LOGOUT, this.onLogout);
            bus.$off(UNAUTHORIZED, this.onLogout);
        }
    };
</script>

<style lang="stylus">
    @import "./pageable.styl"
</style>