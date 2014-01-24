(function() {
    var w = window, d = w.document, db = d.body, de = d.documentElement, eu = encodeURIComponent, du = decodeURIComponent,
        
        BSHARE_FEEDS_BASE = "http://api.bshare.cn",
        // Please don't change this to production API!! This is for local dev!!
        LEZHI_PLUGIN_BASE = "http://lz.bshare.local:8098/plugin",
        LEZHI_STATIC_BASE = "http://lz.bshare.local:8099/assets",
        DEFAULT_PIC_URL = LEZHI_STATIC_BASE + "/plugin/img/default.gif",
		
        BSHARE_FEEDS_LINK = BSHARE_FEEDS_BASE + "/feeds/trending.json?callback=window.lezhi.callback",
        LEZHI_PLUGIN_LINK = LEZHI_PLUGIN_BASE + "/feeds?callback=window.lezhi.callback",
		LEZHI_CORRECT_LINK = LEZHI_PLUGIN_BASE + "/correct?",
        BSHARE_RECOMM_TYPES = ["fixed", "slide"],
        BSHARE_RECOMM_POSITIONS = ["left", "right"],
		BSHARE_RECOMM_POSITIONYS = ["bottom"],
        BSHARE_RECOMM_SOURCES = ["insite", "trending"],
        BSHARE_RECOMM_SOURCES_NAMES = {
            "trending": "最热文章",
            "insite": "您可能也喜欢"
        },
        BSHARE_FEEDS_OPTIONS = {},
        // interval vars:
        config = {}, 
        lezhiLoad = false, 
        lezhiLoadTimeout = 8000,
		insiteFeeds = 0,
		trendingFeeds = 0,
        feedsLength = 0,
        maxCounts = 12,
        currentActivated = "",
		trendingShow = false,
		pages = 0,
		pageCounts = 0,
		currentPage = 0,
		autoPageTime = 3000,
		referrer = d.referrer,
        maxFeedsLength = 0,
		openAds = false,
		bx = false,
		vid = '',
		adsIds = [];
		w.bshare_recomm_config = w.bshare_recomm_config || {};
    lz = w.lezhi = {
        lezhi_ok: true,
        init: function() {
            currentActivated = "";
            
            // these are the default settings:
            BSHARE_FEEDS_OPTIONS = {
                uuid: "",
                promote: "您可能也喜欢",
                col: 5,
                row: 1,
                pic: true, // true for pic mode, false for textMode
                type: "fixed", // BSHARE_RECOMM_TYPES
                picSize: 88, // pic size for picMode only in pixels
                fontSize: 12,  //font-size for textMode only in pixels
                sitePrefix: "",
                defaultPic: DEFAULT_PIC_URL,
                position: "right", // BSHARE_RECOMM_POSITIONS
				//positionY: "bottom", // BSHARE_RECOMM_POSITIONYS
                source: "insite", // BSHARE_RECOMM_SOURCES
                // TODO what if someone changes the URL? we need to handle the title and hash.
                url: eu(d.location.href),
                title: d.title,
                hash: du(w.location.hash.substring(1)),
				htcolor: "#333", // heading text color
				rtcolor: "#333", // recomm text color
				bdcolor: "#DADADA", // border color 
				hvcolor: "#FBFBEF", // border color on mouse over 
				showLogo: true, 
				brand: "", 
				debug: false, 
				mock: false,		//callback default feeds
				thumbnail: "",	//the right imageUrl from the page
				customTitle: "" 
			}; 
				if (!referrer) {
					try {
						if (window.opener) {
					 // ie will throw error while cross domain
						referrer = window.opener.location.href;
						}
					}catch (e) {}
				}
				config = mergeConfig(BSHARE_FEEDS_OPTIONS, getRcommConfig()); // merge javascript parameter options first 
				config = mergeConfig(config, w.bshare_recomm_config); // then merge javascript options 
				
				var lezhiParam = [ 
					"uuid=" + config.uuid, 
					"url=" + config.url, 
					"title=" + eu(config.title), 
					"sitePrefix=" + eu(config.sitePrefix), 
					"count=" + maxCounts, 
					"type=" + config.source, 
					"hash=" + config.hash, 
					"keywords=" + getKeywords(),
					"referrer=" + referrer,
					"mock=" + config.mock ]; 
				if(config.customTitle !== ''){
					lezhiParam.push("customTitle=" + config.customTitle);
				}
				if(config.thumbnail !== ''){
					lezhiParam.push("thumbnail=" + config.thumbnail);
				}
				impDiv(getElemById("bShareRecommDiv")); 
				loadModule(LEZHI_PLUGIN_LINK + "&" + lezhiParam.join("&"));
            
            // set timeout to see if our servers are running
            /*setTimeout(function() {
                if (!lezhiLoad) {
                    // if servers are not reachable, set down variable and manually call callback.
                    lz.lezhi_ok = false;                
                    //lz.callback(null);
                }
            }, lezhiLoadTimeout);*/
        },
        callback: function (result) {
            lezhiLoad = true;
            var recommDiv = getElemById("bShareRecommDiv"), mType, i=0, feeds, feedLen, feedsList, insHtml='', redirectUrl, p, currentPageNum, 
                col, pic, brand, i, bestImage;
			// compare the servers callback imageUrl was correct
			bestImage = matchImage();
			if(result.thumbnail !== bestImage && bestImage !== '' && config.thumbnail === ''){
				var correctParam = [ 
					"url=" + config.url, 
					"thumbnail=" + eu(bestImage) ];
				loadModule(LEZHI_CORRECT_LINK + "&" + correctParam.join("&"));
			}
            if (result && !isUndefined(result.config)) {
                config = mergeConfig(config, result.config);
            }
            config = mergeConfig(config, w.bshare_recomm_config);
			pages = Math.ceil(maxCounts / pageCounts);
			col = config.col;
			pic = config.pic;
			config.picSize = config.picSize > 120 ? 120 : config.picSize;
            if (col * config.row > 100) {
                config.row = parseInt(100 / col, 10);
            }
            maxFeedsLength = feedsLength = 0;//clear the feedsLength & maxFeedsLength

            for (mType in result) {
                // just handle feeds
                if (!arrayContains(BSHARE_RECOMM_SOURCES, mType)) {
                    continue;
                }
                insHtml='';
				i = 0;
                feeds = result[mType];
                feedLen = feeds.length;
				mType == "insite" ? insiteFeeds = feedLen : trendingFeeds = feedLen;
                if (feedLen === 0) {
                    continue;
                }
                if (maxFeedsLength < feedLen) {
                    maxFeedsLength = feedLen;
                }
                
                feedsList = createElement("div", "", "b-recomm-panel");
                feedsList.setAttribute("sourceType",mType);
                if (currentActivated === "") {
                    feedsList.style.display = "block";
                    currentActivated = mType;
                }
				for(currentPageNum = 1; currentPageNum < pages+1; currentPageNum++){
					insHtml += '<ul class="b-recomm-list">';

					for (i; i < currentPageNum * pageCounts && i < feedLen && (i < maxCounts || (!pic && i < config.row)); ++i) {
						var curFeed = feeds[i], curFeedUrl = curFeed.url, curFeedTitle = curFeed.title,
							hvcolor = config.hvcolor, picSize = config.picSize, isAd = curFeed.ad == true, adEntryId = "";
						
						if (lz.lezhi_ok) {
							// show original at browser hint, but use our redirect page when click
							redirectUrl = LEZHI_PLUGIN_BASE 
								+ "/redirect?to=" + eu(curFeedUrl)
								+ "&from=" + eu(config.url)
								+ "&sitePrefix=" + eu(config.sitePrefix)
								+ "&ref=" + mType
								+ "&uuid=" + eu(config.uuid)
								+ "&type=" + eu(config.type)
								+ "&pic=" + eu(config.pic)
								+ "&title=" + eu(config.title);
								if(isAd){
									if(!openAds) openAds = true;
									//get adEntryId from callback url
									adEntryId = curFeedUrl.split("u=")[1].split("_")[1].split("&")[0];
									adsIds.push(adEntryId);
									redirectUrl += "&adEntryId=" + adEntryId;

								}
						} else {
							// server is down, so use original link
							redirectUrl = curFeedUrl;
						}
						insHtml += '<li';
						if (pic) {
							
							insHtml += ' onmouseover="this.style.background=\'' + hvcolor + '\'" onmouseout="this.style.background=\'none\';"';
						}
						insHtml += '><a href="' + curFeedUrl + '" onmousedown="this.href=\''
							+ redirectUrl + '\';return true;" onmouseup="t=this;setTimeout(function(){t.href=\''
							+ curFeedUrl + '\'},50);return true;" target="_blank" title="'
							+ curFeedTitle + '"';
						if (pic) {
							
							insHtml += '" hidefocus="true">';
							
							if (!(p = curFeed.pic)) { // i want to set p here
								p = config.defaultPic;
							} 
							//put img out div because if img and div inner a <a>tag will make the href of tag a does not work // this bug only for ie6
							insHtml += '<img class="bshare-logos" src="' + p + '" style="width:' + config.picSize + 'px !important;height:' + config.picSize + 'px !important;border:1px solid ' + config.bdcolor + '!important;padding:3px !important;" />'
								+ '<div class="feed-title" title="' + htmlEncode(curFeedTitle) + '">' + curFeedTitle + '</div>';
						} else {
							insHtml += '>' + curFeedTitle;
						}
						
						insHtml += '</a></li>';
					}
					insHtml += "</ul>";
				}
                
				
                
                
                feedsList.innerHTML = insHtml;
                impFeedsContent(recommDiv, feedsList);
                feedsLength++;
            }
            main();
            implbShareFeeds(recommDiv, feedsList);
            resizeWidget(recommDiv);
            initRecommType(recommDiv);

			//auto paging
			autoPage = function(){
				
				var objs = getElemsByTagName(getElemById("bShareRecommTab"),"SPAN");
				if(currentPage >= pages) currentPage = 0;
				w.lezhi.switchPage(objs[currentPage],currentPage++);
				setTimeout(arguments.callee,autoPageTime);
			}()
        },
        switchTo: function(target, sourceType) {
            var recommTab = getElemById("bShareRecommTab"),
				recommDiv = getElemById("bShareRecommDiv"),
                recommDiv3 = getElemById("bShareRecommDiv3"),
				pagesList = getElemById("bShareRecommPages").getElementsByTagName("SPAN"),
				pagesLen = pagesList.length,
                lists = recommDiv3.childNodes,
                tabs = recommTab.childNodes, currentPageNum = 1, x = 0, i,
                j = 1;
            openAds = false;
            for (i = 0; i < tabs.length - 1; ++i) {
                tabs[i].className = "";
                //tabs[i].disabled = false;
            }
            target.className = "activated";
            //target.disabled = "disabled";
            for (x = j; x < lists.length; ++x) {
                if (lists[x].getAttribute("sourceType") === sourceType) {
                    lists[x].style.display = "block"; 
                    addClass(lists[x], "activated");
					currentPageNum = lists[x].getAttribute("cp");
                } else {
                    lists[x].style.display = "none";
                    removeClass(lists[x], "activated");
                }
            }
			if(currentPageNum == null) currentPageNum = 0;
			for(i = 0; i < pagesLen; i++){
				if(currentPageNum == i){
					pagesList[i].style.color = "red";
				}else{
					pagesList[i].style.color = "black";
				}
			}
            resizeWidget(recommDiv,false);
			if(!trendingShow && sourceType == "trending"){
				showUp("trending");
				trendingShow = true;
			}
			
        },
		switchPage: function(obj,pageNum){
			var recommDiv = getElemById("bShareRecommDiv"), recommDiv3 = getElemById("bShareRecommDiv3"), movePx =  parseInt(recommDiv3.style.width), finalPos = movePx * pageNum * -1,
			panel = getElemByClassName(recommDiv3,"div","activated")[0], slide, currentPos = parseInt(panel.style.left || 0), pageSpans = obj.parentNode.getElementsByTagName("span"), i, pageSpansLen = pageSpans.length;
			
			//currentPos = config.type === "slide" ? currentPos - 25 : currentPos;
			//finalPos = config.type === "slide" ? finalPos + 25 : finalPos;
			for(i = 0; i < pageSpansLen; i++){
				pageSpans[i].style.color = "black";
			}
			obj.style.color = "red";
			slide = function(){
				var des = finalPos > currentPos ? true : false,
				movePixels = config.col*5, timeIter = 1;
				if (currentPos === finalPos) {
                    return;
                } 
				currentPos = des ? currentPos + movePixels : currentPos - movePixels;
                if ((des && currentPos > finalPos) || (!des && currentPos < finalPos)) {
                    currentPos = finalPos;
                }
				//save panel current page
				panel.setAttribute("cp",pageNum);
                panel.style.left = currentPos + "px";

                setTimeout(slide, timeIter);
			};
			slide();
		}
    }


    // COMMON UTILITY FUNCTIONS
    function getElemById(id) {
        return d.getElementById(id);
    }
    function getElemsByTagName(element, tagName) {
        return element.getElementsByTagName(tagName);
    }

    function isUndefined(test) {
        return typeof test === "undefined";
    }
    function arrayContains(a,b){
		var _o = {};
		if(typeof(b) == "string"){
			b = b.split(",");
		}
		for(var i=0;i<a.length;i++){
			_o[a[i]] = 1;
		}
		for(var x=0;x<b.length;x++){
			if(_o[b[x]]){
				_o[b[x]] = 0;
			}
		}
		var _arr = [];
		for(var per in _o){
			if(_o[per] == 0){
				_arr.push(per);
			}
		}
		if(b.toString() == _arr.toString()){
			return true;
		}else{
			return false;
		}
		
	}
    function hasClass(ele, className) {
        return ele.className && arrayContains(ele.className.split(" "), className);
    }
    function addClass(ele, className) {
        if (!hasClass(ele, className)) {
            ele.className += " " + className;
        }
    }
    function removeClass(ele, className) {
        if (hasClass(ele, className)) {
            var clsss = ele.className.split(" "), i;
            for (i = 0; i < clsss.length; i++) {
                if (clsss[i] === className) clsss.splice(i++, 1);
            }
            ele.className = clsss.join(" ");
        }
    }
    function createElement(eTag, eId, eClass, eStyle, eText) {
        var newEle = d.createElement(eTag);
        if (eTag === "a") {
            newEle.href = "javascript:;";
            newEle.target = "_blank";
        }
        if (eId) newEle.id = eId;
        if (eClass) newEle.className = eClass;
        if (eStyle) newEle.style.cssText = eStyle;
        if (eText) newEle.innerHTML = eText;
        return newEle;
    }
    function htmlEncode(text) { 
        return text.replace(/&/g, '&amp').replace(/\"/g, '&quot;').replace(/\'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); 
    }
    function getElemByClassName(parent, tag, className) {
        var allEls = getElemsByTagName(parent, tag), i, clsss, clssName, retEle = [];
        for (i = 0; i < allEls.length; i++) {
            if (hasClass(allEls[i], className)) retEle.push(allEls[i]);
        }
        return retEle;
    }

    // LEZHI FUNCTIONS
	function showUp(ref){
		var showUpParam = [
                "uuid=" + config.uuid,
                "url=" + config.url,
                "ref=" + ref,
                "type=" + config.type,
				"pic=" + config.pic
            ];
        if(openAds)
		{
		    showUpParam = showUpParam.join("&") + "&adEntryIds=" + adsIds.join(",");
		}else{
			showUpParam = showUpParam.join("&");
		}
        loadModule(LEZHI_PLUGIN_BASE + "/showup?" + showUpParam);
	}

    function getRcommConfig() {
        var _scripts = getElemsByTagName(d, "script"),
            _scriptsLen = _scripts.length,
            _i, _j, _splitChar = ".js#", parms, _par0, _par1,
            _configLen,
            _configIndex,
            _scriptLz,
            _bshare_recomm_config = {};
        for (_i = 0; _i < _scriptsLen; _i++) {
            if (/lzChinaZ(Org|Tmp)?\.js/.test(_scripts[_i].src)) {
                _scriptLz = _scripts[_i].src;
                break;
            }
        }
        // also support ? after embedded javascript.
        if (_scriptLz.indexOf(_splitChar) < 2) {
            if (_scriptLz.indexOf(".js?") < 2) return {};
            else _splitChar = ".js?";
        }
        _configIndex = _scriptLz.indexOf(_splitChar);
        _scriptLz = _scriptLz.substring(_configIndex + _splitChar.length).split("&");
        _configLen = _scriptLz.length;
        for (_j = 0; _j < _configLen; _j++) {
            parms = _scriptLz[_j].split("=");
            _par0 = parms[0];
            _par1 = parms[1];
            // NOTE: checking if params are valid will be done during mergeConfig()
            // but need to convert these first:
            if (/(col|row|picSize|fontSize)$/.test(_par0)) {
                try {
                    _par1 = parseInt(_par1, 10);
                } catch(e) {
                    _par1 = null; // don't set anything here;
                }
            } else if (/(pic|debug)$/.test(_par0)) {
                _par1 = _par1 === "true";
            }
            _bshare_recomm_config[_par0] = _par1;
        }
        return _bshare_recomm_config;
    }

	//get page keywords from meta
	function getKeywords(){
		var _tags = document.getElementsByTagName("META"), _content = "", _len =  _tags.length;
		for(var i=0; i<_len; i++){
			if(_tags[i].getAttribute("name") == "keywords"){
				_content = _tags[i].getAttribute("content");
				break;
			}
		}
		return eu(_content);
	}

    function mergeConfig(conf, newconf) {
        var o, nco;
        for (o in conf) {
            nco = newconf[o];
            if (isUndefined(nco)
                    || (/(pic|debug|showLogo)$/.test(o) && typeof(nco) !== "boolean")
                    || (/(col|row|picSize|fontSize)$/.test(o) && (typeof(nco) !== "number" || nco < 0))
                    || (o === "type" && !arrayContains(BSHARE_RECOMM_TYPES, nco))
                    || (o === "source" && !arrayContains(BSHARE_RECOMM_SOURCES, nco))
                    || (o === "position" && !arrayContains(BSHARE_RECOMM_POSITIONS, nco))) {
                continue;
            }
            // these can not be an empty string
            if (nco === "" && /(defaultPic|title|url|hash|htcolor|rtcolor|bdcolor|hvcolor)/.test(o)) {
                nco = null;
            }
            // these can be an empty string: uuid, promote, sitePrefix, reason
            
            // if nco is not null here, change the conf...
            if (nco !== null) {
                if (o === "promote") nco = du(nco).substring(0,8);
                
                conf[o] = nco;
            }
        }
        // these need to be recalculated:
        var col = conf.col, row = conf.row;
        //conf.col = conf.pic ? col : 1;
        pageCounts = col * row > 10 ? 10 : col * row;
        
        return conf;
    }

	function impDiv(recommDiv){
		if(!getElemById("bShareRecommDiv2")) var recommDiv2 = createElement("div", "bShareRecommDiv2"); recommDiv.appendChild(recommDiv2);
		if(!getElemById("bShareRecommDiv3")){
			var recommDiv3 = createElement("div", "bShareRecommDiv3"), insHtml = ''; 
			
			if(config.brand != ""){
					insHtml += '<div class="b-recomm-footer"><span>' + config.brand + '</span></div>';
			}else if(config.showLogo){
					insHtml += '<div class="b-recomm-footer" style="position:absolute;float:right;"><a href="http://www.lezhi.me" target="_blank">乐知</a></div>';
			}else{
					insHtml += '';
			}
		} 
		recommDiv3.innerHTML = insHtml;
		recommDiv2.appendChild(recommDiv3);
	}

    function impFeedsContent(recommDiv, feedNode) {
        var promote = 4, recommDiv3 = getElemById("bShareRecommDiv3");
		
        recommDiv3.appendChild(feedNode);
        if (config.type === "slide" && !config.pic && getElemsByTagName(feedNode, "li").length < promote){
            feedNode.style.height = promote * 21 + "px";
        }
    }


    function implbShareFeeds(recommDiv) {
        var recommTab = getElemById("bShareRecommTab"), recommDiv2 = getElemById("bShareRecommDiv2"), nodes, insHtml = "", i, t, name, elem;

        if (!recommTab) {
            recommTab = createElement("ul", "bShareRecommTab", "b-recomm-tab");
            recommDiv2.insertBefore(recommTab, recommDiv2.firstChild);
        }

        nodes = getElemByClassName(recommDiv, "div", "b-recomm-panel");
		nodesLen = feedsLength;
        for (i = 0; i < nodes.length; ++i) {
             t = nodes[i].getAttribute("sourceType");
            name = BSHARE_RECOMM_SOURCES_NAMES[t];
            insHtml += '<li ' + (currentActivated === t ? 'class="activated" ' : "") 
                + 'onclick="window.lezhi.switchTo(this,\'' + nodes[i].getAttribute("sourceType") + '\');">';
			
			insHtml += t == "insite" ? config.promote : name;
			
			insHtml += '</li>';
            if (currentActivated === t) {
                // activate the content node:
                addClass(nodes[i], "activated");
            }
			if(i==0 && config.type === "fixed"){
				showUp(t);
			}

        }
		insHtml += "<div id='bShareRecommPages' class='bShareRecommPages'>";
		for(i = 0; i < pages; i++){
			
			insHtml += '<span onclick="window.lezhi.switchPage(this,' + (i) + ')" style="color:' + (i === 0 ? "red" : "black") + '">' + (i+1) + '</span>';
		}
        insHtml += "</div>";
        if (nodesLen === 0) {
            recommDiv.style.display = "none";
            return;
        } 
        
        //recommTab.style.visibility = nodes.length === 1 && config.type === "slide" && nodes[0].sourceType == "insite" ? "hidden" : "visible";
        recommTab.innerHTML = insHtml;

    }

    function resizePanelWidget(recommDiv, wid) {
        var _panels = getElemByClassName(recommDiv, "div", "b-recomm-panel"),
            _i,
            _len = _panels.length;
        for(_i = 0; _i < _len; _i++){
            _panels[_i].style.width = wid + "px";
        }
    }

    function resizeWidget(recommDiv,sFlag) {  
        var _wid = config.picSize + 30, _padding = 10, col = config.col, pic = config.pic, _slideTag = 0, recommDiv2 = getElemById("bShareRecommDiv2"), recommDiv3 = getElemById("bShareRecommDiv3"),
			_slideWid =recommDiv.style.width ? parseInt(recommDiv.style.width) : config.pic ? _wid * col : 600;
        if (pic) {
			if(config.type == "slide") _slideTag = 25;
            //recommDiv.style.width = recommDiv2.style.width = (col + colPad) * _wid + _padding + _slideTag + "px";
			if(sFlag != false){
				recommDiv.style.width=_slideWid + _slideTag + "px";
				recommDiv2.style.width = parseInt(recommDiv.style.width) - 2 + "px";
				recommDiv3.style.width = parseInt(recommDiv2.style.width) - _slideTag + "px";
				//recommTab.style.width = (col + colPad) * _wid + _padding - 4 + "px";
			}else{
				recommDiv.style.width=_slideWid + "px";
				recommDiv2.style.width = parseInt(recommDiv.style.width) - 2 + "px";
				recommDiv3.style.width = parseInt(recommDiv2.style.width) - _slideTag + "px";
				//recommTab.style.width = (col + colPad) * _wid + _padding - 4 + "px";
			} 

        } else {
			if(config.type == "slide"){
				_slideTag = sFlag == false ? 0 : 25;
			} 
            recommDiv.style.width = recommDiv2.style.width = _slideWid + _slideTag + "px";
			recommDiv2.style.width = parseInt(recommDiv.style.width) - 2 + "px";
			recommDiv3.style.width = parseInt(recommDiv2.style.width) - _slideTag + "px";
			//recommTab.style.width = (col + colPad) * _wid + _padding - 4 + "px";
			if(sFlag != false){
				resizePanelWidget(recommDiv, _slideWid-2);
			}
            
        }
        
        var recommPanel, 
            panels = getElemsByTagName(recommDiv, "div"),
            i, panel,
            recommDivWidth = parseInt(recommDiv.style.width), // if this does not work in ie6, try clientWidth
            customWidth, feedLists, feedList;
        for (i = 0; i < panels.length; ++i) {
            panel = panels[i];
            if (hasClass(panel, "b-recomm-panel") && hasClass(panel, "activated")) {
                recommPanel = panel;
                break;
            }
        }
        if (!recommPanel) {
            return;
        }
        recommPanel.style.width = (recommDiv2.clientWidth - _slideTag) * pages + "px";
			pageLists = getElemsByTagName(recommPanel, "ul");
			for(i = 0; i < pageLists.length; ++i){
				pageLists[i].style.width = (recommDiv2.clientWidth - _slideTag - 8) + "px";
			}
        customWidth = config.pic ? 
            // fixed column width for image widget, calculate margin length of image widget
            "auto" :
            // fixed margin length for text widget, calculate column width of text widget
            recommDivWidth - 2;
        customWidth = config.type == 'fixed' ? customWidth : customWidth - 30;
        feedLists = getElemsByTagName(recommPanel, "li");
        for (i = 0; i < feedLists.length; ++i) {
            feedList = feedLists[i];
            if (!config.pic){
				feedList.style.width = (config.col == 1 ? customWidth - (i == (feedLists.length - 1) ? 50 : 20) : customWidth/2 - 15) + "px";
				feedList.style.textAlign="left";
			}else{
				if(config.type == "slide"){
					_slideTag = sFlag == false ? 25 : 0;
				} 
				feedList.style.width=((_slideWid - _slideTag - _padding)/col)-2+"px";
			} 
        } 
    }

    function initRecommType(recommDiv) {                  
        var recommType = config.type, slideTag, recommClassName = "b-" + recommType, recommDiv2 = getElemById("bShareRecommDiv2"), lock = false;

        if (recommType === "slide") {
            slideTag = createElement("div", "recommSlideTag", "recommSlideTag", "", "<div>&gt;&gt;</div>" + "乐知推荐");
            recommDiv2.insertBefore(slideTag, recommDiv2.childNodes[0]);
        }

        if (hasClass(recommDiv, recommClassName)) return;
            
        
        recommDiv.className = recommClassName;
        
        var position, offset, out, curPosition, recommTag, slide,
            innerHeight, offsetHeight, overHeight,isShowUp;
        
        if (recommType === "slide") {
            position = config.position;
            recommDiv.className = "b-" + recommType + " " + "b-" + position;

            offset = -1 * recommDiv2.offsetWidth;
			
				if (position === "left"){
					recommDiv2.style.left = offset + "px";recommDiv.style.left = "0px";
				} 
				else{
					recommDiv2.style.right = offset + "px";recommDiv.style.right = "0px";
				} 
			
            

            out = true;
			isShowUp = false;
			
            curPosition = offset;
            
            slide = function() {
				//remove the slide animate for ie6/7 beacuse the plugin slide out form right has scroll-x bar in ie6
				/*if (isIE6o7()){
					out ? recommDiv.style.display = "block" : recommDiv.style.display = "none"
					if (position === "left") recommDiv.style.left = "0px";
                else recommDiv.style.right = "0px";
					if (!out) recommTagToggle(out); 
					return;
				} */
				if(out === true && isShowUp === false){
					if(insiteFeeds > 0){
						showUp("insite");
					}else if(trendingFeeds > 0){
						showUp("trending");
					}
					isShowUp = true;
				}
                var targetPosition = out ? 0 : -1 * recommDiv.offsetWidth,
                    movePixels = 40, timeIter = 1;
				recommDiv.style.zIndex = out ? "2147483584" : "-1";
                if (curPosition === targetPosition) {
                    if (!out) recommTagToggle(out); 
                    return;
                }     
                curPosition = out ? curPosition + movePixels : curPosition - movePixels;
                if ((out && curPosition > targetPosition) || (!out && curPosition < targetPosition)) {
                    curPosition = targetPosition;
                }
                if (position === "left") recommDiv2.style.left = curPosition + "px";
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
            
            ready(function() {
                recommTag = createElement("div", "bShareRecommTag", "bShareRecommTag");
                if (!getElemById("bShareRecommTag")) db.appendChild(recommTag);
                w.onscroll = function() {
                    var scrollTop = de.scrollTop || db.scrollTop, recSlTag = getElemById("recommSlideTag"), recommTag = getElemById("bShareRecommTag");
					if(scrollTop == 0) lock = false;
					if(!lock){
						if (out = (overHeight / 2 <= scrollTop && scrollTop !== 0)) { // i want to set out here
							recommTagToggle(out);
						}
						slide();
						recSlTag.style.display = out ? "block" : "none";
					}
                    
                };
                getElemById("bShareRecommTag").onclick = function() {
                    var scrollTop = de.scrollTop || db.scrollTop, recSlideTag = getElemById("recommSlideTag");
                    out = true;
					lock = true;
                    recommTagToggle();
                    slide();
                    this.style.display = "none";
                    if (recSlideTag) recSlideTag.style.display = "block";
                };
                getElemById("recommSlideTag").onclick = function() {
                    out = false;
					lock = true;
                    //recommTagToggle();
                    slide();
                    this.style.display = "none";
                };
            });
        }
    }

    function recommTagToggle(pOut) {
        getElemById("bShareRecommTag").style.display = pOut ? "none" : "block";
    }

    function loadStyle(css) {
        var styleElem = createElement("style");
        styleElem.type = "text/css";
        if (styleElem.styleSheet) {
            styleElem.styleSheet.cssText = css;
        } else {
            styleElem.appendChild(d.createTextNode(css));
        }
        getElemsByTagName(d, "head")[0].appendChild(styleElem);
    }

    function loadModule(url) {
        var jsonp = createElement("script");
        jsonp.type = "text/javascript";
		jsonp.charset = "utf-8";
        jsonp.src = url;
        getElemsByTagName(d, "head")[0].appendChild(jsonp);
    }

    function ready(callback) {
        // Mozilla, Opera and webkit
        if (d.addEventListener) {
           callback();
        } else if (d.attachEvent) {
            // ensure firing before onload,
            // maybe late but safe also for iframes
            d.attachEvent("onreadystatechange", function () {
                if (d.readyState === "complete") {
                    d.detachEvent("onreadystatechange", arguments.callee);
                    callback.call();
                }
            });

            // A fallback to window.onload, that will always work
            w.attachEvent("onload", callback);

            // If IE and not a frame
            // continually check to see if the document is ready
            var toplevel = false;
            try {
                toplevel = w.frameElement === null;
            } catch (e) {}

            if (de.doScroll && toplevel) (function () {
                try {
                    // http://javascript.nwbox.com/IEContentLoaded/
                    de.doScroll("left");
                } catch (error) {
                    setTimeout(arguments.callee, 10);
                    return;
                }
                callback.call();
            })();
        } else {
            w.onload = callback;
        }
    }
    
	function isIE6(){
		return /MSIE 6/.test(navigator.userAgent);
	}

	//match imagesUrl
	function matchImage(){
		var images = document.images, maxscore = 0, bestimage = null, bestLeft = 0, bestTop = 0, bodyWidth = document.body.scrollWidth, bodyHeight = document.body.scrollHeight, str = "";
		for ( var i = 0, len = images.length; i < len; i++) {
			var image = images[i];
			var offsetLeft = image.offsetLeft;
			var offsetTop = image.offsetTop;
			var obj = image.offsetParent;
			while (obj != null && obj.toString() != '[object HTMLBodyElement]') {	
				offsetLeft += obj.offsetLeft;
				offsetTop += obj.offsetTop;
				obj = obj.offsetParent;		
			}
			offsetLeft = Math.abs(offsetLeft);
			offsetTop = Math.abs(offsetTop);
			str += image.src + "=>" + image.width + '*' + image.height + ' '
					+ offsetLeft + "," + offsetTop + "\n";
		 
			if (image.width >= 200 && image.height >= 150 && offsetLeft > 30
					&& offsetLeft < bodyWidth / 2 && offsetTop > 30) {
				// alert(image.width + 'X' + image.height + '@' + image.offsetLeft + ','
				// + image.offsetTop + '->' + image.src + '->' + hex_md5(image.src));
				var fs = 1.0;
				if (endsWith(image.src, '.gif') || endsWith(image.src, '.ico'))
					fs = 0.4;
				var score =posScore(offsetLeft , offsetTop) * sizeScore(image.width, image.height) * fs;
				// var score = image.width * image.height * fs;
				if (score > maxscore) {
					maxscore = score;
					bestimage = image;
					bestLeft = offsetLeft;
					bestTop = offsetTop;
				}
			}
		}

		if (bestimage) {
			return bestimage.src;
		} else {
			return "";
		}

		function endsWith(str, suffix) {
			return str.indexOf(suffix, str.length - suffix.length) !== -1;
		}

		// 图片的尺寸计算分值
		function sizeScore(width, height) {
			if(width/height > 2.5){
				return 0;
			}
			var result = 0;
			if (width >= 400 && height >= 300) {
				result = 120000;
			} else {
				result = width * height;
				if (result > 120000) {
					result = 120000;
				}
			}
			return result;
		}

		// 图片的位置计算分值
		function posScore(left, top) {
			if(top > 1500){
				return 0;
			}
			var posScore =0;
			try{
				posScore = Math.abs((bodyWidth / 2 - left) * (1500 - top)
					/ (bodyWidth / 2 * 1500));
		//		alert(left+"x"+top+","+bodyWidth+"x"+bodyHeight+"=>"+posScore);
			}catch(e){
				//alert(e.name+e.message);
			}
			return posScore;
		}
	}

    function main() {
        // css: #bShareRecommDiv
        var pic = config.pic, picSize = config.picSize, position = config.position, fontSize = config.fontSize, type = config.type, promote = config.promote,
            rtcolor = config.rtcolor, hvcolor = config.hvcolor, htcolor = config.htcolor, bdcolor = config.bdcolor, convPosition = position == "left" ? "right" : "left",
            cssStyles = "#bShareRecommDiv{font-size:12px;z-index:2147483584;overflow:hidden !important;margin:0 !important;padding:0 !important;}"
			+ "#bShareRecommDiv2{position:relative;margin:0 !important;padding:0 !important;overflow:hidden;border:1px solid "
            + (bdcolor ? bdcolor : "#dadada") + "}"
			+ "#bShareRecommDiv3{position:relative;margin:0 !important;padding:0 0 20px 0 !important;overflow:hidden;left:"
			+ (type == 'slide' ? "25px" : "0px") + "}"
            + "#bShareRecommDiv:after,#bShareRecommDiv2:after,.b-recomm-panel:after,.b-recomm-tab:after,.b-recomm-list:after{content:'.';display:block;height:0;clear:both;visibility:hidden;}"
        // css: .b-recomm-tab
            + ".b-recomm-tab{list-style:none !important;padding:0 !important;margin:1px !important;background:#B8CDE3;position:relative;z-index:999;zoom:1;"
			if(type == "slide"){
			   cssStyles += (position == "left" ? "left:0px;":"left:25px;");
			}else{
				cssStyles += "left:0px;";
			}
			cssStyles += "}"
        // css: .b-recomm-tab li

            + ".b-recomm-tab li{width:auto;list-style:none !important;font-weight:bold;text-indent:0px !important;height:15px;position:relative;line-height:15px;float:left;cursor:pointer;margin:0 5px 0 0 !important;padding:5px 10px !important;text-align:center;position:relative;top:1px;z-index:100;font-size:" + fontSize + "px;"
            + (htcolor ? "color:" + htcolor : "") + "}"
        // css: .b-recomm-tab li.activated
           // + ".b-recomm-tab li.activated{font-weight:bold;cursor:default;background-color:#fff;border-style:solid;border-width:2px 1px 0;"
            //+ (bdcolor ? "border-color:" + bdcolor : "") + "}"
		// css: .bShareRecommPages
		    + ".bShareRecommPages{float:right;}"
			+ ".bShareRecommPages span{display:block;float:left;padding:0 6px;line-height:25px;cursor:pointer;}"
        // css: .b-recomm-panel
            + ".b-recomm-panel{display:none;margin:0 !important;padding:0 !important;float:left;position:relative;";

		    if(type == "slide"){
			   cssStyles += (position == "left" ? "left:0px;":"left:25px;");
			}else{
				cssStyles += "left:0px;";
			}
		    cssStyles += "background-color:#fff;clear:both;}"

        // css: .b-recomm-panel.activated
            //+ ".b-recomm-panel.activated{display:block;border:1px solid " + bdcolor + ";}"
        // css: .b-recomm-list

            + ".b-recomm-list{height:auto;float:left;";
        
        if ((!pic && type === 'slide' && promote.length > (maxFeedsLength * config.row))
                || (pic && type === 'slide' && maxFeedsLength === 0)) {
            cssStyles += "height:" + (4 * 17) + "px;";
        } 
        cssStyles += "padding:0px 4px !important;margin:0 !important;list-style:disc inside none;}"
 
        // css: .b-recomm-list li  alice changed text-align:left!important; become text-align:center
            + ".b-recomm-list li{padding:0 !important;display:block;text-align:center;text-indent:0px !important;"
            + (pic ? "width:" + (picSize + 8) + "px;height:auto;border:1px solid #fff;cursor:pointer;list-style:none !important;float:left;margin:3px 0;"
                    : "float:left;overflow:hidden;border:0;padding-left:10px !important;") + "}"
            
        // css: .b-recomm-list a
        cssStyles += ".b-recomm-list a{font-size:" + fontSize + "px;margin:0 !important;position:relative;text-decoration:none !important;display:"
            + (pic ? "block;height:auto;line-height:auto;overflow:visible;" : "list-item;zoom:1;height:24px;line-height:24px;overflow:hidden;color:" + rtcolor) + ";list-style:disc inside none !important;}"
        // css: .b-recomm-list a:hover
            + ".b-recomm-list a:hover{border-ratio:5px;"
            + (hvcolor ? "color:" + hvcolor : "") + "}"
            //+ ".b-recomm-list a:hover .feed-pic{border:1px solid #dadada;}"
			//+ ".b-recomm-list li a:hover{" + (hvcolor ? "color:" + hvcolor : "") + "}"
        // css: miscellaneous
            //+ ".feed-star{margin:0;padding:0;width:16px;height:16px;background:url(" + LEZHI_STATIC_BASE + "/images/star.gif) no-repeat;float:right;position:relative;bottom:5px;bottom:-6px\9;right:5px;visibility:hidden;cursor:pointer;}"
            //+ ".bShareRecommTit{font-size:14px;color:#4a4a4a;font-weight:bold;margin:0 0 20px;padding:0;}"
            + ".bShareRecommTag{margin:0;padding:0;background:url(" + LEZHI_STATIC_BASE + "/images/recommend.png) no-repeat;width:40px;height:40px;position:fixed;bottom:60px;*bottom:66px;";
			/*if(config.positionY == "top"){
				cssStyles += "top:10px;bottom:auto;";
			}else if(config.positionY == "center"){
				cssStyles += "top:50%;bottom:auto;";
			}else{
				cssStyles += "bottom:10px;*bottom:16px;top:auto;";
			}*/
			cssStyles +=  (position == "left" ? "left:10px;right:auto;" : "right:10px;left:auto;")
			+ "z-index:2147483584;cursor:pointer;}";
            if(isIE6()){
				cssStyles += "*html .bShareRecommTag{position:absolute;z-index:2147483584;bottom:auto;top:expression(eval(-70+document.documentElement.scrollTop+document.documentElement.clientHeight-this.offsetHeight-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
				/*if(config.positionY == "top"){
					cssStyles += "expression(eval(10+document.documentElement.scrollTop));}";
				}else if(config.positionY == "center"){
					cssStyles += "expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight/2-this.offsetHeight/2-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
				}else{
					cssStyles += "expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight-this.offsetHeight-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
				}*/
			}
        // css: .b-recomm-list .feed-title

            cssStyles += ".b-recomm-list .feed-title{overflow:hidden;padding:0 !important;margin:5px 0 0 0 !important;line-height:15px;height:30px;font-size:12px;"
            + (rtcolor ? "color:" + rtcolor + ";" : "")
            +"}";

        // css: image box of image widget
        if (pic) {
            // css: .b-recomm-list .feed-pic
            //cssStyles += ".b-recomm-list .feed-pic{position:relative;left:0px;width:" + picSize + "px;height:" + picSize + "px;background:#fff;margin:5px 0px;padding:3px;overflow:hidden;border:1px solid #DADADA;}"
            // css: .b-recomm-list .feed-pic img

                cssStyles +=  ".b-recomm-list img{display:inline-block !important;padding:3px !important;margin:0 !important;position:relative;top:4px;border:1px solid " + bdcolor + ";float:none !important;}";

        }
        // css: .b-recomm-footer a
        cssStyles += ".b-recomm-footer{overflow:hidden;float:right;margin:0 !important;position:absolute;text-align:right;bottom:0px;right:3px;" + ((pic || (config.col == 2 && config.row > 2) || type == "fixed") ? "width:100%;_bottom:0px;" : "position:absolute;padding:0px;") + "}"
            + ".b-recomm-footer a{font-size:12px;color:#999;text-decoration:none;height:15px !important;line-height:15px !important;}"
        
        // TYPE: Slide
            + "#bShareRecommDiv.b-slide{position:fixed;bottom:10px;top:auto;margin-top:0px !important;}";
			/*if(config.positionY == "top"){
				cssStyles += "top:0px;bottom:auto;margin-top:0px !important;";
			}else if(config.positionY == "center"){
				cssStyles += "top:50%;bottom:auto;margin-top:" + getElemById("bShareRecommDiv2").clientHeight/2/-1 + "px !important;";
			}else{
				cssStyles += "bottom:10px;top:auto;margin-top:0px !important;";
			}
			cssStyles += "}";*/
            if(isIE6()){
				cssStyles += "*html #bShareRecommDiv.b-slide{position:absolute;bottom:auto;top:expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight-this.offsetHeight-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
				/*if(config.positionY == "top"){
					cssStyles += "expression(eval(document.documentElement.scrollTop));}";
				}else if(config.positionY == "center"){
					cssStyles += "expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight/2-this.offsetHeight/2-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
				}else{
					cssStyles += "expression(eval(-10+document.documentElement.scrollTop+document.documentElement.clientHeight-this.offsetHeight-(parseInt(this.currentStyle.marginTop,10)||0)-(parseInt(this.currentStyle.marginBottom,10)||0)));}";
				}*/
			} 
            cssStyles += "#bShareRecommDiv.b-slide.b-left{left:0;}"
            + "#bShareRecommDiv.b-slide.b-right{right:0px !important;}"
            + ".b-slide.b-right .b-recomm-tab li{float:left;}"
			+ ".b-slide.b-" + position + " .recommSlideTag{display:none;margin:0 !important;padding:0 0 4px !important;border:1px solid #c2c2c2;position:absolute;width:23px;" + convPosition + ":1px;top:27px;cursor:pointer;text-align:center;line-height:17px;color:#444;"
            + "background:-webkit-gradient(linear,0% 20%,0% 90%,from(#f4f4f4),to(#d7d7d7));background:-moz-linear-gradient(top,#f4f4f4,#d7d7d7);"
            + "filter:progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr='#f4f4f4',EndColorStr='#d7d7d7');_background:#e4e4e4;}";
        
        loadStyle(cssStyles);
    }
    lz.init();
})();
