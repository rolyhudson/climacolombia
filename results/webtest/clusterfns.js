var mainimgHeight = 200;
var analysisParams;
var performanceChart;
function makePage(){
	mainimgHeight = window.innerHeight/6;
	setuplayout();
	showParams();
	showPerformance();
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
function processPerformance(error, data){
	tableData = data.split(/\r?\n/);
	var performanceData=[];
	for(var i=0;i<tableData.length;i++)
	{
	performanceData.push(JSON.parse(tableData[i]))	
	}
performanceData.sort(compare);
	performanceChart = document.getElementById("performancechart");
	var w = performanceChart.clientWidth;
	var h = performanceChart.clientHeight;
   

var maxmin = d3.extent(performanceData, function(d){return d.NClusters;})
  var xscale = d3.scaleLinear()
    .domain(maxmin)
    .rangeRound([0, 200]);

	maxmin = d3.extent(performanceData, function(d){return d.cost;})
	var yscale = d3.scaleLinear()
       .domain(maxmin) 
       .rangeRound([100,0]); 

    var  rhline = d3.line()
    .x(function(d) { return xscale(d.NClusters); })
    .y(function(d) { return yscale(d.cost); });

    var chart = d3.select("#performancechart")
   .append("svg")
  .attr("width", w)
  .attr("height", 100);

  chart.append("path").datum(performanceData)
  		.attr("fill", "none")
      .attr("stroke", "black")
      .attr("d",rhline);
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
}
function setuplayout(){
	var overview = document.getElementById("overview");
	overview.append(addTextToDiv("h1","overview"));
	var rowoverview1 = makeSection();
	 
	var p = makeContentElement("input parameters",1,"parameters","h2","twothirds");
	var a = makeContentElement("performance",1,"performancechart","h2","third");
	rowoverview1.appendChild(p);
	rowoverview1.appendChild(a);
	overview.appendChild(rowoverview1);

	var rowoverview2 = makeSection();
	 
	var p = makeContentElement("design strategies",0.6,"strategies","h2","twothirds");
	var a = makeContentElement("cluster populations",0.6,"populations","h2","third");
	rowoverview2.appendChild(p);
	rowoverview2.appendChild(a);
	overview.appendChild(rowoverview2);
	
	var clusterbrowser = document.getElementById("clusterbrowser");
	clusterbrowser.append(addTextToDiv("h1","cluster explorer"));

	var rowexplorer2 = makeSection(); 
	var p = makeContentElement("map",0.3,"map","h2","full");
	rowexplorer2.appendChild(p);
	clusterbrowser.appendChild(rowexplorer2);

	var rowexplorer1 = makeSection();
	var p = makeContentElement("control",1,"parameters","h2","third");
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
    contentCol.appendChild(addTextToDiv(titlesize,title));
    
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