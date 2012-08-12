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
package data.set;


/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public abstract class AbstractStaticDataSetContainer<T>
{
	
	/** The data set. It is final because a changing data set reference produces more problems than uses. */
	protected final IndexedDataSet<T> data;
		
	/**
	 * The standard constructor, taking the data set that is supposed to be contained.
	 * Classes extending this class expect a static data set. Therefore, it must be sealed and an
	 * exception is thrown if it is not sealed.
	 * 
	 * @param data The data set that is to be contained.
	 * 
	 * @throws DataSetNotSealedException if the data set is not sealed.
	 */
	public AbstractStaticDataSetContainer(IndexedDataSet<T> data) throws DataSetNotSealedException
	{
		if(!data.isSealed()) throw new DataSetNotSealedException("The data set is not sealed.");
		
		this.data = data;
	}
	
	/**
	 * The copy constructor. 
	 * 
	 * @param c The <code>AbstractStaticDataMiningAlgorithm</code> to be copied.
	 */
	public AbstractStaticDataSetContainer(AbstractStaticDataSetContainer<T> c)
	{
		this.data					= c.data;
	}
		

	/**
	 * Returns the number of data objects in the data set.
	 * 
	 * @return the number of data objects in the data set.
	 */
	public int getDataCount()
	{
		return this.data.size();
	}

	/**
	 * @TODO: remove.  
	 */
	public void clone(AbstractStaticDataSetContainer<T> clone)
	{}
	

	/**
	 * Returns the data set.
	 * 
	 * @return the data set.
	 */
	public IndexedDataSet<T> getDataSet()
	{
		return this.data;
	}
	
	
}
