class Pchart{
	
	constructor(divID){
	this.par =  document.getElementById(divID);
	this.divid = divID;
	this.padding=40;

	this.tempMax =45;
	this.tempMin =-10;
	this.humidtyRatioMax = 28;
	this.humidtyRatioMin =0;
	this.altitude = 0;
	this.atmosPress=101000;
	this.lat=0;
	this.lon=0;
	this.runPChartTool();
	
	}
	//methods
	runPChartTool(){
		this.w = this.par.clientWidth;//- margin.left - margin.right;
		this.h = this.par.clientHeight;//- margin.top - margin.bottom;

		this.setScales();

		this.svgChart  = d3.select("#"+this.divid)
		.append("svg")
		.attr("width", this.w)
		.attr("height", this.h);

		this.chartUpdate();
  
	}
	setScales(){
		this.xScale = d3.scaleLinear()
		 .domain([this.tempMin, this.tempMax])
		 .range([this.padding, this.w - this.padding]);

		this.yScale = d3.scaleLinear()
		 .domain([this.humidtyRatioMin, this.humidtyRatioMax])
		 .range([this.h - this.padding, this.padding]);
			var xs = this.xScale;
			var ys = this.yScale;
		this.rhline = d3.line()
		    .x(function(d) { return xs(d.dbt); })
		    .y(function(d) { return ys(1000*d.hr); });

		this.comfBound = d3.line()
		    .x(function(d) { return xs(d.dbt); })
		    .y(function(d) { return ys(1000*d.hr); });
	}
	chartUpdate(){
		this.drawPsychrometricAxes();
		this.locationChanged();
		this.setStrategy();
	}
	locationChanged(){
  
	this.removeRHCurves();
	this.addRHCurves();
	}
	removeRHCurves(){
	d3.selectAll(".line").remove();
	d3.selectAll(".rhtext").remove();
	}
	getRHCurveCoords(rh,altitude){
	var rhData=[];

	var satPress;
	var partialPress;
	for(var i=this.tempMin; i<this.tempMax;i+=0.5){
		satPress= saturationPress(i);
		partialPress = partPress(rh,satPress);
		rhData.push({"dbt": i,"hr":humidtyRatio(this.atmosPress,partialPress)});
	}
	return rhData;
	}
	drawPsychrometricAxes(){
	//axis
	d3.selectAll(".axis").remove();
	this.svgChart.append("g")
	.attr("class", "axis")
	.attr("transform", "translate(0," + (this.h - this.padding) + ")")
	.call(d3.axisBottom(this.xScale)
	.ticks(10));   

	// text label for the x axis
	this.svgChart.append("text")             
	.attr("transform",
	    "translate(" + (this.w/2) + " ," + 
	                   (this.h-this.padding/3) + ")")
	.style("text-anchor", "middle")
	.attr("class", "axis")
	.text("Dry Bulb Temperature (C)");

	//y axis

	this.svgChart.append("g")
	.attr("class", "axis")
	.attr("transform", "translate(" + (this.w - this.padding) +",0 )")
	.call(d3.axisRight(this.yScale).ticks(5));

	// text label for the y axis
	this.svgChart.append("text")
	  .attr("transform", "rotate(-90)")
	  .attr("y", this.w-this.padding/2 )
	  .attr("x",-this.h/2 )
	  .attr("dy", "1em")
	  .style("text-anchor", "middle")
	  .attr("class", "axis")
	  .text("Humidity Ratio (g/kg(d.a)");  
	}

	addRHCurves()
	{
	  var rhdata;
	    for(var i=100;i>9;i-=10){
	      rhdata = this.getRHCurveCoords(i,this.altitude);
	          
	          this.svgChart.append("path")
	          .data([rhdata])
	          .attr("class", "line")
	          .attr("d", this.rhline)
	          .attr("id", "rh"+i);
	      //add a path label   
	// Add a text label.
	 
	  var text =this.svgChart.append("text")
	  .append("textPath") //append a textPath to the text element
	  .attr("xlink:href", "#rh"+i) //place the ID of the path here
	  .attr("class", "rhtext")
	  .style("text-anchor","start") //place the text on the path start
	  .attr("startOffset", "0%");
	  
	  if(i===100) text.text(i+"% relative humidity");
	  if(i===10) text.text(i+"%");
	      
	    }
	}
	comfortZoneChartCoords(data){
	  var dataOut=[];
	  for(var i=0;i<data.length;i++){
	      dataOut.push([xScale(data[i].dbt),yScale(1000*data[i].hr)]);

	  }
	  return dataOut;
	}
	round(value, precision) {
	    var multiplier = Math.pow(10, precision || 0);
	    return Math.round(value * multiplier) / multiplier;
	}
}