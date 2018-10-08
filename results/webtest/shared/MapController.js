
var mapW ;
var mapH ;
var stYr;
var endYr;
var stMonth;
var endMonth;
var year =2007;
var month =7;
var cluster =0
var clusterData=[];
var minClusterId=0;
var maxClusterId;
function runExplorerMapTool(){

	stYr =analysisParams["startDate"].year;
  year =stYr+1;
	endYr =analysisParams["endDate"].year;
	stMonth = analysisParams["seasonStartMonth"];
	endMonth = analysisParams["seasonEndMonth"];
	maxClusterId = analysisParams["nclusters"];
  setUpControl();
  readMapData();
  
}
function readMapData(){
    d3.queue()
    .defer(d3.json,"../shared/worldTopo.json")
    .defer(d3.json,"../shared/regionsTopo.json")
    .await(createMap);
}
function createMap(error, context, country){

  var viewerdims = set3dView("alltimestepsmapDiv");
  singleTimeStepMap = new MapGrid("singletimestepmapDiv",viewerdims[0],viewerdims[1],context,country,"clusterMap");
  
  explorerUpdate();
}
function explorerUpdate(){
	getData(year+"/"+month+"/clusters.json"); 
  readData(year+"/"+month+"/stats/clusterStats/clusters.json",processSingleTimeStepAllClusterPop);
  readData(year+"/"+month+"/stats/strategyStats/clusters.json",processTimeStepAllClusterStrategies);
  withinClusterUpdate();
}
function withinClusterUpdate(){
  readData(year+"/"+month+"/stats/cluster"+cluster+"Stats/clusters.json",processTimeStepSingleClusterStrategies);
  //show population as text
  var pop = pointsInSingleTimeStepSingleCluster(cluster);
  updateText("cluster population :"+pop,"singletimestepsingleclusterPop");
  var popAll = pointsInAllTimeStepSingleCluster(cluster);
  updateText("cluster population :"+popAll,"alltimestepsingleclusterPop");
}


function setUpControl(){
	 addRangeSlider("alltimestepssingleclustercontrol","cluster_id","cluster_idSelector",clusterChange,0,maxClusterId,1,cluster,"slider","h3","");
   
   document.getElementById("alltimestepssingleclustercontrol").appendChild(makeTextID("alltimestepsingleclusterPop","cluster population: ","h3"));

  	addRangeSlider("singletimestepallclusterscontrol","year","yearSelector",yearChange,stYr,endYr,1,stYr+1,"slider","h3","");
  	addRangeSlider("singletimestepallclusterscontrol","month","monthSelector",monthChange,stMonth,endMonth,1,month,"slider","h3","");
    
    addRangeSlider("singletimestepsingleclustercontrol","cluster_id","cluster_idSelector",clusterChange,0,maxClusterId,1,cluster,"slider","h3","");

    addRangeSlider("singletimestepsingleclustercontrol","year","yearSelector",yearChange,stYr,endYr,1,stYr+1,"slider","h3","");
    addRangeSlider("singletimestepsingleclustercontrol","month","monthSelector",monthChange,stMonth,endMonth,1,month,"slider","h3","");
    document.getElementById("singletimestepsingleclustercontrol").appendChild(makeTextID("singletimestepsingleclusterPop","cluster population: ","h3"));
 	
}
function clusterChange(event){
  var slidertext = document.getElementsByClassName("cluster_idvalue");
  var sliders= document.getElementsByClassName("cluster_idSelector");
  cluster = Number(event.target.value);
  for(var i=0;i<slidertext.length;i++){
      var t = document.createTextNode(+cluster);
      slidertext[i].replaceChild(t, slidertext[i].childNodes[0]);
      sliders[i].value = cluster;
  }
  withinClusterUpdate();
}
function yearChange(event){
  var slidertext = document.getElementsByClassName("yearvalue");
  var sliders= document.getElementsByClassName("yearSelector");
  year = Number(event.target.value);
  for(var i=0;i<slidertext.length;i++){
      var t = document.createTextNode(year);
      slidertext[i].replaceChild(t, slidertext[i].childNodes[0]);
      sliders[i].value = year;
  }
	explorerUpdate();
  
}
function monthChange(){
  var slidertext = document.getElementsByClassName("monthvalue");
  var sliders= document.getElementsByClassName("monthSelector");
  month = Number(event.target.value);
  for(var i=0;i<slidertext.length;i++){
      var t = document.createTextNode(month);
      slidertext[i].replaceChild(t, slidertext[i].childNodes[0]);
      sliders[i].value =month;
  }
	explorerUpdate();
  
}
function getData(file)
{
	loading();
	d3.queue()
    .defer(d3.text, file)
    .await(process);
}
function process(error, data)
{
	if (error) throw error;
	loaded();
  	//console.log(data);
  	tableData = data.split(/\r?\n/);
  	clusterData=[];
  	var lat=0;
  	var lon=0;
  	var clusterid=0;
  	var vector =[];
  	var alt=0;
    
  	for(var i=0;i<tableData.length;i++){
  		if(tableData[i]!="")clusterData.push(JSON.parse(tableData[i]));

  	}
  	singleTimeStepMap.mapUpdate(clusterData);
    withinClusterUpdate();
}
function loading(){
	document.getElementById("loading").innerHTML="loading data...";
}
function loaded(){
 	document.getElementById("loading").innerHTML="";
}


