class ScaleBar{
	constructor(divID,labels,title,mapOwner,blockS,h){
  	this.par = document.getElementById(divID);
    this.scaleLbl = labels;
    this.scaleTitle = title;
    this.owner = mapOwner;
    this.divid= divID;
    this.blocksize=blockS;
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

    .attr("width", 50)
    .attr("height",this.height)
    .attr("class","svgScaleBar "+this.owner);
  }
  drawScaleBarText(){
    
    var labelData=[];
   var block = this.blocksize;
    for(var i=0;i<this.scaleLbl.length;i++)
    {
      labelData.push({"value":i});
    }
    // remove previous
    d3.selectAll(".blocks ."+this.owner).remove();
    //add the new blocks
    var  label = this.svgScaleBar.selectAll("rect")
    .data(labelData)
    .enter()
    .append("rect")
    .attr("class","blocks "+this.owner)
    .attr("id",function(d,i){return i;})
    .attr("x", 0)
    .attr("y", function(d,i){return i*block+block;})
    .attr("width", block)
    .attr("height",block)
    .style("fill", function(d) {return getColorSpectral(d.value);})
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
    for(var b=0;b<mapBlocks.length;b++){
      if(Number(mapBlocks[b].id)===d.value){
        mapBlocks[b].style.fill = "red";   
      }
    }
    popTSDonut.highlight(d.value,popTSDonut.id);
  }
  handleMouseOutScale(d, i){
    var owner = this.classList[1]
    var mapBlocks = document.getElementsByClassName("mapBlocks "+owner);
    d3.select(this).style("fill",function (d) {return  getColorSpectral(d.value);} )
    for(var i=0;i<mapBlocks.length;i++){
      
      mapBlocks[i].style.fill = getColorSpectral(mapBlocks[i].id);
    }

    popTSDonut.unhighlight(d.value,popTSDonut.id);
  }
  highlightBlock(i,color){
    this.findBlockInScale(i);
    this.scaleblocks[this.highlightedBlock].style.fill = "red";
  }
  resethighlightBlock(){
    var v = this.scaleblocks[this.highlightedBlock].id;
    
    this.scaleblocks[this.highlightedBlock].style.fill = getColorSpectral(v);
    
  }
  findBlockInScale(i){
    this.highlightedBlock = i;
  }
}