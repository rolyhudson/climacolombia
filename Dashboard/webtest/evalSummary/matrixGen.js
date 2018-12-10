function makeMatrix(){
 readData("evalSummary/perf.json",processData);
 makeTablesFromCSV();
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
function makeTablesFromCSV(){
	readData("evalSummary/rankedResults.csv",parseCSVResultsToTable);
	readData("evalSummary/jobsDescrip.csv",parseCSVDescriptionsToTable);
}
function parseCSVDescriptionsToTable(error,data){
var tableData = data.split(/\r?\n/);
var tbl = document.createElement("TABLE");
	var tbdy = document.createElement('tbody');
for(var i=0;i<tableData.length;i++){
	if(tableData[i]!=""){
		

		var cells = tableData[i].split(",");
		var tr = document.createElement('tr');
		if(i%2===0)tr.style.backgroundColor = "#f2f2f2";
		for(var c=0;c<cells.length;c++){
			var td;
			if(i==0) td = document.createElement('th');
			else td = document.createElement('td');

			if(cells[c].includes("http"))
			{
				var link = document.createElement("a");
				link.href = cells[c];
				link.innerHTML = "results";
				td.appendChild(link);
			}
			else{
				td.appendChild(document.createTextNode(cells[c]));
			}
			
			tr.appendChild(td);
		}
		tbdy.appendChild(tr);
	}
}

	tbl.appendChild(tbdy);
	document.getElementById("workflowDescriptions").appendChild(tbl);
}
function parseCSVResultsToTable(error,data){
var tableData = data.split(/\r?\n/);
var tbl = document.createElement("TABLE");
	var tbdy = document.createElement('tbody');
for(var i=0;i<tableData.length;i++){
	if(tableData[i]!=""){
		var cells = tableData[i].split(",");
		var tr = document.createElement('tr');
		for(var c=0;c<cells.length;c++){
			var td;
			if(i==0||i==1) td = document.createElement('th');
			else td = document.createElement('td');
			td.appendChild(document.createTextNode(cells[c]));
			if(c===2||c===3||c===4) td.style.backgroundColor= "#f2f2f2";
			if(c===7||c===8) td.style.backgroundColor= "#f2f2f2";
			if(c===11||c===12||c===13||c==14) td.style.backgroundColor= "#f2f2f2";
			if(c==19) td.style.backgroundColor= "#f2f2f2";
			if(c==0) td.style.backgroundColor= "#f2f2f2";
			tr.appendChild(td);
			tr.appendChild(td);
		}
		tbdy.appendChild(tr);
	}
}

	tbl.appendChild(tbdy);
	document.getElementById("rankedResults").appendChild(tbl);
}