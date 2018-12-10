
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
var typicalYearClusterData=[];
var clusterMonthlyData=[];
var minClusterId=0;
var maxClusterId;
var contextMapData;
var countryMap;
var mapNames=[];
var cityData=[];
function runExplorerMapTool(){

	stYr =analysisParams["startDate"].year;
  year =stYr+1;
	endYr =analysisParams["endDate"].year;
	stMonth = analysisParams["seasonStartMonth"];
	endMonth = analysisParams["seasonEndMonth"];
	
  setUpControl();
  readMapData();
  
}
function readMapData(){
  //typical year and map context files are only read once
    d3.queue()
    .defer(d3.json,"../shared/worldTopo.json")
    .defer(d3.json,"../shared/regionsTopo.json")
    .await(createMap);
}
function createMap(error, context, country){
  contextMapData=context;
  countryMap=country;
  
  allTimeStepMap = new MapGrid("alltimestepsmapDiv",0,0,context,country,"TYclusterMap","clusternum","c_id",getColorSpectral);
  allTimeStepMap.makeScaleBarCluster();
  allTimeStepMap.mapUpdate(typicalYearClusterData);
  allTimeStepMap.showCities(cityData);
  mapNames.push({"name":"TYclusterMap","mapObject":allTimeStepMap});

  singleTimeStepMap = new MapGrid("singletimestepmapDiv",allTimeStepMap.mapW,allTimeStepMap.mapH,context,country,"clusterMap","clusternum","c_id",getColorSpectral);
  singleTimeStepMap.makeScaleBarCluster();
  mapNames.push({"name":"clusterMap","mapObject":singleTimeStepMap});

  monthlyTypicalMap = new MapGrid("monthlytypicalmapDiv",allTimeStepMap.mapW,allTimeStepMap.mapH,context,country,"clusterMonthlyMap","clusternum","c_id",getColorSpectral);
  monthlyTypicalMap.makeScaleBarCluster();

  mapNames.push({"name":"clusterMonthlyMap","mapObject":monthlyTypicalMap });
  runThermalComparison();
  explorerUpdate();
}
function explorerUpdate(){
	getData(year+"/"+month+"/clusters.json",processCluster); 
  getData("stats/typicalYear/"+month+"/clusters.json",processClusterMonthly);
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
  processAllTimeSingleClusterStrategies(cluster);
}
function readTypicalYear(error,data){
  tableData = data.split(/\r?\n/);
  for(var i=0;i<tableData.length;i++){
      if(tableData[i]!="")typicalYearClusterData.push(JSON.parse(tableData[i]));

    }
}

function setUpControl(){
	 addRangeSlider("alltimestepssingleclustercontrol","cluster_id","cluster_idSelector",clusterChange,0,maxClusterId-1,1,cluster,"slider","h3","");
   
   document.getElementById("alltimestepssingleclustercontrol").appendChild(makeTextID("alltimestepsingleclusterPop","cluster population: ","h3"));

  	addRangeSlider("singletimestepallclusterscontrol","year","yearSelector",yearChange,stYr,endYr,1,stYr+1,"slider","h3","");
  	addRangeSlider("singletimestepallclusterscontrol","month","monthSelector",monthChange,stMonth,endMonth,1,month,"slider","h3","");
    
    addRangeSlider("singletimestepsingleclustercontrol","cluster_id","cluster_idSelector",clusterChange,0,maxClusterId-1,1,cluster,"slider","h3","");

    addRangeSlider("singletimestepsingleclustercontrol","year","yearSelector",yearChange,stYr,endYr,1,stYr+1,"slider","h3","");
    addRangeSlider("singletimestepsingleclustercontrol","month","monthSelector",monthChange,stMonth,endMonth,1,month,"slider","h3","");

    addRangeSlider("monthlytypicalallclusterscontrol","month","monthSelector",monthChange,stMonth,endMonth,1,month,"slider","h3","");
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
function readCities(error,data){
  cityData = d3.csvParseRows(data);
  }
function getData(file,func)
{
	loading();
	d3.queue()
    .defer(d3.text, file)
    .await(func);
}
function processCluster(error, data)
{
	if (error) throw error;
	loaded();
  	//console.log(data);
  	tableData = data.split(/\r?\n/);
  	clusterData=[];
    
  	for(var i=0;i<tableData.length;i++){
  		if(tableData[i]!="")clusterData.push(JSON.parse(tableData[i]));

  	}
  	singleTimeStepMap.mapUpdate(clusterData);
    singleTimeStepMap.showCities(cityData)
    withinClusterUpdate();
    //get the thermal data
    thermalComparison();
    //update the thermal comparisons
    utciMaps.updateMaps(thermalData);
    ideamciMaps.updateMaps(thermalData);
}
function processClusterMonthly(error, data)
{
  if (error) throw error;

    //console.log(data);
    tableData = data.split(/\r?\n/);
    clusterMonthlyData=[];
    var foundstrat = [];
    var strategyData=[];
    var clusterSummary=[];
    var points=0;
      for(var i=0;i<tableData.length;i++){
        if(tableData[i]!=""){
          points++;
          
          clusterMonthlyData.push(JSON.parse(tableData[i]));
          c = clusterSummary.find(cs=>cs.x===clusterMonthlyData[i].clusternum);
            if(c=== undefined){
              clusterSummary.push({"x":clusterMonthlyData[i].clusternum,"y":1,"percent":0});
            }
            else{
             c.y++;
            }

          for(var j=0;j<clusterMonthlyData[i].strategies.length;j++){
            foundstrat.push(clusterMonthlyData[i].strategies[j]);
          }
        }
      
      }
      monthlyTypicalMap.mapUpdate(clusterMonthlyData);
      monthlyTypicalMap.showCities(cityData);
    var s;
    //summarise occurances
    for(var i=0;i<foundstrat.length;i++){
      s = strategyData.find(sd=>sd.name===foundstrat[i]);
      if(s=== undefined){
        strategyData.push({"name":foundstrat[i],"count":1,"percent":0});
      }
      else{
        s.count++;
      }
        
    }
    //calc percentages
    for(var i=0;i<strategyData.length;i++){
      strategyData[i].percent = Math.round(strategyData[i].count/points*1000)/10;
    }
    clusterSummary.sort(orderPopulationByClusterID);
    pc5.addFoundStrategies(strategyData,"pc5");
    popMonthlyDonut = new Donutchart("monthlytypicalallclusterscontrol",clusterSummary,"populationMonthlydonut",["all clusters"],popAllDonut.w,popAllDonut.h);
}
function orderPopulationByClusterID(a, b){
  return a.x-b.x;
}
function loading(){
	document.getElementById("loading").innerHTML="loading data...";
}
function loaded(){
 	document.getElementById("loading").innerHTML="";
}


