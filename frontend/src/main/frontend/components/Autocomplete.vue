<template>
    <div>
        <h1>Type some country</h1>

        <vue-instant
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
        ></vue-instant>
    </div>
</template>

<script>
    import Vue from 'vue'
    import 'vue-instant/dist/vue-instant.css'
    // import VueInstant from 'vue-instant/dist/vue-instant.common.js'
    import VueInstant from 'vue-instant'
    Vue.use(VueInstant);

    export default {
        name: "Autocomplete",
        data() {
            return {
                value: '',
                suggestionAttribute: 'value', // AutocompleteResponse
                suggestions: [],
            }
        },
        methods: {
            changed: function() {
                var that = this;
                this.suggestions = [];
                this.$http.get('/api/autocomplete?prefix=' + this.value)
                    .then(
                        response => {
                            that.suggestions = response.body;
                            console.log(that.suggestions)
                        },
                        response => {
                            // error callback
                            console.error(response);
                        }
                    )
            }
        },
    }
</script>
