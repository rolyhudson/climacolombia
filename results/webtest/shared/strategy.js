var strategies = [];
var strategiesDisplay=[];
var atmosPress=101000;
var zoneColors = ["#66cc00","turquoise", "dodgerblue","blue","orchid","purple","SandyBrown","lightsalmon","gold","orange","darkorange","red","darkred" ];
function getStrategiesZones(){
readData("../shared/zonesdisplay.txt",parseDisplay);
}

function parseDisplay(error,data)
{
tableData = d3.csvParseRows(data);
strategiesDisplay = readZones(tableData);
addPCharts();

}
function highlightPoints(sName){
	var blocks = [];
	
	for(var i=0;i<clusterData.length;i++){
		var foundstrat=false;
		for(var s=0;s<clusterData[i].strategies.length;s++){
			if(clusterData[i].strategies[s]===sName) foundstrat=true;
			
		}
		blocks.push(foundstrat);
	}
	return blocks;
}
function highlightPointsByCluster(sName,cnum){
	var blocks = [];
	for(var i=0;i<clusterData.length;i++){
		var foundstrat=false;
		if(clusterData[i].clusternum===cnum){
			for(var s=0;s<clusterData[i].strategies.length;s++){
				if(clusterData[i].strategies[s]===sName) foundstrat=true;
			}
		}
		blocks.push(foundstrat);
	}
	return blocks;
}
function pointsInSingleTimeStepSingleCluster(clusNum){
	var total =0;
	for(var i=0;i<clusterData.length;i++){
		if(clusterData[i].clusternum===clusNum) total++;
	}
	return total;
}
function pointsInAllTimeStepSingleCluster(clusNum){
	var total =0;
	var result = allclustersstats.find(s=>s.x===clusNum);
	
	return result.y;
}
function processAllTimeAllClusterStrategies(error, data){
	tableData = data.split(/\r?\n/);
	var strategyData=parseStrategies(tableData);
	pc1.addFoundStrategies(strategyData,"pc1");
}
function processTimeStepSingleClusterStrategies(error,data){
	tableData = data.split(/\r?\n/);
	var strategyData=parseStrategies(tableData);
	pc4.addFoundStrategies(strategyData,"pc4");
}
function processTimeStepAllClusterStrategies(error, data){
	tableData = data.split(/\r?\n/);
	var strategyData=parseStrategies(tableData);
	pc3.addFoundStrategies(strategyData,"pc3");
}

function processAllTimeSingleClusterStrategies(error, data){
	tableData = data.split(/\r?\n/);
	var strategyData=parseStrategies(tableData);
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
function readZones(tableData){
	var boundaries = [];
	for(var i=0;i<tableData.length;i++)
	{
		var bound =[];
		var centroid;
		var dbt=0;
			var rh=0;
			var name="";
		for(var j=0;j<tableData[i].length;j++)
		{
			if(j==0)
			{
				//name
				name = tableData[i][j].replace(/ /g,'_');
			}
			else{
				if(j>tableData[i].length-2){
					//last point is centroid
					if(j%2==0){
					//rh
					rh = Number(tableData[i][j]);
					if(rh>100)rh=100;
					satPress= saturationPress(dbt);
					partialPress = partPress(rh,satPress);
					centroid ={"dbt": dbt,"hr":humidtyRatio(atmosPress,partialPress)};
					}
					else{
						//temp
						dbt = Number(tableData[i][j]);
					}
				}
				else{
					if(j%2==0){
					//rh
					rh = Number(tableData[i][j]);
					if(rh>100)rh=100;
					satPress= saturationPress(dbt);
					partialPress = partPress(rh,satPress);
					bound.push({"dbt": dbt,"hr":humidtyRatio(atmosPress,partialPress)});
					}
					else{
						//temp
						dbt = Number(tableData[i][j]);
					}
				}
				
			}
		}
		
		boundaries.push({"name":name,"bound":bound,"color":zoneColors[i],"centroid":centroid});

	}
	return boundaries;
}

function round(value, precision) {
    var multiplier = Math.pow(10, precision || 0);
    return Math.round(value * multiplier) / multiplier;
	}
function orderPopulation(a, b){
	return a.count-b.count;
}
function processAllTimeAllClusterPop(error, data){
	tableData = data.split(/\r?\n/);
	allclustersstats=parsePopData(tableData);
	popAllDonut = new Donutchart("alltimestepsallclusterscontrol",allclustersstats,"populationoverviewdonut",["all clusters"],0,0);
	
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


function processSingleTimeStepAllClusterPop(error, data){
	tableData = data.split(/\r?\n/);
	var popData=parsePopData(tableData);
	popTSDonut = new Donutchart("singletimestepallclusterscontrol",popData,"populationTSdonut",["all clusters"],popAllDonut.w,popAllDonut.h);
}
function processSingleTimeStepClusterSingleClusterPop(error, data){
	tableData = data.split(/\r?\n/);
	var popData=parsePopData(tableData);
}


function processPerformance(error, data){
	tableData = data.split(/\r?\n/);
	var performanceData=[];
	for(var i=0;i<tableData.length;i++)
	{
	if(tableData[i]!="")performanceData.push(JSON.parse(tableData[i]))	
	}
	var performanceDivs=["performanceWssse","performanceDunn","performanceSil"];
	if(performanceData.length===1){
		for(var i=0;i<performanceDivs.length;i++){
			var div = document.getElementById(performanceDivs[i]);
			var text="";
			var value;
			if(i==0) {value=round(performanceData[0].costWSSSE,2);}
			if(i==1) {value=round(performanceData[0].dunn,2);}
			if(i==2) {value=round(performanceData[0].silhouette,2);}
			div.appendChild(addTextToDiv("p",performanceData[0].NClusters+" user selected clusters")); 
			div.appendChild(addTextToDiv("h1",value));
		}
	}
	else{
		performanceData.sort(compare);
		const result = performanceData.findIndex( p => p.selected===true);
		for(var i=0;i<performanceDivs.length;i++){
			var data=[];
			for(var d=0;d<performanceData.length;d++){
				if(i==0) {text = "WSSSE";value=round(performanceData[d].costWSSSE,2);}
				if(i==1) {text = "Dunn";value=round(performanceData[d].dunn,2);}
				if(i==2) {text = "Silhouette";value=round(performanceData[d].silhouette,2);}
				data.push({"x":performanceData[d].NClusters,"y":value});
			}
			
			LineGraph(performanceDivs[i],data,"k clusters",text, result );

			if(i==0){
				var div = document.getElementById("performanceOther");
				div.appendChild(addTextToDiv("p","k optimised for wssse at: "+performanceData[result].NClusters+" clusters"));
				div.appendChild(addTextToDiv("p","wssse cost = "+round(performanceData[result].costWSSSE,2)));
			}
		}
	}
}
function compare(a, b){
  	return a.NClusters - b.NClusters;
}
function getColorSpectral(t){

var cScale = d3.scaleLinear()
 .domain([0,maxClusterId])
 .range([1, 0]);

  return d3.interpolateCool(cScale(t));
}
function handleMouseOverStrategy(d,i) { 
		var name = event.target.classList[0];
		var pChartSvg = d3.select("#"+event.path[2].id+"_svg");
		var result = strategiesDisplay.filter(obj => {
		return obj.name === name})

		var objectsToHighlight = document.getElementById(event.path[2].id+"_svg").getElementsByClassName(name);
		var col = result[0].color;
		for(var i=0;i<objectsToHighlight.length;i++){
			if(objectsToHighlight[i].id=="sztext"){
				objectsToHighlight[i].attributes["font-weight"].value= "bold";
			}
			else{
				objectsToHighlight[i].attributes["fill"].value= col;
				objectsToHighlight[i].attributes["opacity"].value= 0.5;
			}
		}
		//highlight on map if single time step
		var blocks=[];
		var pNodeId = event.target.parentNode.id;
		if(pNodeId.includes("singlecluster")){
			blocks = highlightPointsByCluster(name.replace(/_/g,' '),cluster);
		}
		else{
			blocks = highlightPoints(name.replace(/_/g,' '));
		}
		var mapBlocks = document.getElementsByClassName("mapBlocks clusterMap");
		for(var i=0;i<mapBlocks.length;i++){
			if(blocks[i]) mapBlocks[i].style.fill = "red";
		}
		
	}

	function handleMouseOutStrategy(d, i) {
		var name = event.target.classList[0];
		var pChartSvg = d3.select("#"+event.path[2].id+"_svg");
		var result = strategiesDisplay.filter(obj => {
		return obj.name === name})

		var objectsToHighlight = document.getElementById(event.path[2].id+"_svg").getElementsByClassName(name);
		var col = result[0].color;
		for(var i=0;i<objectsToHighlight.length;i++){
			if(objectsToHighlight[i].id=="sztext"){
				objectsToHighlight[i].attributes["font-weight"].value= "normal";
			}
			else{
				objectsToHighlight[i].attributes["fill"].value= "none";
				objectsToHighlight[i].attributes["opacity"].value= 1;
			}
		}
		var blocks = highlightPoints(name.replace(/_/g,' '));
		var mapBlocks = document.getElementsByClassName("mapBlocks clusterMap");
		for(var i=0;i<mapBlocks.length;i++){
			if(blocks[i]) mapBlocks[i].style.fill = getColorSpectral(mapBlocks[i].id);
		}
		//remove highlight on map
	}