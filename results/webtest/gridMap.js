
var mapW ;
var mapH ;
var stYr;
var endYr;
var stMonth;
var endMonth;
var year =2007;
var month =7;
var clusterData=[];
var minClusterId=0;
var maxClusterId;
function runMapTool(divID){
  //called only on page load

	mapW =document.getElementById(divID).clientWidth;
	mapH = document.getElementById(divID).clientHeight;
	stYr =analysisParams["startDate"].year;
	endYr =analysisParams["endDate"].year;
	stMonth = analysisParams["seasonStartMonth"];
	endMonth = analysisParams["seasonEndMonth"];
	maxClusterId = analysisParams["nclusters"];
  	setMap("regionsTopo.json");
  	 
    setUpMapping();
    setverticalScaleBar();
 	defineScaleBar();
}
function explorerUpdate(){
	getData(year+"/"+month+"/clusters.json"); 
	
 	

}
function defineScaleBar(){
	
	var barlabels=[];
	for(var i=0;i<=maxClusterId;i+=1)
	  {
	    barlabels.push(Math.round(i));
	  }
	drawScaleBarText(20,barlabels);

}
function setUpMapping(){
	
  	addRangeSlider("control","year","yearSelector","yearChange()",stYr,endYr,1,year,"slider","h3","");
  	addRangeSlider("control","month","monthSelector","monthChange()",stMonth,endMonth,1,month,"slider","h3","");
 	
}
function yearChange(){
	var e = document.getElementById('yearSelector');
	var id = e.getAttribute("id");  
	title =id.substring(0, id.length - 8);
	year = e.value;
	updateText(e.value,title+"value");
	explorerUpdate();
}
function monthChange(){
	var e = document.getElementById('monthSelector');
	var id = e.getAttribute("id");  
	title =id.substring(0, id.length - 8);
	month = e.value-1;
	updateText(e.value,title+"value");
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
  		if(tableData[i]!=""){
  		var dv = tableData[i].split("[");
		var parts = dv[0].split(",");
  		lat=Number(parts[3]);
  		lon=Number(parts[4]);
  		clusterid=Number(parts[1]);
  		alt=Number(parts[2]);
  		var vBits = dv[1].replace(']','').split(",");
  		for(var v=0;v<vBits.length;v++){
  			vector.push(Number(vBits[v]));
  		}
  		clusterData.push({"lat":lat,"lon":lon,"alt":alt,"clusterid":clusterid,"vector":vector});
  	}
  	}
  	mapUpdate();
}
function loading(){
	document.getElementById("loading").innerHTML="loading data...";
}
function loaded(){
 	document.getElementById("loading").innerHTML="";
}
function addRangeSlider(divID,title,selectorID,onChangeFn,start,end,step,optionSet,classN,style,units){

if(units==null) units="";
  var control = document.getElementById(divID);
  var p = document.createElement("p");
    var x = document.createElement("INPUT");
    x.setAttribute("type", "range");
    x.setAttribute("id", selectorID);
    x.setAttribute("onchange", onChangeFn);
    x.setAttribute("class",classN);
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
    x.setAttribute("id",id);
    var t = document.createTextNode(text);
    x.appendChild(t);
    return x;
}
function updateText(text,id){
  var h = document.getElementById(id);
  var t = document.createTextNode(text);
  h.replaceChild(t, h.childNodes[0]);
}
