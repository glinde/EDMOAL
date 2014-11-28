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


package data.objects.doubleArray;

import java.io.Serializable;

import data.algebra.ScalarProduct;

/**
 * The standard scalar product (or dot product, or inner product) for double arrays. This class implements the standard scalar product for double arrays as they
 * occur for n-dimensional real vector spaces with orthogonal dimensions. 
 *
 * @author Roland Winkler
 */
public class DAStandardScalarProduct extends DAEuclideanNorm implements ScalarProduct<double[]>, Serializable
{

	/**  */
	private static final long	serialVersionUID	= 2469145610627380378L;

	/* (non-Javadoc)
	 * @see data.algebra.ScalarProduct#scalarProduct(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double scalarProduct(double[] x, double[] y)
	{
		double prod = 0.0d;
		
		for(int i=0; i<x.length && i<y.length; i++)
		{
			prod += x[i]*y[i];
		}
		
		return prod;
	}

	/**
	 * Calculates the scalar product between two double arrays, taking only the first <code>dim</code> dimensions into account.  
	 * 
	 * @param x The coordinates of position x
	 * @param y The coordinates of position y
	 * @param dim The number of dimensions that should be used for calculation
	 * @return The scalar product between <code>x</code> and <code>y</code>, calculated in the first <code>dim</code> dimensions.
	 */
	public double scalarProduct(double[] x, double[] y, int dim)
	{
		double prod = 0.0d;
		
		if(x.length < dim || y.length < dim) throw new IllegalArgumentException("The number of elements in x and y must be at least dim.");
		
		for(int i=0; i<dim; i++)
		{
			prod += x[i]*y[i];
		}
		
		return prod;
	}
	
}
