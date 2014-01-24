// NOTE: THIS JS FILE USES JQUERY TOOLS FOR THE POPUP OVERLAY.
// YOU MUST LOAD THE JQUERY TOOL JS FILE BEFORE LOADING THIS JS FILE

function copyToClipboard() {
	// this is IE only!
	var code = document.getElementById("bShare_Code");
	code.select();
	var toCopy = code.createTextRange();
	toCopy.execCommand("Copy");
}

// OFC object that handles menu clicks:
OFC = {};
OFC.jquery = {
    name: "jQuery",
	version: function(src) { return $('#'+ src)[0].get_version(); },
	rasterize: function(src, dst) { $('#'+ dst).replaceWith(OFC.jquery.image(src)); },
	image: function(src) { 
		var flashHtmlObj = $('#'+src)[0];
		var html = "";
		try {
			var imgBinary = flashHtmlObj.get_img_binary();
			html = "<img src='data:image/png;base64," + imgBinary + "' />";
		} catch(e) {
			html = e;
		}
		return html;
	},
	popup: function(src) {
	    var img_win = window.open('');
	    with(img_win.document) {
	    	var imageHtml = OFC.jquery.image(src);
	        write('<html><head><title>' + OFC2MENU_SAVEAS_IMAGE + '</title></head><body>' + OFC2MENU_SAVEIMAGE_NOTE + '<br/><br/>' + imageHtml + '</body></html>'); 
	    }
		// stop the 'loading...' message
	    img_win.document.close();
	}
};
if (typeof(Control == "undefined")) { var Control = {OFC: OFC.jquery}; }

// ofc menu functions:
function save_image(chart_id) {
	if($('#'+ chart_id).length == 0) {
		alert(OFC2MENU_INVALID_CHARTID + " " + chart_id);
		return;
	}
	OFC.jquery.popup(chart_id);
}

var $overlayObj = null;
function save_csv(chart_id) {
	var dataArr;
	var label;
	if(chart_id == "websiteChartViews") {
		dataArr = getOfcDataViews().split("<br/>");
		label = OFC2MENU_VIEWS;
	}
	else if(chart_id == "websiteChartClicks") {
		dataArr = getOfcDataClicks().split("<br/>");
		label = OFC2MENU_CLICKS;
	}
	else if(chart_id == "websiteChartShares") {
		dataArr = getOfcDataShares().split("<br/>");
		label = OFC2MENU_SHARES;
	}
	else if(chart_id == "websiteChartViewToShare") {
		dataArr = getOfcDataViewToShare().split("<br/>");
		label = "分享/浏览";
	}
	else if(chart_id == "websiteChartShareToClickback") {
		dataArr = getOfcDataShareToClickback().split("<br/>");
		label = "回流/分享";
	}
	else if(chart_id == "adChartViews") {
		dataArr = getOfcDataViews().split("<br/>");
		label = OFC2MENU_ADVIEWS;
	}
	else if(chart_id == "adChartClicks") {
		dataArr = getOfcDataClicks().split("<br/>");
		label = OFC2MENU_ADCLICKS;
	}
	else if(chart_id == "adChartIpViews") {
		dataArr = getOfcDataIpViews().split("<br/>");
		label = OFC2MENU_ADVIEWS_UNIQUE;
	}
	else if(chart_id == "adChartIpClicks") {
		dataArr = getOfcDataIpClicks().split("<br/>");
		label = OFC2MENU_ADCLICKS_UNIQUE;
	}
	else if(chart_id == "adChartAggrStats") {
		dataArr = getOfcDataAggrStats().split("<br/>");
		label = OFC2MENU_ADSTATS;
	}
	else if(chart_id == "ratingChartRates") {
		dataArr = getOfcDataRates().split("<br/>");
		label = OFC2MENU_RATES;
	}
	else if(chart_id == "burlChartClicks") {
		dataArr = getOfcDataBurlClicks().split("<br/>");
		label = OFC2MENU_BURLCLICKS;
	}
	else if(chart_id == "beChartBE") {
		// TODO 
		dataArr = getOfcDataBE().split("<br/>");
		label = OFC2MENU_BSHAREEFFECT;
	}
	else {
		alert(OFC2MENU_INVALID_CHARTID + " " + chart_id);
		return;
	}
	
	// prepare csv data:
	var data = '"' + OFC2MENU_DATE + '","' + label + '"';
	for(var i = 0; i < dataArr.length; i++) {
		if(dataArr[i] == "") continue;
		if(i > 0) {
			data += ',"' + dataArr[i].substr(0,dataArr[i].indexOf("\"")) + '"';
		}
		if(i < dataArr.length-1) {
			data += "\n" + '"' + dataArr[i].substr(dataArr[i].lastIndexOf("\"") + 1, dataArr[i].length) + '"';
		}
	}
	$("#bShare_Code").val(data);
	
	// load overlay:
	if ($overlayObj == null) {
		$overlayObj = $(".bOverlay2").overlay({
			top:'20%',
			mask: {
				color: '#666',
				loadSpeed: 200,
				opacity: 0.8
			},
			onLoad: function() {
				var code = document.getElementById("bShare_Code");
				if(code != null) {
					// ie9 will throw exception if overlay is not visible...
					try {code.select();} catch(e){}
				}
			}
		});
	}
	$(".bOverlay2").data("overlay").load();
}

// creates a form and submits it to get the CSV file.
function getCsvFile() {
	var submitForm = document.createElement("FORM");
	document.body.appendChild(submitForm);
	submitForm.method = "POST";

	var hiddenField = document.createElement("INPUT");
	hiddenField.setAttribute("type", "hidden");
    hiddenField.setAttribute("name", "csvData");
    hiddenField.setAttribute("value", $("#bShare_Code").val());
	submitForm.appendChild(hiddenField);
	
	submitForm.action = "saveAsCsvFileAction";
	submitForm.submit();
}

$(function(e) {
	if ($.browser.msie) $("#copyCodeButton").css("display", "block");
});