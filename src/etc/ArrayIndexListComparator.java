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


package etc;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Similar to the {@link ArrayIndexComparator}, but this comparator class
 * specifies a list of indices. If the compared double arrays are equal at the first index,
 * the second index of the index list is compared. If these are equal too, the third is
 * compared etc. until no more indeces are left in the list.
 *
 * @author Roland Winkler
 */
public class ArrayIndexListComparator implements Comparator<double[]>, Serializable
{
	/**  */
	private static final long	serialVersionUID	= 5822489769788186713L;
	
	/**
	 * The list of indices that are used for comparing double arrays.
	 */
	private int[] indexList;
	
	/**
	 * A list of boolean values describing whether or not to sort increasing or decreasing.
	 */
	private boolean[] decreasingList;
	
	/**
	 * Creates a new Comparator for the cpecified list of indices.
	 * 
	 * @param indexList The list of indices that are used for comparing double arrays.
	 */
	public ArrayIndexListComparator(int[] indexList)
	{
		this.indexList = indexList.clone();
		
		this.decreasingList = new boolean[indexList.length];
	}
	
	/**
	 * Creates a new Comparator for the cpecified list of indices.
	 * 
	 * @param indexList The list of indices that are used for comparing double arrays.
	 * @param decreasingList A list of boolean values, specifying for each index if it is sorted decreasingly (true entry) or increasingly (false entry)
	 */
	public ArrayIndexListComparator(int[] indexList, boolean[] decreasingList)
	{
		this.indexList = indexList.clone();
		
		this.decreasingList = decreasingList.clone();
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(double[] a, double[] b)
	{
		int i=0, j=0;
		
//		for(i=0, j=this.indexList[i]; i<this.indexList.length && a[j]==b[j]; j=this.indexList[i], i++) ;

		for(i=0; i<this.indexList.length; i++)
		{
			j = this.indexList[i];
			if(a[j]!=b[j]) break;
		}
		
		if(i >= this.indexList.length) return 0;
		
		if(this.decreasingList[i])
			return (a[j]==b[j])?0:((a[j]>b[j])?-1:1);
		else
			return (a[j]==b[j])?0:((a[j]<b[j])?-1:1);
	}

	/**
	 * Returns the index list.
	 * 
	 * @return the index list.
	 */
	public int[] getIndexList()
	{
		return this.indexList;
	}

	/**
	 * Sets the index list.
	 * 
	 * @param indexList The indexList to set.
	 */
	public void setIndexList(int[] indexList)
	{
		this.indexList = indexList.clone();
	}

	/**
	 * @return the decreasingList
	 */
	public boolean[] getDecreasingList()
	{
		return decreasingList;
	}

	/**
	 * @param decreasingList the decreasingList to set
	 */
	public void setDecreasingList(boolean[] decreasingList)
	{
		this.decreasingList = decreasingList;
	}

	
}
