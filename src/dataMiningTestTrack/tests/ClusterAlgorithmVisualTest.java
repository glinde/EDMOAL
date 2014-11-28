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
import datamining.resultProviders.DummyCrispClusteringAlgorithm;
import etc.DataGenerator;


/**
 * This class provides some functionality to test cluster algorithms and to verify the result visually.
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
public class ClusterAlgorithmVisualTest extends TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -4791280640176444354L;

	/**
	 * The data set to be clustered
	 */
	private IndexedDataSet<double[]> dataSet;
	
	/**
	 * The number of clusters
	 */
	private int clusterCount;
	
	/**
	 * The correct clustering (partitioning) according to the data generation process.
	 */
	private int[] correctClustering;
	
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
	 * number of seeds that are uniformly distributed in the hypercube with cornes at each
	 * dimension at 0.1 and 0.9 (the hypercube is specified by [0.1, 0.9]^dim).
	 * For each seed, a set of dim-dimensional normal distributed data objects is generated.
	 * The variance of the (dim-dimensional) normal distribution is randomly picked between 0 and 0.1.
	 * Finally, 10% of noise data objects is added to the data set.
	 * The noise data objects are generated uniformly on the unit-hypercube ([0, 1]^dim) 
	 * 
	 * @param dim The dimension of the data set.
	 * @param dataObjectCount The number of data objects.
	 * @param clusterCount The number of clusters.
	 */
	public ClusterAlgorithmVisualTest(int dim, int dataObjectCount, int clusterCount)
	{
		super();
		
		this.clusterCount = clusterCount;
		
		ArrayList<double[]> tmpData;
		ArrayList<double[]> data = new ArrayList<double[]>();
		ArrayList<double[]> seeds = new ArrayList<double[]>();
		
		DataGenerator dg = new DataGenerator();		
		seeds.addAll(dg.uniformStandardPoints(dim+1, clusterCount));

		// seeds for clutsers
		for(double[] seed:seeds)
		{
			for(int k=0; k<seed.length; k++) seed[k] = 0.8*seed[k] + 0.1d;
		}
		
		// add clusters
		for(int i=0; i<this.clusterCount; i++)
		{
			tmpData = dg.gaussPoints(seeds.get(i), 0.1*dg.generatorRand.nextDouble(), (9*dataObjectCount)/(10*this.clusterCount));

			// indicate the correct cluster
			for(int j=0; j<tmpData.size(); j++)
			{
				tmpData.get(j)[dim] = i;
			}
			
			data.addAll(tmpData);
		}
		
		// add noise
		tmpData = dg.uniformStandardPoints(dim+1, dataObjectCount/10);

		// indicate the noise cluster
		for(int j=0; j<tmpData.size(); j++)
		{
			tmpData.get(j)[dim] = -1.0d;
		}
		data.addAll(tmpData);
		
		// mix it
		Collections.shuffle(data);
		
		// store the correct cluster indices with shuffled order
		this.correctClustering = new int[data.size()];
		for(int j=0; j<data.size(); j++)
		{
			this.correctClustering[j] = (int)data.get(j)[dim];
		}
		
		// store data set without the cluster information
		this.dataSet = new IndexedDataSet<double[]>(data.size());
		for(int j=0; j<data.size(); j++)
		{
			this.dataSet.add(new IndexedDataObject<double[]>(Arrays.copyOfRange(data.get(j), 0, dim)));
		}
		this.dataSet.seal();
				
		// initialPositions
		DoubleArrayPrototypeGenerator gen = new DoubleArrayPrototypeGenerator(new DAEuclideanVectorSpace(dim));
		ArrayList<Centroid<double[]>> initialPrototypes = gen.randomUniformOnDataBounds(this.dataSet, clusterCount);
		this.initialPositons = new ArrayList<double[]>();
		for(Centroid<double[]> c:initialPrototypes) this.initialPositons.add(c.getInitialPosition().clone());
	}
	
	/**
	 * Shows the generated data set.
	 */
	public void showDataSet()
	{
		this.showDataSet(this.dataSet, null, null, null, null, null, null);
	}
	
	/**
	 * Shows the generated data set, coloured with the clusters, specified by the generation.
	 */
	public void showClusteredDataSet()
	{
		DummyCrispClusteringAlgorithm<double[]> dummy = new DummyCrispClusteringAlgorithm<double[]>(this.dataSet, this.correctClustering, this.clusterCount);
		this.showDataSet(this.dataSet, null, dummy, null, null, null, null);
	}
	
	/**
	 * Performs a hard c-means clustering algorithm on the generated data set.
	 */
	public void testHardCMeans()
	{
		HardCMeansClusteringAlgorithm<double[]> clusterAlgo = new HardCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "HCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs a fuzzy c-means clustering algorithm on the generated data set.
	 */
	public void testFuzzyCMeans(double fuzzifier)
	{
		FuzzyCMeansClusteringAlgorithm<double[]> clusterAlgo = new FuzzyCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(fuzzifier);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "FCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	
	
	/**
	 * Performs a fuzzy c-means (noise-) clustering algorithm on the generated data set.
	 */
	public void testFuzzyCMeansNoise(double fuzzifier, double noiseDist)
	{
		FuzzyCMeansNoiseClusteringAlgorithm<double[]> clusterAlgo = new FuzzyCMeansNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(fuzzifier);
		clusterAlgo.setNoiseDistance(noiseDist);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "FCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a fuzzy c-means clustering algorithm with polynomial fuzzifier function on the generated data set.
	 */
	public void testPolynomialFuzzyCMeans(double beta)
	{
		PolynomFCMClusteringAlgorithm<double[]> clusterAlgo = new PolynomFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setBeta(beta);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "PFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a fuzzy c-means (noise-) clustering algorithm with polynomial fuzzifier function on the generated data set.
	 */
	public void testPolynomialFuzzyCMeansNoise(double beta, double noiseDist)
	{
		PolynomFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new PolynomFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setBeta(beta);
		clusterAlgo.setNoiseDistance(noiseDist);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "PFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a rewarding crisp membership values fuzzy c-means clustering algorithm on the generated data set.
	 */
	public void testRewardingCrispFCM(double distMul)
	{
		RewardingCrispFCMClusteringAlgorithm<double[]> clusterAlgo = new RewardingCrispFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceMultiplierConstant(distMul);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "PFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a rewarding crisp membership values fuzzy c-means (noise-) clustering algorithm on the generated data set.
	 */
	public void testRewardingCrispFCMNoise(double distMul, double noiseDist)
	{
		RewardingCrispFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new RewardingCrispFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceMultiplierConstant(distMul);
		clusterAlgo.setNoiseDistance(noiseDist);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "PFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs a voronoi cell optimized fuzzy c-means clustering algorithm on the generated data set.
	 */
	public void testVoronoiPartitionFCM()
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		VoronoiPartitionFCMClusteringAlgorithm<double[]> clusterAlgo = new VoronoiPartitionFCMClusteringAlgorithm<double[]>(this.dataSet, evs, evs, evs);
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "VPFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs a voronoi cell optimized fuzzy c-means (noise-) clustering algorithm on the generated data set.
	 */
	public void testVoronoiPartitionFCMNoise(double noiseDist)
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		VoronoiPartitionFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new VoronoiPartitionFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, evs, evs, evs);
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setNoiseDistance(noiseDist);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "VPFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs distance adapted fuzzy c-means clustering algorithm on the generated data set.
	 */
	public void testDistAdaptedFCM(double fuzzifier, double distCorr)
	{
		DistAdaptedFCMClusteringAlgorithm<double[]> clusterAlgo = new DistAdaptedFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(fuzzifier);
		clusterAlgo.setDistanceCorrectionParameter(distCorr);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "DAFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs a  distance adapted fuzzy c-means (noise-) clustering algorithm on the generated data set.
	 */
	public void testDistAdaptedFCMNoise(double fuzzifier, double distCorr, double noiseDist)
	{
		DistAdaptedFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new DistAdaptedFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(fuzzifier);
		clusterAlgo.setDistanceCorrectionParameter(distCorr);
		clusterAlgo.setNoiseDistance(noiseDist);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "DAFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs a fuzzy c-means clustering algorithm, optimised by using a centered ball tree on the generated data set.
	 */
	public void testBallTreeFuzzyCMeans(double fuzzifier, double membershipIntervalLength)
	{
		BallTreeFuzzyCMeansClusteringAlgorithm<double[]> clusterAlgo = new BallTreeFuzzyCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(fuzzifier);
		clusterAlgo.setMaximalMembershipIntervalLength(membershipIntervalLength);
		clusterAlgo.setEpsilon(0.01d);
		clusterAlgo.apply(50);
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "BTFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * Performs an expectation maximisation clustering algorithm  with a mixture of spherical gaussians model on the generated data set.
	 */
	public void testExpectationMaximization()
	{
		ExpectationMaximizationSGMMClusteringAlgorithm clusterAlgo = new ExpectationMaximizationSGMMClusteringAlgorithm(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setVarianceBounded(true);
		clusterAlgo.setVarianceLowerBound(0.0001d);
		clusterAlgo.setVarianceUpperBound(1000.0d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
//		for(SphericalNormalDistributionPrototype d:clusterAlgo.getActivePrototypes()) System.out.println(d.getClusterIndex() + ": " + d.getVariance());
		this.showDataSet(clusterAlgo.getDataSet(), clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "EMGMM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * Performs the DBScan clustering algorithm on the generated data set.
	 */
	public void testDBScan(int coreNum, double coreDist)
	{
		DBScan<double[]> clusterAlgo = new DBScan<double[]>(this.dataSet, new DAEuclideanMetric());
		clusterAlgo.setCoreNum(coreNum);
		clusterAlgo.setCoreDist(coreDist);
		clusterAlgo.apply();
		this.showDataSet(clusterAlgo.getDataSet(), null, clusterAlgo, null, null, clusterAlgo.algorithmName(), "DBScan_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
}
