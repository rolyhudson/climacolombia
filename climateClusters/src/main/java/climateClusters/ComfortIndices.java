package climateClusters;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import scala.Tuple2;

public class ComfortIndices
{
	static double[] wind;
	static double[] alt;
	static double[] rh;
	static double[] temp;
	static int[] pointsInside;
    /* !~ UTCI, Version a 0.002, October 2009
!~ Copyright (C) 2009  Peter Broede

!~ Program for calculating UTCI Temperature (UTCI)
!~ released for public use after termination of COST Action 730

!~ replaces Version a 0.001, from September 2009

     
     * */
    /// <summary>
    /// Calculate UTCI
    /// </summary>
    /// <param name="Ta">Dry bulb air temp, degC</param>
    /// <param name="va">Air movement, m/s</param>
    /// <param name="Tmrt">Radiant temperature</param>
    /// <param name="RH">Relative humidity, 0-100%</param>
    /// <returns>UTCI 'perceived' temperature, degC. Returns double.min if input is out of range for model</returns>
    public static double CalcUTCI(double Ta, double va, double Tmrt, double RH)
    {

        if (CheckIfInputsValid(Ta, va, Tmrt, RH) != InputsChecks.Pass) return Double.MIN_VALUE;

        double ehPa = es(Ta) * (RH / 100.0);
        double D_Tmrt = Tmrt - Ta;
        double Pa = ehPa / 10.0;//  convert vapour pressure to kPa

        //region whoa_mamma
        double UTCI_approx = Ta +
          (0.607562052) +
          (-0.0227712343) * Ta +
          (8.06470249 * Math.pow(10, (-4))) * Ta * Ta +
          (-1.54271372 * Math.pow(10, (-4))) * Ta * Ta * Ta +
          (-3.24651735 * Math.pow(10, (-6))) * Ta * Ta * Ta * Ta +
          (7.32602852 * Math.pow(10, (-8))) * Ta * Ta * Ta * Ta * Ta +
          (1.35959073 * Math.pow(10, (-9))) * Ta * Ta * Ta * Ta * Ta * Ta +
          (-2.25836520) * va +
          (0.0880326035) * Ta * va +
          (0.00216844454) * Ta * Ta * va +
          (-1.53347087 * Math.pow(10, (-5))) * Ta * Ta * Ta * va +
          (-5.72983704 * Math.pow(10, (-7))) * Ta * Ta * Ta * Ta * va +
          (-2.55090145 * Math.pow(10, (-9))) * Ta * Ta * Ta * Ta * Ta * va +
          (-0.751269505) * va * va +
          (-0.00408350271) * Ta * va * va +
          (-5.21670675 * Math.pow(10, (-5))) * Ta * Ta * va * va +
          (1.94544667 * Math.pow(10, (-6))) * Ta * Ta * Ta * va * va +
          (1.14099531 * Math.pow(10, (-8))) * Ta * Ta * Ta * Ta * va * va +
          (0.158137256) * va * va * va +
          (-6.57263143 * Math.pow(10, (-5))) * Ta * va * va * va +
          (2.22697524 * Math.pow(10, (-7))) * Ta * Ta * va * va * va +
          (-4.16117031 * Math.pow(10, (-8))) * Ta * Ta * Ta * va * va * va +
          (-0.0127762753) * va * va * va * va +
          (9.66891875 * Math.pow(10, (-6))) * Ta * va * va * va * va +
          (2.52785852 * Math.pow(10, (-9))) * Ta * Ta * va * va * va * va +
          (4.56306672 * Math.pow(10, (-4))) * va * va * va * va * va +
          (-1.74202546 * Math.pow(10, (-7))) * Ta * va * va * va * va * va +
          (-5.91491269 * Math.pow(10, (-6))) * va * va * va * va * va * va +
          (0.398374029) * D_Tmrt +
          (1.83945314 * Math.pow(10, (-4))) * Ta * D_Tmrt +
          (-1.73754510 * Math.pow(10, (-4))) * Ta * Ta * D_Tmrt +
          (-7.60781159 * Math.pow(10, (-7))) * Ta * Ta * Ta * D_Tmrt +
          (3.77830287 * Math.pow(10, (-8))) * Ta * Ta * Ta * Ta * D_Tmrt +
          (5.43079673 * Math.pow(10, (-10))) * Ta * Ta * Ta * Ta * Ta * D_Tmrt +
          (-0.0200518269) * va * D_Tmrt +
          (8.92859837 * Math.pow(10, (-4))) * Ta * va * D_Tmrt +
          (3.45433048 * Math.pow(10, (-6))) * Ta * Ta * va * D_Tmrt +
          (-3.77925774 * Math.pow(10, (-7))) * Ta * Ta * Ta * va * D_Tmrt +
          (-1.69699377 * Math.pow(10, (-9))) * Ta * Ta * Ta * Ta * va * D_Tmrt +
          (1.69992415 * Math.pow(10, (-4))) * va * va * D_Tmrt +
          (-4.99204314 * Math.pow(10, (-5))) * Ta * va * va * D_Tmrt +
          (2.47417178 * Math.pow(10, (-7))) * Ta * Ta * va * va * D_Tmrt +
          (1.07596466 * Math.pow(10, (-8))) * Ta * Ta * Ta * va * va * D_Tmrt +
          (8.49242932 * Math.pow(10, (-5))) * va * va * va * D_Tmrt +
          (1.35191328 * Math.pow(10, (-6))) * Ta * va * va * va * D_Tmrt +
          (-6.21531254 * Math.pow(10, (-9))) * Ta * Ta * va * va * va * D_Tmrt +
          (-4.99410301 * Math.pow(10, (-6))) * va * va * va * va * D_Tmrt +
          (-1.89489258 * Math.pow(10, (-8))) * Ta * va * va * va * va * D_Tmrt +
          (8.15300114 * Math.pow(10, (-8))) * va * va * va * va * va * D_Tmrt +
          (7.55043090 * Math.pow(10, (-4))) * D_Tmrt * D_Tmrt +
          (-5.65095215 * Math.pow(10, (-5))) * Ta * D_Tmrt * D_Tmrt +
          (-4.52166564 * Math.pow(10, (-7))) * Ta * Ta * D_Tmrt * D_Tmrt +
          (2.46688878 * Math.pow(10, (-8))) * Ta * Ta * Ta * D_Tmrt * D_Tmrt +
          (2.42674348 * Math.pow(10, (-10))) * Ta * Ta * Ta * Ta * D_Tmrt * D_Tmrt +
          (1.54547250 * Math.pow(10, (-4))) * va * D_Tmrt * D_Tmrt +
          (5.24110970 * Math.pow(10, (-6))) * Ta * va * D_Tmrt * D_Tmrt +
          (-8.75874982 * Math.pow(10, (-8))) * Ta * Ta * va * D_Tmrt * D_Tmrt +
          (-1.50743064 * Math.pow(10, (-9))) * Ta * Ta * Ta * va * D_Tmrt * D_Tmrt +
          (-1.56236307 * Math.pow(10, (-5))) * va * va * D_Tmrt * D_Tmrt +
          (-1.33895614 * Math.pow(10, (-7))) * Ta * va * va * D_Tmrt * D_Tmrt +
          (2.49709824 * Math.pow(10, (-9))) * Ta * Ta * va * va * D_Tmrt * D_Tmrt +
          (6.51711721 * Math.pow(10, (-7))) * va * va * va * D_Tmrt * D_Tmrt +
          (1.94960053 * Math.pow(10, (-9))) * Ta * va * va * va * D_Tmrt * D_Tmrt +
          (-1.00361113 * Math.pow(10, (-8))) * va * va * va * va * D_Tmrt * D_Tmrt +
          (-1.21206673 * Math.pow(10, (-5))) * D_Tmrt * D_Tmrt * D_Tmrt +
          (-2.18203660 * Math.pow(10, (-7))) * Ta * D_Tmrt * D_Tmrt * D_Tmrt +
          (7.51269482 * Math.pow(10, (-9))) * Ta * Ta * D_Tmrt * D_Tmrt * D_Tmrt +
          (9.79063848 * Math.pow(10, (-11))) * Ta * Ta * Ta * D_Tmrt * D_Tmrt * D_Tmrt +
          (1.25006734 * Math.pow(10, (-6))) * va * D_Tmrt * D_Tmrt * D_Tmrt +
          (-1.81584736 * Math.pow(10, (-9))) * Ta * va * D_Tmrt * D_Tmrt * D_Tmrt +
          (-3.52197671 * Math.pow(10, (-10))) * Ta * Ta * va * D_Tmrt * D_Tmrt * D_Tmrt +
          (-3.36514630 * Math.pow(10, (-8))) * va * va * D_Tmrt * D_Tmrt * D_Tmrt +
          (1.35908359 * Math.pow(10, (-10))) * Ta * va * va * D_Tmrt * D_Tmrt * D_Tmrt +
          (4.17032620 * Math.pow(10, (-10))) * va * va * va * D_Tmrt * D_Tmrt * D_Tmrt +
          (-1.30369025 * Math.pow(10, (-9))) * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (4.13908461 * Math.pow(10, (-10))) * Ta * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (9.22652254 * Math.pow(10, (-12))) * Ta * Ta * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (-5.08220384 * Math.pow(10, (-9))) * va * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (-2.24730961 * Math.pow(10, (-11))) * Ta * va * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (1.17139133 * Math.pow(10, (-10))) * va * va * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (6.62154879 * Math.pow(10, (-10))) * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (4.03863260 * Math.pow(10, (-13))) * Ta * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (1.95087203 * Math.pow(10, (-12))) * va * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (-4.73602469 * Math.pow(10, (-12))) * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt +
          (5.12733497) * Pa +
          (-0.312788561) * Ta * Pa +
          (-0.0196701861) * Ta * Ta * Pa +
          (9.99690870 * Math.pow(10, (-4))) * Ta * Ta * Ta * Pa +
          (9.51738512 * Math.pow(10, (-6))) * Ta * Ta * Ta * Ta * Pa +
          (-4.66426341 * Math.pow(10, (-7))) * Ta * Ta * Ta * Ta * Ta * Pa +
          (0.548050612) * va * Pa +
          (-0.00330552823) * Ta * va * Pa +
          (-0.00164119440) * Ta * Ta * va * Pa +
          (-5.16670694 * Math.pow(10, (-6))) * Ta * Ta * Ta * va * Pa +
          (9.52692432 * Math.pow(10, (-7))) * Ta * Ta * Ta * Ta * va * Pa +
          (-0.0429223622) * va * va * Pa +
          (0.00500845667) * Ta * va * va * Pa +
          (1.00601257 * Math.pow(10, (-6))) * Ta * Ta * va * va * Pa +
          (-1.81748644 * Math.pow(10, (-6))) * Ta * Ta * Ta * va * va * Pa +
          (-1.25813502 * Math.pow(10, (-3))) * va * va * va * Pa +
          (-1.79330391 * Math.pow(10, (-4))) * Ta * va * va * va * Pa +
          (2.34994441 * Math.pow(10, (-6))) * Ta * Ta * va * va * va * Pa +
          (1.29735808 * Math.pow(10, (-4))) * va * va * va * va * Pa +
          (1.29064870 * Math.pow(10, (-6))) * Ta * va * va * va * va * Pa +
          (-2.28558686 * Math.pow(10, (-6))) * va * va * va * va * va * Pa +
          (-0.0369476348) * D_Tmrt * Pa +
          (0.00162325322) * Ta * D_Tmrt * Pa +
          (-3.14279680 * Math.pow(10, (-5))) * Ta * Ta * D_Tmrt * Pa +
          (2.59835559 * Math.pow(10, (-6))) * Ta * Ta * Ta * D_Tmrt * Pa +
          (-4.77136523 * Math.pow(10, (-8))) * Ta * Ta * Ta * Ta * D_Tmrt * Pa +
          (8.64203390 * Math.pow(10, (-3))) * va * D_Tmrt * Pa +
          (-6.87405181 * Math.pow(10, (-4))) * Ta * va * D_Tmrt * Pa +
          (-9.13863872 * Math.pow(10, (-6))) * Ta * Ta * va * D_Tmrt * Pa +
          (5.15916806 * Math.pow(10, (-7))) * Ta * Ta * Ta * va * D_Tmrt * Pa +
          (-3.59217476 * Math.pow(10, (-5))) * va * va * D_Tmrt * Pa +
          (3.28696511 * Math.pow(10, (-5))) * Ta * va * va * D_Tmrt * Pa +
          (-7.10542454 * Math.pow(10, (-7))) * Ta * Ta * va * va * D_Tmrt * Pa +
          (-1.24382300 * Math.pow(10, (-5))) * va * va * va * D_Tmrt * Pa +
          (-7.38584400 * Math.pow(10, (-9))) * Ta * va * va * va * D_Tmrt * Pa +
          (2.20609296 * Math.pow(10, (-7))) * va * va * va * va * D_Tmrt * Pa +
          (-7.32469180 * Math.pow(10, (-4))) * D_Tmrt * D_Tmrt * Pa +
          (-1.87381964 * Math.pow(10, (-5))) * Ta * D_Tmrt * D_Tmrt * Pa +
          (4.80925239 * Math.pow(10, (-6))) * Ta * Ta * D_Tmrt * D_Tmrt * Pa +
          (-8.75492040 * Math.pow(10, (-8))) * Ta * Ta * Ta * D_Tmrt * D_Tmrt * Pa +
          (2.77862930 * Math.pow(10, (-5))) * va * D_Tmrt * D_Tmrt * Pa +
          (-5.06004592 * Math.pow(10, (-6))) * Ta * va * D_Tmrt * D_Tmrt * Pa +
          (1.14325367 * Math.pow(10, (-7))) * Ta * Ta * va * D_Tmrt * D_Tmrt * Pa +
          (2.53016723 * Math.pow(10, (-6))) * va * va * D_Tmrt * D_Tmrt * Pa +
          (-1.72857035 * Math.pow(10, (-8))) * Ta * va * va * D_Tmrt * D_Tmrt * Pa +
          (-3.95079398 * Math.pow(10, (-8))) * va * va * va * D_Tmrt * D_Tmrt * Pa +
          (-3.59413173 * Math.pow(10, (-7))) * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (7.04388046 * Math.pow(10, (-7))) * Ta * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (-1.89309167 * Math.pow(10, (-8))) * Ta * Ta * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (-4.79768731 * Math.pow(10, (-7))) * va * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (7.96079978 * Math.pow(10, (-9))) * Ta * va * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (1.62897058 * Math.pow(10, (-9))) * va * va * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (3.94367674 * Math.pow(10, (-8))) * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (-1.18566247 * Math.pow(10, (-9))) * Ta * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (3.34678041 * Math.pow(10, (-10))) * va * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (-1.15606447 * Math.pow(10, (-10))) * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * Pa +
          (-2.80626406) * Pa * Pa +
          (0.548712484) * Ta * Pa * Pa +
          (-0.00399428410) * Ta * Ta * Pa * Pa +
          (-9.54009191 * Math.pow(10, (-4))) * Ta * Ta * Ta * Pa * Pa +
          (1.93090978 * Math.pow(10, (-5))) * Ta * Ta * Ta * Ta * Pa * Pa +
          (-0.308806365) * va * Pa * Pa +
          (0.0116952364) * Ta * va * Pa * Pa +
          (4.95271903 * Math.pow(10, (-4))) * Ta * Ta * va * Pa * Pa +
          (-1.90710882 * Math.pow(10, (-5))) * Ta * Ta * Ta * va * Pa * Pa +
          (0.00210787756) * va * va * Pa * Pa +
          (-6.98445738 * Math.pow(10, (-4))) * Ta * va * va * Pa * Pa +
          (2.30109073 * Math.pow(10, (-5))) * Ta * Ta * va * va * Pa * Pa +
          (4.17856590 * Math.pow(10, (-4))) * va * va * va * Pa * Pa +
          (-1.27043871 * Math.pow(10, (-5))) * Ta * va * va * va * Pa * Pa +
          (-3.04620472 * Math.pow(10, (-6))) * va * va * va * va * Pa * Pa +
          (0.0514507424) * D_Tmrt * Pa * Pa +
          (-0.00432510997) * Ta * D_Tmrt * Pa * Pa +
          (8.99281156 * Math.pow(10, (-5))) * Ta * Ta * D_Tmrt * Pa * Pa +
          (-7.14663943 * Math.pow(10, (-7))) * Ta * Ta * Ta * D_Tmrt * Pa * Pa +
          (-2.66016305 * Math.pow(10, (-4))) * va * D_Tmrt * Pa * Pa +
          (2.63789586 * Math.pow(10, (-4))) * Ta * va * D_Tmrt * Pa * Pa +
          (-7.01199003 * Math.pow(10, (-6))) * Ta * Ta * va * D_Tmrt * Pa * Pa +
          (-1.06823306 * Math.pow(10, (-4))) * va * va * D_Tmrt * Pa * Pa +
          (3.61341136 * Math.pow(10, (-6))) * Ta * va * va * D_Tmrt * Pa * Pa +
          (2.29748967 * Math.pow(10, (-7))) * va * va * va * D_Tmrt * Pa * Pa +
          (3.04788893 * Math.pow(10, (-4))) * D_Tmrt * D_Tmrt * Pa * Pa +
          (-6.42070836 * Math.pow(10, (-5))) * Ta * D_Tmrt * D_Tmrt * Pa * Pa +
          (1.16257971 * Math.pow(10, (-6))) * Ta * Ta * D_Tmrt * D_Tmrt * Pa * Pa +
          (7.68023384 * Math.pow(10, (-6))) * va * D_Tmrt * D_Tmrt * Pa * Pa +
          (-5.47446896 * Math.pow(10, (-7))) * Ta * va * D_Tmrt * D_Tmrt * Pa * Pa +
          (-3.59937910 * Math.pow(10, (-8))) * va * va * D_Tmrt * D_Tmrt * Pa * Pa +
          (-4.36497725 * Math.pow(10, (-6))) * D_Tmrt * D_Tmrt * D_Tmrt * Pa * Pa +
          (1.68737969 * Math.pow(10, (-7))) * Ta * D_Tmrt * D_Tmrt * D_Tmrt * Pa * Pa +
          (2.67489271 * Math.pow(10, (-8))) * va * D_Tmrt * D_Tmrt * D_Tmrt * Pa * Pa +
          (3.23926897 * Math.pow(10, (-9))) * D_Tmrt * D_Tmrt * D_Tmrt * D_Tmrt * Pa * Pa +
          (-0.0353874123) * Pa * Pa * Pa +
          (-0.221201190) * Ta * Pa * Pa * Pa +
          (0.0155126038) * Ta * Ta * Pa * Pa * Pa +
          (-2.63917279 * Math.pow(10, (-4))) * Ta * Ta * Ta * Pa * Pa * Pa +
          (0.0453433455) * va * Pa * Pa * Pa +
          (-0.00432943862) * Ta * va * Pa * Pa * Pa +
          (1.45389826 * Math.pow(10, (-4))) * Ta * Ta * va * Pa * Pa * Pa +
          (2.17508610 * Math.pow(10, (-4))) * va * va * Pa * Pa * Pa +
          (-6.66724702 * Math.pow(10, (-5))) * Ta * va * va * Pa * Pa * Pa +
          (3.33217140 * Math.pow(10, (-5))) * va * va * va * Pa * Pa * Pa +
          (-0.00226921615) * D_Tmrt * Pa * Pa * Pa +
          (3.80261982 * Math.pow(10, (-4))) * Ta * D_Tmrt * Pa * Pa * Pa +
          (-5.45314314 * Math.pow(10, (-9))) * Ta * Ta * D_Tmrt * Pa * Pa * Pa +
          (-7.96355448 * Math.pow(10, (-4))) * va * D_Tmrt * Pa * Pa * Pa +
          (2.53458034 * Math.pow(10, (-5))) * Ta * va * D_Tmrt * Pa * Pa * Pa +
          (-6.31223658 * Math.pow(10, (-6))) * va * va * D_Tmrt * Pa * Pa * Pa +
          (3.02122035 * Math.pow(10, (-4))) * D_Tmrt * D_Tmrt * Pa * Pa * Pa +
          (-4.77403547 * Math.pow(10, (-6))) * Ta * D_Tmrt * D_Tmrt * Pa * Pa * Pa +
          (1.73825715 * Math.pow(10, (-6))) * va * D_Tmrt * D_Tmrt * Pa * Pa * Pa +
          (-4.09087898 * Math.pow(10, (-7))) * D_Tmrt * D_Tmrt * D_Tmrt * Pa * Pa * Pa +
          (0.614155345) * Pa * Pa * Pa * Pa +
          (-0.0616755931) * Ta * Pa * Pa * Pa * Pa +
          (0.00133374846) * Ta * Ta * Pa * Pa * Pa * Pa +
          (0.00355375387) * va * Pa * Pa * Pa * Pa +
          (-5.13027851 * Math.pow(10, (-4))) * Ta * va * Pa * Pa * Pa * Pa +
          (1.02449757 * Math.pow(10, (-4))) * va * va * Pa * Pa * Pa * Pa +
          (-0.00148526421) * D_Tmrt * Pa * Pa * Pa * Pa +
          (-4.11469183 * Math.pow(10, (-5))) * Ta * D_Tmrt * Pa * Pa * Pa * Pa +
          (-6.80434415 * Math.pow(10, (-6))) * va * D_Tmrt * Pa * Pa * Pa * Pa +
          (-9.77675906 * Math.pow(10, (-6))) * D_Tmrt * D_Tmrt * Pa * Pa * Pa * Pa +
          (0.0882773108) * Pa * Pa * Pa * Pa * Pa +
          (-0.00301859306) * Ta * Pa * Pa * Pa * Pa * Pa +
          (0.00104452989) * va * Pa * Pa * Pa * Pa * Pa +
          (2.47090539 * Math.pow(10, (-4))) * D_Tmrt * Pa * Pa * Pa * Pa * Pa +
          (0.00148348065) * Pa * Pa * Pa * Pa * Pa * Pa;
        //endregion

        return UTCI_approx;
    }


    /// <summary>
    /// Calc saturation vapour pressure
    /// </summary>
    /// <param name="ta">Input air temperature, degC</param>
    /// <returns></returns>
    private static double es(double ta)
    {
        //calculates saturation vapour pressure over water in hPa for input air temperature (ta) in celsius according to:
        //Hardy, R.; ITS-90 Formulations for Vapor Pressure, Frostpoint Temperature, Dewpoint Temperature and Enhancement Factors in the Range -100 to 100 °C; 
        //Proceedings of Third International Symposium on Humidity and Moisture; edited by National Physical Laboratory (NPL), London, 1998, pp. 214-221
        //http://www.thunderscientific.com/tech_info/reflibrary/its90formulas.pdf (retrieved 2008-10-01)

        double[] g = new double[] { -2836.5744, -6028.076559, 19.54263612, -0.02737830188, 0.000016261698, ((double)(7.0229056 * Math.pow(10, -10))), ((double)(-1.8680009 * Math.pow(10, -13))) };
        double tk = ta + 273.15;
        double es = 2.7150305 * Math.log(tk);
        //for count, i in enumerate(g):
        for (int count = 0; count < g.length; count++)
        {
            double i = g[count];
            es = es + (i * Math.pow(tk, (count - 2)));
        }
        es = Math.exp(es) * 0.01;
        return es;
    }


    public static InputsChecks CheckIfInputsValid(double Ta, double va, double Tmrt, double hum)
    {

        if (Ta < -50.0 || Ta > 50.0) return InputsChecks.Temp_OutOfRange;
        if (Tmrt - Ta < -30.0 || Tmrt - Ta > 70.0) return InputsChecks.Large_Gap_Between_Trmt_Ta;
        if (va < 0.0) return InputsChecks.WindSpeed_Too_Low;
        if (va > 17) return InputsChecks.WindSpeed_TooHigh;
        return InputsChecks.Pass;
    }

    public enum InputsChecks { Temp_OutOfRange, Large_Gap_Between_Trmt_Ta, WindSpeed_Too_Low, WindSpeed_TooHigh, Pass, Unknown }

    public static double ideamIC(double alt, double t, double rh, double w)
    {
        //http://documentacion.ideam.gov.co/openbiblio/bvirtual/007574/Metodologiaconfort.pdf
        double ic = 0;
        if (alt < 1000)
        {
            //IC = (36.5 - t s ) (0.05 + 0.04 sqrt(v) + h / 250) para elevaciones inferiores a 1.000 metros(7)
            ic = (36.5 - t) * (0.05 + 0.04 * Math.sqrt(w) + rh / 250);
        }
        if (alt >= 1000 && alt < 2000)
        {
            //IC = (34.5 - t s ) (0.05 + 0.06  sqrt(v) + h / 180) para elevaciones entre 1.000 y 2.000 metros(8)
            ic = (34.5 - t) * (0.05 + 0.06 * Math.sqrt(w) + rh / 180);
        }
        if (alt >= 2000)
        {
            //IC = (33.5 - t s ) (0.05 + 0.18  sqrt(v) + h / 160) para elevaciones superiores a 2.000 metros(9)
            ic = (33.5 - t) * (0.05 + 0.18 * Math.sqrt(w) + rh / 160);
        }
        
        return ic;
    } 
    public static int ideamICClass(double ic)
    {
        if (ic <= 3) return 0;//Muy caluroso
        if (ic > 3 && ic <= 5) return 1;//3.1 a 5 Caluroso
        if (ic > 5 && ic <= 7) return 2;//5.1 a 7 Cálido
        if (ic > 7 && ic <= 11) return 3;//7.1 a 11 Agradable
        if (ic > 11 && ic <= 13) return 4;//11.1 a 13 Algo frío
        if (ic > 13 && ic <= 15) return 5;//13.1 a 15 Frío
        if (ic > 15) return 6;//Más de 15 Muy frío
        return 0;
    }
    public static double appTemp(double t, double vp, double w)
    {
        double at = 0;
        at = t + 0.33 * vp - 0.7 * w - 4;
        return at;
    }
    public static double vapourPressFromRH(double rh, double t)
    {
        double vp = rh / 100 * 6.105 * Math.exp(17.27 * t / (237.7 + t));
        return vp;
    }
    private static void averageComfortIndex(JavaRDD<Record> records,int nclusters) {
    	JavaPairRDD<Integer , Iterable<Vector>> comfortRecords = records
				.mapToPair(f->ClusterUtils.getComfortIndices(f)).groupByKey();
    	
    	JavaPairRDD<Integer,Vector> typicalyear = comfortRecords.mapToPair(d->{
			Iterable<Vector> it = d._2;
			double sumideamci = 0;
			double sumutci = 0;
			int count=0;
			for (Vector cPair : it) {
				double[] vals = cPair.toArray();
			    sumutci += vals[0];
			    sumideamci +=vals[1];
			    count++;
			}
			Vector avIndices = Vectors.dense(sumutci /count,sumideamci/count);
			Tuple2<Integer,Vector> result = new Tuple2<Integer,Vector>(d._1,avIndices);
			return result; 
		});
    }
    public static double[][] getComfortIndicesClusters(JavaRDD<Record> records,int nclusters) {
    	
    	JavaPairRDD<Integer , Iterable<Vector>> comfortRecords = records
				.mapToPair(f->ClusterUtils.getComfortIndices(f)).groupByKey();
    	//comfort index vector is utci, ideamci,temp, rh,trange,windspd
    	JavaPairRDD<Integer,Vector> clusterComfort = comfortRecords.mapToPair(d->{
			Iterable<Vector> it = d._2;
			double sumideamci = 0;
			double sumutci = 0;
			double sumtemp = 0;
			double sumrh = 0;
			double maxtemp=-30;
			double mintemp=50;
			double maxtemprange=0;
			double mintemprange=100;
			double maxrh=-30;
			double minrh=100;
			double maxws=-30;
			double minws=100;
			int count=0;
			for (Vector cPair : it) {
				double[] vals = cPair.toArray();
			    sumutci += vals[0];
			    sumideamci +=vals[1];
			    sumtemp+=vals[2];
			    sumrh+=vals[3];
			    //set max mins temp is index 2
			    if(vals[2]>maxtemp)maxtemp=vals[2];
			    if(vals[2]<mintemp)mintemp=vals[2];
			  //set max mins rh is index 3
			    if(vals[3]>maxrh)maxrh=vals[3];
			    if(vals[3]<minrh)minrh=vals[3];
			  //set max mins temprange is index 4
			    if(vals[4]>maxtemprange)maxtemprange=vals[4];
			    if(vals[4]<mintemprange)mintemprange=vals[4];
			  //set max mins windrange is index 5
			    if(vals[5]>maxws)maxws=vals[5];
			    if(vals[5]<minws)minws=vals[5];
			    count++;
			}
			
			Vector avIndices = Vectors.dense(sumutci /count,sumideamci/count,sumtemp/count,sumrh/count,maxtemp,mintemp,maxtemprange,mintemprange,maxrh,minrh,maxws,minws);
			Tuple2<Integer,Vector> result = new Tuple2<Integer,Vector>(d._1,avIndices);
			return result; 
		});
    	double[][] indices = new double[nclusters][];
    	double[] utci = new double[nclusters];
    	double[] ideamCI = new double[nclusters];
    	//temp,vp,rh,tmin,tmax,trange,precip,windSpd
    	List<Tuple2<Integer,Vector>> kvs = clusterComfort.collect();
    	for(int i=0;i<kvs.size();i++) {
    		double[] vals = kvs.get(i)._2.toArray();
    		int cnum =kvs.get(i)._1$mcI$sp();
    		indices[cnum] = vals;
    	}
    	
    	
    	
    	return indices;
    }
}


