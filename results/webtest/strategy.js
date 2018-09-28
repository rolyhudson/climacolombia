var strategies = [];
var strategiesDisplay=[];
var zoneColors = ["greenyellow","turquoise", "dodgerblue","blue","orchid","purple","SandyBrown","lightsalmon","gold","orange","darkorange","red","darkred" ];
function setStrategy(){
getZones("zones.txt");
getZones("zonesdisplay.txt");
}
function getZones(file){
	if(file.includes("display")){
		readData(file,parseDisplay);
	}
	else{
	readData(file,parseZones);
	}
}
function parseZones(error,data)
{
tableData = d3.csvParseRows(data);
strategies = readZones(tableData);
percentInZone();
}
function parseDisplay(error,data)
{
tableData = d3.csvParseRows(data);
strategiesDisplay = readZones(tableData);
displayOnChart();
readData("stats/strategyStats.json",processStrategies);
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

function displayOnChart(){
	for(var i=0;i<strategiesDisplay.length;i++){
		d3.selectAll("."+strategiesDisplay[i].name+".strategy").remove();
           svgChart.append("path")
           .data([strategiesDisplay[i].bound])
          	.attr("class", strategiesDisplay[i].name+" strategy")
          	.attr("d", comfBound)
         	.attr("id", "sz"+i)
         	.attr("stroke", strategiesDisplay[i].color)
         	.attr("fill","none")
			.on("mouseover", handleMouseOverStrategy)
    		.on("mouseout", handleMouseOutStrategy);
}

	
}

function handleMouseOverStrategy(d, i) {  // Add interactivity

  // Use D3 to select element, change color and size
var name = this.classList[0];

var result = strategies.filter(obj => {
  return obj.name === name
})

var coords = comfortZoneChartCoords([result[0].centroid]);
var col = result[0].color;
  d3.select(this).style("fill", col);
d3.select(this).style("opacity", 0.5);


svgChart.append("text")
  .attr("x", coords[0][0])
  .attr("y", coords[0][1])
  .attr("text-anchor","middle")
  .text(name)
  .attr("class", "mOver sztext");
}

function handleMouseOutStrategy(d, i) {
  // Use D3 to select element, change color back to normal
  d3.select(this).style("fill","none");
  d3.select(this).style("opacity", 1);
  d3.selectAll(".mOver").remove();
}
function percentInZone(){
	var results= [];
	for(var i=0;i<strategies.length;i++){
		comfZpoints = comfortZoneChartCoords(strategies[i].bound);
   comfortHours = hoursInComfortZ(climatePoints, comfZpoints);
   var percentYr = round(comfortHours/87.6,1);
   if(percentYr>0){
   	results.push({"hours":comfortHours,"percent":percentYr,"name":strategies[i].name,"color":strategies[i].color,"text":percentYr+"% "+strategies[i].name+" "+comfortHours +"hrs" });
   
 }
	}
// sort by value
results.sort(function (a, b) {
  return b.percent-a.percent ;
});
for(var i=0;i<results.length;i++){
}
d3.selectAll(".comfH").remove();
  svgChart.selectAll(".comfH")
  .data(results)
   .enter()
   .append("text")
    .attr("class",function(d){return "comfH "+  d.name;} )
    .attr("stroke", function(d,i){return  d.color;})
   .attr("x", 5)
  .attr("y",function(d,i){return  30+(15*i);})
  .attr("font-size", 10+"px")  
  .text(function(d){return  d.text;});
}
function textPos(d,i){
	return  15+(15*i);
}