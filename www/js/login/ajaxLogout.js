
    /**
     * Attaches event to a dom element.
     * @param {Element} el
     * @param type event name
     * @param fn callback This refers to the passed element
     */
     function passport_ajax_logout(url, service, callback) {
    	var logout_url = url + "/logout?ajax=1&service=" + service;
    	if (callback) {
    		logout_url += "&callback=" + callback;
    	}
    	
    	$('body').append($('<iframe/>').attr({  
            style: "display:none;width:0;height:0",   
            id: "ssoLogoutFrame",  
            name: "ssoLogoutFrame",  
            src: logout_url
        }));
    }
    
    function ajaxLogoutCallback(result) {
    	//alert(result);
    	deleteIFrame('#ssoLogoutFrame');
    }
    
    var deleteIFrame = function (iframeName) {  
        var iframe = $(iframeName);   
        if (iframe) { // 鍒犻櫎鐢ㄥ畬鐨刬frame锛岄伩鍏嶉〉闈㈠埛鏂版垨鍓嶈繘銆佸悗閫€鏃讹紝閲嶅鎵ц璇frame鐨勮姹�
            iframe.remove();
        }  
    };  
//})(); 