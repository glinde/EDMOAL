/**
Copyright (c) 2013, The EDMOAL Project

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
package dataMiningTestTrack.diss;

import io.CSVFileWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.objects.doubleArray.DAEuclideanMetric;
import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.SphericalNormalDistributionPrototype;
import datamining.clustering.protoype.altopt.ExpectationMaximizationSGMMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.FuzzyCMeansClusteringAlgorithm;
import datamining.clustering.protoype.altopt.FuzzyCMeansNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.HardCMeansClusteringAlgorithm;
import datamining.clustering.protoype.altopt.PolynomFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.PolynomFCMNoiseClusteringAlgorithm;
import datamining.clustering.protoype.altopt.RewardingCrispFCMClusteringAlgorithm;
import datamining.clustering.protoype.altopt.RewardingCrispFCMNoiseClusteringAlgorithm;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DissMembershipPlotCoordsCreator
{
	private double min;
	
	private double max;
	
	private double stepsize;
	
	private ArrayList<double[]> protoPos;
	
	private IndexedDataSet<double[]> dataSet;
	
	private String filePath;
	
	public DissMembershipPlotCoordsCreator(int nodes)
	{
		this.min = 0.0d;
		this.max = 12.0d;
		this.stepsize = (this.max-this.min)/nodes;
		
		this.protoPos = new ArrayList<double[]>();
		this.dataSet = new IndexedDataSet<double[]>();
		this.filePath = "dissartation/plotsdata/member/";
		File file = new File(filePath);
		if(!file.exists()) file.mkdirs();
				
		this.protoPos.add(new double[]{2.0d});
		this.protoPos.add(new double[]{3.0d});
		this.protoPos.add(new double[]{6.0d});
		
		for(int i=0; i<nodes; i++)
		{
			this.dataSet.add(new IndexedDataObject<double[]>(new double[]{i*this.stepsize}));
		}
		this.dataSet.add(new IndexedDataObject<double[]>(new double[]{max}));
		this.dataSet.seal();
	}
	
	public void writeMembershipCoordinates(List<double[]> membershipCoordinates, String filepath)
	{
		File file = new File(filepath);
		CSVFileWriter writer = new CSVFileWriter();
		writer.setAddFirstAttributeAsID(true);
		writer.setFirstLineAsAtributeNames(true);
		ArrayList<String> names = new ArrayList<String>();
		
		names.add("x");
		names.add("a");
		names.add("b");
		names.add("c");
		if(membershipCoordinates.get(0).length == 5) names.add("n");
		
		try
		{
			writer.openFile(file);
			writer.writeDoubleDataTable(membershipCoordinates, names);
			writer.closeFile();
		}
		catch(IOException e)
		{
			writer.closeFile();
			e.printStackTrace();
		}
	}
	
	public ArrayList<double[]> makeMembershipCoordinates(List<double[]> membershipValues)
	{
		ArrayList<double[]> coords = new ArrayList<double[]>(membershipValues.size());
		
		for(int i=0; i<this.dataSet.size(); i++)
		{
			double[] msv = membershipValues.get(i);
			double[] coord = new double[msv.length+1];
			coord[0] = this.dataSet.get(i).x[0];
			for(int k=0; k<msv.length; k++) coord[k+1] = msv[k];
			coords.add(coord);
		}
		
		return coords;
	}
	
	public ArrayList<double[]> makeMembershipCoordinates(List<double[]> membershipValues, double[] noiseMemberships)
	{
		ArrayList<double[]> coords = new ArrayList<double[]>(membershipValues.size());
		
		for(int i=0; i<this.dataSet.size(); i++)
		{
			double[] msv = membershipValues.get(i);
			double[] coord = new double[msv.length+2];
			coord[0] = this.dataSet.get(i).x[0];
			for(int k=0; k<msv.length; k++) coord[k+1] = msv[k];
			coord[msv.length+1] = noiseMemberships[i];
			coords.add(coord);
		}
		
		return coords;
	}
	
	public void hcm()
	{
		HardCMeansClusteringAlgorithm<double[]> algo = new HardCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(1), new DAEuclideanMetric());
		algo.initializeWithPositions(this.protoPos);
		
		ArrayList<double[]> membershipCoords = new ArrayList<double[]>();
		int[] clus = algo.getAllCrispClusterAssignments();
		for(int i=0; i<this.dataSet.size(); i++)
		{
			double[] coord = new double[this.protoPos.size()+1];
			coord[0] = this.dataSet.get(i).x[0];
			coord[clus[i]+1] = 1.0d;
			membershipCoords.add(coord);
		}
		
		this.writeMembershipCoordinates(membershipCoords, this.filePath+"HCM.csv");
	}

	
	public void fcm(double fuzzifier)
	{
		FuzzyCMeansClusteringAlgorithm<double[]> algo = new FuzzyCMeansClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(1), new DAEuclideanMetric());
		algo.setFuzzifier(fuzzifier);
		algo.initializeWithPositions(this.protoPos);
		
		ArrayList<double[]> membershipCoords = this.makeMembershipCoordinates(algo.getAllFuzzyClusterAssignments(null));
		
		this.writeMembershipCoordinates(membershipCoords, this.filePath+"FCM_"+fuzzifier+".csv");
	}

	
	public void nfcm(double fuzzifier)
	{
		FuzzyCMeansNoiseClusteringAlgorithm<double[]> algo = new FuzzyCMeansNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(1), new DAEuclideanMetric());
		algo.setFuzzifier(fuzzifier);
		algo.setNoiseDistance(1.0d);
		algo.initializeWithPositions(this.protoPos);
		
		ArrayList<double[]> membershipCoords = this.makeMembershipCoordinates(algo.getAllFuzzyClusterAssignments(null), algo.getFuzzyNoiseAssignments());
		
		this.writeMembershipCoordinates(membershipCoords, this.filePath+"NFCM_"+fuzzifier+".csv");
	}

	
	public void pfcm(double beta)
	{
		PolynomFCMClusteringAlgorithm<double[]> algo = new PolynomFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(1), new DAEuclideanMetric());
		algo.setBeta(beta);
		algo.initializeWithPositions(this.protoPos);
		
		ArrayList<double[]> membershipCoords = this.makeMembershipCoordinates(algo.getAllFuzzyClusterAssignments(null));
		
		this.writeMembershipCoordinates(membershipCoords, this.filePath+"PFCM_"+beta+".csv");
	}

	
	public void pnfcm(double beta)
	{
		PolynomFCMNoiseClusteringAlgorithm<double[]> algo = new PolynomFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(1), new DAEuclideanMetric());
		algo.setBeta(beta);
		algo.setNoiseDistance(1.0d);
		algo.initializeWithPositions(this.protoPos);

		ArrayList<double[]> membershipCoords = this.makeMembershipCoordinates(algo.getAllFuzzyClusterAssignments(null), algo.getFuzzyNoiseAssignments());
		
		this.writeMembershipCoordinates(membershipCoords, this.filePath+"PNFCM_"+beta+".csv");
	}

	
	public void rcfcm(double distMul)
	{
		RewardingCrispFCMClusteringAlgorithm<double[]> algo = new RewardingCrispFCMClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(1), new DAEuclideanMetric());
		algo.setFuzzifier(2.0d);
		algo.setDistanceMultiplierConstant(distMul);
		algo.initializeWithPositions(this.protoPos);
		
		ArrayList<double[]> membershipCoords = this.makeMembershipCoordinates(algo.getAllFuzzyClusterAssignments(null));
		
		this.writeMembershipCoordinates(membershipCoords, this.filePath+"RCFCM_"+distMul+".csv");
	}

	
	public void rcnfcm(double distMul)
	{
		RewardingCrispFCMNoiseClusteringAlgorithm<double[]> algo = new RewardingCrispFCMNoiseClusteringAlgorithm<double[]>(this.dataSet, new DAEuclideanVectorSpace(1), new DAEuclideanMetric());
		algo.setFuzzifier(2.0d);
		algo.setDistanceMultiplierConstant(distMul);
		algo.setNoiseDistance(1.0d);
		algo.initializeWithPositions(this.protoPos);

		ArrayList<double[]> membershipCoords = this.makeMembershipCoordinates(algo.getAllFuzzyClusterAssignments(null), algo.getFuzzyNoiseAssignments());
		
		this.writeMembershipCoordinates(membershipCoords, this.filePath+"RCNFCM_"+distMul+".csv");
	}

	
	public void emgmm(double variance)
	{
		ExpectationMaximizationSGMMClusteringAlgorithm algo = new ExpectationMaximizationSGMMClusteringAlgorithm(this.dataSet, new DAEuclideanVectorSpace(1), new DAEuclideanMetric());
		algo.initializeWithPositions(this.protoPos);
		for(SphericalNormalDistributionPrototype proto:algo.getPrototypes())
		{
			proto.setVariance(variance);
		}
		
		ArrayList<double[]> membershipCoords = this.makeMembershipCoordinates(algo.getAllFuzzyClusterAssignments(null));
		
		this.writeMembershipCoordinates(membershipCoords, this.filePath+"EMGMM.csv");
	}
}
