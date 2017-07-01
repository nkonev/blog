<template>
    <div>
        <h1>Type some country</h1>

        <!--<vue-typeahead
                :suggestion-attribute="suggestionAttribute"
                v-model="value"
                :disabled="false"
                @input="changed"
                :show-autocomplete="true"
                :autofocus="false"
                :suggestions="suggestions"
                name="customName"
                placeholder="custom placeholder"
                type="google"
        ></vue-typeahead>-->
        <vue-typeahead
                prefetch="https://twitter.github.io/typeahead.js/data/films/post_1960.json"
                display-key='value'
                classes="form-control"
                remote="https://twitter.github.io/typeahead.js/data/films/queries/%QUERY.json'"
                v-on:selected="done">
        </vue-typeahead>
    </div>
</template>

<script>
    import Vue from 'vue'
    import VueTypeahead from 'vuejs-autocomplete'

    export default {
        name: "Autocomplete",
        data() {
            return {
                responseWrapper: 'value', // AutocompleteResponse
                remote: [],
            }
        },
        methods: {
            done: function(data) {
                /*var that = this;
                this.suggestions = [];
                this.$http.get('/api/public/autocomplete?prefix=' + this.value)
                    .then(
                        response => {
                            that.suggestions = response.body;
                            console.log(that.suggestions)
                        },
                        response => {
                            // error callback
                            console.error(response);
                        }
                    )*/
                console.log(data);
            }
        },
        components: {
            vueTypeahead: VueTypeahead
        }
    }

</script>