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
    import VueInstant from 'vue-instant/dist/vue-instant.common'
    Vue.use(VueInstant);

    export default {
        name: "Autocomplete",
        /*mounted(){
            console.log("autocomplete.js mounted");

            $("#countries-list").autocomplete({
                // http://jqueryui.com/autocomplete/#multiple-remote http://api.jqueryui.com/autocomplete/
                source: function( request, response ) {
                    $.getJSON(
                        "/api/public/autocomplete",
                        {
                            prefix: request.term
                        },
                        response
                    );
                },
                minLength: 1,
                select: function( event, ui ) {
                    console.log( "Selected: " + ui.item.value + " aka " + ui.item.id );
                }
            });
        },*/
        data() {
            return {
                value: '',
                suggestionAttribute: 'original_title',
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
                        },
                        response => {
                            // error callback
                            console.error(response);
                        }
                    )
            }
        }
    }

</script>