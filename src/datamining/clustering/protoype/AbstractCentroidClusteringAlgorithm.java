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

import java.util.Collection;

import data.algebra.VectorSpace;
import data.set.IndexedDataSet;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public abstract class AbstractCentroidClusteringAlgorithm<T> extends AbstractPrototypeClusteringAlgorithm<T, Centroid<T>>
{

	/**  */
	private static final long	serialVersionUID	= 6508613595872091359L;

	/**
	 * @param c
	 * @param useOnlyActivePrototypes
	 */
	public AbstractCentroidClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);
	}

	/**
	 * @param data
	 * @param vs
	 */
	public AbstractCentroidClusteringAlgorithm(IndexedDataSet<T> data, VectorSpace<T> vs)
	{
		super(data, vs);
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.protoype.AbstractPrototypeClusteringAlgorithm#initializeWithPositions(java.util.Collection)
	 */
	@Override
	public void initializeWithPositions(Collection<T> initialPrototypePositions)
	{
		Centroid<T> centr;
		int i = 0;
		
		this.prototypes.clear();		
		for(T x: initialPrototypePositions)
		{
			centr = new Centroid<T>(this.vs, x);
			centr.setClusterIndex(i);
			this.prototypes.add(centr);
			i++;
		}
		this.initialized = true;
	}
}
