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
package datamining.gradient.parameter;

import java.util.List;

import data.algebra.Norm;
import data.algebra.ScalarProduct;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class PositionListParameterNorm<T> implements Norm<PositionListParameter<T>>
{
	/** A norm of the base object type */
	protected Norm<T> elementNorm;
	
	/** A norm to how to connect the individual centroids together. */
	protected Norm<double[]> sumNorm;
	
	/** The number of elements in the list this vector space. */
	protected int centroidCount;

	/**
	 * @param norm
	 * @param centroidCount
	 */
	public PositionListParameterNorm(Norm<T> elementNorm, Norm<double[]> sumNorm, int centroidCount)
	{
		this.elementNorm = elementNorm;
		this.sumNorm  = sumNorm;
		this.centroidCount = centroidCount;
	}
	
	
	/* (non-Javadoc)
	 * @see data.algebra.Norm#length(java.lang.Object)
	 */
	@Override
	public double length(PositionListParameter<T> x)
	{
		double[] length = new double[this.centroidCount];
		
		for(int i=0; i<this.centroidCount; i++) length[i] = this.elementNorm.length(x.getPosition(i));
		
		return this.sumNorm.length(length);
	}

	/* (non-Javadoc)
	 * @see data.algebra.Norm#lengthSq(java.lang.Object)
	 */
	@Override
	public double lengthSq(PositionListParameter<T> x)
	{
		double length = this.length(x);
		return length*length;
	}
}
