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
import data.objects.matrix.FeatureSpaceSampling2D;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import dataMiningTestTrack.experiments.snFCM.DAPositionListParameterBound;
import dataMiningTestTrack.experiments.snFCM.ScaledNormFuzzyCMeansObjectiveFunction;
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
import datamining.resultProviders.DummyCrispClusteringAlgorithm;
import etc.DataGenerator;
import etc.SimpleStatistics;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class SymmetricalDataAlgorithmVisualTest extends TestVisualizer
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

	/**
	 * Creats an empty test class
	 */
	public SymmetricalDataAlgorithmVisualTest()
	{
		super();
		this.dataSet = null;
		this.seeds = null;
		this.clusterCount = 0;
		this.correctClustering = null;
		this.initialPositons = null;
	}
	
	public void gen2DCircleClusterPoints(int clusterCount, double variance, int dataObjectsPerCluster, int noisePoints)
	{
		this.clusterCount = clusterCount;
		int dim = 2;
		
		ArrayList<double[]> tmpData;
		ArrayList<double[]> data = new ArrayList<double[]>();
		this.seeds = new ArrayList<double[]>();
		double[] tmp;

		double[] min = new double[dim+1];
		double[] max = new double[dim+1];
		
//		DAHypercupeBound bounds;

		for(int k=0; k<dim; k++)
		{
			min[k] =  Double.MAX_VALUE;
			max[k] = -Double.MAX_VALUE;
		}
		
		
		DataGenerator dg = new DataGenerator();		

		// seeds for clutsers
		for(int i=0; i<clusterCount; i++)
		{
			tmp = new double[dim+1];
			
			tmp[0] = Math.cos(2.0d*Math.PI*(double)i/((double)clusterCount));
			tmp[1] = Math.sin(2.0d*Math.PI*(double)i/((double)clusterCount));
			tmp[dim] = i;
			this.seeds.add(tmp);
		}
		
		// add clusters
		for(int i=0; i<this.clusterCount; i++)
		{
			tmpData = dg.gaussPoints(this.seeds.get(i), variance, dataObjectsPerCluster);

			// indicate the correct cluster
			for(int j=0; j<tmpData.size(); j++)
			{
				tmpData.get(j)[dim] = this.seeds.get(i)[dim];
			}
			
			data.addAll(tmpData);
		}
		

		
		for(double[] x:data)
		{
			for(int k=0; k<=dim; k++)
			{
				min[k] = (min[k] < x[k])? min[k] : x[k];
				max[k] = (max[k] > x[k])? max[k] : x[k];
			}
		}
				
		// add noise
		tmpData = dg.uniformPoints(min, max, noisePoints);

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
	public void showDataSet(boolean clustered)
	{
		if(clustered)
		{
			DummyCrispClusteringAlgorithm<double[]> dummy = new DummyCrispClusteringAlgorithm<double[]>(dataSet, correctClustering, clusterCount);
			this.showDataSet(dataSet, null, dummy, null, null, "Clustered Dataset", "Clustered Dataset"  + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
		}
		else this.showDataSet(this.dataSet, null, null, null, null, "Dataset", "Dataset"  + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}
	
	
	public void leastSquaresTest()
	{
		DALeastSquaresObjectiveFunction lsqOF = new DALeastSquaresObjectiveFunction(this.dataSet);
		SingleCentroidGradientOptimizationAlgorithm<double[]> gradientoptimization = new SingleCentroidGradientOptimizationAlgorithm<double[]>(this.dataSet, lsqOF.getVs(), lsqOF.getVs(), lsqOF, null);
		gradientoptimization.setAscOrDesc(false);
		gradientoptimization.initializeWithParameter(this.initialPositons.get(0));
		gradientoptimization.setLearningFactor(0.25d);
		gradientoptimization.apply(50);
		
		this.showDataSet(this.dataSet, gradientoptimization, null, null, null, gradientoptimization.algorithmName() + " - " + lsqOF.getName(), "LeastSquaresTest");
	}

	
	public void fcmGradientTest()
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		PositionListParameterVectorSpace<double[]> parameterVS = new PositionListParameterVectorSpace<double[]>(evs, this.clusterCount);
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

		this.showDataSet(this.dataSet, algo, fcmFunction, null, null, algo.algorithmName(), "FCMGradientTest");
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
		
		this.showDataSet(this.dataSet, algo, snfcmFunction, null, null, algo.algorithmName(), "SNFCMGradientTest_"+evs.getDimension()+"Dim_w"+snfcmFunction.getFuzzifier());
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
		
		this.showDataSet(this.dataSet, algo, snfcmFunction, null, null, algo.algorithmName(), "SNFCMGradientTest_"+evs.getDimension()+"Dim_w"+fuzzifier+"_LF"+learningFactor);
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
		
		this.showDataSet(this.dataSet, clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "PFCM_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
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
		
		this.showDataSet(this.dataSet, clusterAlgo, clusterAlgo, null, null, clusterAlgo.algorithmName(), "PFCMN_" + this.dataSet.first().x.length + "d_"+ this.clusterCount+"c");
	}

	public void relativeVarianceGradientTest()
	{
		DAEuclideanVectorSpace evs = new DAEuclideanVectorSpace(this.dataSet.first().x.length);
		
		RelativeVarianceOfDistancesObjectiveFunction relVarFunction = new RelativeVarianceOfDistancesObjectiveFunction(this.dataSet);
		SingleCentroidGradientOptimizationAlgorithm<double[]> algo = new SingleCentroidGradientOptimizationAlgorithm<double[]>(this.dataSet, evs, evs, relVarFunction, null); 
		
		algo.setObjectiveFunctionMonitoring(true);
		algo.setAscOrDesc(false);
		algo.initializeWithParameter(this.initialPositons.get(0));
		algo.setLearningFactor(0.5d);
		algo.apply(50);
		System.out.println(Arrays.toString(algo.getObjectiveFunctionValueHistory()));
		
		this.showDataSet(this.dataSet, algo, null, null, null, algo.algorithmName(), "RelVarGradientTest");

		double scale = 0.01d;
		double[] parameter = new double[2];
		double value;
		double min = Double.MAX_VALUE, max = 0.0d;
		double[] llC = new double[2];
		double[] urC = new double[2];
		
		ArrayList<double[]> boundingBox = SimpleStatistics.boundingBoxCornersIndexed(this.dataSet);
		llC[0] = (boundingBox.get(0)[0]-(boundingBox.get(1)[0] - boundingBox.get(0)[0])*0.5d);  
		llC[1] = (boundingBox.get(0)[1]-(boundingBox.get(1)[1] - boundingBox.get(0)[1])*0.5d);  
		urC[0] = (boundingBox.get(1)[0]+(boundingBox.get(1)[0] - boundingBox.get(0)[0])*0.5d);  
		urC[1] = (boundingBox.get(1)[1]+(boundingBox.get(1)[0] - boundingBox.get(0)[0])*0.5d);  
		
		FeatureSpaceSampling2D featureSampling = new FeatureSpaceSampling2D((int)((urC[0] - llC[0])/scale), (int)((urC[1] - llC[1])/scale));
		for(int x=0; x<featureSampling.sizeX(); x++)
		{
			for(int y=0; y<featureSampling.sizeY(); y++)
			{
				parameter[0] = ((double)x)*scale + llC[0] + 0.5d * scale;
				parameter[1] = ((double)y)*scale + llC[1] + 0.5d * scale;
				relVarFunction.setParameter(parameter);
				value = relVarFunction.functionValue();
				min = (value < min)? value : min;
				max = (value > max)? value : max;
				featureSampling.set(x, y, value);
			}	
		}
		for(int x=0; x<featureSampling.sizeX(); x++)
		{
			for(int y=0; y<featureSampling.sizeY(); y++)
			{
				value = featureSampling.get(x, y);
				featureSampling.set(x, y, (value - min)/(max - min));
			}	
		}
		
		featureSampling.setLowerLeftCorner(llC);
		featureSampling.setUpperRightCorner(urC);
		
		ArrayList<FeatureSpaceSampling2D> list = new ArrayList<FeatureSpaceSampling2D>();
		list.add(featureSampling);
		this.showDataSet(this.dataSet, null, null, list, null, algo.algorithmName(), "RelVarGradientTest");
	}

}
