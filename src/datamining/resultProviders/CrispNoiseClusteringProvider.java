


package datamining.resultProviders;

import data.set.IndexedDataObject;

/**
 * The use of this interface states, that a clustering algorithm is using a noise cluster in order to specify outliers.
 * A data object can not be both, crisply assigned and noise. A data object can be in one of three states:
 * unassigned and not noise, assigned and noise, assigned and not noise. It can not be unassigned and noise, because
 * if a data object is detected as noise, it is assigned to the noise cluster. Therefore, the state of unassigned and noise is
 * equivalent to the state of assigned and noise.
 * 
 * @author Roland Winkler
 */
public interface CrispNoiseClusteringProvider<T> extends CrispClusteringProvider<T>
{
	/**
	 * Returns a list that states for all data objects whether or not they are assigned to the noise cluster or not. 
	 * 
	 * @return a list that states for all data objects whether or not they are assigned to the noise cluster or not.
	 */
	public boolean[] getCrispNoiseAssignments();
	
	/**
	 * Determines for the specified data object if it is assigned to the noise cluster or not.
	 * 
	 * @param obj the data object.
	 * @return true, if the data object is assigned to the noise cluster, false otherwise.
	 */
	public boolean isCrispNoiseAssigned(IndexedDataObject<T> obj);
}