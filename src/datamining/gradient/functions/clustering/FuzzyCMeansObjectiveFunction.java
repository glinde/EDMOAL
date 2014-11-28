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
package datamining.gradient.functions.clustering;

import java.util.ArrayList;
import java.util.List;

import data.algebra.Metric;
import data.algebra.VectorSpace;
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
public class FuzzyCMeansObjectiveFunction<T> extends AbstractObjectiveFunction<T, PositionListParameter<T>> implements FuzzyClusteringProvider<T>
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
	 * A metric for measuring the distance between objects of type <code>T</code>, that can be data objects
	 * or other locations in the feature space.
	 */
	protected final Metric<T> metric;

	/** The vector space that is used for prototype position calculations. */
	protected final VectorSpace<T> vs;
	
	public FuzzyCMeansObjectiveFunction(IndexedDataSet<T> dataSet, Metric<T> metric, VectorSpace<T> vs)
	{
		this(dataSet, 2.0d, metric, vs);
	}
	
	public FuzzyCMeansObjectiveFunction(IndexedDataSet<T> dataSet, double fuzzifier, Metric<T> metric, VectorSpace<T> vs)
	{
		super(dataSet, null);
		this.fuzzifier = fuzzifier;
		this.metric = metric;
		this.vs = vs;
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
		double[] distancesSq					= new double[this.parameter.getPositionCount()];
		boolean zeroDistance = false;
		
						
		for(j=0; j < this.getDataCount(); j++)
		{				
			zeroDistance = false;
			distanceSum = 0.0d;
			for(i=0; i<this.parameter.getPositionCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(this.data.get(j).x, this.parameter.getPosition(i));
				if(doubleTMP <= 0.0d)
				{
					zeroDistance = true;
				}
				else
				{ 
					distancesSq[i] = doubleTMP;
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
								
				objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * distancesSq[i];
			}
		}
	
		return objectiveFunctionValue * this.parameter.getPositionCount()/this.getDataCount();
	}


	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object)
	 */
	@Override
	public PositionListParameter<T> gradient()
	{
		PositionListParameter<T> grad = this.parameter.clone(this.vs);
		this.gradient(grad);
		return grad;
	}
	
	
	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void gradient(PositionListParameter<T> gradient)
	{		
		int i, j, k; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[this.parameter.getPositionCount()];
		double[] membershipValues			= new double[this.parameter.getPositionCount()];
		double linearFactor					= 2.0d*this.parameter.getPositionCount()/this.getDataCount();
		T tmpX								= this.vs.getNewAddNeutralElement();
		
		int[] zeroDistanceIndexList			= new int[this.parameter.getPositionCount()];
		int zeroDistanceCount;
		
		for(i = 0; i < this.parameter.getPositionCount(); i++)
		{
			this.vs.resetToAddNeutralElement(gradient.getPosition(i));
		}
		
		for(j = 0; j < this.getDataCount(); j++)
		{
			// membership values
			for(i=0; i<this.parameter.getPositionCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i = 0; i <this.parameter.getPositionCount(); i++)
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
				for(i = 0; i < this.parameter.getPositionCount(); i++)
				{
					doubleTMP = fuzzDistances[i] / distanceSum;
					membershipValues[i] = doubleTMP;
				}
			}
			
			for(i = 0; i < this.parameter.getPositionCount(); i++)
			{
				doubleTMP = linearFactor*MyMath.pow(membershipValues[i], this.fuzzifier);

				this.vs.copy(tmpX, this.parameter.getPosition(i));
				this.vs.sub(tmpX, this.data.get(j).x);
				this.vs.mul(tmpX, doubleTMP);
				this.vs.add(gradient.getPosition(i), tmpX);
			}
		}
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#getName()
	 */
	@Override
	public String getName()
	{
		return "Fuzzy c-Means Clustering Objective Function";
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
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<T> obj)
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
			doubleTMP = this.metric.distanceSq(obj.x, this.parameter.getPosition(i));
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
		
		return membershipValues;
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
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj)
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

	/* (non-Javadoc)
	 * @see datamining.resultProviders.FuzzyClusteringProvider#getClusterCount()
	 */
	@Override
	public int getClusterCount()
	{
		return this.parameter.getPositionCount();
	}

}
