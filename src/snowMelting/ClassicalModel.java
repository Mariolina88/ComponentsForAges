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
package snowMelting;


// TODO: Auto-generated Javadoc
/**
 * The Class ClassicalModel.
 */
public class ClassicalModel implements SnowModel{

	/** The combined melting factor. */
	double combinedMeltingFactor;
	
	/** The temperature. */
	double temperature; 
	
	/** The melting temperature. */
	double meltingTemperature;


	/**
	 * Instantiates a new classical model.
	 *
	 * @param combinedMeltingFactor is the combined melting factor
	 * @param temperature is the temperature
	 * @param meltingTemperature is the melting temperature
	 */
	public ClassicalModel(double combinedMeltingFactor, double temperature, double meltingTemperature){
		
		this.combinedMeltingFactor=combinedMeltingFactor;
		this.temperature=temperature;
		this.meltingTemperature=meltingTemperature;
		
	}



	/* (non-Javadoc)
	 * @see snowMelting.SnowModel#snowValues()
	 */
	public double snowValues() {
		return combinedMeltingFactor * (temperature - meltingTemperature);
	}







}
