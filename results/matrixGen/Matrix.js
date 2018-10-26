class Matrix{
	constructor(data,divID,w,h,scaleTitle){
		//data is name value list to be compared with itself
		this.data=data;
		this.divID = divID;
		this.dataset =[];
		this.names=[];
		this.padding = 100;
		this.w=w;
		this.h=h;
		this.count = data.length;
		this.maxDiff=0;
		this.scaleTitle = scaleTitle;
		this.xScale = d3.scaleLinear()
		 .domain([0, this.count])
		 .range([0, this.w - this.padding]);

		 this.yScale = d3.scaleLinear()
		 .domain([this.count,0])
		 .range([this.h - this.padding, 0]);

		 this.svgChart = d3.select("#"+divID)
		   .append("svg")
		  .attr("width", this.w)
		  .attr("height", this.h);



		  
		  this.makeDataMatrix();
		  this.bands =d3.scaleBand()
    		.domain(this.names)
    		.range([0, this.w - this.padding]);
    		this.drawAxes();
		  this.chartUpdate();
		  this.makeScaleBarMaxMin();

	}
	makeDataMatrix(){
		
		for(var i=0;i<this.data.length;i++){
			this.names.push(i+"_name "+Math.round(this.data[i].value*100)/100);
			for(var j=0;j<this.data.length;j++){
			var	diff = Math.abs(this.data[i].value-this.data[j].value);
			if(diff>this.maxDiff) this.maxDiff=diff;
			this.dataset.push({"row":i,"col":j,"diff":diff});
			}
		}
	}
	makeScaleBarMaxMin(){
		
		var range  = this.maxDiff;
		var inc = range/10;
		var barlabels=[];
		for(var i=0;i<=10+1;i+=1)
		  {
		    barlabels.push(Math.round(i*inc*10)/10);
		  }
		this.scalebar = new ScaleBar(this.divID+"Scale",barlabels,this.scaleTitle,this.divID+"Chart",25,this.h,this.getColor);//divID,labels,title,mapOwner,blockS
	}
	chartUpdate(){
		var rectW = (this.w-this.padding)/this.count;
		var rectH = (this.h-this.padding)/this.count;
		var col = this.getColor;
		var x = this.xScale;
		var y =this.yScale;
		var rects=this.svgChart.selectAll("rect")
		.data(this.dataset,function(d){return d;});

		rects.enter()
		.append("rect")
		.attr("x", function(d){return x(d.col);})
		.attr("y", function(d){return y(d.row);})
		.attr("width", rectW)
		.attr("height",rectH)
		.attr("fill", function(d) {return col(d.diff);});
    
	}
	drawAxes(){
	//axis
	var x = this.xScale;
		var y =this.yScale;
		var bands = this.bands;
	this.svgChart.append("g")
	.attr("class", "axis")
	.attr("transform", "translate(0," + (this.h - this.padding) + ")")
	.call(d3.axisBottom(bands)).append("text");
	
	this.svgChart.selectAll("text")
	.style("text-anchor", "start")
	.attr("y", 0)
    .attr("x", 9)
    .attr("dy", ".35em")
    .attr("transform", "rotate(90)")
    .style("text-anchor", "start");

	// text label for the x axis
	// this.svgChart.append("text")             
	// .attr("transform","translate(" + (w/2) + " ," + (this.h-this.padding/3) + ")")
	// .style("text-anchor", "middle")
	// .text("Day");

	//y axis
	this.svgChart.append("g")
	.attr("class", "axis")
	.attr("transform", "translate(" + (this.w - this.padding) +",0 )")
	.call(d3.axisRight(bands)).append("text");

	 // text label for the y axis
	// this.svgChart.append("text")
	// .attr("transform", "rotate(-90)")
	// .attr("y", this.w-this.padding/3 )
	// .attr("x",-this.h/2 )
	// .attr("dy", "1em")
	// .style("text-anchor", "middle")
	// .text("Hour");  

	}
	getColor(t){
	var cScale = d3.scaleLinear()
	.domain([0,1])
	.range([0,1]);
	return d3.interpolateMagma(cScale(t));
	}
}
