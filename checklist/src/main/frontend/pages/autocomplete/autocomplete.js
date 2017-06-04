import $ from 'jquery';
// https://stackoverflow.com/questions/33998262/jquery-ui-and-webpack-how-to-manage-it-into-module
import "jquery-ui/ui/widgets/autocomplete";
// import "jquery-ui/themes/base/all.css";
import "jquery-ui/themes/base/base.css";
import "jquery-ui/themes/base/theme.css";
import "jquery-ui/themes/base/autocomplete.css";

console.log("autocomplete.js");

$(`#app`).html(`
<h1>Type some country</h1>

<div class="ui-widget">
    <label for="countries-list">Countries: </label>
    <input id="countries-list"/>
</div>
`);

$("#countries-list").autocomplete({
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
