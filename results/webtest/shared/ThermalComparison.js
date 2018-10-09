class ThermalComparison{
	constructor(mapdivs,props,thermaldata,context,country,scaletitle){
		this.mapDivs =mapdivs;
		this.mapProps = props;
		this.maps=[];
		this.context =context;
		this.country=country;
		this.scaleTitle =scaletitle;
		this.addScaleDiv();

		this.maph = document.getElementById(mapdivs[0]).clientHeight-document.getElementById(mapdivs[0]).childNodes[0].clientHeight;
		this.mapw = document.getElementById(mapdivs[0]).clientWidth;
		this.setupMaps();
	}
	addScaleDiv(){
		for(var i=0;i<this.mapDivs.length;i++){
		var scalediv = document.createElement("div");
		scalediv.id = this.mapDivs[i]+"scale";
		document.getElementById(this.mapDivs[i]).appendChild(scalediv);
		}
	}
	setupMaps(){
		for(var i=0;i<this.mapDivs.length;i++){
			var colorf;
			if(this.mapDivs[i].includes("edutci")) colorf=this.getColorUtci;
			if(this.mapDivs[i].includes("edideam")) colorf=this.getColorIdeamci;
			if(this.mapDivs[i].includes("delta")) colorf=this.getColorDelta;
			var map =new MapGrid(this.mapDivs[i],this.mapw,this.maph,this.context,this.country,this.mapDivs[i]+"Map",this.mapProps[i],this.scaleTitle,colorf);
			this.maps.push(map);
		}
		
	}
	
	updateMaps(data){
		for(var i=0;i<this.maps.length;i++){
			this.maps[i].makeScaleBarMaxMin(data);
			this.maps[i].mapUpdate(data);
		}
	}
	getColorUtci(t){
		var cScale = d3.scaleLinear()
		.domain([-5,35])
		.range([1, 0]);
		return d3.interpolateSpectral(cScale(t));
	}
	getColorIdeamci(t){
		var cScale = d3.scaleLinear()
		.domain([0,24])
		.range([0,1]);
		return d3.interpolateSpectral(cScale(t));
	}
	getColorDelta(t){
		var cScale = d3.scaleLinear()
		.domain([-5,15])
		.range([1, 0]);
		return d3.interpolateCool(cScale(t));
	}
}