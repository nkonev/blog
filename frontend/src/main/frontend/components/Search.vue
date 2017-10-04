<template>
    <div class="search">
        <label for="search">Search</label>
        <input id="search" v-model="searchString" @input="onChangeSearchString()"/> <button @click="onClearButton()">clear</button>
    </div>

</template>

<script>
    import debounce from "lodash/debounce";

    // https://alligator.io/vuejs/component-communication/
    const SEARCH_EVENT = 'SEARCH_EVENT'; // don't works as export (sic!)
    export default {
        data() {
            return {
                searchString: ''
            }
        },
        methods: {
            onChangeSearchString(){
                this.$emit(SEARCH_EVENT, this.searchString);
            },
            onClearButton() {
                if (this.searchString !== '') {
                    this.searchString = '';
                    this.onChangeSearchString();
                }
            },
        },
        created() {
            // https://forum-archive.vuejs.org/topic/5174/debounce-replacement-in-vue-2-0
            this.onChangeSearchString = debounce(this.onChangeSearchString, 500);
        },
    };
</script>

<style lang="stylus" scoped>
    .search {
        margin-top 4px;
    }
</style>
