class DesignStrategies{
	constructor(pchart,zonesfile,zonesdisplayfile){
		this.strategies = [];
		this.strategiesDisplay=[];
		this.zoneColors = ["#66cc00","turquoise", "dodgerblue","blue","orchid","purple","SandyBrown","lightsalmon","gold","orange","darkorange","red","darkred" ];
		//this.tableData=[];
		this.PChart =pchart;
		this.zones = zonesfile;
		this.displayzones = zonesdisplayfile;
		this.setStrategy();
	}
	

setStrategy(){
	this.getZones(this.zones);
	this.getZones(this.displayzones);
}
getZones(file){
	if(file.includes("display")){
	d3.queue()
    .defer(d3.text,file )
    .await(function(error,data)
		{
			var tableData = d3.csvParseRows(data);
			this.readZones(tableData);
			this.displayOnChart();
			//readData("stats/strategyStats/clusters.json",this.processStrategies);
		});
	}
	else{
		d3.queue()
    .defer(d3.text,file )
    .await(function(error,data)
		{
			var tableData = d3.csvParseRows(data);
			this.readZones(tableData);
			this.percentInZone();
		});
	}
}
parseZones(error,data)
{
	var tableData = d3.csvParseRows(data);
	this.readZones(tableData);
	this.percentInZone();
}
parseDisplay(error,data)
{
	var tableData = d3.csvParseRows(data);
	this.readZones(tableData);
	this.displayOnChart();
	readData("stats/strategyStats/clusters.json",this.processStrategies);
}
readZones(tableData){
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
		
		boundaries.push({"name":name,"bound":bound,"color":this.zoneColors[i],"centroid":centroid});

	}
	return boundaries;
}
displayOnChart(){
	for(var i=0;i<this.strategiesDisplay.length;i++){
		d3.selectAll("."+this.strategiesDisplay[i].name+".strategy").remove();
           this.PChart.svgChart.append("path")
           .data([this.strategiesDisplay[i].bound])
          	.attr("class", this.strategiesDisplay[i].name+" strategy")
          	.attr("d", this.PChart.comfBound)
         	.attr("id", "sz"+i)
         	.attr("stroke", this.strategiesDisplay[i].color)
         	.attr("fill","none")
			.on("mouseover", this.handleMouseOverStrategy)
    		.on("mouseout", this.handleMouseOutStrategy);
	}
}

handleMouseOverStrategy(d, i) {  // Add interactivity

  // Use D3 to select element, change color and size
var name = this.classList[0];

var result = strategies.filter(obj => {
  return obj.name === name
})

var coords = comfortZoneChartCoords([result[0].centroid]);
var col = result[0].color;
d3.select(this).style("fill", col);
d3.select(this).style("opacity", 0.5);


this.PChart.svgChart.append("text")
  .attr("x", coords[0][0])
  .attr("y", coords[0][1])
  .attr("text-anchor","middle")
  .text(name)
  .attr("class", "mOver sztext");
}

handleMouseOutStrategy(d, i) {
  // Use D3 to select element, change color back to normal
  d3.select(this).style("fill","none");
  d3.select(this).style("opacity", 1);
  d3.selectAll(".mOver").remove();
}
percentInZone(){
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

	d3.selectAll(".comfH").remove();
	this.PChart.svgChart.selectAll(".comfH")
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
}