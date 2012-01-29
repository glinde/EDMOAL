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


package datamining.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ClusterAnalyser<T>  implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 3198045630164304643L;

	public static enum ClusterDistance
	{
		PAIRWISE_DATAOBJECTS, PAIRWISE_PROTOTYPES
	}
	
	public static enum ClusterSize
	{
		LINEAR, SQUARED
	}
	
	private ClusterDistance clusterDistance;
	private ClusterSize clusterSize;
	
	public ClusterAnalyser()
	{
		this.clusterDistance = ClusterDistance.PAIRWISE_DATAOBJECTS;
		this.clusterSize = ClusterSize.LINEAR;
	}

	public ClusterAnalyser(ClusterDistance cd, ClusterSize cs)
	{
		this.clusterDistance = cd;
		this.clusterSize = cs;
	}
	
	
	public double[][] pariwise_dataObject_clusterDistance(List<IndexedDataObject<T>> dataSet, List<double[]> fuzzyResult, Metric<T> dist)
	{
		int i, j, k, l;

		T dataObjectJ;
		T dataObjectL;
		double[] membershipValuesJ;
		double[] membershipValuesL;
		double doubleTMP;
		int clusterCount = fuzzyResult.get(0).length;
		double[][] pairwiseClusterDistances = new double[clusterCount][clusterCount];
		
		double[] membershipValueSum = this.membershipValueSums(fuzzyResult);
		
		for(j=0; j<dataSet.size(); j++)
		{
			dataObjectJ = dataSet.get(j).element;
			membershipValuesJ = fuzzyResult.get(j);

			for(l=j+1; l<dataSet.size(); l++)
			{
				dataObjectL = dataSet.get(l).element;
				membershipValuesL = fuzzyResult.get(l);
				doubleTMP = dist.distance(dataObjectJ, dataObjectL);
				
				if(doubleTMP == 0.0d) continue;
				
				for(i=0; i<clusterCount; i++)
				{
					for(k=i+1; k<clusterCount; k++)
					{
						pairwiseClusterDistances[i][k] += 2.0d * membershipValuesJ[i] * membershipValuesL[k] * doubleTMP;
					}
				}
				
			}
		}
		
		for(i=0; i<clusterCount; i++)
		{
			for(k=i+1; k<clusterCount; k++)
			{
				if(membershipValueSum[i] * membershipValueSum[k] == 0.0d) 
				{
					pairwiseClusterDistances[i][k] = Double.POSITIVE_INFINITY;
					pairwiseClusterDistances[k][i] = Double.POSITIVE_INFINITY;
				}
				else
				{
					pairwiseClusterDistances[i][k] /= membershipValueSum[i] * membershipValueSum[k];
					pairwiseClusterDistances[k][i] = pairwiseClusterDistances[i][k];
				}
			}
		}
		
		return pairwiseClusterDistances;
	}

	public double[][] pariwise_prototype_clusterDistance(List<Prototype<T>> prototypes, Metric<T> dist)
	{
		int i, k;

		double doubleTMP;
		double[][] pairwiseClusterDistances = new double[prototypes.size()][prototypes.size()];
		
				
		for(i=0; i<prototypes.size(); i++)
		{
			for(k=i+1; k<prototypes.size(); k++)
			{
				doubleTMP = dist.distance(prototypes.get(i).getPosition(), prototypes.get(k).getPosition());
				pairwiseClusterDistances[i][k] = doubleTMP;
				pairwiseClusterDistances[k][i] = doubleTMP;
			}
		}
		
		return pairwiseClusterDistances;
	}
	
	
	public double[] clusterDiameters(List<IndexedDataObject<T>> dataSet, List<double[]> fuzzyResult, List<Prototype<T>> prototypes, Metric<T> dist)
	{
		int i, j;
		T dataObject;
		
		double[] clusterDiameters = new double[prototypes.size()];	
		double[] membershipValueSum = new double[prototypes.size()];
		double[] membershipValues;
		for(j=0; j<dataSet.size(); j++)
		{
			dataObject = dataSet.get(j).element;
			membershipValues = fuzzyResult.get(j);
			
			for(i=0; i<prototypes.size(); i++)
			{

				switch(this.clusterSize)
				{
					case LINEAR:
						clusterDiameters[i] += membershipValues[i] * dist.distance(dataObject, prototypes.get(i).getPosition());
						break;

					case SQUARED:
						clusterDiameters[i] += membershipValues[i] * dist.distanceSq(dataObject, prototypes.get(i).getPosition());
						break;
						
					default:
						clusterDiameters[i] += membershipValues[i] * dist.distance(dataObject, prototypes.get(i).getPosition());
				}
				
				membershipValueSum[i] += membershipValues[i];
			}
		}
		for(i=0; i<prototypes.size(); i++)
		{
			switch(this.clusterSize)
			{
				case LINEAR:
					clusterDiameters[i] = clusterDiameters[i]/membershipValueSum[i];
					break;

				case SQUARED:
					clusterDiameters[i] = Math.sqrt(clusterDiameters[i]/membershipValueSum[i]);
					break;
					
				default:
					clusterDiameters[i] = clusterDiameters[i]/membershipValueSum[i];
			}
		}
		
		return clusterDiameters;
	}
	
	public ArrayList<Prototype<T>> estimatePrototypeLocations(List<IndexedDataObject<T>> dataSet, List<double[]> fuzzyResult, VectorSpace<T> vs)
	{
		int i, j;
		int clusterCount = fuzzyResult.get(0).length;
		T tmpX = vs.getNewAddNeutralElement();	
		ArrayList<T> prototypeLocations = new ArrayList<T>();
		for(i=0; i<clusterCount; i++) prototypeLocations.add(vs.getNewAddNeutralElement());
		double[] membershipValueSum = new double[clusterCount];
		
		for(j=0; j<dataSet.size(); j++)
		{
			for(i=0; i<clusterCount; i++)
			{
				vs.copy(tmpX, dataSet.get(j).element);
				vs.mul(tmpX, fuzzyResult.get(j)[i]);
				vs.add(prototypeLocations.get(i), tmpX);
				
				membershipValueSum[i] += fuzzyResult.get(j)[i];
			}
		}
		for(i=0; i<clusterCount; i++)
		{
			if(membershipValueSum[i] > 0.0)
			{
				vs.mul(prototypeLocations.get(i), membershipValueSum[i]);
			}
		}
		
		ArrayList<Prototype<T>> prototypes = new ArrayList<Prototype<T>>(clusterCount);
		for(T d:prototypeLocations) prototypes.add(new Centroid<T>(vs, d));
		
		return prototypes;
	}
	
	public double[] membershipValueSums(List<double[]> fuzzyResult)
	{
		int i, j, clusterCount = fuzzyResult.get(0).length;
		double[] sum = new double[clusterCount];
		
		for(j=0; j<fuzzyResult.size(); j++)
		{
			for(i=0; i<clusterCount; i++)
			{
				sum[i] += fuzzyResult.get(j)[i]; 
			}
		}
		
		return sum;
	}
		
	/** 
	 * @param fuzzyAlgorithm
	 * @param vs
	 * @param dist
	 * @return
	 */
	public double xieBeniIndex(FuzzyClusteringAlgorithm<T> fuzzyAlgorithm, VectorSpace<T> vs, Metric<T> dist)
	{
		if(fuzzyAlgorithm instanceof CrispNoiseClusteringAlgorithm || fuzzyAlgorithm instanceof FuzzyNoiseClusteringAlgorithm)
		{
			System.err.println("The Xie-Beni-Index is not valid for noise clustering");
			return 0.0d;
		}
//		System.out.print("Xie-Beni-Index ");
		
		int i, j, k;
		
		double[] membershipValues = new double[fuzzyAlgorithm.getClusterCount()];
		T dataObject;
		double objectiveFunctionValue = 0.0d;
		ArrayList<double[]> fuzzyResult = new ArrayList<double[]>(fuzzyAlgorithm.getDataCount());
		fuzzyAlgorithm.getAllFuzzyClusterAssignments(fuzzyResult);		
		ArrayList<Prototype<T>> prototypes = this.estimatePrototypeLocations(fuzzyAlgorithm.getDataSet(), fuzzyResult, vs);
		
		// Objective function value... sort of!
		for(j=0; j<fuzzyAlgorithm.getDataCount(); j++)
		{			
			dataObject = fuzzyAlgorithm.getDataSet().get(j).element;
			membershipValues = fuzzyResult.get(j);
			for(i=0; i<fuzzyAlgorithm.getClusterCount(); i++)
			{
				objectiveFunctionValue += membershipValues[i] * membershipValues[i] * dist.distanceSq(dataObject, prototypes.get(i).getPosition());
			}
		}

		
		double[][] pairwiseClusterDistances = null;		
		switch(this.clusterDistance)
		{
			case PAIRWISE_DATAOBJECTS:
				pairwiseClusterDistances = this.pariwise_dataObject_clusterDistance(fuzzyAlgorithm.getDataSet(), fuzzyResult, dist);
				break;

			case PAIRWISE_PROTOTYPES:
				pairwiseClusterDistances = this.pariwise_prototype_clusterDistance(prototypes, dist);
				
				break;
			default:
				return 0.0d;
		}
		
		
		double minClusterDistance = Double.POSITIVE_INFINITY;
		for(i=0; i<fuzzyAlgorithm.getClusterCount(); i++)
		{
			for(k=i+1; k<fuzzyAlgorithm.getClusterCount(); k++)
			{
				if(pairwiseClusterDistances[i][k] < minClusterDistance) minClusterDistance = pairwiseClusterDistances[i][k];
			}
		}
			
		return objectiveFunctionValue/(fuzzyAlgorithm.getDataCount()*minClusterDistance*minClusterDistance);
	}

	/**
	 * TODO: make version that does not need a vector space, i.e. make one that does not uses prototypes
	 * 
	 * @param fuzzyAlgorithm
	 * @param vs
	 * @param dist
	 * @return
	 */
	public double daviesBouldinIndex(FuzzyClusteringAlgorithm<T> fuzzyAlgorithm, VectorSpace<T> vs, Metric<T> dist)
	{
		if(fuzzyAlgorithm instanceof CrispNoiseClusteringAlgorithm || fuzzyAlgorithm instanceof FuzzyNoiseClusteringAlgorithm)
		{
			System.err.println("The Davies-Bouldin-Index is not valid for noise clustering");
			return 0.0d;
		}
//		System.out.print("Davies-Bouldin-Index ");
		
		int i, k;
		double doubleTMP;
		ArrayList<double[]> fuzzyResult = new ArrayList<double[]>(fuzzyAlgorithm.getDataCount());
		fuzzyAlgorithm.getAllFuzzyClusterAssignments(fuzzyResult);		
		ArrayList<Prototype<T>> prototypes = this.estimatePrototypeLocations(fuzzyAlgorithm.getDataSet(), fuzzyResult, vs);
		
		double[] clusterDiameters = null;
		double[][] pairwiseClusterDistances = null;		
		switch(this.clusterDistance)
		{
			case PAIRWISE_DATAOBJECTS:
				pairwiseClusterDistances = this.pariwise_dataObject_clusterDistance(fuzzyAlgorithm.getDataSet(), fuzzyResult, dist);
				break;

			case PAIRWISE_PROTOTYPES:				
				pairwiseClusterDistances = this.pariwise_prototype_clusterDistance(prototypes, dist);
				break;
				
			default:
				return 0.0d;
		}
		
		clusterDiameters = this.clusterDiameters(fuzzyAlgorithm.getDataSet(), fuzzyResult, prototypes, dist);
				
		
		// maximal diameter of cluster		
		double maxRatio = 0.0d;
		double maxRatioSum = 0.0d;
		
		for(i=0; i<fuzzyAlgorithm.getClusterCount(); i++)
		{
			maxRatio = 0.0d;
			for(k=0; k<fuzzyAlgorithm.getClusterCount(); k++)
			{
				if(i==k) continue;
				doubleTMP = (clusterDiameters[i] + clusterDiameters[k])/pairwiseClusterDistances[i][k];
				if(maxRatio < doubleTMP) maxRatio = doubleTMP;
			}
			maxRatioSum += maxRatio;
		}
		
		return maxRatioSum/fuzzyAlgorithm.getClusterCount();
	}
	
	/**
	 * TODO: make version that does not need a vector space, i.e. make one that does not uses prototypes
	 * 
	 * @param exampleSet
	 * @return
	 */
	public double bezdecSeperationIndex(FuzzyClusteringAlgorithm<T> fuzzyAlgorithm, VectorSpace<T> vs, Metric<T> dist)
	{
		if(fuzzyAlgorithm instanceof CrispNoiseClusteringAlgorithm || fuzzyAlgorithm instanceof FuzzyNoiseClusteringAlgorithm)
		{
			System.err.println("The Bezdec Seperation Index is not valid for noise clustering");
			return 0.0d;
		}
//		System.out.print("Bezdec Seperation Index ");

		int i, k;
		ArrayList<double[]> fuzzyResult = new ArrayList<double[]>(fuzzyAlgorithm.getDataCount());
		fuzzyAlgorithm.getAllFuzzyClusterAssignments(fuzzyResult);		
		ArrayList<Prototype<T>> prototypes = this.estimatePrototypeLocations(fuzzyAlgorithm.getDataSet(), fuzzyResult, vs);
		double[] clusterDiameters = null;
		double[][] pairwiseClusterDistances = null;		
		switch(this.clusterDistance)
		{
			case PAIRWISE_DATAOBJECTS:
				pairwiseClusterDistances = this.pariwise_dataObject_clusterDistance(fuzzyAlgorithm.getDataSet(), fuzzyResult, dist);
				break;

			case PAIRWISE_PROTOTYPES:				
				pairwiseClusterDistances = this.pariwise_prototype_clusterDistance(prototypes, dist);
				break;
				
			default:
				return 0.0d;
		}
		
		clusterDiameters = this.clusterDiameters(fuzzyAlgorithm.getDataSet(), fuzzyResult, prototypes, dist);
				
		double minClusterDistance = Double.POSITIVE_INFINITY;
		for(i=0; i<fuzzyAlgorithm.getClusterCount(); i++)
		{
			for(k=i+1; k<fuzzyAlgorithm.getClusterCount(); k++)
			{
				if(pairwiseClusterDistances[i][k] < minClusterDistance) minClusterDistance = pairwiseClusterDistances[i][k];
			}
		}
		
		// maximal diameter of cluster	
		double maxDiameter = Double.NEGATIVE_INFINITY;
		for(i=0; i<fuzzyAlgorithm.getClusterCount(); i++)
		{
			if(clusterDiameters[i] > maxDiameter) maxDiameter = clusterDiameters[i];
		}
		
		return minClusterDistance/maxDiameter;
	}
	
	/**
	 * @return
	 */
	public double partitionEntropy(FuzzyClusteringAlgorithm<T> fuzzyAlgorithm)
	{
//		System.out.println("Partition Entropy");
		
		double sum = 0.0d;
		int i;		

		ArrayList<double[]> fuzzyResult = new ArrayList<double[]>(fuzzyAlgorithm.getDataCount());
		fuzzyAlgorithm.getAllFuzzyClusterAssignments(fuzzyResult);
		
		for(double[] membershipValues : fuzzyResult)
		{			
			for(i=0; i<fuzzyAlgorithm.getClusterCount(); i++)
			{
				if(membershipValues[i] > 0.0d)	sum += membershipValues[i] * Math.log(membershipValues[i])/Math.log(2.0d);
			}
		}
		
		sum /= ((double)fuzzyAlgorithm.getDataCount());
		
		return -sum;
	}
	
	/**
	 * @return
	 */
	public double partitionCoefficient(FuzzyClusteringAlgorithm<T> fuzzyAlgorithm)
	{
		
		double sum = 0.0d;
		int i;		

		ArrayList<double[]> fuzzyResult = new ArrayList<double[]>(fuzzyAlgorithm.getDataCount());
		fuzzyAlgorithm.getAllFuzzyClusterAssignments(fuzzyResult);
		
		for(double[] membershipValues:fuzzyResult)
		{
			for(i=0; i<fuzzyAlgorithm.getClusterCount(); i++) sum += membershipValues[i] * membershipValues[i];
		}
		
		sum /= ((double)fuzzyAlgorithm.getDataCount());
		
		return sum;
	}
	
	/**
	 * @return
	 */
	public double nonFuzzynessIndex(FuzzyClusteringAlgorithm<T> fuzzyAlgorithm)
	{
//		System.out.println("Non Fuzziness Index");
		
		return 1.0d - (((double)fuzzyAlgorithm.getClusterCount())/(((double)fuzzyAlgorithm.getClusterCount()) - 1.0d))*(1.0d - this.partitionCoefficient(fuzzyAlgorithm));
	}
	

	public String clusterResultProperties(FuzzyClusteringAlgorithm<T> fuzzyAlgorithm, VectorSpace<T> vs, Metric<T> dist)
	{
		StringBuffer sb = new StringBuffer();
		StringBuffer nameB = new StringBuffer();
		String name = fuzzyAlgorithm.algorithmName();
		System.out.print("Analysing Result ");

		sb.append("\n----------------------------------------------------------------------------------------------------\n");
		for(int i=0; i<49-name.length()/2; i++)nameB.append("-"); 
		nameB.append(" "+name+" ");
		while(nameB.length() < 100) nameB.append("-");
		sb.append(nameB);
		sb.append("\n----------------------------------------------------------------------------------------------------");
		System.out.print(".");
		sb.append("\nNon Fuzzyness Index = ");
		sb.append(this.nonFuzzynessIndex(fuzzyAlgorithm));
		System.out.print(".");
		sb.append("\nPartition Entropy = ");
		sb.append(this.partitionEntropy(fuzzyAlgorithm));
		System.out.print(".");
		sb.append("\nBezdec Seperation Index = ");
		sb.append(this.bezdecSeperationIndex(fuzzyAlgorithm, vs, dist));
		System.out.print(".");
		sb.append("\nDavies-Bouldin-Index = ");
		sb.append(this.daviesBouldinIndex(fuzzyAlgorithm, vs, dist));
		System.out.print(".");
		sb.append("\nXie-Beni-Index = ");
		sb.append(this.xieBeniIndex(fuzzyAlgorithm, vs, dist));
		System.out.print(".");
		sb.append("\n----------------------------------------------------------------------------------------------------\n");
		System.out.println("");
		
		return sb.toString();
	}

	/**
	 * @return the clusterDistance
	 */
	public ClusterDistance getClusterDistance()
	{
		return this.clusterDistance;
	}

	/**
	 * @param clusterDistance the clusterDistance to set
	 */
	public void setClusterDistance(ClusterDistance clusterDistance)
	{
		this.clusterDistance = clusterDistance;
	}

	/**
	 * @return the clusterSize
	 */
	public ClusterSize getClusterSize()
	{
		return this.clusterSize;
	}

	/**
	 * @param clusterSize the clusterSize to set
	 */
	public void setClusterSize(ClusterSize clusterSize)
	{
		this.clusterSize = clusterSize;
	}
	
	
}
