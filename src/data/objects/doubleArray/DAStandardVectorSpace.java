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
import java.util.Arrays;

import data.algebra.VectorSpace;


/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DAStandardVectorSpace implements VectorSpace<double[]>, Serializable
{
	/**  */
	private static final long	serialVersionUID	= -6786617853477957151L;
	/** the dimension of the vector space */
	protected final int dim;
	
	/**
	 * @param dim
	 */
	public DAStandardVectorSpace(int dim)
	{
		this.dim = dim;
	}

	/* (non-Javadoc)
	 * @see data.algebratorSpace#getAddNeutral()
	 */
	@Override
	public double[] getNewAddNeutralElement()
	{
		return new double[this.dim];
	}

	/* (non-Javadoc)
	 * @see data.algebratorSpace#getAddNeutral()
	 */
	@Override
	public void resetToAddNeutralElement(double[] x)
	{
		if(x.length < this.dim) throw new IllegalArgumentException("The number of elements in x must be at least dim.");
		
		for(int i=0; i<this.dim; i++) x[i] = 0.0d;
	}
	
	/* (non-Javadoc)
	 * @see data.algebratorSpace#add(data.objects.DataObject, data.objects.DataObject)
	 */
	@Override
	public void add(double[] x, double[] y)
	{
		if(x.length < this.dim || y.length < this.dim) throw new IllegalArgumentException("The number of elements in x and y must be at least dim.");
		
		for(int i=0; i<this.dim; i++)	x[i] += y[i];
	}

	/* (non-Javadoc)
	 * @see data.algebratorSpace#addNew(data.objects.DataObject, data.objects.DataObject)
	 */
	@Override
	public double[] addNew(double[] x, double[] y)
	{		
		if(x.length < this.dim || y.length < this.dim) throw new IllegalArgumentException("The number of elements in x and y must be at least dim.");
		
		double[] z = new double[this.dim];
		
		for(int i=0; i<this.dim; i++) z[i] = x[i] + y[i];
		
		return z;
	}

	/* (non-Javadoc)
	 * @see data.algebratorSpace#inv(data.objects.DataObject)
	 */
	@Override
	public void inv(double[] x)
	{
		if(x.length < this.dim) throw new IllegalArgumentException("The number of elements in x must be at least dim.");
		
		for(int i=0; i<this.dim; i++)	x[i] = -x[i];
	}

	/* (non-Javadoc)
	 * @see data.algebratorSpace#invNew(data.objects.DataObject)
	 */
	@Override
	public double[] invNew(double[] x)
	{
		if(x.length < this.dim) throw new IllegalArgumentException("The number of elements in x must be at least dim.");
		
		double[] z = new double[this.dim];
		
		for(int i=0; i<this.dim; i++) z[i] = -x[i];
		
		return z;
	}

	/* (non-Javadoc)
	 * @see data.algebratorSpace#mul(double, data.objects.DataObject)
	 */
	@Override
	public void mul(double[] x, double a)
	{
		if(x.length < this.dim) throw new IllegalArgumentException("The number of elements in x must be at least dim.");
		
		for(int i=0; i<this.dim; i++)	x[i] *= a;		
	}

	/* (non-Javadoc)
	 * @see data.algebratorSpace#mulNew(double, data.objects.DataObject)
	 */
	@Override
	public double[] mulNew(double[] x, double a)
	{
		if(x.length < this.dim) throw new IllegalArgumentException("The number of elements in x must be at least dim.");
		
		double[] z = new double[this.dim];
		
		for(int i=0; i<this.dim; i++) z[i] = a*x[i];
		
		return z;
	}

	/* (non-Javadoc)
	 * @see data.algebratorSpace#sub(data.objects.DataObject, data.objects.DataObject)
	 */
	@Override
	public void sub(double[] x, double[] y)
	{
		if(x.length < this.dim || y.length < this.dim) throw new IllegalArgumentException("The number of elements in x and y must be at least dim.");
		
		for(int i=0; i<this.dim; i++)	x[i] -= y[i];
	}

	/* (non-Javadoc)
	 * @see data.algebratorSpace#subNew(data.objects.DataObject, data.objects.DataObject)
	 */
	@Override
	public double[] subNew(double[] x, double[] y)
	{
		if(x.length < this.dim || y.length < this.dim) throw new IllegalArgumentException("The number of elements in x and y must be at least dim.");
		
		double[] z = new double[this.dim];
		
		for(int i=0; i<this.dim; i++) z[i] = x[i] - y[i];
		
		return z;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#getDimension()
	 */
	@Override
	public int getDimension()
	{
		return this.dim;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#copy(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void copy(double[] x, double[] y)
	{
		if(x.length < this.dim || y.length < this.dim) throw new IllegalArgumentException("The number of elements in x and y must be at least dim.");
		
		for(int i=0; i<this.dim; i++) x[i] = y[i];
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#copy(java.lang.Object)
	 */
	@Override
	public double[] copyNew(double[] x)
	{
		if(x.length < this.dim) throw new IllegalArgumentException("The number of elements in x must be at least dim.");
		
		return Arrays.copyOf(x, this.dim);
	}

}
