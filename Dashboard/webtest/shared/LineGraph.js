function LineGraph(divID,data,xtitle,ytitle,points){
	//console.log(info);
    var chartDiv = document.getElementById(divID);
    var margin = {top: 40, right: 20, bottom: 50, left: 50};
    var w = chartDiv.clientWidth- margin.left - margin.right;
    var h = chartDiv.clientHeight- margin.top - margin.bottom-chartDiv.childNodes[0].clientHeight;
   

    var max = d3.max(data, function(d){return d.x;})
    var xscale = d3.scaleLinear()
    .domain([0,max])
    .rangeRound([0, w]);
    max= d3.max(data, function(d){return d.y;})
    var yscale = d3.scaleLinear()
    .domain([0,max]) 
    .rangeRound([h,0]); 

    var pline = d3.line()
    .x(function(d) { return xscale(d.x); })
    .y(function(d) { return yscale(d.y); });

    var chart = d3.select("#"+divID)
    .append("svg")
    .attr("width", chartDiv.clientWidth)
    .attr("height", chartDiv.clientHeight);
    
    //the line plot
    chart.append("path").datum(data)
    .attr("fill", "none")
    .attr("stroke", "black")
    .attr("d",pline)
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    chart.append("g")
    .attr("transform", "translate(" + margin.left + "," + (h+ margin.top) + ")")
    .call(d3.axisBottom(xscale)).append("text")
    .attr("y",margin.bottom/2)
      .attr("x",margin.left/2)
      .attr("fill", "#000")
      .attr("dy", "0.71em")
      .text(xtitle+" ->")
    .select(".domain");

    chart.append("g")
      .call(d3.axisLeft(yscale))
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
    .append("text")
      .attr("fill", "#000")
      .attr("transform","rotate(-90)")
      .attr("y",-margin.left*0.8)
      .attr("x",-h)
      .attr("dy", "0.71em")
      .attr("text-anchor", "start")
      .text(ytitle+" ->");

      if(points==="all"){
        var p = chart.selectAll("circle")
        .data(data).enter()
        .append("circle")
        .attr("cx", function (d) { return xscale(d.x)+ margin.left; })//
        .attr("cy", function (d) { return yscale(d.y)+ margin.top; })
        .attr("r", "2px")
        .attr("fill", "red")
      }
      else{
        if(points!=""){
          chart.append("circle")
          .attr("cx", function () { return xscale(data[points].x)+ margin.left; })//
          .attr("cy", function () { return yscale(data[points].y)+ margin.top; })
          .attr("r", "3px")
          .attr("fill", "red")
        }
      }
    }