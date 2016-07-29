/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package swrb;


import java.io.IOException;

import oms3.annotations.Author;
import oms3.annotations.Bibliography;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


@Description("Calculate the amount of direct, diffuse and top atomosphere shortwave radiation .")
@Documentation("")
@Author(name = "Marialaura Bancheri, Giuseppe Formetta, Daniele Andreis and Riccardo Rigon", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Radiation, SkyviewFactor, Hillshade")
@Bibliography("Corripio (2002), Corripio (2003), Formetta (2013)")
@Label("")
@Name("shortradbal")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class ShortwaveRadiationBalance {

	@Description("The double value of the  temperature")
	@In
	@Unit ("°C")
	double temperature;

	@Description("The double value of the  humidity")
	@In
	@Unit ("%")
	double humidity;
	
	@Description("The double value of the  latitude of the point")
	@In
	double latitude;
	
	@Description("The double value of the  skyview factor")
	@In
	@Unit ("-")
	double skyviewFactor;
	
	@Description("The double value of elevation of the point")
	@In 
	@Unit ("m")
	double z;
	
	@Description("The first day of the simulation.")
	@In
	public String currentDate;

	@Description("Ozone layer thickness in cm")
	@In
	public double pCmO3;
	// pCmO3 = 0.4-0.6;

	@Description("Default relative humidity value")
	public double pRH = 0.7;

	@Description(" Visibility depending on aerosol attenuation (5 < vis < 180 Km) [km].")
	@In
	@Unit ("km")
	public double pVisibility;
	//pVisibility = 60-80;

	@Description("The soil albedo.")
	@In
	public double pAlphag;
	//pAlphag = 0.9;

	@Description("The solar constant")
	private static final double SOLARCTE = 1370.0;
	// double SOLARCTE = 1360.0;

	@Description("The atmospheric pressure")
	private static final double ATM = 1013.25;

	@Description("The declination of the sun, in the current the day")
	double delta;

	@Description("The relative air mass")
	double ma;

	@Description("The trasmittance function for the Reyleigh scattering")
	double tau_r;

	@Description("The trasmittance by ozone")
	double tau_o;

	@Description("The trasmiitance by gases")
	double tau_g;

	@Description("The trasmittance by aerosol")
	double tau_a;

	@Description("The trasmittance by water vapor")
	double tau_w;

	@Description("The direct normal irradiance")
	double In;

	@Description("The hour of the consdiered day")
	double hour;

	@Description("The sunrise in the considered day")
	double sunrise;

	@Description("The sunrise in the considered day")
	double sunset;

	@Description("The direct radiation on an arbitrary sloping surface in a point"
			+ "under cloudless condition, according to Corripio (2002)")
	@Out 
	@Unit ("W/m2")
	double directRadiation;
	
	@Description("The diffuse radiation")
	@Out 
	@Unit ("W/m2")
	double diffuseRadiation;
	
	@Description("The radiation at the top of the atmosphere")
	@Out
	@Unit ("W/m2")
	double topAtmposphere;

	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.UTC);


	@Execute
	public void process() throws Exception { 

		// Format the current date in a DateTime format
		DateTime date = formatter.parseDateTime(currentDate);

		// calculating the sun vector
		double sunVector[] = calcSunVector(latitude, getHourAngle(date,latitude));

		// E0 is the correction factor related to Earth’s orbit eccentricity computed according to Spencer (1971):
		double E0=computeE0(date);
		
		// compute the direct radiation
		directRadiation=(hour > (sunrise) && hour < (sunset))?
				calcDirectRadiation(sunVector, E0):0;
				
		// compute the diffuse radiation
		diffuseRadiation=(hour > (sunrise) && hour < (sunset))?
				calcDiffuseRadiation(sunVector, E0):0;
			
		// compute the raidationat the top of the atmosphere
		topAtmposphere=(hour > (sunrise) && hour < (sunset))?
				calcTopAtmosphere(E0, sunVector[2]):0;
						
}

/**
 * Compute the correction factor related to Earth’s orbit eccentricity.
 *
 * @param date is the current date
 * @return the double value of E0
 */


private double computeE0(DateTime date) {
	// k is the day angle in radiant 
	double k = 2 * Math.PI * (date.getDayOfMonth() - 1.0) / 365.0;
	return 1.00011 + 0.034221 * Math.cos(k) + 0.00128
			* Math.sin(k) + 0.000719 * Math.cos(2 * k) + 0.000077
			* Math.sin(2 * k);
}





/**
 * getHourAngle is the value of the hour angle at a given time and latitude (Corripio (2003))
 * 
 * @param date is the current date
 * @param latitude is the latitude of the station
 * @return the double value of the hour angle
 */
private double getHourAngle(DateTime date, double latitude) {
	int day = date.getDayOfYear();
	
	// check this part, if it is daily I put the 12:00 pm as hour
	hour=(double)date.getMillisOfDay() / (1000 * 60 * 60);

	// (360 / 365.25) * (day - 79.436) is the number of the day 
	double dayangb = Math.toRadians((360 / 365.25) * (day - 79.436));

	// Evaluate the declination of the sun.
	delta = Math.toRadians(.3723 + 23.2567 * Math.sin(dayangb) - .758
			* Math.cos(dayangb) + .1149 * Math.sin(2 * dayangb) + .3656
			* Math.cos(2 * dayangb) - .1712 * Math.sin(3 * dayangb) + .0201
			* Math.cos(3 * dayangb));

	// ss is the absolute value of the hour angle at sunrise or sunset
	double ss = Math.acos(-Math.tan(delta) * Math.tan(latitude));
	sunrise = 12 * (1.0 - ss / Math.PI);
	sunset = 12 * (1.0 + ss / Math.PI);

	if (hour > (sunrise) && hour < (sunset) & (hour - sunrise) < 0.01) hour = hour + 0.1;
	if (hour > (sunrise) && hour < (sunset) & (sunset - hour) < 0.01) hour = hour - 0.1;

	//the hour angle is zero at noon and has the following value in radians at any time t
	// given in hours and decimal fraction:
	double hourangle=(hour/ 12.0 - 1.0) * Math.PI;
	return hourangle;

}

/**
 * calcSunVector compute the vector vector in the direction of the Sun (Corripio (2003))
 *
 * @param latitude is the latitude of the station 
 * @param the hour angle  
 * @return the sun vector 
 */
protected double[] calcSunVector(double latitude, double hourAngle) {
	double sunVector[] = new double[3];
	sunVector[0] = -Math.sin(hourAngle) * Math.cos(delta);
	sunVector[1] = Math.sin(latitude) * Math.cos(hourAngle) * Math.cos(delta)
			- Math.cos(latitude) * Math.sin(delta);
	sunVector[2] = Math.cos(latitude) * Math.cos(hourAngle) * Math.cos(delta)
			+ Math.sin(latitude) * Math.sin(delta);
	return sunVector;

}



/**
 * calcDirectRadiation calculates the direct radiation according to Corripio (2002)
 *
 * @param shadowIndex is the shadow index [0,1] that accounts for the sun or shadow in the point 
 * @param sunVector is the sun vector
 * @param normalVector is  the vector normal to a pixel
 * @param E0 is the correction of the eccentricity
 * @param temperture is the air temperature
 * @param himidity is the relative humidity
 * @return the double value of the direct radiation
 */

private double calcDirectRadiation( double[] sunVector,double E0) throws IOException {

	// zenith angle
	double zenith = Math.acos(sunVector[2]);

	//mr [–] relative optical air mass:
	double mr = 1.0 / (sunVector[2] + 0.15 * Math.pow( (93.885 - (zenith * (180 / (2*Math.PI)))), (-1.253)));


	// local atmospheric pressure
	double pressure = ATM * Math.exp(-0.0001184 * z);

	// relative air mass
	ma = mr * pressure / ATM;

	// The transmittance functions for Rayleigh scattering
	tau_r = Math.exp((-.09030 * Math.pow(ma, 0.84))
			* (1.0 + ma - Math.pow(ma, 1.01)));

	//transform the temperature in Kelvin
	temperature  = temperature + 273.0;

	// evaluate the saturated valor pressure
	double saturatedVaporPressure = Math.exp(26.23 - 5416.0 / temperature );

	// the precipitable water in cm calculated according to Prata (1996)
	double w = 0.493 * (humidity / 100) * saturatedVaporPressure / temperature ;

	// Transmittance by water vapour
	tau_w = 1.0 - 2.4959 * w * mr / (Math.pow(1.0 + 79.034 * w * mr, 0.6828) + 6.385 * w * mr);

	// The transmittance by ozone 
	tau_o = 1.0 - ((0.1611 * pCmO3 * mr * Math.pow(1.0 + 139.48 * pCmO3 * mr,-0.3035)) 
			- (0.002715 * pCmO3 * mr / (1.0 + 0.044 * pCmO3 * mr + 0.0003 * Math.pow(pCmO3 * mr, 2))));

	// Transmittance by uniformly mixed gases
	tau_g = Math.exp(-0.0127 * Math.pow(ma, 0.26));


	// The transmittance by aerosols
	tau_a = Math.pow((0.97 - 1.265 * Math.pow(pVisibility,(-0.66))), Math.pow(ma, 0.9));

	// correction factor [m] for increased trasmittance with elevation z[m] according to Corripio (2002)
	double beta_s = (z <= 3000)?2.2 * Math.pow(10, -5) * z:2.2 * Math.pow(10, -5) * 3000;


	// Direct radiation under cloudless sky incident on arbitrary tilted
	// surfaces 
	In=0.9571*SOLARCTE*E0*(tau_r * tau_o * tau_g * tau_w * tau_a + beta_s);

	double S_incident=In* sunVector[2] * skyviewFactor;
	
	S_incident=(S_incident>3000)?Double.NaN:S_incident;
	S_incident=(S_incident<=0)?Double.NaN:S_incident;

	return S_incident;
}


/**
 * calcDiffuseRadiation calculates the diffuse radiation according to Corripio (2002)
 *
 * @param sunVector is the sun vector
 * @param E0 is the correction of the eccentricity
 * @return the double value of the diffuse radiation
 */
private double calcDiffuseRadiation(double [] sunVector,double E0){

	//single-scattering albedo fraction of incident energy scattered to total attenuation by aerosol
	double omega0 = 0.9;

	//trasmittance of direct radiation due to aerosol absorbance 
	double tau_aa = 1.0 - (1.0 - omega0)* (1.0 - ma + Math.pow(ma, 1.06)) * (1 - tau_a);
	// ////////////////////////////////////////////////////

	//  Rayleigh scattered diffunce irradiance
	double I_dr = 0.79 * E0*SOLARCTE* sunVector[2] * (1.0 - tau_r)* (tau_o * tau_g * tau_w * tau_aa) * 0.5/ (1.0 - ma + Math.pow(ma, 1.02));

	// The aerosol-scattered diffuse irradiance 
	double FC = 0.74;
	double I_da = 0.79 * E0*SOLARCTE* sunVector[2] * (tau_o * tau_g * tau_w * tau_aa) * FC
			* (1.0 - (tau_a / tau_aa)) / ((1 - ma + Math.pow(ma, 1.02)));

	// The atmospheric albedo is computed as
	double alpha_a = 0.0685 + (1.0 - FC) * (1.0 - (tau_a / tau_aa));

	// the diffuse irradiance from multiple reflection between the earth and the atmosphere
	double I_dm = (In*sunVector[2]+ I_dr + I_da) * alpha_a * pAlphag / (1.0 - pAlphag * alpha_a);

	double diffuse = (I_dr + I_da + I_dm)* skyviewFactor;
	
	diffuse= (diffuse>3000)?Double.NaN:diffuse;
	diffuse=(diffuse<0)?Double.NaN:diffuse;
	
	return diffuse;

}

/**
 * calcTopAtmosphere calculates the radiation at the top of the atmosphere according to Corripio (2002)
 *
 * @param sunVector is the sun vector
 * @param E0 is the correction of the eccentricity
 * @return the double value of the top atmosphere radiation
 */
private double calcTopAtmosphere(double E0, double sunVector){
	double topATM=E0 * SOLARCTE * Math.cos(Math.acos(sunVector));
	return topATM=(topATM<0)?0:topATM;	
}


}