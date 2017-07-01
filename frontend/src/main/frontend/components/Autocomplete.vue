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
    import VueInstant from '../lib/VueInstant.vue'

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
                    )
            }
        },
        components: {
            vueInstant: VueInstant
        }
    }

</script>