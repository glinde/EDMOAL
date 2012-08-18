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

import java.util.List;

import data.set.IndexedDataObject;

/**
 * Fuzzy clustering algorithms do not assign data objects uniquely to one cluster like a crisp clustering algorithm.
 * They do it in a fuzzy manner, that is: they create a partitioning of the data set such that the
 * sum of membership values of one data object is 1. Terefore, the fuzzy assignment (membership values) for one data object
 * are always a list of double values between 0 and 1.<br>
 * 
 * This interface provides the functions to gain access to the fuzzy membership values, which are essentially the result
 * of the clustering algorithm.
 * 
 * @author Roland Winkler
 */
public interface FuzzyClusteringProvider<T> extends ResultProvider<T>
{
	/**
	 * Returns the list of fuzzy membership values of the specified data object to all clusters.
	 * 
	 * @param obj The data object.
	 * @return The membership values of the data object.
	 */
	public double[] getFuzzyAssignmentsOf(IndexedDataObject<T> obj);
	
	/**
	 * Gets the fuzzy cluster assignments of all data objects and stores them in the specified {@link List}.
	 * If the list is <code>null</code>, a new {@link List} object is created and returned.<br>
	 * 
	 * Since there is a membership value for each pair of data object and cluster, the size
	 * of the result is in O(n*c) with n being the number of data objects and c being the number of clusters.
	 * 
	 * @param assignmentList The list to store the membership values in.
	 * @return The <code>assignmentList</code> reference or a new {@link List} object if <code>assignmentList</code> is <code>null</code>. 
	 */
	public List<double[]> getAllFuzzyClusterAssignments(List<double[]> assignmentList);
	
	/**
	 * Determines if the specified data object is assigned to the clusters.
	 * Unlike for the crisp cluster assignments, it is not immediately clear whether or not a data object
	 * is assigned to the clusters or not. Therefore, a data object can have fuzzy cluster assignments and
	 * still be not assigned. In this case, the membership values are without meaning.<br>
	 * 
	 * For clustering algorithms, that have both characteristics of fuzzy, and crisp clustering, crisp
	 * cluster assigned data objects are a subset of fuzzy cluster assigned data objects. For example
	 * a data object with the membership values [0.5, 0.5, 0] is fuzzy assigned but not crisp assigned.
	 * A data object with the membership values [0, 1, 0] is both, fuzzy assigned and crisp assigned.
	 * 
	 * @param obj The data object.
	 * @return true, if the data object is fuzzy assigned to the clusters, false if it is not.
	 */
	public boolean isFuzzyAssigned(IndexedDataObject<T> obj);
	
	/**
	 * Returns a list of fuzzy assignment sums. Each value contains the sum of membership values of one
	 * cluster. The list contains the values for all clusters.
	 * 
	 * @return A list of membership value sums, by cluster.
	 */
	public double[] getFuzzyAssignmentSums();
}
