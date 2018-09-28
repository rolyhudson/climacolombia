var mainimgHeight = 200;
var analysisParams;
var performanceChart;
var popAllDonut;
var popTSDonut;
var popClusterDonut;
function makePage(){
	mainimgHeight = window.innerHeight/4;
	setuplayout();
	runPChartTool();
	showParams();//runMapTool after params
	readData("stats/performanceDF/clusters.json",processPerformance);
	readData("stats/clusterStats.json",processPopulations);
	
}
function readData(file,awaitFn){
d3.queue()
    .defer(d3.text,file )
    .await(awaitFn);
}
function orderPopulation(a, b){
	return a.clusterId-b.clusterId;
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
		data.push({"x":popData[i].clusterId,"y":popData[i].count});
		
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
function insertStrategyText(divID,strategyData){
	var stratDiv = document.getElementById(divID);
	  for(var i=0;i<strategyData.length;i++){
	  	var p = addTextToDiv("p",strategyData[i].percent+"% "+strategyData[i].name+" at "+strategyData[i].count+" points");
	  	
	  	if(strategyData[i].name.includes("No strategy found")) p.style.color = "black";
	  	else p.style.color = strategiesDisplay.find(s=>s.name===strategyData[i].name.replace(/ /g,'_')).color;
	  	stratDiv.appendChild(p);
	  }
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
	div.appendChild(addTextToDiv("p",performanceData[0].NClusters+" selected")); 
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
function showParams(){
	d3.queue()
    .defer(d3.json, "parameters.txt")
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
function setuplayout(){
	var overview = document.getElementById("overview");
	overview.append(addTextToDiv("h1","overview"));
	var rowoverview1 = makeSection();
	 
	var p = makeContentElement("input parameters",1,"parameters","h2","third");
	var a = makeContentElement("",1,"performancechart","h2","third");
	a.style.overflow = "hidden";
	a.appendChild(makeFloatTextDiv("clustering performance","performancetext","h2","right"));
	var c = makeContentElement("",1,"populationchart","h2","third");
	c.style.overflow = "hidden";
	c.appendChild(makeFloatTextDiv("cluster populations","populationstext","h2","right"));
	rowoverview1.appendChild(p);
	rowoverview1.appendChild(a);
	rowoverview1.appendChild(c);
	overview.appendChild(rowoverview1);

	var rowoverview2 = makeSection();
	 
	var p = makeContentElement("",0.5,"strategies","h2","twothirds");
	p.style.overflow = "hidden";
	p.appendChild(makeFloatTextDiv("design strategies psychrometric chart","strategiescharttext","h2","right"));
	var a = makeContentElement("",0.5,"foundstrategies","h2","third");
	a.appendChild(makeFloatTextDiv("found design strategies","strategiestext","h2","left"));
	rowoverview2.appendChild(a);
	rowoverview2.appendChild(p);
	
	overview.appendChild(rowoverview2);
	
	var clusterbrowser = document.getElementById("clusterbrowser");
	clusterbrowser.append(addTextToDiv("h1","cluster explorer"));
var explorerH =0.4;
	var rowexplorer2 = makeSection(); 
	var p = makeContentElement("",explorerH,"mapDiv","h2","third");
	p.style.overflow = "hidden";
	var scalediv = document.createElement("div");
	scalediv.id = "scale";
	p.appendChild(scalediv);
	p.appendChild(makeFloatTextDiv("map","maptext","h2","left"));
	rowexplorer2.appendChild(p);

	var timesteppanel = makeContentElement("",explorerH,"sidePanel","h2","third");
	rowexplorer2.appendChild(timesteppanel);

	var clusterpanel = makeContentElement("",explorerH,"sidePanel","h2","third");
	rowexplorer2.appendChild(clusterpanel);
	clusterbrowser.appendChild(rowexplorer2);
	timeStepPanel(timesteppanel);
	clusterPanel(clusterpanel);
}
function clusterPanel(sidepanel){
	var side1 = makeSection();
	var p = makeContentElement("cluster control",3,"clustercontrol","h2","full");

	side1.appendChild(p);
	sidepanel.appendChild(side1);

	var side3 = makeSection();
	var c = makeContentElement("",0.8,"withinclusterpopulations","h2","full");
	c.appendChild(makeFloatTextDiv("within cluster populations","strategiestext","h2","left"));
	c.style.overflow = "hidden";
	side3.appendChild(c);
	sidepanel.appendChild(side3);

	var side2 = makeSection();
	var a = makeContentElement("within cluster strategies",2,"withinclusterstrategies","h2","full");
	side2.appendChild(a);
	sidepanel.appendChild(side2);

	
}
function timeStepPanel(sidepanel){
	var side1 = makeSection();
	var p = makeContentElement("time step control",3,"control","h2","full");
	side1.appendChild(p);
	sidepanel.appendChild(side1);

	var side3 = makeSection();
	var c = makeContentElement("",0.8,"timesteppopulations","h2","full");
	c.appendChild(makeFloatTextDiv("time step populations","strategiestext","h2","left"));
	c.style.overflow = "hidden";
	side3.appendChild(c);
	sidepanel.appendChild(side3);

	var side2 = makeSection();
	var a = makeContentElement("time step strategies",2,"timestepstrategies","h2","full");
	side2.appendChild(a);
	sidepanel.appendChild(side2);
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