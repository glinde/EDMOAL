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


package datamining.clustering.protoype;

import java.io.Serializable;
import java.util.ArrayList;

import data.algebra.VectorSpace;
import datamining.clustering.AbstractCluster;

/**
 * The Centroid is a prototype, as it is initially intended. It provides all necessary functionality
 * to be used in Hars c-Means, Fuzzy c-Means etc.
 *
 * @author Roland Winkler
 */
/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class Centroid<T> extends AbstractCluster implements Serializable, Prototype<T>
{
	/**  */
	private static final long	serialVersionUID	= -2838176928230591009L;

	/** The Vectorspace of the underlying data set. */
	protected VectorSpace<T> vs;
	
	/** The current position of the centroid. */
	protected T position;
	
	/** The initial position of the centroid. */
	protected T initialPosition;
	
	/** The way a prototype goes during the iteration Process */
	protected ArrayList<T> way;
	
	/** Whether or not to record the way of the centroid. */
	protected boolean recordWay;
	
	
	/**
	 *  Creates a new centroid for the specified vector space. 
	 * 
	 * @param vs The vector space.
	 */
	public Centroid(VectorSpace<T> vs)
	{
		super(0);
		this.vs = vs;
		this.position = null;
		this.initialPosition = null;
		this.way = new ArrayList<T>(100);
		this.recordWay = true;
		this.activated = true;
	}

	/**
	 *  Creates a new centroid for the specified vector space and
	 *  initialises the centroid at the specified position.
	 * 
	 * @param vs The vector space.
	 * @param initialPos The initial position.
	 */
	public Centroid(VectorSpace<T> vs, T initialPos)
	{
		this(vs);
		this.position = this.vs.copyNew(initialPos);
		this.initialPosition = this.vs.copyNew(initialPos);
		this.way.add(this.vs.copyNew(initialPos));
	}

	/** 
	 * The copy constructor.
	 */
	public Centroid(Centroid<T> proto)
	{
		this(proto.vs, proto.position);
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#moveTo(java.lang.Object)
	 */
	@Override
	public void moveTo(T pos)
	{
		this.vs.copy(this.position, pos);	
		if(this.recordWay) this.way.add(this.vs.copyNew(this.position));
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#moveBy(java.lang.Object)
	 */
	@Override
	public void moveBy(T dif)
	{
		this.vs.add(this.position, dif);
		if(this.recordWay) this.way.add(this.vs.copyNew(this.position));
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#resetToInitialPosition()
	 */
	@Override
	public void resetToInitialPosition()
	{
		this.vs.copy(this.position, this.initialPosition);
		this.way.clear();
		this.way.add(this.vs.copyNew(this.position));
	}


	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#initializeWithPosition(java.lang.Object)
	 */
	@Override
	public void initializeWithPosition(T pos)
	{
		this.position = this.vs.copyNew(pos);
		this.initialPosition = this.vs.copyNew(pos);

		this.way.clear();
		this.way.add(this.vs.copyNew(pos));
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#getInitialPosition()
	 */
	@Override
	public T getInitialPosition()
	{
		return this.initialPosition;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#getWay()
	 */
	@Override
	public ArrayList<T> getWay()
	{
		return this.way;
	}
		
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#isRecordWay()
	 */
	@Override
	public boolean isRecordWay()
	{
		return this.recordWay;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#setRecordWay(boolean)
	 */
	@Override
	public void setRecordWay(boolean recordWay)
	{
		this.recordWay = recordWay;
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#getPosition()
	 */
	@Override
	public T getPosition()
	{
		return this.position;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#setPosition(java.lang.Object)
	 */
	@Override
	public void setPosition(T pos)
	{
		if(this.position == null) this.position = this.vs.copyNew(pos);
		else this.vs.copy(this.position, pos);
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.Prototype#getVectorSpace()
	 */
	@Override
	public VectorSpace<T> getVectorSpace()
	{
		return this.vs;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Centroid<T> clone()
	{
		Centroid<T> clone = new Centroid<T>(this.vs);
		clone.position = this.vs.copyNew(this.position);
		clone.initialPosition = this.vs.copyNew(this.initialPosition);
		clone.recordWay = this.recordWay;
		for(T p: this.way) clone.way.add(this.vs.copyNew(p));
		return clone;
	}
}
