<template>
    <div>
        <h1>Пользователи</h1>

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
        <div v-else>
            No data
        </div>
    </div>

</template>

<script>
    import Vue from 'vue'
    import UserItem from "./UserItem.vue";
    import {PAGE_SIZE} from "../constants";
    import Paginate from 'vuejs-paginate';
    import {users} from '../router';
    import bus from '../bus'
    import {LOGIN, LOGOUT} from '../bus'

    Vue.component('paginate', Paginate);

    export default {
        components: {UserItem}, // говорим, из каких компонентов могут быть извлечены тэги
        // props: ['page'], // see in router.js
        data() {
            return {
                users: [],
                pageCount: 0,
            }
        },
        methods: {
            reloadPage: function(pageNum) {
                // this.$router.push({path: users + "/" + pageNum});
                this.$router.push({path: users, query: {page: pageNum}});
                console.log("opening page ", pageNum);

                // API request
                this.$http.get('/api/user?page='+(pageNum-1)+'&size='+PAGE_SIZE).then(response => {
                    this.users = response.body;
                }, response => {
                    console.error(response);
                    // alert(response);
                });
            },
            initPageCount() {
                // get page count
                this.$http.get('/api/user-count').then(response => {
                    const userCount = response.body;
                    this.pageCount = Math.ceil(userCount / PAGE_SIZE);
                }, response => {
                    console.error(response);
                    // alert(response);
                });
            },
            onLogin(ignore) {
                this.reloadPage(this.initialPageIndex+1);
                this.initPageCount();
            },
            onLogout(ignore) {
                this.pageCount=0;
                this.users=[];
            }
        },
        computed: {
            /*
             The index of initial page which selected. default: 0
             */
            initialPageIndex() {
                // return this.$props.page ? parseInt(this.$props.page-1) : 0;
                return this.$route.query.page ? parseInt(this.$route.query.page-1) : 0;
            }
        },
        created() {
            //console.log("created");
            const page = this.initialPageIndex+1;
            this.reloadPage(page);
            this.initPageCount();

            bus.$on(LOGIN, this.onLogin);
            bus.$on(LOGOUT, this.onLogout);
        },
        destroyed() {
            //console.log("destroyed");
            bus.$off(LOGIN, this.onLogin);
            bus.$off(LOGOUT, this.onLogout);
        }
    };
</script>

<style lang="stylus">
    .pagination {
        li {
            display: inline-block
        }

        .page-link-item {
            min-width 26px;
            text-align center;
        }

        .active {
            background-color: #4CAF50;
            border-radius 2px;
            .page-link-item {
                color: white;
            }
            :hover {
                color: black;
            }
        }

        .disabled  {
            pointer-events: none;
            cursor: default;
            .arrow-link-item {
                color #aaabac
                text-decoration line-through
            }
        }

        a {
            color: black;
            // float: left; // float makes it as block(synonym for followed line), see https://habrahabr.ru/post/136588/
            display block
            padding: 8px 16px;
            text-decoration: none;
        }

        a:hover {
            background-color: #ddd
        }
    }
</style>