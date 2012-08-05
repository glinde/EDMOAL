/**
 * TODO File Description
 */
package datamining.gradient.functions;

import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataSet;
import etc.SimpleStatistics;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class RelativeVarianceOfDistancesObjectiveFunction implements GradientFunction<double[], double[]>
{
	protected final DAEuclideanVectorSpace vs;
	
	
	/**
	 * @param c
	 */
	public RelativeVarianceOfDistancesObjectiveFunction(RelativeVarianceOfDistancesObjectiveFunction c)
	{
		this.vs = c.vs;
	}

	/**
	 * @param dimension
	 */
	public RelativeVarianceOfDistancesObjectiveFunction(int dimension)
	{		
		this.vs = new DAEuclideanVectorSpace(dimension);
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.GradientFunction#functionValue(java.lang.Object)
	 */
	@Override
	public double functionValue(IndexedDataSet<double[]> dataSet, double[] parameter)
	{
		double[] distances = new double[dataSet.size()];
		
		for(int j=0; j<dataSet.size(); j++)
		{
			distances[j] = this.vs.distance(parameter, dataSet.get(j).x);
		}
		
		double[] meanVar = SimpleStatistics.mean_variance(distances);
		
		return meanVar[1]/(meanVar[0] * meanVar[0]);
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.GradientFunction#gradient(java.lang.Object)
	 */
	@Override
	public double[] gradient(IndexedDataSet<double[]> dataSet, double[] parameter)
	{
		double[] y = this.vs.getNewAddNeutralElement();
		
		// TODO: complete function
		
		return y;
	}

	/**
	 * @return the vs
	 */
	public DAEuclideanVectorSpace getVs()
	{
		return this.vs;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.GradientFunction#getName()
	 */
	@Override
	public String getName()
	{
		return "Least Squares for Euclidean Real Vector Space";
	}

}
