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

import static org.jgrasstools.gears.libs.modules.ModelsEngine.calcInverseSunVector;
import static org.jgrasstools.gears.libs.modules.ModelsEngine.calcNormalSunVector;
import static org.jgrasstools.gears.libs.modules.ModelsEngine.calculateFactor;
import static org.jgrasstools.gears.libs.modules.ModelsEngine.scalarProduct;

import org.jgrasstools.gears.libs.modules.JGTConstants;

import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.jgrasstools.gears.libs.modules.JGTConstants.doubleNovalue;
import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import javax.media.jai.RasterFactory;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrasstools.gears.libs.modules.JGTModel;

import org.jgrasstools.gears.utils.CrsUtilities;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

@Description("Calculate the shadowMap with the shadow index")
@Documentation("")
@Author(name = "Marialaura Bancheri, Giuseppe Formetta, Daniele Andreis and Riccardo Rigon", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Radiation, SkyviewFactor, Hillshade")
@Bibliography("")
@Label(JGTConstants.HYDROGEOMORPHOLOGY)
@Name("shortradbal")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class ShadowMap extends JGTModel {

	@Description("The map of the digital elevation model.")
	@In
	public GridCoverage2D inDem;
	WritableRaster demWR;

	@Description("The cuurent date")
	@In
	DateTime date;
	
	@Description("The latitude of the point")
	@In
	double latitude;
	
	@Description("the hour of the consdiered day")
	double hour;

	@Description("the sunrise in the considered day")
	double sunrise;

	@Description("the sunrise in the considered day")
	double sunset;
	
	@Description("The declination of the sun, diven the day")
	double delta;
	
	@Description("The shadow map with the shadow index")
	@Out
	WritableRaster shadowWR;


	@Execute
	public void process() throws Exception { 

		// transform the GrifCoverage2D maps into writable rasters
		demWR=mapsTransform(inDem);

		//get the dimension of the DEM and the resolution
		int height=demWR.getHeight();
		int width=demWR.getWidth();
		double dx = CoverageUtilities.getRegionParamsFromGridCoverage(inDem).get(CoverageUtilities.XRES);


		// calculating the sun vector
		double sunVector[] = calcSunVector(latitude, getHourAngle(date,latitude));

		// calculate the inverse of the sun vector
		double[] inverseSunVector = calcInverseSunVector(sunVector);

		// calculate the normal of the sun vector according to Corripio (2003)
		double[] normalSunVector = calcNormalSunVector(sunVector);

		//evaluate the shadow map
		shadowWR = calculateFactor(height, width, sunVector, inverseSunVector, normalSunVector, demWR, dx);
		

						
}

	/**
	 * Maps reader transform the GrifCoverage2D in to the writable raster,
	 * replace the -9999.0 value with no value.
	 *
	 * @param inValues: the input map values
	 * @return the writable raster of the given map
	 */
	private WritableRaster mapsTransform ( GridCoverage2D inValues){	
		RenderedImage inValuesRenderedImage = inValues.getRenderedImage();
		WritableRaster inValuesWR = CoverageUtilities.replaceNovalue(inValuesRenderedImage, -9999.0);
		inValuesRenderedImage = null;
		return inValuesWR;
	}


/**
 * getHourAngle is the value of the hour angle at a given time and latitude (Corripio (2003))
 * 
 *
 * @param date is the current date
 * @param latitude is the latitude of the station
 * @return the double value of the hour angle
 */
private double getHourAngle(DateTime date, double latitude) {
	int day = date.getDayOfYear();	
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


}