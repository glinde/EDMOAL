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

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 * @param <D>
 */
public class DALeastSquaresObjectiveFunction extends AbstractObjectiveFunction<double[], double[]>
{
	protected final DAEuclideanVectorSpace vs;
	
	/**
	 * @param c
	 */
	public DALeastSquaresObjectiveFunction(DALeastSquaresObjectiveFunction c)
	{
		super(c);
		this.vs = c.vs;
	}

	/**
	 * @param data
	 * @throws DataSetNotSealedException
	 */
	public DALeastSquaresObjectiveFunction(IndexedDataSet<double[]> dataSet) throws DataSetNotSealedException
	{
		super(dataSet, new double[dataSet.first().x.length]);
		this.vs = new DAEuclideanVectorSpace(dataSet.first().x.length);
	}

	public double functionValue()
	{
		double result = 0.0d;
		
		for(int i=0; i<this.getDataCount(); i++)
		{
			result += this.vs.distanceSq(this.data.get(i).x, this.parameter);
		}
		result /= this.getDataCount();
		
		return result;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object)
	 */
	@Override
	public double[] gradient()
	{
		double[] grad = this.vs.getNewAddNeutralElement();
		
		this.gradient(grad);
		
		return grad;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void gradient(double[] gradient)
	{
		this.vs.resetToAddNeutralElement(gradient);
		double[] tmp = this.vs.getNewAddNeutralElement();

		for(int i=0; i<this.getDataCount(); i++)
		{
			this.vs.copy(tmp, this.parameter);
			this.vs.sub(tmp, this.data.get(i).x);
			this.vs.add(gradient, tmp);
		}
		
		this.vs.mul(gradient, 2.0d/this.getDataCount());
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
