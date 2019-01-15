<template>
    <div class="users-page">
        <h1>Users</h1>
        <Search @searchEvent="onChangeSearchString" placeholder="Contain search by login"></Search>
        <paginate
                v-model="currentPage"
                :page-count="pageCount"
                :margin-pages="2"
                :click-handler="reloadPage"
                :page-range="4"
                :initial-page="initialPageIndex"
                :container-class="'pagination'"
                :page-class="'page-item'"
                :page-link-class="'page-link-item'"
                :prev-class="'prev-item'"
                :prev-link-class="'prev-link-item arrow-link-item'"
                :next-class="'next-item'"
                :next-link-class="'next-link-item arrow-link-item'"
        ></paginate>

        <div v-if="users.length>0" id="user-list">
            <user-item v-for="user in users" v-bind:userDTO="user" :key="user.id" :currentUser="currentUser" @USER_DELETED="onDeleteEvent"></user-item>
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
    import Search from './Search.vue';
    import store, {GET_USER} from '../store'
    import {mapGetters} from 'vuex'

    Vue.component('paginate', Paginate);

    export default {
        data() {
            return {
                users: [],
                pageCount: 0,
                currentPage: 1,
            }
        },
        store,
        methods: {
            reloadPage(pageNum) {
                this.$router.push({path: users, query: {page: pageNum}});
                console.log("opening page ", pageNum);
                this.fetchUsers('', pageNum);
            },
            fetchUsers(searchString='', pageNum=(this.initialPageIndex+1)) {
                this.$router.push({path: users, query: {page: pageNum}});

                // API request
                this.$http.get('/api/user',
                    {
                        params: {
                            searchString: searchString,
                            page: (pageNum-1),
                            size: PAGE_SIZE
                        },
                    }).then(
                    response => {
                        this.pageCount = this.getPageCount(response.body.totalCount);
                        this.users = response.body.data;
                        this.currentPage = pageNum;
                    }, response => {
                        console.error(response);
                        // alert(response);
                    }
                );
            },
            onLogin(ignore) {
                console.log("onLogin");
                this.fetchUsers();
            },
            onLogout(ignore) {
                console.log("onLogout");
                this.fetchUsers();
            },
            onChangeSearchString(str) {
                console.debug('onChangeSearchString', str);
                const initPage = 0;
                this.fetchUsers(str, initPage+1);
            },
            getPageCount(totalUsersCount){
                return Math.ceil(totalUsersCount / PAGE_SIZE);
            },
            shouldSwitch(totalUsersCount){
                return !(totalUsersCount % PAGE_SIZE);
            },

            onDeleteEvent(newUsersCount){
                console.info("Delete event in list");
                this.pageCount = this.getPageCount(newUsersCount);
                const nextPage = this.shouldSwitch(newUsersCount) ? this.initialPageIndex : this.initialPageIndex +1;
                this.reloadPage(nextPage);
            }
        },
        components: {
            Search,
            UserItem
        },
        computed: {
            // The index of initial page which selected. default: 0
            initialPageIndex() {
                return this.$route.query.page ? parseInt(this.$route.query.page-1) : 0;
            },
            ...mapGetters({currentUser: GET_USER})
        },
        created() {
            //console.log("created");
            this.fetchUsers();

            bus.$on(LOGIN, this.onLogin);
            bus.$on(LOGOUT, this.onLogout);
        },
        destroyed() {
            //console.log("destroyed");
            bus.$off(LOGIN, this.onLogin);
            bus.$off(LOGOUT, this.onLogout);
        },
        metaInfo: {
            title: 'Users',
        }
    };
</script>

<style lang="stylus">
    @import "./pageable.styl"
</style>