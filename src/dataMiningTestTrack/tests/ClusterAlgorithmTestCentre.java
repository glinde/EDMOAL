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
import datamining.validation.ClusterMaxRecallIndex;
import datamining.validation.ClusteringInformation;
import etc.DataGenerator;
import generation.data.ClusteredDataSetGenerator;
import generation.data.HyperrectangleUniformGenerator;


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
public class ClusterAlgorithmTestCentre extends TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -4791280640176444354L;

	/**
	 * The data set to be clustered
	 */
	private IndexedDataSet<double[]> dataSet;
	
	/**  */
	private int dim;
	/**
	 * The number of clusters
	 */
	private int clusterCount;
			
	/**
	 * The correct clustering (partitioning) according to the data generation process.
	 */
	private int[] correctClustering;
	
	/**  */
	private ArrayList<double[]> lastFuzzyClusteringResult;

	/**  */
	private int[] lastCrispClusteringResult;
	
	/**
	 * The initial positions of prototypes.
	 */
	private ArrayList<double[]> initialPositons;
	
	
	public ClusterAlgorithmTestCentre(int dim, int clusterCount)
	{
		this.dim = dim;
		this.clusterCount = clusterCount;
		this.dataSet = new IndexedDataSet<double[]>();
		this.correctClustering = new int[0];
		this.initialPositons = new ArrayList<double[]>();
		this.lastFuzzyClusteringResult = new ArrayList<double[]>();
		this.lastCrispClusteringResult = new int[0];
	}

	/**
	 * Shows the generated data set.
	 */
	public void showDataSet(int x, int y)
	{
		this.xIndex = x;
		this.yIndex = y;
		this.showDataSet(this.dataSet, null);
	}

	/**
	 * Shows the generated data set.
	 */
	public void showDataSetClustered(int x, int y, int[] clustering, String filename)
	{
		this.xIndex = x;
		this.yIndex = y;
		this.showCrispDataSetClustering(this.dataSet, this.clusterCount, clustering, filename);
	}
	
	/**
	 * Shows the generated data set.
	 */
	public void showDataSetClustered(int x, int y, Collection<double[]> clustering, String filename)
	{
		this.xIndex = x;
		this.yIndex = y;
		this.showFuzzyDataSetClustering(this.dataSet, clustering, filename);
	}
	
	public void showDataSetClustered2DProjections(double addA, double addB, String filename)
	{
		if(filename == null) filename = "Clustered Data Set";
		
		for(int i=0; i<this.dim; i+=Math.max(addA, 1.0d))
		{
			for(int j=i+1; j<this.dim; j+=Math.max(addB, 1.0d))
			{
				this.showDataSetClustered(i, j, this.correctClustering, filename + "_proj_" + i + "-" + j);
			}
		}
	}

	public void showCrispClusteringResult2DProjections(double addA, double addB, String filename)
	{
		if(filename == null) filename = "Crisp Clustering Result";
		
		for(int i=0; i<this.dim; i+=Math.max(addA, 1.0d))
		{
			for(int j=i+1; j<this.dim; j+=Math.max(addB, 1.0d))
			{
				this.showDataSetClustered(i, j, this.lastCrispClusteringResult, filename + "_proj_" + i + "-" + j);
			}
		}
	}

	public void showFuzzyClusteringResult2DProjections(double addA, double addB, String filename)
	{
		if(filename == null) filename = "Fuzzy Clustering Result";
		
		for(int i=0; i<this.dim; i+=Math.max(addA, 1.0d))
		{
			for(int j=i+1; j<this.dim; j+=Math.max(addB, 1.0d))
			{
				this.showDataSetClustered(i, j, this.lastFuzzyClusteringResult, filename + "_proj_" + i + "-" + j);
			}
		}
	}
	
	public void generateDistortedData(int dataPerClusterCount, int noise, boolean scale, int shuffleLocation)
	{
		ClusteredDataSetGenerator clusterGen = new ClusteredDataSetGenerator(this.dim);
		
		clusterGen.generateClusteredDataSet(dataPerClusterCount, this.clusterCount, noise, scale, shuffleLocation);
		
		this.dataSet = new IndexedDataSet<double[]>(clusterGen.getData().size());
		for(double[] x:clusterGen.getData()) this.dataSet.add(new IndexedDataObject<double[]>(x));
		
		this.dataSet.seal();
		
		this.correctClustering = clusterGen.getClusterIndices();
	}
	
	public void generateInitialPositionsRandomUniform()
	{
		this.initialPositons = new ArrayList<double[]>();
				
		HyperrectangleUniformGenerator gen = new HyperrectangleUniformGenerator(this.dim);
		
		this.initialPositons.addAll(gen.generateDataObjects(this.clusterCount));
	}

	/**
	 * Performs a hard c-means clustering algorithm on the generated data set.
	 */
	public void testHardCMeans()
	{
		HardCMeansClusteringAlgorithm<double[]> clusterAlgo = new HardCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().x.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.apply(50);
		
		this.lastCrispClusteringResult = clusterAlgo.getAllCrispClusterAssignments();
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastFuzzyClusteringResult.clear();
		clusterAlgo.getAllFuzzyClusterAssignments(this.lastFuzzyClusteringResult);
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
		
		this.lastCrispClusteringResult = clusterAlgo.getAllCrispClusterAssignments();
	}
	
	public void validateLastFuzzyClusteringResult()
	{
		ClusteringInformation<double[]> info = new ClusteringInformation<double[]>(this.clusterCount);
		
		info.setDataSet(this.dataSet);
		info.setFuzzyClusteringResult(this.lastFuzzyClusteringResult);
		info.setTrueClusteringResult(this.correctClustering);
		
		ClusterMaxRecallIndex<double[]> maxRecall = new ClusterMaxRecallIndex<double[]>(info);
		
		double index = maxRecall.index();
		
		System.out.println("Max Recall Index: " + index);
	}
}
