<template>
    <div class="search">
        <input class="search-input" v-model="searchString" @input="onChangeSearchString()" :placeholder="placeholder"/>
        <span class="search-clear" @click="onClearButton()">x</span>
    </div>

</template>

<script>
    import debounce from "lodash/debounce";

    // https://alligator.io/vuejs/component-communication/
    const SEARCH_EVENT = 'searchEvent'; // don't works as export (sic!)
    export default {
        props: ['placeholder', 'initialString'],
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
            this.onChangeSearchString = debounce(this.onChangeSearchString, 700);

            if (this.initialString) {
                this.searchString = this.initialString;
            }
        },
    };
</script>

<style lang="stylus" scoped>
    .search {
        margin-top 0px;
        margin-bottom 0px;
        display flex
        flex-direction row
        align-items center
    }

    input.search-input {
        outline: none;
        border solid
        border-width 1px
        border-color cornflowerblue
        height 1.4em
        font-size 1.4em
        width 100%
    }

    .search-clear {
        display block
        cursor pointer
        padding 5px 5px
        font-family monospace
        font-weight bold
        height 100%

        &:hover {
            transition: 0.2s all;
            color red
        }
    }
</style>
