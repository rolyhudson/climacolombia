function makeMatrix(){
 readData("perf.json",processData);
}
function readData(file,awaitFn){
d3.queue()
    .defer(d3.text,file )
    .await(awaitFn);
}
function processData(error,data){
	var tableData = data.split(/\r?\n/);
	var silhouette =[];
	var dunn =[];
	var wssse =[];
	var utcirmse =[];
	var ideamcirmse =[];
	for(var i=0;i<tableData.length;i++){
		if(tableData[i]!=""){
			var obj = JSON.parse(tableData[i]);
			silhouette.push({"name":obj.jobname,"value":obj.silhouette});
			dunn.push({"name":obj.jobname,"value":obj.dunn});
			wssse.push({"name":obj.jobname,"value":obj.costWSSSE});
			utcirmse.push({"name":obj.jobname,"value":obj.utcirmse});
			ideamcirmse.push({"name":obj.jobname,"value":obj.ideamcirmse});
			// silhouette.push({"name":obj.jobname,"value":Math.random()});
			// dunn.push({"name":obj.jobname,"value":Math.random()});
			// wssse.push({"name":obj.jobname,"value":Math.random()});
		}
	}
	var sMatrix = new Matrix(silhouette,"silMatrix",560,560,"Silhouette index dissimilarity matrix","maximise");
	var dMatrix = new Matrix(dunn,"dunnMatrix",560,560,"Dunn index dissimilarity matrix","maximise");
	var wMatrix = new Matrix(wssse,"wssseMatrix",560,560,"WSSSE dissimilarity matrix","minimise");
	var utMatrix = new Matrix(utcirmse,"utciMatrix",560,560,"UTCI rmse dissimilarity matrix","minimise");
	var idMatrix = new Matrix(ideamcirmse,"ideamMatrix",560,560,"IDEAMCI rmse dissimilarity matrix","minimise");
}