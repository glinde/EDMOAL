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
import java.util.Arrays;
import java.util.Collections;

import data.objects.doubleArray.DAEuclideanMetric;
import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.structures.balltree.BallTree;
import data.structures.balltree.CenteredBallTree;
import etc.DataGenerator;
import etc.DataManipulator;


/**
 * This class provides some functionality to test data structure algorithms and to verify their performance
 * of applying queries. When performing tests, please do take care not to disturb the process
 * by using other, CPU intensive or memory intensive applications.<br>
 * 
 * 
 * The data for testing is generated artificially, which is also done by this class.
 * 
 * @author Roland Winkler
 */
public class DataStructureSpeedTest extends TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 3319024847560964090L;

	/**
	 * The data set that is used for the tests.
	 */
	private IndexedDataSet<double[]> dataSet;
	
	/**
	 * The list of sphere query centres that are used for testing.
	 */
	private ArrayList<double[]> sphereQueryList;
	
	/**
	 * The list of sphere query radius values that are used for testing.
	 */
	private double[] shereQueryRadius;
	
	/**
	 * Randomly generated data objects that are used for k-nearest neighbour queries.
	 */
	private ArrayList<double[]> knnQueryRandomList;
	
	/**
	 * List of (randomly picked) data objects from the data set that are used for k-nearest neighbour queries.
	 */
	private ArrayList<IndexedDataObject<double[]>> knnQueryDataList;
	
	
	/**
	 * Creates a new test environment and generates a data set with the specified number of
	 * dimensions and data objects. Also generates the queries, used for testing.
	 * 
	 * @param dim The dimension of the data set.
	 * @param dataObjectCount The number of data objects in the data set.
	 * @param queryNumber The number of queries that are to be performed on the data set.
	 */
	public DataStructureSpeedTest(int dim, int dataObjectCount, int queryNumber)
	{
		super();
		
		ArrayList<double[]> data = new ArrayList<double[]>();
		ArrayList<double[]> seeds = new ArrayList<double[]>();
		int centers = 50;
		
		// generate data structure
		DataGenerator dg = new DataGenerator();		
		seeds.addAll(dg.uniformStandardPoints(dim, centers));
		
		for(double[] seed:seeds)
		{
			data.addAll(dg.gaussPoints(seed, 0.04*Math.sqrt(dg.generatorRand.nextDouble()), dataObjectCount/(2*centers)));
//			data.addAll(dg.gaussPoints(seed, 0.04d, dataObjectCount/(2*centers)));
		}
		data.addAll(dg.uniformStandardPoints(dim, dataObjectCount/2));

		Collections.shuffle(data);
		
		this.dataSet = new IndexedDataSet<double[]>(data);
		this.dataSet.seal();
		
		// queries
		this.sphereQueryList = dg.uniformStandardPoints(dim, queryNumber);
		this.shereQueryRadius = dg.uniformArray(queryNumber);
		for(int i=0; i<this.shereQueryRadius.length; i++) this.shereQueryRadius[i] = 0.009d*Math.sqrt(dim)*shereQueryRadius[i] + 0.001d;
		this.knnQueryRandomList = dg.uniformStandardPoints(dim, queryNumber);
		this.knnQueryDataList = DataManipulator.selectWithoutCopy(this.dataSet, queryNumber);
		
	}
	
	/**
	 * Shows a subset of the data set. This is useful because the test may be performed for
	 * very large data sets which are difficult to visualise. Therefore, this method
	 * only visualises a randomly picked subset of data objects of the original data set.
	 * 
	 * @param numberOfDataObjects The number of data objects to be visualised.
	 */
	public void showSimplifiedDataSet(int numberOfDataObjects)
	{
		ArrayList<IndexedDataObject<double[]>> smallerDataSet;
		
		smallerDataSet = DataManipulator.selectWithoutCopy(this.dataSet, numberOfDataObjects);
		
		this.showDataSet(smallerDataSet, null);
	}
	
	/**
	 * Performs a speed test on a ball tree structure, including
	 * time to build the tree, and the time needed for performing the queries.
	 */
	public void ballTreeSpeedTest()
	{
		long milliseconds = 0;
		int queryNumber = 100000;
		int knnK = 20;
		long queryResultCounter = 0L;
				
		ArrayList<IndexedDataObject<double[]>> queryResults = new ArrayList<IndexedDataObject<double[]>>(10000);
				
		// build tree
		System.out.println("");
		System.out.println("===== Ball Tree =====");
		System.out.print("Build ball tree structure .. ");
		milliseconds = -System.currentTimeMillis();
		BallTree<double[]> ballTree = new BallTree<double[]>(this.dataSet, new DAEuclideanMetric());
		ballTree.buildNaive();
		milliseconds += System.currentTimeMillis();
		System.out.println("done: "+ milliseconds + "ms");
		
		System.out.println("height: " + ballTree.height());
		System.out.println("tree size: " + ballTree.size());
		System.out.println("leaf depth distribution: " + Arrays.toString(ballTree.leafDepthDistribution()));
		System.out.println("number inner nodes: " + ballTree.numberOfInnerNodes());
		System.out.println("number leaf nodes: " + ballTree.numberOfLeafNodes());
		
		// sphere query
		System.out.println("");
		System.out.print(queryNumber + " sphere queries ");
		queryResultCounter = 0L;
		milliseconds = -System.currentTimeMillis();
		for(int i=0; i<queryNumber; i++)
		{
			if((i+1)%(queryNumber/10) == 0) System.out.print(".");
			queryResults.clear();
			ballTree.sphereQuery(queryResults, this.sphereQueryList.get(i), this.shereQueryRadius[i]);
			queryResultCounter += queryResults.size(); 
		}
		milliseconds += System.currentTimeMillis();		
		System.out.println("done: "+ milliseconds + "ms");
		System.out.println("Number of reported objects: " + queryResultCounter);
		
		// knn queries of random locations
		System.out.println("");
		System.out.print(queryNumber + " knn queries with random locations ");
		queryResultCounter = 0L;
		milliseconds = -System.currentTimeMillis();
		for(int i=0; i<queryNumber; i++)
		{
			if((i+1)%(queryNumber/10) == 0) System.out.print(".");
			queryResults.clear();
			ballTree.knnQuery(queryResults, this.knnQueryRandomList.get(i), knnK);
			queryResultCounter += queryResults.size(); 
		}
		milliseconds += System.currentTimeMillis();		
		System.out.println("done: "+ milliseconds + "ms");
		System.out.println("Number of reported objects: " + queryResultCounter);
		
		// knn queries of data set locations
		System.out.println("");
		System.out.print(queryNumber + " knn queries with locations from data set");
		queryResultCounter = 0L;
		milliseconds = -System.currentTimeMillis();
		for(int i=0; i<queryNumber; i++)
		{
			if((i+1)%(queryNumber/10) == 0) System.out.print(".");
			queryResults.clear();
			ballTree.knnQuery(queryResults, this.knnQueryDataList.get(i).x, knnK);
			queryResultCounter += queryResults.size(); 
		}
		milliseconds += System.currentTimeMillis();		
		System.out.println("done: "+ milliseconds + "ms");
		System.out.println("Number of reported objects: " + queryResultCounter);
	}

	/**
	 * Performs a speed test on a centered ball tree structure, including
	 * time to build the tree, and the time needed for performing the queries.
	 */
	public void centeredBallTreeSpeedTest()
	{
		long milliseconds = 0;
		int dim = dataSet.first().x.length;
		int queryNumber = 100000;
		int knnK = 20;
		long queryResultCounter = 0L;
				
		ArrayList<IndexedDataObject<double[]>> queryResults = new ArrayList<IndexedDataObject<double[]>>(10000);
				
		// build tree
		System.out.println("");
		System.out.println("===== Centered Ball Tree =====");
		System.out.print("Build tree structure .. ");
		milliseconds = -System.currentTimeMillis();
		CenteredBallTree<double[]> ballTree = new CenteredBallTree<double[]>(this.dataSet, new DAEuclideanVectorSpace(dim), new DAEuclideanMetric());
		ballTree.buildNaive();
		milliseconds += System.currentTimeMillis();
		System.out.println("done: "+ milliseconds + "ms");
		
		System.out.println("height: " + ballTree.height());
		System.out.println("tree size: " + ballTree.size());
		System.out.println("leaf depth distribution: " + Arrays.toString(ballTree.leafDepthDistribution()));
		System.out.println("number inner nodes: " + ballTree.numberOfInnerNodes());
		System.out.println("number leaf nodes: " + ballTree.numberOfLeafNodes());
		
		// sphere query
		System.out.println("");
		System.out.print(queryNumber + " sphere queries ");
		queryResultCounter = 0L;
		milliseconds = -System.currentTimeMillis();
		for(int i=0; i<queryNumber; i++)
		{
			if((i+1)%(queryNumber/10) == 0) System.out.print(".");
			queryResults.clear();
			ballTree.sphereQuery(queryResults, this.sphereQueryList.get(i), this.shereQueryRadius[i]);
			queryResultCounter += queryResults.size(); 
		}
		milliseconds += System.currentTimeMillis();		
		System.out.println("done: "+ milliseconds + "ms");
		System.out.println("Number of reported objects: " + queryResultCounter);
		
		// knn queries of random locations
		System.out.println("");
		System.out.print(queryNumber + " knn queries with random locations ");
		queryResultCounter = 0L;
		milliseconds = -System.currentTimeMillis();
		for(int i=0; i<queryNumber; i++)
		{
			if((i+1)%(queryNumber/10) == 0) System.out.print(".");
			queryResults.clear();
			ballTree.knnQuery(queryResults, this.knnQueryRandomList.get(i), knnK);
			queryResultCounter += queryResults.size(); 
		}
		milliseconds += System.currentTimeMillis();		
		System.out.println("done: "+ milliseconds + "ms");
		System.out.println("Number of reported objects: " + queryResultCounter);
		
		// knn queries of data set locations
		System.out.println("");
		System.out.print(queryNumber + " knn queries with locations from data set");
		queryResultCounter = 0L;
		milliseconds = -System.currentTimeMillis();
		for(int i=0; i<queryNumber; i++)
		{
			if((i+1)%(queryNumber/10) == 0) System.out.print(".");
			queryResults.clear();
			ballTree.knnQuery(queryResults, this.knnQueryDataList.get(i).x, knnK);
			queryResultCounter += queryResults.size(); 
		}
		milliseconds += System.currentTimeMillis();		
		System.out.println("done: "+ milliseconds + "ms");
		System.out.println("Number of reported objects: " + queryResultCounter);
	}
}
