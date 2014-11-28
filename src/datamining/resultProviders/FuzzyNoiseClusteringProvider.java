/**
Copyright (c) 2012, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
	roland.winkler@gmail.com
 
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


package datamining.resultProviders;

import data.set.IndexedDataObject;

/**
 * Extends the interface of {@link FuzzyClusteringProvider} to handle a noise cluster.
 * It is similar to the {@link CrispNoiseClusteringProvider} interface, just for fuzzy assignments.<br>
 * 
 * The noise cluster is regarded as an additional cluster, but is not build into the usual cluster index structure.
 * Therefore, the noise fuzzy values are handled separated from the usual fuzzy cluster values.
 *
 * @author Roland Winkler
 */
public interface FuzzyNoiseClusteringProvider<T> extends FuzzyClusteringProvider<T>
{
	/**
	 * Returns a list of fuzzy noise assignment values for all data objects.
	 * 
	 * @return a list of fuzzy noise assignment values for all data objects.
	 */
	public double[] getFuzzyNoiseAssignments();
	
	/**
	 * Returns a the fuzzy noise assignment value for the specified data object.
	 * 
	 * @param obj the data object.
	 * @return a the fuzzy noise assignment value for the specified data object.
	 */
	public double getFuzzyNoiseAssignmentOf(IndexedDataObject<T> obj);
}
