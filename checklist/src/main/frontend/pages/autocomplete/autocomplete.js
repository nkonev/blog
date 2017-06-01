import $ from 'jquery';
// https://stackoverflow.com/questions/33998262/jquery-ui-and-webpack-how-to-manage-it-into-module
require("jquery-ui/ui/widgets/autocomplete");
require("jquery-ui/themes/base/all.css");

console.log("autocomplete.js");

$(`#app`).append("autocomplete.js\n");

const availableTags = [
    "ActionScript",
    "AppleScript",
    "Asp",
    "BASIC",
    "C",
    "C++",
    "Clojure",
    "COBOL",
    "ColdFusion",
    "Erlang",
    "Fortran",
    "Groovy",
    "Haskell",
    "Java",
    "JavaScript",
    "Lisp",
    "Perl",
    "PHP",
    "Python",
    "Ruby",
    "Scala",
    "Scheme"
];
$( "#tags" ).autocomplete({
    source: availableTags
});