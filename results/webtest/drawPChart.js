var w=800;
var h = 500;
var padding=40;

var tempMax =45;
var tempMin =-10;
var humidtyRatioMax = 28;
var humidtyRatioMin =0;
var altitude = 0;
var atmosPress=101000;
var lat=0;
var lon=0;
//svg for chart
var svgChart;


var climatePoints=[];//points in chart space to test if inside comfort poly
var comfZpoints=[];
var comfortHours=0;

var climateUrl;
var climateObj;//to recive the data

var xScale,yScale,inXScale,inYScale,rhline,comfBound;
function setScales(){


xScale = d3.scaleLinear()
 .domain([tempMin, tempMax])
 .range([padding, w - padding]);

yScale = d3.scaleLinear()
 .domain([humidtyRatioMin, humidtyRatioMax])
 .range([h - padding, padding]);

// inXScale = d3.scaleLinear()
//  .domain([padding, w - padding])
//  .range([tempMin, tempMax]);

// inYScale = d3.scaleLinear()
//  .domain([h - padding, padding])
//  .range([humidtyRatioMin, humidtyRatioMax]);
//rhline 
rhline = d3.line()
    .x(function(d) { return xScale(d.dbt); })
    .y(function(d) { return yScale(1000*d.hr); });

comfBound = d3.line()
    .x(function(d) { return xScale(d.dbt); })
    .y(function(d) { return yScale(1000*d.hr); });
}
function initChart(){

  chartUpdate();
}

function runPChartTool(){
 strategyChart = document.getElementById("strategies");

 w = strategyChart.clientWidth;//- margin.left - margin.right;
 h = strategyChart.clientHeight;//- margin.top - margin.bottom;

 setScales();

  svgChart  = d3.select("#strategies")
  .append("svg")
  .attr("width", w)
  .attr("height", h);

  chartUpdate();
  
}
function chartUpdate(){
  drawPsychrometricAxes();
  locationChanged();
  setStrategy();
}

function locationChanged(){
  
  removeRHCurves();
  addRHCurves();
}
function removeRHCurves(){
d3.selectAll(".line").remove();
d3.selectAll(".rhtext").remove();
}

function getRHCurveCoords(rh,altitude){
  var rhData=[];
  
  var satPress;
  var partialPress;
  for(var i=tempMin; i<tempMax;i+=0.5){
      satPress= saturationPress(i);
      partialPress = partPress(rh,satPress);
      rhData.push({"dbt": i,"hr":humidtyRatio(atmosPress,partialPress)});
  }
 return rhData;
}

function drawPsychrometricAxes(){
//axis
d3.selectAll(".axis").remove();
svgChart.append("g")
.attr("class", "axis")
.attr("transform", "translate(0," + (h - padding) + ")")
  .call(d3.axisBottom(xScale)
  .ticks(10));   

// text label for the x axis
svgChart.append("text")             
.attr("transform",
    "translate(" + (w/2) + " ," + 
                   (h-padding/3) + ")")
.style("text-anchor", "middle")
.attr("class", "axis")
.text("Dry Bulb Temperature (C)");

//y axis

      svgChart.append("g")
      .attr("class", "axis")
      .attr("transform", "translate(" + (w - padding) +",0 )")
      .call(d3.axisRight(yScale).ticks(5));

 // text label for the y axis
  svgChart.append("text")
      .attr("transform", "rotate(-90)")
      .attr("y", w-padding/2 )
      .attr("x",-h/2 )
      .attr("dy", "1em")
      .style("text-anchor", "middle")
      .attr("class", "axis")
      .text("Humidity Ratio (g/kg(d.a)");  
}

function addRHCurves()
{
  
  var rhdata;
    for(var i=100;i>9;i-=10){
      rhdata = getRHCurveCoords(i,altitude);
          
          svgChart.append("path")
          .data([rhdata])
          .attr("class", "line")
          .attr("d", rhline)
          .attr("id", "rh"+i);
      //add a path label   
// Add a text label.
 
  var text =svgChart.append("text")
  .append("textPath") //append a textPath to the text element
  .attr("xlink:href", "#rh"+i) //place the ID of the path here
  .attr("class", "rhtext")
  .style("text-anchor","start") //place the text on the path start
  .attr("startOffset", "0%");
  
  if(i===100) text.text(i+"% relative humidity");
  if(i===10) text.text(i+"%");
      
    }
}
function comfortZoneChartCoords(data){
  var dataOut=[];
  for(var i=0;i<data.length;i++){
      dataOut.push([xScale(data[i].dbt),yScale(1000*data[i].hr)]);

  }
  return dataOut;
}
function round(value, precision) {
    var multiplier = Math.pow(10, precision || 0);
    return Math.round(value * multiplier) / multiplier;
}