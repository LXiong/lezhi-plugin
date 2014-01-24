(function() {
    var w = window, eu = encodeURIComponent, d = w.document,
        lz = w.lezhi, u = lz.util, lzMod = lz.modules, lzVars = lz.variables,
        config, count = 0, i, k, j, l;

    lz.initEngine = function() {
		if (!lzVars.referrer && w.opener) {
			try {
				// ie will throw error while cross domain
				lzVars.referrer = w.opener.location.href;
			} catch (e) {}
		}
		
		// configuration processing:
		lz.getConfigurations();
		
		// config can not overwrite by lz.config.global so have to reset config = lz.config.global
		config = lz.config.global;
		//load adMax.js
		if (w.ADMAX_CONFIG != undefined) {
			w.ADMAX_CONFIG.uuid = config.uuid;
			lz.util.loadModule(lz.variables.ADMAX_LINK);
		} 
		// see if we should preload ad:
		for (i = 0, k = 0; i < lzMod.nodes.length; i++) {
			j = lzMod.nodes[i], l = lzMod.config[i];
	        if (l.adEnabled) {
	        	// if the ads js url array is not long enough, reset to the first js url
	        	if (!lzVars.ADS_JS_URL[k]) k = 0;
	        	if (l.adPreload) lz.ads.preload(i, j, lzVars.ADS_JS_URL[k]);
	        	j.adsJsUrl = lzVars.ADS_JS_URL[k++];
	        }
	    }
		
		// load the recommendations from servers
		lz.loadRecomm();
	};
	lz.loadRecomm = function(_url, _title) {
		// check params:
		if (_url) {
			config.url = _url;
			config.hash = lz.util.getHash(_url);
		}
		if (_title) config.title = _title;
		
		// Set parameters to send to server to get recommendations:
		var lezhiParam = [ 
			"uuid=" + config.uuid, 
			"url=" + config.url, 
			"title=" + eu(config.title), 
			"sitePrefix=" + eu(config.sitePrefix), 
			//"count=" + config.count,
			"hash=" + config.hash, 
			"keywords=" + u.getKeywords(),
			"referrer=" + eu(lzVars.referrer),
			"mock=" + config.mock,
			"timestamp=" + +new Date()
		], p, x, _p;

		for (p = 0, _p = 1; p < lzMod.nodes.length; p++) {
			if (!!lzMod.nodes[p].oldVersionId) {
				lezhiParam.push("type=" + (config.source = lzMod.config[p].source));
				if (!!lzMod.isSetCount[p]) {
					if (lzMod.config[p].count == 0) {
						count = lzMod.config[p].row * lzMod.config[p].col;
					} else {
						count = lzMod.config[p].count;
					}
					if (lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_300x250
					    || lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_200x200
					    || lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_250x250
                        || lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_125x125
					    || lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_610x100) {
						lz.modules.config[p]["adCount"] = 0;
					}
				}
				lezhiParam.push("count=" + count);
			}
			
			var singleSource = lzMod.config[p]["source"].split(","), obj = {};
			for (x = 0; x < singleSource.length; x++, _p++) {
				if (lzMod.config[p].count == 0 && lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_DEFAULT) {
					// type 300x250 and 200x200 can set count to 0... this means only show ads
					obj[singleSource[x] + ":" + _p] = lzMod.config[p].col * lzMod.config[p].row;
				} else {
					obj[singleSource[x] + ":" + _p] = lzMod.config[p].count;
				}
			}
			lz.modules.sourceCount[p] = obj;
		}
		if (config.customTitle) lezhiParam.push("customTitle=" + config.customTitle);
		if (config.customThumbnail) lezhiParam.push("customThumbnail=" + config.customThumbnail);
		
		if (!(lzMod.nodes.length == 1 && !!lzMod.nodes[0].oldVersionId)) {
			var _sourceCount = [], _currentCount = 0;
			for (p = 0, _p = 1; p < lzMod.nodes.length; p++) {
				if (!lzMod.nodes[p].oldVersionId) {
					if (lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_300x250
					 || lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_200x200 
					 || lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_250x250
					 || lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_125x125
					 || lzMod.nodes[p].lezhiStyle == lzVars.STYLETYPE_610x100) {
						lz.modules.config[p]["adCount"] = 0;
					}
					for (x in lzMod.sourceCount[p]) {
						if (!!lzMod.isSetCount[p]) {
							if (lzMod.config[p].count == 0) {
								_currentCount = lzMod.config[p].row * lzMod.config[p].col;
							} else {
								_currentCount = lzMod.sourceCount[p][x];
							}
						}
						_sourceCount.push('{"order":"' + _p + '",' 
					              + '"recommendType":"' + x.split(":")[0] + '",' 
					              + '"count":"' + _currentCount + '",' 
					              + '"adCount":"' + lzMod.config[p]["adCount"] + '"}');
						_p++;
					}
				}
			}
			
			lezhiParam.push("types=[" + _sourceCount.join(",") + "]");
		}
		
		// Send request to server for recommendations
		u.loadModule(lzVars.LEZHI_PLUGIN_LINK + "&" + lezhiParam.join("&"));
	}
	
	// This is the callback that comes back with recommendation info and configurations
	lz.callback = function(result) {
		var bestImage = u.matchImage(), q, oldDivIndex;
		lzVars.recommendation = result;
		
		// configuration processing:
		// priority: page JS config, server config, parameter config
		// see if there was a result
		if (result) {
			lz.getConfigurations(result.config); // merge server config with all other configs
			
			if (q = lz.util.getElemById("bShareRecommDiv")) {
				var oldDivSource = [], oldDivConfig;
				oldDivIndex = lzMod.nodes.length - 1; // the last obj in lz.module.nodes is always the old div
				oldDivConfig = lzMod.config[oldDivIndex];
				oldDivSource = oldDivConfig.source.split(",");
				lz.modules.sourceCount[oldDivIndex] = {};
				for (var i = 0; i < oldDivSource.length; i++) {
					lz.modules.sourceCount[oldDivIndex][oldDivSource[i]] = oldDivConfig.count == 0 ? oldDivConfig.col * oldDivConfig.row : oldDivConfig.count;
				}
			}

            if (!!result.bx && d.images) new Image().src="http://www.bznx.net/cms.gif?a=lezhi&c=" + result.vid;
			// if bestImage does not match then post to server
	        if (result.thumbnail !== bestImage && bestImage && !config.customThumbnail) {
	            u.loadModule(lz.variables.LEZHI_CORRECT_LINK + "&" + ["url=" + config.url, "thumbnail=" + eu(bestImage)].join("&"));
	        }
	
	        // load necessary style JS files
			for (q in lzMod.nodes) {
				if (lzMod.nodes[q].lezhiStyle == lzVars.STYLETYPE_DEFAULT) {
					if (typeof lz.render == "function") {
						lzMod.nodes[q].innerHTML = "";
						lz.render(lzVars.recommendation, true);
					}
					else u.loadModule(lzVars.LEZHI_STYLEDEFAULT_LINK);
				}
				if (lzMod.nodes[q].lezhiStyle == lzVars.STYLETYPE_300x250) {
					if (typeof lz.render300x250 == "function") {
						lzMod.nodes[q].innerHTML = "";
						lz.render300x250(lzVars.recommendation, true);
					}
					else u.loadModule(lzVars.LEZHI_STYLE300x250_LINK);
				}
				if (lzMod.nodes[q].lezhiStyle == lzVars.STYLETYPE_200x200) {
					if (typeof lz.render200x200 == "function") {
						lzMod.nodes[q].innerHTML = "";
						lz.render200x200(lzVars.recommendation, true);
					}
					else u.loadModule(lzVars.LEZHI_STYLE200x200_LINK);
				}
				if (lzMod.nodes[q].lezhiStyle == lzVars.STYLETYPE_250x250) {
					if (typeof lz.render250x250 == "function") {
						lzMod.nodes[q].innerHTML = "";
						lz.render250x250(lzVars.recommendation, true);
					}
					else u.loadModule(lzVars.LEZHI_STYLE250x250_LINK);
				}
				if (lzMod.nodes[q].lezhiStyle == lzVars.STYLETYPE_125x125) {
					if (typeof lz.render125x125 == "function") {
						lzMod.nodes[q].innerHTML = "";
						lz.render125x125(lzVars.recommendation, true);
					}
					else u.loadModule(lzVars.LEZHI_STYLE125x125_LINK);
				}
				if (lzMod.nodes[q].lezhiStyle == lzVars.STYLETYPE_610x100) {
				    if (typeof lz.render610x100 == "function") {
                        lzMod.nodes[q].innerHTML = "";
                        lz.render610x100(lzVars.recommendation, true);
                    }
					else u.loadModule(lzVars.LEZHI_STYLE610x100_LINK);
				}
				if (lzMod.nodes[q].lezhiStyle == lzVars.STYLETYPE_CUSTOM) {
					//if (typeof lz.renderCustom == "function") {
					//	lzMod.nodes[q].innerHTML = "";
					//	lz.renderCustom(lzVars.recommendation, true);
					//}
					//else 
					u.loadModule(lzMod.config[q].styleJs);
				}
				//if (lzMod.nodes[q].lezhiStyle == lzVars.STYLETYPE_728x90) {
				//	u.loadModule(lzVars.LEZHI_STYLE728x90_LINK);
				//}
			}
		}
	}
	
	// initialize the engine:
	lz.initEngine();
})();
