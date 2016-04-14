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
 * The Class CazorziModel.
 */
public class CazorziModel implements SnowModel{

	/** The combined melting factor. */
	double combinedMeltingFactor;
	
	/** The temperature. */
	double temperature; 
	
	/** The melting temperature. */
	double meltingTemperature;
	
	/** The energy index. */
	double EI;
	
	/** The skyview. */
	double skyview;


	/**
	 * Instantiates a new cazorzi model.
	 *
	 * @param combinedMeltingFactor is the combined melting factor
	 * @param temperature is the temperature
	 * @param meltingTemperature is the melting temperature
	 * @param EI is the energy index
	 * @param skyview is the skyview
	 */
	public CazorziModel(double combinedMeltingFactor, double temperature, double meltingTemperature,
			double EI, double skyview){
		
		this.combinedMeltingFactor=combinedMeltingFactor;
		this.temperature=temperature;
		this.meltingTemperature=meltingTemperature;
		this.EI=EI;
		this.skyview=skyview;
		
	}



	/* (non-Javadoc)
	 * @see snowMeltingPointCase.SnowModel#snowValues()
	 */
	@Override
	public double snowValues() {
		return combinedMeltingFactor * (temperature - meltingTemperature)*EI*skyview;
	}







}
