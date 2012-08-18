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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import data.objects.doubleArray.DAEuclideanMetric;
import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.density.DBScan;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.altopt.BallTreeFuzzyCMeansClusteringAlgorithm;
import datamining.clustering.protoype.altopt.DistAdaptedFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.DistAdaptedFCMNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.ExpectationMaximizationSGMMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm;
import datamining.clustering.protoype.altopt.FuzzyCMeansNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.HardCMeansClusteringAlgorithm;
import datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.PolynomFCMNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.RewardingCrispFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.RewardingCrispFCMNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.VoronoiPartitionFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.VoronoiPartitionFCMNoiseClusteringAlgorithm;
import datamining.clustering.protoype.initial.DoubleArrayPrototypeGenerator;
import etc.DataGenerator;


/**
 * This class provides some functionality to test cluster algorithms and to verify the result visually and
 * give some numbers on their execution time.
 * The data for testing is generated artificially, which is also done by this class.<br>
 * 
 * The data set is generated in the following way: The number of clusters determines the
 * number of seeds that are uniformly distributed in the hypercube with cornes at each
 * dimension at 0.1 and 0.9 (the hypercube is specified by [0.1, 0.9]^dim).
 * For each seed, a set of dim-dimensional normal distributed data objects is generated.
 * The variance of the (dim-dimensional) normal distribution is randomly picked between 0 and 0.1.
 * Finally, 10% of noise data objects is added to the data set.
 * The noise data objects are generated uniformly on the unit-hypercube ([0, 1]^dim) 
 * 
 *
 * @author Roland Winkler
 */
public class ClusterAlgorithmVisualSpeedTest extends TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -5614041122326224796L;

	/**
	 * The data set to be clustered
	 */
	private IndexedDataSet<double[]> dataSet;
	
	/**
	 * The number of clusters
	 */
	private int clusterCount;
		
	
	/** 
	 * The number of data objects that should randomly be piced for visualization.
	 */
	private int visibleDataObjects;
	
	/** The list of data objects that are shown during the visualization. */
	private int[] indexesOfVisibleDataObjects;
	
	/**
	 * The initial positions of prototypes.
	 */
	private ArrayList<double[]> initialPositons;
	
	/**
	 * The standard constructor. It generates a data set of double arrays with the specified
	 * dimension and number of data objects and generates the starting positions for prototypes
	 * according to the number of clusters.<br>
	 * 
	 * The data set is generated in the following way: The number of clusters determines the
	 * number of seeds that are uniformly distributed in the hypercube with corners at each
	 * dimension at 0.1 and 0.9 (the hypercube is specified by [0.1, 0.9]^dim).
	 * For each seed, a set of dim-dimensional normal distributed data objects is generated.
	 * The variance of the (dim-dimensional) normal distribution is randomly picked between 0 and 0.1.
	 * Finally, 10% of noise data objects is added to the data set.
	 * The noise data objects are generated uniformly on the unit-hypercube ([0, 1]^dim) 
	 * 
	 * @param dim The dimension of the data set.
	 * @param dataObjectCount The number of data objects.
	 * @param clusterCount The number of clusters.
	 * @param visibleDataObjects The number of data objects that are visible.
	 */
	public ClusterAlgorithmVisualSpeedTest(int dim, int dataObjectCount, int clusterCount, int visibleDataObjects)
	{
		super();
		

		long time;
		System.out.println("Building Data Set .. ");
		time = -System.currentTimeMillis();
		
		this.clusterCount = clusterCount;
		this.visibleDataObjects = visibleDataObjects;
		
		ArrayList<double[]> data = new ArrayList<double[]>();
		ArrayList<double[]> seeds = new ArrayList<double[]>();
		
		DataGenerator dg = new DataGenerator();		
		seeds.addAll(dg.uniformStandardPoints(dim, clusterCount));

		// seeds for clutsers
		for(double[] seed:seeds)
		{
			for(int k=0; k<seed.length; k++) seed[k] = 0.8*seed[k] + 0.1d;
		}
		
		// add clusters
		for(int i=0; i<this.clusterCount; i++)
		{
			data.addAll(dg.gaussPoints(seeds.get(i), 0.1*dg.generatorRand.nextDouble(), (9*dataObjectCount)/(10*this.clusterCount)));
		}
		
		// add noise
		data.addAll(dg.uniformStandardPoints(dim, dataObjectCount/10));
		
		// mix it
		Collections.shuffle(data);
		
		// store data set without the cluster information
		this.dataSet = new IndexedDataSet<double[]>(data.size());
		for(int j=0; j<data.size(); j++)
		{
			this.dataSet.add(new IndexedDataObject<double[]>(data.get(j)));
		}
		this.dataSet.seal();
				
		// initialPositions
		DoubleArrayPrototypeGenerator gen = new DoubleArrayPrototypeGenerator(new DAEuclideanVectorSpace(dim));
		ArrayList<Centroid<double[]>> initialPrototypes = gen.randomUniformOnDataBounds(this.dataSet, clusterCount);
		this.initialPositons = new ArrayList<double[]>();
		for(Centroid<double[]> c:initialPrototypes) this.initialPositons.add(c.getInitialPosition().clone());
		
		// select visualized data objects
		ArrayList<Integer> intList = new ArrayList<Integer>(dataObjectCount);
		for(int i=0; i<dataObjectCount; i++) intList.add(new Integer(i));
		Collections.shuffle(intList);
		this.indexesOfVisibleDataObjects = new int[this.visibleDataObjects];
		for(int i=0; i<this.visibleDataObjects; i++) this.indexesOfVisibleDataObjects[i] = intList.get(i).intValue();

		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
	}
	
	/**
	 * Shows the generated data set.
	 */
	public void showDataSet()
	{
		this.showDataSet(this.dataSet, null);
	}
		
	/**
	 * Performs a hard c-means clustering algorithm on the generated data set.
	 */
	public void testHardCMeans()
	{
		long time;
		System.out.println("HCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		HardCMeansClusteringAlgorithm<double[]> clusterAlgo = new HardCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "HCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs a fuzzy c-means clustering algorithm on the generated data set.
	 */
	public void testFuzzyCMeans()
	{
		long time;
		System.out.println("FCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		FuzzyCMeansClusteringAlgorithm<double[]> clusterAlgo = new FuzzyCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setEpsilon(0.0001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "FCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	
	
	/**
	 * Performs a fuzzy c-means (noise-) clustering algorithm on the generated data set.
	 */
	public void testFuzzyCMeansNoise()
	{
		long time;
		System.out.println("NFCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		FuzzyCMeansNoiseClusteringAlgorithm<double[]> clusterAlgo = new FuzzyCMeansNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setNoiseDistance(0.2d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "FCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a fuzzy c-means clustering algorithm with polynomial fuzzifier function on the generated data set.
	 */
	public void testPolynomialFuzzyCMeans()
	{
		long time;
		System.out.println("P-FCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		PolynomFCMClusteringAlgorithm<double[]> clusterAlgo = new PolynomFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setBeta(0.3d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "PFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a fuzzy c-means (noise-) clustering algorithm with polynomial fuzzifier function on the generated data set.
	 */
	public void testPolynomialFuzzyCMeansNoise()
	{
		long time;
		System.out.println("P-NFCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		PolynomFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new PolynomFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setBeta(0.3d);
		clusterAlgo.setNoiseDistance(0.3d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "PFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a rewarding crisp membership values fuzzy c-means clustering algorithm on the generated data set.
	 */
	public void testRewardingCrispFCM()
	{
		long time;
		System.out.println("RC-FCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		RewardingCrispFCMClusteringAlgorithm<double[]> clusterAlgo = new RewardingCrispFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceMultiplierConstant(0.7d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "PFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a rewarding crisp membership values fuzzy c-means (noise-) clustering algorithm on the generated data set.
	 */
	public void testRewardingCrispFCMNoise()
	{
		long time;
		System.out.println("RC-NFCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		RewardingCrispFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new RewardingCrispFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceMultiplierConstant(0.7d);
		clusterAlgo.setNoiseDistance(0.3d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "PFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs a voronoi cell opimized fuzzy c-means clustering algorithm on the generated data set.
	 */
	public void testVoronoiPartitionFCM()
	{
		long time;
		System.out.println("VP-FCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		VoronoiPartitionFCMClusteringAlgorithm<double[]> clusterAlgo = new VoronoiPartitionFCMClusteringAlgorithm<double[]>(this.dataSet, evs, evs, evs);
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "VPFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs a voronoi cell opimized fuzzy c-means (noise-) clustering algorithm on the generated data set.
	 */
	public void testVoronoiPartitionFCMNoise()
	{
		long time;
		System.out.println("VP-NFCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		VoronoiPartitionFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new VoronoiPartitionFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, evs, evs, evs);
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setNoiseDistance(0.2d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "VPFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs distance adapted fuzzy c-means clustering algorithm on the generated data set.
	 */
	public void testDistAdaptedFCM()
	{
		long time;
		System.out.println("DA-FCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		DistAdaptedFCMClusteringAlgorithm<double[]> clusterAlgo = new DistAdaptedFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceCorrectionParameter(1.0d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "DAFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs a  distance adapted fuzzy c-means (noise-) clustering algorithm on the generated data set.
	 */
	public void testDistAdaptedFCMNoise()
	{
		long time;
		System.out.println("DA-NFCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		DistAdaptedFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new DistAdaptedFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceCorrectionParameter(1.0d);
		clusterAlgo.setNoiseDistance(0.2d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "DAFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a fuzzy c-means clustering algorithm, opimised by using a centered ball tree on the generated data set.
	 */
	public void testBallTreeFuzzyCMeans()
	{
		long time;
		System.out.println("BT-FCM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		BallTreeFuzzyCMeansClusteringAlgorithm<double[]> clusterAlgo = new BallTreeFuzzyCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setMaximalMembershipIntervalLength(0.2d);
		clusterAlgo.setEpsilon(0.001d);

		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "BTFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs an expectation maximisation clustering algorithm  with a mixture of spherical gaussians model on the generated data set.
	 */
	public void testExpectationMaximization()
	{
		long time;
		System.out.println("EM-GMM");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		ExpectationMaximizationSGMMClusteringAlgorithm clusterAlgo = new ExpectationMaximizationSGMMClusteringAlgorithm(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setVarianceBounded(true);
		clusterAlgo.setVarianceLowerBound(0.0001d);
		clusterAlgo.setVarianceUpperBound(1000.0d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
//		for(SphericalNormalDistributionPrototype d:clusterAlgo.getActivePrototypes()) System.out.println(d.getClusterIndex() + ": " + d.getVariance());
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "EMGMM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs the DBScan clustering algorithm on the generated data set.
	 */
	public void testDBScan()
	{
		long time;
		System.out.println("DBScan");
		System.out.print("Initialize clustering algorithm .. ");
		time = -System.currentTimeMillis();
		
		DBScan<double[]> clusterAlgo = new DBScan<double[]>(this.dataSet, new DAEuclideanMetric());
		clusterAlgo.setCoreNum(50);
		clusterAlgo.setCoreDist(0.02d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply();
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo, this.indexesOfVisibleDataObjects, clusterAlgo.algorithmName(), "DBScan_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
}
