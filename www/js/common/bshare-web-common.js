/**
 * Common js file for bshare website.
 * - Status message
 * - Help popup
 * - button styling
 */

// Displays status messages
var statusMessageTimeout = 0;
function displayStatusMessage(message, type) {
	var color = "#333", bgcolor = "#fff8cc", delay = 7000;
	if(type == "error") {
		delay = 30000;
		color = "red";
		bgcolor = "#FFD9D9";
	} else if(type == "success") {
		delay = 7000;
		color = "green";
		bgcolor = "#DDF8CC";
	} else if(type == "info"){
		delay = 10000;
		color = "#333";
		bgcolor = "#fff8cc";
	}

	$("#notifications .notification-msg").css("color", color);
	$("#notifications .notification-bar-bkg").css("background", bgcolor);
	if($("#notifications").is(":visible")) {
		$("#notifications").hide();
	}
	$("#notifications .notification-msg").html(message);
	$("#notifications").show();
	// handle overflows:
	$("#notifications .notification-bar-bkg").height($("#notifications .notification-bar").height());
	
	if (statusMessageTimeout != 0) {
		clearTimeout(statusMessageTimeout);
	}
	statusMessageTimeout = setTimeout(function() { 
		$("#notifications").slideUp(150);
		$("#notifications .notification-msg").html(""); 
	}, delay);
}
function hideStatusMessage() {
	$("#notifications").hide();
	$("#notifications .notification-msg").html("");
	$(".field-error-messages").hide();
	$(".errorInput").hide();
}

function setReturnHandler($obj, returnHandlerFn) {
	$obj.keypress(function(e) {
		if (e.keyCode == 13) {
			e.stopPropagation();
			returnHandlerFn();
			return false;
	    }
		e.stopPropagation();
	});
}

//breadcrumbs generator
function setBreadCrumbs(bcs, bcsl) {
	var bcHtml = genLink(bcs[0], bcsl[0]);
	for (var i = 1; i < bcs.length; i++) {
		bcHtml += "<img style='margin:0 3px;' alt='>' src='" + BSHARE_STATIC_HOST + "/images/arrow-right-grey2.gif'/>" + genLink(bcs[i], bcsl[i], (i === bcs.length-1));
	}
	$("#breadCrumbs").html(bcHtml);
}
function genLink(label, link, isBold) {
	return '<a class="bLink" style="' + (isBold?'color:#333;font-weight:bold;':'color:#666;') + '" title="' + label + '" href="' + link + '">' + label + '</a>';
}

$(function(){
	// Prepare for status message
	var $statusMsgDiv = $("#status-messages-div");
	var $errMegDiv = $("#field-error-messages-div");
	if($statusMsgDiv.text()) {
        if($(".action-error-messages").text()) {
            displayStatusMessage($statusMsgDiv.html(), "error");
        } else if($(".action-messages").text()) {
            displayStatusMessage( $statusMsgDiv.html(), "success");
        }
    }
    if($errMegDiv.text()) {
    	if ($(".field-error-messages").text()) {
    		displayStatusMessage($errMegDiv.html(), "error");
    	}
    	//$errMegDiv.show();
    }
    
    // floating notification system
    $("#notifications .notification-bar").click(function() {
    	$("#notifications").slideUp(150);
    });
	
    // Global ajax error handling
    $("#status-messages").ajaxError(function(event, request, settings) {
		if (settings.url.indexOf('php/rssFetcher.php?url=') != 1) {
			if (typeof initNewsBar == "function") initNewsBar();
			return;
		}
		displayStatusMessage("无法连接服务器，请尝试刷新页面或者稍后再试！", "error");
	});
	
	// top menu nav
    try {
		$(".top-button").tooltip({
			tipClass: 'top-menu-popup',
			position: 'bottom right',
			//opacity: 0.9,
			onShow: function() {
				var trig = this.getTrigger();
				trig.css("background-color", "#f90");
				trig.css("color", "#fff").addClass("div-shadow-5");
				trig.children(".arrow-down").addClass("arrow-down-on");
				var trigW = trig.width() + 30;
				var tipOff = this.getTip().offset().left;
				this.getTip().css("left", tipOff - trigW);
			},
			onHide: function() {
				var trig = this.getTrigger();
				trig.css("background-color", "#fff");
				trig.css("color", "#666").removeClass("div-shadow-5");
				trig.children(".arrow-down").removeClass("arrow-down-on");
			}
		});
    } catch(e) { }
	
	// init help popups
	$("span.help-popup-img[title]").tooltip({
		tipClass: 'helpPopupBoxes',
		position: 'top right',
		delay: 100,
		layout: '<div><span></span></div>'
	});
	
	// search box handlers
	$("input.bSearchBox").focus(function(e) {
		if ($(this).val() == SEARCH_BOX_DEFAULT_TEXT) {
			$(this).css("color", "#000").val('');
		}
		$(this).parent().parent().parent().addClass("focused");
	}).blur(function(e) {
		if ($(this).val() == '') {
			$(this).css("color", "#aaa").val(SEARCH_BOX_DEFAULT_TEXT);
			$(this).parent().parent().parent().children(".bSearchBox-reset").hide();
		} else {
			$(this).parent().parent().parent().children(".bSearchBox-reset").show();
		}
		$(this).parent().parent().parent().removeClass("focused");
	}).blur();
	$("a.bSearchBox-reset").click(function(e) {
		$(this).parent().find("#searchBox").val('');
		$(this).parent().find(".bSearchBox").focus();
		if (typeof onClearSearchBoxCallback == "function") onClearSearchBoxCallback($(this));
		//$(this).parent().find(".bSearchBox").blur();
	});
	
	// placeholder code (remove when all browsers support HTML5)
	$('[placeholder]').focus(function() {
		var input = $(this);
		if (input.val() == '' || input.val() == input.attr('placeholder')) {
			input.val('');
		    input.removeClass('bsPlaceholder');
		}
	}).blur(function() {
		var input = $(this);
		if (input.val() == '' || input.val() == input.attr('placeholder')) {
		    input.addClass('bsPlaceholder');
		    input.val(input.attr('placeholder'));
		}
	}).blur();
	$('[placeholder]').parents('form').submit(function() {
		$(this).find('[placeholder]').each(function() {
		    var input = $(this);
		    if (input.val() == input.attr('placeholder')) {
		    	input.val('');
		    }
		})
	});
	
	// ie6 fixes for select boxes appearing over overlays
	$(".bOverlay2").bgiframe();
	$(".bModal").bgiframe();
	//$(".top-menu-popup").bgiframe();
	//$(".helpPopupBoxes").bgiframe(); dunno why this doesn't work?
});