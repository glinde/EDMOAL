/**
 * TODO File Description
 */
package datamining.gradient.centroid;

import java.util.ArrayList;
import java.util.List;

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
public class CentroidGradientOptimizationAlgorithm<D> extends AbstractGradientOptimizationAlgorithm<D, List<D>>
{
	protected ArrayList<Centroid<D>> centroids;
	
	
	/**
	 * @param c
	 */
	public CentroidGradientOptimizationAlgorithm(CentroidGradientOptimizationAlgorithm<D> c)
	{
		super(c);
		
		this.centroids = new ArrayList<Centroid<D>>(c.centroids.size());
		for(Centroid<D> centr:c.centroids) this.centroids.add(centr.clone());
	}

	/**
	 * @param data
	 * @param vs
	 * @param parameterMetric
	 * @param objectiveFunction
	 * @throws DataSetNotSealedException
	 */
	public CentroidGradientOptimizationAlgorithm(IndexedDataSet<D> data, VectorSpace<List<D>> vs, Metric<List<D>> parameterMetric, GradientFunction<D, List<D>> objectiveFunction) throws DataSetNotSealedException
	{
		super(data, vs, parameterMetric, objectiveFunction);
	}

	/* (non-Javadoc)
	 * @see datamining.DataMiningAlgorithm#algorithmName()
	 */
	@Override
	public String algorithmName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#getParameter()
	 */
	@Override
	public ArrayList<D> getParameter()
	{
		ArrayList<D> parameter = new ArrayList<D>(this.centroids.size());
		for(Centroid<D> centr:this.centroids) parameter.add(centr.getPosition());
		
		return parameter;
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#updateParameter(java.lang.Object)
	 */
	@Override
	public void updateParameter(List<D> parameter)
	{
		if(parameter.size() != this.centroids.size()) throw new IllegalArgumentException("Number of Parameters does not match number of Centroids. Parametersize: " + parameter.size() + " Centroids: " + this.centroids.size());
		
		for(int i=0; i<this.centroids.size(); i++) this.centroids.get(i).moveTo(parameter.get(i));
	}

	/* (non-Javadoc)
	 * @see datamining.ParameterOptimization#initializeWith(java.lang.Object)
	 */
	@Override
	public void initializeWith(List<D> initialParameter)
	{
		if(initialParameter.size() != this.centroids.size()) throw new IllegalArgumentException("Number of Parameters does not match number of Centroids. Parametersize: " + initialParameter.size() + " Centroids: " + this.centroids.size());
		
		for(int i=0; i<this.centroids.size(); i++) this.centroids.get(i).initializeWithPosition(initialParameter.get(i));
	}

	/**
	 * @return the centroids
	 */
	public ArrayList<Centroid<D>> getCentroids()
	{
		return this.centroids;
	}
}
