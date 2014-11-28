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

import datamining.clustering.ClusteringAlgorithm;
import datamining.resultProviders.PrototypeProvider;

/**
 * The interface for all prototype based clustering algorithms.
 *
 * @author Roland Winkler
 */
public interface PrototypeClusteringAlgorithm<T, S extends Prototype<T>> extends ClusteringAlgorithm<T>, PrototypeProvider<T, S>
{
//	/**
//	 * Initialises the clustering algorithm with the specified prototypes.
//	 * 
//	 * @param initialPrototypes The prototypes for initialising the clustering algorithm.
//	 */
//	public void initializeWithPrototypes(Collection<S> initialPrototypes);
//
//	/**
//	 * Initialises the clustering algorithm and puts prototypes at the specified positions.
//	 * This requires the clustering algorithm to inherit its clustering procedure with its
//	 * standard prototype.<br>
//	 * 
//	 * the number of positions in the specified collection determines the number of prototypes initialised:
//	 * for each position, one prototype is initialised.
//	 * 
//	 * @param initialPrototypePositions The positions at which the prototypes are allowed to be initialised.
//	 */
//	public void initializeWithPositions(Collection<T> initialPrototypePositions);
//	
//	/**
//	 * Returns the prototypes.
//	 * 
//	 * @return The prototypes.
//	 */
//	public ArrayList<S> getPrototypes();
//	
//	/**
//	 * Returns all active prototypes.
//	 * 
//	 * @return All active prototypes.
//	 */
//	public ArrayList<S> getActivePrototypes();
//	
//	/**
//	 * Returns the vector space the clustering algorithm uses to calculate prototype positions.
//	 * 
//	 * @return The vector space.
//	 */
//	public VectorSpace<T> getVectorSpace();
}
