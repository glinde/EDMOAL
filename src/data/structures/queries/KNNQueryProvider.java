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


package data.structures.queries;

import java.util.Collection;

import data.set.IndexedDataObject;
import data.structures.DataSetFunctionalityProvider;

/**
 * Provides the functionality of a k-nearest neighbour query. It return the k data objects, that
 * are closest to a specified centre.
 * 
 * @author Roland Winkler
 */
public interface KNNQueryProvider<T> extends DataSetFunctionalityProvider<T>
{
	/**
	 * Performs a k-nearest neighbour query, i.e. it returns the <code>k</code> closest data objects to <code>centre</code>.
	 * The <code>centre</code> object does not need to be contained as a member of the underlying data structure.
	 * The query result is added to the <code>result</code> collection.<br>
	 * 
	 * If the <code>result</code> is <code>null</code>, a new instance of a <code>Collection</code> is returned, containing the result. 
	 * 
	 * @param result A collection to which the query result is added 
	 * @param centre The centre of the query
	 * @param k The number of data objects for this query, must not be negative
	 * @return The <code>result</code> collection, or in case <code>result</code> is <code>null</code>,
	 * 		   a new Collection containing the result
	 */
	public Collection<IndexedDataObject<T>> knnQuery(Collection<IndexedDataObject<T>> result, T centre, int k);
}