// build script will replace the following variables:
var BSHARE_FEEDS_BASE="http://api.bshare.cn",
    LEZHI_PLUGIN_BASE="http://lz.bshare.local:8098/plugin",
    LEZHI_STATIC_BASE="http://lz.bshare.local:8099/assets";
    
	//var LEZHI_PLUGIN_BASE="http://lzplugin.bshare.cn/plugin",
    //LEZHI_STATIC_BASE="http://lzstatic.bshare.cn";

(function() {
	if (typeof window.lezhi.init != "undefined") return;

    var w = window, d = w.document, eu = encodeURIComponent, du = decodeURIComponent,
        lzJsPath = LEZHI_STATIC_BASE + "/plugin/", 
        lzImgPath = LEZHI_STATIC_BASE + "/plugin/img/",
        defaultPicUrl = lzImgPath + "default.gif", 

        //lzJsPath = "",
	
    lz = w.lezhi = w.lezhi.util.joinProperty(w.lezhi, {
		modules: { 
			nodes: [],
			config: [],
			currentActivated: [],
			sourceCount: [],
			isSetCount: []
        },
		variables: {
			ADS_JS_URL: ["http://static.mediav.com/js/mvf_g2.js"],
			ADS_JS_IDS_MEDIAV: {
				"300x250": "3cv6mn_1019305",
				"200x200": "I1hc34_1019688",
				"300x100": "8yAhOD_1019306",
				"610x100": "gxGLV3_1019413",
				"660x90": "pMlk3O_1019412",
				"728x90": "7ZRsbM_1015958"
				
			},
			ADS_JS_WRAP_MEDIAV: "mediaV",
			ADS_PRELOAD_DIV_ID: "lzPreloadDiv",
			ADS_CONTENT_ID: "mvdiv_1019305_holder",
			DEFAULT_PIC_URL: defaultPicUrl,
			HOT_PIC_URL: lzImgPath + "hot.png",
			PAGEON_PIC_URL: lzImgPath + "on.png",
			PAGEOFF_PIC_URL: lzImgPath + "off.png",
	        BSHARE_FEEDS_LINK: BSHARE_FEEDS_BASE + "/feeds/trending.json?callback=window.lezhi.callback",
	        LEZHI_PLUGIN_LINK: LEZHI_PLUGIN_BASE + "/feeds?callback=window.lezhi.callback",
			LEZHI_CORRECT_LINK: LEZHI_PLUGIN_BASE + "/correct?",
			LEZHI_UTIL_LINK: lzJsPath + "lz-utilOrg.js",
			LEZHI_ENGINE_LINK: lzJsPath + "lz-engineOrg.js",
			LEZHI_STYLEDEFAULT_LINK: lzJsPath + "lz-style-defaultOrg.js",
			LEZHI_STYLE300x250_LINK: lzJsPath + "lz-style-300x250Org.js",
			LEZHI_STYLE200x200_LINK: lzJsPath + "lz-style-200x200Org.js",
			LEZHI_STYLE300x100_LINK: lzJsPath + "lz-style-300x100Org.js",
			LEZHI_STYLE728x90_LINK: lzJsPath + "lz-style-728x90Org.js",
			BSHARE_RECOMM_TYPES: ["fixed", "slide"],
			BSHARE_RECOMM_POSITIONS: ["left", "right"],
			//BSHARE_RECOMM_POSITIONYS: ["bottom"],
			//BSHARE_RECOMM_SOURCES: ["insite", "trending", "itemcf"],
			//BSHARE_RECOMM_REDIRECT: ["js", "http"],
			BSHARE_RECOMM_REDIRECT_TYPE: ["_self", "_blank"],
			//BSHARE_RECOMM_PICMATCH: ["insite, outsite"],
			BSHARE_RECOMM_LISTTYPE: ["disc", "square", "none"],
			STYLETYPE_DEFAULT: "lz-style-default",
			STYLETYPE_300x250: "lz-style-300x250",
			STYLETYPE_200x200: "lz-style-200x200",
			STYLETYPE_300x100: "lz-style-300x100",
			STYLETYPE_728x90: "lz-style-728x90",
			BSHARE_RECOMM_SOURCES_NAMES: {
				"trending": "最热文章",
				"insite": "您可能也喜欢",
				"itemcf": "个人推荐"
			},
			//BSHARE_FEEDS_OPTIONS: {},
			// interval vars:
			adsIds: [],
			adsPreloadDone: [], // if ads has been preloaded already, array by module index
			adsShow: false, // if ads has been shown (for sending stats once)
			currentPage: [], // keeps track of the current page for each lz module
			//recommendation: {},  //save feeds back result
			referrer: d.referrer,
			requestedScripts: [],
			trendingShow: false, // if trending has already been shown (for sending stats once)
			vid: ''
		},
		// these are the default settings:
		config: {
			global: {
				uuid: "",
				promote: "您可能也喜欢",
				col: 5,
				row: 2,
				pic: true, // true for pic mode, false for textMode
				type: "fixed", // BSHARE_RECOMM_TYPES
				picSize: 88, // pic size for picMode only in pixels
				fontSize: 12,  //font-size for textMode only in pixels
				sitePrefix: "",
				defaultPic: defaultPicUrl,
				position: "right", // BSHARE_RECOMM_POSITIONS
				//positionY: "bottom", // BSHARE_RECOMM_POSITIONYS
				source: "insite", // BSHARE_RECOMM_SOURCES
				url: eu(d.location.href), // TODO what if someone changes the URL? we need to handle the title and hash.
				title: d.title,
				hash: du(w.location.hash.substring(1)),
				htcolor: "#333", // heading text color
				rtcolor: "#333", // recomm text color
				bdcolor: "#DADADA", // border color 
				hvcolor: "#333", // bgcolor/font color on mouse over 
				highlight: false, // set 2 results color to red
				showLogo: true, // show logo or hide logo
				mock: false, // callback default feeds
				count: 0,
				adCount: 0,
				autoPageTime: 6000,
				//redirectMode: "js", // redirect mode
				/* we don't need to explicitly set these to an empty string.
				 * if it is null, it will be ignored...
				//brand: "", // set logo by user set
				//customThumbnail: "",
				//customTitle: "" ,*/
				
		        // new params for Lezhi 2.0
				width: 0, // set the plugin div width
				height: 0, // set the plugin div height
				titleBgColor: "#fff", // title background color
				//titleImage: "", // title background image
				titleFontSize: 12, // title font size
				titleBold: false, // title font weight
				fontBold: false, // recomm text font weight
				fontUnderline: false, // recomm text underline
				linkUnderline: false, // recomm text underline when mouse hover
				redirectType: "_blank", // link to by _self or _blank
				picMatch: "insite", // pic match mody by insite , outsite
				bgcolor: "#fff", // plugin div background color
				picBorderRadius: true, // pic border is radius 5px
				lineHeight: 15, // recomm text line height
				listType: "disc" // recomm text list type on text mode
				//adEnabled: true, // ad override for 300x250 modules
				//adPreload: true // ad override for 300x250 preloading js ads
			}
		},
        init: function() {
        	var i;
        	
			// get all lz-modules
			lz.modules.nodes = lz.util.getElemByClassName(d, "div", "lz-module");
			if (lz.util.getElemById("bShareRecommDiv")) {
				i = lz.modules.nodes.push(lz.util.getElemById("bShareRecommDiv"));
				lz.modules.nodes[i-1].oldVersionId = true;
			}
			if (lz.modules.nodes.length == 0) return;
			
			//load lz-engine.js
			lz.util.loadModule(lz.variables.LEZHI_ENGINE_LINK);
        },
        switchTo: function(target, sourceType, type, pages, index) {
        	//lz.variables.openAds = false;
            var recommTab = target.parentNode, recommDiv2, pagesList = [], currentPageNum = 0;
            if (pages > 1) {
            	recommDiv2 = lz.util.getElemByClassName(recommTab.parentNode, "div", "bShareRecommDiv3")[0];
            	pagesList = lz.util.getElemByClassName(recommTab.parentNode, "div" ,"bShareRecommPages")[0].getElementsByTagName("span");
            	currentPageNum = 1;
            } else {
            	recommDiv2 = recommTab.parentNode;
            }
			var	lists = recommDiv2.childNodes,
                tabs = recommTab.getElementsByTagName("li"), i, lengths = lists.length,
                //j = type == "slide" ? 2 : 1;
                j = 0;
            j = pages > 1 ? 0 : type === "slide" ? 2 : 1;	
            for (i = 0; i < tabs.length; ++i) {
                tabs[i].className = "";
                //tabs[i].disabled = false;
            }
            target.className = "activated";
            //target.disabled = "disabled";
            for (i = j; i < lengths; ++i) {
                if (lists[i].getAttribute("sourceType") == sourceType) {
                    lists[i].style.display = "block"; 
                    lz.util.addClass(lists[i], "activated");
                    currentPageNum = lists[i].getAttribute("cp") || 0;
                } else {
                    lists[i].style.display = "none";
                    lz.util.removeClass(lists[i], "activated");
                }
            }

			for (i = 0; i < pagesList.length; i++) {
			    pagesList[i].className = currentPageNum == i ? "on" : "";
			}
            //resizeWidget(recommDiv,false);
			if (!lz.variables.trendingShow && sourceType == "trending") {
				lz.stats.view("trending", index);
				lz.variables.trendingShow = true;
			}
        },
        autoPage: function(x2, pages2, col2, time, offset) {
        	var objs = lz.util.getElemsByTagName(lz.util.getElemByClassName(lz.modules.nodes[x2], "div", "bShareRecommPages")[0], "SPAN");
        	if (lz.util.isUndefined(lz.variables.currentPage[x2])) lz.variables.currentPage[x2] = 0;
			if (lz.variables.currentPage[x2] >= pages2) lz.variables.currentPage[x2] = 0;
			lz.switchPage(objs[lz.variables.currentPage[x2]], lz.variables.currentPage[x2]++, col2, offset);
			setTimeout(function() { lz.autoPage(x2, pages2, col2, time, offset) }, time);
        },
        switchPage: function(obj, pageNum, col, offset) {
			var recommDiv3 = lz.util.getElemByClassName(obj.parentNode.parentNode.parentNode, "div", "bShareRecommDiv3")[0], 
			    finalPos = +recommDiv3.clientWidth * -pageNum - (!lz.util.isUndefined(offset) ? offset : 0),
			    panel = lz.util.getElemByClassName(recommDiv3, "div", "activated")[0], list = lz.util.getElemsByTagName(panel, "li"), 
			    adEntryIds = [], index = lz.util.getIndex(lz.modules.nodes, obj.parentNode.parentNode.parentNode),
			    slide, currentPos = parseInt(panel.style.left || 0), pageSpans = obj.parentNode.getElementsByTagName("span"), 
			    i, start = (pageNum + 1) * col, pageSpansLen = pageSpans.length, adEntryId;
			
			//currentPos = config.type === "slide" ? currentPos - 25 : currentPos;
			//finalPos = config.type === "slide" ? finalPos + 25 : finalPos;
			for (i = 0; i < pageSpansLen; i++) {
				lz.util.removeClass(pageSpans[i], "on");
			}
			lz.util.addClass(obj, "on");
			slide = function() {
				var des = finalPos > currentPos,
				    movePixels = !des ? col*5 : col*10, timeIter = 1;
				if (currentPos == finalPos) {
                    return;
                } 
				currentPos = des ? currentPos + movePixels : currentPos - movePixels;
                if ((des && currentPos > finalPos) || (!des && currentPos < finalPos)) {
                    currentPos = finalPos;
                }
				// save panel current page
				panel.setAttribute("cp", pageNum);
                panel.style.left = currentPos + "px";
                setTimeout(slide, timeIter);
			};
			slide();
			
			for (i = start - col; i < start; i++) {
				if (list[i] && (adEntryId = list[i].getAttribute("adEntryId"))) {
					if (adEntryId && !lz.util.arrayContains(lz.variables.adsIds[index], adEntryId)) {
						lz.variables.adsIds[index].push(adEntryId);
						adEntryIds.push(adEntryId);
					} else {
						return;
					}
				} else {
					return;
				}
			}

			lz.stats.adsView(index, adEntryIds);
		},
		stats: {
			view: function(ref, index) {
				var config = lz.modules.config[index],
				showUpParam = [
	                "uuid=" + config.uuid,
	                "url=" + config.url,
	                "ref=" + ref,
	                "type=" + config.type,
					"pic=" + config.pic,
					"timestamp=" + +new Date()
				];
				showUpParam = showUpParam.join("&");
				lz.util.loadModule(LEZHI_PLUGIN_BASE + "/showup?" + showUpParam);																							
			},
			adsView: function(index, ads) {
				var config = lz.modules.config[index],
				showUpParam = [
	                "uuid=" + config.uuid,
	                "url=" + config.url,
					"timestamp=" + +new Date()
				];
				showUpParam = showUpParam.join("&") + (lz.variables.adsShow ? "&adEntryIds=" + ads.join(",") : "");
				lz.util.loadModule(LEZHI_PLUGIN_BASE + "/adShowup?" + showUpParam);	
			}
		},
		ads: {
			preload: function(idx, node, jsUrl) {
				var width = 300, height = 250, scriptEle;
				if (node.lezhiStyle == lz.variables.STYLETYPE_200x200) {
					width = 200;
					height = 200;
				}
				if (node.lezhiStyle == lz.variables.STYLETYPE_300x100) {
					width = 300;
					height = 100;
				}
				if (node.lezhiStyle == lz.variables.STYLETYPE_728x90) {
					width = 728;
					height = 90;
				}
				w.mediav_ad_wrap = lz.variables.ADS_PRELOAD_DIV_ID + idx;
			    w.mediav_ad_pub = lz.variables.ADS_JS_IDS_MEDIAV[width + "x" + height];
			    w.mediav_ad_width = width;
			    w.mediav_ad_height= height;
			    var div = lz.util.createElement("div", lz.variables.ADS_PRELOAD_DIV_ID + idx, "", "width:" + width + "px;height:" + height + "px;background:#eee;"),
	        	scriptEle = lz.util.createElement("script");
	        	scriptEle.src = jsUrl;
	        	scriptEle.type = 'text/javascript';
	        	scriptEle.charset = 'utf-8';
	        	//scriptEle.setAttribute("language", "javascript");
        		scriptEle.onload = function() {
        			lz.ads.preloadCb(idx);
        		}
        	    scriptEle.onreadystatechange = function() {
                	if (/complete|loaded/.test(this.readyState)) {
                		lz.ads.preloadCb(idx);
					}
				};
				
				node.appendChild(div);
				// append to doc head:
				lz.util.getElemsByTagName(d, "head")[0].appendChild(scriptEle);
			},
			preloadCb: function(idx) {
				lz.variables.adsPreloadDone[idx] = true;
			}
		},
		getConfigurations: function(serverConfig) {
			// this function loads correct configurations for all modules into lezhi.modules.config[index]
			var  bshareRecommConfig = w.bshare_recomm_config || {},
		    lezhiConfig = w.lezhi_config || {}, //get user defined window.lezhi_config
			lezhiStyleCoreConfig = w.lezhi_style_core || {}, //get user defined window.lezhi_style_core config
			lezhiStyleDefaultConfig = w.lezhi_style_default || {}, // get user defined window.lezhi_style_default config
			lezhiStyle300x250Config = w.lezhi_style_300x250 || {}, // get user defined window.lezhi_style_300x250 config
			lezhiStyle200x200Config = w.lezhi_style_200x200 || {}, // get user defined window.lezhi_style_200x200 config
			lezhiStyle300x100Config = w.lezhi_style_300x100 || {}, // get user defined window.lezhi_style_300x100 config
			lezhiStyle728x90Config = w.lezhi_style_728x90 || {}; // get user defined window.lezhi_style_728x90 config
			
	    	var p, utilVar, jsUserConfig = {}, iu = lz.util.isUndefined, inu = lz.util.isNotUndefined;
	    	    lezhiStyleDefaultConfigGlobal = lezhiStyleDefaultConfig.global || {},
	    	    lezhiStyle300x250ConfigGlobal = lezhiStyle300x250Config.global || {},
	    	    lezhiStyle200x200ConfigGlobal = lezhiStyle200x200Config.global || {},
	    	    lezhiStyle300x100ConfigGlobal = lezhiStyle300x100Config.global || {},
	    	    lezhiStyle728x90ConfigGlobal = lezhiStyle728x90Config.global || {};

	    	// first merge with pre loaded js params:
	    	lz.config.global = lz.util.mergeConfig(lz.config.global, lz.variables.configPre);
			// priority: specific style config (id, global), core style JS config (global), lezhi JS config (id, global), server config, parameter config
			// take care of global declarations first:
			lz.config.global = lz.util.mergeConfig(lz.config.global, getRcommConfig()); // merge default config with javascript parameter options first
			if (serverConfig) lz.config.global = lz.util.mergeConfig(lz.config.global, serverConfig); // merge server configs
			// these are the user js configs. Save into a separate array for shorter code later:
			jsUserConfig.global = lz.util.mergeConfig({}, w.bshare_recomm_config || {}); // merge old lezhi configs
			jsUserConfig.global = lz.util.mergeConfig(jsUserConfig.global, lezhiConfig.global || {}); // merge lezhi_config global
			jsUserConfig.global = lz.util.mergeConfig(jsUserConfig.global, lezhiStyleCoreConfig.global || {}); // merge lezhi_style_core global
			// merge user js configs into main config:
			lz.config.global = lz.util.mergeConfig(lz.config.global, jsUserConfig.global);

			// iterate through all lz-modules and set configurations (that do not include server side)
			for (p = 0; p < lz.modules.nodes.length; p++) {
				// if user has specifically defined any one of col, row, or count, mark it for later.
				lz.modules.isSetCount[p] = checkIfSetCount(jsUserConfig.global);

				utilVar = lz.modules.nodes[p];
				lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lz.config.global); // make a deep copy first
				if (lz.util.lzhasClass(utilVar, lz.variables.STYLETYPE_DEFAULT) || utilVar.oldVersionId) {
					// merge lezhi_style_default global
					lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyleDefaultConfig.global || {});
					// set the lezhi style (use this variable to determine style)
					lz.modules.nodes[p].lezhiStyle = lz.variables.STYLETYPE_DEFAULT;
					// if user has specifically defined any one of col, row, or count, mark it for later.
					if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyleDefaultConfigGlobal);

					if (utilVar.id && !iu(lezhiStyleDefaultConfig[utilVar.id])) {
						// node has an ID, and has specific JS settings
						lz.modules.nodes[p].idConfig = true;
                        lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyleDefaultConfig[utilVar.id]);
                        if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyleDefaultConfig[utilVar.id]);
                    }
	            } else if (lz.util.lzhasClass(utilVar, lz.variables.STYLETYPE_300x250)) {
	            	// 300x250 modules default to ad enabled and preloading disabled but can be overridden by front-end js:
	            	lz.modules.config[p].adEnabled = true;
	            	lz.modules.config[p].adPreload = false;
	            	
	            	// merge lezhi_style_300x250 global
					lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyle300x250Config.global || {}); 
					// set the lezhi style (use this variable to determine style)
					lz.modules.nodes[p].lezhiStyle = lz.variables.STYLETYPE_300x250;
					// if user has specifically defined any one of col, row, or count, mark it for later.
					if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyle300x250ConfigGlobal);
					
					if (utilVar.id && !iu(lezhiStyle300x250Config[utilVar.id])) {
						// node has an ID, and has specific JS settings
						lz.modules.nodes[p].idConfig = true;
                        lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyle300x250Config[utilVar.id]);
                        if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyle300x250Config[utilVar.id]);
                    }
	            } else if (lz.util.lzhasClass(utilVar, lz.variables.STYLETYPE_200x200)) {
	            	// 200x200 modules default to ad enabled and preloading disabled but can be overridden by front-end js:
	            	lz.modules.config[p].adEnabled = true;
	            	lz.modules.config[p].adPreload = false;
	            	
	            	// merge lezhi_style_200x200 global
					lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyle200x200Config.global || {}); 
					// set the lezhi style (use this variable to determine style)
					lz.modules.nodes[p].lezhiStyle = lz.variables.STYLETYPE_200x200;
					// if user has specifically defined any one of col, row, or count, mark it for later.
					 if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyle200x200ConfigGlobal);
					
					if (utilVar.id && !iu(lezhiStyle200x200Config[utilVar.id])) {
						// node has an ID, and has specific JS settings
						lz.modules.nodes[p].idConfig = true;
                        lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyle200x200Config[utilVar.id]);
                        if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyle200x200Config[utilVar.id]);
                    }
	            } else if (lz.util.lzhasClass(utilVar, lz.variables.STYLETYPE_300x100)) {
	            	// 300x100 modules default to ad enabled and preloading disabled but can be overridden by front-end js:
	            	lz.modules.config[p].adEnabled = true;
	            	lz.modules.config[p].adPreload = false;
	            	
	            	// merge lezhi_style_300x100 global
					lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyle300x100Config.global || {}); 
					// set the lezhi style (use this variable to determine style)
					lz.modules.nodes[p].lezhiStyle = lz.variables.STYLETYPE_300x100;
					// if user has specifically defined any one of col, row, or count, mark it for later.
					 if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyle300x100ConfigGlobal);
					
					if (utilVar.id && !iu(lezhiStyle300x100Config[utilVar.id])) {
						// node has an ID, and has specific JS settings
						lz.modules.nodes[p].idConfig = true;
                        lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyle300x100Config[utilVar.id]);
                        if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyle300x100Config[utilVar.id]);
                    }
	            } else if (lz.util.lzhasClass(utilVar, lz.variables.STYLETYPE_728x90)) {
	            	// 300x100 modules default to ad enabled and preloading disabled but can be overridden by front-end js:
	            	lz.modules.config[p].adEnabled = true;
	            	lz.modules.config[p].adPreload = false;
	            	
	            	// merge lezhi_style_300x100 global
					lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyle728x90Config.global || {}); 
					// set the lezhi style (use this variable to determine style)
					lz.modules.nodes[p].lezhiStyle = lz.variables.STYLETYPE_728x90;
					// if user has specifically defined any one of col, row, or count, mark it for later.
					 if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyle300x100ConfigGlobal);
					
					if (utilVar.id && !iu(lezhiStyle728x90Config[utilVar.id])) {
						// node has an ID, and has specific JS settings
						lz.modules.nodes[p].idConfig = true;
                        lz.modules.config[p] = lz.util.mergeConfig(lz.modules.config[p], lezhiStyle728x90Config[utilVar.id]);
                        if (!lz.modules.isSetCount[p]) lz.modules.isSetCount[p] = checkIfSetCount(lezhiStyle728x90Config[utilVar.id]);
                    }
	            }
			}
			
			function checkIfSetCount(globalConfig) {
				return inu(globalConfig.col) || inu(globalConfig.row) || inu(globalConfig.count);
			}
	    }
    });
    

    // get parameter config before doc ready
    lz.variables.configPre = getRcommConfig();
    
	// Begin display of Lezhi    
    lz.util.ready(lz.init);
	
	function getRcommConfig() {
        var _scripts = lz.util.getElemsByTagName(d, "script"),
            _i, _j, parms, _par0, _par1, _scriptLz,
            _bshare_recomm_config = {};
        for (_i = 0; _i < _scripts.length; _i++) {
            if (/(lzKDS|lezhiKDS)(Org|Tmp)?\.js/.test(_scripts[_i].src)) {
            	// gets recommendation param options from the FIRST declaration of lz.js
            	// support both # and ? after embedded javascript.
                _scriptLz = /\?(.*)|#(.*)/.exec(_scripts[_i].src);
                if (_scriptLz) {
                	_scriptLz = _scriptLz[0].slice(1).split("&");
        	        for (_j = 0; _j < _scriptLz.length; _j++) {
        	            parms = _scriptLz[_j].split("=");
        	            if ((_par0 = parms[0]) != "") {
        		            _par1 = parms[1];
        		            // NOTE: checking if params are valid will be done during mergeConfig()
        		            // but need to convert these first:
        		            if (/(col|row|(pic|font|titleFont)Size|width|height|lineHeight|count)$/.test(_par0)) {
        		                _par1 = +_par1;
        		            } else if (/(pic|highlight|showLogo|picBorderRadius|(title|font)Bold|(font|link)Underline)$/.test(_par0)) {
        		                _par1 = _par1 == "true";
        		            }
        		            _bshare_recomm_config[_par0] = _par1;
        	            }
        	        }
        	        break;
        	    }
            }
        }
        return _bshare_recomm_config;
    }
})();
