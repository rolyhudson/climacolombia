var mainimgHeight = 200;
var analysisParams;
var performanceChart;
var popAllDonut;
var popTSDonut;
var popClusterDonut;
function makePage(params){
	mainimgHeight = window.innerHeight/4;
	setuplayout();
	//runPChartTool();

	addPCharts();
	getParams(params);//runMapTool after params
	readData("stats/performanceDF/clusters.json",processPerformance);
	readData("stats/clusterStats/clusters.json",processPopulations);
	
}
function addPCharts(){
	pc1 = new Pchart("alltimestepsallclusterspchart");
}
function readData(file,awaitFn){
d3.queue()
    .defer(d3.text,file )
    .await(awaitFn);
}
function orderPopulation(a, b){
	return a.count-b.count;
}
function processPopulations(error, data){
	tableData = data.split(/\r?\n/);
	var popData=parsePopData(tableData);
	popAllDonut = new Donutchart("populationchart",popData,"populationoverviewdonut",["all clusters"]);
	
}
function parsePopData(tableData){
	var popData=[];
	for(var i=0;i<tableData.length;i++)
	{
	if(tableData[i]!="")popData.push(JSON.parse(tableData[i]))	
	}
	popData.sort(orderPopulation);
	var data=[];
	for(var i=0;i<popData.length;i++){
		if(popData[i].hasOwnProperty("count")){
			data.push({"x":popData[i].clusterId,"y":popData[i].count});
		}
		
		
	}
	return data;
}
function orderStrategy(a, b){
	return b.percent-a.percent;
}
function processStrategies(error, data){
	tableData = data.split(/\r?\n/);
	var strategyData=parseStrategies(tableData);
 insertStrategyText("strategiestext",strategyData);
  
}
function processWithinClusterStrategies(error,data){
	tableData = data.split(/\r?\n/);
	var strategyData=parseStrategies(tableData);
	insertStrategyText("withinclusterstrategies",strategyData);
}
function insertStrategyText(divID,strategyData){
	var stratDiv = document.getElementById(divID);
var cnodes =stratDiv.childNodes; 
while (cnodes.length>1) {
	stratDiv.removeChild(stratDiv.lastChild);
	cnodes =stratDiv.childNodes;
}
	  for(var i=0;i<strategyData.length;i++){
	  	var p = addTextToDiv("p",strategyData[i].percent+"% "+strategyData[i].name+" ("+strategyData[i].count+" points)");
	  	var classname = strategyData[i].name.replace(/ /g,'_');
	  	p.onmouseover = function(){strategyMouseOver(this)};
	  	p.onmouseout = function(){strategyMouseOut(this)};
	  	p.className = classname;
	  	if(strategyData[i].name.includes("No strategy found")) p.style.color = "black";
	  	else p.style.color = strategiesDisplay.find(s=>s.name===classname).color;
	  	stratDiv.appendChild(p);
	  }
}
function strategyMouseOver(x){
	var classname = x.className;
	x.style.fontWeight="bold";
	var zone = document.getElementsByClassName(classname+" strategy");
	var color = strategiesDisplay.find(s=>s.name===classname).color;
	zone[0].setAttribute("fill",color);
	zone[0].setAttribute("opacity",0.5);
}
function strategyMouseOut(x){
	var classname = x.className;
	x.style.fontWeight="normal";
	var zone = document.getElementsByClassName(classname+" strategy");
	zone[0].setAttribute("fill","none");
	zone[0].setAttribute("opacity",1);
}
function processTSStrategies(error, data){
	tableData = data.split(/\r?\n/);
	var strategyData=parseStrategies(tableData);
	insertStrategyText("timestepstrategies",strategyData);
}
function processTSPopulations(error, data){
	tableData = data.split(/\r?\n/);
	var popData=parsePopData(tableData);
	popTSDonut = new Donutchart("timesteppopulations",popData,"populationTSdonut",["all clusters"]);
}
function processClusterStrategies(error, data){
	tableData = data.split(/\r?\n/);
	var strategyData=parseStrategies(tableData);
}
function processClusterPopulations(error, data){
	tableData = data.split(/\r?\n/);
	var popData=parsePopData(tableData);
}
function parseStrategies(tableData){
	var strategyData=[];
	for(var i=0;i<tableData.length;i++)
	{
	if(tableData[i]!="")strategyData.push(JSON.parse(tableData[i]))	
	}
	strategyData.sort(orderStrategy);
	return strategyData;
}
function compare(a, b){
  return a.NClusters - b.NClusters;
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
function processPerformance(error, data){
	tableData = data.split(/\r?\n/);
	var performanceData=[];
	for(var i=0;i<tableData.length;i++)
	{
	if(tableData[i]!="")performanceData.push(JSON.parse(tableData[i]))	
	}
	if(performanceData.length===1){
		var div = document.getElementById("performancetext");
	div.appendChild(addTextToDiv("p",performanceData[0].NClusters+" user selected clusters")); 
	div.appendChild(addTextToDiv("p","cost = "+round(performanceData[0].cost,2)));
	}
	else{
		performanceData.sort(compare);
	var data=[];
	for(var i=0;i<performanceData.length;i++){
		data.push({"x":performanceData[i].NClusters,"y":performanceData[i].cost});
		
	}
	const result = performanceData.findIndex( p => p.selected===true);
	var div = document.getElementById("performancetext");
	div.appendChild(addTextToDiv("p","k optimised at: "+performanceData[result].NClusters+" clusters"));
	div.appendChild(addTextToDiv("p","cost = "+round(performanceData[result].cost,2)));
	lineGraph("performancechart",data,"k clusters","cost",result ); 
	}
	
	
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
    				else
    				{
    				document.getElementById("parameters").append(addTextToDiv("p",k + " -> " + analysisParams[k]));
    			}
    			
    			}
    		}
    		
    	}

        
    }
}
runMapTool("mapDiv");
}
function setupOverview(){
	var overview = document.getElementById("overview");
	overview.append(addTextToDiv("h1","overview"));
	var rowoverview1 = makeSection();
	var param = makeContentElement("input parameters",1,"parameters","h2","1/5");
	var sil = makeContentElement("",1,"performanceSil","h2","1/5");
	sil.style.overflow = "hidden";
	var dunn = makeContentElement("",1,"performanceDunn","h2","1/5");
	dunn.style.overflow = "hidden";
	var wssse = makeContentElement("",1,"performanceWssse","h2","1/5");
	wssse.style.overflow = "hidden";
	rowoverview1.appendChild(param);
	rowoverview1.appendChild(sil);
	rowoverview1.appendChild(dunn);
	rowoverview1.appendChild(wssse);
	
	overview.appendChild(rowoverview1);
}
function setupClusterBrowser(){
	var clusterbrowser = document.getElementById("clusterbrowser");
	clusterbrowser.append(addTextToDiv("h1","cluster explorer"));
	var rowexplorer1 = makeSection();
	var p = makeContentElement("",0.4,"timestepTabs","h2","5/5");
	p.style.overflow = "hidden";
	
	//addtab panel
	p.appendChild(tabButtonsDiv(["all time steps","single time step"],clickTimeStepTab,["alltimesteps","singletimestep"],"timeTabContent"));
	addContentTab(["alltimesteps","singletimestep"],p,"timeTabContent");
	rowexplorer1.appendChild(p);
	clusterbrowser.appendChild(rowexplorer1);
	//add contents to tabs
	insertTimeTabContents(["alltimesteps","singletimestep"],0.5);

}
function insertTimeTabContents(tabIDs,h){
	for(var i=0;i<tabIDs.length;i++){
	var tabsection = makeSection();
	var map = makeContentElement(tabIDs[i]+" map",h,tabIDs[i]+"mapDiv","h2","2/5");
	map.style.overflow = "hidden";
	var clustertabs = makeContentElement(tabIDs[i]+" cluster control",h,tabIDs[i]+"clustertabs","h2","3/5");
	clustertabs.style.overflow = "hidden";
	var clusterTabIDs = [tabIDs[i]+"allclusters",tabIDs[i]+"singlecluster"]
	clustertabs.appendChild(tabButtonsDiv(["all clusters","single cluster"],clickTimeStepTab,clusterTabIDs,"clusterTabContent"));
	addContentTab(clusterTabIDs,clustertabs,"clusterTabContent");

	tabsection.appendChild(map);
	tabsection.appendChild(clustertabs);
	document.getElementById(tabIDs[i]).appendChild(tabsection);
	//add cluster tab contents
	insertClusterTabContents(clusterTabIDs);
	}
	
}
function insertClusterTabContents(tabIDs){
	for(var i=0;i<tabIDs.length;i++){
		var tabsection = makeSection();
		if(tabIDs[i].includes("all")){
			var pop = makeContentElement(tabIDs[i]+" pop",1,tabIDs[i]+"pop","h2","third");
			pop.style.overflow = "hidden";
			tabsection.appendChild(pop);
		}
		else{
			var control = makeContentElement(tabIDs[i]+" control",1,tabIDs[i]+"control","h2","third");
			control.style.overflow = "hidden";
			tabsection.appendChild(control);
		}
		var pchart = makeContentElement(tabIDs[i]+" pchart",1,tabIDs[i]+"pchart","h2","twothirds");
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
	if(i===0) tabcontent.style.display ="block";
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
;
	setupOverview();
	setupClusterBrowser();
	eventFire(document.getElementById("alltimesteps"), 'click');
	eventFire(document.getElementById("alltimestepsallclusters"), 'click');

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
function eventFire(el, etype){
  if (el.fireEvent) {
    el.fireEvent('on' + etype);
  } else {
    var evObj = document.createEvent('Events');
    evObj.initEvent(etype, true, false);
    el.dispatchEvent(evObj);
  }
}