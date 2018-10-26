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
	for(var i=0;i<tableData.length;i++){
		if(tableData[i]!=""){
			var obj = JSON.parse(tableData[i]);
			// silhouette.push({"name":obj.jobname,"value":obj.silhouette});
			// dunn.push({"name":obj.jobname,"value":obj.dunn});
			// wssse.push({"name":obj.jobname,"value":obj.costWSSSE});

			silhouette.push({"name":obj.jobname,"value":Math.random()});
			dunn.push({"name":obj.jobname,"value":Math.random()});
			wssse.push({"name":obj.jobname,"value":Math.random()});
		}
	}
	var sMatrix = new Matrix(silhouette,"silMatrix",500,500,"Silhouette index dissimilarity matrix");
	var dMatrix = new Matrix(dunn,"dunnMatrix",500,500,"Dunn index dissimilarity matrix");
	var wMatrix = new Matrix(wssse,"wssseMatrix",500,500,"WSSSE dissimilarity matrix");
}