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

import org.jgrasstools.gears.libs.modules.JGTConstants;

import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;


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
import oms3.annotations.Status;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.libs.modules.JGTModel;

import org.jgrasstools.gears.utils.coverage.CoverageUtilities;


@Description("Calculate the raster of the normal vectors to the surface for each pixel of the DEM")
@Documentation("")
@Author(name = "Marialaura Bancheri, Giuseppe Formetta, Daniele Andreis and Riccardo Rigon", contact = "maryban@hotmail.it")
@Keywords("Hydrology, Radiation, SkyviewFactor, Hillshade")
@Bibliography("Corripio (2003)")
@Label(JGTConstants.HYDROGEOMORPHOLOGY)
@Name("normalVector")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class NormalVector extends JGTModel {


	@Description("The map of the elevation.")
	@In
	public GridCoverage2D inDem;
	WritableRaster demWR;
	
	@Description("The map of the elevation.")
	@In
	WritableRaster normalWR;



	@Execute
	public void process() throws Exception { 

		// transform the GrifCoverage2D maps into writable rasters
		demWR=mapsTransform(inDem);

		double dx = CoverageUtilities.getRegionParamsFromGridCoverage(inDem).get(CoverageUtilities.XRES);

		// compute the vector normal to a grid cell surface.
		normalWR = normalVector(demWR, dx);

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
	 * normalVector compute the vector normal to a grid cell surface, according to Corripio (2003)
	 *
	 * @param demWR is the Writable raster of DEM 
	 * @param res is the resolution of the DEM
	 * @return the normal vector for each cell
	 */
	protected WritableRaster normalVector(WritableRaster demWR, double res) {

		int minX = demWR.getMinX();
		int minY = demWR.getMinY();
		int rows = demWR.getHeight();
		int cols = demWR.getWidth();

		RandomIter pitIter = RandomIterFactory.create(demWR, null);
		/*
		 * Initialize the image of the normal vector in the central point of the
		 * cells, which have 3 components (X;Y;Z), so the Image have 3 bands..
		 */
		SampleModel sm = RasterFactory.createBandedSampleModel(5, cols, rows, 3);
		WritableRaster tmpNormalVectorWR = CoverageUtilities .createDoubleWritableRaster(cols, rows, null, sm, 0.0);
		WritableRandomIter tmpNormalIter = RandomIterFactory.createWritable( tmpNormalVectorWR, null);
		/*
		 * apply the corripio's formula 
		 */
		for (int j = minY; j < minX + rows - 1; j++) {
			for (int i = minX; i < minX + cols - 1; i++) {
				double zij = pitIter.getSampleDouble(i, j, 0);
				double zidxj = pitIter.getSampleDouble(i + 1, j, 0);
				double zijdy = pitIter.getSampleDouble(i, j + 1, 0);
				double zidxjdy = pitIter.getSampleDouble(i + 1, j + 1, 0);
				double firstComponent = res * (zij - zidxj + zijdy - zidxjdy);
				double secondComponent = res * (zij + zidxj - zijdy - zidxjdy);
				double thirthComponent = 2 * (res * res);
				double den = Math.sqrt(firstComponent * firstComponent
						+ secondComponent * secondComponent + thirthComponent
						* thirthComponent);
				tmpNormalIter.setPixel(i, j, new double[] {
						firstComponent / den, secondComponent / den,
						thirthComponent / den });

			}
		}
		pitIter.done();

		return tmpNormalVectorWR;

	}

}