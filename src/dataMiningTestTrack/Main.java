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


package dataMiningTestTrack;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

import data.structures.balltree.BallTree;
import dataMiningTestTrack.diss.ClusterDataSetsGenerator;
import dataMiningTestTrack.diss.DissExperiments;
import dataMiningTestTrack.tests.ClusterAlgorithmTestCentre;
import dataMiningTestTrack.tests.ClusterAlgorithmVisualSpeedTest;
import dataMiningTestTrack.tests.ClusterAlgorithmVisualTest;
import dataMiningTestTrack.tests.DataGenerationAlgorithmTest;
import dataMiningTestTrack.tests.DataStructureSpeedTest;
import dataMiningTestTrack.tests.DataStructureVisualTest;
import dataMiningTestTrack.tests.DistortionTester;
import dataMiningTestTrack.tests.GradientAlgorithmVisualTest;
import dataMiningTestTrack.tests.SymmetricalDataAlgorithmVisualTest;
import datamining.clustering.ClusteringAlgorithm;
import etc.MyMath;

/**
 * Since the EDMOAL project is just an API, a main class would not be necessary to provide.
 * In fact, it is very likely that it will vanish in due time. For now, the main function and all the
 * classes in this package are provided for the user to get some examples, how to use EDMOAL.<br>
 * 
 * Many commands are deactivated by line comments. Please activate them as you wish.
 * 
 * @author Roland Winkler
 */
public class Main
{
	/**
	 * Decide whether you want to visually test some algorithms or if you like to
	 * perform a performance test of the various aspects of EDMOAL.
	 * 
	 * @param args The command arguments. They are ignored for now.
	 */
	public static void main(String[] args)
	{
		System.out.println("number of cores: " + Runtime.getRuntime().availableProcessors());

//		Main.dataStructureVisualTest();
//		Main.dataStructureSpeedTest();
//		Main.clusterAlgorithmVisualTest();
//		Main.clusterAlgorithmVisualSpeedTest();
//		Main.gradientAlgorithmVisualTest();
//		Main.symmetricalGgradientAlgorithmVisualTest();
//		Main.dataGenerationAlgorithmTest();
//		Main.distortionTest();
//		Main.distortionClusteringTest();
//		Main.clusterValidityTest();
//		DissExperiments.genDissDataSets();
		DissExperiments.clusterDissDataSets();
	}
	
	
	/**
	 * Performs a test of the data structure algorithms like the {@link BallTree} and visualises
	 * the result such that the properties of the data structure become visible.
	 */
	public static void dataStructureVisualTest()
	{
		DataStructureVisualTest visualTest = new DataStructureVisualTest(10000);
		visualTest.ballTreeStructureTest(); System.gc();
//		visualTest.centeredBallTreeStructureTest(); System.gc();
	}
	

	/**
	 * Performs a test of the data structures w.r.t. to their time consumption on certain tasks.
	 * Please refrain from applying other applications that require high CPU power or need a lot
	 * of memory bandwidth.
	 */
	public static void dataStructureSpeedTest()
	{
		long milliseconds = 0;		
		int dim = 2, number = 10000000, queryNumber = 100000;
		
		System.out.println("===== Randomly Generated Data Set =====");
		System.out.print("Build data set ("+dim+" dim, "+number+" obj) .. ");
		milliseconds = -System.currentTimeMillis();
		DataStructureSpeedTest speedTest = new DataStructureSpeedTest(dim, number, queryNumber);
		milliseconds += System.currentTimeMillis();
		System.out.print("done: "+ milliseconds + "ms");
//		speedTest.showSimplifiedDataSet(20000);
		speedTest.ballTreeSpeedTest();System.gc();
		speedTest.centeredBallTreeSpeedTest();System.gc();
	}
	
	/**
	 * Performs tests of the clustering algorithms, using synthetic data sets.
	 * The result is visualised so that the result of the algorithm is easily
	 * interpretable. The point of these tests is, to give the user some
	 * feeling of how the algorithms behave and to decide whether or not they
	 * might be useful for him.
	 */
	public static void clusterAlgorithmVisualTest()
	{
		int dim = 20, number = 15000, clusterCount = 30;
		
		ClusterAlgorithmVisualTest clusterTest = new ClusterAlgorithmVisualTest(dim, number, clusterCount);
//		clusterTest.showDataSet();
//		clusterTest.showClusteredDataSet();
//		clusterTest.testHardCMeans();
//		clusterTest.testFuzzyCMeans();
//		clusterTest.testFuzzyCMeansNoise();
//		clusterTest.testPolynomialFuzzyCMeans();
//		clusterTest.testPolynomialFuzzyCMeansNoise();
//		clusterTest.testRewardingCrispFCM();		
//		clusterTest.testRewardingCrispFCMNoise();
//		clusterTest.testVoronoiPartitionFCM();		
//		clusterTest.testVoronoiPartitionFCMNoise(); 
//		clusterTest.testDistAdaptedFCM(); 
//		clusterTest.testDistAdaptedFCMNoise();
//		clusterTest.testBallTreeFuzzyCMeans();
//		clusterTest.testExpectationMaximization();
//		clusterTest.testDBScan();
	}

	
	/**
	 * Performs tests of the clustering algorithms, using synthetic data sets.
	 * The result is visualised so that the result of the algorithm is easily
	 * interpretable. The point of these tests is, to give the user some
	 * feeling of how the algorithms behave and to decide whether or not they
	 * might be useful for him. Also provides some time measurments and
	 * the visualization of the data set is not the complete data set but rather a randomly
	 * selected subset to limit the complexity of showing the figures.
	 */
	public static void clusterAlgorithmVisualSpeedTest()
	{
		int dim = 20, number = 50000, clusterCount = 30, visibleDataCount = 10000;

		ClusterAlgorithmVisualSpeedTest clusterTest = new ClusterAlgorithmVisualSpeedTest(dim, number, clusterCount, visibleDataCount);		
		
//		clusterTest.showDataSet();
//		clusterTest.showClusteredDataSet();
//		clusterTest.testHardCMeans();
//		clusterTest.testFuzzyCMeans();
//		clusterTest.testFuzzyCMeansNoise();
//		clusterTest.testPolynomialFuzzyCMeans();
//		clusterTest.testPolynomialFuzzyCMeansNoise();
		clusterTest.testRewardingCrispFCM();		
		clusterTest.testRewardingCrispFCMNoise();
//		clusterTest.testVoronoiPartitionFCM();		
//		clusterTest.testVoronoiPartitionFCMNoise(); 
//		clusterTest.testDistAdaptedFCM(); 
//		clusterTest.testDistAdaptedFCMNoise();
//		clusterTest.testBallTreeFuzzyCMeans();
//		clusterTest.testExpectationMaximization();
//		clusterTest.testDBScan();
	}

	/**
	 * Performs tests of the clustering algorithms, using synthetic data sets.
	 * The result is visualised so that the result of the algorithm is easily
	 * interpretable. The point of these tests is, to give the user some
	 * feeling of how the algorithms behave and to decide whether or not they
	 * might be useful for him.
	 */
	public static void gradientAlgorithmVisualTest()
	{
		int dim = 2, number = 1500, clusterCount = 3;
		
		GradientAlgorithmVisualTest gradientTest = new GradientAlgorithmVisualTest(dim, number, clusterCount, 0.0d);
		gradientTest.printPNG = false;

		gradientTest.showDataSet(true);
//		gradientTest.leastSquaresTest();
//		gradientTest.fcmGradientTest();
//		gradientTest.snfcmGradientTest();
//		gradientTest.testRewardingCrispFCM();		
//		gradientTest.testRewardingCrispFCMNoise();
//		gradientTest.snfcmGradientTest(1.2d, 0.5d);
//		gradientTest.snfcmGradientTest(1.2d, 1.0d);
//		gradientTest.snfcmGradientTest(1.2d, 1.5d);
//		gradientTest.snfcmGradientTest(1.2d, 2.0d);
//		gradientTest.snfcmGradientTest(1.25d, 0.5d);
//		gradientTest.snfcmGradientTest(1.25d, 1.0d);
//		gradientTest.snfcmGradientTest(1.25d, 1.5d);
//		gradientTest.snfcmGradientTest(1.25d, 2.0d);
//		gradientTest.snfcmGradientTest(1.3d, 0.5d);
//		gradientTest.snfcmGradientTest(1.3d, 1.0d);
//		gradientTest.snfcmGradientTest(1.3d, 1.5d);
//		gradientTest.snfcmGradientTest(1.3d, 2.0d);
		gradientTest.relativeVarianceGradientTest();
		
//		MyMath.pow(1.25d, 14);
	}
	
	/**
	 * Performs tests of the clustering algorithms, using synthetic, highly symmetrical data sets.
	 * The result is visualised so that the result of the algorithm is easily interpretable.
	 * The point of these tests is, to give the user some feeling of how the algorithms behave
	 * and to decide whether or not they might be useful for him.
	 */
	public static void symmetricalGgradientAlgorithmVisualTest()
	{
		int cluster = 5, dim = 2, dataObjectsPerCluster = 100, noisePoints = 100;
		
		SymmetricalDataAlgorithmVisualTest gradientTest = new SymmetricalDataAlgorithmVisualTest();
		gradientTest.gen2DCircleClusterPoints(cluster, 0.01d, dataObjectsPerCluster, noisePoints);
		gradientTest.printPNG = false;

		gradientTest.showDataSet(true);
//		gradientTest.leastSquaresTest();
//		gradientTest.fcmGradientTest();
//		gradientTest.snfcmGradientTest();
//		gradientTest.testRewardingCrispFCM();		
//		gradientTest.testRewardingCrispFCMNoise();
//		gradientTest.snfcmGradientTest(1.2d, 0.5d);
//		gradientTest.snfcmGradientTest(1.2d, 1.0d);
//		gradientTest.snfcmGradientTest(1.2d, 1.5d);
//		gradientTest.snfcmGradientTest(1.2d, 2.0d);
//		gradientTest.snfcmGradientTest(1.25d, 0.5d);
//		gradientTest.snfcmGradientTest(1.25d, 1.0d);
//		gradientTest.snfcmGradientTest(1.25d, 1.5d);
//		gradientTest.snfcmGradientTest(1.25d, 2.0d);
//		gradientTest.snfcmGradientTest(1.3d, 0.5d);
//		gradientTest.snfcmGradientTest(1.3d, 1.0d);
//		gradientTest.snfcmGradientTest(1.3d, 1.5d);
//		gradientTest.snfcmGradientTest(1.3d, 2.0d);
		gradientTest.relativeVarianceGradientTest();
		
//		MyMath.pow(1.25d, 14);
	}
	
	public static void dataGenerationAlgorithmTest()
	{
		DataGenerationAlgorithmTest test = new DataGenerationAlgorithmTest(10, 10000, 3, 0.2);
		
//		test.mixtureOfGaussiansTest();
//		test.variousDistributionTest();
	}
	
	public static void distortionTest()
	{
		DistortionTester tester = new DistortionTester();
		
		int dim = 10;
		
		int dataPerCluster = 10000;
		int clusterCount = 1;
		int noise = 0;// = dataPerCluster*clusterCount/10;
				
		tester.testDistortedClusters(dim, dataPerCluster, false, clusterCount, noise, true, 5);
	}

	public static void distortionClusteringTest()
	{
		int dim = 50;		
		int dataPerCluster = 5000;
		int clusterCount = 1;
		int noise = dataPerCluster*clusterCount/10;

		ClusterAlgorithmTestCentre testCentre = new ClusterAlgorithmTestCentre(dim, clusterCount);

		testCentre.printPNG = false;
		testCentre.printJPG = false;
		testCentre.printSVG = false;
		testCentre.printPDF = false;
		
		testCentre.generateDistortedData(dataPerCluster, false, 0, true, 0);
//		testCentre.generateInitialPositionsRandomUniform();
//		testCentre.showDataSetClustered2DProjections(dim/3.0d, dim/2.0d, null);
		for(int i=0; i<dim; i+=Math.max(dim/3, 1.0d))
		{
			for(int j=i+1; j<dim; j+=Math.max(dim/2, 1.0d))
			{
				testCentre.showDataSet(i, j, dim+"Dim_Single_Cluster_proj_" + i + "-" + j);
			}
		}
		
//		testCentre.testRewardingCrispFCM(1.0d-1.0d/dim);
//		testCentre.showFuzzyClusteringResult2DProjections(dim/3.0d, dim/2.0d, null);
	}

	public static void clusterValidityTest()
	{
		int dim = 100;
		int dataPerCluster = 1000;
		int clusterCount = 250;
		int noise = dataPerCluster*clusterCount/10;
		int cluster1 = (int)(Math.sqrt(dim));
		int flip1 = (int)(Math.sqrt(cluster1));
		int flip0 = (int)(Math.sqrt(cluster1))/2;

		long time = System.currentTimeMillis();
		
		ClusterAlgorithmTestCentre testCentre = new ClusterAlgorithmTestCentre(dim, clusterCount);


		System.out.println("Total time for worst case generating: " + (System.currentTimeMillis() - time)/1000 + " s");
		
		testCentre.printPNG = false;
		testCentre.printJPG = false;
		testCentre.printSVG = false;
		testCentre.printPDF = false;

//		testCentre.generateUniformDistributedNormalClusteres(dataPerCluster, false, 0, 0.01, false);
//		testCentre.generateUniformDistributedNormalClusteres(dataPerCluster, true, noise, 0.01, true);
		testCentre.generateDistortedData(dataPerCluster, true, noise, true, 6);
//		testCentre.generateDistortedData(dataPerCluster, false, 0, true, 4);
//		testCentre.generateCornerCentricClusteres(dataPerCluster, true, clusterCount, noise, cluster1, true, flip1, flip0);
		
		
		testCentre.generateInitialPositionsRandomUniform();
		testCentre.printDataStatistics();
		testCentre.showDataSetClustered2DProjections(dim/1.0d, dim/1.0d, dim+"Dim_"+clusterCount+"_Clusters");
//
//		testCentre.setCrispClusteringResultToTrueClusteringResult();
//		testCentre.validateLastClusteringResult();
//		testCentre.showCrispClusteringResult2DProjections(dim/1.0d, dim/1.0d, "True Classes");
//
//		testCentre.setCrispClusteringResultToRandomClusteringResult();
//		testCentre.validateLastClusteringResult();
//		testCentre.showCrispClusteringResult2DProjections(dim/1.0d, dim/1.0d, "Random");
//		
		time = System.currentTimeMillis();
		
		testCentre.testHardCMeans();
		testCentre.validateLastClusteringResult();
		testCentre.showCrispClusteringResult2DProjections(dim/1.0d, dim/1.0d, "HCM");
//
		testCentre.testFuzzyCMeans(2.0d);
		testCentre.validateLastClusteringResult();
		testCentre.showFuzzyClusteringResult2DProjections(dim/1.0d, dim/1.0d, "FCM 2.0");
		
		testCentre.testFuzzyCMeansNoise(2.0d, 0.2d);
		testCentre.validateLastClusteringResult();
		testCentre.showFuzzyClusteringResult2DProjections(dim/1.0d, dim/1.0d, "NFCM 2.0");
		
		testCentre.testFuzzyCMeans(1.0d+1.0d/dim);
		testCentre.validateLastClusteringResult();
		testCentre.showFuzzyClusteringResult2DProjections(dim/1.0d, dim/1.0d, "FCM 1+1/m");
		
		testCentre.testFuzzyCMeansNoise(1.0d+1.0d/dim, 0.2d);
		testCentre.validateLastClusteringResult();
		testCentre.showFuzzyClusteringResult2DProjections(dim/1.0d, dim/1.0d, "NFCM 2.0");
//		
		testCentre.testPolynomialFuzzyCMeans(0.5d);
		testCentre.validateLastClusteringResult();
		testCentre.showFuzzyClusteringResult2DProjections(dim/1.0d, dim/1.0d, "PFCM");
//	
		testCentre.testPolynomialFuzzyCMeansNoise(0.5d, 0.2d);
		testCentre.validateLastClusteringResult();
		testCentre.showFuzzyClusteringResult2DProjections(dim/1.0d, dim/1.0d, "PNFCM");
//		
		testCentre.testRewardingCrispFCM(1.0d-1.0d/dim);
		testCentre.validateLastClusteringResult();
		testCentre.showFuzzyClusteringResult2DProjections(dim/1.0d, dim/1.0d, "RFCM");
//
		testCentre.testRewardingCrispFCMNoise(1.0d-1.0d/dim, 0.2d);
		testCentre.validateLastClusteringResult();
		testCentre.showFuzzyClusteringResult2DProjections(dim/1.0d, dim/1.0d, "RNFCM");
//
		testCentre.testExpectationMaximization();
		testCentre.validateLastClusteringResult();
		testCentre.showFuzzyClusteringResult2DProjections(dim/1.0d, dim/1.0d, "EM");

		System.out.println("Total time for worst case analysing: " + (System.currentTimeMillis() - time)/1000 + " s");
	}
}
