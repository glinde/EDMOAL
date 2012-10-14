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
package dataMiningTestTrack.experiments.snFCM;

import java.util.ArrayList;
import java.util.List;

import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.IndexedDataObject;
import data.set.IndexedDataSet;
import datamining.gradient.functions.AbstractObjectiveFunction;
import datamining.gradient.parameter.PositionListParameter;
import datamining.resultProviders.FuzzyClusteringProvider;
import etc.MyMath;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ScaledNormFuzzyCMeansObjectiveFunction extends AbstractObjectiveFunction<double[], PositionListParameter<double[]>> implements FuzzyClusteringProvider<double[]>
{
	/**
	 * The fuzzifier. It specifies how soft the membership values are going to be calculated. if the
	 * value is 1, the algorithm is identical to crisp clustering and for values going to infinity, it is complete soft clustering.
	 * A useful value is around 2.<br>
	 *
	 * Range of values: <code>fuzzifier</code> > 1
	 */
	protected double fuzzifier;

	/**
	 * The the metric that is used here can hold a parameter<br>
	 *
	 * Range of values: <code>normExponent</code> >= 1
	 */
	protected double normExponent;	
		
	/**
	 * A metric for measuring the distance between objects of type <code>T</code>, that can be data objects
	 * or other locations in the feature space.
	 */
	protected final DAScaledNormMetric metric;

	/**
	 * @param dataSet
	 * @param normExponent
	 */
	public ScaledNormFuzzyCMeansObjectiveFunction(IndexedDataSet<double[]> dataSet, double normExponent)
	{
		super(dataSet, null);
		this.fuzzifier = 2.0d;
		this.normExponent = normExponent;
		this.metric = new DAScaledNormMetric(normExponent);
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#functionValue(data.set.IndexedDataSet, java.lang.Object)
	 */
	@Override
	public double functionValue()
	{
		int i, j; 
		// i: index for clusters
		// j: index for data objects
		
		double objectiveFunctionValue = 0.0d;
		
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances					= new double[this.parameter.getPositionCount()];
		double[] distances						= new double[this.parameter.getPositionCount()];
		boolean zeroDistance = false;
		
						
		for(j=0; j < this.getDataCount(); j++)
		{				
			zeroDistance = false;
			distanceSum = 0.0d;
			for(i=0; i<this.parameter.getPositionCount(); i++)
			{
				doubleTMP = this.metric.distance(this.data.get(j).x, this.parameter.getPosition(i));
				if(doubleTMP <= 0.0d)
				{
					zeroDistance = true;
				}
				else
				{ 
					distances[i] = doubleTMP;
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
			if(zeroDistance) continue;

			// don't check for distance sum to be zero.. that would just be ridiculous!!
			for(i=0; i<this.parameter.getPositionCount(); i++)
			{
				doubleTMP = fuzzDistances[i] / distanceSum;
								
				objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * distances[i];
			}
		}
	
		return objectiveFunctionValue* this.parameter.getPositionCount()/this.getDataCount();
	}


	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object)
	 */
	@Override
	public PositionListParameter<double[]> gradient()
	{
		PositionListParameter<double[]> grad = this.parameter.clone(new DAEuclideanVectorSpace(this.data.first().x.length));
		this.gradient(grad);
		return grad;
	}
	
	
	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void gradient(PositionListParameter<double[]> gradient)
	{		
		int i, j, k; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		
		// the dimension of the data set
		int dim = this.data.first().x.length;
				
		// some variables
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d; 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses		
		
		// calculate membership values w.r.t. one data object at a time
		double[] fuzzDistances				= new double[this.parameter.getPositionCount()];
		double[] membershipValues			= new double[this.parameter.getPositionCount()];
		double fuzzMembershipValue			= 0.0d;
		
		// scaler for indipendence of data objects and prototypes
		double linearFactor					= ((double)this.parameter.getPositionCount())/this.getDataCount();
		
		// corner case handling of zero distances
		int[] zeroDistanceIndexList			= new int[this.parameter.getPositionCount()];
		int zeroDistanceCount;
		
		// reset
		for(i = 0; i < this.parameter.getPositionCount(); i++)
		{
			for(k=0; k<dim; k++) gradient.getPosition(i)[k] = 0.0d;
		}
		
		// loop over every data object
		for(j = 0; j < this.getDataCount(); j++)
		{
			// calculate membership values
			for(i=0; i<this.parameter.getPositionCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i = 0; i <this.parameter.getPositionCount(); i++)
			{
				doubleTMP = this.metric.distance(this.data.get(j).x, this.parameter.getPosition(i));
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}

			// special case handling: if one (or more) prototype sits on top of a data object
			if(zeroDistanceCount>0)
			{
				for(i = 0; i < this.parameter.getPositionCount(); i++)
				{
					membershipValues[i] = 0.0d;
				}
				doubleTMP = 1.0d / ((double)zeroDistanceCount);
				for(k=0; k<zeroDistanceCount; k++)
				{
					membershipValues[zeroDistanceIndexList[k]] = doubleTMP;
				}
			}
			else
			{
				doubleTMP = 1.0d / distanceSum;
				for(i = 0; i < this.parameter.getPositionCount(); i++)
				{
					membershipValues[i] = fuzzDistances[i] * doubleTMP;
				}
			}
			
			// calculate gradient
			for(i = 0; i < this.parameter.getPositionCount(); i++)
			{
				// membership value is equal for all dimensions
				fuzzMembershipValue = MyMath.pow(membershipValues[i], this.fuzzifier);
				
				for(k=0; k<dim; k++)
				{
					// denominator: ((y-x)^p - 1)^2
					doubleTMP = MyMath.pow(this.parameter.getPosition(i)[k] - this.data.get(i).x[k], this.normExponent);
					doubleTMP -= 1.0d;
					doubleTMP *= doubleTMP;
					
					// nominator/denominator: c/n * u_ij^w * p*(y-x)^(p-1) / ((y-x)^p - 1)^2
					gradient.getPosition(i)[k] += linearFactor*this.normExponent*fuzzMembershipValue * MyMath.pow(this.parameter.getPosition(i)[k] - this.data.get(j).x[k], this.normExponent-1.0d) / doubleTMP;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#getName()
	 */
	@Override
	public String getName()
	{
		return "Scaled Norm Fuzzy c-Means Clustering Objective Function";
	}

	/**
	 * @return the fuzzifier
	 */
	public double getFuzzifier()
	{
		return this.fuzzifier;
	}

	/**
	 * @param fuzzifier the fuzzifier to set
	 */
	public void setFuzzifier(double fuzzifier)
	{
		this.fuzzifier = fuzzifier;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.clustering.FuzzyClusteringProvider#getFuzzyAssignmentsOf(data.set.IndexedDataObject)
	 */
	@Override
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<double[]> obj)
	{
		if(this.parameter == null) throw new IllegalStateException("Parameter is null.");

		int i, k;
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] distances					= new double[this.parameter.getPositionCount()];
		double[] membershipValues			= new double[this.parameter.getPositionCount()];
		int[] zeroDistanceIndexList			= new int[this.parameter.getPositionCount()];
		int zeroDistanceCount;
		
		
		for(i=0; i<this.parameter.getPositionCount(); i++) zeroDistanceIndexList[i] = -1;
		zeroDistanceCount = 0;
		distanceSum = 0.0d;
		for(i=0; i<this.parameter.getPositionCount(); i++)
		{
			doubleTMP = this.metric.distance(obj.x, this.parameter.getPosition(i));
			if(doubleTMP <= 0.0d)
			{
				doubleTMP = 0.0d;
				zeroDistanceIndexList[zeroDistanceCount] = i;
				zeroDistanceCount++;
			}
			else
			{
				doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
				distances[i] = doubleTMP;
				distanceSum += doubleTMP;
			}
		}

		// special case handling: if one (or more) prototype sits on top of a data object
		if(zeroDistanceCount>0)
		{
			for(i=0; i<this.parameter.getPositionCount(); i++)
			{
				membershipValues[i] = 0.0d;
			}
			doubleTMP = 1.0d / ((double)zeroDistanceCount);
			for(k=0; k<zeroDistanceCount; k++)
			{
				membershipValues[zeroDistanceIndexList[k]] = doubleTMP;
			}
		}
		else
		{
			for(i=0; i<this.parameter.getPositionCount(); i++)
			{
				doubleTMP = distances[i] / distanceSum;
				membershipValues[i] = doubleTMP;
			}
		}
		
		return membershipValues ;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.clustering.FuzzyClusteringProvider#getAllFuzzyClusterAssignments(java.util.List)
	 */
	@Override
	public List<double[]> getAllFuzzyClusterAssignments(List<double[]> assignmentList)
	{
		if(this.parameter == null) throw new IllegalStateException("Parameter is null.");

		if(assignmentList == null) assignmentList = new ArrayList<double[]>(this.getDataCount());
		assignmentList.clear();
				
		int i, j, k;
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.parameter.getPositionCount()];
		double[] membershipValues			= new double[this.parameter.getPositionCount()];
		int[] zeroDistanceIndexList			= new int[this.parameter.getPositionCount()];
		int zeroDistanceCount;
			
						
		for(j=0; j < this.getDataCount(); j++)
		{				
			for(i=0; i<this.parameter.getPositionCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i=0; i<this.parameter.getPositionCount(); i++)
			{
				doubleTMP = this.metric.distance(this.data.get(j).x, this.parameter.getPosition(i));
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
	
			// special case handling: if one (or more) prototype sits on top of a data object
			if(zeroDistanceCount>0)
			{
				for(i=0; i<this.parameter.getPositionCount(); i++)
				{
					membershipValues[i] = 0.0d;
				}
				doubleTMP = 1.0d / ((double)zeroDistanceCount);
				for(k=0; k<zeroDistanceCount; k++)
				{
					membershipValues[zeroDistanceIndexList[k]] = doubleTMP;
				}
			}
			else
			{
				for(i=0; i<this.parameter.getPositionCount(); i++)
				{
					doubleTMP = fuzzDistances[i] / distanceSum;
					membershipValues[i] = doubleTMP;
				}
			}
			
			assignmentList.add(membershipValues.clone());
		}
		
		return assignmentList;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.clustering.FuzzyClusteringProvider#isFuzzyAssigned(data.set.IndexedDataObject)
	 */
	@Override
	public boolean isFuzzyAssigned(IndexedDataObject<double[]> obj)
	{
		return this.parameter != null;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.clustering.FuzzyClusteringProvider#getFuzzyAssignmentSums()
	 */
	@Override
	public double[] getFuzzyAssignmentSums()
	{
		if(this.parameter == null) throw new IllegalStateException("Parameter is null.");
		int i, j, k;
		
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.parameter.getPositionCount()];
		double[] membershipValueSums		= new double[this.parameter.getPositionCount()];
		int[] zeroDistanceIndexList			= new int[this.parameter.getPositionCount()];
		int zeroDistanceCount;
			
						
		for(j=0; j < this.getDataCount(); j++)
		{				
			for(i=0; i<this.parameter.getPositionCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i=0; i<this.parameter.getPositionCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.parameter.getPosition(i));
				if(doubleTMP <= 0.0d)
				{
					doubleTMP = 0.0d;
					zeroDistanceIndexList[zeroDistanceCount] = i;
					zeroDistanceCount++;
				}
				else
				{
					doubleTMP = MyMath.pow(doubleTMP, distanceExponent);
					fuzzDistances[i] = doubleTMP;
					distanceSum += doubleTMP;
				}
			}
	
			// special case handling: if one (or more) prototype sits on top of a data object
			if(zeroDistanceCount>0)
			{
				doubleTMP = 1.0d / ((double)zeroDistanceCount);
				for(k=0; k<zeroDistanceCount; k++)
				{
					membershipValueSums[zeroDistanceIndexList[k]] += doubleTMP;
				}
			}
			else
			{
				for(i=0; i<this.parameter.getPositionCount(); i++)
				{
					doubleTMP = fuzzDistances[i] / distanceSum;
					membershipValueSums[i] += doubleTMP;
				}
			}
		}
		
		return membershipValueSums;
	}

}
