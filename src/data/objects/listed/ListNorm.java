/**
 * TODO File Description
 */
package data.objects.listed;

import java.util.List;

import data.algebra.Norm;
import data.algebra.ScalarProduct;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ListNorm<T> implements Norm<List<T>>
{
	/** A norm of the base object type */
	protected Norm<T> norm;
	
	/** The number of elements in the list this vector space. */
	protected int listLength;

	/**
	 * @param norm
	 * @param listLength
	 */
	public ListNorm(Norm<T> norm, int listLength)
	{
		this.norm = norm;
		this.listLength = listLength;
	}
	
	
	/* (non-Javadoc)
	 * @see data.algebra.Norm#length(java.lang.Object)
	 */
	@Override
	public double length(List<T> x)
	{
		double length = 0.0d;		
		for(int i=0; i<this.listLength; i++) length += this.norm.length(x.get(i));		
		return length;
	}

	/* (non-Javadoc)
	 * @see data.algebra.Norm#lengthSq(java.lang.Object)
	 */
	@Override
	public double lengthSq(List<T> x)
	{
		double length = this.length(x);
		return length*length;
	}
}
