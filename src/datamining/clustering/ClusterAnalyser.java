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
 * This class provides a collection of standard cluster validation indices.
 * Some of the indices compare data objects pairwise. That is quite some effort for large data
 * sets. Therefore, there are two options, <code>PAIRWISE_DATAOBJECTS</code> and <code>PAIRWISE_PROTOTYPES</code>.
 * The former (<code>PAIRWISE_DATAOBJECTS</code>) indicates, that the exact, but cost intensive method is used.
 * The latter calculates first the weighted mean in form of a virtual prototype of a cluster and calculates
 * the prototype positions for calculations.<br>
 * 
 * The second option, if the cluster size is calculated <code>LINEAR</code> or  <code>SQUARED</code> is not
 * a performance option but rather how the user likes it to be applied either one might work.<br>
 * 
 * For all algorithms in this class, a paper reference is provided. However, even though the original
 * papers are cited, the actual implementations are followed by the habilitation thesis of Christian Borgelt:<br>
 *
 * See: Borgelt, C. Prototype-based Classification and Clustering, Otto-von-Guericke-University of Magdeburg, Germany, 2005 http://www.borgelt.net/habil.html
 * 
 * @TODO: implement consistent cluster diameter calculations, especially some that do not require a vector space.
 *
 * @author Roland Winkler
 */
public class ClusterAnalyser<T>  implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 3198045630164304643L;

	/**
	 * This enum is used for specifying the type of algorithm that is used for pairwise cluster distance calculations.
	 *
	 * @author Roland Winkler
	 */
	public static enum ClusterDistance
	{
		
		/**
		 * States, that for cluster distance calculations, the data objects are used and no prototype is used.
		 */
		PAIRWISE_DATAOBJECTS,
		
		/** 
		 * Uses the prototype positions of pairs of clusters for calculating the distance in between the clusters. 
		 */
		PAIRWISE_PROTOTYPES;
	}
	
	/**
	 * This states the type of distance that is used for cluster diameter calculations,
	 * either the linear distance or the squared distance.
	 *
	 * @author Roland Winkler
	 */
	public static enum ClusterSize
	{
		
		/** Indicates that the linear distance is used. */
		LINEAR,
		
		/** Indicates that the squared distance is used. */
		SQUARED;
	}
	
	/** Stores with which algorithm the distance between pairs of clusters is calculated. */
	private ClusterDistance clusterDistance;
	
	/** Stored with which distance function the diameter of clusters is calculations. */
	private ClusterSize clusterSize;
	
	/**
	 * The standard constructor, setting the cluster distance to <code>PAIRWISE_DATAOBJECTS</code> and
	 * the cluster size to <code>LINEAR</code>.
	 */
	public ClusterAnalyser()
	{
		this.clusterDistance = ClusterDistance.PAIRWISE_DATAOBJECTS;
		this.clusterSize = ClusterSize.LINEAR;
	}

	/**
	 * Creates a new instance, using the specified methods for calculations.
	 * 
	 * @param cd Specifies the pairwise cluster distance algorithm
	 * @param cs Specifies the cluster size distance function
	 */
	public ClusterAnalyser(ClusterDistance cd, ClusterSize cs)
	{
		this.clusterDistance = cd;
		this.clusterSize = cs;
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
	 * @return A distance matrix containing the pairwise distances of clusters.
	 */
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
			dataObjectJ = dataSet.get(j).x;
			membershipValuesJ = fuzzyResult.get(j);

			for(l=j+1; l<dataSet.size(); l++)
			{
				dataObjectL = dataSet.get(l).x;
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
	 * @param dist The distance function 
	 * @return A distance matrix containing the pairwise distances of clusters.
	 */
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
	public double[] clusterDiameters(List<IndexedDataObject<T>> dataSet, List<double[]> fuzzyResult, List<Prototype<T>> prototypes, Metric<T> dist)
	{
		int i, j;
		T dataObject;
		
		double[] clusterDiameters = new double[prototypes.size()];	
		double[] membershipValueSum = new double[prototypes.size()];
		double[] membershipValues;
		for(j=0; j<dataSet.size(); j++)
		{
			dataObject = dataSet.get(j).x;
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
				vs.copy(tmpX, dataSet.get(j).x);
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
	
	/**
	 * Calculates the sum of membership values for all clusters.
	 * 
	 * Complexity: O(n*c)
	 * 
	 * @param fuzzyResult The fuzzy clustering result.
	 * @return The sum of membership values as double array with each entry corresponding to one cluster.
	 */
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
	 * Calculates the Xie - Beni index. The complexity depends on how the pairwise distances of clusters and the
	 * diameter of one cluster is calculated.<br>
	 * 
	 * Complexity for pairwise prototype calculations: O(n*c + c^2). 
	 * Complexity for pairwise data object calculations: O(n^2*c^2), 
	 *  with n being the number of data objects and c being the number of clusters.<br> 
	 * 
	 * See paper: X.L. Xie and G.A. Beni. Validity Measure for Fuzzy Clustering.IEEE Transactions on Pattern Analysis and Machine Intel-ligence (PAMI) 3(8):841–846. IEEE Press, Piscataway, NJ, USA 1991. Reprinted in [Bezdek and Pal 1992], 219–226
	 * 
	 * @param fuzzyAlgorithm The fuzzy clustering algorithm containing the clustering result
	 * @param vs The vector space used for the clustering process
	 * @param dist The distance metric.
	 * @return The Xie - Beni index for the specified clustering result.
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
			dataObject = fuzzyAlgorithm.getDataSet().get(j).x;
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
	 * Calculates the Davies - Bouldin index. The complexity depends on how the pairwise distances of clusters and the
	 * diameter of one cluster is calculated.<br>
	 * 
	 * Complexity for pairwise prototype calculations: O(n*c + c^2).
	 * Complexity for pairwise data object calculations: O(n^2*c^2),
	 *  with n being the number of data objects and c being the number of clusters.<br>    
	 * 
	 * See paper: D.L. Davies and D.W. Bouldin. A Cluster Separation Measure.IEEE Trans. on Pattern Analysis and Machine Intelligence (PAMI) 1(4):224–227. IEEE Press, Piscataway, NJ, USA 1979
	 * 
	 * @param fuzzyAlgorithm The fuzzy clustering algorithm containing the clustering result
	 * @param vs The vector space used for the clustering process
	 * @param dist The distance metric.
	 * @return The Davies - Bouldin index for the specified clustering result.
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
	 * Calculates the Bezdec separation index. It is actually a more robust form of Dunns separation index, but
	 * since bezdec made the more importent alterations and Dunns index is not valid for fuzzy membership values,
	 * it is called Bezdec separation index here. The complexity depends on how the pairwise distances of clusters and the
	 * diameter of one cluster is calculated.<br>
	 * 
	 * Complexity for pairwise prototype calculations: O(n*c + c^2).
	 * Complexity for pairwise data object calculations: O(n^2*c^2),
	 *  with n being the number of data objects and c being the number of clusters. <br>   
	 * 
	 * See paper: J.C. Bezdek, W.Q. Li, Y. Attikiouzel, and M. Wind-ham. A Geometric Approach to Cluster Validity for Normal Mixtures. Soft Computing 1(4):166–179. Springer-Verlag, Heidelberg, Germany 1997<br>
	 * See paper: J.C. Dunn. A Fuzzy Relative of the ISODATA Process and Its Use in Detecting Compact Well-Separated Clusters.Journal of Cyber-netics3(3):32–57. American Society for Cybernetics, Washington, DC, USA 1973 Reprinted in [Bezdek and Pal 1992], 82–101<br>
	 * 
	 * @param fuzzyAlgorithm The fuzzy clustering algorithm containing the clustering result
	 * @param vs The vector space used for the clustering process
	 * @param dist The distance metric.
	 * @return The Bezdec separation index (fuzzy Dunn separation index) for the specified clustering result.
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
	 * Calculates the partition entropie of the fuzzy clustering result.<br>
	 * 
	 * The complexity of the function is in O(n*c) with n being the number of data objects and c being the number of clusters.<br> 
	 * 
	 * See paper: J.C. Bezdek.Pattern Recognition with Fuzzy Objective Func-tion Algorithms. Plenum Press, New York, NY, USA 1981
	 *
	 * @param fuzzyAlgorithm  The fuzzy clustering algorithm containing the clustering result
	 * @return The partition coefficient of the specified clustering result.
	 */
	public double partitionEntropy(FuzzyClusteringAlgorithm<T> fuzzyAlgorithm)
	{
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
	 * Calculates the partition coefficient of the fuzzy clustering result.<br>
	 * 
	 * The complexity of the function is in O(n*c) with n being the number of data objects and c being the number of clusters.<br> 
	 * 
	 * See paper: J.C. Bezdek.Pattern Recognition with Fuzzy Objective Func-tion Algorithms. Plenum Press, New York, NY, USA 1981
	 *
	 * @param fuzzyAlgorithm  The fuzzy clustering algorithm containing the clustering result
	 * @return The partition coefficient of the specified clustering result.
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
	 * Calculates the non fuzzines index (normalised partition coefficient) of the fuzzy clustering result.<br>
	 * 
	 * The complexity of the function is in O(n*c) with n being the number of data objects and c being the number of clusters.<br> 
	 * 
	 * See paper: E. Backer and A.K. Jain. A Clustering Performance Measure based on Fuzzy Set Decomposition.IEEE Trans. on Pattern Analysis and Machine Intelligence (PAMI)3(1):66–74. IEEE Press, Piscataway, NJ, USA 1981<br>
	 *
	 * @param fuzzyAlgorithm  The fuzzy clustering algorithm containing the clustering result
	 * @return The partition coefficient of the specified clustering result.
	 */
	public double nonFuzzynessIndex(FuzzyClusteringAlgorithm<T> fuzzyAlgorithm)
	{
		return 1.0d - (((double)fuzzyAlgorithm.getClusterCount())/(((double)fuzzyAlgorithm.getClusterCount()) - 1.0d))*(1.0d - this.partitionCoefficient(fuzzyAlgorithm));
	}
	

	/**
	 * Creates a report of all indices and returns the string. Depending on the complexity of the data set
	 * and the calculation methods, this may take some time. 
	 * 
	 * @param fuzzyAlgorithm The fuzzy clustering algorithm containing the clustering result
	 * @param vs The vector space used for the clustering process
	 * @param dist The distance metric.
	 * @return The string holding the report.
	 */
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
	 * Returns the type of distance calculation algorithm for the pairwise cluster distances.
	 * 
	 * @return the cluster distance indicator.
	 */
	public ClusterDistance getClusterDistance()
	{
		return this.clusterDistance;
	}

	/**
	 * Sets the type of distance calculation algorithm for the pairwise cluster distances.
	 * 
	 * @param clusterDistance the cluster distance indicator to set
	 */
	public void setClusterDistance(ClusterDistance clusterDistance)
	{
		this.clusterDistance = clusterDistance;
	}

	/**
	 * Returns the type of distance function for the diameter calculation of a cluster.
	 * 
	 * @return the cluster diameter indicator.
	 */
	public ClusterSize getClusterSize()
	{
		return this.clusterSize;
	}

	/**
	 * Sets the type of distance function for the diameter calculation of a cluster.
	 * 
	 * @param clusterSize the cluster diameter indicator to set
	 */
	public void setClusterSize(ClusterSize clusterSize)
	{
		this.clusterSize = clusterSize;
	}
	
	
}
