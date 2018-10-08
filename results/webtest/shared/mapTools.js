
// var svgMap;
// var projection;

function setMap(mapfile,divid){
setUpMap(divid);

d3.queue()
    .defer(d3.json, mapfile)
    .await(setMapProjection);
}

function setUpMap(divid){

 d3.selectAll(".mapspace").remove();
 svgMap = d3.select("#"+divid)
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

function contextWorld(){
    d3.queue()
    .defer(d3.json, "../shared/worldTopo.json")
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
   .attr("fill",function (d) {return getColorSpectral(d.clusternum);} )
   .attr("class","mapBlocks")
    .attr("id",function(d){return d.clusternum;})
   .on("mouseover", handleMouseOverMap)
    .on("mouseout", handleMouseOutMap);
}




