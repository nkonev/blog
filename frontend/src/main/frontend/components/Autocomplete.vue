<template>
    <div>
        <h1>Type some country</h1>

        <template>
            <v-autocomplete :items="items" v-model="item" v-bind:minLen="1":get-label="getLabel" :component-item='template' @update-items="fetchItems" @input="onSelected">
            </v-autocomplete>
        </template>
    </div>
</template>

<script>
    import Vue from 'vue'
    import Autocomplete from 'v-autocomplete'
    import 'v-autocomplete/dist/v-autocomplete.css'

    Vue.use(Autocomplete);
    import ItemTemplate from './AutocompleteItemTemplate.vue'
    export default {
        data () {
            return {
                item: null,
                template: ItemTemplate,
                items: [],
            }
        },
        methods: {
            getLabel (item) {
                return item.value
            },
            fetchItems (text) {
                console.log('fetching items for', text);
                const that = this;
                this.$http.get('/api/public/autocomplete?prefix=' + text)
                    .then(
                        response => {
                            that.items = response.body;
                            console.log(that.items);
                        },
                        response => {
                            // error callback
                            console.error(response);
                        }
                    )
            },
            onSelected() {
                console.log("Selected", this.item);
            }
        }
    }
</script>

<style lang="stylus">
    .v-autocomplete
        .v-autocomplete-input-group
            .v-autocomplete-input
                font-size 1.5em
                padding 10px 15px
                box-shadow none
                border 1px solid #157977
                width 100%
                outline none
                background-color #eee
            &.v-autocomplete-selected
                .v-autocomplete-input
                    color green
                    background-color #f2fff2
        .v-autocomplete-list
            width 100%
            text-align left
            border none
            border-top none
            max-height 400px
            overflow-y auto
            border-bottom 1px solid #157977
            .v-autocomplete-list-item
                cursor pointer
                background-color #fff
                padding 10px
                border-bottom 1px solid #157977
                border-left 1px solid #157977
                border-right 1px solid #157977
                &:last-child
                    border-bottom none
                &:hover
                    background-color #eee
                abbr
                    opacity 0.8
                    font-size 0.8em
                    display block
                    font-family sans-serif
</style>