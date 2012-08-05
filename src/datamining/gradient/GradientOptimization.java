/**
 * TODO File Description
 */
package datamining.gradient;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import datamining.DataMiningAlgorithm;
import datamining.IterativeObjectiveFunctionOptimization;
import datamining.ParameterOptimization;
import datamining.gradient.functions.GradientFunction;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public interface GradientOptimization<D, P> extends DataMiningAlgorithm<D>, ParameterOptimization<P>, IterativeObjectiveFunctionOptimization
{
	/**
	 * @return
	 */
	public VectorSpace<P> getParameterVectorSpace();
	
	/**
	 * @return the parameterMetric
	 */
	public Metric<P> getParameterMetric();
	
	
	/**
	 * @return the learningFactor
	 */
	public double getLearningFactor();

	/**
	 * @param learningFactor the learningFactor to set
	 */
	public void setLearningFactor(double learningFactor);
	
	/**
	 * @return the objectiveFunction
	 */
	public GradientFunction<D, P> getObjectiveFunction();

	/**
	 * Returns the parameter that specifies whether this is a gradient ascending or a gradient descending algorithm.<br>
	 * 
	/** If true: this is a gradient ascending algorithm. (parameter maximization) <br>
	 *  If false: this is a gradient descending algorithm. (parameter minimization)
	 * 
	 * @return the ascending/descending parameter.
	 */
	public boolean isAscOrDesc();

	/**
	 * Sets the parameter that specifies whether this is a gradient ascending or a gradient descending algorithm.<br>
	 * 
	/** If true: this is a gradient ascending algorithm. (parameter maximization) <br>
	 *  If false: this is a gradient descending algorithm. (parameter minimization)
	 * 
	 * @param descOrAsc the ascending/descending parameter to set.
	 */
	public void setAscOrDesc(boolean ascOrDesc);
}
