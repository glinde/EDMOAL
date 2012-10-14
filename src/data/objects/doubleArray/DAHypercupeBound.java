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
package data.objects.doubleArray;

import data.algebra.BoundedAlgebraicStructure;
import data.algebra.VectorSpace;
import datamining.gradient.parameter.PositionListParameter;


/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DAHypercupeBound implements BoundedAlgebraicStructure<double[]>
{
	protected int dimension;
	protected double[] lowerBounds;
	protected double[] upperBounds;

	/**
	 * unit hypercube bound: [0, 1]^vs.dim
	 * 
	 * @param vs
	 * @param centroidCount
	 */
	public DAHypercupeBound(VectorSpace<double[]> vs)
	{
		this.dimension = vs.getDimension();
		this.lowerBounds = vs.getNewAddNeutralElement();
		this.upperBounds = vs.getNewAddNeutralElement();
		for(int k=0; k<this.dimension; k++)
		{
			this.upperBounds[k] += 1.0d;
		}
	}
	
	/**
	 * @param vs
	 * @param centroidCount
	 */
	public DAHypercupeBound(double[] lowerBounds, double[] upperBounds)
	{
		this.lowerBounds = lowerBounds.clone();
		this.upperBounds = upperBounds.clone();
	}
	
	public void ensureBounds(double[] x)
	{
		for(int k=0; k<this.dimension; k++)
		{
			x[k] = (x[k] < this.lowerBounds[k])? this.lowerBounds[k] : x[k];
			x[k] = (this.upperBounds[k] < x[k])? this.upperBounds[k] : x[k];
		}
	}

}
