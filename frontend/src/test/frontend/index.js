// этот файл `index.js` подключается на страницу 1 раз ( == 3 раза в karma.conf.js =) )

// apply and configure jasmine plugins
import 'jquery';
import 'jasmine-jquery';
//jasmine.getFixtures().fixturesPath = 'base/src/test/frontend/fixtures';
//jasmine.getJSONFixtures().fixturesPath = 'base/src/test/frontend/responses';
import 'jasmine-ajax';

import Vue from 'vue'
Vue.config.devtools = false;
Vue.config.productionTip = false;


console.log("[autotest] index.js loaded");