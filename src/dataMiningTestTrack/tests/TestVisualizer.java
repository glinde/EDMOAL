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

import data.objects.matrix.FeatureSpaceSampling2D;
import data.set.IndexedDataObject;
import datamining.DataMiningAlgorithm;
import datamining.clustering.ClusteringAlgorithm;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;
import datamining.clustering.protoype.PrototypeClusteringAlgorithm;
import datamining.gradient.centroid.SingleCentroidGradientOptimizationAlgorithm;
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
	
	
	/**  */
	public int xIndex;
	
	/**  */
	public int yIndex;
	
	/**  */
	public int xRes;
	
	/**  */
	public int yRes;
	
	
	/**
	 * The standard constructor.
	 */
	public TestVisualizer()
	{
		this.printSVG = false;
		this.printJPG = false;
		this.printPDF = false;
		this.printPNG = false;
		
		this.dataObjectSize = 5.0f;
		this.seriesColorList = new Color[]{
				ColorList.RED,				ColorList.GREEN,			ColorList.BLUE,
				ColorList.ORANGE,			ColorList.MAGENTA,			ColorList.CYAN,
				ColorList.BRIGHT_RED,		ColorList.BRIGHT_GREEN,		ColorList.BRIGHT_BLUE,
				ColorList.BRIGHT_ORANGE,	ColorList.BRIGHT_MAGENTA,	ColorList.BRIGHT_CYAN,
				ColorList.DARK_RED,			ColorList.DARK_GREEN,		ColorList.DARK_BLUE,
				ColorList.DARK_ORANGE,		ColorList.DARK_MAGENTA,		ColorList.DARK_CYAN
			};
		
		this.xIndex = 0;
		this.yIndex = 1;
		
		this.xRes = 1050;
		this.yRes = 1050;
	}
	
	/**
	 * Creates a visualisation of the specified clustering algorithm and its results.
	 * The visualisation is automatically adopted to the type of the algorithm.
	 * 
	 * @param clusterAlgo The algorithm that is to be visualised.
	 * @param title The title of the window (just for easy window management, the text is not shown in the figure it self).
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 */
	@SuppressWarnings("unchecked")
	public void showClusteringAlgorithm(ClusteringAlgorithm<double[]> clusterAlgo, ResultProvider<double[]> resultProvider, String title, String filename)
	{
		GClusteredDataSet gClusteredDS;
		ScreenViewer sv;
		ArrayList<GCentroid> gPrototypes = new ArrayList<GCentroid>();
		
		if(clusterAlgo instanceof PrototypeProvider)
		{
			try
			{
				gClusteredDS = new GCentroidClusteringAlgorithm((PrototypeProvider<double[], ? extends Centroid<double[]>>) clusterAlgo, resultProvider);

			}
			catch(ClassCastException e)
			{
				gClusteredDS = new GClusteredDataSet(clusterAlgo, resultProvider);
				
			}
			
		}
		else
		{
			gClusteredDS = new GClusteredDataSet(clusterAlgo, resultProvider);
		}

		gClusteredDS.setDrawMembershipLevels(true);


		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gClusteredDS.setProjection(projection);
		gClusteredDS.getDataObjectsTemplate().setPixelSize(this.dataObjectSize);

		sv = new ScreenViewer(this.xRes, this.yRes);
		sv.screen.setFileName(filename);
		sv.screen.setBackground(Color.WHITE);
		sv.screen.addDrawableObject(gClusteredDS);
		sv.screen.setScreenToDisplayAllIndexed(clusterAlgo.getDataSet());
		sv.setTitle(title);
		sv.repaint();
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}

	/**
	 * Creates a visualisation of the specified data mining algorithm and its results.
	 * The visualisation is automatically adopted to the type of the algorithm.
	 * 
	 * @param dmAlgo The algorithm that is to be visualised.
	 * @param resultProvider The result of the data mining.
	 * @param title The title of the window (just for easy window management, the text is not shown in the figure it self).
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 * @param dataObjectSubsectionIndexes
	 */
	@SuppressWarnings("unchecked")
	public void showDataMiningAlgorithm(DataMiningAlgorithm<double[]> dmAlgo, ResultProvider<double[]> resultProvider, int[] dataObjectSubsectionIndexes, String title, String filename)
	{
		GClusteredDataSet gClusteredDS = null;
		ScreenViewer sv;
		
		if(dmAlgo instanceof PrototypeProvider && dmAlgo instanceof ClusteringAlgorithm)
		{

			gClusteredDS = new GCentroidClusteringAlgorithm((PrototypeProvider<double[], ? extends Prototype<double[]>>)dmAlgo, resultProvider, dataObjectSubsectionIndexes);

		}
		else if(dmAlgo instanceof ClusteringAlgorithm)
		{
			gClusteredDS = new GClusteredDataSet((ClusteringAlgorithm<double[]>)dmAlgo, resultProvider, dataObjectSubsectionIndexes);
		}
		else
		{
			return;
		}

		gClusteredDS.setDrawMembershipLevels(true);


		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gClusteredDS.setProjection(projection);
		gClusteredDS.getDataObjectsTemplate().setPixelSize(this.dataObjectSize);

		sv = new ScreenViewer();
		sv.screen.setFileName(filename);
		sv.screen.setBackground(Color.WHITE);
		sv.screen.addDrawableObject(gClusteredDS);
		sv.screen.setScreenToDisplayAllIndexed(dmAlgo.getDataSet());
		sv.setTitle(title);
		sv.repaint();
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}

	/**
	 * Creates a visualisation of the specified data mining algorithm and its results.
	 * The visualisation is automatically adopted to the type of the algorithm.
	 * 
	 * @param dmAlgo The algorithm that is to be visualised.
	 * @param resultProvider The result of the data mining.
	 * @param title The title of the window (just for easy window management, the text is not shown in the figure it self).
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 * @param dataObjectSubsectionIndexes
	 */
	@SuppressWarnings("unchecked")
	public void showDataMiningAlgorithm(DataMiningAlgorithm<double[]> dmAlgo, ResultProvider<double[]> resultProvider, String title, String filename)
	{
		GClusteredDataSet gClusteredDS = null;
		ScreenViewer sv;
		
		if(dmAlgo instanceof PrototypeProvider)
		{

			gClusteredDS = new GCentroidClusteringAlgorithm((PrototypeProvider<double[], ? extends Prototype<double[]>>)dmAlgo, resultProvider);

		}
		else if(dmAlgo instanceof ClusteringAlgorithm)
		{
			gClusteredDS = new GClusteredDataSet((ClusteringAlgorithm<double[]>)dmAlgo, resultProvider);
		}
		else
		{
			return;
		}

		gClusteredDS.setDrawMembershipLevels(true);


		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gClusteredDS.setProjection(projection);
		gClusteredDS.getDataObjectsTemplate().setPixelSize(this.dataObjectSize);

		sv = new ScreenViewer(this.xRes, this.yRes);
		sv.screen.setFileName(filename);
		sv.screen.setBackground(Color.WHITE);
		sv.screen.addDrawableObject(gClusteredDS);
		sv.screen.setScreenToDisplayAllIndexed(dmAlgo.getDataSet());
		sv.setTitle(title);
		sv.repaint();
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}

	/**
	 * Creates a visualisation of the specified data mining algorithm and its results.
	 * The visualisation is automatically adopted to the type of the algorithm.
	 * 
	 * @param dmAlgo The algorithm that is to be visualised.
	 * @param resultProvider The result of the data mining.
	 * @param title The title of the window (just for easy window management, the text is not shown in the figure it self).
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 * @param dataObjectSubsectionIndexes
	 */
	@SuppressWarnings("unchecked")
	public void showDataMiningAlgorithm(DataMiningAlgorithm<double[]> dmAlgo, ResultProvider<double[]> resultProvider, boolean drawCrispMembershipLevels, String title, String filename)
	{
		GClusteredDataSet gClusteredDS = null;
		ScreenViewer sv;
		
		if(dmAlgo instanceof PrototypeProvider)
		{

			gClusteredDS = new GCentroidClusteringAlgorithm((PrototypeProvider<double[], ? extends Prototype<double[]>>)dmAlgo, resultProvider);

		}
		else if(dmAlgo instanceof ClusteringAlgorithm)
		{
			gClusteredDS = new GClusteredDataSet((ClusteringAlgorithm<double[]>)dmAlgo, resultProvider);
		}
		else
		{
			return;
		}

		gClusteredDS.setDrawMembershipLevels(drawCrispMembershipLevels);


		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gClusteredDS.setProjection(projection);
		gClusteredDS.getDataObjectsTemplate().setPixelSize(this.dataObjectSize);

		sv = new ScreenViewer(this.xRes, this.yRes);
		sv.screen.setFileName(filename);
		sv.screen.setBackground(Color.WHITE);
		sv.screen.addDrawableObject(gClusteredDS);
		sv.screen.setScreenToDisplayAllIndexed(dmAlgo.getDataSet());
		sv.setTitle(title);
		sv.repaint();
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}


	/**
	 * Creates a visualisation of the specified clustering algorithm and its results.
	 * The visualisation is automatically adopted to the type of the algorithm.
	 * 
	 * @param clusterAlgo The algorithm that is to be visualised.
	 * @param title The title of the window (just for easy window management, the text is not shown in the figure it self).
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 * @param dataObjectSubsectionIndexes
	 */
	@SuppressWarnings("unchecked")
	public void showClusteringAlgorithm(ClusteringAlgorithm<double[]> clusterAlgo, ResultProvider<double[]> resultProvider, int[] dataObjectSubsectionIndexes, String title, String filename)
	{
		GClusteredDataSet gClusteredDS;
		ScreenViewer sv;
		
		if(clusterAlgo instanceof PrototypeClusteringAlgorithm)
		{
			gClusteredDS = new GCentroidClusteringAlgorithm((PrototypeClusteringAlgorithm<double[], ? extends Centroid<double[]>>) clusterAlgo, resultProvider, dataObjectSubsectionIndexes);
		}
		else
		{
			gClusteredDS = new GClusteredDataSet(clusterAlgo, resultProvider, dataObjectSubsectionIndexes);
		}

		gClusteredDS.setDrawMembershipLevels(true);

		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gClusteredDS.setProjection(projection);
		gClusteredDS.getDataObjectsTemplate().setPixelSize(this.dataObjectSize);

		sv = new ScreenViewer();
		sv.screen.setFileName(filename);
		sv.screen.setBackground(Color.WHITE);
		sv.screen.addDrawableObject(gClusteredDS);
		sv.screen.setScreenToDisplayAllIndexed(clusterAlgo.getDataSet());
		sv.setTitle(title);
		sv.repaint();
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}
	
	/**
	 * Creates a visualisation of the specified gradient algorithm and the way of its centroid.
	 * 
	 * @param gradientAlgo The algorithm that is to be visualised.
	 * @param title The title of the window (just for easy window management, the text is not shown in the figure it self).
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 */
	public void showSingleCentroidGradientAlgorithm(SingleCentroidGradientOptimizationAlgorithm<double[]> gradientAlgo, String title, String filename)
	{
		ScreenViewer sv  = new ScreenViewer();
		GDataSet gCluster = new GDataSet();

		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gCluster.setProjection(projection);
		gCluster.setDataObjects(gradientAlgo.getDataSet());
		gCluster.getScheme().setColor(0, ColorList.BLACK);
		gCluster.getDataObjectsTemplate().setPixelSize(4.0d);
		
		GCentroid gCentroid = new GCentroid();
		gCentroid.setPrototype(gradientAlgo.getCentroid());
		gCentroid.setProjection(projection);
		
		sv.screen.setFileName(filename);
//		sv.setPreferredSize(new Dimension(1200, 800));
//		sv.setSize(new Dimension(1200, 800));
		sv.screen.addDrawableObject(gCluster);
		sv.screen.addDrawableObject(gCentroid);
//		sv.screen.addDrawableObject(new GScale());
//		sv.screen.getTranslator().moveOffset(new double[]{0.0d, 1.0d});
//		sv.screen.zoomToDisplay(data);
		sv.screen.setScreenToDisplayAllIndexed(gradientAlgo.getDataSet());
		sv.repaint();
		sv.setTitle(filename);
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}

	/**
	 * Creates a visualisation of the specified gradient algorithm and the way of its centroid.
	 * 
	 * @param gradientAlgo The algorithm that is to be visualised.
	 * @param title The title of the window (just for easy window management, the text is not shown in the figure it self).
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 */
	public void showDataSetImaged(Collection<IndexedDataObject<double[]>> dataSet, FeatureSpaceSampling2D matrix, String title, String filename)
	{
		ScreenViewer sv  = new ScreenViewer();
		GDataSet gCluster = new GDataSet();

		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gCluster.setProjection(projection);
		gCluster.setDataObjects(dataSet);
		gCluster.getScheme().setColor(0, ColorList.BLACK);
		gCluster.getDataObjectsTemplate().setPixelSize(4.0d);
		
		
		GImage image = new GImage(null, matrix);
//		image.generateTestImage(0.0d, 0.0d, 100, 100, 100);
		
		sv.screen.setFileName(filename);
//		sv.setPreferredSize(new Dimension(1200, 800));
//		sv.setSize(new Dimension(1200, 800));
		sv.screen.addDrawableObject(image);
		sv.screen.addDrawableObject(gCluster);
//		sv.screen.addDrawableObject(new GScale());
//		sv.screen.getTranslator().moveOffset(new double[]{0.0d, 1.0d});
//		sv.screen.zoomToDisplay(data);
		sv.screen.setScreenToDisplayAllIndexed(dataSet);
		sv.repaint();
		sv.setTitle(filename);
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}
	
	/**
	 * Visualises the specified data set. Currently, only the first two dimensions of
	 * the data vectors are presented. For visualising other dimensions, please change
	 * the order of the attributes in the data vector.
	 * 
	 * @param dataSet The data set to be visualised.
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 */
	public void showDataSet(Collection<IndexedDataObject<double[]>> dataSet, String filename)
	{
		ScreenViewer sv  = new ScreenViewer(this.xRes, this.yRes);
		GDataSet gCluster = new GDataSet();
		
		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gCluster.setProjection(projection);
		gCluster.setDataObjects(dataSet);
		gCluster.getScheme().setColor(0, ColorList.BLACK);
		gCluster.getDataObjectsTemplate().setPixelSize(this.dataObjectSize);
		
		sv.screen.setFileName(filename);
		sv.screen.addDrawableObject(gCluster);
//		sv.screen.addDrawableObject(new GScale());
//		sv.screen.getTranslator().moveOffset(new double[]{0.0d, 1.0d});
//		sv.screen.zoomToDisplay(data);
		sv.screen.setScreenToDisplayAllIndexed(dataSet);
		sv.repaint();
		sv.setTitle(filename);
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}
	
	/**
	 * Visualises the specified data set. Currently, only the first two dimensions of
	 * the data vectors are presented. For visualising other dimensions, please change
	 * the order of the attributes in the data vector.
	 * 
	 * @param dataSet The data set to be visualised.
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 */
	public void showDataSet(Collection<IndexedDataObject<double[]>> dataSet, int[] dataObjectSubsectionIndexes, String filename)
	{
		ScreenViewer sv  = new ScreenViewer(this.xRes, this.yRes);
		GDataSet gCluster = new GDataSet();

		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gCluster.setProjection(projection);
		gCluster.setDataObjects(dataSet);
		gCluster.setDataSubsetList(dataObjectSubsectionIndexes);
		gCluster.getScheme().setColor(0, ColorList.BLACK);
		gCluster.getDataObjectsTemplate().setPixelSize(4.0d);
		
		sv.screen.setFileName(filename);
	//	sv.setPreferredSize(new Dimension(1200, 800));
	//	sv.setSize(new Dimension(1200, 800));
		sv.screen.addDrawableObject(gCluster);
//		sv.screen.addDrawableObject(new GScale());
	//	sv.screen.getTranslator().moveOffset(new double[]{0.0d, 1.0d});
	//	sv.screen.zoomToDisplay(data);
		sv.screen.setScreenToDisplayAllIndexed(dataSet);
		sv.repaint();
		sv.setTitle(filename);
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}
	

	/**
	 * Presents the specified data set, coloured by the specified crisp clustering (partitioning). Currently,
	 * only the first two dimensions of the data vectors are presented. For visualising other
	 * dimensions, please change the order of the attributes in the data vector.
	 * 
	 * @param dataSet The data set to be visualised.
	 * @param clusterCount The number of clusters in the clustering or the number of partitions of the data set.
	 * @param crispCluster The crisp clustering (partitioning) that should be used for colouring the data.
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 */
	public void showCrispDataSetClustering(Collection<IndexedDataObject<double[]>> dataSet, int clusterCount, int[] crispCluster, String filename)
	{
		GClusteredDataSet gClusteredDS;
		ScreenViewer sv;
		
		gClusteredDS = new GClusteredDataSet(clusterCount);

		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gClusteredDS.setProjection(projection);
		gClusteredDS.setDrawMembershipLevels(false);
		gClusteredDS.getDataObjectsTemplate().setPixelSize(this.dataObjectSize);
//		gClusteredDS.getDataObjectsTemplate().setPixelSize(4.0d);
		gClusteredDS.setFuzzyColoring(false);
		gClusteredDS.setDataSet(dataSet);
		gClusteredDS.setCrispClusterAssignments(crispCluster);

		sv = new ScreenViewer(this.xRes, this.yRes);
		sv.screen.setFileName(filename);
//		sv.setPreferredSize(new Dimension(1200, 800));
//		sv.setSize(new Dimension(1200, 800));
		sv.screen.addDrawableObject(gClusteredDS);
//		sv.screen.addDrawableObject(new GScale());
//		sv.screen.getTranslator().moveOffset(new double[]{0.0d, 1.0d});
//		sv.screen.zoomToDisplay(data);
		sv.screen.setScreenToDisplayAllIndexed(dataSet);
		sv.repaint();
		sv.setTitle(filename);
		sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.print(sv.screen, filename);
	}


	/**
	 * Presents the specified data set, coloured by the specified crisp clustering (partitioning). Currently,
	 * only the first two dimensions of the data vectors are presented. For visualising other
	 * dimensions, please change the order of the attributes in the data vector.
	 * 
	 * @param dataSet The data set to be visualised.
	 * @param clusterCount The number of clusters in the clustering or the number of partitions of the data set.
	 * @param crispCluster The crisp clustering (partitioning) that should be used for colouring the data.
	 * @param filename The filename if the figure is supposed to be saved as picture on the hard disk.
	 */
	public void showFuzzyDataSetClustering(Collection<IndexedDataObject<double[]>> dataSet, Collection<double[]> clustering, String filename)
	{
		GClusteredDataSet gClusteredDS;
		ScreenViewer sv;
		
		gClusteredDS = new GClusteredDataSet(clustering.iterator().next().length);

		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		gClusteredDS.setProjection(projection);
		gClusteredDS.setDrawMembershipLevels(false);
		gClusteredDS.getDataObjectsTemplate().setPixelSize(4.0d);
		gClusteredDS.setFuzzyColoring(true);
		gClusteredDS.setDataSet(dataSet);
		gClusteredDS.setFuzzyMemberships(clustering);

		sv = new ScreenViewer(this.xRes, this.yRes);
		sv.screen.setFileName(filename);
//		sv.setPreferredSize(new Dimension(1200, 800));
//		sv.setSize(new Dimension(1200, 800));
		sv.screen.addDrawableObject(gClusteredDS);
//		sv.screen.addDrawableObject(new GScale());
//		sv.screen.getTranslator().moveOffset(new double[]{0.0d, 1.0d});
//		sv.screen.zoomToDisplay(data);
		sv.screen.setScreenToDisplayAllIndexed(dataSet);
		sv.repaint();
		sv.setTitle(filename);
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
