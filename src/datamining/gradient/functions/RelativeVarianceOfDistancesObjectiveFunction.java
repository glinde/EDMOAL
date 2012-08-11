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

import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataSet;
import etc.SimpleStatistics;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class RelativeVarianceOfDistancesObjectiveFunction implements GradientFunction<double[], double[]>
{
	protected final DAEuclideanVectorSpace vs;
	
	
	/**
	 * @param c
	 */
	public RelativeVarianceOfDistancesObjectiveFunction(RelativeVarianceOfDistancesObjectiveFunction c)
	{
		this.vs = c.vs;
	}

	/**
	 * @param dimension
	 */
	public RelativeVarianceOfDistancesObjectiveFunction(int dimension)
	{		
		this.vs = new DAEuclideanVectorSpace(dimension);
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.GradientFunction#functionValue(java.lang.Object)
	 */
	@Override
	public double functionValue(IndexedDataSet<double[]> dataSet, double[] parameter)
	{
		double[] distances = new double[dataSet.size()];
		
		for(int j=0; j<dataSet.size(); j++)
		{
			distances[j] = this.vs.distance(parameter, dataSet.get(j).x);
		}
		
		double[] meanVar = SimpleStatistics.mean_variance(distances);
		
		return meanVar[1]/(meanVar[0] * meanVar[0]);
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.GradientFunction#gradient(java.lang.Object)
	 */
	@Override
	public double[] gradient(IndexedDataSet<double[]> dataSet, double[] parameter)
	{
		double[] y = this.vs.getNewAddNeutralElement();
		
		// TODO: complete function
		
		return y;
	}

	/**
	 * @return the vs
	 */
	public DAEuclideanVectorSpace getVs()
	{
		return this.vs;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.GradientFunction#getName()
	 */
	@Override
	public String getName()
	{
		return "Least Squares for Euclidean Real Vector Space";
	}

}
