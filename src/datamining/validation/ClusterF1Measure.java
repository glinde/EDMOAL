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
package datamining.validation;

import datamining.resultProviders.FuzzyNoiseClusteringProvider;
import etc.HungarianAlgorithm;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ClusterF1Measure<T> extends ClusterValidation<T>
{
	protected boolean crisp;
	
	/**
	 * @param clusterInfo
	 */
	public ClusterF1Measure(ClusteringInformation<T> clusterInfo, boolean crisp)
	{
		super(clusterInfo);
		
		this.crisp = crisp;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.validation.ClusterValidation#index()
	 */
	@Override
	public double index()
	{
		double[][] f1Matrix;
		
		f1Matrix = this.crisp ? this.crispF1Matrix() : this.fuzzyF1Matrix();
		
		// reverse matrix because the hungarian method implementation is minimizing the cost.
		for(int clus=0; clus<this.clusterInfo.getClusterCount(); clus++)
		{
			for(int clas=0; clas<this.clusterInfo.getClusterCount(); clas++)
			{
				f1Matrix[clus][clas] = -f1Matrix[clus][clas]; 
			}
		}
		
		// use the hungarian algorithm to find the best fitting combination of classes and clusters.
		HungarianAlgorithm hAlgo = new HungarianAlgorithm(f1Matrix);
		int[] oPer = hAlgo.execute();
		double index = 0.0d;
		
		for(int i=0; i<this.clusterInfo.getClusterCount(); i++)
		{
			index += -f1Matrix[i][oPer[i]]; // - because of the previous reverse of values
		}
		double noise = this.crisp ? this.crispF1Noise() : this.fuzzyF1Noise();
		index += noise;
		index /= ((double)this.clusterInfo.getClusterCount() + 1);

		return index;
	}
		
	public double[][] crispF1Matrix()
	{
		this.clusterInfo.checkCrispClusteringResult();
		this.clusterInfo.checkTrueClusteringResult();
	
		double[][] f1Matrix = new double[this.clusterInfo.getClusterCount()][this.clusterInfo.getClusterCount()];
		int dataObjectCount;
		double[] clusterSize = new double[this.clusterInfo.getClusterCount()];
		double[] classesSize = new double[this.clusterInfo.getClusterCount()];
		int clas, clus;
	
		dataObjectCount = this.clusterInfo.getCrispClusteringResult().length;
	
		for(int j=0; j<dataObjectCount; j++)
		{
			clus = this.clusterInfo.getCrispClusteringResult()[j];
			clas = this.clusterInfo.getTrueClusteringResult()[j];
			if(clas >= 0 && clus >= 0)
			{
				f1Matrix[clus][clas]+=1.0d;
			}
			if(clas >= 0) classesSize[clas]+=1.0d;
			if(clus >= 0) clusterSize[clus]+=1.0d;
		}

//		for(int i=0; i<this.clusterInfo.getClusterCount(); i++)
//		{
//			classesSize[i] = 1.0d/classesSize[i];
//			clusterSize[i] = 1.0d/clusterSize[i];
//		}
	
		for(clus=0; clus<this.clusterInfo.getClusterCount(); clus++)
		{
			for(clas=0; clas<this.clusterInfo.getClusterCount(); clas++)
			{
				if(classesSize[clas] == 0 && clusterSize[clus] == 0d) f1Matrix[clus][clas] = 1.0d;
				else if(classesSize[clas] == 0 || clusterSize[clus] == 0 || f1Matrix[clus][clas] == 0.0d) f1Matrix[clus][clas] = 0.0d;
				
				else f1Matrix[clus][clas] = 2.0d*(f1Matrix[clus][clas]/clusterSize[clus])*(f1Matrix[clus][clas]/classesSize[clas])/(f1Matrix[clus][clas]/classesSize[clas] + f1Matrix[clus][clas]/clusterSize[clus]);
			}
		}
		
		return f1Matrix;
	}

	public double[][] fuzzyF1Matrix()
	{
		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
		this.clusterInfo.checkTrueClusteringResult();
	
		double[][] f1Matrix = new double[this.clusterInfo.getClusterCount()][this.clusterInfo.getClusterCount()];
		int dataObjectCount;
		double[] clusterSize = new double[this.clusterInfo.getClusterCount()];
		double[] classesSize = new double[this.clusterInfo.getClusterCount()];
		int clas, clus;
		double[] membershipValues;
	
		dataObjectCount = (this.clusterInfo.getFuzzyClusteringResult() != null) ?
				this.clusterInfo.getFuzzyClusteringResult().size() :
				this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size();
	
		for(int j=0; j<dataObjectCount; j++)
		{
			membershipValues = (this.clusterInfo.getFuzzyClusteringResult() != null)?
					this.clusterInfo.getFuzzyClusteringResult().get(j) :
					this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(this.clusterInfo.getFuzzyClusteringProvider().getDataSet().get(j));			
			clas = this.clusterInfo.getTrueClusteringResult()[j];
			
			if(clas >= 0)
			{
				for(int i=0; i<this.clusterInfo.getClusterCount(); i++)
					f1Matrix[i][clas] += membershipValues[i];
			}

			if(clas >= 0) classesSize[clas]+=1.0d;
			for(int i=0; i<this.clusterInfo.getClusterCount(); i++) clusterSize[i] += membershipValues[i];
		}

	
		for(clus=0; clus<this.clusterInfo.getClusterCount(); clus++)
		{
			for(clas=0; clas<this.clusterInfo.getClusterCount(); clas++)
			{
				if(classesSize[clas] == 0 && clusterSize[clus] == 0d) f1Matrix[clus][clas] = 1.0d;
				else if(classesSize[clas] == 0 || clusterSize[clus] == 0 || f1Matrix[clus][clas] == 0.0d) f1Matrix[clus][clas] = 0.0d;
				
				else f1Matrix[clus][clas] = 2.0d*(f1Matrix[clus][clas]/clusterSize[clus])*(f1Matrix[clus][clas]/classesSize[clas])/(f1Matrix[clus][clas]/classesSize[clas] + f1Matrix[clus][clas]/clusterSize[clus]);
			}
		}
		
		return f1Matrix;
	}
//
//	public double[][] crispPrecisionMatrix()
//	{
//		this.clusterInfo.checkCrispClusteringResult();
//		this.clusterInfo.checkTrueClusteringResult();
//
//		double[][] clusterPrecision = new double[this.clusterInfo.getClusterCount()][this.clusterInfo.getClusterCount()];
//		int dataObjectCount;
//		int[] clusterSize = new int[this.clusterInfo.getClusterCount()];
//		int clas, clus;
//		double doubleTMP = 0.0d;
//
//		dataObjectCount = this.clusterInfo.getCrispClusteringResult().length;
//
//		for(int j=0; j<dataObjectCount; j++)
//		{
//			clus = this.clusterInfo.getCrispClusteringResult()[j];
//			clas = this.clusterInfo.getTrueClusteringResult()[j];
//			if(clas >= 0 && clus >= 0)
//			{
//				clusterSize[clus]++;
//				clusterPrecision[clus][clas]+=1.0d;
//			}
//		}
//
//		for(clus=0; clus<this.clusterInfo.getClusterCount(); clus++)
//		{
//			doubleTMP = 1.0d/clusterSize[clus];
//			for(clas=0; clas<this.clusterInfo.getClusterCount(); clas++)
//			{
//				clusterPrecision[clus][clas] *= doubleTMP;
//			}
//		}
//		
//		return clusterPrecision;
//	}
//	
//	public double[][] fuzzyPrecisionMatrix()
//	{
//		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
//		this.clusterInfo.checkTrueClusteringResult();
//		
//		double[][] clusterPrecision = new double[this.clusterInfo.getClusterCount()][this.clusterInfo.getClusterCount()];
//		double[] membershipValues;
//		int dataObjectCount;
//		double[] clusterSize = new double[this.clusterInfo.getClusterCount()];
//		int clas, clus;
//		double doubleTMP = 0.0d;
//
//		dataObjectCount = (this.clusterInfo.getFuzzyClusteringResult() != null) ?
//			this.clusterInfo.getFuzzyClusteringResult().size() :
//			this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size();
//
//		int i, j;
//		for(j=0; j<dataObjectCount; j++)
//		{
//			membershipValues = (this.clusterInfo.getFuzzyClusteringResult() != null)?
//				this.clusterInfo.getFuzzyClusteringResult().get(j) :
//				this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(this.clusterInfo.getFuzzyClusteringProvider().getDataSet().get(j));
//
//			for(i=0; i<this.clusterInfo.getClusterCount(); i++) clusterSize[i] += membershipValues[i];
//				
//			clas = this.clusterInfo.getTrueClusteringResult()[j];
//			if(clas >= 0)
//			{
//				for(i=0; i<this.clusterInfo.getClusterCount(); i++)
//					clusterPrecision[i][clas] += membershipValues[i];
//			}
//		}
//
//		for(clus=0; clus<this.clusterInfo.getClusterCount(); clus++)
//		{
//			doubleTMP = 1.0d/clusterSize[clus];
//			for(clas=0; clas<this.clusterInfo.getClusterCount(); clas++)
//			{
//				clusterPrecision[clus][clas] *= doubleTMP;
//			}
//		}
//				
//		return clusterPrecision;
//	}
//
//
//	public double[][] crispRecallMatrix()
//	{
//		this.clusterInfo.checkCrispClusteringResult();
//		this.clusterInfo.checkTrueClusteringResult();
//
//		double[][] clusterRecall = new double[this.clusterInfo.getClusterCount()][this.clusterInfo.getClusterCount()];
//		int dataObjectCount;
//		int[] classSize = new int[this.clusterInfo.getClusterCount()];
//		int clas, clus;
//		double doubleTMP = 0.0d;
//
//		dataObjectCount = this.clusterInfo.getCrispClusteringResult().length;
//
//		for(int j=0; j<dataObjectCount; j++)
//		{
//			clus = this.clusterInfo.getCrispClusteringResult()[j];
//			clas = this.clusterInfo.getTrueClusteringResult()[j];
//			if(clas >= 0 && clus >= 0)
//			{
//				classSize[clas]++;
//				clusterRecall[clus][clas]+=1.0d;
//			}
//		}
//
//		for(clas=0; clas<this.clusterInfo.getClusterCount(); clas++)
//		{
//			doubleTMP = 1.0d/classSize[clas];
//			for(clus=0; clus<this.clusterInfo.getClusterCount(); clus++)
//			{
//				clusterRecall[clus][clas] *= doubleTMP;
//			}
//		}
//		
//		return clusterRecall;
//	}
//	
//	public double[][] fuzzyRecallMatrix()
//	{
//		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
//		this.clusterInfo.checkTrueClusteringResult();
//		
//		double[][] clusterRecall = new double[this.clusterInfo.getClusterCount()][this.clusterInfo.getClusterCount()];
//		double[] membershipValues;
//		int dataObjectCount;
//		int[] classSize = new int[this.clusterInfo.getClusterCount()];
//		int clas, clus;
//		double doubleTMP = 0.0d;
//
//		dataObjectCount = (this.clusterInfo.getFuzzyClusteringResult() != null) ?
//			this.clusterInfo.getFuzzyClusteringResult().size() :
//			this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size();
//
//		int i, j;
//		for(j=0; j<dataObjectCount; j++)
//		{
//			membershipValues = (this.clusterInfo.getFuzzyClusteringResult() != null)?
//				this.clusterInfo.getFuzzyClusteringResult().get(j) :
//				this.clusterInfo.getFuzzyClusteringProvider().getFuzzyAssignmentsOf(this.clusterInfo.getFuzzyClusteringProvider().getDataSet().get(j));
//				
//			clas = this.clusterInfo.getTrueClusteringResult()[j];
//			if(clas >= 0)
//			{
//				classSize[clas]++;
//				for(i=0; i<this.clusterInfo.getClusterCount(); i++)
//					clusterRecall[i][clas] += membershipValues[i];
//			}
//		}
//
//		for(clas=0; clas<this.clusterInfo.getClusterCount(); clas++)
//		{
//			doubleTMP = 1.0d/classSize[clas];
//			for(clus=0; clus<this.clusterInfo.getClusterCount(); clus++)
//			{
//				clusterRecall[clus][clas] *= doubleTMP;
//			}
//		}
//				
//		return clusterRecall;
//	}

	public double crispF1Noise()
	{
		this.clusterInfo.checkCrispClusteringResult();
		this.clusterInfo.checkTrueClusteringResult();
	
		double noise = 0.0d;
		int dataObjectCount;
		int noiseClasCount = 0;
		int noiseClusCount = 0;
		int clas, clus;
	
		dataObjectCount = this.clusterInfo.getCrispClusteringResult().length;
	
		for(int j=0; j<dataObjectCount; j++)
		{
			clus = this.clusterInfo.getCrispClusteringResult()[j];
			clas = this.clusterInfo.getTrueClusteringResult()[j];
			if(clas == 0 && clus == 0)
			{
				noise += 1.0d;
			}
			if(clas == 0) noiseClasCount++;
			if(clus == 0) noiseClusCount++;
		}

		if(noiseClasCount == 0 && noiseClusCount == 0d) return 1.0d; // if there is neither noise in the data nor the algorithm detected any noise, the result is obviously perfect. 
		else if(noiseClasCount > 0 && noiseClusCount == 0d) return 0.0d; // if there is noise in the data, but the algorithm did not detect any, this is obviously wrong which is why the result is 0.
		else if(noiseClasCount == 0 && noiseClusCount > 0d) return 0.0d; // if there is no noise in the data, but the algorithm detect some, this is obviously wrong which is why the result is 0.
		else if(noise == 0.0d) return 0.0d;
		
		return 2.0d*noise/noiseClusCount*noise/noiseClasCount/(noise/noiseClusCount + noise/noiseClasCount);
	}

	public double fuzzyF1Noise()
	{
		this.clusterInfo.checkFuzzyClusteringProvider_FuzzyClusteringResult();
		this.clusterInfo.checkTrueClusteringResult();
		
		double noise = 0.0d;
		double noiseClusObj = 0.0d;
		int noiseClasObj = 0;
		double[] noiseAssignments = null;
		double dataObjectCount = (this.clusterInfo.getFuzzyClusteringResult() != null) ?
			this.clusterInfo.getFuzzyClusteringResult().size() :
			this.clusterInfo.getFuzzyClusteringProvider().getDataSet().size();
						
		if(this.clusterInfo.getNoiseClusterMembershipValues() != null)
		{
			noiseAssignments = this.clusterInfo.getNoiseClusterMembershipValues();
		}
		else if(this.clusterInfo.getFuzzyClusteringProvider() instanceof FuzzyNoiseClusteringProvider)
		{
			noiseAssignments = ((FuzzyNoiseClusteringProvider<T>)this.clusterInfo.getFuzzyClusteringProvider()).getFuzzyNoiseAssignments();
		}
		
		if(noiseAssignments != null)
		{
			for(int j=0; j<dataObjectCount; j++)
			{
				noiseClusObj += noiseAssignments[j];
				if(this.clusterInfo.getTrueClusteringResult()[j] < 0)
				{
					noise += noiseAssignments[j];
					noiseClasObj++;
				}
			}
		}
		
		if(noiseClasObj == 0 && noiseClusObj == 0.0d) return 1.0d; // if there is neither noise in the data nor the algorithm detected any noise, the result is obviously perfect. 
		else if(noiseClasObj > 0 && noiseClusObj == 0.0d) return 0.0d; // if there is noise in the data, but the algorithm did not detect any, this is obviously wrong which is why the result is 0.
		else if(noiseClasObj == 0 && noiseClusObj > 0.0d) return 0.0d; // if there is no noise in the data, but the algorithm detect some, this is obviously wrong which is why the result is 0.
		else if(noise == 0.0d) return 0.0d;
		
		return 2.0d*noise/noiseClasObj*noise/noiseClusObj/(noise/noiseClasObj + noise/noiseClusObj);
	}
}
