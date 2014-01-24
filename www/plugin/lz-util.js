(function() {
	// this is loaded first... so if lezhi is already defined, do not load again
	if (typeof window.lezhi === "undefined") window.lezhi = {};
	else return;
	
	window.lezhi.util = {
	    // COMMON UTILITY FUNCTIONS
	    getElemById: function(id) {
	        return document.getElementById(id);
	    },
	    getElemsByTagName: function(element, tagName) {
	    	//alert(element);
	        return element.getElementsByTagName(tagName);
	    },
	    isUndefined: function(test) {
	        return typeof test === "undefined";
	    },
	    arrayContains: function(a, b) {
			var _o = {}, _arr = [], i;
			if (typeof(b) == "string") b = b.split(",");
			for (i = 0; i < a.length; i++) _o[a[i]] = 1;
			for (i = 0; i < b.length; i++) {
				if (_o[b[i]]) _o[b[i]] = 0;
			}
			for (i in _o) {
				if(_o[i] == 0) _arr.push(i);
			}
			return b.toString() == _arr.toString();
		},
		hasClass: function(ele, className) {
	        return ele.className && this.arrayContains(ele.className.split(" "), className);
	    },
	    hasId: function(ele) {
	        return ele.id && ele.id !== "";
	    },
	    addClass: function(ele, className) {
	        if (!this.hasClass(ele, className)) {
	            ele.className += " " + className;
	        }
	    },
	    removeClass: function(ele, className) {
	        if (this.hasClass(ele, className)) {
	            var clsss = ele.className.split(" "), i;
	            for (i = 0; i < clsss.length; i++) {
	                if (clsss[i] === className) clsss.splice(i++, 1);
	            }
	            ele.className = clsss.join(" ");
	        }
	    },
	    joinProperty: function(conf,otherConf) {
	    	var newConf = {}, attrname;
		    for (attrname in conf) newConf[attrname] = conf[attrname];
		    for (attrname in otherConf) newConf[attrname] = otherConf[attrname];
		    return newConf;
	    },
	    createElement: function(eTag, eId, eClass, eStyle, eText) {
	        var newEle = document.createElement(eTag);
	        if (eTag === "a") {
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
	        var allEls = this.getElemsByTagName(parent, tag), i, clsss, clssName, retEle = [];
	        for (i = 0; i < allEls.length; i++) {
	            if (this.hasClass(allEls[i], className)) retEle.push(allEls[i]);
	        }
	        return retEle;
	    },
		//get page keywords from meta
		getKeywords: function() {
			var _tags = this.getElemsByTagName(document, "META"), i;
			for (i = 0; i < _tags.length; i++){
				if (_tags[i].getAttribute("name") == "keywords") {
					return encodeURIComponent(_tags[i].getAttribute("content"));
				}
			}
			return "";
		},
		getIndex: function(domList, ele) {
	    	var i, len = domList.length;
	    	for (i = 0; i < len; i++) {
	    		if (domList[i] === ele) {
	    			return i;
	    		}
	    	}
	    	return -1;
	    },
	    loadStyle: function(css) {
	        var styleElem = this.createElement("style");
	        styleElem.type = "text/css";
	        if (styleElem.styleSheet) {
	            styleElem.styleSheet.cssText = css;
	        } else {
	            styleElem.appendChild(document.createTextNode(css));
	        }
	        this.getElemsByTagName(document, "head")[0].appendChild(styleElem);
	    },
	    loadModule: function(url) {
	    	// If the script has been requested, then we don't request it again.
			if (this.arrayContains(window.lezhi.variables.requestedScripts, url)) return;

			// don't store things that can be requested more than once! this will make the requestedScripts array smaller.
			if (!/(\showup)/.test(url)) {
				window.lezhi.variables.requestedScripts.push(url);
			}
			
	        var jsonp = this.createElement("script");
	        jsonp.type = "text/javascript";
			jsonp.charset = "utf-8";
	        jsonp.src = url;
	        this.getElemsByTagName(document, "head")[0].appendChild(jsonp);
	    },
	    mergeConfig: function(conf, newconf) {
	        var o, nco, config = {}, variables = window.lezhi.variables;
	        for (o in conf) {
	            nco = newconf[o];
	            if (this.isUndefined(nco)
	                    || (/(pic|debug|highlight|showLogo|picBorderRadius)$/.test(o) && typeof(nco) !== "boolean")
	                    || (/(col|row|picSize|fontSize|width|height|titleFontSize|lineHeight|count)$/.test(o) && (typeof(nco) !== "number" || nco < 0))
	                    || (o === "type" && !this.arrayContains(variables.BSHARE_RECOMM_TYPES, nco))
						|| (o === "redirectType" && !this.arrayContains(variables.BSHARE_RECOMM_REDIRECT_TYPE, nco))
						|| (o === "listType" && !this.arrayContains(variables.BSHARE_RECOMM_LISTTYPE, nco))
						|| (o === "picMatch" && !this.arrayContains(variables.BSHARE_RECOMM_PICMATCH, nco))
	                    || (o === "source" && !this.arrayContains(variables.BSHARE_RECOMM_SOURCES, nco))
	                    || (o === "position" && !this.arrayContains(variables.BSHARE_RECOMM_POSITIONS, nco))) {
	                nco = conf[o];
	            }
	            
	            // if nco is not null here, change the conf...
	            if (o === "promote") nco = decodeURIComponent(nco).substring(0,8);
	            if (!this.hasOwnProp(config, o)) {
	            	config[o] = null;
	            }
	            config[o] = nco;
	        }
	        // these need to be recalculated:
	        //var col = conf.col, row = conf.row;
	        //conf.col = conf.pic ? col : 1;
	        //maxCounts = col * row > 10 ? 10 : col * row;
	        
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
	    },
	    hasOwnProp: function (obj,pram) {
	    	for (q in obj) {
	    		if (q === pram) {
	    			return true;
	    		}
	    	}
	    	return false;
	    },
	    isIE6: function() {
			return /MSIE 6/.test(navigator.userAgent);
		},
		//match imagesUrl
		matchImage: function(){
			var images = document.images, maxscore = 0, bestimage = null, bestLeft = 0, bestTop = 0, 
			    bodyWidth = document.body.scrollWidth, bodyHeight = document.body.scrollHeight, str = "",
			    i, len = images.length, image, offsetLeft, offsetTop, obj, fs, score;
			for (i = 0; i < len; i++) {
				image = images[i],
				    offsetLeft = image.offsetLeft,
				    offsetTop = image.offsetTop,
				    obj = image.offsetParent;
				while (obj != null && obj.toString() != '[object HTMLBodyElement]') {	
					offsetLeft += obj.offsetLeft;
					offsetTop += obj.offsetTop;
					obj = obj.offsetParent;		
				}
				offsetLeft = Math.abs(offsetLeft);
				offsetTop = Math.abs(offsetTop);
				str += image.src + "=>" + image.width + '*' + image.height + ' '
						+ offsetLeft + "," + offsetTop + "\n";
			 
				if (image.width >= 200 && image.height >= 150 && offsetLeft > 40
						&& offsetLeft < bodyWidth / 2 && offsetTop > 40) {
					// alert(image.width + 'X' + image.height + '@' + image.offsetLeft + ','
					// + image.offsetTop + '->' + image.src + '->' + hex_md5(image.src));
					fs = 1.0;
					if (endsWith(image.src, '.gif') || endsWith(image.src, '.ico')) fs = 0.4;
					score = posScore(offsetLeft , offsetTop) * sizeScore(image.width, image.height) * fs;
					// var score = image.width * image.height * fs;
					if (score > maxscore) {
						maxscore = score;
						bestimage = image;
						bestLeft = offsetLeft;
						bestTop = offsetTop;
					}
				}
			}
	        
			return bestimage ? bestimage.src : "";
	
			// INTERNAL FUNCTIONS:
			function endsWith(str, suffix) {
				return str.indexOf(suffix, str.length - suffix.length) !== -1;
			}
	
			// 图片的尺寸计算分值
			function sizeScore(width, height) {
				if (width/height > 2) {
					return 0;
				}
				var result = width * height;
				if (result > 120000) {
					result = 120000;
				}
				return result;
			}
	
			// 图片的位置计算分值
			function posScore(left, top) {
				if (top > 1500) {
					return 0;
				}
				var posScore = 0;
				try {
					posScore = Math.abs((bodyWidth / 2 - left) * (1500 - top) / (bodyWidth / 2 * 1500));
					//alert(left+"x"+top+","+bodyWidth+"x"+bodyHeight+"=>"+posScore);
				} catch(e) {
					//alert(e.name+e.message);
				}
				return posScore;
			}
		}
	};
})();