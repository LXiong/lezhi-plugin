(function() {
    var w = window, d = w.document, db = d.body, de = d.documentElement, eu = encodeURIComponent, 
        lz = w.lezhi, variables = lz.variables, u = lz.util, maxFeedsLength, feedsLength;
         
    lz.render = function (result, isReload) {
        maxFeedsLength = feedsLength = 0; //clear the feedsLength & maxFeedsLength
		/*
		only for chinadaily ads position
		will be remove later
		*/
		if (w.location.href.indexOf("chinadaily.com.cn") > -1 && lz.config.global.uuid == "a92f8285-36fe-4168-8f86-8e177d015e67" && lz.config.global.col == 4) {
		    var insite = result.insite, feeds = [], ads = [], copy = [], i, length;
		    for (i = 0, length = insite.length; i < length; ++i) {
		        if (!!insite[i].adEntryId) {
		            ads.push(insite[i]);
		        } else {
		            feeds.push(insite[i]);
		        }
		    }
		    if(ads.length == 3){
		    	copy.push(feeds[0]);
			    copy.push(ads[0]);
			    copy.push(feeds[1]);
			    copy.push(feeds[2]);
			    copy.push(ads[1]);
			    copy.push(feeds[3]);
			    copy.push(feeds[4]);
			    copy.push(ads[2]);
			    result.insite = copy;
		    }
		    
		}
		/*
		only for chinadaily ads position
		will be remove later
		*/
        // for each lz-modules, find default styles
        for (var x = 0; x < lz.modules.nodes.length; x++) {
        	if (lz.modules.nodes[x].lezhiStyle == variables.STYLETYPE_DEFAULT) {
	            var mType, recommDiv = lz.modules.nodes[x], feeds, feedLen, feedsList, insHtml, redirectUrl, p, i = 0,
	                config = lz.modules.config[x], col = config.col, row = config.row, pic = config.pic, 
	                count = config.count, highlight = config.highlight, cssPrefix = "." + variables.STYLETYPE_DEFAULT, 
	                tagId = "bShareRecommTag" + u.getIndex(lz.modules.nodes, recommDiv),
	                pages, pageCounts, currentPageNum = 0, addtionPage = 0, allCounts = 0, found;
	            // fix for loading in a div and copying contents to another div...
	            if (recommDiv.oldVersionId) {
	            	recommDiv = u.getElemById(recommDiv.id);
                    recommDiv.oldVersionId = true;
	            	recommDiv.lezhiStyle = variables.STYLETYPE_DEFAULT;
	            	lz.modules.nodes[x] = recommDiv;
	            }
	            // get id specific config and support old versions
                if (recommDiv.idConfig || recommDiv.oldVersionId) {
                    cssPrefix = "#" + recommDiv.id;
                }
                // init the ads id array
	            variables.adsIds[x] = [];
	            
	            // count how much feed in one page
	            pageCounts = col * row > 10 ? 10 : col * row;
	            // insert paging or panel container and set pages
	            // note that if server returns less than count, pages will be shown, but there will be no content!
	            pages = Math.ceil(count / pageCounts);
	            impDiv(recommDiv, x, pages);

	            if (isReload) lz.modules.currentActivated[x] = undefined;
	            	
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
		                feedLen = feeds.length;
		                mType = mType.split(":")[0];
						mType == "insite" ? insiteFeeds = feedLen : trendingFeeds = feedLen;
		                if (feedLen == 0) {
		                    continue;
		                }
		                if (maxFeedsLength < feedLen) {
		                    maxFeedsLength = feedLen;
		                }
		                
		                feedsList = u.createElement("div", "", "b-recomm-panel");
		                feedsList.setAttribute("sourceType", mType);
		                if (u.isUndefined(lz.modules.currentActivated[x])) {
		                    feedsList.style.display = "block";
		                    lz.modules.currentActivated[x] = mType;
		                }
						addtionPage = pages > 1 ? 0 : 1;
		                for (currentPageNum = 0; currentPageNum < pages + addtionPage; currentPageNum++) {
		                	insHtml += '<ul class="b-recomm-list">';
		                    allCounts = pages > 1 ? config.count : col * row;
		                    for (var _lengths = (currentPageNum+1) * pageCounts; i < _lengths && i < feedLen && (i < allCounts || (!pic && i < row)); ++i) {
		                        var curFeed = feeds[i], curFeedUrl = curFeed.url, curFeedTitle = curFeed.title,
		                            hvcolor = config.hvcolor, picSize = config.picSize, isMark = highlight && curFeed.mark, 
		                            isAd = curFeed.ad, adEntryId = "";
		                        
		                        // construct the URL for clicking a recommendation:
	                            redirectUrl = LEZHI_PLUGIN_BASE 
	                                + "/redirect?to=" + eu(curFeedUrl)
	                                + "&from=" + config.url
	                                + "&sitePrefix=" + eu(config.sitePrefix)
	                                + "&ref=" + mType
	                                + "&uuid=" + eu(config.uuid)
	    							+ "&type=" + eu(config.type)
	    							+ "&pic=" + eu(pic)
	                                + "&title=" + eu(config.title);
	    						// ads statistics
	    						if (isAd) {
	    							if (!variables.adsShow) variables.adsShow = true;
	    							//get adEntryId from callback url
	    							adEntryId = curFeed.adEntryId;
	    							if (pages <= 1) variables.adsIds[x].push(adEntryId);
	    							redirectUrl += "&adEntryId=" + adEntryId;
	    						}
		                        insHtml += '<li';
		                        if (isAd) {
		                        	insHtml += ' adEntryId="' + adEntryId + '"';
		                        }
		                        if (pic) {
		                            if (i == feedLen - 1 || i == 9 || (i+1) % col == 0) {
		                                insHtml += ' style="border-right:1px solid #fff;"';
		                            }
		                            insHtml += ' onmouseover="this.style.background=\'' + hvcolor + '\'" onmouseout="this.style.background=\'none\'"';
		                        }
		    					//if(config.redirectMode == "js"){
		    						insHtml += '><a href="' + curFeedUrl + '" onmousedown="this.href=\''
		    							+ redirectUrl + '\';return true;" onmouseup="t=this;setTimeout(function(){t.href=\''
		    							+ curFeedUrl + '\'},50);return true;" target="' + config.redirectType + '" title="'
		    							+ curFeedTitle + '"';
		    					/*}else if(config.redirectMode == "http"){
		    						insHtml += '><a href="' + curFeedUrl + '" onmousedown="loadModule(\'' + redirectUrl + '\');return true;" target="_blank" title="'
		    							+ curFeedTitle + '"';
		    					}*/
		                        
		                        if (pic) {
		                            insHtml += '" hidefocus="true">';
		                            
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
		    						insHtml += '<img class="bshare-logos" src="' + p + '" />'
		                                + '<div class="feed-title" title="' + u.htmlEncode(curFeedTitle) + '"';
	    							if (isMark) {
	    								insHtml += 'style="color:red;"';
	    							}
	    							insHtml += '>' + curFeedTitle + '</div>';
		                        } else {
		    						if (isMark) {
		    						    insHtml += 'style="color:red;"';
		    						}
		    						insHtml += '>' + curFeedTitle;
		                        }
		                        insHtml += '</a></li>';
		                    }
		                    if (pages > 1) {
                                insHtml += "</ul>";
                            } else {
                                if(config.showLogo){
                                    insHtml += '</ul><div class="b-recomm-footer"><a href="http://www.lezhi.me" target="_blank">乐知</a></div>';
                                }else{
                                    insHtml += '</ul>';
                                }
                            }
		                }
		                feedsList.innerHTML = insHtml;
		                impFeedsContent(recommDiv, feedsList, x, pages);
		                feedsLength++;
	            	}
	            }
	            
	            if (recommDiv.styleObj) {
	            	// remove previous style object and rewrite it
	            	recommDiv.styleObj.parentNode.removeChild(recommDiv.styleObj);
	            }
	            recommDiv.styleObj = main(x, cssPrefix, tagId, pages);
	            
	            implbShareFeeds(recommDiv, x, pages);
	            resizeWidget(recommDiv, x, pages);
	            initRecommType(recommDiv, x, tagId);
	            if (pages > 1 && found) {
                    impFooter(recommDiv, x);
	                // auto paging
					if (!isReload) lz.autoPage(x, pages, col, config.autoPageTime);
	            }
        	}
        }
	}

	function impDiv(module, index, pages) {
		var i, insHtml = "", utilVar;
		    recommDiv2 = u.createElement("div", module.oldVersionId ? "bShareRecommDiv2" : "", "bShareRecommDiv2");
        module.appendChild(recommDiv2);
        if (pages > 1) {
            recommDiv3 = u.createElement("div", "", "bShareRecommDiv3"); 
            recommDiv2.appendChild(recommDiv3);
        }
        if (pages > 1) {
        	utilVar = u.createElement("div", "", "bShareRecommPages");
            for (i = 0; i < pages; i++) {
                insHtml += '<span onclick="window.lezhi.switchPage(this,' + i + ',' + lz.modules.config[index].col + ')" class="' + (i == 0 ? "on" : "") + '"></span>';
            }
            utilVar.innerHTML = insHtml;
            recommDiv2.appendChild(utilVar);
        }
	}

    function impFooter(recommDiv, index) {
        var config = lz.modules.config[index], insHtml = '';
            if (config.brand) {
                insHtml = '<div class="b-recomm-footer"><span>' + config.brand + '</span></div>';
            } else if (config.showLogo) {
                insHtml = '<div class="b-recomm-footer"><a href="http://www.lezhi.me" target="_blank">乐知</a></div>';
            }
            u.getElemByClassName(recommDiv, "div", "bShareRecommDiv3")[0].innerHTML += insHtml;
    }

    function impFeedsContent(recommDiv, feedNode, index, pages) {
        var recommDiv2 = pages > 1 ? u.getElemByClassName(recommDiv, "div", "bShareRecommDiv3")[0] : u.getElemByClassName(recommDiv, "div", "bShareRecommDiv2")[0];
        recommDiv2.appendChild(feedNode);
    }

    function implbShareFeeds(recommDiv, index, pages) {
        var config = lz.modules.config[index], recommTab = u.getElemByClassName(recommDiv, "div", "b-recomm-tab")[0], 
            recommDiv2 = u.getElemByClassName(recommDiv, "div", "bShareRecommDiv2")[0], insHtml = "", i, t,
            nodes = u.getElemByClassName(recommDiv, "div", "b-recomm-panel"), nodesLen = nodes.length,
            name, elem, currentActivated = lz.modules.currentActivated[index];

        if (!recommTab) {
        	recommTab = u.createElement("ul", recommDiv.oldVersionId ? "bShareRecommTab" : "", "b-recomm-tab");
            recommDiv2.insertBefore(recommTab, recommDiv2.firstChild);
        }

        for (i = 0; i < nodesLen; ++i) {
            t = nodes[i].getAttribute("sourceType");
            name = variables.BSHARE_RECOMM_SOURCES_NAMES[t];
            insHtml += '<li ' + (currentActivated === t ? 'class="activated" ' : "") 
                + 'onclick="window.lezhi.switchTo(this,\'' + nodes[i].getAttribute("sourceType") + '\',\'' + lz.modules.config[index].type + '\',' + pages + ',' + index + ')" style="text-indent:0;">';
			insHtml += t == "insite" ? config.promote : name;
			insHtml += '</li>';
            if (currentActivated == t) {
                // activate the content node:
            	u.addClass(nodes[i], "activated");
            }
			if (i == 0 && config.type == "fixed") {
				lz.stats.view(t, index);
				if (pages <= 1 && variables.adsShow) {
					lz.stats.adsView(index, variables.adsIds[index]);
				}
			}
        }
        
        if (nodesLen == 0) {
            recommDiv.style.display = "none";
            return;
        } 
        
        recommTab.style.visibility = nodes.length == 1 && config.type == "slide" && nodes[0].getAttribute("sourceType") == "insite" ? "hidden" : "visible";
        recommTab.innerHTML = insHtml;
    }

    function resizePanelWidget(recommDiv, wid) {
        var _panels = u.getElemByClassName(recommDiv, "div", "b-recomm-panel"), _i;
        for (_i = 0; _i < _panels.length; _i++) {
            _panels[_i].style.width = wid + "px";
        }
    }

    function resizeWidget(recommDiv, index, pages) {  
         var config = lz.modules.config[index], _wid = config.picSize + 30, _padding = 10, col = config.col, 
            pic = config.pic, _slideTag = 0, recommDiv2 = u.getElemByClassName(recommDiv, "div", "bShareRecommDiv2")[0], 
            titleOffsetWidth = config.source == "insite,trending" ? 114 : 35,
            minWidth = (config.titleBold ? titleOffsetWidth + 8 : titleOffsetWidth) + config.titleFontSize * config.promote.length,
            _slideWid = recommDiv.style.width ? parseInt(recommDiv.style.width,10) : config.width != 0 ? config.width : config.pic ? (_wid * col) > minWidth ? _wid * col : minWidth : 600,
            _height = config.height, _surpassHeight = 0, _borderWidth = 2, hotTag = u.getElemByClassName(recommDiv, "div", "hot-tag");
        if(pages > 1){
            var recommDiv3 = u.getElemByClassName(recommDiv, "div", "bShareRecommDiv3")[0];
            _borderWidth = 4;
            recommDiv3.style.width=_slideWid + _slideTag - 2 + "px";
        }
        
        if (pic) {
			if (config.type == "slide") _slideTag = 25;
            //recommDiv.style.width = recommDiv2.style.width = (col + colPad) * _wid + _padding + _slideTag + "px";
			recommDiv.style.width = _slideWid + _slideTag + "px";
        } else {
			if (config.type == "slide") {
				_slideTag  = 25;
			} 
            recommDiv.style.width = recommDiv2.style.width = _slideWid + _slideTag + "px";
		    resizePanelWidget(recommDiv, _slideWid-_borderWidth);
        }
        
        var recommPanel = [],
            panels = u.getElemsByTagName(recommDiv, "div"),
            i, j, panel,
            recommDivWidth = parseInt(recommDiv.style.width,10), // if this does not work in ie6, try clientWidth
            customWidth, feedLists, feedList;
        for (i = 0; i < panels.length; ++i) {
            panel = panels[i];
            if (u.lzhasClass(panel, "b-recomm-panel")) {
                recommPanel.push(panel);
            }
        }
        if (recommPanel.length == 0) {
            return;
        }
        for (j = 0; j < recommPanel.length; ++j) {
            if (pages > 1) {
                recommPanel[j].style.width = (recommDiv2.offsetWidth - _slideTag) * pages + "px";
                pageLists = u.getElemsByTagName(recommPanel[j], "ul");
                for (i = 0; i < pageLists.length; ++i) {
                    pageLists[i].style.width = (recommDiv2.offsetWidth - _slideTag - 8) + "px";
                }
            } else {
                if (config.pic) {
                	recommPanel[j].style.width = (recommDiv.offsetWidth - _slideTag - 2) < 0 ? "" : recommDiv.offsetWidth - _slideTag - 2 + "px";
                }
            }
            
            customWidth = config.pic ? 
                // fixed column width for image widget, calculate margin length of image widget
                "auto" :
                // fixed margin length for text widget, calculate column width of text widget
                recommDivWidth - 2;
            customWidth = config.type == 'fixed' ? customWidth : customWidth - 30;
            feedLists = u.getElemsByTagName(recommPanel[j], "li");
            
            for (i = 0; i < feedLists.length; ++i) {
                feedList = feedLists[i];
                if (!config.pic) {
                    feedList.style.width = (config.col == 1 ? customWidth - (i == (feedLists.length - 1) ? 50 : 20) : customWidth/2 - 25) + "px";
                    feedList.style.textAlign = "left";
                } else {
                    //if(config.type == "slide"){
                        //_slideTag = sFlag == false ? 25 : 0;
                    //} 
                    feedList.style.width = ((_slideWid - 0 - _padding)/col) - 2 + "px";
                } 
            } 
            for (i = 0; i < hotTag.length; i++) {
                hotTag[i].style.right = (((_slideWid - 0 - _padding)/col)-2) / 2 - config.picSize/2 + "px";
            }
            // limit the min height is plugin DIV base height & if user set height > min height then reset the plugin DIV style height value 
            // compute the surpass height
            if (j == 0) {
                recommDivHeight = pages > 1 ? recommDiv.clientHeight + 18 : recommDiv.clientHeight;
            }
            if (_height > recommDivHeight) {
                _surpassHeight = _height - +recommDivHeight;
                //recommDiv.style.height = _height + "px";
                var surpassMarginTopBottom = ~~(_surpassHeight/config.row)/2;
                for (i = 0; i < feedLists.length; ++i) {
                    if (feedLists && feedLists[i]) {
                    	feedLists[i].style.paddingTop = feedLists[i].style.paddingBottom = surpassMarginTopBottom + "px";
                    }
                }
            }
        }
    }

    function initRecommType(recommDiv, index, tagId) {                  
        var config = lz.modules.config[index], recommType = config.type, slideTag, 
        recommClassName = "b-" + recommType, recommDiv2 = u.getElemByClassName(recommDiv, "div", "bShareRecommDiv2")[0], 
        lock = false, tag = "&gt;&gt;";
        if (config.position == "left") tag = "&lt;&lt;";
        if (recommType == "slide") {
            slideTag = u.createElement("div", "", "recommSlideTag", "", "<div>" + tag + "</div>" + "乐知推荐");
            recommDiv2.insertBefore(slideTag, recommDiv2.childNodes[0]);
        }

        if (u.lzhasClass(recommDiv, recommClassName)) return; 

        //recommDiv.className = recommClassName;
        u.addClass(recommDiv, recommClassName);
        var position, offset, out = config.out, curPosition, recommTag, slide,
            innerHeight, offsetHeight, overHeight, isShowUp;
        
        if (recommType == "slide") {
            position = config.position;
            //recommDiv.className = "b-" + recommType + " " + "b-" + position;
            u.addClass(recommDiv, "b-" + recommType + " " + "b-" + position);
            offset = -recommDiv.offsetWidth;
			if (position == "left") {
				recommDiv2.style.left = offset + "px";
				recommDiv.style.left = "0";
			} else {
				recommDiv2.style.right = offset + "px";
				recommDiv.style.right = "0";
			} 
            out = true;
			isShowUp = false;
			
            curPosition = offset;
            
            slide = function() {
				//remove the slide animate for ie6/7 beacuse the plugin slide out form right has scroll-x bar in ie6
				/*if (isIE6o7){
					out ? recommDiv.style.display = "block" : recommDiv.style.display = "none"
					if (position === "left") recommDiv.style.left = "0";
                else recommDiv.style.right = "0";
					if (!out) recommTagToggle(out); 
					return;
				} */
				if (out && !isShowUp) {
					if (insiteFeeds > 0) {
						//showUp("insite");
                        lz.stats.view("insite",index);
                        if (variables.adsShow) lz.stats.adsView(index, variables.adsIds[index]);
					} else if (trendingFeeds > 0) {
						//showUp("trending");
                        lz.stats.view("trending",index);
					}
					isShowUp = true;
				}
                var targetPosition = out ? 0 : -recommDiv.offsetWidth,
                    movePixels = 40, timeIter = 1;
				recommDiv.style.zIndex = out ? "99999999" : "-1";
                if (curPosition == targetPosition) {
                    if (!out) recommTagToggle(tagId, out); 
                    return;
                }     
                curPosition = out ? curPosition + movePixels : curPosition - movePixels;
                if ((out && curPosition > targetPosition) || (!out && curPosition < targetPosition)) {
                    curPosition = targetPosition;
                }
                if (position == "left") recommDiv2.style.left = curPosition + "px";
                else recommDiv2.style.right = curPosition + "px";
                setTimeout(slide, timeIter);
            };
            
            // get page innerHeight
            innerHeight = w.innerHeight ? w.innerHeight : (db.clientHeight || de.clientHeight);
            // get page totalHeight
            offsetHeight = db.scrollHeight || de.scrollHeight;
            // set scroll offset top distance
            //offsetTop = d.all ? 50 : 20;
            overHeight = offsetHeight - innerHeight;
            
            u.ready(function() {
                recommTag = u.createElement("div", tagId, "bShareRecommTag");
				if (feedsLength > 0) {
					if (!u.getElemById(tagId)) db.appendChild(recommTag);
				}
                w.onscroll = function() {
                    var scrollTop = de.scrollTop || db.scrollTop, recSlTag = u.getElemByClassName(recommDiv, "div", "recommSlideTag")[0];
					if (scrollTop == 0) lock = false;
					if (!lock) {
						if (out = (overHeight / 2 <= scrollTop && scrollTop != 0)) { // i want to set out here
							recommTagToggle(tagId,out);
						}
						slide();
						recSlTag.style.display = out ? "block" : "none";
					}
                };
                if (u.getElemById(tagId)) {
                	u.getElemById(tagId).onclick = function() {
                        var scrollTop = de.scrollTop || db.scrollTop, recSlTag = u.getElemByClassName(recommDiv, "div", "recommSlideTag")[0];
                        out = true;
                        lock = true;
                        recommTagToggle(this.id);
                        slide();
                        this.style.display = "none";
                        if (recSlTag) recSlTag.style.display = "block";
                    };
                }
                u.getElemByClassName(recommDiv, "div", "recommSlideTag")[0].onclick = function() {
                    out = false;
					lock = true;
                    //recommTagToggle();
                    slide();
                    this.style.display = "none";
                };
            });
        }
    }

    function recommTagToggle(tagId, pOut) {
    	u.getElemById(tagId).style.display = pOut ? "none" : "block";
    }

    function main(index, cssPrefix, tagId, pages) {
        // css: #bShareRecommDiv
        var config = lz.modules.config[index], pic = config.pic, picSize = config.picSize, position = config.position, fontSize = config.fontSize, type = config.type, promote = config.promote,
            rtcolor = config.rtcolor, hvcolor = config.hvcolor, htcolor = config.htcolor, bdcolor = config.bdcolor, titleBgColor = config.titleBgColor, titleImage = config.titleImage, 
			titleFontSize = config.titleFontSize, lineHeight = config.lineHeight, listType = config.listType, picBorderRadius = config.picBorderRadius,
			bgcolor = config.bgcolor, titleBold = config.titleBold, fontBold = config.fontBold, fontUnderline = config.fontUnderline, linkUnderline = config.linkUnderline, convPosition = position == "left" ? "right" : "left",
            cssStyles = cssPrefix + "{z-index:" + (type == "slide" ? 999999 : 99999) + ";overflow:hidden !important;margin:0 !important;padding:0 !important;}"
			+ cssPrefix + " .bShareRecommDiv2{position:relative;margin:0 !important;padding:0 !important;}"
            + ".lz-module:after,.bShareRecommDiv2:after,.bShareRecommDiv3:after,.b-recomm-panel:after,.b-recomm-tab:after,.b-recomm-list:after{content:'.';display:block;height:0;clear:both;visibility:hidden;overflow:hidden;}"
            // css: .b-recomm-tab
            + cssPrefix + " .b-recomm-tab{width:100%;list-style:none !important;padding:0 !important;margin:0 !important;position:relative;z-index:999;"
		if (type == "slide") {
		    cssStyles += (position == "left" ? "left:0;":"left:25px;");
		} else {
			cssStyles += "left:0;";
		}
		cssStyles += "}";
        if (pages > 1) {
            cssStyles += cssPrefix + " .bShareRecommDiv3{overflow:hidden;margin:0;padding:0;position:relative;border:1px solid " + (bdcolor ? bdcolor : "#dadada") + ";"
                + (bgcolor ? "background:" + bgcolor + ";" : "transparent;")
            if (type == "slide" && position == "right") {
               cssStyles += "left:25px;";
            } else {
                cssStyles += "left:0;";
            }
            if (!!lz.util.isIE6 && bdcolor == "transparent") {
                cssStyles += "border:none;";
            }
            cssStyles += "}";
        }
        
        // css: .b-recomm-tab li
        cssStyles += cssPrefix + " .b-recomm-tab li{width:auto;list-style:none !important;height:15px;line-height:15px;float:left;"
            + "cursor:pointer;margin:0 5px 0 0 !important;padding:5px 10px !important;text-align:center;border:1px solid " + bdcolor 
            + ";border-width:1px 1px 0;position:relative;top:1px;z-index:100;font-size:" + titleFontSize + "px;";
            if (!!lz.util.isIE6 && bdcolor == "transparent") {
                cssStyles += "border:none;";
            }
            cssStyles += (titleBgColor ? "background-color:" + titleBgColor + ";" : "")
			+ (titleImage ? "background:url('" + titleImage + "') 3px center no-repeat;padding-left:20px !important;" : "")
			+ (htcolor ? "color:" + htcolor : "") + "}"
            // css: .b-recomm-tab li.activated
            + cssPrefix + " .b-recomm-tab li.activated{cursor:default;background-color:#fff;border-top-width:2px;"
			+ (titleBold ? "font-weight:bold;" : "")
            + "}"
            // css: .b-recomm-panel
            + cssPrefix + " .b-recomm-panel{display:none;margin:0 !important;overflow:hidden;padding:5px 0 0 !important;float:left;position:relative;left:0;";
        if (pages <= 1) {
            cssStyles += "border:1px solid " + (bdcolor ? bdcolor : "#dadada") + ";";
            if (!!lz.util.isIE6 && bdcolor == "transparent") {
                cssStyles += "border:none;";
            }
        } else {
            cssStyles += "border:none;";
        }
        if(type == "slide"){
           cssStyles += (position == "left" ? "left:0;": pages > 1 ? "left:0;" : "left:25px;");
        } else {
            cssStyles += "left:0;";
        }
        cssStyles += (bgcolor ? "background:" + bgcolor + ";" : "background-color:#fff;")
	    cssStyles += "clear:both;}"
            // css: .b-recomm-panel.activated
            //+ ".b-recomm-panel.activated{display:block;border:1px solid " + bdcolor + ";}"
            // css: .b-recomm-list
            + cssPrefix + " .b-recomm-list{height:auto;float:left;";
        if (!pic && type == 'slide' && 3 >= config.row) {
            cssStyles += "height:68px;";
        } 
        cssStyles += "padding:0 4px !important;margin:0 !important;list-style:disc inside none;}"
            // css: .b-recomm-list li  alice changed text-align:left!important; become text-align:center
            + cssPrefix + " .b-recomm-list li{padding:0;display:block;vertical-align:top;text-align:center;text-indent:0 !important;"
			+ (fontBold ? "font-weight:bold;" : "")
            + (pic ? "width:" + (picSize + 28) + "px;height:auto;border:1px solid #fff;cursor:pointer;border-right-color:#f2f2f2;list-style:none !important;"
                    : "border:0;padding-left:10px !important;text-align:left;") + "overflow:hidden;margin:0;float:left;list-style: inside !important;}"
            
        // css: .b-recomm-list a
        cssStyles += cssPrefix + " .b-recomm-list a{font-size:" + fontSize + "px;margin:0 !important;position:relative;"
			+ "text-decoration:" + (fontUnderline ? "underline" : "none") + " !important;"
			+ "display:" + (pic ? "block;height:100%;overflow:visible;clear:both;" : "list-item;zoom:1;height:24px;line-height:24px;"
			+ (listType ? "list-style-type:" + listType + " !important;" : "")
			+ "overflow:hidden;color:" + rtcolor) + ";}"
            // css: .b-recomm-list a:hover
            + cssPrefix + " .b-recomm-list a:hover{border-ratio:5px;"
			+ "text-decoration:" + (linkUnderline ? "underline" : "none") + " !important;"
            + (pic ? "" : hvcolor ? "color:" + hvcolor : "") + "}"
            //+ ".b-recomm-list a:hover .feed-pic{border:1px solid #dadada;}"
			//+ ".b-recomm-list li a:hover{" + (hvcolor ? "color:" + hvcolor : "") + "}"
            // css: miscellaneous
            //+ ".feed-star{margin:0;padding:0;width:16px;height:16px;background:url(" + LEZHI_STATIC_BASE + "/images/star.gif) no-repeat;float:right;position:relative;bottom:5px;bottom:-6px\9;right:5px;visibility:hidden;cursor:pointer;}"
            //+ ".bShareRecommTit{font-size:14px;color:#4a4a4a;font-weight:bold;margin:0 0 20px;padding:0;}"
            + "#" + tagId + "{margin:0;padding:0;background:url(" + LEZHI_STATIC_BASE + "/images/recommend.png) no-repeat;width:40px;height:40px;position:fixed;bottom:60px;*bottom:66px;";
		/*if (config.positionY == "top") {
			cssStyles += "top:10px;bottom:auto;";
		}else if (config.positionY == "center") {
			cssStyles += "top:50%;bottom:auto;";
		}else{
			cssStyles += "bottom:10px;*bottom:16px;top:auto;";
		}*/
		cssStyles += (position == "left" ? "left:10px;right:auto;" : "right:10px;left:auto;") + "z-index:999999;cursor:pointer;}";
        if (pages > 1) {
            cssStyles += cssPrefix + " .bShareRecommPages{text-align:center;position:absolute !important;width:100%;bottom:5px;}"
            + cssPrefix + " .bShareRecommPages span{display:inline-block;padding:0;margin:0 6px;line-height:25px;cursor:pointer;width:11px;height:11px;background:url(" + w.lezhi.variables.PAGEOFF_PIC_URL + ") no-repeat;}"
            + cssPrefix + " .bShareRecommPages span.on{background:url(" + w.lezhi.variables.PAGEON_PIC_URL + ") no-repeat;}"
        }
        if (u.isIE6) {
			cssStyles += "*html #" + tagId + "{position:absolute;z-index:999999;bottom:auto;top:expression(eval(-70+document.documentElement.scrollTop+document.documentElement.clientHeight-this.offsetHeight-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
			/*if (config.positionY == "top") {
				cssStyles += "expression(eval(10+document.documentElement.scrollTop));}";
			} else if (config.positionY == "center") {
				cssStyles += "expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight/2-this.offsetHeight/2-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
			} else {
				cssStyles += "expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight-this.offsetHeight-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
			}*/
		}
		cssStyles += cssPrefix + " .b-recomm-list a:hover .feed-title{"
			//+ (hvcolor ? "color:" + hvcolor + ";" : "")
            + "text-decoration:" + (linkUnderline ? "underline" : "none") + " !important;}"
        	// css: .b-recomm-list .feed-title
            cssStyles += cssPrefix + " .b-recomm-list .feed-title{cursor:pointer;overflow:hidden;word-wrap:break-word;word-break:break-all;padding:0 !important;margin:10px !important;font-size:" + fontSize + "px;"
            + (lineHeight ? "height:" + (lineHeight+1) * 3 + "px;line-height:" + (lineHeight+1) + "px;" : "")
			+ "text-decoration" + (fontUnderline ? "underline" : "none") + " !important;"
            + (rtcolor ? "color:" + rtcolor + ";" : "")
            + "}";

        // css: image box of image widget
        if (pic) {
			// css: .b-recomm-list .feed-pic
            //cssStyles += ".b-recomm-list .feed-pic{position:relative;left:10px;width:" + picSize + "px;height:" + picSize + "px;background:#fff;margin:5px 0;padding:3px;overflow:hidden;border:1px solid " + bdcolor + ";}"
            // css: .b-recomm-list .feed-pic img  alice remove .b-recomm-list img {left:10px;}
			cssStyles += cssPrefix + " .b-recomm-list .hot-tag{padding:0 !important;margin:0 !important;position:absolute;right:10px;top:4px;width:59px;height:59px;background:url(" + w.lezhi.variables.HOT_PIC_URL + ") no-repeat;z-index:1;}"
                + cssPrefix + " .b-recomm-list img{padding:3px !important;display:inline;margin:0 !important;position:relative;top:4px;border:1px solid " + bdcolor + ";float:none !important;width:" + picSize + "px !important;height:" + picSize + "px !important;"
				+ (!!picBorderRadius ? "border-radius:5px;" : "")
                if (!!lz.util.isIE6 && bdcolor == "transparent") {
                    cssStyles += "border:none;";
                }
				cssStyles += "}";

        }
        // css: .b-recomm-footer a
        cssStyles += cssPrefix + " .b-recomm-footer{overflow:hidden;margin:2px 0 0 !important;text-align:right;height:18px;width:100%;}"
            + cssPrefix + " .b-recomm-footer a{font-size:12px;color:#999;text-decoration:none;height:15px !important;line-height:15px !important;position:relative;";
        cssStyles += (config.position == "left" && type == "slide")  ? "left:3px;float:left;}" : "right:3px;float:right;}";
        
        // TYPE: Slide
        cssStyles += cssPrefix + ".b-slide{position:fixed;bottom:10px;top:auto;margin-top:0 !important;}";
		/*if (config.positionY == "top") {
			cssStyles += "top:0;bottom:auto;margin-top:0 !important;";
		} else if (config.positionY == "center") {
			cssStyles += "top:50%;bottom:auto;margin-top:" + getElemById("bShareRecommDiv2").clientHeight/2/-1 + "px !important;";
		} else {
			cssStyles += "bottom:10px;top:auto;margin-top:0 !important;";
		}
		cssStyles += "}";*/
        if (u.isIE6) {
			cssStyles += "*html " + cssPrefix + ".b-slide{position:absolute;bottom:auto;top:expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight-this.offsetHeight-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
			/*if (config.positionY == "top") {
				cssStyles += "expression(eval(document.documentElement.scrollTop));}";
			} else if(config.positionY == "center") {
				cssStyles += "expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight/2-this.offsetHeight/2-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
			} else {
				cssStyles += "expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight-this.offsetHeight-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
			}*/
		} 
        cssStyles += cssPrefix + ".b-slide.b-left{left:0;}"
            + cssPrefix + ".b-slide.b-right{right:0 !important;}"
            + cssPrefix + ".b-slide.b-right .b-recomm-tab li{float:left;}"
			+ cssPrefix + ".b-slide.b-" + position + " .recommSlideTag{display:none;margin:0 !important;padding:0 0 4px !important;font-size:12px;border:1px solid #c2c2c2;position:absolute;width:23px;" + convPosition + ":1px;top:27px;cursor:pointer;text-align:center;line-height:17px;color:#444;"
            + "background:-webkit-gradient(linear,0% 20%,0% 90%,from(#f4f4f4),to(#d7d7d7));background:-moz-linear-gradient(top,#f4f4f4,#d7d7d7);"
            + "filter:progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr='#f4f4f4',EndColorStr='#d7d7d7');_background:#e4e4e4;}";
        
        return u.loadStyle(cssStyles);
    }
	w.lezhi.render(w.lezhi.variables.recommendation);
})();
