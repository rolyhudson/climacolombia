function saturationPress(temp) {
    //convert temp C to kelvin
    var kelvin = temp +273.15;
    var c=[]
    var pascals=0;
    if(kelvin<273.15){
        c[0] = -5674.5359;
        c[1] = 6.3925247;
        c[2] = -0.9677843 * Math.pow(10, -2);
        c[3] = 0.62215701 * Math.pow(10, -6);
        c[4] = 0.20747825 * Math.pow(10, -8);
        c[5] = -0.9484024 * Math.pow(10, -12);
        c[6] = 4.1635019;
    }   
    else{
         c[0] = -5800.2206;
        c[1] = 1.3914993;
        c[2] = -0.048640239;
        c[3] = 0.41764768 * Math.pow(10, -4);
        c[4] = -0.14452093 * Math.pow(10, -7);
        c[5] = 0;
        c[6] = 6.5459673;
    }
    
    for(var i=0;i<6;i++){
    pascals+=c[i]*Math.pow(kelvin,i-1)
    }
    pascals += c[6]*Math.log(kelvin);
    pascals = Math.exp(pascals);
    return pascals;
}    
function relHumidfromHumidRatioTemp(t,hr,atmosP){
 var partPress = partPressure(hr,atmosP);
 var satPress = saturationPress(t);
 var rh = relHumid(partPress, satPress);
 return rh;
}   
function partPress(relHumid, saturationPress){
    //relhumid 0>1
    return relHumid*saturationPress/100;

}
function relHumid(partPress, saturationPress){
    //relhumid 0>1
    return partPress*100/saturationPress;

}

function humidtyRatio(atmosP,pw){//pw is partial pressure of water vapour
    var w = (0.62198*pw)/(atmosP-pw);
    return w;
}
function partPressure(humidtyRatio,atmosP){
var pp= (50000*atmosP*humidtyRatio)/(50000*humidtyRatio+31099);
return pp;
}
function antoniePartPress(t){
  var T =t+273.15;
 var A = 8.07131+2.124903, B=1730.63,C=233.426-273.15;
 var pp = Math.pow(10,A-(B/(C+T)));
 return pp;//in pa
}
function getStandardPressure(altitude)   // input is m Returns result in Pascals
  {
    // Below 51 km: Practical Meteorology by Roland Stull, pg 12
    // Above 51 km: http://www.braeunig.us/space/atmmodel.htm
    // Validation data: https://www.avs.org/AVS/files/c7/c7edaedb-95b2-438f-adfb-36de54f87b9e.pdf

    altitude = altitude / 1000.0;  // Convert m to km
    var geopot_height = getGeopotential(altitude);

    var t = getStandardTemperature(geopot_height);

    if (geopot_height <= 11)
      return  101325 * Math.pow(288.15 / t, -5.255877);
    else if (geopot_height <= 20)
      return 22632.06 * Math.Exp(-0.1577 * (geopot_height - 11));
    else if (geopot_height <= 32)
      return 5474.889 * Math.pow(216.65 / t, 34.16319);
    else if (geopot_height <= 47)
      return 868.0187 * Math.pow(228.65 / t, 12.2011);
    else if (geopot_height <= 51)
      return 110.9063 * Math.Exp(-0.1262 * (geopot_height - 47));
    else if (geopot_height <= 71)
      return 66.93887 * Math.pow(270.65 / t, -12.2011);
    else if (geopot_height <= 84.85)
      return 3.956420 * Math.pow(214.65 / t, -17.0816);

    //throw std::out_of_range("altitude must be less than 86 km.");
    return -1;
  }

  // geopot_height = earth_radius * altitude / (earth_radius + altitude) /// All in km
  // Temperature is in Kelvin = 273.15 + Celsius
function getStandardTemperature(geopot_height)
  {
    // Standard atmospheric pressure
    // Below 51 km: Practical Meteorology by Roland Stull, pg 12
    // Above 51 km: http://www.braeunig.us/space/atmmodel.htm

    if (geopot_height <= 11)          // Troposphere
      return 288.15 - (6.5 * geopot_height);
    else if (geopot_height <= 20)     // Stratosphere starts
      return 216.65;
    else if (geopot_height <= 32)
      return 196.65 + geopot_height;
    else if (geopot_height <= 47)
      return 228.65 + 2.8 * (geopot_height - 32);
    else if (geopot_height <= 51)     // Mesosphere starts
      return 270.65;
    else if (geopot_height <= 71)
      return 270.65 - 2.8 * (geopot_height - 51);
    else if (geopot_height <= 84.85)
      return 214.65 - 2 * (geopot_height - 71);
    // Thermosphere has high kinetic temperature (500 C to 2000 C) but temperature
    // as measured by a thermometer would be very low because of almost vacuum.

    //throw std::out_of_range("geopot_height must be less than 84.85 km.")
    return -1;
  }

  function getGeopotential(altitude_km)
  {
    var EARTH_RADIUS = 6356.766; // km

    return EARTH_RADIUS * altitude_km / (EARTH_RADIUS + altitude_km);
  }