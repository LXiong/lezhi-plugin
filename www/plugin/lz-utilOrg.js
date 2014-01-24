(function() {
	// this is loaded first... so if lezhi is already defined, do not load again
	if (typeof window.lezhi != "undefined") return

	var w = window, lz = w.lezhi = {}, d = document;
	lz.util = {
		isIE6: /msie|MSIE 6/.test(navigator.userAgent),
		isIE: /msie|MSIE/.test(navigator.userAgent),
	    // COMMON UTILITY FUNCTIONS
	    getElemById: function(id) {
	        return d.getElementById(id);
	    },
	    getElemsByTagName: function(element, tagName) {
	        return element.getElementsByTagName(tagName);
	    },
	    isUndefined: function(test) {
	        return typeof test == "undefined";
	    },
	    isNotUndefined: function(test) {
	        return typeof test != "undefined";
	    },
		arrayContains: function(a, b) {
            var i = a.length;
            while (i--) {
               if (a[i] === b) {
                   return true;
               }
            }
            return false;
        },
		lzhasClass: function(ele, className) {
	        return ele.className && this.arrayContains(ele.className.split(" "), className);
	    },
	    addClass: function(ele, className) {
	        if (!this.lzhasClass(ele, className)) {
	            ele.className += (ele.className ? " " : "") + className;
	        }
	    },
	    removeClass: function(ele, className) {
	        if (this.lzhasClass(ele, className)) {
	            var clsss = ele.className.split(" "), i;
	            for (i = 0; i < clsss.length; i++) {
	                if (clsss[i] == className) clsss.splice(i++, 1);
	            }
	            ele.className = clsss.join(" ");
	        }
	    },
	    joinProperty: function(conf, otherConf) {
		    for (var attrname in otherConf) conf[attrname] = otherConf[attrname];
		    return conf;
	    },
	    createElement: function(eTag, eId, eClass, eStyle, eText) {
	        var newEle = d.createElement(eTag);
	        if (eTag == "a") {
	            newEle.href = "javascript:;";
	            newEle.target = "_blank";
	        }
	        if (eId) newEle.id = eId;
	        if (eClass) newEle.className = eClass;
	        if (eStyle) newEle.style.cssText = eStyle;
	        if (eText) newEle.innerHTML = eText;
	        return newEle;
	    },
	    htmlEncode: function(text) { 
	        return text.replace(/&/g, '&amp').replace(/\"/g, '&quot;').replace(/\'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); 
	    },
	    getElemByClassName: function(parent, tag, className) {
	        var allEls = this.getElemsByTagName(parent, tag), i, retEle = [];
	        for (i = 0; i < allEls.length; i++) {
	        	if (this.lzhasClass(allEls[i], className)) retEle.push(allEls[i]);
	        }
	        return retEle;
	    },
		// get page keywords from meta
		getKeywords: function() {
			var _tags = this.getElemsByTagName(d, "META"), i, j = "";
			for (i = 0; i < _tags.length; i++) {
				if (_tags[i].getAttribute("name") == "keywords") {
					j = encodeURIComponent(_tags[i].getAttribute("content"));
				}
			}
			return j;
		},
		getIndex: function(domList, ele) {
			var i, j = -1;
	    	for (i in domList) {
	    		if (domList[i] === ele) {
	    		    j = i;
	    		}
	    	}
	    	return j;
	    },
	    getHash: function(url) {
	    	url = /#(.+)/.exec(url);
	        if (url) {
	            url = url[0].slice(1);
	        }
	        return url || "";
	    },
	    loadStyle: function(css) {
	        var styleElem = this.createElement("style");
	        styleElem.type = "text/css";
	        if (styleElem.styleSheet) {
	            styleElem.styleSheet.cssText = css;
	        } else {
	            styleElem.appendChild(d.createTextNode(css));
	        }
	        this.getElemsByTagName(d, "head")[0].appendChild(styleElem);
	        return styleElem;
	    },
	    loadModule: function(url) {
	    	// If the script has been requested, then we don't request it again.
			if (!this.arrayContains(lz.variables.requestedScripts, url)) {
				// don't store things that can be requested more than once! this will make the requestedScripts array smaller.
				if (!/\showup|\feeds/.test(url) && !lz.util.arrayContains(lz.variables.ADS_JS_URL, url)) {
					lz.variables.requestedScripts.push(url);
				}
				
		        var jsonp = this.createElement("script");
		        jsonp.type = "text/javascript";
				jsonp.charset = "utf-8";
		        jsonp.src = url;
		        this.getElemsByTagName(d, "head")[0].appendChild(jsonp);
		    }
	    },
	    mergeConfig: function(conf, newconf) {
	        var o, nco, variables = lz.variables, config = this.joinProperty({}, conf);
	        for (o in newconf) {
	        	nco = newconf[o];
	        	if (this.isUndefined(nco)
	                    || (/(pic|highlight|showLogo|picBorderRadius)$/.test(o) && typeof(nco) != "boolean")
	                    || (/(col|row|(pic|font|titleFont)Size|width|height|lineHeight|count)$/.test(o) && (isNaN(nco) || typeof(nco) != "number" || nco < 0))
	                    || (o == "type" && !this.arrayContains(variables.BSHARE_RECOMM_TYPES, nco))
						|| (o == "redirectType" && !this.arrayContains(variables.BSHARE_RECOMM_REDIRECT_TYPE, nco))
						|| (o == "listType" && !this.arrayContains(variables.BSHARE_RECOMM_LISTTYPE, nco))
	                    || (o == "position" && !this.arrayContains(variables.BSHARE_RECOMM_POSITIONS, nco))) {
	                nco = config[o];
	            }
				if(/(ht|rt|bd|hv|bg)color|titleBgColor/.test(o) && nco){
					try{
						nco = decodeURIComponent(nco);
					}catch(e){
						nco = nco;
					}
				} 
				if (o == "promote"){
					try{
						nco = decodeURIComponent(nco).slice(0,8);
					}catch(e){
						nco = nco.slice(0,8);
					}
				} 
				// these can be an empty string: uuid, promote, sitePrefix, reason
				if (!nco && /uuid|promote|sitePrefix|reason/.test(o)) config[o] = "";
				
				// if nco is not null here, change the conf...
				if (nco !== null && !this.isUndefined(nco)) config[o] = nco;
	        }
	        
	        return config;
	    },
	    ready: function(callback) {
	        // Mozilla, Opera and webkit
	        if (document.addEventListener) {
	           callback();
	        } else if (document.attachEvent) {
	            // ensure firing before onload,
	            // maybe late but safe also for iframes
	            document.attachEvent("onreadystatechange", function () {
	                if (document.readyState === "complete") {
	                    document.detachEvent("onreadystatechange", arguments.callee);
	                    callback.call();
	                }
	            });
	
	            // A fallback to window.onload, that will always work
	            window.attachEvent("onload", callback);
	
	            // If IE and not a frame
	            // continually check to see if the document is ready
	            var toplevel = false;
	            try {
	                toplevel = window.frameElement === null;
	            } catch (e) {}
	
	            if (document.documentElement.doScroll && toplevel) (function () {
	                try {
	                    // http://javascript.nwbox.com/IEContentLoaded/
	                    document.documentElement.doScroll("left");
	                } catch (error) {
	                    setTimeout(arguments.callee, 10);
	                    return;
	                }
	                callback.call();
	            })();
	        } else {
	            window.onload = callback;
	        }
	    },
	    contains: function (items,item){
			if(items.length == 0){
				return false;
			}else{
				for(var i = 0;i < items.length;i ++){
					if(items[i] == item){
						return true;
					}
				}
			}
		},
		//match imagesUrl
		matchImage: function() {
			var images = d.images, maxscore = 0, bestimage, //bestLeft = 0, bestTop = 0, 
			    bodyWidth2 = d.body.scrollWidth/2, //bodyHeight = document.body.scrollHeight,
			    i, image, offsetLeft, offsetTop, obj, score, top = 0, 
			    unlikelyCandidates = /combx|ad|comment|community|disqus|extra|foot|header|menu|remark|rss|shoutbox|sidebar|sponsor|ad-break|agegate|pagination|pager|popup|tweet|twitter/i,
				okMaybeItsACandidate = /and|article|body|column|main|shadow/i,
				allElements = lz.util.getElemsByTagName(d,'*'),
				allElementsLen = allElements.length,unlikelyArray = [];

			for (var nodeIndex = 0; nodeIndex < allElementsLen; nodeIndex++) {
				var node = allElements[nodeIndex];
				var unlikelyMatchString = node.className + node.id;
				if (unlikelyMatchString.length > 0 && unlikelyMatchString.search(unlikelyCandidates) != -1 && unlikelyMatchString.search(okMaybeItsACandidate) == -1 && node.tagName !== "BODY"
				) {
					var nodeImages = lz.util.getElemsByTagName(node,"img"), imagesLen = nodeImages.length, i;
					for(i = 0; i < imagesLen; i++){
						unlikelyArray.push(nodeImages[i]);
					}
				}
			}

			for (i = 0; i < images.length; i++) {
				image = images[i];
				if(lz.util.contains(unlikelyArray,image)){
					continue;
				}
				offsetLeft = image.offsetLeft;
				offsetTop = image.offsetTop;
				obj = image.offsetParent;
				while (obj && obj.nodeName != 'BODY') {
					offsetLeft += obj.offsetLeft;
					offsetTop += obj.offsetTop;
					obj = obj.offsetParent;
				}
				offsetLeft = offsetLeft > 0 ? offsetLeft : -offsetLeft;
				offsetTop = offsetTop > 0 ? offsetTop : -offsetTop;
			 
				if (image.width >= 200 && image.height >= 150 && offsetLeft > 40
						&& offsetLeft < bodyWidth2 && offsetTop > 40) {
					score = (top > 1500 ? 0 : Math.abs((bodyWidth2 - offsetLeft) * (1500 - offsetTop) / (bodyWidth2 * 1500)))
					    * (image.width/image.height > 2 ? 0 : (image.width * image.height > 120000 ? 120000 : image.width * image.height))
					    * (/\.(gif|ico)$/.test(image.src) ? 0.4 : 1.0);
					if (score > maxscore) {
						maxscore = score;
						bestimage = image;
						//bestLeft = offsetLeft;
						//bestTop = offsetTop;
					}
				}
			}
	        
			return bestimage ? bestimage.src : "";
		}
	};
})();
