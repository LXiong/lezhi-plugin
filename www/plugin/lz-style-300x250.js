(function(){function D(e,c){var a,b="",i,k=j.createElement("div","","bShareRecommDiv2");a=j.createElement("div","","bShareRecommDiv3");e.appendChild(k);k.appendChild(a);if(c>1){i=j.createElement("div","","bShareRecommPages");for(a=0;a<c;a++)b+='<span onclick="window.lezhi.switchPage(this,'+a+',2,0)" class="'+(a==0?"on":"")+'"></span>';i.innerHTML=b;k.appendChild(i)}}function E(m,c,a){var b=e.modules.config[m],i=b.position,k=b.fontSize,p=b.bdcolor,f=b.rtcolor,h=b.lineHeight,o=b.picBorderRadius,d=b.bgcolor,
q=b.fontBold,s=b.fontUnderline,b=b.linkUnderline,g=c+"{width:300px;height:250px;z-index:99999;overflow:hidden !important;margin:0 !important;padding:0 !important;}"+c+" .bShareRecommDiv2{position:relative;margin:0 !important;padding:0 !important;}.lz-module:after,.bShareRecommDiv2:after,.bShareRecommDiv3:after,.b-recomm-panel:after,.b-recomm-tab:after{content:'.';display:block;height:0;clear:both;visibility:hidden;overflow:hidden;}";g+=c+" .bShareRecommDiv3{overflow:hidden;position:relative;left:0;width:300px;border:0px solid "+
(p?p:"#dadada")+";"+(d?"background:"+d+";":"")+"}";g+=c+" .b-recomm-panel{width:"+300*a+"px;display:none;margin:0 !important;overflow:hidden;padding:0 !important;float:left;position:relative;left:0;border:none;";g+="clear:both;}"+c+" .b-recomm-list{height:250px;width:300px;padding:0 !important;margin:0 !important;"+(a>0?"float:left;":"")+"list-style:disc inside none;}";g+=c+" .b-recomm-list li{padding:0;display:block;text-align:center;text-indent:0 !important;"+(q?"font-weight:bold;":"")+"width:148px;height:248px;overflow:hidden;border:1px solid "+
d+";cursor:pointer;list-style:none !important;float:left;margin:0;}";g+=c+" .b-recomm-list a{font-size:"+k+"px;margin:0 !important;list-style:disc inside none !important;position:relative;text-decoration:"+(s?"underline":"none")+" !important;display:block;clear:both;overflow:hidden;color:"+f+";}"+c+" .b-recomm-list a:hover{border-ratio:5px;text-decoration:"+(b?"underline":"none")+" !important;}#bShareRecommTag"+m+"{margin:0;padding:0;background:url("+LEZHI_STATIC_BASE+"/images/recommend.png) no-repeat;width:40px;height:40px;position:fixed;bottom:60px;*bottom:66px;";
g+=(i=="left"?"left:10px;right:auto;":"right:10px;left:auto;")+"z-index:999999;cursor:pointer;}";a>0&&(g+=c+" .bShareRecommPages{text-align:center;position:absolute !important;width:300px;bottom:8px;}"+c+" .bShareRecommPages span{display:inline-block;padding:0;margin:0 6px;cursor:pointer;width:11px;height:11px;background:url("+l.lezhi.variables.PAGEOFF_PIC_URL+") no-repeat;}"+c+" .bShareRecommPages span.on{background:url("+l.lezhi.variables.PAGEON_PIC_URL+") no-repeat;}");g+=c+" .b-recomm-list a:hover .feed-title{text-decoration:"+
(b?"underline":"none")+" !important;}";g+=c+" .b-recomm-list .feed-title{cursor:pointer;overflow:hidden;word-wrap:break-word;word-break:break-all;padding:0 !important;margin:10px !important;font-size:"+k+"px;"+(h?"height:"+(h+1)*3+"px;line-height:"+(h+1)+"px;":"")+"text-decoration:"+(s?"underline":"none")+" !important;"+(f?"color:"+f+";":"")+"}";g+=c+" .b-recomm-list .hot-tag{padding:0 !important;margin:0 !important;position:absolute;right:10px;top:29px;width:59px;height:59px;background:url("+l.lezhi.variables.HOT_PIC_URL+
") no-repeat;z-index:1;}"+c+" .b-recomm-list img{padding:3px !important;display:inline;margin:0 !important;position:relative;top:4px;float:none !important;border:none !important;"+(o?"border-radius:5px;":"")+"}";g+=c+" #"+e.variables.ADS_CONTENT_ID+" img{margin:0px !important;padding:0px !important;left:0px !important;top:0px !important;border:0px !important;background:none !important;border-radius: 0px !important;}";return j.loadStyle(g)}var l=window,q=encodeURIComponent,e=l.lezhi,m=e.variables,
j=e.util;e.render300x250=function(l,c){for(var a=0;a<e.modules.nodes.length;a++)if(e.modules.nodes[a].lezhiStyle==m.STYLETYPE_300x250){var b=e.modules.nodes[a],i,k,p,f,h,o=0,d=e.modules.config[a],z="."+m.STYLETYPE_300x250,s=d.count,g=d.highlight,r,t,n,A,F=!!d.adPreload,v=!!d.adEnabled;b.idConfig&&(z="#"+b.id);d.source="insite";m.adsIds[a]=[];r=Math.ceil(s/2)+ +v;if(r<=0)break;D(b,r);for(h in e.modules.sourceCount[a])for(n in l)if(h===n){A=!0;f="";o=0;i=l[n];k=i.length;n=n.split(":")[0];p=j.createElement("div",
"","b-recomm-panel activated","display:block");p.setAttribute("sourceType",n);e.modules.currentActivated[a]=n;e.stats.view(n,a);for(t=0;t<r;t++){f+='<ul class="b-recomm-list">';if(t==0&&v){if(F){var w=0,B=function(){m.adsPreloadDone[a]&&(f+=j.getElemById(m.ADS_PRELOAD_DIV_ID+a).innerHTML,w=30,b.removeChild(j.getElemById(m.ADS_PRELOAD_DIV_ID+a)));w>=30||(++w,setTimeout(B,100))};B()}}else for(var G=(t+1-+v)*2;o<G&&o<k&&o<s;++o){var u=i[o],x=u.url,y=u.title,H=d.hvcolor,C=g&&u.mark;if(!u.ad){h=LEZHI_PLUGIN_BASE+
"/redirect?to="+q(x)+"&from="+d.url+"&sitePrefix="+q(d.sitePrefix)+"&ref="+n+"&uuid="+q(d.uuid)+"&type="+q(d.type)+"&pic=true&title="+q(d.title);f+="<li onmouseover=\"this.style.background='"+H+"'\" onmouseout=\"this.style.background='none'\"";f+='><a href="'+x+'" onmousedown="this.href=\''+h+"';return true;\" onmouseup=\"t=this;setTimeout(function(){t.href='"+x+'\'},50);return true;" target="'+d.redirectType+'" title="'+y+'"';f+='" hidefocus="true" style="padding:25px 0 !important;">';if(!(h=u.pic))if(h=
d.defaultPic,!h)h=e.variables.DEFAULT_PIC_URL;C&&(f+='<div class="hot-tag"></div>');f+='<img class="bshare-logos" src="'+h+'" style="width:120px !important;height:120px !important;"/><div class="feed-title" title="'+j.htmlEncode(y)+'"';C&&(f+='style="color:red;"');f+=">"+y+"</div></a></li>"}}f+="</ul>"}p.innerHTML=f;j.getElemByClassName(b,"div","bShareRecommDiv3")[0].appendChild(p)}if(A)b.styleObj&&b.styleObj.parentNode.removeChild(b.styleObj),b.styleObj=E(a,z,r),r>1&&(c||e.autoPage(a,r,2,d.autoPageTime,
0))}};l.lezhi.render300x250(l.lezhi.variables.recommendation)})();