var utciMaps;
var ideamciMaps;
var utciStats;
var ideamciStats;
function runThermalComparison(){
	setStatsOutput(["statsutci","statsideamci"],["mean","standardDeviation","rootMeanSquared"]);
	thermalComparison();
	utciMaps = new ThermalComparison(["predictedutci","calculatedutci","deltautci"],["clusterUtci","cellUtci","deltaUtci"],thermalData,contextMapData,countryMap,"");
	ideamciMaps = new ThermalComparison(["predictedideamci","calculatedideamci","deltaideamci"],["clusterIdeamci","cellIdeamci","deltaIdeamci"],thermalData,contextMapData,countryMap,""); 
	
}
function setStatsOutput(divids,props){
	for(var i=0;i<divids.length;i++){
		var div = document.getElementById(divids[i]);
		for(var p=0;p<props.length;p++){
			div.appendChild(makeTextID(divids[i]+props[p],props[p],"p"));
		}
	}
	
}
function thermalComparison (){
	thermalData=[];
	var clusternum;
	var cluster;
	var deltaideamci=[];
	var deltautci=[];
	for(var i=0;i<clusterData.length;i++){
	clusternum = clusterData[i].clusternum;
	cluster = allclustersstats.find(s=>s.x===clusternum);
	deltautci.push(cluster.clusterUtci-clusterData[i].utci);
	
	deltaideamci.push(cluster.clusterIdeamci-clusterData[i].ideamci);
	
	thermalData.push({"lat":clusterData[i].lat,"lon":clusterData[i].lon,"clusterUtci":cluster.clusterUtci,"cellUtci":clusterData[i].utci,"deltaUtci":cluster.clusterUtci-clusterData[i].utci,
						"clusterIdeamci":cluster.clusterIdeamci,"cellIdeamci":clusterData[i].ideamci,"deltaIdeamci":cluster.clusterIdeamci-clusterData[i].ideamci});
	}
	//var jstat = this.jStat(deltautci);
	utciStats={"mean":jStat.mean(deltautci).toFixed(2),"standardDeviation":jStat.stdev(deltautci).toFixed(2),"rootMeanSquared":Math.sqrt(jStat.sumsqrd(deltautci)/deltautci.length).toFixed(2)};
	ideamciStats={"mean":jStat.mean(deltaideamci).toFixed(2),"standardDeviation":jStat.stdev(deltaideamci).toFixed(2),"rootMeanSquared":Math.sqrt(jStat.sumsqrd(deltaideamci)/deltaideamci.length).toFixed(2)};
	showThermalStats(["statsutci","statsideamci"],[utciStats,ideamciStats]);
}
function showThermalStats(divids,stats){
	for(var i=0;i<divids.length;i++){
		
		for (var property in stats[i]) {
	    if (stats[i].hasOwnProperty(property)) {
	    	updateText(property+": "+stats[i][property],divids[i]+property);
	        
	    }
		}
	}

}