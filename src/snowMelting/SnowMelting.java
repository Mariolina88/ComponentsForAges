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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package snowMelting;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;
import snowMelting.SimpleModelFactory;


@Description("The component computes the snow water equivalent and the melting discharge. "
		+ "The inputs of the components are: the rainfall, the snowfall,"
		+ "the shortwave radiation, the temperature, the skyview factor, the elevation of the point, all the"
		+ "parameters of the models chosen and the intial cnditions")
@Author(name = "Marialaura Bancheri & Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Snow Model")
@Label("")
@Name("Snow")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class SnowMelting {

	@Description("The double value of the rainfall")
	@In
	@Unit("mm")
	double rainfall;

	@Description("The double value of the snowfall")
	@In
	@Unit("mm")
	double snowfall;

	@Description("The double value of the  shortwave radiation")
	@In
	@Unit("W/m2")
	double shortwaveRadiation;

	@Description("The double value of the  temperature, once read from the HashMap")
	@In
	@Unit("°C")
	double temperature;


	@Description("the skyview factor value, read from the map")
	double skyview;
	
	@Description("The double value of the  EI, once read from the map")
	double EI;

	@Description("It is possibile to chose between 3 different models to compute the melting: "
			+ " Classical; Cazorzi; Hoock")
	@In
	public String model;

	@Description("The melting temperature")
	@In
	@Unit("°C")
	public double meltingTemperature;

	@Description("Combined melting factor")
	@In
	public double combinedMeltingFactor;

	@Description("Radiation factor")
	@In
	public double radiationFactor;

	@Description("Freezing factor")
	@In
	public double freezingFactor;

	@Description("Alfa_l is the coefficient for the computation of the maximum liquid water")
	@In
	public double alfa_l;
	
	@Description("Initial condition for solid water")
	@In
	public double initialConditionSolid;
	
	@Description("Initial condition for liquid water")
	@In
	public double initialConditionLiquid;

	SnowModel snowModel;


	@Description("liquid water value obtained from the soultion of the budget")
	double liquidWater;


	@Description("Integration interval")
	double dt=1;

	@Description(" The output SWE value")
	@Out
	@Unit("mm")
	double SWE;

	@Description(" The output mlting discharge value")
	@Unit("mm")
	double meltingDischarge;


	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception { 

		double freezing=(temperature<meltingTemperature)?computeFreezing():0;
		double melting=(temperature>meltingTemperature)?computeMelting():0;
		double solidWater=computeSolidWater(initialConditionSolid,freezing, melting);
		computeLiquidWater(initialConditionLiquid, freezing, melting);
		
		meltingDischarge=computeMeltingDischarge(solidWater);
		SWE=computeSWE(solidWater);

		initialConditionSolid=solidWater;
		initialConditionLiquid=liquidWater;
	}


	/**
	 * Compute the freezing rate.
	 *
	 * @param freezingFactor is the freezing factor
	 * @param temperature is the actual temperature
	 * @param meltingTemperature is the melting temperature
	 * @return the double value of the freezing rate
	 */
	private double computeFreezing(){
		// compute the freezing
		return freezingFactor*(meltingTemperature-temperature);		
	}
	
	
	/**
	 * Compute the melting rate according to the model used.
	 *
	 * @param model is the string containing the name of the model chosen 
	 * @param combinedMeltingFactor is the combined melting factor
	 * @param temperature is the input temperature
	 * @param meltingTemperature is the melting temperature
	 * @param skyview is the the skyview factor value
	 * @param radiationFactor is the radiation factor
	 * @param shortwaveRadiation is the shortwave radiation
	 * @return the double value of the melting rate
	 */
	
	private double computeMelting(){
		// compute the snowmelt rate
		snowModel=SimpleModelFactory.createModel(model, combinedMeltingFactor, temperature, meltingTemperature, 
				skyview, radiationFactor, shortwaveRadiation,EI);
		
		return Math.min(snowModel.snowValues(), SWE);				
	}
	
	
	/**
	 * Compute the Solid Water solving the differential equation of the mass balance.
	 * @param initialConditionSolidWater is the value of the liquid water at the previous time step
	 * @param freezing is the freezing rate
	 * @param melting is the melting rate
	 * @return the double value of Solid Water
	 */
	private double computeSolidWater(double initialConditionSolidWater, double freezing, double melting){
		// solve the differential equation for the solid water
		double solidWater=initialConditionSolidWater+ dt * (snowfall + freezing - melting);  
		if (solidWater<0){ 
			solidWater=0; 
			melting=0;
		}	
		return solidWater;	
	}
	
	/**
	 * Compute the Liquid Water solving the differential equation of the mass balance.
	 * @param initialConditionLiquidWater is the value of the liquid water at the previous time step
	 * @param freezing is the freezing rate
	 * @param melting is the melting rate
	 * @return the double value of Liquid Water
	 */
	void computeLiquidWater(double initialConditionLiquidWater, double freezing, double melting){
		// solve the differential equation for the liquid water
		liquidWater=initialConditionLiquidWater+ dt * (rainfall - freezing + melting); 
		if (liquidWater<0) liquidWater=0;	
	}
	
	/**
	 * Compute the melting discharge: the liquid water that exceeds
     * the maximum amount of liquid water in the snow pack becomes
     * snowmelt discharge.
	 * 
	 * @param solidWater is the solid water 
	 * @return the double value of the melting discharge
	 */
	private double computeMeltingDischarge(double solidWater){
		// compute the maximum value of the liquid water
		double maxLiquidWater = alfa_l * solidWater;
		
		// compute the melting discharge
		double melting_discharge=0;
		if (liquidWater > maxLiquidWater) {
			melting_discharge = liquidWater - maxLiquidWater;		
		}
	
		liquidWater=Math.min(maxLiquidWater, liquidWater);
		
		return melting_discharge;
	}


	/**
	 * Compute the snow water equivalent.
	 * @param solidWater is the solid water 
	 * @return the double value of the snow water equivalent
	 */
	private double computeSWE(double solidWater){
		SWE=solidWater+liquidWater;
		return SWE;

	}


}