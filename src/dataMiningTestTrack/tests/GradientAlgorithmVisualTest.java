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
import java.util.Collections;

import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.objects.doubleArray.DAMaximumNorm;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import dataMiningTestTrack.experiments.snFCM.ScaledNormFuzzyCMeansObjectiveFunction;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.initial.DoubleArrayPrototypeGenerator;
import datamining.gradient.centroid.PrototypeGradientOptimizationAlgorithm;
import datamining.gradient.centroid.SingleCentroidGradientOptimizationAlgorithm;
import datamining.gradient.functions.DALeastSquaresObjectiveFunction;
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
	 * The number of clusters
	 */
	private int clusterCount;
		
	/**
	 * The initial positions of prototypes.
	 */
	private ArrayList<double[]> initialPositons;
	
	
	public GradientAlgorithmVisualTest(int dim, int dataObjectCount, int clusterCount)
	{
		super();
		
		this.clusterCount = clusterCount;

		ArrayList<double[]> noise = new ArrayList<double[]>();
		ArrayList<double[]> data = new ArrayList<double[]>();
		ArrayList<double[]> seeds = new ArrayList<double[]>();

		double[] min = new double[dim];
		double[] max = new double[dim];

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
		
		// add noise
		noise.addAll(dg.uniformStandardPoints(dim, dataObjectCount/10));
		for(double[] x:noise)
		{
			for(int k=0; k<dim; k++)
			{
				x[k] *= 2.0d;
				x[k] -= 0.5d;
			}
		}
		data.addAll(noise);
		
		// mix it
		Collections.shuffle(data);
		
				
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
	public void showDataSet()
	{
		this.showDataSet(this.dataSet, null);
	}
	
	
	public void leastSquaresTest()
	{
		DALeastSquaresObjectiveFunction lsqOF = new DALeastSquaresObjectiveFunction(this.dataSet);
		SingleCentroidGradientOptimizationAlgorithm<double[]> gradientoptimization = new SingleCentroidGradientOptimizationAlgorithm<double[]>(this.dataSet, lsqOF.getVs(), lsqOF.getVs(), lsqOF);
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
		
		PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>> algo = new PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>>(this.dataSet, parameterVS, parameterMetric, fcmFunction, centroids);
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
		PositionListParameterVectorSpace<double[]> parameterVS = new PositionListParameterVectorSpace<double[]>(evs, this.clusterCount);
		DAMaximumNorm maximumNorm = new DAMaximumNorm();
		PositionListParameterMetric<double[]> parameterMetric = new PositionListParameterMetric<double[]>(evs, maximumNorm, this.clusterCount);
		
		ScaledNormFuzzyCMeansObjectiveFunction snfcmFunction = new ScaledNormFuzzyCMeansObjectiveFunction(this.dataSet, 2.0d);
		snfcmFunction.setFuzzifier(2.0d);
		ArrayList<Centroid<double[]>> centroids = new ArrayList<Centroid<double[]>>(this.clusterCount);
		
		for(int i=0; i<this.clusterCount; i++) centroids.add(new Centroid<double[]>(evs, this.initialPositons.get(i)));
		
		PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>> algo = new PrototypeGradientOptimizationAlgorithm<double[], Centroid<double[]>, PositionListParameter<double[]>>(this.dataSet, parameterVS, parameterMetric, snfcmFunction, centroids);
		PositionListParameter<double[]> parameter = new PositionListParameter<double[]>(this.initialPositons);
		
		algo.setAscOrDesc(false);
		algo.initializeWithParameter(parameter);
		algo.setLearningFactor(2.0d);
		algo.apply(50);
		
		this.showDataMiningAlgorithm(algo, snfcmFunction, algo.algorithmName(), "SNFCMGradientTest");
	}
}
