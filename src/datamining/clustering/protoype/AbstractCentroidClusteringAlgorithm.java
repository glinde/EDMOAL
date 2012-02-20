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
 * An abstract class for {@link Centroid} based clustering algorithms. It actually does only 
 * provide the functionality of initialisation with positions. Also it fixes the {@link Prototype}
 * class to being a {@link Centroid}.
 *
 * @author Roland Winkler
 */
public abstract class AbstractCentroidClusteringAlgorithm<T> extends AbstractPrototypeClusteringAlgorithm<T, Centroid<T>>
{

	/**  */
	private static final long	serialVersionUID	= 6508613595872091359L;

	/**
	 * This constructor creates a new clustering algorithm, taking an existing one. It has the option to use only
	 * active prototypes from the old clustering algorithm. This constructor is especially useful if the clusteing is done
	 * in multiple steps. So the first clustering algorithm can for example calculate the initial positions of the 
	 * prototypes for the second clustering algorithm. An other option is, that the first clustering algorithm
	 * creates a set of deactivated prototypes and the second clustering algorithm is initialised with less clusters than the
	 * first.
	 * 
	 * @param c the elders clustering algorithm object
	 * @param useOnlyActivePrototypes States, that only prototypes that are active in the old clustering
	 * algorithm are used for the new clustering algorithm.
	 */
	public AbstractCentroidClusteringAlgorithm(AbstractPrototypeClusteringAlgorithm<T, Centroid<T>> c, boolean useOnlyActivePrototypes)
	{
		super(c, useOnlyActivePrototypes);
	}

	/**
	 * The initial constructor for clustering. The number of clusters can be changed after initialisation, but it
	 * is not recommended because some algorithms have to be reinitialised.
	 * 
	 * @param data The data set for clustering.
	 * @param vs The vector space of which the data objects are elements.
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
