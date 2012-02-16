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


package datamining.clustering;

import data.set.IndexedDataObject;

/**
 * The use of this interface states, that a clustering algorithm is using a noise cluster in order to specify outliers.
 * A data object can not be both, crisply assigned and noise. A data object can be in one of three states:
 * unassigned and not noise, assigned and noise, assigned and not noise. It can not be unassigned and noise, because
 * if a data object is detected as noise, it is assigned to the noise cluster. Therefore, the state of unassigned and noise is
 * equivalent to the state of assigned and noise.
 * 
 * @author Roland Winkler
 */
public interface CrispNoiseClusteringAlgorithm<T> extends CrispClusteringAlgorithm<T>
{
	/**
	 * Returns a list that states for all data objects whether or not they are assigned to the noise cluster or not. 
	 * 
	 * @return a list that states for all data objects whether or not they are assigned to the noise cluster or not.
	 */
	public boolean[] getCrispNoiseAssignments();
	
	/**
	 * Determines for the specified data object if it is assigned to the noise cluster or not.
	 * 
	 * @param obj the data object.
	 * @return true, if the data object is assigned to the noise cluster, false otherwise.
	 */
	public boolean isCrispNoiseAssigned(IndexedDataObject<T> obj);
}