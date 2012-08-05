/**
 * TODO File Description
 */
package data.objects.listed;

import java.util.ArrayList;
import java.util.List;

import data.algebra.ScalarProduct;
import data.algebra.VectorSpace;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ListScalarProduct<T> implements ScalarProduct<List<T>>
{
	/** A scalar product of the base object type */
	protected ScalarProduct<T> sp;
	
	/** The number of elements in the list this vector space. */
	protected int listLength;

	/**
	 * @param sp
	 * @param listLength
	 */
	public ListScalarProduct(ScalarProduct<T> sp, int listLength)
	{
		this.sp = sp;
		this.listLength = listLength;
	}
	
	/* (non-Javadoc)
	 * @see data.algebra.ScalarProduct#scalarProduct(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double scalarProduct(List<T> x, List<T> y)
	{
		double scalarProduct = 0.0d;		
		for(int i=0; i<this.listLength; i++) scalarProduct += this.sp.scalarProduct(x.get(i), y.get(i));		
		return scalarProduct;
	}

}
