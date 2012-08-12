


package datamining.resultProviders;

import data.set.IndexedDataObject;

/**
 * Extends the interface of {@link FuzzyClusteringProvider} to handle a noise cluster.
 * It is similar to the {@link CrispNoiseClusteringProvider} interface, just for fuzzy assignments.<br>
 * 
 * The noise cluster is regarded as an additional cluster, but is not build into the usual cluster index structure.
 * Therefore, the noise fuzzy values are handled separated from the usual fuzzy cluster values.
 *
 * @author Roland Winkler
 */
public interface FuzzyNoiseClusteringProvider<T> extends FuzzyClusteringProvider<T>
{
	/**
	 * Returns a list of fuzzy noise assignment values for all data objects.
	 * 
	 * @return a list of fuzzy noise assignment values for all data objects.
	 */
	public double[] getFuzzyNoiseAssignments();
	
	/**
	 * Returns a the fuzzy noise assignment value for the specified data object.
	 * 
	 * @param obj the data object.
	 * @return a the fuzzy noise assignment value for the specified data object.
	 */
	public double getFuzzyNoiseAssignmentOf(IndexedDataObject<T> obj);
}
