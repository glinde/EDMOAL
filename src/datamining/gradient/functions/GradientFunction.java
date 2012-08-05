/**
 * TODO File Description
 */
package datamining.gradient.functions;

import data.set.IndexedDataSet;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public interface GradientFunction<D, P>
{
	public double functionValue(IndexedDataSet<D> dataSet, P parameter);
	
	public P gradient(IndexedDataSet<D> dataSet, P parameter);
	
	public String getName();
}
