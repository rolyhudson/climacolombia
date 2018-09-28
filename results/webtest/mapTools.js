
var svgMap;
var projection;

function setMap(mapfile){
setUpMap(w,h);

d3.queue()
    .defer(d3.json, mapfile)
    .await(setMapProjection);
}

function setUpMap(){

 d3.selectAll(".mapspace").remove();
 svgMap = d3.select("#mapDiv")
  .append("svg")
  .attr("class","mapspace")
  .attr("width", mapW)
  .attr("height", mapH)
  .call(d3.zoom().on("zoom", function () {
              svgMap.attr("transform", d3.event.transform)
      }))
      .append("g");

}
function setMapProjection(error, data){
  if (error) throw error;
  console.log(data);

  d3.selectAll(".map").remove();
  //assuming only one set of features 
  var features;
  for(var prop in data.objects)
  {
    features = prop;
  }
  var colom = topojson.feature(data,data.objects[features]);
  projection = d3.geoMercator().fitExtent([[0, 0], [mapW, mapH]], colom);
  var path = d3.geoPath()
    .projection(projection);

    contextWorld(); 
      
}
function drawDepartments(){
d3.queue()
    .defer(d3.json, "departTopo.json")
    .await(addDeparts);
}
function addDeparts(error, data){
  if (error) throw error;
  
  //assuming only one set of features
  var features;
  for(var prop in data.objects)
  {
    features = prop;
  }
  var path = d3.geoPath()
    .projection(projection); 
      svgMap.append("path")
      .attr("stroke", "#777")
      .attr("fill","none" )
      .attr("class","map")
      .attr("d", path(topojson.mesh(data,data.objects[features])));

      getData(year+"/"+month+"/clusters.json");
}
function contextWorld(){
    d3.queue()
    .defer(d3.json, "worldTopo.json")
    .await(addWorld);
}
function addWorld(error, data){
  if (error) throw error;
  console.log(data);
  //assuming only one set of features
  var features;
  for(var prop in data.objects)
  {
    features = prop;
  }
  var path = d3.geoPath()
    .projection(projection); 
  svgMap.append("path")
      .attr("stroke", "none")
            .attr("fill","rgb(235, 249, 235)" )
      .attr("class","map")
      .attr("d", path(topojson.mesh(data,data.objects["land"])));

      svgMap.append("path")
      .attr("stroke", "#777")
      .attr("fill","none" )
      .attr("class","map")
      .attr("d", path(topojson.mesh(data,data.objects["countries"])));

      explorerUpdate();
}
function defineCell(d){
  var cell=[];
  var p1 = projection([d.lon,d.lat]);
  var p2 = projection([d.lon+0.5,d.lat]);
  cell[0] = Math.round(Math.abs(p1[0]-p2[0]));
  p2 = projection([d.lon,d.lat+0.5]);
  cell[1] = Math.round(Math.abs(p1[1]-p2[1]));
  return cell;
}
function mapUpdate(){
  // remove previous
  d3.selectAll(".mapBlocks").remove();

 var  rects=svgMap.selectAll("rect")
    .data(clusterData,function(d){return d;})
    .enter()
   .append("rect")
   .attr("x", function (d) { return projection([d.lon-0.25,d.lat+0.25])[0];})
   .attr("y", function (d) { return projection([d.lon-0.25,d.lat+0.25])[1];})
   .attr("width", function(d){return defineCell(d)[0];})
   .attr("height", function(d){return defineCell(d)[1];})
   .attr("fill-opacity",0.8)
   .attr("fill",function (d) {return getColorSpectral(d.clusterid);} )
   .attr("class","mapBlocks")
    .attr("id",function(d){return d.clusterid;})
   .on("mouseover", handleMouseOverMap)
    .on("mouseout", handleMouseOutMap);
}

// Create Event Handlers for mouse
function handleMouseOverMap(d, i) {  // Add interactivity
  // Use D3 to select element, change color and size
  d3.select(this).attr("fill", "red");
  // var info = getAllValuesFromCell(d);

  // svgMap.selectAll(".mOver")
  // .data(info)
  //  .enter()
  //  .append("text")
  //   .attr("class", "mOver")
  //  .attr("x", 610)
  // .attr("y",function(d,i){return  15+(15*i);})
  // .attr("font-size", 12+"px")  
  // .text(String);
  // get the block in the scale bar
  
  findBlockInScale(d);
  highlightBlock("red");

}


function handleMouseOutMap(d, i) {
  // Use D3 to select element, change color back to normal
  d3.select(this).attr("fill",function (d) {return  getColorSpectral(d.clusterid);} )
  d3.selectAll(".mOver").remove();
  resethighlightBlock();
  // Select text by id and then remove
  
}


