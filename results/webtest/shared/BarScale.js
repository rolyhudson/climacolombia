class ScaleBar{
	constructor(divID,labels,title,mapOwner,blockS,h,colorfn){
  	this.par = document.getElementById(divID);
    this.scaleLbl = labels;
    this.scaleTitle = title;
    this.owner = mapOwner;
    this.divid= divID;
    this.blocksize=blockS;
    this.colorFn = colorfn;
    this.height=h;
    this.setverticalScaleBar();
    this.drawScaleBarText();
    this.highlightedBlock=0;
	}
	setverticalScaleBar(){
    var block = this.blocksize;
    d3.selectAll(".svgScaleBar ."+this.owner).remove();
    this.svgScaleBar = d3.select("#"+this.divid)
    .append("svg")

    .attr("width", 80)
    .attr("height",this.height)
    .attr("class","svgScaleBar "+this.owner);
  }
  drawScaleBarText(){
    
    var labelData=[];
    var block = this.blocksize;
    for(var i=0;i<this.scaleLbl.length;i++){
      labelData.push({"value":this.scaleLbl[i],"sblock":this.findBlockInScale(this.scaleLbl[i])});
    }
    // remove previous
    d3.selectAll(".blocks ."+this.owner).remove();
    //add the new blocks
    var color = this.colorFn;
    
    var  label = this.svgScaleBar.selectAll("rect")
    .data(labelData)
    .enter()
    .append("rect")
    .attr("class","blocks "+this.owner)
    .attr("id",function(d,i){return d.sblock;})
    .attr("x", 0)
    .attr("y", function(d,i){return i*block+block;})
    .attr("width", block)
    .attr("height",block)
    .style("fill", function(d) {return color(d.value);})
    .on("mouseover", this.handleMouseOverScale)
    .on("mouseout", this.handleMouseOutScale);
   
    this.scaleblocks = document.getElementsByClassName("blocks "+this.owner);
    var labelX=block*1.5;
    // remove previous
    this.svgScaleBar.selectAll(".scaletitle ."+this.owner).remove();
    this.svgScaleBar.append("text")
    .attr("x",0)
    .attr("y",block/2)
    .text(this.scaleTitle.toString())
    .attr("class","scaletitle "+this.owner);

    this.svgScaleBar.selectAll(".label ."+this.owner).remove();

    //add the new labels
    label = this.svgScaleBar.selectAll(".label ."+this.owner)
    .data(this.scaleLbl)
    .enter()
    .append("text")
    .attr("class","label "+this.owner)
    .attr("x",labelX)
    .attr("y",function(d,i){return i*block+block*1.5;})
    .attr("dy", ".35em")
    .text(String);
  }
  handleMouseOverScale(d, i){
    var owner = this.classList[1]
    var mapBlocks = document.getElementsByClassName("mapBlocks "+owner);
    d3.select(this).style("fill", "red");
    var map;
    var mapIndex;
    if(owner==="clusterMap"){
      map = singleTimeStepMap;
      popTSDonut.highlight(d.value,popTSDonut.id);
    }
    else{
      mapIndex = utciMaps.maps.findIndex(m=>m.mapname===owner);
        if(mapIndex===-1) {
          mapIndex = ideamciMaps.maps.findIndex(m=>m.mapname===owner);
          map = ideamciMaps.maps[mapIndex];
        }
        else{
          map = utciMaps.maps[mapIndex];
        }
    }
    for(var b=0;b<mapBlocks.length;b++){
      var bnum = map.scalebar.findBlockInScale(mapBlocks[b].id)
      if(Number(bnum)===d.sblock){
        mapBlocks[b].style.fill = "red";   
      }
    }
    
  }
  handleMouseOutScale(d, i){
    var ownerMap = this.classList[1]
    var mapBlocks = document.getElementsByClassName("mapBlocks "+ownerMap);
    var map;
    var mapIndex;
    if(ownerMap==="clusterMap"){
      map = singleTimeStepMap;
      popTSDonut.unhighlight(d.value,popTSDonut.id);
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
    d3.select(this).style("fill",function (d) {return  map.colorFn(d.value);} )

    for(var i=0;i<mapBlocks.length;i++){
      
      mapBlocks[i].style.fill = map.colorFn(mapBlocks[i].id);
    }

    
  }
  highlightBlock(i,color){
    this.findBlockInScale(i);
    this.scaleblocks[this.highlightedBlock].style.fill = "red";
  }
  resethighlightBlock(v){
    
    this.scaleblocks[this.highlightedBlock].style.fill = this.colorFn(v);
  }
  findBlockInScale(v){
    var blockNum=0;
    for(var i=0;i<this.scaleLbl.length-1;i++){
      if(v>=this.scaleLbl[i]&&v<this.scaleLbl[i+1]){
        blockNum=i;
      }
    }
    this.highlightedBlock = blockNum;
    return blockNum;
  }
}