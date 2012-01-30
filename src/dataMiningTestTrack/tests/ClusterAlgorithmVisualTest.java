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
import etc.DataGenerator;


/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ClusterAlgorithmVisualTest extends TestVisualizer implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -4791280640176444354L;

	private IndexedDataSet<double[]> dataSet;
	
	private int clusterCount;
	
	private int[] correctClustering;
	
	private ArrayList<double[]> initialPositons;
	
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
			tmpData = dg.gaussPoints(seeds.get(i), 0.1*dg.generatorRand.nextDouble(), 9*dataObjectCount/(10*this.clusterCount));

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
	 * 
	 */
	public void showDataSet()
	{
		this.showDataSet(this.dataSet, null);
	}
	
	/**
	 * 
	 */
	public void showClusteredDataSet()
	{
		this.showCrispDataSetClustering(this.dataSet, this.clusterCount, this.correctClustering, null);
	}
	
	/**
	 * 
	 */
	public void testHardCMeans()
	{
		HardCMeansClusteringAlgorithm<double[]> clusterAlgo = new HardCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "HCM_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * 
	 */
	public void testFuzzyCMeans()
	{
		FuzzyCMeansClusteringAlgorithm<double[]> clusterAlgo = new FuzzyCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "FCM_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * 
	 */
	public void testFuzzyCMeansNoise()
	{
		FuzzyCMeansNoiseClusteringAlgorithm<double[]> clusterAlgo = new FuzzyCMeansNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setNoiseDistance(0.2d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "FCMN_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * 
	 */
	public void testPolynomialFuzzyCMeans()
	{
		PolynomFCMClusteringAlgorithm<double[]> clusterAlgo = new PolynomFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setBeta(0.3d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "PFCM_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * 
	 */
	public void testPolynomialFuzzyCMeansNoise()
	{
		PolynomFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new PolynomFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setBeta(0.3d);
		clusterAlgo.setNoiseDistance(0.3d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "PFCMN_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * 
	 */
	public void testRewardingCrispFCM()
	{
		RewardingCrispFCMClusteringAlgorithm<double[]> clusterAlgo = new RewardingCrispFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceMultiplierConstant(0.7d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "PFCM_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * 
	 */
	public void testRewardingCrispFCMNoise()
	{
		RewardingCrispFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new RewardingCrispFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceMultiplierConstant(0.7d);
		clusterAlgo.setNoiseDistance(0.3d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "PFCMN_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * 
	 */
	public void testVoronoiPartitionFCM()
	{
		VoronoiPartitionFCMClusteringAlgorithm<double[]> clusterAlgo = new VoronoiPartitionFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length));
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "VPFCM_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * 
	 */
	public void testVoronoiPartitionFCMNoise()
	{
		VoronoiPartitionFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new VoronoiPartitionFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length));
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setNoiseDistance(0.2d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "VPFCMN_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * 
	 */
	public void testDistAdaptedFCM()
	{
		DistAdaptedFCMClusteringAlgorithm<double[]> clusterAlgo = new DistAdaptedFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceCorrectionParameter(1.0d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "DAFCM_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * 
	 */
	public void testDistAdaptedFCMNoise()
	{
		DistAdaptedFCMNoiseClusteringAlgorithm<double[]> clusterAlgo = new DistAdaptedFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setDistanceCorrectionParameter(1.0d);
		clusterAlgo.setNoiseDistance(0.2d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "DAFCMN_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}

	/**
	 * 
	 */
	public void testBallTreeFuzzyCMeans()
	{
		BallTreeFuzzyCMeansClusteringAlgorithm<double[]> clusterAlgo = new BallTreeFuzzyCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setFuzzifier(2.0d);
		clusterAlgo.setMaximalMembershipIntervalLength(0.2d);
		clusterAlgo.setEpsilon(0.01d);
		clusterAlgo.apply(50);
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "BTFCM_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}


	/**
	 * 
	 */
	public void testExpectationMaximization()
	{
		ExpectationMaximizationSGMMClusteringAlgorithm clusterAlgo = new ExpectationMaximizationSGMMClusteringAlgorithm(this.dataSet, new DAEuclideanVectorSpace(this.dataSet.first().element.length), new DAEuclideanMetric());
		clusterAlgo.initializeWithPositions(this.initialPositons);
		clusterAlgo.setVarianceBounded(true);
		clusterAlgo.setVarianceLowerBound(0.0001d);
		clusterAlgo.setVarianceUpperBound(1000.0d);
		clusterAlgo.setEpsilon(0.001d);
		clusterAlgo.apply(50);
//		for(SphericalNormalDistributionPrototype d:clusterAlgo.getActivePrototypes()) System.out.println(d.getClusterIndex() + ": " + d.getVariance());
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "EMGMM_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}
	

	/**
	 * 
	 */
	public void testDBScan()
	{
		DBScan<double[]> clusterAlgo = new DBScan<double[]>(this.dataSet, new DAEuclideanMetric());
		clusterAlgo.setCoreNum(50);
		clusterAlgo.setCoreDist(0.02d);
		clusterAlgo.apply();
		this.showClusteringAlgorithm(clusterAlgo, clusterAlgo.algorithmName(), "DBScan_" + this.dataSet.first().element.length + "d_"+ this.clusterCount+"c");
	}
}
