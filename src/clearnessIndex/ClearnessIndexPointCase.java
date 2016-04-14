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
package clearnessIndex;


import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;



@Description("The component computes the Clearness index, which is the ratio between the incoming shortwave"
		+ "and the shortwave at the top of the atmosphere")
@Author(name = "Marialaura Bancheri and Giuseppe Formetta", contact = "maryban@hotmail.it")
@Keywords("Hydrology, clearness index")
@Name("ClearnessIndexPointCase")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class ClearnessIndexPointCase {

	@Description("The double value of the SWRB meadured")
	@In
	@Unit("W/m2")
	double SWRBMeasured;


	@Description("The double value of the SWRB at the top of the atmosphere")
	@In
	@Unit("W/m2")
	double SWRBTopATM;


	@Description("Clearness index input value") 
	@Out
	@Unit("[0,1]")
	public double clearnessIndex;


	@Execute
	public void process() throws Exception { 

		// compute the clearness index
		clearnessIndex=(SWRBTopATM==0)?Double.NaN:SWRBMeasured/SWRBTopATM;
		

	}

}
