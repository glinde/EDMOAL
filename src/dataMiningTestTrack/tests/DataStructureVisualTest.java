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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import data.objects.doubleArray.DAEuclideanMetric;
import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.structures.balltree.BallTree;
import data.structures.balltree.BallTreeNode;
import data.structures.balltree.CenteredBallTree;
import data.structures.balltree.CenteredBallTreeNode;
import etc.DataGenerator;
import gui.ColorList;
import gui.ScreenViewer;
import gui.DataMiningGraphics.GDataSet;
import gui.generalGraphics.GCircle;
import gui.projections.Orthogonal2DProjection;
import gui.templates.DrawableTemplate;
import gui.templates.GeomTemplate;


/**
 * This class provides some functionality to test data structure algorithms and to verify them visually.
 * The data for testing is generated artificially, which is also done by this class.<br>
 * 
 * The data set is just 2-dimensional and semi-randomly generated.
 * The actual location of the data objects is randomly generated, but the distribution
 * of the data objects is predefined.
 *
 * @author Roland Winkler
 */
public class DataStructureVisualTest extends TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 7166084323032199964L;
	
	/**
	 *  The data set, used for the tests.
	 */
	private IndexedDataSet<double[]> dataSet;
	
	/**
	 * Generates a new test environment. The data set is just 2-dimensional and semi-randomly generated.
	 * The actual location of the data objects is randomly generated, but the distribution
	 * of the data objects is predefined.
	 * 
	 * @param dataObjectCount The number of data objects, generated for this test.
	 */
	public DataStructureVisualTest(int dataObjectCount)
	{
		ArrayList<double[]> data = new ArrayList<double[]>();
				
		// generate data structure
		DataGenerator dg = new DataGenerator();		
		data.addAll(dg.uniformStandardPoints(2, 						1*dataObjectCount/10));		
		data.addAll(dg.gaussPoints(new double[]{0.3d, 0.8d}, 0.1, 		2*dataObjectCount/10));	
		data.addAll(dg.gaussPoints(new double[]{0.8d, 0.5d}, 0.02, 		2*dataObjectCount/10));	
		data.addAll(dg.gaussPoints(new double[]{0.4d, 0.1d}, 0.1, 		2*dataObjectCount/10));
		data.addAll(dg.gaussPoints(new double[]{0.2d, 0.2d}, 0.001, 	3*dataObjectCount/10));

		Collections.shuffle(data);
		
		this.dataSet = new IndexedDataSet<double[]>(data);
		this.dataSet.seal();
	}
	
	/**
	 * Organises the data set in a ball tree, using the naive build. It visualises
	 * the data set according to each level of the tree. So for each level, a
	 * new window is opened and the properties of the nodes of this level are
	 * visualised.
	 */
	public void ballTreeStructureTest()
	{
		int k = 10;
		int maxVisuaiDepth = 3;

		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
		
		ScreenViewer dataViewFrame;
		ArrayList<ArrayList<IndexedDataObject<double[]>>> levelList = new ArrayList<ArrayList<IndexedDataObject<double[]>>>();
		GDataSet graphicsCluster;
		GCircle graphicsCircle;
		DrawableTemplate template;
		
		int i;
		ArrayList<ArrayList<IndexedDataObject<double[]>>> levelSets;
		ArrayList<BallTreeNode<double[]>> nodesOfLevel;

		BallTree<double[]> ballTree = new BallTree<double[]>(this.dataSet, new DAEuclideanMetric());
		ballTree.buildNaive();
		

		// data structure visualization		
		for(k = 0; k<ballTree.height() && k<maxVisuaiDepth; k++)
		{
			levelList.clear();
			dataViewFrame = new ScreenViewer(this.xRes, this.yRes);
//			dataViewFrame.screen.getTranslator().moveOffset(0.5d, 0.5d);	
			dataViewFrame.screen.setScreenToDisplayAllIndexed(this.dataSet, projection);
			levelSets = ballTree.subtreeElementsOfLevel(k);
			nodesOfLevel = ballTree.nodesOfLevel(k);

			System.out.println("number of Nodes of Level "+k+": " + levelSets.size());
			
			// draw subtree data objects
			for(i=0; i<levelSets.size(); i++)
			{
				graphicsCluster = new GDataSet(levelSets.get(i));
//				graphicsCluster.setDrawPrototype(false);
				graphicsCluster.calcCrispInternalSourrounding();
				graphicsCluster.setDrawInternalArea(true);
				graphicsCluster.setInternalAreaAlpha(50);
				graphicsCluster.getScheme().setColor(0, ColorList.BLACK);
				graphicsCluster.getDataObjectsTemplate().setPixelSize(4.0d);
				graphicsCluster.getScheme().setStrokeThickness(graphicsCluster.getConvexHullStrokeIndex(), 2.0d);
				
				// add circles
				graphicsCircle = new GCircle(null);
				graphicsCluster.addChild(graphicsCircle);
				graphicsCircle.setCenter(nodesOfLevel.get(i).getObj().x);
				graphicsCircle.setRadius(nodesOfLevel.get(i).getRadius());
				graphicsCircle.getScheme().setColor(graphicsCircle.getBorderColorIndex(), ColorList.RED);
				graphicsCircle.setDrawInternal(false);
				graphicsCircle.getScheme().setStrokeThickness(graphicsCircle.getStrokeIndex(), 2.0d);

				// show center of circle
				template = new DrawableTemplate(null, new GeomTemplate(GeomTemplate.FILL_CIRCLE));
				graphicsCluster.addChild(template);
				template.setPosition(nodesOfLevel.get(i).getObj().x);
				template.getBody().setPixelSize(32.0d);
				template.getScheme().setColor(template.getBody().getInternalColorIndex(), ColorList.RED);
				template.getScheme().setColor(template.getBody().getBorderColorIndex(), ColorList.BLACK);
				template.getScheme().setStrokeThickness(graphicsCircle.getStrokeIndex(), 2.0d);
				
				// show node object
				template = new DrawableTemplate(null, new GeomTemplate(GeomTemplate.FILL_CROSS));
				graphicsCluster.addChild(template);
				template.setPosition(nodesOfLevel.get(i).getObj().x);
				template.getBody().setPixelSize(16.0d);
				template.getScheme().setColor(template.getBody().getInternalColorIndex(), ColorList.RED);
				template.getScheme().setColor(template.getBody().getBorderColorIndex(), ColorList.BLACK);
				template.getScheme().setStrokeThickness(graphicsCircle.getStrokeIndex(), 2.0d);
				
				dataViewFrame.screen.addDrawableObject(graphicsCluster);
			}
			

			dataViewFrame.setTitle("Ball Tree Level " + k);
			dataViewFrame.repaint();
		}
	}

	
	/**
	 * Organises the data set in a centered ball tree, using the naive build. It visualises
	 * the data set according to each level of the tree. So for each level, a
	 * new window is opened and the properties of the nodes of this level are
	 * visualised.
	 */
	public void centeredBallTreeStructureTest()
	{
		int k = 10;
		int maxVisuaiDepth = 3;
		
		Orthogonal2DProjection projection = new Orthogonal2DProjection();
		projection.setDimensionX(this.xIndex);
		projection.setDimensionY(this.yIndex);
				
		ScreenViewer dataViewFrame;
		ArrayList<ArrayList<IndexedDataObject<double[]>>> levelList = new ArrayList<ArrayList<IndexedDataObject<double[]>>>();
		GDataSet graphicsCluster;
		GCircle graphicsCircle;
		DrawableTemplate template;
		
		int i;
		ArrayList<ArrayList<IndexedDataObject<double[]>>> levelSets;
		ArrayList<CenteredBallTreeNode<double[]>> nodesOfLevel;

		CenteredBallTree<double[]> cballTree = new CenteredBallTree<double[]>(this.dataSet, new DAEuclideanVectorSpace(2), new DAEuclideanMetric());
		cballTree.buildNaive();
		

		// data structure visualization		
		for(k = 0; k<cballTree.height() && k<maxVisuaiDepth; k++)
		{
			levelList.clear();
			dataViewFrame = new ScreenViewer(this.xRes, this.yRes);
//			dataViewFrame.screen.getTranslator().moveOffset(0.5d, 0.5d);	
			dataViewFrame.screen.setScreenToDisplayAllIndexed(this.dataSet, projection);
			levelSets = cballTree.subtreeElementsOfLevel(k);
			nodesOfLevel = cballTree.nodesOfLevel(k);

			System.out.println("number of Nodes of Level "+k+": " + levelSets.size());
			
			// draw subtree data objects
			for(i=0; i<levelSets.size(); i++)
			{
				graphicsCluster = new GDataSet(levelSets.get(i));
//				graphicsCluster.setDrawPrototype(false);
				graphicsCluster.calcCrispInternalSourrounding();
				graphicsCluster.setDrawInternalArea(true);
				graphicsCluster.setInternalAreaAlpha(50);
				graphicsCluster.getScheme().setColor(0, ColorList.BLACK);
				graphicsCluster.getDataObjectsTemplate().setPixelSize(4.0d);
				graphicsCluster.getScheme().setStrokeThickness(graphicsCluster.getConvexHullStrokeIndex(), 2.0d);
				
				// add circles
				graphicsCircle = new GCircle(null);
				graphicsCluster.addChild(graphicsCircle);
				graphicsCircle.setCenter(nodesOfLevel.get(i).getCenterOfGravity());
				graphicsCircle.setRadius(nodesOfLevel.get(i).getRadius());
				graphicsCircle.getScheme().setColor(graphicsCircle.getBorderColorIndex(), ColorList.RED);
				graphicsCircle.setDrawInternal(false);
				graphicsCircle.getScheme().setStrokeThickness(graphicsCircle.getStrokeIndex(), 2.0d);

				// show center of circle
				template = new DrawableTemplate(null, new GeomTemplate(GeomTemplate.FILL_CIRCLE));
				graphicsCluster.addChild(template);
				template.setPosition(nodesOfLevel.get(i).getCenterOfGravity());
				template.getBody().setPixelSize(32.0d);
				template.getScheme().setColor(template.getBody().getInternalColorIndex(), ColorList.RED);
				template.getScheme().setColor(template.getBody().getBorderColorIndex(), ColorList.BLACK);
				template.getScheme().setStrokeThickness(graphicsCircle.getStrokeIndex(), 2.0d);
				
				// show node object
				template = new DrawableTemplate(null, new GeomTemplate(GeomTemplate.FILL_CROSS));
				graphicsCluster.addChild(template);
				template.setPosition(nodesOfLevel.get(i).getObj().x);
				template.getBody().setPixelSize(16.0d);
				template.getScheme().setColor(template.getBody().getInternalColorIndex(), ColorList.RED);
				template.getScheme().setColor(template.getBody().getBorderColorIndex(), ColorList.BLACK);
				template.getScheme().setStrokeThickness(graphicsCircle.getStrokeIndex(), 2.0d);
				
				
				dataViewFrame.screen.addDrawableObject(graphicsCluster);
			}
			
			dataViewFrame.setTitle("Centered Ball Tree Level " + k);
			dataViewFrame.repaint();
		}
	}
}
