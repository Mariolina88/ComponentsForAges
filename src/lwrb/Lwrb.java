/*
 * GNU GPL v3 License
 *
 * Copyright 2015 Marialaura Bancheri
 *
 * This program is free software: you can redistribute it and/or modify
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
package lwrb;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;



@Description("The component computes the longwave solar radiation, both upwelling and downwelling.")
@Documentation("")
@Author(name = "Marialaura Bancheri and Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Radiation, Downwelling , upwelling")
@Name("lwrb")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class Lwrb {


	@Description("Air temperature input value")
	@Unit("°C")
	public double airTemperature;

	@Description("Soil temperature input value") 
	@Unit("°C")
	public double soilTemperature;

	@Description("Humidity input value") 
	@Unit("%")
	public double relative_humidity;
	
	@Description("skyview factor input value") 
	@Unit("-")
	public double skyview;

	@Description("Reference humidity")
	private static final double pRH = 0.7;

	@Description("Clearness index input value") 
	@Unit("[0,1]")
	public double clearnessIndex;

	@Description("X parameter of the literature formulation")
	@In
	public double X; 

	@Description("Y parameter of the literature formulation")
	@In
	public double Y ;

	@Description("Z parameter of the literature formulation")
	@In
	public double Z;


	@Description("Soil emissivity")
	@Unit("-")
	@In
	public double epsilonS;	

	@Description("String containing the number of the model: "
			+ "1: Brunt's [1932];"
			+ " 2: Idso and Jackson [1969];"
			+ " 3: Idso [1981];"
			+ " 4: Monteith and Unsworth [1990];"
			+ " 5: Dilley and O'Brien [1998];"
			+ " 6: To be implemented")
	@In
	public String model;

	@Description("Coefficient to take into account the cloud cover,"
			+ "set equal to 0 for clear sky conditions ")
	@In
	public double A_Cloud;

	@Description("Exponent  to take into account the cloud cover,"
			+ "set equal to 1 for clear sky conditions")
	@In
	public double B_Cloud;

	@Description("Stefan-Boltzaman costant")
	private static final double ConstBoltz = 5.670373 * Math.pow(10, -8);

	Model modelCS;
	
	@Description("The downwelling radiation computed with all-sky conditions")
	@Out
	@Unit("W/m2")
	double downwellingALLSKY;

	@Description("The upwelling radiation")
	@Out
	@Unit("W/m2")
	double upwelling;


	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception { 
			
		/**Input data reading*/
		if (Double.isNaN(relative_humidity)) relative_humidity= pRH;
		if (Double.isNaN(clearnessIndex )) clearnessIndex = 1;
		
		/**Computation of the downwelling, upwelling and longwave:
		 * if there is no value in the input data, there will be no value also in
		 * the output*/
		upwelling=(Double.isNaN(soilTemperature))? Double.NaN:computeUpwelling(soilTemperature);
		
		downwellingALLSKY=(Double.isNaN(airTemperature))? Double.NaN:
			computeDownwelling(model,airTemperature,relative_humidity/100, clearnessIndex, upwelling, skyview);


	}
	
	/**
	 * Compute the upwelling longwave radiation .
	 *
	 * @param soilTemperature: the soil temperature input
	 * @return the double value of the upwelling
	 */
	private double computeUpwelling( double soilTemperature){

		/**compute the upwelling*/
		return epsilonS * ConstBoltz * Math.pow(soilTemperature+ 273.15, 4);
	}



	/**
	 * Compute downwelling longwave radiation.
	 *
	 * @param model: the string containing the number of the model
	 * @param airTemperature:  the air temperature input
	 * @param humidity: the humidity input
	 * @param clearnessIndex: the clearness index input
	 * @return the double value of the all sky downwelling
	 */
	private double computeDownwelling(String model,double airTemperature, 
			double humidity, double clearnessIndex, double skyviewvalue, double upwelling){

		/**e is the screen-level water-vapor pressure*/
		double e = humidity *6.11 * Math.pow(10, (7.5 * airTemperature) / (237.3 + airTemperature)) / 10;

		/**compute the clear sky emissivity*/
		modelCS=SimpleModelFactory.createModel(model,X,Y,Z,airTemperature+ 273.15,e);
		double epsilonCS=modelCS.epsilonCSValues();

		/**compute the downwelling in clear sky conditions*/
		double downwellingCS=epsilonCS* ConstBoltz* Math.pow(airTemperature+ 273.15, 4);
		
		/**correct downwelling clear sky for sloping terrain*/
		downwellingCS=downwellingCS*skyviewvalue+upwelling*(1-skyviewvalue);

		/**compute the cloudness index*/
		double cloudnessIndex = 1 + A_Cloud* Math.pow(clearnessIndex, B_Cloud);

		/**compute the downwelling in all-sky conditions*/
		return downwellingCS * cloudnessIndex;

	}




}