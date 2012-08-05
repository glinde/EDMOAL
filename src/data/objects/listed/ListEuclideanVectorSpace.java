/**
 * TODO File Description
 */
package data.objects.listed;

import java.util.List;

import data.algebra.EuclideanVectorSpace;
import data.algebra.VectorSpace;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ListEuclideanVectorSpace<T> extends ListVectorSpace<T> implements EuclideanVectorSpace<List<T>>
{
	protected EuclideanVectorSpace<T> evs;
	
	/**
	 * @param vs
	 * @param listLength
	 */
	public ListEuclideanVectorSpace(EuclideanVectorSpace<T> evs, int listLength)
	{
		super(evs, listLength);
		
		this.evs = evs;
	}

	
	/* (non-Javadoc)
	 * @see data.algebra.Metric#distance(java.lang.Object)
	 */
	@Override
	public double distance(List<T> x, List<T> y)
	{
		double dist = 0.0d;		
		for(int i=0; i<this.listLength; i++) dist += this.evs.distance(x.get(i), y.get(i));		
		return dist;
	}

	/* (non-Javadoc)
	 * @see data.algebra.Metric#distanceSq(java.lang.Object)
	 */
	@Override
	public double distanceSq(List<T> x, List<T> y)
	{
		double dist = this.distance(x, y);		
		return dist*dist;
	}

	/* (non-Javadoc)
	 * @see data.algebra.Norm#length(java.lang.Object)
	 */
	@Override
	public double length(List<T> x)
	{
		double length = 0.0d;		
		for(int i=0; i<this.listLength; i++) length += this.evs.length(x.get(i));		
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

	/* (non-Javadoc)
	 * @see data.algebra.ScalarProduct#scalarProduct(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double scalarProduct(List<T> x, List<T> y)
	{
		double scalarProduct = 0.0d;		
		for(int i=0; i<this.listLength; i++) scalarProduct += this.evs.scalarProduct(x.get(i), y.get(i));		
		return scalarProduct;
	}
	
}
