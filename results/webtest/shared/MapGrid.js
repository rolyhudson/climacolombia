class MapGrid{
	constructor(divID,w,h,contextData,mapData,mapName){
	this.par = document.getElementById(divID);
	this.contextdata=contextData;
	this.mapdata=mapData;
	this.mapname = mapName;

	if(w===0&&h===0){
		this.mapW =this.par.clientWidth;
		this.mapH = this.par.clientHeight;
	}
	else{
	    this.mapW = w;
	    this.mapH = h;
  	}
		this.setUpMap(divID);
		this.setMapProjection(this.mapdata);
		this.makeScaleBar(divID);
	}
	makeScaleBar(divid){
		var barlabels=[];
		for(var i=0;i<=maxClusterId+1;i+=1)
		  {
		    barlabels.push(Math.round(i));
		  }
		this.scalebar = new ScaleBar(divid+"scale",barlabels,"c_id",this.mapname,20,this.mapH);//divID,labels,title,mapOwner,blockS
	}
	setUpMap(divid){

		d3.selectAll(".mapspace").remove();
		this.svgMap = d3.select("#"+divid)
		.append("svg")
		.attr("class","mapspace")
		.attr("width", this.mapW)
		.attr("height", this.mapH)
		.call(d3.zoom().on("zoom", function () {
		svgMap.attr("transform", d3.event.transform)
		}))
		.append("g");
	}
	setMapProjection(data){
		var features;
		for(var prop in data.objects)
		{
		features = prop;
		}
		var colom = topojson.feature(data,data.objects[features]);
		this.projection = d3.geoMercator().fitExtent([[0, 0], [this.mapW, this.mapH]], colom);
		this.path = d3.geoPath().projection(this.projection);

		this.addContext(this.contextdata); 
	}
	addContext(data){
		
		//var path = d3.geoPath().projection(this.projection); 
		this.svgMap.append("path")
		.attr("stroke", "none")
		.attr("fill","rgb(235, 249, 235)" )
		.attr("class","map")
		.attr("d", this.path(topojson.mesh(data,data.objects["land"])));
		

		this.svgMap.append("path")
		.attr("stroke", "#777")
		.attr("fill","none" )
		.attr("class","map")
		.attr("d", this.path(topojson.mesh(data,data.objects["countries"])));
	}
	defineCell(d,proj){
		var cell=[];
		var p1 = proj([d.lon,d.lat]);
		var p2 = proj([d.lon+0.5,d.lat]);
		cell[0] = Math.round(Math.abs(p1[0]-p2[0]));
		p2 = proj([d.lon,d.lat+0.5]);
		cell[1] = Math.round(Math.abs(p1[1]-p2[1]));
		return cell;
	}
	mapUpdate(clusterData){
	// remove previous
		d3.selectAll(".mapBlocks ."+this.mapname).remove();
		var proj = this.projection;
		var cellDef = this.defineCell;
		var  rects=this.svgMap.selectAll("rect")
		.data(clusterData,function(d){return d;})
		.enter()
		.append("rect")
		.attr("x", function (d) { return proj([d.lon-0.25,d.lat+0.25])[0];})
		.attr("y", function (d) { return proj([d.lon-0.25,d.lat+0.25])[1];})
		.attr("width", function(d){return cellDef(d,proj)[0];})
		.attr("height", function(d){return cellDef(d,proj)[1];})
		.attr("fill-opacity",0.8)
		.attr("fill",function (d) {return getColorSpectral(d.clusternum);} )
		.attr("class","mapBlocks "+this.mapname)
		.attr("id",function(d){return d.clusternum;})
		.on("mouseover", this.handleMouseOverMap)
		.on("mouseout", this.handleMouseOutMap);
		}
		// Create Event Handlers for mouse
	handleMouseOverMap(d, i) {  // Add interactivity
		// Use D3 to select element, change color and size
		var ownerMap = this.classList[1];
		d3.select(this).style("fill", "red");
		if(ownerMap==="clusterMap"){
		
		singleTimeStepMap.scalebar.highlightBlock(d.clusternum,"red");
		popTSDonut.highlight(d.clusternum,popTSDonut.id);
		}
		
	}
	handleMouseOutMap(d, i) {
		// Use D3 to select element, change color back to normal
		var ownerMap = this.classList[1];
		d3.select(this).style("fill",function (d) {return  getColorSpectral(d.clusternum);} )
		d3.selectAll(".mOver").remove();
		if(ownerMap==="clusterMap"){
		singleTimeStepMap.scalebar.resethighlightBlock();
		popTSDonut.unhighlight(d.clusternum,popTSDonut.id);
	}
	  
	}
}