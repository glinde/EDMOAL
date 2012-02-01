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
 * This class is the second part of the backbone of the EDMOAL analysis structure. It is simply
 * a container for an unspecified object of type <code>T</code>. The <code>IndexedDataObject</code>
 * provides merely additional information in order to provide a consistent indexing of the data at hand.
 * It can be regarded as similar to the container of a linked list or a tree. However, there is one
 * very important difference. The containers of lists and trees are usually not accessed openly. They are masked
 * by the data structure (e.g. the List class) and all access to the data is channeled through the data structure.
 * This is not the case for the <code>IndexedDataObject</code>. It is supposed to be in the open. In fact,
 * when dealing with data in the EDMOAL project, data should always be "carried around" in the
 * container of the <code>IndexedDataObject</code> and organized in a <code>IndexedDataSet</code> and all
 * classes and methods concerned with the organization of the data should also only access these container classes.<br>
 * 
 * The reason for this is the index integrity <code>IndexedDataObject</code> and <code>IndexedDataSet</code> provides.
 * This is a design decision that is done to simplify the handling of data for many different algorithms. If an
 * algorithm wants to provide additional information for a data set (for example a classification),
 * it just needs to provide a list, containing this additional data. The raw data is unaffected. This way, many
 * different algorithms can simultaneously provide different information for the same data set. The
 * index integrity of the <code>IndexedDataSet</code> guarantees, that the additional information is valid
 * if the data set is sealed. So it basically provides a 1:1 relationship between indices and data objects.<br>
 * 
 * Of course, all operations regarding the contents of the data, are performed on the data it self and not
 * on the <code>IndexedDataObject</code>. For this reason, the classes in the data.algebra package
 * (which perform operations on the type <code>T</code>) are applied directly on the type <code>T</code> while
 * classes in the data.set package are applied on <code>IndexedDataObject</code>, because they are concerned
 * with the organization of the data.<br>
 * 
 * Since index integrity is the key, <code>IndexedDataObject</code> also stores its own index under which it is
 * stored in the <code>IndexedDataSet</code>. The reason is, that it simplifies many access operations.
 * The index integrity is provided in order to have easy access to additional external information. This
 * information must be accessible in a fast way which can be done using the index, stored in the 
 * <code>IndexedDataObject</code>. Therefore, it is enough to pass the <code>IndexedDataObject</code> around
 * to know which additional information are associated with it, without having a lookup-table. Also the index
 * defines an ordering of <code>IndexedDataObject</code>s that can be used to optimize data structures. Together with
 * the <code>IndexedDataSet.instanceCounter</code>, the index defines a global ordering of all 
 * <code>IndexedDataObjects</code>s that are assigned to a <code>IndexedDataset</code>.  
 *
 * @author Roland Winkler
 */
public class IndexedDataObject<T> implements Serializable, Comparable<IndexedDataObject<T>>
{
	/**  */
	private static final long	serialVersionUID	= 60723425937211221L;

	/** The data object that is stored in this container */
	public T element;
	
	/** The index of the data object */
	private int id;
	
	/** The <code>IndexedDataSet</code>, this data object is associated with  */
	private IndexedDataSet<T> dataSet;
	
	/** The constructor, that returns a new <code>IndexedDataObject</code> with a given element as content */
	public IndexedDataObject(T element)
	{
		this.id = -1;
		this.dataSet = null;
		this.element = element;
	}

	/**
	 * Gets the index of the data object
	 * 
	 * @return the index
	 */
	public int getID()
	{
		return id;
	}

	/**
	 * Sets the index for trusted package classes.
	 * 
	 * @param id the new index
	 */
	protected void setID(int id)
	{
		this.id = id;
	}
	
	/**
	 * Returns the <code>IndexedDataSet</code> this <code>IndexedDataObject</code> assigned to.
	 * 
	 * @return the <code>IndexedDataSet</code> this <code>IndexedDataObject</code> assigned to 
	 */
	public IndexedDataSet<T> getDataSet()
	{
		return this.dataSet;
	}

	/**
	 * Sets the <code>IndexedDataSet</code> for trusted package classes.
	 * 
	 * @param dataSet the new <code>IndexedDataSet</code>
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
	 * Checks if this data Object is associated with a <code>IndexedDataSet</code>.
	 * 
	 * @return true if this <code>IndexedDataObject</code> is contained in a <code>IndexedDataSet</code>
	 */
	public boolean isInSet()
	{
		return this.dataSet != null;
	}
	
	/**
	 * Removes the association with an <code>IndexedDataSet</code>.
	 */
	protected void clearDataSetConnection()
	{
		this.dataSet = null;
		this.id = -1;
	}
	
	/**
	 * Sets the index and associated <code>IndexedDataSet</code> for trusted package classes.
	 * 
	 * @param index the new index
	 * @param dataSet the new <code>IndexedDataSet</code>
	 */
	protected void setDataSetConnection(int index, IndexedDataSet<T> dataSet)
	{
		this.id = index;
		this.dataSet = dataSet;
	}
	
	/**
	 * Tests whether or not this <code>IndexedDataObject</code> is assigned to the specified <code>IndexedDataSet</code>. 
	 * 
	 * @param set The <code>IndexedDataSet</code> for which is tested if this data object is contained in it.
	 * @return true if this object is contained in the specified <code>IndexedDataSet</code>.
	 */
	public boolean isInSet(IndexedDataSet<T> set)
	{
		return this.dataSet == set;
	}
}
