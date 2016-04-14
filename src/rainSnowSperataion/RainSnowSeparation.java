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
package rainSnowSperataion;


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



@Description("The component separates the precipitation into rainfall and snowfall,"
		+ "according to Kavetski et al. (2006)")
@Author(name = "Marialaura Bancheri and Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Rain-snow separation")
@Label("")
@Name("Rain-snow separation point case")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class RainSnowSeparation {


	@Description("The double value of the precipitation")
	@In
	@Unit("mm")
	double precipitation;

	@Description("Alfa_r is the adjustment parameter for the rainfall measurements errors")
	@In
	public double alfa_r;

	@Description("Alfa_s is the adjustment parameter for the snow measurements errors")
	@In
	public double alfa_s;

	@Description("m1 is the parameter controling the degree of smoothing")
	@In
	public double m1 = 1.0;

	@Description("The double value of the  temperature")
	@In
	@Unit("°C")
	double temperature;

	@Description("The melting temperature")
	@In
	@Unit("°C")
	public double meltingTemperature;

	@Description("The souble value of the output rainfall") 
	@Out
	@Unit("mm")
	double rainfall;
	
	@Description("The souble value of the output snowall") 
	@Out
	@Unit("mm")
	double snowfall;
	


	@Execute
	public void process() throws Exception { 

			// compute the rainfall and the snowfall according to Kavetski et al. (2006)
			rainfall=alfa_r*((precipitation/ Math.PI)* Math.atan((temperature - meltingTemperature) / m1)+precipitation/2);
			snowfall=alfa_s*(precipitation-rainfall);
			snowfall=(snowfall<0)?0:snowfall;
			
		}
	}

	
