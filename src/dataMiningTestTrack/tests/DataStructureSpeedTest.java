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

import data.objects.doubleArray.DAEuclideanDistance;
import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import data.set.structures.BallTree;
import data.set.structures.CenteredBallTree;
import etc.DataGenerator;
import etc.DataManipulator;


/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DataStructureSpeedTest extends TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 3319024847560964090L;

	private IndexedDataSet<double[]> dataSet;
	
	private ArrayList<double[]> sphereQueryList;
	private double[] shereQueryRadius;
	private ArrayList<double[]> knnQueryRandomList;
	private ArrayList<IndexedDataObject<double[]>> knnQueryDataList;
	
	
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
	 * @param numberOfDataObjects
	 */
	public void showSimplifiedDataSet(int numberOfDataObjects)
	{
		ArrayList<IndexedDataObject<double[]>> smallerDataSet;
		
		smallerDataSet = DataManipulator.selectWithoutCopy(this.dataSet, numberOfDataObjects);
		
		this.showDataSet(smallerDataSet, null);
	}
	
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
		BallTree<double[]> ballTree = new BallTree<double[]>(this.dataSet, new DAEuclideanDistance());
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
			ballTree.knnQuery(queryResults, this.knnQueryDataList.get(i).element, knnK);
			queryResultCounter += queryResults.size(); 
		}
		milliseconds += System.currentTimeMillis();		
		System.out.println("done: "+ milliseconds + "ms");
		System.out.println("Number of reported objects: " + queryResultCounter);
	}

	public void centeredBallTreeSpeedTest()
	{
		long milliseconds = 0;
		int dim = dataSet.first().element.length;
		int queryNumber = 100000;
		int knnK = 20;
		long queryResultCounter = 0L;
				
		ArrayList<IndexedDataObject<double[]>> queryResults = new ArrayList<IndexedDataObject<double[]>>(10000);
				
		// build tree
		System.out.println("");
		System.out.println("===== Centered Ball Tree =====");
		System.out.print("Build tree structure .. ");
		milliseconds = -System.currentTimeMillis();
		CenteredBallTree<double[]> ballTree = new CenteredBallTree<double[]>(this.dataSet, new DAEuclideanVectorSpace(dim), new DAEuclideanDistance());
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
			ballTree.knnQuery(queryResults, this.knnQueryDataList.get(i).element, knnK);
			queryResultCounter += queryResults.size(); 
		}
		milliseconds += System.currentTimeMillis();		
		System.out.println("done: "+ milliseconds + "ms");
		System.out.println("Number of reported objects: " + queryResultCounter);
	}
}
