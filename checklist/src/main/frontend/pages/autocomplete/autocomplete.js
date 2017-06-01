import $ from 'jquery';
// https://stackoverflow.com/questions/33998262/jquery-ui-and-webpack-how-to-manage-it-into-module
require("jquery-ui/ui/widgets/autocomplete");
require("jquery-ui/themes/base/all.css");

console.log("autocomplete.js");

$(`#app`).append("autocomplete.js\n");


$( "#tags" ).autocomplete({
    // http://jqueryui.com/autocomplete/#multiple-remote http://api.jqueryui.com/autocomplete/
    source: function( request, response ) {
        $.getJSON(
            "/api/autocomplete",
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