class Pchart{
	
	constructor(divID,zoneBounds,chartName,w,h){
	this.par =  document.getElementById(divID);
	if(w===0&&h===0){
		this.w = this.par.clientWidth;//- margin.left - margin.right;
		this.h = this.par.clientHeight;//- margin.top - margin.bottom;
	}
	else{
		this.w = w;//- margin.left - margin.right;
		this.h = h;//- margin.top - margin.bottom;
	}
	this.divid = divID;
	this.chartname = chartName
	this.padding=40;
	this.zones = zoneBounds;
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
		

		this.setScales();

		this.svgChart  = d3.select("#"+this.divid)
		.append("svg")
		.attr("width", this.w)
		.attr("height", this.h)
		.attr("id",this.divid+"_svg");
		this.chartUpdate();
  
	}
	setScales(){
		this.xScale = d3.scaleLinear()
		 .domain([this.tempMin, this.tempMax])
		 .range([this.padding, this.w - this.padding]);

		this.yScale = d3.scaleLinear()
		 .domain([this.humidtyRatioMin, this.humidtyRatioMax])
		 .range([this.h - 2*this.padding, 2*this.padding]);
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
		
	}
	locationChanged(){
  
		this.removeRHCurves();
		this.addRHCurves();
		this.displayStrategiesOnChart();
	}
		removeRHCurves(){
		d3.selectAll(".line ."+this.chartname).remove();
		d3.selectAll(".rhtext ."+this.chartname).remove();
	}
	addFoundStrategies(strategyData,pcname){
		d3.selectAll(".sztext ."+pcname).remove();

		var t =document.getElementsByClassName("sztext "+pcname);
		if(t.length>0){
			var parent = t[0].parentNode;
			while(t.length>0){
				parent.removeChild(t[0]);
				
			}
		}
		var labels =[];
		for(var i=0;i<strategyData.length;i++){
			var classname = strategyData[i].name.replace(/ /g,'_');
			strategyData[i].color = this.zones.find(s=>s.name===classname).color;
		}
		//add the new labels
		this.svgChart.selectAll(".sztext ."+pcname)
			.data(strategyData)
			.enter()
			.append("text")
			.attr("class",function(d,i){return d.name.replace(/ /g,'_')+" sztext "+pcname;})
			.attr("x",10)
			.attr("y",function(d,i){return (i+1)*15;})
			.attr("dy", ".35em")
			.attr("id", "sztext")
			.attr("fill",function(d,i){return d.color;})
			.attr("font-weight","normal")
			.on("mouseover", handleMouseOverStrategy)
			.on("mouseout", handleMouseOutStrategy)
			.text(function(d,i){return d.percent+"% "+d.name+" ("+d.count+" points)";});

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
	d3.selectAll(".axis ."+this.chartname).remove();
	this.svgChart.append("g")
	.attr("class", "axis "+this.chartname)
	.attr("transform", "translate(0," + (this.h - 2*this.padding) + ")")
	.call(d3.axisBottom(this.xScale)
	.ticks(10));   

	// text label for the x axis
	this.svgChart.append("text")             
	.attr("transform",
	    "translate(" + (this.w/2) + " ," + 
	                   (this.h-this.padding) + ")")
	.style("text-anchor", "middle")
	.attr("class", "axis "+this.chartname)
	.text("Dry Bulb Temperature (C)");

	//y axis

	this.svgChart.append("g")
	.attr("class", "axis "+this.chartname)
	.attr("transform", "translate(" + (this.w - this.padding) +",0 )")
	.call(d3.axisRight(this.yScale).ticks(5));

	// text label for the y axis
	this.svgChart.append("text")
	  .attr("transform", "rotate(-90)")
	  .attr("y", this.w-this.padding/2 )
	  .attr("x",-this.h/2 )
	  .attr("dy", "1em")
	  .style("text-anchor", "middle")
	  .attr("class", "axis "+this.chartname)
	  .text("Humidity Ratio (g/kg(d.a)");  
	}

	addRHCurves()
	{
	  var rhdata;
	    for(var i=100;i>9;i-=10){
	      rhdata = this.getRHCurveCoords(i,this.altitude);
	          
	          this.svgChart.append("path")
	          .data([rhdata])
	          .attr("class", "line "+this.chartname)
	          .attr("d", this.rhline)
	          .attr("id", "rh"+i);
	      //add a path label   
	// Add a text label.
	 
	  var text =this.svgChart.append("text")
	  .append("textPath") //append a textPath to the text element
	  .attr("xlink:href", "#rh"+i) //place the ID of the path here
	  .attr("class", "rhtext "+this.chartname)
	  .style("text-anchor","start") //place the text on the path start
	  .attr("startOffset", "0%");
	  
	  if(i===100) text.text(i+"% relative humidity");
	  if(i===10) text.text(i+"%");
	      
	    }
	}
	comfortZoneChartCoords(data){
	  var dataOut=[];
	  for(var i=0;i<data.length;i++){
	      dataOut.push([this.xScale(data[i].dbt),this.yScale(1000*data[i].hr)]);

	  }
	  return dataOut;
	}
	
	displayStrategiesOnChart(){
		for(var i=0;i<this.zones.length;i++){
		var centroid = this.comfortZoneChartCoords([this.zones[i].centroid]);
			d3.selectAll("."+this.zones[i].name+".strategy ."+this.chartname).remove();
			this.svgChart.append("path")
				.data([this.zones[i].bound])
				.attr("class", this.zones[i].name+" strategy "+this.chartname)
				.attr("d", this.comfBound)
				.attr("id", "szone"+i)
				.attr("stroke", this.zones[i].color)
				.attr("fill","none")
				.attr("opacity",1)
				.attr("svgcentroid",[centroid])
				.on("mouseover", handleMouseOverStrategy)
				.on("mouseout", handleMouseOutStrategy);
		}	
		
	}

	
}