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
import data.set.IndexedDataSet;

/**
 * A crisp clustering algorithm creates a hard partitioning of the data set.
 * Each data object is assigned to exactly one cluster or remains unassigned.
 * The assignment of a data object is equal to the index of the cluster that it is assigned to.<br>
 * 
 * This interface provides the functions to gain access to the crisp cluster indices, which are essentially
 * the result of the clustering algorithm.  
 *
 * @author Roland Winkler
 */
public interface CrispClusteringProvider<T> extends ResultProvider<T>
{
	/** 
	 * if a data object is not assigned its cluster assignment index must be equal to
	 * <code>UNASSIGNED_INDEX</code>. The valid cluster assignments start with <code>0</code>.
	 *  */
	public static final int UNASSIGNED_INDEX = -1;
	
	/**
	 * Determines if a data object is crisply assigned to a cluster. That is, 
	 * the function returns true, if the cluster assignment index is larger or equal to 0.
	 * If the data object is not part of the {@link IndexedDataSet}, stored in the algorithm,
	 * the value of <code>UNASSIGNED_INDEX</code> is returned.
	 * 
	 * @param obj The data object for which the cluster assignent is supposed to be determined.
	 * @return true, if the data object is crisply assigned by this clustering algorithm
	 */
	public boolean isCrispAssigned(IndexedDataObject<T> obj);
	
	/**
	 * Returns the list of cluster assignments for all data objects.
	 * 
	 * @return the list of cluster assignments for all data objects.
	 */
	public int[] getAllCrispClusterAssignments();
	
	/**
	 * Returns the cluster assignment of the specified data object.
	 * If the data object is not part of the {@link IndexedDataSet}, stored in the algorithm,
	 * the value of <code>UNASSIGNED_INDEX</code> is returned.
	 * 
	 * @param obj The data object for which the cluster assignment should be returned.
	 * @return the cluster assignment.
	 */
	public int getCrispClusterAssignmentOf(IndexedDataObject<T> obj);
}
