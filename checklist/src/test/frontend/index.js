// этот файл `index.js` подключается на страницу 1 раз ( == 3 раза в karma.conf.js =) )

// apply and configure jasmine plugins
import $ from 'jquery';
import 'jasmine-jquery';
jasmine.getFixtures().fixturesPath = 'base/src/test/frontend/fixtures';
jasmine.getJSONFixtures().fixturesPath = 'base/src/test/frontend/responses';
import 'jasmine-ajax';

console.log("[autotest] index.js loaded");