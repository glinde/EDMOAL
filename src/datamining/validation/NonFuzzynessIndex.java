/**
Copyright (c) 2013, The EDMOAL Project

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
package datamining.validation;



/**
 * Calculates the non fuzzines index (normalised partition coefficient) of the fuzzy clustering result.<br>
 * 
 * The complexity of the function is in O(n*c) with n being the number of data objects and c being the number of clusters.<br> 
 * 
 * See paper: E. Backer and A.K. Jain. A Clustering Performance Measure based on Fuzzy Set Decomposition.IEEE Trans. on Pattern Analysis and Machine Intelligence (PAMI)3(1):66�74. IEEE Press, Piscataway, NJ, USA 1981<br>
 *
 * @param fuzzyAlgorithm  The fuzzy clustering algorithm containing the clustering result
 * @return The partition coefficient of the specified clustering result.
 */
public class NonFuzzynessIndex<T> extends PartitionCoefficient<T>
{
	/**
	 * @param clusterInfo
	 */
	public NonFuzzynessIndex(ClusteringInformation<T> clusterInfo)
	{
		super(clusterInfo);
	}

	public double index()
	{
		return 1.0d - (((double)this.clusterInfo.getClusterCount())/(((double)this.clusterInfo.getClusterCount()) - 1.0d))*(1.0d - super.index());
	}
}
