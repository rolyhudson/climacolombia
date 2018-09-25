var svgScaleBar;
var highlightedBlock;
var scaleblocks;
function highlightBlock(color){
  scaleblocks[highlightedBlock].style.fill = "red";
}
function resethighlightBlock(){
  var v = scaleblocks[highlightedBlock].id;
  if(isNaN(v))
  {
  scaleblocks[highlightedBlock].style.fill = getColorSpectral(highlightedBlock);
  }
  else
  {
  scaleblocks[highlightedBlock].style.fill = getColorSpectral(v);
  }
}
function getColorSpectral(t){

var cScale = d3.scaleLinear()
 .domain([0,maxClusterId])
 .range([1, 0]);

  return d3.interpolateSpectral(cScale(t));
}

function findBlockInScale(d){
  
  var v = getValueFromCell(d);
  if(isNaN(v))
  {
    for(var i=0;i<scaleblocks.length;i++){
      if(scaleblocks[i].id.indexOf(v)>-1)
      {
        highlightedBlock=i;
        break;
      }
    }
    
  }
  else
  {
    var min = Number(scaleblocks[0].id);
    var range = Number(scaleblocks[1].id)-min;
    var i = Math.ceil((v-min)/range);

    //handle the numeric classificiations
    if(currentField.description.indexOf("Classfication numeric")>-1){
    i=v;
    }

    highlightedBlock=i;
  }
}

function setverticalScaleBar(){

   d3.selectAll(".svgScaleBar").remove();
  svgScaleBar = d3.select("#scale")
  .append("svg")

  .attr("width", 100)
  .attr("height", h)
  .attr("class","svgScaleBar");
}

function defineTextTransform(d,i,blocksize,labelX)
{
var xText = i*blocksize+blocksize/2;
var yText = labelX+blocksize;
return "translate(" + xText + "," + yText + ") rotate(90)";
}
function drawScaleBarText(blocksize,labels){
  
  var barBlocks =labels.length;
  var range =maxClusterId+1;
  var step = range/barBlocks;
  
  var labelData=[];
  
  for(var i=0;i<=maxClusterId;i+=step)
  {
    labelData.push(i);
  }
// remove previous
d3.selectAll(".blocks").remove();
//add the new blocks
 var  label = svgScaleBar.selectAll("rect")
  .data(labelData)
   .enter()
    .append("rect")
    .attr("class","blocks")
    .attr("id",function(d,i){return labels[i];})
    .attr("x", 0)
      .attr("y", function(d,i){return i*blocksize+blocksize/5;})
      .attr("width", blocksize)
      .attr("height",blocksize)
      .style("fill", function(d) {return getColorSpectral(d);})
      .on("mouseover", handleMouseOverScale)
    .on("mouseout", handleMouseOutScale);
 
      scaleblocks = document.getElementsByClassName("blocks");

  
  var labelX=blocksize*1.5;
// remove previous
var label = svgScaleBar.selectAll(".label").remove();
    
//add the new labels
  label = svgScaleBar.selectAll(".label")
  .data(labels)
   .enter()
    .append("text")
    .attr("class","label")
    .attr("x",labelX)
    .attr("y",function(d,i){return i*blocksize+blocksize/2;})
    .attr("dy", ".35em")
    .text(String);



}
function handleMouseOverScale(d, i){
  var mapBlocks = document.getElementsByClassName("mapBlocks");
  
  var id = scaleblocks[i].id;
  if(isNaN(id))
  {

      for(var b=0;b<mapBlocks.length;b++){

        v = mapBlocks[b].id;
          
          if(id.indexOf(v)>-1){
          mapBlocks[b].style.fill = "red";
          
      }
    }
  }
  else
    {
      var min = Number(scaleblocks[0].id);

      var brange = (Number(scaleblocks[1].id)-min)/2;
      min = d-brange;
      var max = d+brange;
      var v;
      for(var b=0;b<mapBlocks.length;b++){

        v = Number(mapBlocks[b].id);
          
          if(v>min&&v<max){
          mapBlocks[b].style.fill = "red";
          
      }
    }
}
}
function handleMouseOutScale(d, i){
    var mapBlocks = document.getElementsByClassName("mapBlocks");
    
    for(var i=0;i<mapBlocks.length;i++){
    v = mapBlocks[i].id;
    if(isNaN(v)) v = matchtextToLabels(v)
      else v = Number(v);
     mapBlocks[i].style.fill = getColorSpectral(v);
    
    }
  
}
function setScaleBarText(width,height){
  svgScaleBar = d3.select("#description")
  .append("svg")
  .attr("width", width)
  .attr("height", height)
  .attr("class","svgScaleBar");
}

