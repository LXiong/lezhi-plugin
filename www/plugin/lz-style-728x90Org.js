(function() {
    var w = window, d = w.document, db = d.body, eu = encodeURIComponent,
        lz = w.lezhi, variables = lz.variables, u = lz.util;

    lz.render728x90 = function(result) {
        // for each lz-modules, find 728x90 styles
        for (var x = 0; x < lz.modules.nodes.length; x++) {
        	if (lz.modules.nodes[x].lezhiStyle == variables.STYLETYPE_728x90) {
	            var recommDiv = lz.modules.nodes[x], 
	                cssPrefix = recommDiv.idConfig ? "#" + recommDiv.id : "." + variables.STYLETYPE_728x90, // get id specific config
	                config = lz.modules.config[x], count = config.count, highlight = config.highlight,
	                pages, pageCounts = 2, currentPageNum, mType, feeds, feedLen, feedsList, insHtml, redirectUrl, p, i = 0, utilVar,
	                recommDiv2 = u.createElement("div", "", "bShareRecommDiv2"),
					recommDiv3 = u.createElement("div", "", "bShareRecommDiv3"),
	            
	            adPreload = !!config.adPreload,
	            adEnabled = !!config.adEnabled;
	            //if (result.config && !u.isUndefined(result.config.adEnabled)) {
	            	// server side overrides front-end adEnabled variable
	            //	adEnabled = !!result.config.adEnabled;
	            //}
                
	            // only for 728 * 90 config
	            config.source = "insite";
	            variables.adsIds[x] = [];
	            // just handle feeds
	            for (mType in result) {
	            	for (p in lz.modules.sourceCount[x]) {
		                if (p !== mType) {
		                    continue;
		                }
		                
		                insHtml = "";
		                i = 0;
		                feeds = result[mType];
		                feedLen = feeds.length;
		                count = count < feedLen ? count : feedLen;
		                pages = Math.ceil(count / pageCounts) + +adEnabled; // calculate real pages (+ 1 for ad page)
		                
		                // this type only has one panel!
		                feedsList = u.createElement("div", "", "b-recomm-panel activated", "display:block;");
		                mType = mType.split(":")[0];
		                feedsList.setAttribute("sourceType", mType);
		                lz.modules.currentActivated[x] = mType;
		                lz.stats.view(mType, x); // mark as viewed
		                
		                for (currentPageNum = 0; currentPageNum < pages; currentPageNum++) {
		                	var scriptEle, adElem;
		                	insHtml += '<ul class="b-recomm-list">';
		                	
		                	if (currentPageNum == 0 && adEnabled) {
		                		if (adPreload) {
			                		// wait for adsPreload to finish
			                		var maxLoop = 30, idx = 0, cbLoop = function () {
			                			if (!!variables.adsPreloadDone[x]) {
			                				insHtml += u.getElemById(variables.ADS_PRELOAD_DIV_ID + x).innerHTML;
			                				idx = 30;
			                				recommDiv.removeChild(u.getElemById(variables.ADS_PRELOAD_DIV_ID + x));
			        					}
			        					if (idx >= maxLoop) return;
			        					++idx;
			        					setTimeout(cbLoop, 100);
			        				};
			        				cbLoop();
		                		} else {
		                			// no preload... load now
		                			adElem = lz.util.createElement("div", lz.variables.ADS_PRELOAD_DIV_ID + x);
		                			w.mediav_ad_wrap = variables.ADS_PRELOAD_DIV_ID + x;
								    w.mediav_ad_pub = variables.ADS_JS_IDS_MEDIAV["728x90"];
								    w.mediav_ad_width = "728";
								    w.mediav_ad_height= "90";
		            	        	// append this later or it will not load...
		            	        	scriptEle = recommDiv.adsJsUrl;
		                		}
		                	} else {
			                    for (var _lengths = (currentPageNum+1 - +adEnabled) * pageCounts; i < _lengths && i < feedLen && i < count; ++i) {
			                    	var curFeed = feeds[i], curFeedUrl = curFeed.url, curFeedTitle = curFeed.title,
			                            isMark = highlight && curFeed.mark;
			                        if (curFeed.ad) {
			                        	// small ads are disabled in 728x90
			                        	/*if (!variables.adsShow) variables.adsShow = true;
		    							//get adEntryId from callback url
		    							adEntryId = curFeed.adEntryId;
		    							if (pages <= 1) variables.adsIds[x].push(adEntryId);
		    							redirectUrl += "&adEntryId=" + adEntryId;*/
		    							continue;
			                        }
			                        if (!(p = curFeed.pic)) { // i want to set p here
		                                p = config.defaultPic;
		                                if (!p) {
                                            p = lz.variables.DEFAULT_PIC_URL;
                                        }
		                            } 
			                        // construct the URL for clicking a recommendation:
		                            redirectUrl = LEZHI_PLUGIN_BASE 
		                                + "/redirect?to=" + eu(curFeedUrl)
		                                + "&from=" + config.url
		                                + "&sitePrefix=" + eu(config.sitePrefix)
		                                + "&ref=" + mType
		                                + "&uuid=" + eu(config.uuid)
		    							+ "&type=" + eu(config.type)
		    							+ "&pic=true"
		                                + "&title=" + eu(config.title);
		                            
			                        insHtml += '<li onmouseover="this.style.background=\'' + config.hvcolor + '\'" onmouseout="this.style.background=\'none\'"';
			                        
			    					//if (config.redirectMode == "js") {
			    						insHtml += '><a href="' + curFeedUrl + '" onmousedown="this.href=\''
			    							+ redirectUrl + '\';return true;" onmouseup="t=this;setTimeout(function(){t.href=\''
			    							+ curFeedUrl + '\'},50);return true;" target="' + config.redirectType + '" title="'
			    							+ curFeedTitle + '"';
			    					/*} else if (config.redirectMode == "http") {
			    						insHtml += '><a href="' + curFeedUrl + '" onmousedown="loadModule(\'' 
			    						    + redirectUrl + '\');return true;" target="_blank" title="'
			    							+ curFeedTitle + '"';
			    					}*/
			    						
		                            insHtml += '" hidefocus="true" style="padding:25px 0 !important;">';
		                            
		    						// put img out div because if img and div inner a <a>tag will make the href of tag a does not work // this bug only for ie6
		    						insHtml += (isMark ? '<div class="hot-tag"></div>' : '')
		    						    + '<img class="bshare-logos" src="' + p
		    						    + '" style="width:80px !important;height:80px !important;"/>'
		                                + '<div class="feed-title" title="' + u.htmlEncode(curFeedTitle) + '"'
	    							    + (isMark ? ' style="color:red;"' : '')
	    							    + '>' + curFeedTitle + '</div></a></li>';
			                    }
			                }
		                    insHtml += "</ul>";
		                }
						
		                feedsList.innerHTML = insHtml;
		                if (adElem) {
		                	feedsList.childNodes[0].appendChild(adElem);
		                	lz.util.loadModule(scriptEle);
		                }
		                recommDiv3.appendChild(feedsList);
	            	}
	            }
	            recommDiv2.appendChild(recommDiv3);
	            
	            // append paging div if necessary:
	            if (pages > 1) {
		        	utilVar = u.createElement("div", "", "bShareRecommPages");
		        	insHtml = "";
		            for (i = 0; i < pages; i++) {
		            	insHtml += '<span onclick="window.lezhi.switchPage(this,' + i + ',2,0)" class="' + (i == 0 ? "on" : "") + '"></span>';
		            }
		            utilVar.innerHTML = insHtml;
		            recommDiv2.appendChild(utilVar);
		        }
	            
	            recommDiv.appendChild(recommDiv2);
	            loadCss(x, cssPrefix, pages);
	            
	            if (pages > 1) {
	            	// auto paging
		            lz.autoPage(x, pages, 2, config.autoPageTime, 0);
	            }
        	}
        }
	}

    function loadCss(index, cssPrefix, pages) {
        // css: #bShareRecommDiv
        var config = lz.modules.config[index], fontSize = config.fontSize, bdcolor = config.bdcolor,
            rtcolor = config.rtcolor, hvcolor = config.hvcolor, lineHeight = config.lineHeight, picBorderRadius = config.picBorderRadius,
			bgcolor = config.bgcolor, fontBold = config.fontBold, fontUnderline = config.fontUnderline, linkUnderline = config.linkUnderline,
            cssStyles = cssPrefix + "{width:728px;height:90px;z-index:99999;overflow:hidden !important;margin:0 !important;padding:0 !important;}"
				+ cssPrefix + " .bShareRecommDiv2{position:relative;margin:0 !important;padding:0 !important;}"
	            + ".lz-module:after,.bShareRecommDiv2:after,.bShareRecommDiv3:after,.b-recomm-panel:after,.b-recomm-tab:after{content:'.';display:block;height:0;clear:both;visibility:hidden;overflow:hidden;}";

        cssStyles += cssPrefix + " .bShareRecommDiv3{overflow:hidden;position:relative;left:0;width:728px;border:0 solid " + bdcolor + ";"
            + "background:" + bgcolor + ";}";
        // css: .b-recomm-tab li
        cssStyles += cssPrefix + " .b-recomm-panel{width:" + 728 * pages + "px;display:none;margin:0 !important;overflow:hidden;padding:0 !important;float:left;position:relative;left:0;border:none;clear:both;}"
            // css: .b-recomm-panel.activated
            //+ ".b-recomm-panel.activated{display:block;border:1px solid " + bdcolor + ";}"
            // css: .b-recomm-list
            + cssPrefix + " .b-recomm-list{width:90px;height:728px;padding:0 !important;margin:0 !important;"
            + (pages > 0 ? "float:left;" : "") + "list-style:disc inside none;}"; 
        
        cssStyles += cssPrefix + " .b-recomm-list li{padding:0;display:block;text-align:center;text-indent:0 !important;"
			+ (fontBold ? "font-weight:bold;" : "")
            + "width:98px;height:198px;overflow:hidden;border:1px solid " + bgcolor + ";cursor:pointer;list-style:none !important;float:left;margin:0;}";
            
        // css: .b-recomm-list a
        cssStyles += cssPrefix + " .b-recomm-list a{font-size:" + fontSize + "px;margin:0 !important;list-style:disc inside none !important;position:relative;"
			+ "text-decoration:" + (fontUnderline ? "underline" : "none") + " !important;"
			+ "display:block;clear:both;overflow:hidden;color:" + rtcolor + ";}"
            // css: .b-recomm-list a:hover
            + cssPrefix + " .b-recomm-list a:hover{border-ratio:5px;"
			+ "text-decoration:" + (linkUnderline ? "underline" : "none") + " !important;}";
        if (pages > 0) {
            cssStyles +=  cssPrefix + " .bShareRecommPages{text-align:center;position:absolute !important;width:728px;bottom:8px;}"
	            + cssPrefix + " .bShareRecommPages span{display:inline-block;padding:0;margin:0 6px;cursor:pointer;width:11px;height:11px;background:url(" + w.lezhi.variables.PAGEOFF_PIC_URL + ") no-repeat;}"
	            + cssPrefix + " .bShareRecommPages span.on{background:url(" + w.lezhi.variables.PAGEON_PIC_URL + ") no-repeat;}"
        }

		cssStyles += cssPrefix + " .b-recomm-list a:hover .feed-title{"
			//+ "color:" + hvcolor + ";"
            + "text-decoration:" + (linkUnderline ? "underline" : "none") + " !important;}";
        	// css: .b-recomm-list .feed-title
        cssStyles += cssPrefix + " .b-recomm-list .feed-title{cursor:pointer;overflow:hidden;word-wrap:break-word;word-break:break-all;padding:0 !important;margin:10px !important;font-size:" + fontSize + "px;"
            + "height:" + (lineHeight+1) * 3 + "px;line-height:" + (lineHeight+1) + "px;"
			+ "text-decoration:" + (fontUnderline ? "underline" : "none") + " !important;"
            + "color:" + rtcolor + ";}";

        // css: image box of image widget
		// css: .b-recomm-list .feed-pic
        //cssStyles += ".b-recomm-list .feed-pic{position:relative;left:10px;width:" + picSize + "px;height:" + picSize + "px;background:#fff;margin:5px 0;padding:3px;overflow:hidden;border:1px solid " + bdcolor + ";}"
        // css: .b-recomm-list .feed-pic img  alice remove .b-recomm-list img {left:10px;}
		cssStyles += cssPrefix + " .b-recomm-list .hot-tag{padding:0 !important;margin:0 !important;position:absolute;right:5px;top:23px;width:59px;height:59px;background:url(" + w.lezhi.variables.HOT_PIC_URL + ") no-repeat;z-index:1;}"
            + cssPrefix + " .b-recomm-list img{padding:3px !important;margin:0 !important;position:relative;top:4px;float:none !important;border:none !important;"
			+ (!!picBorderRadius ? "border-radius:5px;" : "")
			+ "}";
			cssStyles += cssPrefix + " #" + lz.variables.ADS_CONTENT_ID + " img{margin:0px !important;padding:0px !important;left:0px !important;top:0px !important;border:0px !important;background:none !important;border-radius: 0px !important;}";
        
        u.loadStyle(cssStyles);
    }
	w.lezhi.render728x90(w.lezhi.variables.recommendation);
})();
