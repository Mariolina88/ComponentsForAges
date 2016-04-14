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
 * The Class HoockModel.
 */
public class HoockModel implements SnowModel{

	/** The combined melting factor. */
	double combinedMeltingFactor;
	
	/** The radiation factor. */
	double radiationFactor;
	
	/** The temperature. */
	double temperature; 
	
	/** The melting temperature. */
	double meltingTemperature;
	
	/** The shortwave. */
	double shortwave;
	
	/** The skyview. */
	double skyview;


	/**
	 * Instantiates a new hoock model.
	 *
	 * @param combinedMeltingFactor is the combined melting factor
	 * @param radiationFactor is the radiation factor
	 * @param temperature is the temperature
	 * @param meltingTemperature is the melting temperature
	 * @param shortwave is the shortwave
	 * @param skyview is the skyview
	 */
	public HoockModel(double combinedMeltingFactor, double radiationFactor, double temperature, double meltingTemperature,
			double shortwave, double skyview){
		
		this.combinedMeltingFactor=combinedMeltingFactor;
		this.temperature=temperature;
		this.meltingTemperature=meltingTemperature;
		this.radiationFactor=radiationFactor;
		this.shortwave=shortwave;
		this.skyview=skyview;
		
	}


	/* (non-Javadoc)
	 * @see snowMelting.SnowModel#snowValues()
	 */
	public double snowValues() {
		return (combinedMeltingFactor+radiationFactor*shortwave)*(temperature-meltingTemperature)*skyview;
	}







}
