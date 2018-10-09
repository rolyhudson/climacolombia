class MapGrid{
	constructor(divID,w,h,contextData,mapData,mapName,propertyToMap,scaleTitle,colorfn){
	this.divID = divID;
	this.par = document.getElementById(divID);
	this.contextdata=contextData;
	this.mapdata=mapData;
	this.mapname = mapName;
	this.prop = propertyToMap;
	this.scaleTitle = scaleTitle;
	
	this.colorFn =colorfn;
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
		//this.makeScaleBar(divID);
	}
	makeScaleBarCluster(){
		var barlabels=[];
		for(var i=0;i<=maxClusterId+1;i+=1)
		  {
		    barlabels.push(Math.round(i));
		  }
		this.scalebar = new ScaleBar(this.divID+"scale",barlabels,this.scaleTitle,this.mapname,20,this.mapH,this.colorFn);//divID,labels,title,mapOwner,blockS
	}
	makeScaleBarMaxMin(data){
		var prop = this.prop
		var maxmin = d3.extent(data,function(d){return d[prop]});
		var range  = maxmin[1]-maxmin[0];
		var inc = range/10;
		var barlabels=[];
		for(var i=0;i<=10+1;i+=1)
		  {
		    barlabels.push(round(maxmin[0]+i*inc,1));
		  }
		this.scalebar = new ScaleBar(this.divID+"scale",barlabels,this.scaleTitle,this.mapname,15,this.mapH,this.colorFn);//divID,labels,title,mapOwner,blockS
	}
	setUpMap(divid){

		d3.selectAll(".mapspace ."+this.mapname).remove();
		this.svgMap = d3.select("#"+divid)
		.append("svg")
		.attr("class","mapspace "+this.mapname)
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
		for(var i=0;i<clusterData.length;i++){
			 
			clusterData[i].sblock = this.scalebar.findBlockInScale(clusterData[i][this.prop]);
		}
	// remove previous

		var t =document.getElementsByClassName("mapBlocks "+this.mapname);
		if(t.length>0){
			var parent = t[0].parentNode;
			while(t.length>0){
				parent.removeChild(t[0]);
				
			}
		}
		var color = this.colorFn;
		var prop = this.prop;
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
		.attr("fill",function (d) {return color(d[prop]);} )
		.attr("class","mapBlocks "+this.mapname)
		.attr("id",function(d){return d[prop];})
		.attr("sblock",function(d){return d.sblock;})
		.on("mouseover", this.handleMouseOverMap)
		.on("mouseout", this.handleMouseOutMap);
		}
		// Create Event Handlers for mouse
	handleMouseOverMap(d, i) {  // Add interactivity
		// Use D3 to select element, change color and size
		var ownerMap = this.classList[1];
		var mapIndex;
		var map;
		d3.select(this).style("fill", "red");
		if(ownerMap==="clusterMap"){
		
		map = singleTimeStepMap;
		popTSDonut.highlight(d.clusternum,popTSDonut.id);
		}
		else{
			mapIndex = utciMaps.maps.findIndex(m=>m.mapname===ownerMap);
			if(mapIndex===-1) {
				mapIndex = ideamciMaps.maps.findIndex(m=>m.mapname===ownerMap);
				map = ideamciMaps.maps[mapIndex];
			}
			else{
				map = utciMaps.maps[mapIndex];
			}
		}
		map.scalebar.highlightBlock(d[map.prop],"red");
	}
	handleMouseOutMap(d, i) {
		// Use D3 to select element, change color back to normal
		var ownerMap = this.classList[1];
		var mapIndex;
		var map;
		d3.selectAll(".mOver").remove();
		if(ownerMap==="clusterMap"){
			map = singleTimeStepMap;
			popTSDonut.unhighlight(d.clusternum,popTSDonut.id);
		}
		else{
				mapIndex = utciMaps.maps.findIndex(m=>m.mapname===ownerMap);
				if(mapIndex===-1) {
					mapIndex = ideamciMaps.maps.findIndex(m=>m.mapname===ownerMap);
					map = ideamciMaps.maps[mapIndex];
				}
				else{
					map = utciMaps.maps[mapIndex];
				}
		}
		var prop = map.prop;	
		d3.select(this).style("fill",function (d) {return  map.colorFn(d[prop]);} )
		map.scalebar.resethighlightBlock(d[prop]);
		}
}