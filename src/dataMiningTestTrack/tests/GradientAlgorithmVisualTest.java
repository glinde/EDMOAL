/**
Copyright (c) 2012, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
 
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
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm;
import datamining.clustering.protoype.initial.DoubleArrayPrototypeGenerator;
import datamining.gradient.centroid.SingleCentroidGradientOptimizationAlgorithm;
import datamining.gradient.functions.DALeastSquaresObjectiveFunction;
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
			data.addAll(dg.gaussPoints(seeds.get(i), 0.1*dg.generatorRand.nextDouble()+0.1d, (9*dataObjectCount)/(10*this.clusterCount)));
		}
		
		// add noise
		noise.addAll(dg.uniformStandardPoints(dim, dataObjectCount/10));
		for(double[] x:noise)
		{
			for(int k=0; k<x.length; k++)
			{
				x[k] *= 2.0d;
				x[k] -= 0.5d;
			}
		}
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
	
	
}
