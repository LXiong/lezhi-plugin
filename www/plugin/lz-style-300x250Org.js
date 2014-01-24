(function() {
    var w = window, d = w.document, db = d.body, eu = encodeURIComponent,
        lz = w.lezhi, variables = lz.variables, u = lz.util;

    lz.render300x250 = function(result, isReload) {
        // for each lz-modules, find 300x250 styles
        for (var x = 0; x < lz.modules.nodes.length; x++) {
        	if (lz.modules.nodes[x].lezhiStyle == variables.STYLETYPE_300x250) {
	            var recommDiv = lz.modules.nodes[x], feeds, feedLen, feedsList, insHtml, redirectUrl, p, i = 0,
	                config = lz.modules.config[x], cssPrefix = "." + variables.STYLETYPE_300x250, count = config.count,
	                highlight = config.highlight,
	                pages, pageCounts = 2, currentPageNum, mType, found,
	            
	            adPreload = !!config.adPreload,
	            adEnabled = !!config.adEnabled;
	            //if (result.config && !u.isUndefined(result.config.adEnabled)) {
	            	// server side overrides front-end adEnabled variable
	            	//adEnabled = !!result.config.adEnabled;
	            //}
        	
	            // get id specific config
                if (recommDiv.idConfig) {
                    cssPrefix = "#" + recommDiv.id;
                }
                
	            // only for 300 * 250 config
	            config.source = "insite";
	            variables.adsIds[x] = [];

	            // insert paging or panel Container
	            // set pages
	            // + 1 for ad page
	            // note that if server returns less than count, pages will be shown, but there will be no content!
                pages = Math.ceil(count / pageCounts) + +adEnabled;
                if (pages <= 0) return;
	            impDiv(recommDiv, pages);
            	
	            // just handle feeds
	            for (p in lz.modules.sourceCount[x]) {
	            	for (mType in result) {
		                if (p !== mType) {
		                    continue;
		                }
		                found = true;
		                
		                insHtml = '';
		                i = 0;
		                feeds = result[mType];
		                //if (feeds != "itemcf") continue;
		                feedLen = feeds.length;
		                mType = mType.split(":")[0];		                
		                feedsList = u.createElement("div", "", "b-recomm-panel activated", "display:block");
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
		                		}
		                	} else {
			                    for (var _lengths = (currentPageNum+1 - +adEnabled) * pageCounts; i < _lengths && i < feedLen && i < count; ++i) {
			                        var curFeed = feeds[i], curFeedUrl = curFeed.url, curFeedTitle = curFeed.title,
			                            hvcolor = config.hvcolor, isMark = highlight && curFeed.mark, isAd = curFeed.ad;
			                        if (isAd) {
			                        	// small ads are disabled in 300x250
			                        	/*if (!variables.adsShow) variables.adsShow = true;
		    							//get adEntryId from callback url
		    							adEntryId = curFeed.adEntryId;
		    							if (pages <= 1) variables.adsIds[x].push(adEntryId);
		    							redirectUrl += "&adEntryId=" + adEntryId;*/
		    							continue;
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
		                            
			                        insHtml += '<li onmouseover="this.style.background=\'' + hvcolor + '\'" onmouseout="this.style.background=\'none\'"';
			                        
			    					//if (config.redirectMode == "js") {
			    						insHtml += '><a href="' + curFeedUrl + '" onmousedown="this.href=\''
			    							+ redirectUrl + '\';return true;" onmouseup="t=this;setTimeout(function(){t.href=\''
			    							+ curFeedUrl + '\'},50);return true;" target="' + config.redirectType + '" title="'
			    							+ curFeedTitle + '"';
			    					/*} else if (config.redirectMode == "http") {
			    						insHtml += '><a href="' + curFeedUrl + '" onmousedown="loadModule(\'' + redirectUrl + '\');return true;" target="_blank" title="'
			    							+ curFeedTitle + '"';
			    					}*/
			    						
		                            insHtml += '" hidefocus="true" style="padding:25px 0 !important;">';
		                            if (!(p = curFeed.pic)) { // i want to set p here
		                                p = config.defaultPic;
		                                if (!p) {
                                            p = lz.variables.DEFAULT_PIC_URL;
                                        }
		                            } 
		    						// put img out div because if img and div inner a <a>tag will make the href of tag a does not work // this bug only for ie6
		    						if (isMark) {
		    							insHtml += '<div class="hot-tag"></div>';
		    						}
		    						insHtml += '<img class="bshare-logos" src="' + p + '" style="width:120px !important;height:120px !important;"/>'
		                                + '<div class="feed-title" title="' + u.htmlEncode(curFeedTitle) + '"';
	    							if (isMark) {
	    								insHtml += 'style="color:red;"';
	    							}
	    							insHtml += '>' + curFeedTitle + '</div></a></li>';
			                    }
			                }
		                    insHtml += "</ul>";
		                }
						
		                feedsList.innerHTML = insHtml;
		                if (adElem && scriptEle) {
		                	feedsList.childNodes[0].appendChild(adElem);
		                	// load into doc head:
		                	lz.util.loadModule(scriptEle);
		                }
		                u.getElemByClassName(recommDiv, "div", "bShareRecommDiv3")[0].appendChild(feedsList);
	            	}
	            }
	            
	            if (found) {
	            	if (recommDiv.styleObj) {
		            	// remove previous style object and rewrite it
		            	recommDiv.styleObj.parentNode.removeChild(recommDiv.styleObj);
		            }
		            recommDiv.styleObj = main(x, cssPrefix, pages);

		            if (pages > 1) {
		                // auto paging
		            	if (!isReload) lz.autoPage(x, pages, 2, config.autoPageTime, 0);
		            }
	            }
        	}
        }
	}

	function impDiv(module, pages) {
		var i, insHtml = "", utilVar,
			recommDiv2 = u.createElement("div", "", "bShareRecommDiv2"),
			recommDiv3 = u.createElement("div", "", "bShareRecommDiv3"); 
            
		module.appendChild(recommDiv2);
        recommDiv2.appendChild(recommDiv3);
        if (pages > 1) {
        	utilVar = u.createElement("div", "", "bShareRecommPages");
            for (i = 0; i < pages; i++) {
            	insHtml += '<span onclick="window.lezhi.switchPage(this,' + i + ',2,0)" class="' + (i == 0 ? "on" : "") + '"></span>';
            }
            utilVar.innerHTML = insHtml;
            recommDiv2.appendChild(utilVar);
        }
	}


    function main(index, cssPrefix, pages) {
        // css: #bShareRecommDiv
        var config = lz.modules.config[index], position = config.position, fontSize = config.fontSize, bdcolor = config.bdcolor,
            rtcolor = config.rtcolor, hvcolor = config.hvcolor, lineHeight = config.lineHeight, picBorderRadius = config.picBorderRadius,
			bgcolor = config.bgcolor, fontBold = config.fontBold, fontUnderline = config.fontUnderline, linkUnderline = config.linkUnderline,
            cssStyles = cssPrefix + "{width:300px;height:250px;z-index:99999;overflow:hidden !important;margin:0 !important;padding:0 !important;}"
				+ cssPrefix + " .bShareRecommDiv2{position:relative;margin:0 !important;padding:0 !important;}"
	            + ".lz-module:after,.bShareRecommDiv2:after,.bShareRecommDiv3:after,.b-recomm-panel:after,.b-recomm-tab:after{content:'.';display:block;height:0;clear:both;visibility:hidden;overflow:hidden;}";

        cssStyles += cssPrefix + " .bShareRecommDiv3{overflow:hidden;position:relative;left:0;width:300px;border:0px solid " + (bdcolor ? bdcolor : "#dadada") + ";"
            + (bgcolor ? "background:" + bgcolor + ";" : "") + "}";
        // css: .b-recomm-tab li
        cssStyles += cssPrefix + " .b-recomm-panel{width:" + 300 * pages + "px;display:none;margin:0 !important;overflow:hidden;padding:0 !important;float:left;position:relative;left:0;border:none;";
	    cssStyles += "clear:both;}"
            // css: .b-recomm-panel.activated
            //+ ".b-recomm-panel.activated{display:block;border:1px solid " + bdcolor + ";}"
            // css: .b-recomm-list
            + cssPrefix + " .b-recomm-list{height:250px;width:300px;padding:0 !important;margin:0 !important;"
            + (pages > 0 ? "float:left;" : "") + "list-style:disc inside none;}"; 
        
        cssStyles += cssPrefix + " .b-recomm-list li{padding:0;display:block;text-align:center;text-indent:0 !important;"
			+ (fontBold ? "font-weight:bold;" : "")
            + "width:148px;height:248px;overflow:hidden;border:1px solid " + bgcolor + ";cursor:pointer;list-style:none !important;float:left;margin:0;}";
            
        // css: .b-recomm-list a
        cssStyles += cssPrefix + " .b-recomm-list a{font-size:" + fontSize + "px;margin:0 !important;list-style:disc inside none !important;position:relative;"
			+ "text-decoration:" + (fontUnderline ? "underline" : "none") + " !important;"
			+ "display:block;clear:both;overflow:hidden;color:" + rtcolor + ";}"
            // css: .b-recomm-list a:hover
            + cssPrefix + " .b-recomm-list a:hover{border-ratio:5px;"
			+ "text-decoration:" + (linkUnderline ? "underline" : "none") + " !important;}"
            //+ ".b-recomm-list a:hover .feed-pic{border:1px solid #dadada;}"
			//+ ".b-recomm-list li a:hover{" + (hvcolor ? "color:" + hvcolor : "") + "}"
            // css: miscellaneous
            //+ ".feed-star{margin:0;padding:0;width:16px;height:16px;background:url(" + LEZHI_STATIC_BASE + "/images/star.gif) no-repeat;float:right;position:relative;bottom:5px;bottom:-6px\9;right:5px;visibility:hidden;cursor:pointer;}"
            //+ ".bShareRecommTit{font-size:14px;color:#4a4a4a;font-weight:bold;margin:0 0 20px;padding:0;}"
            + "#bShareRecommTag" + index + "{margin:0;padding:0;background:url(" + LEZHI_STATIC_BASE + "/images/recommend.png) no-repeat;width:40px;height:40px;position:fixed;bottom:60px;*bottom:66px;";
		/*if (config.positionY == "top") {
			cssStyles += "top:10px;bottom:auto;";
		} else if (config.positionY == "center") {
			cssStyles += "top:50%;bottom:auto;";
		} else {
			cssStyles += "bottom:10px;*bottom:16px;top:auto;";
		}*/
		cssStyles += (position == "left" ? "left:10px;right:auto;" : "right:10px;left:auto;") + "z-index:999999;cursor:pointer;}";
        if (pages > 0) {
            cssStyles +=  cssPrefix + " .bShareRecommPages{text-align:center;position:absolute !important;width:300px;bottom:8px;}"
            + cssPrefix + " .bShareRecommPages span{display:inline-block;padding:0;margin:0 6px;cursor:pointer;width:11px;height:11px;background:url(" + w.lezhi.variables.PAGEOFF_PIC_URL + ") no-repeat;}"
            + cssPrefix + " .bShareRecommPages span.on{background:url(" + w.lezhi.variables.PAGEON_PIC_URL + ") no-repeat;}"
        }

		cssStyles += cssPrefix + " .b-recomm-list a:hover .feed-title{"
			//+ (hvcolor ? "color:" + hvcolor + ";" : "")
            + "text-decoration:" + (linkUnderline ? "underline" : "none") + " !important;}";
        	// css: .b-recomm-list .feed-title
        cssStyles += cssPrefix + " .b-recomm-list .feed-title{cursor:pointer;overflow:hidden;word-wrap:break-word;word-break:break-all;padding:0 !important;margin:10px !important;font-size:" + fontSize + "px;"
            + (lineHeight ? "height:" + (lineHeight+1) * 3 + "px;line-height:" + (lineHeight+1) + "px;" : "")
			+ "text-decoration:" + (fontUnderline ? "underline" : "none") + " !important;"
            + (rtcolor ? "color:" + rtcolor + ";" : "")
            +"}";

        // css: image box of image widget
		// css: .b-recomm-list .feed-pic
        //cssStyles += ".b-recomm-list .feed-pic{position:relative;left:10px;width:" + picSize + "px;height:" + picSize + "px;background:#fff;margin:5px 0;padding:3px;overflow:hidden;border:1px solid " + bdcolor + ";}"
        // css: .b-recomm-list .feed-pic img  alice remove .b-recomm-list img {left:10px;}
		cssStyles += cssPrefix + " .b-recomm-list .hot-tag{padding:0 !important;margin:0 !important;position:absolute;right:10px;top:29px;width:59px;height:59px;background:url(" + w.lezhi.variables.HOT_PIC_URL + ") no-repeat;z-index:1;}"
            + cssPrefix + " .b-recomm-list img{padding:3px !important;display:inline;margin:0 !important;position:relative;top:4px;float:none !important;border:none !important;"
			+ (!!picBorderRadius ? "border-radius:5px;" : "")
			+ "}";
		cssStyles += cssPrefix + " #" + lz.variables.ADS_CONTENT_ID + " img{margin:0px !important;padding:0px !important;left:0px !important;top:0px !important;border:0px !important;background:none !important;border-radius: 0px !important;}";
        return u.loadStyle(cssStyles);
    }
	w.lezhi.render300x250(w.lezhi.variables.recommendation);
})();
