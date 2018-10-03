class Donutchart{
constructor(divID,info,id,centreTitleA){
this.centreTitle = [centreTitleA];
this.data=info;
this.links=[]
this.par =  document.getElementById(divID);
this.id = id;
this.circleRad = this.par.clientWidth*0.025;

this.w = this.par.clientWidth;
this.h = this.par.clientHeight;

this.radius = this.w/3.5;

d3.select(".svg"+this.id).remove();
this.svgChart = d3.select("#"+divID)
  .append("svg")
  .attr("class","svg"+this.id)
  .attr("width", this.w)
  .attr("height", this.h);

//this.drawTitle();
this.setColors();
this.getTotalUnits();
this.drawPie();
this.drawKey();

this.addCentreTitle();
//this.drawDerivedStats()
}

updateChart(){

this.drawPie();
}
getTotalUnits(){
    this.totalUnits=0;
    for(var i=0;i<this.data.length;i++){
       this.totalUnits+= this.data[i].y;
    }
    for(var i=0;i<this.data.length;i++){
       this.data[i]["percent"] =round(this.data[i].y/this.totalUnits*100,0);
       
    }
    this.centreTitle.push(this.totalUnits+" points");
    this.centreTitle.push("100%");
for(var i=0;i<this.data.length;i++){
    this.data[i]["originalTitle"] =this.centreTitle;
    this.data[i]["id"] =this.id;
}
}
setColors(){
  for(var i=0;i<this.data.length;i++){
    this.data[i]["col"] = this.getColorSpectral(i);
  }
}
getradius(){
  return this.radius;
}
getColorSpectral(t){

var cScale = d3.scaleLinear()
 .domain([0,this.data.length])
 .range([0, 1]);

  return d3.interpolateCool(cScale(t));
}
drawPie(){
  var w = this.w;
  var h = this.h;
this.g =this.svgChart.selectAll(".graphic"+this.id).remove();
this.g = this.svgChart.append('g')
          .attr('transform', 'translate(' + (this.w/ 3.5) +',' + (this.h/2) + ')')
          .attr("class","graphic"+this.id)
          .attr("id",this.id);
  
  this.pie = d3.pie();
this.pie.sort(null)
    .value(function(d) { return d.y; });

this.path = d3.arc()
    .outerRadius(this.radius*0.8)
    .innerRadius(this.radius*0.5);


this.arc = this.g.selectAll(".arc"+this.id).remove();
this.arc = this.g.selectAll(".arc"+this.id)
    .data(this.pie(this.data))
    .enter().append("g")
    .attr("id", function(d) { return d.x; })
    .attr("class", "arc"+this.id)
    .on("mouseover", this.handleMouseOver)
    .on("mouseout", this.handleMouseOut);

this.arc.append("path")
      .attr("d", this.path)
      .attr("fill", function(d) { return d.data.col; });

}
handleMouseOver(d,i){
    var id = d.data.id;
    var arcs = document.getElementsByClassName("arc"+id);
    var textA = document.getElementsByClassName("graphLabelA donut"+id);
    var textB = document.getElementsByClassName("graphLabelB donut"+id);
    var circles = document.getElementsByClassName("circle donut"+id);
    for(var j=0;j<arcs.length;j++)
    {
        if(i!=j){
            arcs[j].style.opacity = "0.3";
            textA[j].style.opacity = "0.3";
            textB[j].style.opacity = "0.3";
            circles[j].style.opacity = "0.3";
        }
    }
    var title = document.getElementsByClassName("centreTitle donut"+id);
    title[0].innerHTML = "cluster "+d.data.x;
    title[1].innerHTML = d.data.y+" data points";
    title[2].innerHTML = d.data.percent+"%";
}
handleMouseOut(d,i){
    var id = d.data.id;
    var arcs = document.getElementsByClassName("arc"+id);
    var textA = document.getElementsByClassName("graphLabelA donut"+id);
    var textB = document.getElementsByClassName("graphLabelB donut"+id);
    var circles = document.getElementsByClassName("circle donut"+id);
    for(var j=0;j<arcs.length;j++)
    {
        arcs[j].style.opacity = "";
        textA[j].style.opacity = "";
        textB[j].style.opacity = "";
        circles[j].style.opacity = "";
    }
    
    var title = document.getElementsByClassName("centreTitle donut"+id);
    var t = this;
    title[0].innerHTML = d.data.originalTitle[0];
    title[1].innerHTML = d.data.originalTitle[1];
    title[2].innerHTML = d.data.originalTitle[2];
}
highlight(cluster,id){
    var arcs = document.getElementsByClassName("arc"+id);
    var textA = document.getElementsByClassName("graphLabelA donut"+id);
    var textB = document.getElementsByClassName("graphLabelB donut"+id);
    var circles = document.getElementsByClassName("circle donut"+id);
    for(var j=0;j<arcs.length;j++)
    {
        if(i!=j){
            arcs[j].style.opacity = "0.3";
            textA[j].style.opacity = "0.3";
            textB[j].style.opacity = "0.3";
            circles[j].style.opacity = "0.3";
        }
    }
    var title = document.getElementsByClassName("centreTitle donut"+id);
    title[0].innerHTML = "cluster "+d.data.x;
    title[1].innerHTML = d.data.y+" data points";
    title[2].innerHTML = d.data.percent+"%";
}
addCentreTitle()
{
    //text labels
    var margin = this.radius*0.1;
    var h = this.h/2-margin;
    this.svgChart.selectAll(".centreTitle donut"+this.id).remove();
    this.svgChart.selectAll(".centreTitle donut"+this.id)
    .data(this.centreTitle)
    .enter().append("text")
    .attr("class","centreTitle donut"+this.id)
    .attr("y",function(d,i){return h+i*margin;}) 
    .attr("x", this.w/3.5)
    .attr("text-anchor","middle")
    .text(String);
}
drawKey(){
    
    //top to top of donut
    var margin = this.radius*0.35;
    var r = (this.h-margin)/(this.data.length+2)/2;
    
    this.svgChart.selectAll(".circle donut"+this.id).remove();
    this.svgChart.selectAll(".circle donut"+this.id)
    .data(this.data)
    .enter().append("circle")
    .attr("class","circle donut"+this.id)
    .attr("id",function(d) { return d.x; })
    .attr("cy", function(d,i) { return (margin)+i*r*2.1; })
    .attr("cx", this.w*0.55)
    .attr("r", r+"px")
    .attr("fill", function(d) { return d.col; });
    
    //text labels
    this.svgChart.selectAll(".graphLabelA donut"+this.id).remove();
    this.svgChart.selectAll(".graphLabelA donut"+this.id)
    .data(this.data)
    .enter().append("text")
    .attr("class","graphLabelA donut"+this.id)
    .attr("id",function(d) { return d.x; })
    .attr("y", function(d,i) { return (margin+r/2)+i*r*2.1; })
    .attr("x", this.w*0.6)
    .text(function(d){return d.x;});

    //text info
    this.svgChart.selectAll(".graphLabelB  donut"+this.id).remove();
    this.svgChart.selectAll(".graphLabelB  donut"+this.id)
    .data(this.data)
    .enter().append("text")
    .attr("class","graphLabelB  donut"+this.id)
    .attr("id",function(d) { return d.x; })
    .attr("y", function(d,i) { return (margin+r/2)+i*r*2.1;})
    .attr("x", this.w*0.65)
    .text(function(d){return d.percent+"% "+d.y+" points";});
    
    
}


}