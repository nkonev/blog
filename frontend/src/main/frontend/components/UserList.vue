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

        <div v-if="users.length>0">
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
    import {PAGE_SIZE, PAGE} from "../constants";
    import Paginate from 'vuejs-paginate';
    import {users} from '../router';
    import store from '../store'
    import {GET_ADMIN_USERS, GET_ADMIN_USERS_PAGE_COUNT, FETCH_ADMIN_USERS, GET_ADMIN_USERS_INITIAL_PAGE_INDEX} from '../store'

    Vue.component('paginate', Paginate);

    export default {
        components: {UserItem}, // говорим, из каких компонентов могут быть извлечены тэги
        // props: ['page'], // see in router.js
        data() {
            return {
                // users: [],
                // pageCount: 0,
            }
        },
        methods: {
            reloadPage: function(pageNum) {
                this.$router.push({path: users, query: {[PAGE]: pageNum}});
                console.log("opening page ", pageNum);

                this.$store.dispatch(FETCH_ADMIN_USERS, pageNum);
            }
        },
        computed: {
            /*
             The index of initial page which selected. default: 0
             */
            initialPageIndex() {
                return this.$store.getters[GET_ADMIN_USERS_INITIAL_PAGE_INDEX]
            },

            users(){
                return this.$store.getters[GET_ADMIN_USERS]
            },
            pageCount() {
                return this.$store.getters[GET_ADMIN_USERS_PAGE_COUNT]
            }
        },
        created(){
            console.log("UserList created");
        },
        mounted() {
            console.log("UserList mounted");
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