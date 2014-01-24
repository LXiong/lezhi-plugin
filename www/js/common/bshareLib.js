(function(window) {

	var BshareLib = function(arg){
			return new BshareLib.fn.init(arg);
	}

	BshareLib.fn = BshareLib.prototype = {
		//pass parms to get dom object
		init: function(selector){
			var selector = selector, selectorTag = '', selectorContent = '', selectorSuper = '', selectorSub = '', results, _this = BshareLib.fn;
			if(typeof(selector) != 'string' || selector.length  == 0){
				return false;
			}
			selectorTag = selector.substring(0,1);
			selectorContent = selector.substring(1);
			if(selectorTag.indexOf("#") != -1){
				_this.selectorMode = _this.getElemById;
			}else if(selectorTag.indexOf(".") != -1){
				_this.selectorMode = _this.getElemByClassName;
			}else{
				return document.getElemensByTagName("*");
			}
			if(selectorContent.indexOf(" ") != -1){
				selector = selectorContent.split(" ");
				selectorSuper = _this.trim(selectorContent[0]);
				selectorSub = _this.trim(selectorContent[1]);
			}else{
				selectorSuper = _this.trim(selectorContent);
			}
			if(selectorSub.length == 0){
				results = _this.selectorMode(selectorSuper);
				_this.selectorcollection = results;
				return BshareLib.fn;
			}else{
				results = _this.selectorMode(selectorSuper);
			}
		},
		// save get dom mode
		selectorMode: function(){
		},
		selectorcollection: [],
		//trim all spaces
		trim: function(str){
			return str.replace(/ /gi,'');
		},
		trimFrontAndEnd: function(str){
			return str.replace( /^\s*|\s*$/g,'');
		},
		//get element by id
		getElemById: function(id) {
			var arr = [];
			arr.push(document.getElementById(id));
			return arr;
		},
		//get element by class name only first element
		getElemByFirstClassName: function(className){
			var allEl = document.getElementsByTagName("*"), i, len = allEl.length;
			for(i = 0; i < len; i++){
				if(BshareLib.fn.hasClass(allEl[i],className)){
					return allEl[i];
				}
			}
		},
		//get element by tag name
		getElemsByTagName: function(element, tagName) {
			return element.getElementsByTagName(tagName);
		},
		//get elements all the same class name
		getElemByClassName: function(className) {
			var _this = BshareLib.fn, ele = _this.getElemByFirstClassName(className), tag =  ele.tagName, parent = ele.parentNode, allEls = _this.getElemsByTagName(parent, tag), i, clsss, clssName, retEle = [];
			for (i = 0; i < allEls.length; i++) {
				if (_this.hasClass(allEls[i], className)) retEle.push(allEls[i]);
			}
			return retEle;
		},
		hasClass: function(ele, className) {
			return ele.className && BshareLib.fn.arrayContains(ele.className.split(" "), className);
		},
		//return document
		doc: document,
		//return document.documentElement
		body: document.documentElement,

			//checkout array b has in array a
			 arrayContains: function(a,b){
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
				
			},
			//checkout object obj has in array arr
			inArray: function(obj,arr){
				if(!BshareLib.fn.isArray(arr)){
					return null;
				}
				for ( var i = 0, length = arr.length; i < length; i++ ) {
				   if ( arr[ i ] === obj ) {
					  return i;
				   }
			   }
			   return -1;
			},
			//remove duplicate value in array
			removeDuplicateInArray: function(arr){
				if(!BshareLib.fn.isArray(arr)){
					return null;
				}
				var o = {}, newArr = [], j = 0;
				for ( var i = 0, length = arr.length; i < length; i++ ) {
				    if(o[arr[i]] == undefined){
						o[arr[i]] = j;
						j++;
					} 
			    }
				for(var p in o){
					//newArr.push(p);
					newArr[o[p]] = p;
				}
				return newArr;
			},
			//inversion array vaule sort
			inversion: function(arr){
				if(!BshareLib.fn.isArray(arr)){
					return null;
				}
				var newArr = [];
				for ( var i = arr.length - 1; i >= 0; i-- ) {
				    newArr.push(arr[i]);
			    }
				return newArr;
			},

		//judge obj type
		isFunction: function(fn){
			return Object.prototype.toString.call(fn).indexOf('Function') > 0 ? !0 : !1;
		},
		isArray: function(arr){
			return Object.prototype.toString.call(arr).indexOf('Array') > 0 ? !0 : !1;
		},
		isString: function(str){
			return Object.prototype.toString.call(str).indexOf('String') > 0 ? !0 : !1;
		},
		isNumber: function(num){
			return Object.prototype.toString.call(num).indexOf('Number') > 0 ? !0 : !1;
		},
		isObject: function(obj){
			return Object.prototype.toString.call(obj).indexOf('Object') > 0 ? !0 : !1;
		},
		isDom: function(el){
			return el.nodeType ? !0 : !1;
		},
		//each for all dom elements, only 1 parm is callback function
		each: function(callback){
			var _this = BshareLib.fn, i, selectors = _this.selectorcollection, len = selectors.length;
			if(len > 0){
				for(i = 0; i < len; i++){
					if(_this.isDom(selectors[i])){
						callback.call(selectors[i]);
					}
				}
			}
		},
		//make string to Object
		stringToObject: function(str,separate,end){
			if(!BshareLib.fn.isString(str)){
				return null;
			}
			if(str.substr(str.length-1) == end){
				str = str.substr(0,str.length-1);
			}
			var strHash = str.split(end), o = {};
			for ( var i = 0, length = strHash.length; i < length; i++ ) {
				var arr = strHash[i].split(separate), key = arr[0], value = arr[1];
				o[key] = value;
			}
			return o;
		},
		//make Object to string
		ObjectToString: function(obj,separate,end){
			if(!BshareLib.fn.isObject(obj)){
				return null;
			}
			var str = '';
			for(var par in obj){
				str += par;
				str += separate;
				str += obj[par];
				str += end;
			}
			return str;
		},
		//for get & set css style
		css: function(css){
			var _this = BshareLib.fn;
			
			if(arguments.length == 0){
				return _this.getCss();
			}else{
				_this.setCss(css);
			}
			
			
		},
		getCss: function(){
			var _this = BshareLib.fn, arr = [];
			_this.each(function(){
				arr.push(_this.stringToObject(this.style.cssText,':',';'));
			});
			return arr;
		},
		setCss: function(css){
			var _this = BshareLib.fn;
			_this.each(function(){
				var cssText = '';
				if(_this.isObject(css)){
					cssText = _this.ObjectToString(css,':',';');
					this.style.cssText += cssText;
				}else if(_this.isString(css)){
					this.style.cssText += css;
				}else{
					return null;
				}
			});
		},
		//get & set htmlcode
		html: function(htmlCode){
			var _this = BshareLib.fn;
			if(arguments.length == 0){
				if(_this.selectorcollection.length > 0){
					return _this.selectorcollection[0].innerHTML;
				}
			}else{
				_this.each(function(){
					this.innerHTML = htmlCode;
				});
			}
		},
		//get URL parms return obj
		getURLParms: function(){
			var _this = BshareLib.fn, url = window.location.href,startIndex = url.lastIndexOf("?"), parmsList = url.substr(startIndex+1,url.length-1);
			return _this.stringToObject(parmsList,"=","&");

		},
		//show elements
		show: function(){
			var _this = BshareLib.fn;
			_this.each(function(){
				_this.setCss({'display':'block'});
			});
		},
		//hide elements
		hide: function(){
			var _this = BshareLib.fn;
			_this.each(function(){
				_this.setCss({'display':'none'});
			});
		},
		//animate
		animate: function(){
			
		},
		//get browser & version
		browser: function(){
			return BshareLib.fn.uaMatch();
			
		},
		//match userAgent for browser
		uaMatch: function() {
			var rwebkit = /(webkit)[ \/]([\w.]+)/,
				ropera = /(opera)(?:.*version)?[ \/]([\w.]+)/,
				rmsie = /(msie) ([\w.]+)/,
				rmozilla = /firefox/;
			ua = navigator.userAgent.toLowerCase();
			var match = rwebkit.exec( ua ) ||
			ropera.exec( ua ) ||
			rmsie.exec( ua ) ||
			ua.indexOf("compatible") < 0 && rmozilla.exec( ua ) || [];
			return { 
				ie: rmsie.test(ua),
				webkit: rwebkit.test(ua),
				firefox: rmozilla.test(ua),
				oprea: ropera.test(ua),
				version: match[2] || "0"
				};
		}
	}
	BshareLib.fn.init.prototype = BshareLib.fn;
	window.BshareLib = window.$$ = BshareLib;

})(window);































































