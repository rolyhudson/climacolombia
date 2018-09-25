var params = {ta:23,tr:24,vel:0,rh:50,met:1,clo:1,wme:0};

function test(){
	var comfort = pmvppd(params.ta, params.tr, params.vel,params.rh, params.met, params.clo, params.wme);
	getComfortBoundary(0.5);
}

function pmvppd(ta, tr, vel, rh, met, clo, wme){
	//coverted from BASIC program in BS EN ISO 7730 - 2005
	// ta;//air temp
	// tr;//mean radiant temp
	// vel;//relative air velocity
	// rh; //relative humidty
	// met;//Metabolic rate
	// Clo;//Clothing 
	// wme;//External work, normally around 0  
	 
	var fcl;// clothing area factor
	var pa = rh*10*saturatedVapPress(ta);//water vapour pressure Pa

	var icl =0.155*clo;//thermal insulation of the clothing in m 2 K/W
	var m = met*58.15;// metabolic rate in W/m 2
	var w = wme*58.15;//external work in W/m 2
	var mw = m-w;//internal heat production in the human body
	if(icl<=0.078) fcl=1+(1.29*icl);
	else fcl = 1.05+(0.645*icl);
	var hcf = 12.1*Math.sqrt(vel);//heat transfer coeff by frced convection
	var taa = ta+273;//air temp kelvin
	var tra = tr+273;//mean radiant temp in kelvin

	//calc surf temp by iteration
	var surft = surfTemp(taa,icl,fcl,ta,mw,tra,hcf);
	
	//heat loss components
	var hl1=3.05 * 0.001 *(5733-6.99 * mw-pa);//heat loss diff. through skin
	var hl2;//heat loss by sweating (comfort)
	if(mw>58.15)hl2 = 0.42*(mw-58.15);
	else hl2 = 0;
	var hl3 = 1.7*0.00001*m*(5867-pa);//latent respiration heat loss
	var hl4 = 0.0014*m*(34-ta);//dry respiration heat loss
	var hl5 = 3.96*fcl*(Math.pow(surft.xn,4)-Math.pow(tra/100,4));// heat loss by radiation
 	var hl6 = fcl*surft.hc*(surft.tcla-ta);// heat loss by convection missing from the BASIC code!
	//calc pmv and ppd
	var ts = 0.303*Math.exp(-0.036*m)+0.028;//thermal sensation trans coeff
	var pmv = ts*(mw-hl1-hl2-hl3-hl4-hl5-hl6);//predicted mean vote
	var ppd = 100 - 95 * Math.exp(-0.03353 * Math.pow(pmv,4) - 0.2179 * Math.pow(pmv, 2));// predicted percentage dissat.
var results = {"pmv":pmv,"ppd":ppd};
	return results;
		
}
function saturatedVapPress(t){
	return Math.exp(16.6536-4030.183/(t+235));

}
function surfTemp(taa,icl,fcl,ta,mw,tra,hcf){
	var tcla = taa + (35.5-ta) / (3.5 * icl + 0.1);//first guess surf temp clothing 

	var p1 = icl*fcl;
	var p2 = p1*3.96;
	var p3 = p1*100;
	var p4 = p1*taa;
	var p5 = 308.7 - 0.028 * mw + p2 * Math.pow(tra/100 , 4);
	var xn = tcla/100;
	var xf=tcla/50;
	var n=0;
	var eps = 0.00015;//stop criteria
	var hcn,hc;
	while(Math.abs(xn-xf)>eps){
		xf=(xf+xn)/2;
		hcn = 2.38 * Math.pow(Math.abs(100 * xf - taa) ,0.25);// heat transf. coeff. by natural convection
		if(hcf>hcn)hc=hcf;
		else hc=hcn;
		xn =  (p5 + p4 * hc - p2 * Math.pow(xf, 4)) / (100 + p3 * hc);
		n++;
		if (n > 150) {
            alert('iterations exceeded');
            return 1;
        }
	}
	var surfTemp ={};
	surfTemp.tcla = 100*xn-273;//surface temperature of the clothing
	surfTemp.hc = hc;
	surfTemp.xn = xn;
	return surfTemp;
}
//from http://comfort.cbe.berkeley.edu/static/js/util.js
function bisect (a, b, fn, epsilon, target) {
    var a_T, b_T, midpoint, midpoint_T;
    while (Math.abs(b - a) > 2 * epsilon) {
        midpoint = (b + a) / 2;
        a_T = fn(a);
        b_T = fn(b);
        midpoint_T = fn(midpoint);
        if ((a_T - target) * (midpoint_T - target) < 0) b = midpoint;
        else if ((b_T - target) * (midpoint_T - target) < 0) a = midpoint;
        else return -999;
    }
    return midpoint;
}
//from http://comfort.cbe.berkeley.edu/static/js/util.js
function secant(a, b, fn, epsilon) {
  // root-finding only
  var f1 = fn(a)
  if (Math.abs(f1) <= epsilon) return a
  var f2 = fn(b)
  if (Math.abs(f2) <= epsilon) return b
  var slope, c, f3
  for (var i = 0; i < 100; i++){
    slope = (f2 - f1) / (b - a)
    c = b - f2/slope
    f3 = fn(c)
    if (Math.abs(f3) < epsilon) return c
    a = b
    b = c
    f1 = f2
    f2 = f3
  }
  return NaN
}


function getComfortBoundary(cTarget){

var bound = [];
	//get lower limit
	for(var rh =0;rh<=100;rh+=10){
		bound.push(solver(rh, -cTarget));
	}
	//set top edge along the 100%curve
	rh = 100;
	var t = bound[bound.length-1].dbt;
	var comf= {"pmv":0,"ppd":0};
	var satPress;
    var partialPress;
	for(;;){
		comf = pmvppd(t, params.tr, params.vel,rh, params.met, params.clo, params.wme);
		if(comf.pmv>cTarget) break;
		else{
			satPress= saturationPress(t);
		partialPress = partPress(rh,satPress);
		bound.push({"dbt": t,"hr":humidtyRatio(atmosPress,partialPress)});
		t+=0.2
		}
	}
	//get upper limit
	for(var rh =100;rh>=0;rh-=10){
		bound.push(solver(rh, cTarget));
	}
	return bound;
}
function solver(rh, comfortTarget){
	var epsilon = 0.001 // ta precision
	var a = -50
	var b = 50
	var fn  = checkPmv(rh, comfortTarget);

	var t = secant(a, b, fn, epsilon)
    if (isNaN(t)) {
        t = bisect(a, b, fn, epsilon, 0);
    }
    satPress= saturationPress(t);
      partialPress = partPress(rh,satPress); 

    return {"dbt": t,"hr":humidtyRatio(atmosPress,partialPress)};
    
}
function checkPmv(rh, comfortTarget){
	//this gets the function for the solver
	return function(db){
		return pmvppd(db, params.tr, params.vel,rh, params.met, params.clo, params.wme).pmv - comfortTarget;
	}
}
function isPointInPolygon(point, vs) {
   // ray-casting algorithm based on
    // http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html

    var x = point[0], y = point[1];

    var inside = false;
    for (var i = 0, j = vs.length - 1; i < vs.length; j = i++) {
        var xi = vs[i][0], yi = vs[i][1];
        var xj = vs[j][0], yj = vs[j][1];

        var intersect = ((yi > y) != (yj > y))
            && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
        if (intersect) inside = !inside;
    }

    return inside;
}
function hoursInComfortZ(climate, comfort){
	var hours=0;
	for(var i=0;i<climate.length;i++){
		if(isPointInPolygon(climate[i],comfort))hours++;
	}
	return hours;
}
// this.findComfortBoundary = function(d, pmvlimit) {
//         var boundary = []

//         function rhclos(rhx, target) {
//             return function(db) {
//                 if($("#chartSelect").val() == "psychtop"){
//                     return comf.pmvElevatedAirspeed(db, db, d.vel, rhx, d.met, d.clo, 0).pmv - target
//                 } else {
//                     return comf.pmvElevatedAirspeed(db, d.tr, d.vel, rhx, d.met, d.clo, 0).pmv - target
//                 }
//             }
//         }
//         function solve(rhx, target) {
//             var epsilon = 0.001 // ta precision
//             var a = -50
//             var b = 50
//             var fn = rhclos(rhx, target)
//             t = util.secant(a, b, fn, epsilon)
//             if (isNaN(t)) {
//                 t = util.bisect(a, b, fn, epsilon, 0)
//             }
//             return {
//                 "db": t,
//                 "hr": pc.getHumRatio(t, rhx)
//             }
//         }

//         var incr = 10;
//         for (var rhx = 0; rhx <= 100; rhx += incr) {
//             boundary.push(solve(rhx, -pmvlimit))
//         }
//         while (true) {
//             t += 0.5
//             boundary.push({
//                 "db": t,
//                 "hr": pc.getHumRatio(t, 100)
//             })
//             if (comf.pmvElevatedAirspeed(t, d.tr, d.vel, rhx, d.met, d.clo, 0).pmv > pmvlimit) break
//         }
//         for (var rhx = 100; rhx >= 0; rhx -= incr) {
//             boundary.push(solve(rhx, pmvlimit))
//         }
//         return boundary
//     }
