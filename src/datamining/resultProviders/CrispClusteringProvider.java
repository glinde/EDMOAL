
package datamining.resultProviders;

import data.set.IndexedDataObject;
import data.set.IndexedDataSet;

/**
 * A crisp clustering algorithm creates a hard partitioning of the data set.
 * Each data object is assigned to exactly one cluster or remains unassigned.
 * The assignment of a data object is equal to the index of the cluster that it is assigned to.<br>
 * 
 * This interface provides the functions to gain access to the crisp cluster indices, which are essentially
 * the result of the clustering algorithm.  
 *
 * @author Roland Winkler
 */
public interface CrispClusteringProvider<T>
{
	/** 
	 * if a data object is not assigned its cluster assignment index must be equal to
	 * <code>UNASSIGNED_INDEX</code>. The valid cluster assignments start with <code>0</code>.
	 *  */
	public static final int UNASSIGNED_INDEX = -1;
	
	/**
	 * Determines if a data object is crisply assigned to a cluster. That is, 
	 * the function returns true, if the cluster assignment index is larger or equal to 0.
	 * If the data object is not part of the {@link IndexedDataSet}, stored in the algorithm,
	 * the value of <code>UNASSIGNED_INDEX</code> is returned.
	 * 
	 * @param obj The data object for which the cluster assignent is supposed to be determined.
	 * @return true, if the data object is crisply assigned by this clustering algorithm
	 */
	public boolean isCrispAssigned(IndexedDataObject<T> obj);
	
	/**
	 * Returns the list of cluster assignments for all data objects.
	 * 
	 * @return the list of cluster assignments for all data objects.
	 */
	public int[] getAllCrispClusterAssignments();
	
	/**
	 * Returns the cluster assignment of the specified data object.
	 * If the data object is not part of the {@link IndexedDataSet}, stored in the algorithm,
	 * the value of <code>UNASSIGNED_INDEX</code> is returned.
	 * 
	 * @param obj The data object for which the cluster assignment should be returned.
	 * @return the cluster assignment.
	 */
	public int getCrispClusterAssignmentOf(IndexedDataObject<T> obj);
}
