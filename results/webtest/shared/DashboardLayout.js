var mainimgHeight = 200;
var analysisParams;
var performanceChart;
var popAllDonut;
var popTSDonut;
var popClusterDonut;
var popMonthlyDonut;
var pc1,pc2,pc3,pc4,pc5,pc6;
var allclustersstats=[];
var singleTimeStepMap;
var allTimeStepMap;
var monthlyTypicalMap;
function makePage(params){
	mainimgHeight = window.innerHeight/4;
	setuplayout();
	
	getStrategiesZones();
	getParams(params);//runMapTool and thermal maps after params
	readData("stats/performanceDF/clusters.json",processPerformance);
}
function allTimeStepAllClusterInfo(){
	//all time all clusters only loaded once
	readData("stats/typicalYear/clusters.json",readTypicalYear);
	readData("stats/clusterStats/clusters.json",processAllTimeAllClusterPop);
	readData("stats/strategyStats/clusters.json",processAllTimeAllClusterStrategies);
	readData("../shared/ColombianCities.csv",readCities);
}
function addPCharts(){
	
	pc1 = new Pchart("alltimestepsallclusterspchart",strategiesDisplay,"pc1",0,0);
	pc2 = new Pchart("alltimestepssingleclusterpchart",strategiesDisplay,"pc2",pc1.w,pc1.h);
	pc3 = new Pchart("singletimestepallclusterspchart",strategiesDisplay,"pc3",pc1.w,pc1.h);
	pc4 = new Pchart("singletimestepsingleclusterpchart",strategiesDisplay,"pc4",pc1.w,pc1.h);
	pc5 = new Pchart("monthlytypicalallclusterspchart",strategiesDisplay,"pc5",pc1.w,pc1.h);
	pc6 = new Pchart("monthlytypicalsingleclusterpchart",strategiesDisplay,"pc6",pc1.w,pc1.h);
	//getFoundStrategies();
	allTimeStepAllClusterInfo();
	
}
function readData(file,awaitFn){
d3.queue()
    .defer(d3.text,file )
    .await(awaitFn);
}

function makeFloatTextDiv(text,textid,titlesize,align)
{
	var ele = document.createElement("div");
	ele.className="floatText";
	ele.id = textid;
	ele.style.textAlign = align;
	var title = document.createElement(titlesize);
    var node = document.createTextNode(text);
    title.appendChild(node);
    ele.appendChild(title);
	return ele;
}

function getParams(paramsFile){
	d3.queue()
    .defer(d3.json, paramsFile)
    .await(processParams);
}
function processParams(error, data){
	console.log(data);

	for (var key in data) {
    if (data.hasOwnProperty(key)) {
    	if(key==="name"){
    		document.getElementById("parameters").append(addTextToDiv("p",key + " -> " + data[key]));
    	}
    	if(key==="analysisParameters"){
    		analysisParams = data[key];
    		var text="";
    		for (var k in analysisParams) {
    			if (analysisParams.hasOwnProperty(k)) {
    				if(k.includes("Date")){
    					var date = analysisParams[k];
    					var year = date["year"];
    					var month = date["month"];
    					var day = date["dayOfMonth"];
    					document.getElementById("parameters").append(addTextToDiv("p",k + " -> " + day+" "+month+" "+year));
    				}
    				else{
    				document.getElementById("parameters").append(addTextToDiv("p",k + " -> " + analysisParams[k]));
    				}
    			}
    		}
    	}
    }
}

runExplorerMapTool();

}
function set3dView(divid){
	var viewer = document.getElementById(divid);
	return [viewer.clientWidth,viewer.clientHeight];
}
function setupThermalComparision(){
	var mapH=0.9;
	var comfortComp = document.getElementById("comfortcomparison");
	comfortComp.append(addTextToDiv("h1","comfort comparison"));
	var row1 = makeSection();
	var title = makeContentElement("Universal thermal comfort index (UTCI)",mapH,"utci","h2","1/5");
	var predicted = makeContentElement("predicted UTCI by cluster",mapH,"predictedutci","p","1/5");
	var calculated = makeContentElement("calculated UTCI by grid cell",mapH,"calculatedutci","p","1/5");
	var delta = makeContentElement("delta UTCI",mapH,"deltautci","p","1/5");
	var stats = makeContentElement("stats UTCI",mapH,"statsutci","p","1/5");
	predicted.style.overflow = "hidden";
	calculated.style.overflow = "hidden";
	delta.style.overflow = "hidden";
	 title.appendChild(addTextToDiv("p","The UTCI is a thermal comfort indicator based on human heat balance models and designed to be applicable in all seasons and climates and for all spatial and temporal scales"));
	 
	title.appendChild(addTextToDiv("p","            ci >= 46 extreme heat stress")); 
	title.appendChild(addTextToDiv("p","ci >= 38 && ci <  46 very strong heat stress")); 
	title.appendChild(addTextToDiv("p","ci >= 32 && ci <  38 strong heat stress")); 
	title.appendChild(addTextToDiv("p","ci >= 26 && ci <  32 moderate heat stress")); 

	title.appendChild(addTextToDiv("p","ci >= 9  && ci <  26 no thermal stress")); 
	title.appendChild(addTextToDiv("p","ci >= 0  && ci <  9 slight cold stress")); 
	title.appendChild(addTextToDiv("p","ci >=-13 && ci <  0 moderate cold stress")); 
	title.appendChild(addTextToDiv("p","ci >=-27 && ci < -13 strong cold stress")); 
	title.appendChild(addTextToDiv("p","ci >=-40 && ci < -27 very strong cold stress")); 
	title.appendChild(addTextToDiv("p","            ci < -40 extreme cold stress"));
	title.appendChild(onelineLink("More details","http://www.utci.org/"));
	row1.appendChild(title);
	row1.appendChild(predicted);
	row1.appendChild(calculated);
	row1.appendChild(delta);
	row1.appendChild(stats);

	var row2 = makeSection();
	var title = makeContentElement("IDEAM Comfort Index (IDEAMCI)",mapH,"ideamci","h2","1/5");
	var predicted = makeContentElement("predicted IDEAMCI by cluster",mapH,"predictedideamci","p","1/5");
	var calculated = makeContentElement("calculated IDEAMCI by grid cell",mapH,"calculatedideamci","p","1/5");
	var delta = makeContentElement("delta IDEAMCI",mapH,"deltaideamci","p","1/5");
	var stats = makeContentElement("stats IDEAMCI",mapH,"statsideamci","p","1/5");
	predicted.style.overflow = "hidden";
	calculated.style.overflow = "hidden";
	delta.style.overflow = "hidden";

	title.appendChild(addTextToDiv("p","Thermal sensation indicator developed by Colombia's Institute of Hydrology, Meteorology and Environmental Studies (IDEAM)."));
	 
	title.appendChild(addTextToDiv("p","           ci <= 3 very hot"));
    title.appendChild(addTextToDiv("p","ci > 3  && ci <= 5 hot"));
    title.appendChild(addTextToDiv("p","ci > 5  && ci <= 7 warm"));
    title.appendChild(addTextToDiv("p","ci > 7  && ci <= 11 comfortable"));
    title.appendChild(addTextToDiv("p","ci > 11 && ci <= 13 somewhat cold"));
    title.appendChild(addTextToDiv("p","ci > 13 && ci <= 15 cold"));
    title.appendChild(addTextToDiv("p","		   ci >  15 very cold"));
    title.appendChild(onelineLink("More details","http://documentacion.ideam.gov.co/openbiblio/bvirtual/007574/Metodologiaconfort.pdf"));
	row2.appendChild(title);
	row2.appendChild(predicted);
	row2.appendChild(calculated);
	row2.appendChild(delta);
	row2.appendChild(stats);

	comfortComp.appendChild(row1);
	comfortComp.appendChild(row2);
}
function setupOverview(){
	var overview = document.getElementById("overview");
	overview.append(addTextToDiv("h1","overview @"+window.location.href));
	var rowoverview1 = makeSection();
	var param = makeContentElement("input parameters",1,"parameters","h2","1/5");
	var sil = makeContentElement("Silhouette coefficient",1,"performanceSil","p","1/5");
	sil.style.overflow = "hidden";
	var dunn = makeContentElement("Dunn index",1,"performanceDunn","p","1/5");
	dunn.style.overflow = "hidden";
	var wssse = makeContentElement("Within Set Sum of Squared Errors",1,"performanceWssse","p","1/5");
	wssse.style.overflow = "hidden";
	var pother = makeContentElement("",1,"performanceOther","p","1/5");
	wssse.style.overflow = "hidden";
	rowoverview1.appendChild(param);
	rowoverview1.appendChild(sil);
	rowoverview1.appendChild(dunn);
	rowoverview1.appendChild(wssse);
	rowoverview1.appendChild(pother);
	overview.appendChild(rowoverview1);
}
function onelineLink(name, href)
{
    var link = document.createElement("a");
    link.innerHTML=name;
    link.href = href;
	return link;
}
function setupClusterBrowser(){
	var clusterbrowser = document.getElementById("clusterbrowser");
	clusterbrowser.append(addTextToDiv("h1","cluster explorer"));
	var timeTabs = ["alltimesteps","singletimestep","monthlytypical"];
	var rowexplorer1 = makeSection();
	//addtab panel
	var tabb = tabButtonsDiv(["all time steps","single time step","monthly typical"],clickTimeStepTab,timeTabs,"timeTabContent");
	rowexplorer1.appendChild(tabb);
	addContentTab(timeTabs,rowexplorer1 ,"timeTabContent");
	
	clusterbrowser.appendChild(rowexplorer1);
	//add contents to tabs
	insertTimeTabContents(timeTabs,0.5);
	//select start tabs
	//set selected time tab
	var selected = document.getElementsByClassName("timeTabContent tablinks");
	selected[0].className += " active";
	// //cluster tabs all clusters
	var selected = document.getElementsByClassName("clusterTabContent tablinks");
	selected[0].className += " active";
	selected[2].className += " active";


	
}
function insertTimeTabContents(tabIDs,h){
	for(var i=0;i<tabIDs.length;i++){
	var tabsection = makeSection();
	var map = makeContentElement("",h,tabIDs[i]+"mapDiv","h2","2/5");
	map.style.overflow = "hidden";
	var scalediv = document.createElement("div");
	scalediv.id = tabIDs[i]+"mapDiv"+"scale";
	map.appendChild(scalediv);
	var clustertabs = makeContentElement("",h,tabIDs[i]+"clustertabs","h2","3/5");
	
	var clusterTabIDs = [tabIDs[i]+"allclusters",tabIDs[i]+"singlecluster"];
	var tabb = tabButtonsDiv(["all clusters","single cluster"],clickTimeStepTab,clusterTabIDs,"clusterTabContent "+i);
	clustertabs.appendChild(tabb);
	addContentTab(clusterTabIDs,clustertabs,"clusterTabContent "+i);

	tabsection.appendChild(map);
	tabsection.appendChild(clustertabs);
	document.getElementById(tabIDs[i]).appendChild(tabsection);
	//add cluster tab contents
	insertClusterTabContents(clusterTabIDs,0.55);
	}
	
}
function insertClusterTabContents(tabIDs,h){
	for(var i=0;i<tabIDs.length;i++){
		var tabsection = makeSection();
		if(tabIDs[i].includes("all")){
			var pop = makeContentElement("",h,tabIDs[i]+"control","h2","third");
			pop.style.overflow = "hidden";
			tabsection.appendChild(pop);
		}
		else{
			var control = makeContentElement("",h,tabIDs[i]+"control","h2","third");
			control.style.overflow = "hidden";
			tabsection.appendChild(control);
		}
		var pchart = makeContentElement("",h,tabIDs[i]+"pchart","h2","twothirds");
			pchart.style.overflow = "hidden";
			tabsection.appendChild(pchart);

		document.getElementById(tabIDs[i]).appendChild(tabsection);
	}
}
function tabButtonsDiv(text,onlclickfn,divids,tabcontentClass){
	var tabbuttons = document.createElement("div");
	for(var i=0;i<text.length;i++){
		var button = document.createElement("button");
		button.addEventListener("click", onlclickfn);
		button.tabname = divids[i]; 
		button.tabclass = tabcontentClass;
		var t = document.createTextNode(text[i]);
		button.appendChild(t);
		button.className = tabcontentClass+ " tablinks";
		tabbuttons.appendChild(button);
	}
	tabbuttons.className="tab";
	return tabbuttons;
}
function addContentTab(divids,parentdiv,tabcontentClass){
	for(var i=0;i<divids.length;i++){
	var tabcontent= document.createElement("div");
	tabcontent.id = divids[i];
	tabcontent.className = tabcontentClass+" tabcontent";
	if(i===0) tabcontent.style.display = "block";
	parentdiv.appendChild(tabcontent);

	}
}
function clickTimeStepTab(event){
 //window.alert( event.target.tabname );
 var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName(event.target.tabclass+" tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName(event.target.tabclass+" tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(event.target.tabname).style.display = "block";
    event.currentTarget.className += " active";

}

function setuplayout(){
	setupOverview();
	setupClusterBrowser();
	
    setupThermalComparision();
}

function makeSection()
{
	var ele = document.createElement("div");
	ele.className="section group";
	return ele;
}
function makeContentElement(title,scale, id,titlesize,colType)
{
	
	if(colType==="third")var contentCol = makeContentCol("col span_1_of_3",mainimgHeight/scale,id);
	if(colType==="full")var contentCol= makeContentCol("col span_3_of_3",mainimgHeight/scale,id);
	if(colType==="twothirds")var contentCol= makeContentCol("col span_2_of_3",mainimgHeight/scale,id);

	if(colType==="1/5")var contentCol = makeContentCol("col span_1_of_5",mainimgHeight/scale,id);
	if(colType==="2/5")var contentCol= makeContentCol("col span_2_of_5",mainimgHeight/scale,id);
	if(colType==="3/5")var contentCol= makeContentCol("col span_3_of_5",mainimgHeight/scale,id);
	if(colType==="4/5")var contentCol= makeContentCol("col span_4_of_5",mainimgHeight/scale,id);
	if(colType==="5/5")var contentCol= makeContentCol("col span_5_of_5",mainimgHeight/scale,id);
    if(title!="")contentCol.appendChild(addTextToDiv(titlesize,title));
    
    return contentCol;
}
function makeContentCol(classType,maxH,id)
{
	var contentCol = document.createElement("div");
	contentCol.className=classType;
	contentCol.style.height = maxH+"px";
	if(id!="")contentCol.id=id;
	return contentCol;
}
function addTextToDiv(tType,text)
{
	
	var p = document.createElement(tType);
	var node = document.createTextNode(text);
	p.appendChild(node);
	return p;
}


	function addRangeSlider(divID,title,selectorID,onChangeFn,start,end,step,optionSet,classN,style,units){

if(units==null) units="";
  var control = document.getElementById(divID);
  var p = document.createElement("p");
    var x = document.createElement("INPUT");
    x.setAttribute("type", "range");
    x.setAttribute("class", selectorID);
    x.addEventListener("change", onChangeFn);
    x.setAttribute("className",classN);
    x.setAttribute("value",optionSet);
    x.setAttribute("min",start);
    x.setAttribute("max",end);
    x.setAttribute("step",step);
    x.style.width = "100px";
    x.style.height = "10px";
    p.appendChild(x);
    p.appendChild(makeTextID("comTitle"," "+title+": ",style));
    p.appendChild(makeTextID(title+"value",optionSet+" "+units,style));
    
    control.appendChild(p);
   
    
}
function makeTextID(id,text,style){
  var x = document.createElement(style);
    //x.setAttribute("id",id);
    x.className =id;
    var t = document.createTextNode(text);
    x.appendChild(t);
    return x;
}
function updateText(text,id){
  var h = document.getElementsByClassName(id);
  var t = document.createTextNode(text);
  h[0].replaceChild(t, h[0].childNodes[0]);
}