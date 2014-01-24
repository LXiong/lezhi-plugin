/*
 * lezhi plugin
 */

/* !!! Do not add space for "=", write it this way to match the replace in build */
var LEZHI_JS_HOST="http://lz.bshare.local:8099/assets/plugin/";
(function () {
	document.write('<script type="text/javascript" src="' + LEZHI_JS_HOST + 'lz-utilOrg.js"></script>');
	
	document.write('<script type="text/javascript" src="' + LEZHI_JS_HOST + 'lzOrg.js"></script>');
})();
