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


package data.structures;

import java.io.Serializable;

import data.set.IndexedDataSet;
import data.set.Sealable;


/**
 * An IndexedDataSet is a unordered set of data objects, equipped with an index for fast access. This interface
 * is the basis for providing some kind of order or structure to this unordered data set.
 * This structure might be a tree, a list or what ever is useful.<br>
 * 
 * The data structure can either be dynamic (it allows changing the content of the data set which requires a constant
 * re-validation of the structure) or static, in which case the interface <code>Sealable</code> should also be implemented.
 * For a dynamic data structure, no interface exists, but that might change in the future. TODO: rewrite if a dynamic data structure
 * interface is provided. <br>
 * 
 * This interface construction is done to abstract the implementation of the data structure from the algorithms
 * using the data structure. An algorithm requiring a certain index structure should therefore implement an interface
 * and the interface should be implemented separately. <br> 
 * 
 * For example, an algorithm requires a minimal spanning tree on a data set. It is best to create a new interface for this
 * task and use the interface call in the algorithm. This way, the actual spanning tree construction can be replaced
 * by a more efficient algorithm if necessary or available. 
 *
 * @see IndexedDataSet
 * @see Sealable
 * @author Roland Winkler
 */
public interface DataSetFunctionalityProvider<T> extends Serializable
{	
	/**
	 * @return the data set
	 */
	public IndexedDataSet<T> getDataSet();

	/**
	 * After setting the data set, the structure must be rebuild in order to have a valid structure. 
	 * 
	 * @return sets the data set
	 */
	public void setDataSet(IndexedDataSet<T> dataSet);
	
	/**
	 * Clears the all internal information that are stored in order to provide the functionality. 
	 */
	public void clearBuild();
	
	/**
	 * @return true if the functionality can be provided
	 */
	public boolean isBuild();
	
	/**
	 * Creates all internal informations necessary for providing the functionality.
	 */
	public void build();
}
