var pages =["kmeansresults.json","bkmeansresults.json","bkmeanskmeansresults.json"];
var cStats = "stats/clusterStats/clusters.json";
var titles =["kmeans","bkmeans","bkmeanskmeans"];
var colors =["rgb(70, 130, 180)","rgb(70, 70, 180)","rgb(70, 180, 152)"];
var popstats=[];
var legend=[];
function makePage(){
getClusterStats();
}
function getClusterStats(){
for(var i=0; i<titles.length;i++){
legend.push({"title":titles[i],"colour":colors[i]});
}
for(var i=0; i<titles.length;i++){
readData(titles[i]+"results.json",readStats);
}

}
function readStats(error,data){
 var tableData = data.split(/\r?\n/);
 
 var popdata=[];
for(var i=0;i<tableData.length;i++)
{
if(tableData[i]!=""){
var obj = JSON.parse(tableData[i]);
popstats.push(JSON.parse(tableData[i]));
}
}
console.log(popstats);
//var graph = new MultiHistogramGraph("histogram",popstats,"cluster num","population",legend);
}


function readData(file,awaitFn){
d3.queue()
    .defer(d3.text,file )
    .await(awaitFn);
}

function indexOfMax(arr) {
    if (arr.length === 0) {
        return -1;
    }

    var max = arr[0];
    var maxIndex = 0;

    for (var i = 1; i < arr.length; i++) {
        if (arr[i] > max) {
            maxIndex = i;
            max = arr[i];
        }
    }

    return maxIndex;
}

///////////////////////

class MultiHistogramGraph{
constructor(divID,data,xtitle,ytitle,datatitles){
//console.log(info);
    this.chartDiv = document.getElementById(divID);
    this.margin = {top: 40, right: 20, bottom: 50, left: 50};
    this.w = this.chartDiv.clientWidth- this.margin.left - this.margin.right;
    this.h = this.chartDiv.clientHeight- this.margin.top - this.margin.bottom;
   
   var xmaxes =[];
   var ymaxes=[];
    for(var i =0;i<data.length;i++){
      xmaxes.push(d3.max(data[i], d=> d.cnum));
      ymaxes.push(d3.max(data[i], d=> d.pop));
    }
    

    var xscale =d3.scaleBand()
    .domain(data[indexOfMax(xmaxes)].map(d => d.cnum))
    .range([0, this.w]).padding(0.1);

    var yscale = d3.scaleLinear()
    .domain([0,d3.max(ymaxes)]) 
    .rangeRound([this.h,0]); 

    this.chart = d3.select("#"+divID)
    .append("svg")
    .attr("width", this.chartDiv.clientWidth)
    .attr("height", this.chartDiv.clientHeight);

    this.chart.append("g")
    .attr("transform", "translate(" + this.margin.left + "," + (this.h+ this.margin.top) + ")")
    .call(d3.axisBottom(xscale)).append("text")
    .attr("y",this.margin.bottom/2)
      .attr("x",this.margin.left/2)
      .attr("fill", "#000")
      .attr("dy", "0.71em")
      .text(xtitle+" ->")
    .select(".domain");

    this.chart.append("g")
      .call(d3.axisLeft(yscale))
      .attr("transform", "translate(" + this.margin.left + "," + this.margin.top + ")")
    .append("text")
      .attr("fill", "#000")
      .attr("transform","rotate(-90)")
      .attr("y",-this.margin.left*0.8)
      .attr("x",-this.h)
      .attr("dy", "0.71em")
      .attr("text-anchor", "start")
      .text(ytitle+" ->");

      for(var i =0;i<data.length;i++){
        var shift = xscale.bandwidth()/3*i;
        this.chart.append("g")
      .attr("transform", "translate(" + this.margin.left + "," + this.margin.top + ")")
      .attr("fill", colors[i])
      .selectAll("rect").data(data[i]).enter().append("rect")
      .attr("x", d => xscale(d.cnum) +shift)
      .attr("y", d => yscale(d.pop))
      .attr("height", d => yscale(0) - yscale(d.pop))
      .attr("width", xscale.bandwidth()/3);
      }

var legend = this.chart.selectAll(".legend")
        .data(datatitles)
        .enter().append("g")
        .attr("class", "legend")
        .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

    legend.append("rect")
        .attr("x", this.w + 18)
        .attr("width", 18)
        .attr("height", 18)
        .style("fill", function(d) { return d.colour; });

    legend.append("text")
        .attr("x", this.w + 40)
        .attr("y", 9)
        .attr("dy", ".35em")
        .style("text-anchor", "start")
        .text(function(d) { return d.title; });
      
    }
  }
///////////////////////////////////////