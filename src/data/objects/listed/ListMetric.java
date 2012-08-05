/**
 * TODO File Description
 */
package data.objects.listed;

import java.util.List;

import data.algebra.Metric;
import data.algebra.Norm;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ListMetric<T> implements Metric<List<T>>
{
	/** A norm of the base object type */
	protected Metric<T> metric;
	
	/** The number of elements in the list this vector space. */
	protected int listLength;

	/**
	 * @param norm
	 * @param listLength
	 */
	public ListMetric(Metric<T> metric, int listLength)
	{
		this.metric = metric;
		this.listLength = listLength;
	}
	
	
	/* (non-Javadoc)
	 * @see data.algebra.Metric#distance(java.lang.Object)
	 */
	@Override
	public double distance(List<T> x, List<T> y)
	{
		double dist = 0.0d;		
		for(int i=0; i<this.listLength; i++) dist += this.metric.distance(x.get(i), y.get(i));		
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
}
