/**
Copyright (c) 2012, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
	roland.winkler@gmail.com
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * The name of Roland Winkler may not be used to endorse or promote products
		derived from this software without specific prior written permission.

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import data.objects.doubleArray.DAEuclideanMetric;
import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.objects.doubleArray.DAMaximumNorm;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import dataMiningTestTrack.experiments.snFCM.DAPositionListParameterBound;
import dataMiningTestTrack.experiments.snFCM.ScaledNormFuzzyCMeansObjectiveFunction;
import datamining.clustering.ClusterAnalyser;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.altopt.RewardingCrispFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.RewardingCrispFCMNoiseClusteringAlgorithm;
import datamining.clustering.protoype.initial.DoubleArrayPrototypeGenerator;
import datamining.gradient.centroid.PrototypeGradientOptimizationAlgorithm;
import datamining.gradient.centroid.SingleCentroidGradientOptimizationAlgorithm;
import datamining.gradient.functions.DALeastSquaresObjectiveFunction;
import datamining.gradient.functions.RelativeVarianceOfDistancesObjectiveFunction;
import datamining.gradient.functions.clustering.FuzzyCMeansObjectiveFunction;
import datamining.gradient.parameter.PositionListParameter;
import datamining.gradient.parameter.PositionListParameterMetric;
import datamining.gradient.parameter.PositionListParameterVectorSpace;
import etc.DataGenerator;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class GradientAlgorithmVisualTest extends TestVisualizer
{
	/**
	 * The data set to be clustered
	 */
	private IndexedDataSet<double[]> dataSet;

	/**
	 * The initial positions of prototypes.
	 */
	private ArrayList<double[]> seeds;
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

	public GradientAlgorithmVisualTest(int dim, int dataObjectCount, int clusterCount, double noiseFraction)
	{
		super();
		
		this.clusterCount = clusterCount;
		
		ArrayList<double[]> tmpData;
		ArrayList<double[]> data = new ArrayList<double[]>();
		this.seeds = new ArrayList<double[]>();

		double[] min = new double[dim];
		double[] max = new double[dim];
		
//		DAHypercupeBound bounds;

		for(int k=0; k<dim; k++)
		{
			min[k] =  Double.MAX_VALUE;
			max[k] = -Double.MAX_VALUE;
		}
		
		
		DataGenerator dg = new DataGenerator();		
		this.seeds.addAll(dg.uniformStandardPoints(dim+1, clusterCount));

		// seeds for clutsers
		for(double[] seed:this.seeds)
		{
			for(int k=0; k<seed.length; k++) seed[k] = 0.8*seed[k] + 0.1d;
		}
		
		// add clusters
		for(int i=0; i<this.clusterCount; i++)
		{
			tmpData = dg.gaussPoints(this.seeds.get(i), 0.03*dg.generatorRand.nextDouble()+0.01, (int)((1.0d-noiseFraction)*dataObjectCount/this.clusterCount));

			// indicate the correct cluster
			for(int j=0; j<tmpData.size(); j++)
			{
				tmpData.get(j)[dim] = i;
			}
			
			data.addAll(tmpData);
		}
		

		
		// scale data to [0, 1]^dim
		for(double[] x:data)
		{
			for(int k=0; k<dim; k++)
			{
				min[k] = (min[k] < x[k])? min[k] : x[k];
				max[k] = (max[k] > x[k])? max[k] : x[k];
			}
		}
		
		for(double[] x:data)
		{
			for(int k=0; k<dim; k++)
			{
				x[k] -= min[k];
				x[k] /= max[k] - min[k];
			}
		}
		for(double[] x:this.seeds)
		{
			for(int k=0; k<dim; k++)
			{
				x[k] -= min[k];
				x[k] /= max[k] - min[k];
			}
		}
		
		// add noise
		tmpData = dg.uniformStandardPoints(dim+1, (int)(dataObjectCount*noiseFraction));

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
	
	
	public GradientAlgorithmVisualTest(int dim, int dataObjectCount, int clusterCount)
	{
		super();
		
		this.clusterCount = clusterCount;

		ArrayList<double[]> noise = new ArrayList<double[]>();
		ArrayList<double[]> data = new ArrayList<double[]>();
		ArrayList<double[]> seeds = new ArrayList<double[]>();

		double[] min = new double[dim];
		double[] max = new double[dim];
		
//		DAHypercupeBound bounds;

		for(int k=0; k<dim; k++)
		{
			min[k] =  Double.MAX_VALUE;
			max[k] = -Double.MAX_VALUE;
		}
		
		DataGenerator dg = new DataGenerator();		
		seeds.addAll(dg.uniformStandardPoints(dim, clusterCount));

		// seeds for clutsers
//		for(double[] seed:seeds)
//		{
//			for(int k=0; k<seed.length; k++) seed[k] *= 2.0d;
//		}
		
		// add clusters
		for(int i=0; i<this.clusterCount; i++)
		{
			data.addAll(dg.gaussPoints(seeds.get(i), 0.1*dg.generatorRand.nextDouble()+0.0d, (9*dataObjectCount)/(10*this.clusterCount)));
		}

				
		// scale data to [0, 1]^dim
		for(double[] x:data)
		{
			for(int k=0; k<dim; k++)
			{
				min[k] = (min[k] < x[k])? min[k] : x[k];
				max[k] = (max[k] > x[k])? max[k] : x[k];
			}
		}
		
		for(double[] x:data)
		{
			for(int k=0; k<dim; k++)
			{
				x[k] -= min[k];
				x[k] /= max[k] - min[k];
			}
		}

		// add noise in [0, 1]^dim
		noise.addAll(dg.uniformStandardPoints(dim, dataObjectCount/10));
		data.addAll(noise);
		
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
	}

	/**
	 * Shows the generated data set.
	 */
	public void showDataSet(boolean clustered)
	{
		if(clustered)
		{
			this.showCrispDataSetClustering(dataSet, clusterCount, correctClustering, "Clustered Dataset"  + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
		}
		else this.showDataSet(this.dataSet, "Dataset"  + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	
	
	public void leastSquaresTest()
	{
		DALeastSquaresObjectiveFunction lsqOF = new DALeastSquaresObjectiveFunction(this.dataSet);
		SingleCentroidGradientOptimizationAlgorithm<double[]> gradientoptimization = new SingleCentroidGradientOptimizationAlgorithm<double[]>(this.dataSet, lsqOF.getVs(), lsqOF.getVs(), lsqOF, null);
		gradientoptimization.setAscOrDesc(false);
		gradientoptimization.initializeWithParameter(this.initialPositons.get(0));
		gradientoptimization.setLearningFactor(0.25d);
		gradientoptimization.apply(50);
		
		this.showSingleCentroidGradientAlgorithm(gradientoptimization, gradientoptimization.algorithmName() + " - " + lsqOF.getName(), "LeastSquaresTest");
	}

	
	public void fcmGradientTest()
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		PositionListParameterVectorSpace<double[]> parameterVS = new PositionListParameterVectorSpace<>(evs, this.clusterCount);
		DAMaximumNorm maximumNorm = new DAMaximumNorm();
		PositionListParameterMetric<double[]> parameterMetric = new PositionListParameterMetric<double[]>(evs, maximumNorm, this.clusterCount);
		
		FuzzyCMeansObjectiveFunction<double[]> fcmFunction = new FuzzyCMeansObjectiveFunction<double[]>(this.dataSet, evs, evs);
		ArrayList<Centroid<double[]>> centroids = new ArrayList<Centroid<double[]>>(this.clusterCount);
		for(int i=0; i<this.clusterCount; i++) centroids.add(new Centroid<double[]>(evs, this.initialPositons.get(i)));
		
		PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>> algo = new PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>>(this.dataSet, parameterVS, parameterMetric, fcmFunction, centroids, null);
		PositionListParameter<double[]> parameter = new PositionListParameter<double[]>(this.initialPositons);
		
		algo.setAscOrDesc(false);
		algo.initializeWithParameter(parameter);
		algo.setLearningFactor(1.0d);
		algo.apply(50);
		
		this.showDataMiningAlgorithm(algo, fcmFunction, algo.algorithmName(), "FCMGradientTest");
	}

	public void snfcmGradientTest()
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		PositionListParameterVectorSpace<double[]>  parameterVS = new PositionListParameterVectorSpace<double[]> (evs, this.clusterCount);
		double[] manualLowerBounds = new double[evs.getDimension()];
		double[] manualUpperBounds = new double[evs.getDimension()];
		for(int i=0; i<evs.getDimension(); i++)
		{
			manualLowerBounds[i] = 0.1d;
			manualUpperBounds[i] = 0.9d;
		}
		DAPositionListParameterBound parameterBound = new DAPositionListParameterBound(manualLowerBounds, manualUpperBounds);
		PositionListParameterMetric<double[]> parameterMetric = new PositionListParameterMetric<double[]>(evs, new DAMaximumNorm(), this.clusterCount);
		
		ScaledNormFuzzyCMeansObjectiveFunction snfcmFunction = new ScaledNormFuzzyCMeansObjectiveFunction(this.dataSet, 2.0d);
		snfcmFunction.setFuzzifier(1.5d);
		ArrayList<Centroid<double[]>> centroids = new ArrayList<Centroid<double[]>>(this.clusterCount);		
		for(int i=0; i<this.clusterCount; i++) centroids.add(new Centroid<double[]>(evs, this.initialPositons.get(i)));
		
		PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>> algo = new PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>>(this.dataSet, parameterVS, parameterMetric, snfcmFunction, centroids, parameterBound);
		PositionListParameter<double[]> parameter = new PositionListParameter<double[]>(this.initialPositons);
		
		algo.setAscOrDesc(false);
		algo.initializeWithParameter(parameter);
		algo.setLearningFactor(1.0d);
		algo.apply(50);
		
		this.showDataMiningAlgorithm(algo, snfcmFunction, algo.algorithmName(), "SNFCMGradientTest_"+evs.getDimension()+"Dim_w"+snfcmFunction.getFuzzifier());
	}

	public void snfcmGradientTest(double fuzzifier, double learningFactor)
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		PositionListParameterVectorSpace<double[]>  parameterVS = new PositionListParameterVectorSpace<double[]> (evs, this.clusterCount);
		double[] manualLowerBounds = new double[evs.getDimension()];
		double[] manualUpperBounds = new double[evs.getDimension()];
		for(int i=0; i<evs.getDimension(); i++)
		{
			manualLowerBounds[i] = 0.1d;
			manualUpperBounds[i] = 0.9d;
		}
		DAPositionListParameterBound parameterBound = new DAPositionListParameterBound(manualLowerBounds, manualUpperBounds);
		PositionListParameterMetric<double[]> parameterMetric = new PositionListParameterMetric<double[]>(evs, new DAMaximumNorm(), this.clusterCount);
		
		ScaledNormFuzzyCMeansObjectiveFunction snfcmFunction = new ScaledNormFuzzyCMeansObjectiveFunction(this.dataSet, 2.0d);
		snfcmFunction.setFuzzifier(fuzzifier);
		ArrayList<Centroid<double[]>> centroids = new ArrayList<Centroid<double[]>>(this.clusterCount);		
		for(int i=0; i<this.clusterCount; i++) centroids.add(new Centroid<double[]>(evs, this.initialPositons.get(i)));
		
		PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>> algo = new PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>>(this.dataSet, parameterVS, parameterMetric, snfcmFunction, centroids, parameterBound);
		PositionListParameter<double[]> parameter = new PositionListParameter<double[]>(this.initialPositons);
		
		algo.setAscOrDesc(false);
		algo.initializeWithParameter(parameter);
		algo.setLearningFactor(learningFactor);
		algo.apply(50);
		
		this.showDataMiningAlgorithm(algo, snfcmFunction, algo.algorithmName(), "SNFCMGradientTest_"+evs.getDimension()+"Dim_w"+fuzzifier+"_LF"+learningFactor);
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
		clusterAlgo.setDistanceMultiplierConstant(0.9d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showDataMiningAlgorithm(clusterAlgo, clusterAlgo, clusterAlgo.algorithmName(), "PFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
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
		clusterAlgo.setDistanceMultiplierConstant(0.9d);
		clusterAlgo.setNoiseDistance(1.0d);
		clusterAlgo.setEpsilon(0.001d);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		time = -System.currentTimeMillis();
		System.out.print("Clustering process .. ");
		
		clusterAlgo.apply(50);
		
		time += System.currentTimeMillis();
		System.out.println("done. ("+time+" ms)");
		
		this.showDataMiningAlgorithm(clusterAlgo, clusterAlgo, clusterAlgo.algorithmName(), "PFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}

	public void relativeVarianceGradientTest()
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		
		RelativeVarianceOfDistancesObjectiveFunction relVarFunction = new RelativeVarianceOfDistancesObjectiveFunction(this.dataSet);
		SingleCentroidGradientOptimizationAlgorithm<double[]> algo = new SingleCentroidGradientOptimizationAlgorithm<double[]>(this.dataSet, evs, evs, relVarFunction, null); 
		
		
		algo.setAscOrDesc(true);
		algo.initializeWithParameter(this.seeds.get(0));
		algo.setLearningFactor(0.1d);
		algo.apply(50);
		
		this.showSingleCentroidGradientAlgorithm(algo, algo.algorithmName(), "RelVarGradientTest");
	}
//
//	
//	public void snfcmGradientTest()
//	{
//		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
//		PositionListParameterVectorSpace<double[]> parameterVS = new PositionListParameterVectorSpace<double[]>(evs, this.clusterCount);
//		DAMaximumNorm maximumNorm = new DAMaximumNorm();
//		PositionListParameterMetric<double[]> parameterMetric = new PositionListParameterMetric<double[]>(evs, maximumNorm, this.clusterCount);
//		
//		ScaledNormFuzzyCMeansObjectiveFunction snfcmFunction = new ScaledNormFuzzyCMeansObjectiveFunction(this.dataSet, 2.0d);
//		snfcmFunction.setFuzzifier(2.0d);
//		ArrayList<Centroid<double[]>> centroids = new ArrayList<Centroid<double[]>>(this.clusterCount);
//		
//		for(int i=0; i<this.clusterCount; i++) centroids.add(new Centroid<double[]>(evs, this.initialPositons.get(i)));
//		
//		PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>> algo = new PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>>(this.dataSet, parameterVS, parameterMetric, snfcmFunction, centroids);
//		PositionListParameter<double[]> parameter = new PositionListParameter<double[]>(this.initialPositons);
//		
//		algo.setAscOrDesc(false);
//		algo.initializeWithParameter(parameter);
//		algo.setLearningFactor(2.0d);
//		algo.apply(50);
//		
//		this.showDataMiningAlgorithm(algo, snfcmFunction, algo.algorithmName(), "SNFCMGradientTest");
//	}
}
