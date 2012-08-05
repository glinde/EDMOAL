/**
 * TODO File Description
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
