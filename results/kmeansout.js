var svg;
var sCount;
var projection = d3.geoMercator()//.fitExtent([[0, 0], [width, height]], colom);
  .center([-73,8.5])
  .scale([3300]);
  var path = d3.geoPath()
    .projection(projection);
function runMap(){
  setTitle();
var width = 1000,
    height = 1000;

svg = d3.select("#chart").append("svg")
    .attr("width", width)
    .attr("height", height);

    svg.append("text")
  .attr("x", width/2)
  .attr("y", height/2)
  .text("loading data...")
  .attr("class", "loading");

  //setMap();
  showRegions(); 
  plotGrid(); 
}
function readFromS3(error,data)
{
var tableData = d3.csvParseRows(data);
var gridData = [];
for(var i=0;i<tableData.length;i++)
{
  gridData.push({
        clusterN: parseInt(tableData[i][0]),
        lat: Number(tableData[i][1]),
        lon: Number(tableData[i][2]),
        vector:tableData[i][3]
    });
}
rectangles(gridData);
}
function plotGrid(){
//https://s3.amazonaws.com/rolyhudsontestbucket1/climateData/Spark+climate+clustering+with+kmeans_output_2018_07_21_15_37_23%5Cresults2018_07_21_20_44_02/part-00000
  d3.queue()
    .defer(d3.text, "results/results")
    .await(readFromS3);
}

function getColorSpectral(t){

var cScale = d3.scaleLinear()
 .domain([0,20])
 .range([0, 1]);

  return d3.interpolateWarm(cScale(t));
}


function points(data)
{
  console.log(data);
  var s = svg.selectAll(".grid")
  .data(data).enter()
    .append("circle")
    .attr("cx", function (d) { return projection([d.lon,d.lat])[0]; })
    .attr("cy", function (d) { return projection([d.lon,d.lat])[1]; })
    .attr("r", "8px")
    .attr("fill", function (d) { return getColorSpectral(d.clusterN);})
    .attr("class","grid");

   d3.select(".loading").remove();
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
function rectangles(data)
{
  var  rects=svg.selectAll("rect")
    .data(data)
    .enter()
   .append("rect")
   .attr("x", function (d) { return projection([d.lon-0.25,d.lat+0.25])[0];})
   .attr("y", function (d) { return projection([d.lon-0.25,d.lat+0.25])[1];})
   .attr("width", function(d){return defineCell(d)[0];})
   .attr("height", function(d){return defineCell(d)[1];})
   .attr("fill-opacity",0.8)
   .attr("fill",function (d) {return getColorSpectral(d.clusterN);} )
   .attr("class","mapBlocks");
}
function fiftyKMinPX(lon,lat)
{
  var current = projection([lon,lat])[1];
  var newpos = projection([lon,lat+0.5])[1];
  var fiftykm = Math.abs(current-newpos);
  return fiftykm;
}

function handleMouseOut(d, i) {
  // Use D3 to select element, change color back to normal
  d3.select(this)
  d3.selectAll(".mOver").remove();
  this.setAttribute("r", "15px");
  // Select text by id and then remove
}

function handleMouseOutRegion(d, i) {
  // Use D3 to select element, change color back to normal
  d3.select(this)
  d3.selectAll(".mOver").remove();
//
  // Select text by id and then remove
  
}
function setTitle(){
  
  var tit = document.getElementById("title");
  var x = document.createElement("h1");

  var t = document.createTextNode("Preliminary kmeans results");
  x.appendChild(t);
  tit.appendChild(x);
}

function showRegions()
{
  d3.json("regionsTopo.json", function(error, reg) {
  if (error) throw error;
  console.log(reg);
 
  var path = d3.geoPath()
    .projection(projection); 

    svg.append("path")
    .attr("class","region")
      .attr("fill", "grey")
      .attr("fill-opacity", 0.5)
      .attr("stroke", "white")
      .attr("stroke-width", 1)
      .attr("d", path(topojson.mesh(reg,reg.objects.regionsGEOJSON)));
});
}

function setMap(){
d3.json("../../data/maps/COL_adm0CountryBoundaryTopo.json", function(error, col) {
  if (error) throw error;
  console.log(col);

 var colom = topojson.feature(col,col.objects.COL_adm0);
 
 
  var path = d3.geoPath()
    .projection(projection); 

    svg.append("path")
      .attr("fill", "none")
      .attr("stroke", "#777")
      .attr("stroke-width", 0.35)
      .attr("d", path(topojson.mesh(col,col.objects.COL_adm0)));
       
      
  
});
}