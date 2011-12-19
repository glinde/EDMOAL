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


package datamining.frequentDataMining;

import java.io.Serializable;
import java.util.BitSet;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class Transaction implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -183908582757654969L;
	protected BitSet itemSet;

	/**
	 * @param transactions
	 */
	public Transaction(BitSet transactions)
	{
		this.itemSet = transactions;
	}

	/**
	 * @return
	 * @see dataStructures.BitArray#countBitSetOccurances()
	 */
	public int countBitSetOccurances()
	{
		return this.itemSet.cardinality();
	}

//	/**
//	 * @return
//	 * @see dataStructures.BitArray#getLength()
//	 */
//	public int getBitCount()
//	{
//		return this.itemSet.length();
//	}

	/**
	 * @return
	 * @see dataStructures.BitArray#getHighestBitIndex()
	 */
	public int getHighestBitIndex()
	{
		return this.itemSet.length()-1;
	}

	/**
	 * @param bArray
	 * @return
	 * @see dataStructures.BitArray#isEqual(dataStructures.BitArray)
	 */
	public boolean isEqual(BitSet bArray)
	{
		return this.itemSet.equals(bArray);
	}

	/**
	 * @param bArray
	 * @return
	 * @see dataStructures.BitArray#isSubSet(dataStructures.BitArray)
	 */
	public boolean isSubSet(BitSet bArray)
	{
		BitSet clone = (BitSet)this.itemSet.clone();
		clone.and(bArray);
		return this.itemSet.equals(clone);
	}

	/**
	 * @param bArray
	 * @return
	 * @see dataStructures.BitArray#isSuperSet(dataStructures.BitArray)
	 */
	public boolean isSuperSet(BitSet bArray)
	{
		BitSet clone = (BitSet)this.itemSet.clone();
		clone.and(bArray);
		return bArray.equals(clone);
	}

	/**
	 * @param i
	 * @see dataStructures.BitArray#remBit(int)
	 */
	public void remBit(int i)
	{
		this.itemSet.clear(i);
	}

//	/**
//	 * @param array
//	 * @see dataStructures.BitArray#setArray(int[])
//	 */
//	public void setArray(int[] array)
//	{
//		this.itemSet.setArray(array);
//	}

	/**
	 * @param i
	 * @see dataStructures.BitArray#setBit(int)
	 */
	public void setBit(int i)
	{
		this.itemSet.set(i);
	}

	/**
	 * @return the itemSet
	 */
	public BitSet getItemSet()
	{
		return this.itemSet;
	}

	/**
	 * @param itemSet the itemSet to set
	 */
	public void setItemSet(BitSet itemSet)
	{
		this.itemSet = itemSet;
	}
	
	public boolean containsItem(int i)
	{
		return this.itemSet.get(i);
	}

	/**
	 * @return
	 * @see dataStructures.BitArray#toString()
	 */
	public String toString()
	{
		return this.itemSet.toString();
	}
	
	public Transaction clone()
	{
		return new Transaction((BitSet)this.itemSet.clone());
	}
}
