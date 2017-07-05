<template>
    <div>
        <h1>Пользователи</h1>

        <paginate
                :page-count="pageCount"
                :margin-pages="2"
                :click-handler="reloadPage"
                :page-range="4"
                :initial-page="initialPage"
                :container-class="'pagination'"
                :page-class="'page-item'"
                :page-link-class="'page-link-item'"
                :prev-class="'prev-item'"
                :prev-link-class="'prev-link-item'"
                :next-class="'next-item'"
                :next-link-class="'next-link-item'"
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
    import {PAGE_SIZE} from "../constants";
    import Paginate from 'vuejs-paginate';

    Vue.component('paginate', Paginate);

    export default {
        components: {UserItem}, // говорим, из каких компонентов могут быть извлечены тэги
        data() {
            return {
                users: [],
                initialPage: 0,
                pageCount: 0,
            }
        },
        methods: {
            reloadPage: function(pageNum, init) {
                if (!init) {
                    pageNum = pageNum - 1; // for Spring DATA
                }
                console.log("opening page ", pageNum);

                this.$http.get('/api/user?page='+pageNum+'&size='+PAGE_SIZE).then(response => {
                    this.users = response.body;
                }, response => {
                    console.error(response);
                    // alert(response);
                });
            }
        },
        created(){
            console.log("created");
            this.reloadPage(this.initialPage, true);

            // get page count
            this.$http.get('/api/user-count').then(response => {
                const userCount = response.body;
                this.pageCount = userCount / PAGE_SIZE;
            }, response => {
                console.error(response);
                // alert(response);
            });
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
            overflow visible
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
            .prev-link-item,.next-link-item {
                color #aaabac
                text-decoration line-through
            }
        }

        a {
            color: black;
            float: left;
            padding: 8px 16px;
            text-decoration: none;
        }

        a:hover {
            background-color: #ddd
        }
    }
</style>