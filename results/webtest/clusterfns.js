var mainimgHeight = 200;
var analysisParams;
var performanceChart;
function makePage(){
	mainimgHeight = window.innerHeight/6;
	setuplayout();
	runPChartTool();
	showParams();//runMapTool after params
	showPerformance();
	showPopulationOverview();
	
}
function showPopulationOverview(){
d3.queue()
    .defer(d3.text, "stats/clusterStats.json")
    .await(processPopulations);
}
function orderPopulation(a, b){
	return a.clusters-b.clusters;
}
function processPopulations(error, data){
	tableData = data.split(/\r?\n/);
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

	lineGraph("populationchart",data,"cluster #","population","all" );
}
function showStrategiesOverview(){
d3.queue()
    .defer(d3.text, "stats/strategyStats.json")
    .await(processStrategies);
}
function orderStrategy(a, b){
	return b.percent-a.percent;
}
function processStrategies(error, data){
	tableData = data.split(/\r?\n/);
	var strategyData=[];
	for(var i=0;i<tableData.length;i++)
	{
	if(tableData[i]!="")strategyData.push(JSON.parse(tableData[i]))	
	}
	strategyData.sort(orderStrategy);
  var stratDiv = document.getElementById("strategiestext");
  for(var i=0;i<strategyData.length;i++){
  	var p = addTextToDiv("p",strategyData[i].percent+"% "+strategyData[i].name+" at "+strategyData[i].count+" points");
  	
  	if(strategyData[i].name.includes("No strategy found")) p.style.color = "black";
  	else p.style.color = strategiesDisplay.find(s=>s.name===strategyData[i].name.replace(/ /g,'_')).color;

  	stratDiv.appendChild(p);
  }

}
function showPerformance(){
	//check file ending on server
	d3.queue()
    .defer(d3.text, "stats/performanceDF/clusteringPerformance.json")
    .await(processPerformance);
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
	performanceData.push(JSON.parse(tableData[i]))	
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
	a.appendChild(makeFloatTextDiv("clustering performance","performancetext","h2","right"));
	var c = makeContentElement("",1,"populationchart","h2","third");
	c.appendChild(makeFloatTextDiv("cluster populations","populationstext","h2","right"));
	rowoverview1.appendChild(p);
	rowoverview1.appendChild(a);
	rowoverview1.appendChild(c);
	overview.appendChild(rowoverview1);

	var rowoverview2 = makeSection();
	 
	var p = makeContentElement("",0.5,"strategies","h2","twothirds");
	p.style.overflow = "hidden";
	p.appendChild(makeFloatTextDiv("design strategies psychrometric chart","strategiescharttext","h2","right"));
	var a = makeContentElement("",0.5,"populationchart","h2","third");
	a.appendChild(makeFloatTextDiv("found design strategies","strategiestext","h2","left"));
	rowoverview2.appendChild(a);
	rowoverview2.appendChild(p);
	
	overview.appendChild(rowoverview2);
	
	var clusterbrowser = document.getElementById("clusterbrowser");
	clusterbrowser.append(addTextToDiv("h1","cluster explorer"));

	var rowexplorer2 = makeSection(); 
	var p = makeContentElement("",0.3,"mapDiv","h2","full");
	var scalediv = document.createElement("div");
	scalediv.id = "scale";
	p.appendChild(scalediv);
	p.appendChild(makeFloatTextDiv("map","maptext","h2","left"));
	rowexplorer2.appendChild(p);
	clusterbrowser.appendChild(rowexplorer2);

	var rowexplorer1 = makeSection();
	var p = makeContentElement("control",1,"control","h2","third");
	var a = makeContentElement("cluster strategies",1,"clusterstrategies","h2","third");
	var c = makeContentElement("cluster populations",1,"clusterpopulations","h2","third");
	rowexplorer1.appendChild(p);
	rowexplorer1.appendChild(a);
	rowexplorer1.appendChild(c);
	clusterbrowser.appendChild(rowexplorer1);
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