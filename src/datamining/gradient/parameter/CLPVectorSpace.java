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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.algebra.VectorSpace;
import datamining.clustering.protoype.Prototype;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class CLPVectorSpace<T> implements VectorSpace<CentroidListParameter<T>>, Serializable
{
	/** A vector space of the base object type */
	protected VectorSpace<T> vs;
	
	/** The number of centroids in the parameter for this vector space. */
	protected int centroidCount;
	
	/**
	 * @param vs
	 * @param centroidCount
	 */
	public CLPVectorSpace(VectorSpace<T> vs, int centroidCount)
	{
		this.vs = vs;
		this.centroidCount = centroidCount;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#getNewAddNeutralElement()
	 */
	@Override
	public CentroidListParameter<T> getNewAddNeutralElement()
	{
		return new CentroidListParameter<T>(this.centroidCount, this.vs);
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#resetToAddNeutralElement(java.lang.Object)
	 */
	@Override
	public void resetToAddNeutralElement(CentroidListParameter<T> x)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.resetToAddNeutralElement(x.getCentroid(i).getPosition());
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#copy(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void copy(CentroidListParameter<T> x, CentroidListParameter<T> y)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.copy(x.getCentroid(i).getPosition(), y.getCentroid(i).getPosition());
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#inv(java.lang.Object)
	 */
	@Override
	public void inv(CentroidListParameter<T> x)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.inv(x.getCentroid(i).getPosition());		
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#add(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void add(CentroidListParameter<T> x, CentroidListParameter<T> y)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.add(x.getCentroid(i).getPosition(), y.getCentroid(i).getPosition());
		
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#sub(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void sub(CentroidListParameter<T> x, CentroidListParameter<T> y)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.sub(x.getCentroid(i).getPosition(), y.getCentroid(i).getPosition());
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#mul(java.lang.Object, double)
	 */
	@Override
	public void mul(CentroidListParameter<T> x, double a)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.mul(x.getCentroid(i).getPosition(), a);
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#copyNew(java.lang.Object)
	 */
	@Override
	public CentroidListParameter<T> copyNew(CentroidListParameter<T> x)
	{
		return x.clone();
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#invNew(java.lang.Object)
	 */
	@Override
	public CentroidListParameter<T> invNew(CentroidListParameter<T> x)
	{
		CentroidListParameter<T> newP = x.clone();		
		this.inv(newP);
		
		return newP;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#addNew(java.lang.Object, java.lang.Object)
	 */
	@Override
	public CentroidListParameter<T> addNew(CentroidListParameter<T> x, CentroidListParameter<T> y)
	{
		CentroidListParameter<T> newP = x.clone();		
		this.add(newP, y);
		
		return newP;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#subNew(java.lang.Object, java.lang.Object)
	 */
	@Override
	public CentroidListParameter<T> subNew(CentroidListParameter<T> x, CentroidListParameter<T> y)
	{
		CentroidListParameter<T> newP = x.clone();		
		this.add(newP, y);
		
		return newP;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#mulNew(java.lang.Object, double)
	 */
	@Override
	public CentroidListParameter<T> mulNew(CentroidListParameter<T> x, double a)
	{
		CentroidListParameter<T> newP = x.clone();		
		this.mul(newP, a);
		
		return newP;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#getDimension()
	 */
	@Override
	public int getDimension()
	{
		return this.vs.getDimension() * this.centroidCount;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#infiniteDimensionality()
	 */
	@Override
	public boolean infiniteDimensionality()
	{
		return false;
	}
	
}
