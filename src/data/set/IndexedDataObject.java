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


package data.set;

import java.io.Serializable;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class IndexedDataObject<T> implements Serializable, Comparable<IndexedDataObject<T>>
{
	/**  */
	private static final long	serialVersionUID	= 60723425937211221L;

	public T element;
	
	/**  */
	private int id;
	
	/**  */
	private IndexedDataSet<T> dataSet;
	
	/** */
	public IndexedDataObject(T element)
	{
		this.id = -1;
		this.dataSet = null;
		this.element = element;
	}

	/**
	 * @return
	 */
	public int getID()
	{
		return id;
	}

	/**
	 * @param id
	 */
	protected void setID(int id)
	{
		this.id = id;
	}

	public IndexedDataSet<T> getDataSet()
	{
		return this.dataSet;
	}

	/**
	 * @param dataSet
	 */
	protected void setDataSet(IndexedDataSet<T> dataSet)
	{
		this.dataSet = dataSet;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IndexedDataObject<T> o)
	{
		return (this.dataSet == o.dataSet)? this.id - o.id :  this.dataSet.getInstanceNumber() - o.dataSet.getInstanceNumber();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object o)
	{
		if(!(o instanceof IndexedDataObject)) return false;
		
		return this.dataSet == ((IndexedDataObject<?>)o).dataSet && this.id == ((IndexedDataObject<?>)o).id;
	}
	
	/**
	 * @return
	 */
	public boolean isInSet()
	{
		return this.dataSet != null;
	}
	
	/**
	 * 
	 */
	protected void clearDataSetConnection()
	{
		this.dataSet = null;
		this.id = -1;
	}
	
	/**
	 * @param index
	 * @param dataSet
	 */
	protected void setDataSetConnection(int index, IndexedDataSet<T> dataSet)
	{
		this.id = index;
		this.dataSet = dataSet;
	}
	
	/**
	 * @param set
	 * @return
	 */
	public boolean isInSet(IndexedDataSet<T> set)
	{
		return this.dataSet == set;
	}
}
