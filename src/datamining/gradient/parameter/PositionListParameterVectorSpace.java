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
public class PositionListParameterVectorSpace<T> implements VectorSpace<PositionListParameter<T>>, Serializable
{
	/** A vector space of the base object type */
	protected VectorSpace<T> vs;
	
	/** The number of centroids in the parameter for this vector space. */
	protected int centroidCount;
	
	/**
	 * @param vs
	 * @param centroidCount
	 */
	public PositionListParameterVectorSpace(VectorSpace<T> vs, int centroidCount)
	{
		this.vs = vs;
		this.centroidCount = centroidCount;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#getNewAddNeutralElement()
	 */
	@Override
	public PositionListParameter<T> getNewAddNeutralElement()
	{
		return new PositionListParameter<T>(this.centroidCount, this.vs);
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#resetToAddNeutralElement(java.lang.Object)
	 */
	@Override
	public void resetToAddNeutralElement(PositionListParameter<T> x)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.resetToAddNeutralElement(x.getPosition(i));
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#copy(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void copy(PositionListParameter<T> x, PositionListParameter<T> y)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.copy(x.getPosition(i), y.getPosition(i));
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#inv(java.lang.Object)
	 */
	@Override
	public void inv(PositionListParameter<T> x)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.inv(x.getPosition(i));		
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#add(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void add(PositionListParameter<T> x, PositionListParameter<T> y)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.add(x.getPosition(i), y.getPosition(i));
		
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#sub(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void sub(PositionListParameter<T> x, PositionListParameter<T> y)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.sub(x.getPosition(i), y.getPosition(i));
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#mul(java.lang.Object, double)
	 */
	@Override
	public void mul(PositionListParameter<T> x, double a)
	{
		for(int i=0; i<this.centroidCount; i++) this.vs.mul(x.getPosition(i), a);
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#copyNew(java.lang.Object)
	 */
	@Override
	public PositionListParameter<T> copyNew(PositionListParameter<T> x)
	{
		return x.clone(this.vs);
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#invNew(java.lang.Object)
	 */
	@Override
	public PositionListParameter<T> invNew(PositionListParameter<T> x)
	{
		PositionListParameter<T> newP = x.clone(this.vs);		
		this.inv(newP);
		
		return newP;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#addNew(java.lang.Object, java.lang.Object)
	 */
	@Override
	public PositionListParameter<T> addNew(PositionListParameter<T> x, PositionListParameter<T> y)
	{
		PositionListParameter<T> newP = x.clone(this.vs);		
		this.add(newP, y);
		
		return newP;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#subNew(java.lang.Object, java.lang.Object)
	 */
	@Override
	public PositionListParameter<T> subNew(PositionListParameter<T> x, PositionListParameter<T> y)
	{
		PositionListParameter<T> newP = x.clone(this.vs);		
		this.add(newP, y);
		
		return newP;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#mulNew(java.lang.Object, double)
	 */
	@Override
	public PositionListParameter<T> mulNew(PositionListParameter<T> x, double a)
	{
		PositionListParameter<T> newP = x.clone(this.vs);		
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
