/**
 * TODO File Description
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
		DALeastSquaresObjectiveFunction lsqOF = new DALeastSquaresObjectiveFunction(this.dataSet.first().x.length);
		SingleCentroidGradientOptimizationAlgorithm<double[]> gradientoptimization = new SingleCentroidGradientOptimizationAlgorithm<double[]>(this.dataSet, lsqOF.getVs(), lsqOF.getVs(), lsqOF);
		gradientoptimization.setAscOrDesc(false);
		gradientoptimization.initializeWith(this.initialPositons.get(0));
		gradientoptimization.setLearningFactor(0.25d);
		gradientoptimization.apply(50);
		
		this.showSingleCentroidGradientAlgorithm(gradientoptimization, gradientoptimization.algorithmName() + " - " + lsqOF.getName(), "LeastSquaresTest");
	}
	
	
}
