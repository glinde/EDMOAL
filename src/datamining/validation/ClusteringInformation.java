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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.NoInitialContextException;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.Centroid;
import datamining.clustering.protoype.Prototype;
import datamining.resultProviders.FuzzyClusteringProvider;
import datamining.resultProviders.FuzzyNoiseClusteringProvider;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ClusteringInformation<T>
{	
	protected double[] clusterDiameters;
	
	protected double[][] clusterDistances;
	
	protected int clusterCount;
	

	protected IndexedDataSet<T> dataSet;
	
	protected Metric<T> metric;
	
	protected List<Prototype<T>> prototypes;
	
	protected FuzzyClusteringProvider<T> fuzzyClusteringProvider;
	
	protected List<double[]> fuzzyClusteringResult;
	
	protected double[] noiseClusterMembershipValues;
	
	protected int[] crispClusteringResult;

	
	protected int[] trueClusteringResult;
	
	
	/**	 */
	public ClusteringInformation(int clusterCount)
	{
		this.clusterCount = clusterCount;
		this.clusterDiameters = null;
		this.clusterDistances = null;

		this.prototypes = null;
		this.fuzzyClusteringProvider = null;
		this.fuzzyClusteringResult = null;
		this.noiseClusterMembershipValues = null;
		this.crispClusteringResult = null;

		this.trueClusteringResult = null;
	}
	
	/**
	 * Calculates the pairwise distance of clusters using the position of the data objects and the
	 * the fuzzy membership value. The result is a distance matrix, each entry corresponding to
	 * a pair of clusters. The equation for calculating the distance between cluster A and Cluster B is as follows:<br>
	 * 
	 * cluster_dist(A, B) = \frac{1}{(\sum_{i=0}^n u_{Ai})*(\sum_{i=0}^n u_{Bi})} \sum_{i=0}^n \sum_{j=0}^n u_{Ai} u_{Bj} dist(x_i, x_j)<br>
	 * 
	 * The complexity of this method is therefore: O(n^2 * c^2) with n being the number of data objects and c being the number of clusters. 
	 * 
	 * @param dataSet The data set.
	 * @param fuzzyResult The fuzzy clustering result.
	 * @param dist The distance function
	 */
	public void calculateClusterDistances_Fuzzy()
	{
		int i, j, k, l;
		
		this.checkFuzzyClusteringResult();
		this.checkDataSet();
		this.checkMetric();
		
		this.clusterDistances = new double[this.clusterCount][this.clusterCount];
		
		T dataObjectJ;
		T dataObjectL;
		double[] membershipValuesJ;
		double[] membershipValuesL;
		double doubleTMP;
		
		double[] membershipValueSum = this.membershipValueSums();
		
		for(j=0; j<this.dataSet.size(); j++)
		{
			dataObjectJ = this.dataSet.get(j).x;
			membershipValuesJ = this.fuzzyClusteringResult.get(j);

			for(l=j+1; l<this.dataSet.size(); l++)
			{
				dataObjectL = this.dataSet.get(l).x;
				membershipValuesL = this.fuzzyClusteringResult.get(l);
				doubleTMP = metric.distanceSq(dataObjectJ, dataObjectL);
				
				if(doubleTMP == 0.0d) continue;
				
				for(i=0; i<this.clusterCount; i++)
				{
					for(k=i+1; k<this.clusterCount; k++)
					{
						this.clusterDistances[i][k] += 2.0d * membershipValuesJ[i] * membershipValuesL[k] * doubleTMP;
					}
				}
			}
		}
		
		for(i=0; i<this.clusterCount; i++)
		{
			for(k=i+1; k<this.clusterCount; k++)
			{
				if(membershipValueSum[i] * membershipValueSum[k] == 0.0d) 
				{
					this.clusterDistances[i][k] = Double.POSITIVE_INFINITY;
					this.clusterDistances[k][i] = Double.POSITIVE_INFINITY;
				}
				else
				{
					this.clusterDistances[i][k] /= membershipValueSum[i] * membershipValueSum[k];
					this.clusterDistances[i][k] = Math.sqrt(this.clusterDistances[i][k]);
					this.clusterDistances[k][i] = this.clusterDistances[i][k];
				}
			}
		}
	}

	public void calculateClusterDistances_Crisp()
	{
		int i, j, k, l;

		this.checkCrispClusteringResult();
		this.checkDataSet();
		this.checkMetric();
		
		this.clusterDistances = new double[this.clusterCount][this.clusterCount];

		ArrayList<ArrayList<T>> clusters = new ArrayList<ArrayList<T>>(this.clusterCount);
		
		for(i=0; i<this.clusterCount; i++)
		{
			clusters.add(new ArrayList<T>(this.dataSet.size()/this.clusterCount));
		}
		for(j=0; j<this.dataSet.size(); j++)
		{
			if(this.crispClusteringResult[j] >= 0) clusters.get(this.crispClusteringResult[j]).add(this.dataSet.get(j).x);
		}
		
		
		for(j=0; j<this.dataSet.size(); j++)
		{
			if(this.crispClusteringResult[j] < 0) continue;
			
			for(l=j+1; l<this.dataSet.size(); l++)
			{
				if(this.crispClusteringResult[l] < 0) continue;
				
				this.clusterDistances[this.crispClusteringResult[j]][this.crispClusteringResult[l]] +=  2.0d * this.metric.distanceSq(dataSet.get(j).x, this.dataSet.get(l).x);
			}
		}
		
		for(i=0; i<this.clusterCount; i++)
		{
			for(k=i+1; k<this.clusterCount; k++)
			{
				if(clusters.get(i).size() == 0 || clusters.get(k).size() == 0) 
				{
					this.clusterDistances[i][k] = Double.POSITIVE_INFINITY;
					this.clusterDistances[k][i] = Double.POSITIVE_INFINITY;
				}
				else
				{
					this.clusterDistances[i][k] /= clusters.get(i).size() * clusters.get(k).size();
					this.clusterDistances[i][k] = Math.sqrt(this.clusterDistances[i][k]);
					this.clusterDistances[k][i] = this.clusterDistances[i][k];
				}
			}
		}
	}

	/**
	 * Calculates the pairwise distance of clusters using the position of the prototypes.
	 * The result is a distance matrix, each entry corresponding to a pair of clusters.
	 * The equation for calculating the distance between cluster A and Cluster B is as follows:<br>
	 * 
	 * cluster_dist(A, B) = dist(y_A, y_B)<br>
	 * 
	 * The complexity of this method is therefore: O(c^2) with c being the number of clusters. 
	 * 
	 * @param prototypes The prototypes for the corresponding clusters
	 */
	public void calculateClusterDistances_Prototype()
	{
		int i, k;

		this.checkPrototypes();
		this.checkMetric();
		
		this.clusterDistances = new double[this.clusterCount][this.clusterCount];

		double doubleTMP;		
				
		for(i=0; i<this.clusterCount; i++)
		{
			for(k=i+1; k<this.clusterCount; k++)
			{
				doubleTMP = this.metric.distance(this.prototypes.get(i).getPosition(), this.prototypes.get(k).getPosition());
				this.clusterDistances[i][k] = doubleTMP;
				this.clusterDistances[k][i] = doubleTMP;
			}
		}
	}

	/**
	 * Calculates the diameter of a cluster, using the distance of the data objects within the cluster.
	 * The equation for calculating the diameter of cluster A is:
	 * 
	 * cluster_diameter(A) = 2 \frac{\sum_{i=0}^n u_{Ai} dist(xcluster di_i, y_A)}{\sum_{i=0}^n u_{Ai}} <br>
	 * 
	 * Hence the complexity for calculating this value is in O(n) with n being the number of data objects.
	 *  
	 * @param dataSet The data set.
	 * @param fuzzyResult The fuzzy clustering result.
	 * @param prototypes The location of the prototypes
	 * @param dist The distance function 
	 */
	public void calculateClusterDiameters_Fuzzy()
	{
		int i, j, l;

		this.checkFuzzyClusteringResult();
		this.checkDataSet();
		this.checkMetric();
		
		this.clusterDiameters = new double[this.clusterCount];
		
		double[] membershipValueSums = new double[this.clusterCount];
		
		for(j=0; j<this.dataSet.size(); j++)
		{
			for(i=0; i<this.clusterCount; i++)
			{
				membershipValueSums[i] += this.fuzzyClusteringResult.get(j)[i];
			}
			
			for(l=j+1; l<this.dataSet.size(); l++)
			{
				for(i=0; i<this.clusterCount; i++)
				{
					this.clusterDiameters[i] += 2.0d*this.fuzzyClusteringResult.get(j)[i] * this.fuzzyClusteringResult.get(l)[i]* this.metric.distanceSq(this.dataSet.get(j).x, this.dataSet.get(l).x);
				}
			}
		}
		
		for(i=0; i<this.clusterCount; i++)
		{
			this.clusterDiameters[i] = Math.sqrt(this.clusterDiameters[i])/membershipValueSums[i];
		}
	}
	
	/**
	 * Calculates the diameter of a cluster, using the distance of the data objects to the prototype of the cluster. The
	 * equation for calculating the diameter of cluster A is:
	 * 
	 * cluster_diameter(A) = 2 \frac{\sum_{i=0}^n u_{Ai} dist(x_i, y_A)}{\sum_{i=0}^n u_{Ai}} <br>
	 * 
	 * Hence the complexity for calculating this value is in O(n) with n being the number of data objects.
	 *  
	 * @param dataSet The data set.
	 * @param fuzzyResult The fuzzy clustering result.
	 * @param prototypes The location of the prototypes
	 * @param dist The distance function 
	 * @return A diameter of the cluster.
	 */
	public void calculateClusterDiameters_Fuzzy_Prototype()
	{
		int i, j;

		this.checkFuzzyClusteringProvider_FuzzyClusteringResult();	
		this.checkDataSet();
		this.checkPrototypes();
		this.checkMetric();
		
		this.clusterDiameters = new double[this.clusterCount];
		
		boolean fuzzyResultAvailable = this.fuzzyClusteringResult != null;
		
		double[] membershipValueSum = new double[this.clusterCount];
		double[] membershipValues;
		for(j=0; j<this.dataSet.size(); j++)
		{
			if(fuzzyResultAvailable) membershipValues = this.fuzzyClusteringResult.get(j);
			else membershipValues = this.fuzzyClusteringProvider.getFuzzyAssignmentsOf(this.dataSet.get(j));
			
			for(i=0; i<this.clusterCount; i++)
			{
				this.clusterDiameters[i] += membershipValues[i] * this.metric.distanceSq(this.dataSet.get(j).x, this.prototypes.get(i).getPosition());

						
				membershipValueSum[i] += membershipValues[i];
			}
		}
		for(i=0; i<this.clusterCount; i++)
		{
			this.clusterDiameters[i] = Math.sqrt(this.clusterDiameters[i]/membershipValueSum[i]);
		}
	}
		

	public void calculateClusterDiameters_Crisp_Prototype()
	{
		int i, j;

		this.checkCrispClusteringResult();	
		this.checkDataSet();
		this.checkPrototypes();
		this.checkMetric();
		
		this.clusterDiameters = new double[this.clusterCount];
		
		int[] clusterObjectCount = new int[this.clusterCount];
		
		for(j=0; j<dataSet.size(); j++)
		{
			if(this.crispClusteringResult[j] < 0) continue; 
			this.clusterDiameters[this.crispClusteringResult[j]] += this.metric.distanceSq(dataSet.get(j).x, this.prototypes.get(this.crispClusteringResult[j]).getPosition());
			clusterObjectCount[this.crispClusteringResult[j]]++;
		}
		
		for(i=0; i<this.clusterCount; i++)
		{
			this.clusterDiameters[i] /= Math.sqrt(this.clusterDiameters[i]/clusterObjectCount[i]); 
		}
	}
	
	
	public void calculateClusterDiameters_Crisp()
	{
		int i, j, k;

		this.checkCrispClusteringResult();	
		this.checkDataSet();
		this.checkMetric();
		
		this.clusterDiameters = new double[this.clusterCount];
		
		ArrayList<ArrayList<T>> clusters = new ArrayList<ArrayList<T>>(this.clusterCount);
		
		for(i=0; i<this.clusterCount; i++)
		{
			clusters.add(new ArrayList<T>(this.dataSet.size()/this.clusterCount));
		}
		for(j=0; j<this.dataSet.size(); j++)
		{
			if(this.crispClusteringResult[j] >= 0) clusters.get(this.crispClusteringResult[j]).add(this.dataSet.get(j).x);
		}
		
		for(i=0; i<this.clusterCount; i++)
		{
			for(j=0; j<clusters.get(i).size(); i++)
			{
				for(k=j+1; k<clusters.get(i).size(); k++)
				{
					this.clusterDiameters[i] += 2.0d*this.metric.distanceSq(clusters.get(i).get(j), clusters.get(i).get(k));
				}
			}

			this.clusterDiameters[i] = Math.sqrt(this.clusterDiameters[i])/clusters.get(i).size();
		}
	}
	
	/**
	 * Not all clustering algorithms are based on prototypes, therefore, some algorithms cant provide their location as
	 * a distinctive characteristic. Most of the indices provided in this class require a prototype location. Therefore,
	 * it can be estimated using the fuzzy mean of data objects of one cluster. This is basically the result that can be expected from
	 * fuzzy c means if the membership values are fixed and the prototype positions are calculated. The equation for calculating
	 * the prototype position (fuzzy mean) of cluster A is as follows:<br>
	 * 
	 * y_A = \frac{\sum_{i=0}^n u_{Ai} x_i}{\sum_{i=0}^n u_{Ai}} <br>
	 * 
	 * Since this calue is calculated for all clusters, the complexity of this function is in O(n*c)
	 *  with n being the number of data objects and c being the number of clusters. 
	 * 
	 * @param dataSet The data set.
	 * @param fuzzyResult The fuzzy clustering result.
	 * @param vs A vector space.
	 * @return The estimated prototype positions (fuzzy mean) of the clusters. 
	 */
	public ArrayList<Prototype<T>> estimatePrototypeLocations_Fuzzy(VectorSpace<T> vs)
	{
		int i, j;

		this.checkFuzzyClusteringProvider_FuzzyClusteringResult();	
		this.checkDataSet();
		
		
		T tmpX = vs.getNewAddNeutralElement();	
		ArrayList<T> prototypeLocations = new ArrayList<T>();
		for(i=0; i<this.clusterCount; i++) prototypeLocations.add(vs.getNewAddNeutralElement());
		double[] membershipValues = null; 
		double[] membershipValueSum = new double[this.clusterCount];

		boolean fuzzyResultAvailable = this.fuzzyClusteringResult != null;
		
		
		for(j=0; j<this.dataSet.size(); j++)
		{
			if(fuzzyResultAvailable) membershipValues = this.fuzzyClusteringResult.get(j);
			else membershipValues = this.fuzzyClusteringProvider.getFuzzyAssignmentsOf(this.dataSet.get(j));
			
			for(i=0; i<this.clusterCount; i++)
			{
				vs.copy(tmpX, this.dataSet.get(j).x);
				vs.mul(tmpX, membershipValues[i]);
				vs.add(prototypeLocations.get(i), tmpX);
				
				membershipValueSum[i] += membershipValues[i];
			}
		}
		for(i=0; i<this.clusterCount; i++)
		{
			if(membershipValueSum[i] > 0.0)
			{
				vs.mul(prototypeLocations.get(i), membershipValueSum[i]);
			}
		}
		
		ArrayList<Prototype<T>> prototypes = new ArrayList<Prototype<T>>(this.clusterCount);
		for(T d:prototypeLocations) prototypes.add(new Centroid<T>(vs, d));
		
		return prototypes;
	}
	
	/**
	 * Calculates the sum of membership values for all clusters.
	 * 
	 * Complexity: O(n*c)
	 * 
	 * @param fuzzyResult The fuzzy clustering result.
	 * @return The sum of membership values as double array with each entry corresponding to one cluster.
	 */
	public double[] membershipValueSums()
	{
		int i, j;
		
		this.checkFuzzyClusteringProvider_FuzzyClusteringResult();	
		
		double[] sum = new double[this.clusterCount];

		if(this.fuzzyClusteringResult == null) return this.fuzzyClusteringProvider.getFuzzyAssignmentSums();
				
		for(j=0; j<this.fuzzyClusteringResult.size(); j++)
		{			
			for(i=0; i<this.clusterCount; i++)
			{
				sum[i] += this.fuzzyClusteringResult.get(j)[i]; 
			}
		}
		
		return sum;
	}
	
	public int[] defuzzify()
	{
		this.checkFuzzyClusteringProvider_FuzzyClusteringResult();
		
		boolean fuzzyResultAvailable = this.fuzzyClusteringResult != null;
		double[] noiseMembershipValues = null;
		if(this.noiseClusterMembershipValues != null) noiseMembershipValues  = this.noiseClusterMembershipValues;
		else if(this.fuzzyClusteringProvider instanceof FuzzyNoiseClusteringProvider) noiseMembershipValues = ((FuzzyNoiseClusteringProvider<T>)this.fuzzyClusteringProvider).getFuzzyNoiseAssignments();  
		
		int dataCount = fuzzyResultAvailable? this.fuzzyClusteringResult.size():this.fuzzyClusteringProvider.getDataSet().size();
		int[] crispResult = new int[dataCount];
		
		double[] membershipValues;
		
		int i=0, k;
		double max=0.0d;
		
		for(int j=0; j<dataCount; j++)
		{
			max=0.0d;

			if(fuzzyResultAvailable) membershipValues = this.fuzzyClusteringResult.get(j);
			else membershipValues = this.fuzzyClusteringProvider.getFuzzyAssignmentsOf(this.dataSet.get(j));
			
			for(k=0; k<this.clusterCount; k++)
			{
				if(membershipValues[k] > max)
				{
					max = membershipValues[k];
					crispResult[i] = k;
					
					if(max > 0.5d) break;
				}
			}
			
			if(noiseMembershipValues != null && this.noiseClusterMembershipValues[i] > max) crispResult[i] = -1;
			
			i++;
		}
		
		return crispResult;
	}
	
	
	public int getDataCount()
	{
		if(this.dataSet == null && this.fuzzyClusteringResult == null && this.noiseClusterMembershipValues == null && this.crispClusteringResult == null && this.fuzzyClusteringProvider == null)
			throw new NotEnoughInformationException("Not enough information to determine the number of data objects.");
		
		if(this.dataSet != null) return this.dataSet.size();
		if(this.fuzzyClusteringResult == null) return this.fuzzyClusteringResult.size();
		if(this.noiseClusterMembershipValues == null) return this.noiseClusterMembershipValues.length;
		if(this.crispClusteringResult == null) return this.crispClusteringResult.length;
		if(this.fuzzyClusteringProvider == null) return this.fuzzyClusteringProvider.getDataSet().size();

		return 0;
	}

	public void checkDataSet()
	{
		if(this.dataSet == null) throw new NotEnoughInformationException("Data set not available.");
	}

	public void checkMetric()
	{
		if(this.metric == null) throw new NotEnoughInformationException("Metric not available.");
	}

	public void checkPrototypes()
	{
		if(this.prototypes == null) throw new NotEnoughInformationException("Prototypes not available.");
	}

	public void checkFuzzyClusteringProvider_FuzzyClusteringResult()
	{
		if(this.fuzzyClusteringResult == null && this.fuzzyClusteringProvider == null) throw new NotEnoughInformationException("Neither fuzzy clustering result nor fuzzy clustering provider are available. At least one of the two is required.");
	}
	
	public void checkClusteringAvailable()
	{
		if(this.fuzzyClusteringResult == null && this.fuzzyClusteringProvider == null && this.crispClusteringResult == null) throw new NotEnoughInformationException("Neither the fuzzy clustering result, the fuzzy clustering provider or the crisp clustering are available. At least one of the three is required.");
	}

	public void checkFuzzyClusteringResult()
	{
		if(this.fuzzyClusteringResult == null) throw new NotEnoughInformationException("Fuzzy clustering result not available.");
	}
	
	public void checkNoiseClusterMembershipValues()
	{
		if(this.noiseClusterMembershipValues == null) throw new NotEnoughInformationException("Noise cluster membership values not available.");
	}

	public void checkCrispClusteringResult()
	{
		if(this.crispClusteringResult == null) throw new NotEnoughInformationException("Crisp clustering result not available.");
	}

	public void checkTrueClusteringResult()
	{
		if(this.trueClusteringResult == null) throw new NotEnoughInformationException("True clustering result not available.");
	}
		
	public void checkClusterDiameters()
	{
		if(this.clusterDiameters == null) throw new NotEnoughInformationException("Cluster diameters not available.");
	}

	public void checkClusterDistances()
	{
		if(this.clusterDistances == null) throw new NotEnoughInformationException("Cluster distances not available.");
	}
	
	/**
	 * @return the clusterDiameters
	 */
	public double[] getClusterDiameters()
	{
		return this.clusterDiameters;
	}

	/**
	 * @param clusterDiameters the clusterDiameters to set
	 */
	public void setClusterDiameters(double[] clusterDiameters)
	{
		this.clusterDiameters = clusterDiameters;
	}


	/**
	 * @return the clusterDistances
	 */
	public double[][] getClusterDistances()
	{
		return this.clusterDistances;
	}

	/**
	 * @param clusterDistances the clusterDistances to set
	 */
	public void setClusterDistances(double[][] clusterDistances)
	{
		this.clusterDistances = clusterDistances;
	}

	/**
	 * @return the clusterCount
	 */
	public int getClusterCount()
	{
		return this.clusterCount;
	}

	/**
	 * @return the dataSet
	 */
	public IndexedDataSet<T> getDataSet()
	{
		return this.dataSet;
	}

	/**
	 * @param dataSet the dataSet to set
	 */
	public void setDataSet(IndexedDataSet<T> dataSet)
	{
		this.dataSet = dataSet;
	}

	/**
	 * @return the metric
	 */
	public Metric<T> getMetric()
	{
		return this.metric;
	}

	/**
	 * @param metric the metric to set
	 */
	public void setMetric(Metric<T> metric)
	{
		this.metric = metric;
	}

	/**
	 * @return the prototypes
	 */
	public List<Prototype<T>> getPrototypes()
	{
		return this.prototypes;
	}

	/**
	 * @param prototypes the prototypes to set
	 */
	public void setPrototypes(List<Prototype<T>> prototypes)
	{
		this.prototypes = prototypes;
	}

	/**
	 * @return the fuzzyClusteringProvider
	 */
	public FuzzyClusteringProvider<T> getFuzzyClusteringProvider()
	{
		return this.fuzzyClusteringProvider;
	}

	/**
	 * @param fuzzyClusteringProvider the fuzzyClusteringProvider to set
	 */
	public void setFuzzyClusteringProvider(FuzzyClusteringProvider<T> fuzzyClusteringProvider)
	{
		this.fuzzyClusteringProvider = fuzzyClusteringProvider;
	}

	/**
	 * @return the fuzzyClusteringResult
	 */
	public List<double[]> getFuzzyClusteringResult()
	{
		return this.fuzzyClusteringResult;
	}

	/**
	 * @param fuzzyClusteringResult the fuzzyClusteringResult to set
	 */
	public void setFuzzyClusteringResult(List<double[]> fuzzyClusteringResult)
	{
		this.fuzzyClusteringResult = fuzzyClusteringResult;
	}
	
	/**
	 * @return the noiseClusterMembershipValues
	 */
	public double[] getNoiseClusterMembershipValues()
	{
		return this.noiseClusterMembershipValues;
	}

	/**
	 * @param noiseClusterMembershipValues the noiseClusterMembershipValues to set
	 */
	public void setNoiseClusterMembershipValues(double[] noiseClusterMembershipValues)
	{
		this.noiseClusterMembershipValues = noiseClusterMembershipValues;
	}

	/**
	 * @return the crispClusteringResult
	 */
	public int[] getCrispClusteringResult()
	{
		return this.crispClusteringResult;
	}

	/**
	 * @param crispClusteringResult the crispClusteringResult to set
	 */
	public void setCrispClusteringResult(int[] crispClusteringResult)
	{
		this.crispClusteringResult = crispClusteringResult;
	}

	/**
	 * @return the trueClusteringResult
	 */
	public int[] getTrueClusteringResult()
	{
		return this.trueClusteringResult;
	}

	/**
	 * @param trueClusteringResult the trueClusteringResult to set
	 */
	public void setTrueClusteringResult(int[] trueClusteringResult)
	{
		this.trueClusteringResult = trueClusteringResult;
	}
}
