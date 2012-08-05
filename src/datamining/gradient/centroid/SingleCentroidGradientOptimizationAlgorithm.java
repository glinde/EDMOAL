/**
 * TODO File Description
 */
package datamining.gradient.centroid;

import data.algebra.Metric;
import data.algebra.VectorSpace;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataSet;
import datamining.clustering.protoype.Centroid;
import datamining.gradient.AbstractGradientOptimizationAlgorithm;
import datamining.gradient.functions.GradientFunction;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class SingleCentroidGradientOptimizationAlgorithm<D> extends AbstractGradientOptimizationAlgorithm<D, D>
{
	/**
	 * Using the centroid class here to monitor the movement of the data object like parameter.
	 */
	protected Centroid<D> parameterCentroid; 

	/**
	 * The copy constructor. 
	 * 
	 * @param c The <code>AbstractSingleCentroidGOAlgorithm</code> to be copied.
	 */
	public SingleCentroidGradientOptimizationAlgorithm(SingleCentroidGradientOptimizationAlgorithm<D> c)
	{
		super(c);
		
		this.parameterCentroid = c.parameterCentroid.clone();
	}
	
	/**
	 * @param data
	 * @param vs
	 * @param oF
	 * @throws DataSetNotSealedException
	 */
	public SingleCentroidGradientOptimizationAlgorithm(IndexedDataSet<D> data, VectorSpace<D> vs, Metric<D> metric, GradientFunction<D, D> objectiveFunction) throws DataSetNotSealedException
	{
		super(data, vs, metric, objectiveFunction);
		
		this.parameterCentroid = new Centroid<D>(vs);
	}
	
	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#initializeWith(java.lang.Object)
	 */
	@Override
	public void initializeWith(D initialParameter)
	{
		this.parameterCentroid.initializeWithPosition(initialParameter);
		this.initialized = true;
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#getParameter()
	 */
	public D getParameter()
	{
		return this.parameterCentroid.getPosition();
	}
	
	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#updateParameter(java.lang.Object)
	 */
	public void updateParameter(D newParameter)
	{
		this.parameterCentroid.moveTo(newParameter);
	}

	
	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		return "Single Centroid Optimization Algorithm with: " + this.getObjectiveFunction().getName();
	}
	
	/**
	 * @return
	 */
	public Centroid<D> getCentroid()
	{
		return this.parameterCentroid;
	}
}
