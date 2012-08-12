/**
Copyright (c) 2012, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
 
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
package datamining.gradient.functions;

import java.util.ArrayList;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.AlgorithmNotInitializedException;
import datamining.gradient.parameter.CentroidListParameter;
import etc.MyMath;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class FuzzyCMeansObjectiveFunction<T>  implements GradientFunction<T, CentroidListParameter<T>>
{
	/**
	 * The fuzzifier. It specifies how soft the membershiop values are going to be calculated. if the
	 * value is 1, the algorithm is identical to crisp clustering and for values going to infinity, it is complete soft clustering.
	 * A useful value is around 2.<br>
	 *
	 *	Range of values: <code>fuzzifier</code> > 1
	 */
	protected double fuzzifier;
	
	/**
	 * A metric for measuring the distance between objects of type <code>T</code>, that can be data objects
	 * or other locations in the feature space.
	 */
	protected final Metric<T> metric;

	/** The vector space that is used for prototype position calculations. */
	protected final VectorSpace<T> vs;
	
	/**
	 * 
	 */
	public FuzzyCMeansObjectiveFunction(Metric<T> metric, VectorSpace<T> vs)
	{
		this(2.0d, metric, vs);
	}
	
	/**
	 * @param fuzzifier
	 */
	public FuzzyCMeansObjectiveFunction(double fuzzifier, Metric<T> metric, VectorSpace<T> vs)
	{
		this.fuzzifier = fuzzifier;
		this.metric = metric;
		this.vs = vs;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#functionValue(data.set.IndexedDataSet, java.lang.Object)
	 */
	@Override
	public double functionValue(IndexedDataSet<T> dataSet, CentroidListParameter<T> parameter)
	{
		int i, j; 
		// i: index for clusters
		// j: index for data objects
		
		double objectiveFunctionValue = 0.0d;
		
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances					= new double[parameter.getCentroidCount()];
		double[] distancesSq					= new double[parameter.getCentroidCount()];
		boolean zeroDistance = false;
		
						
		for(j=0; j < dataSet.size(); j++)
		{				
			zeroDistance = false;
			distanceSum = 0.0d;
			for(i=0; i<parameter.getCentroidCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(dataSet.get(j).x, parameter.getCentroid(i).getPosition());
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
			for(i=0; i<parameter.getCentroidCount(); i++)
			{
				doubleTMP = fuzzDistances[i] / distanceSum;
								
				objectiveFunctionValue += MyMath.pow(doubleTMP, this.fuzzifier) * distancesSq[i];
			}
		}
	
		return objectiveFunctionValue;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object)
	 */
	@Override
	public CentroidListParameter<T> gradient(IndexedDataSet<T> dataSet, CentroidListParameter<T> parameter)
	{
		CentroidListParameter<T> gradient = parameter.clone();
		this.gradient(dataSet, parameter, gradient);
		return gradient;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void gradient(IndexedDataSet<T> dataSet, CentroidListParameter<T> parameter, CentroidListParameter<T> gradient)
	{		
		int i, j, k; 
		// i: index for clusters
		// j: index for data objects
		// k: index for dimensions, others
		// t: index for iterations	
				
		double distanceExponent = 1.0d / (1.0d - this.fuzzifier);	// to reduce the usage of divisions
		double distanceSum = 0.0d;									// the sum_i dist[i][l]^{2/(1-fuzzifier)}: the sum of all parametrised distances for one cluster l 
		double doubleTMP = 0.0d;									// a temporarly variable for multiple perpuses
		double[] fuzzDistances				= new double[parameter.getCentroidCount()];
		double[] membershipValues			= new double[parameter.getCentroidCount()];
		T tmpX								= this.vs.getNewAddNeutralElement();
		ArrayList<T> newPositions			= new ArrayList<T>(parameter.getCentroidCount());
		
		int[] zeroDistanceIndexList			= new int[parameter.getCentroidCount()];
		int zeroDistanceCount;

		// if getPosition returns a new reference... (which it doesn't as of now: 2012.08.12, but it might change in time)
		for(i = 0; i < parameter.getCentroidCount(); i++)
		{
			newPositions.add(gradient.getCentroid(i).getPosition());
		}
		
		for(i = 0; i < parameter.getCentroidCount(); i++)
		{
			this.vs.resetToAddNeutralElement(newPositions.get(i));
		}
		
		for(j = 0; j < dataSet.size(); j++)
		{
			// membership values
			for(i=0; i<parameter.getCentroidCount(); i++) zeroDistanceIndexList[i] = -1;
			zeroDistanceCount = 0;
			distanceSum = 0.0d;
			for(i = 0; i <parameter.getCentroidCount(); i++)
			{
				doubleTMP = this.metric.distanceSq(dataSet.get(j).x, parameter.getCentroid(i).getPosition());
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
				for(i = 0; i < parameter.getCentroidCount(); i++)
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
				for(i = 0; i < parameter.getCentroidCount(); i++)
				{
					doubleTMP = fuzzDistances[i] / distanceSum;
					membershipValues[i] = doubleTMP;
				}
			}
			
			for(i = 0; i < parameter.getCentroidCount(); i++)
			{
				doubleTMP = 2.0d*MyMath.pow(membershipValues[i], this.fuzzifier);

				this.vs.copy(tmpX, parameter.getCentroid(i).getPosition());
				this.vs.sub(tmpX, dataSet.get(j).x);
				this.vs.mul(tmpX, doubleTMP);
				this.vs.add(newPositions.get(i), tmpX);
			}
		}

		for(i = 0; i < parameter.getCentroidCount(); i++)
		{
			gradient.getCentroid(i).setPosition(newPositions.get(i));
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
	
	

}
