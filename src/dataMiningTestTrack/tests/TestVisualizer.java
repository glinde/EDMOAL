/**
Copyright (c) 2011, The EDMOAL Project

	DLR Deutsches Zentrum fuer Luft- und Raumfahrt e.V.
	German Aerospace Center e.V.
	Institut fuer Flugfuehrung/Institute of Flight Guidance
	Tel. +49 531 295 2500, Fax: +49 531 295 2550
	WWW: http://www.dlr.de/fl/		
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * Neither the name of the DLR nor the names of its contributors
    	may be used to endorse or promote products derived from this software
    	without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.
*/


package dataMiningTestTrack.tests;

import gui.ColorList;
import gui.DrawableObject;
import gui.Scheme;
import gui.Screen;
import gui.ScreenViewer;
import gui.DataMiningGraphics.GCentroid;
import gui.DataMiningGraphics.GCentroidClusteringAlgorithm;
import gui.DataMiningGraphics.GClusteredDataSet;
import gui.DataMiningGraphics.GDataSet;
import gui.generalGraphics.GImage;
import gui.projections.Orthogonal2DProjection;
import io.BatikExport;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;

import data.objects.matrix.FeatureSpaceClusterSampling2D;
import data.objects.matrix.FeatureSpaceSampling2D;
import data.set.IndexedDataObject;
import datamining.DataMiningAlgorithm;
import datamining.clustering.ClusteringAlgorithm;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;
import datamining.clustering.protoype.PrototypeClusteringAlgorithm;
import datamining.gradient.GradientOptimization;
import datamining.gradient.centroid.SingleCentroidGradientOptimizationAlgorithm;
import datamining.resultProviders.CrispClusteringProvider;
import datamining.resultProviders.FuzzyClusteringProvider;
import datamining.resultProviders.PrototypeProvider;
import datamining.resultProviders.ResultProvider;

/**
 * A class to provide basic functions for visualisation and defines some layout constants.
 * It takes care of transforming clustering algorithm objects or data set objects into
 * their graphical representation.
 *
 * @author Roland Winkler
 */
public abstract class TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 121707857795848952L;
	
	
	/**
	 * The font for text in the figure.
	 */
	public static final Font FIGURE_TEXT_FONT = new Font("Dialog", Font.PLAIN, 50);
	
	/**
	 * The font for naming the axis.
	 */
	public static final Font AXIS_TEXT_FONT = new Font("Dialog", Font.PLAIN, 50);
	
	/**
	 * The font for the axis ticks, e.g. the numbers next to the axis.
	 */
	public static final Font TICK_TEXT_FONT = new Font("Dialog", Font.PLAIN, 40);
	
	/**
	 * The font for the legend.
	 */
	public static final Font LEGEND_TEXT_FONT = new Font("Dialog", Font.PLAIN, 45);
	
	/**
	 * The font for the figure title.
	 */
	public static final Font TITLE_TEXT_FONT = new Font("Dialog", Font.BOLD, 60);
	
	/**
	 * The stroke object for the axis lines and ticks.
	 */
	public static final Stroke LINE_STROKE = new BasicStroke(5.0f);
		
	/**
	 * A list of predefined colours. 
	 */
	public Color[] seriesColorList;
	
	public Color[] overlayColors;

	/**
	 * States if the figure is saved in the SVG format, if the figure is printed to a file.
	 */
	public boolean printSVG;

	/**
	 * States if the figure is saved in the JPG format, if the figure is printed to a file.
	 */
	public boolean printJPG;

	/**
	 * States if the figure is saved in the PDF format, if the figure is printed to a file.
	 */
	public boolean printPDF;

	/**
	 * States if the figure is saved in the PNG format, if the figure is printed to a file.
	 */
	public boolean printPNG;
	
	/**
	 * The visual size of a data object in pixels.
	 */
	public float dataObjectSize;
	
	public float lineThickness;
	
	
	/**  */
	public int xIndex;
	
	/**  */
	public int yIndex;
	
	/**  */
	public int xRes;
	
	/**  */
	public int yRes;
	
	public boolean drawCrispMembershipLevels;
	
	public boolean drawOverlays;
	
	public boolean drawMembershipHeightLines;
	
	public double[] membershipLevels;
	
	public int membershipLevelLineWidth;
	
		
	/**
	 * The standard constructor.
	 */
	public TestVisualizer()
	{
		this.printSVG = false;
		this.printJPG = false;
		this.printPDF = false;
		this.printPNG = false;
		
		this.drawCrispMembershipLevels = false;
		this.drawOverlays = true;
		this.drawMembershipHeightLines = false;
		this.membershipLevels = new double[0];
		
		this.dataObjectSize = 5.0f;
		this.lineThickness = 1.0f;
		this.membershipLevelLineWidth = 2;
		this.seriesColorList = new Color[]{
				ColorList.RED,				ColorList.GREEN,			ColorList.BLUE,
				ColorList.ORANGE,			ColorList.MAGENTA,			ColorList.CYAN,
				ColorList.BRIGHT_RED,		ColorList.BRIGHT_GREEN,		ColorList.BRIGHT_BLUE,
				ColorList.BRIGHT_ORANGE,	ColorList.BRIGHT_MAGENTA,	ColorList.BRIGHT_CYAN,
				ColorList.DARK_RED,			ColorList.DARK_GREEN,		ColorList.DARK_BLUE,
				ColorList.DARK_ORANGE,		ColorList.DARK_MAGENTA,		ColorList.DARK_CYAN
			};
		
		this.overlayColors = new Color[]{Color.GREEN, Color.RED};
		
		this.xIndex = 0;
		this.yIndex = 1;
		
		this.xRes = 1050;
		this.yRes = 1050;
	}
	
	/**
	 * Creates a visualisation of the specified data
	 * 
	 * @param dataSet the data set to be visualized. May not be null!
	 * @param prototypeProvider the prototypes to visualize. May be null.
	 * @param resProv the clustering result for coloring the data objects. May be null.
	 * @param matrix the data overlay that is printed in the background. May be null.
	 * @param dataObjectSubsectionIndexes if only a subset of the data objects should be printed, set this list. May be null.
	 * @param title The title of the window. May be null.
	 * @param filename The filename for storing the image to the hard disk. May be null. 
	 */
	public void showDataSet(Collection<IndexedDataObject<double[]>> dataSet, PrototypeProvider<double[], ? extends Prototype<double[]>> prototypeProvider, ResultProvider<double[]> resProv, Collection<? extends FeatureSpaceSampling2D> matrices, int[] dataObjectSubsectionIndexes, String title, String filename)
	{
		ScreenViewer sv  = new ScreenViewer(this.xRes, this.yRes);
		
		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);

		if(matrices != null)
		{
			if(this.drawMembershipHeightLines)
			{
				GImage image = new GImage(null);
				
				for(FeatureSpaceSampling2D matrix:matrices)
				{
					if(matrix instanceof FeatureSpaceClusterSampling2D)
					{
						Color rgbColor  = Color.BLACK;
						Color rgbaColor = new Color(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue(), 220);
						
						image.setLowerLeftCorner(matrix.getLowerLeftCorner());
						image.setUpperRightCorner(matrix.getUpperRightCorner());
						
						if(((FeatureSpaceClusterSampling2D) matrix).getClusterID()>=0)
						{
							rgbColor  = ColorList.clusterColors[((FeatureSpaceClusterSampling2D) matrix).getClusterID()%ColorList.clusterColors.length];
							rgbaColor = new Color(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue(), 200);
						}
						image.fillAboveHeight(matrix, this.membershipLevels[0], rgbaColor);
						for(int k=0; k<this.membershipLevels.length; k++)
						{
							image.addHeightLines(matrix, this.membershipLevels[k], this.membershipLevelLineWidth, rgbColor);
						}
					}
				}
				image.gaussFilterImage(2);
				sv.screen.addDrawableObject(image);
			}
			
			if(this.drawOverlays) for(FeatureSpaceSampling2D matrix:matrices)
			{
				GImage image = new GImage(null);
				image.setImageData(matrix, this.overlayColors[0].getRGB(), this.overlayColors[1].getRGB());
				image.setLowerLeftCorner(matrix.getLowerLeftCorner());
				image.setUpperRightCorner(matrix.getUpperRightCorner());
				sv.screen.addDrawableObject(image);
			}
		}
		
		if(dataSet != null)
		{
			DrawableObject gDataSet = null;		
			if(resProv != null)
			{
				int clusterCount = 0;
				if(resProv instanceof FuzzyClusteringProvider) clusterCount = ((FuzzyClusteringProvider<double[]>)resProv).getClusterCount();
				else if (resProv instanceof CrispClusteringProvider) clusterCount = ((CrispClusteringProvider<double[]>)resProv).getClusterCount();
				
				GClusteredDataSet gClusteredDataSet;
				if(prototypeProvider == null)gClusteredDataSet = new GClusteredDataSet(resProv, dataObjectSubsectionIndexes, clusterCount);
				else gClusteredDataSet = new GCentroidClusteringAlgorithm(prototypeProvider, resProv, dataObjectSubsectionIndexes);
				for(Scheme s:gClusteredDataSet.getClusterSchemes())
				{
					s.setStrokeThickness(0, this.lineThickness);
				}
				gClusteredDataSet.setDataSubsetList(dataObjectSubsectionIndexes);
				gClusteredDataSet.setDrawMembershipLevels(this.drawCrispMembershipLevels);
				gClusteredDataSet.getDataObjectsTemplate().setPixelSize(this.dataObjectSize);
				gDataSet = gClusteredDataSet;
			}
			else
			{
				if(prototypeProvider != null)
				{
					int i=0;
					for(Prototype<double[]> p: prototypeProvider.getPrototypes())
					{
						GCentroid gCentroid = new GCentroid();
						gCentroid.setPrototype(p);
						gCentroid.setProjection(projection);
						gCentroid.getScheme().setColor(0, ColorList.clusterColors[i]);
						gCentroid.getScheme().setStrokeThickness(0, this.lineThickness);
						sv.screen.addDrawableObject(gCentroid);
						i++;
					}
				}
					
				GDataSet gPureDataSet = new GDataSet(dataSet);
				gPureDataSet.setDataSubsetList(dataObjectSubsectionIndexes);
				gPureDataSet.getScheme().setColor(0, ColorList.BLACK);
				gPureDataSet.getDataObjectsTemplate().setPixelSize(this.dataObjectSize);
				gDataSet = gPureDataSet;
			}
	
			gDataSet.setProjection(projection);
			sv.screen.addDrawableObject(gDataSet);
			sv.screen.setScreenToDisplayAllIndexed(dataSet);
		}
		
		
		sv.screen.setBackground(Color.WHITE);
		sv.screen.setFileName(filename);
		sv.setTitle(filename);
		sv.repaint();
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}
	
	
	/**
	 * Prints the specified screen to the hard disk. Note, that the
	 * fields <code>printSVG</code>, <code>printJPG</code>, <code>printPDF</code> and <code>printPNG</code>
	 * specify the data type the screen should be saved in. Any combination of the
	 * print-fields are possible.
	 * 
	 * @param screen The screen to be printed.
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 */
	private void print(Screen screen, String filename)
	{
		if(filename!=null && !filename.equals(""))
		{
			if(this.printPDF && this.printSVG) BatikExport.svgpdfExport(screen, filename);
			else if(this.printPDF) BatikExport.pdfExport(screen, filename);
			else if(this.printSVG) BatikExport.svgExport(screen, filename);
			
			if(this.printPNG) BatikExport.pngExport(screen, filename);
			if(this.printJPG) BatikExport.jpgExport(screen, filename);
		}
	}
}
