/**
 * TODO File Description
 */
package datamining.gradient.functions;

import data.objects.doubleArray.DAEuclideanVectorSpace;
import data.set.DataSetNotSealedException;
import data.set.IndexedDataSet;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 * @param <D>
 */
public class DALeastSquaresObjectiveFunction implements GradientFunction<double[], double[]>
{
	protected final DAEuclideanVectorSpace vs;
	
	/**
	 * @param c
	 */
	public DALeastSquaresObjectiveFunction(DALeastSquaresObjectiveFunction c)
	{
		this.vs = c.vs;
	}

	/**
	 * @param data
	 * @throws DataSetNotSealedException
	 */
	public DALeastSquaresObjectiveFunction(int dimension) throws DataSetNotSealedException
	{
		this.vs = new DAEuclideanVectorSpace(dimension);
	}

	public double functionValue(IndexedDataSet<double[]> dataSet, double[] parameter)
	{
		double result = 0.0d;
		
		for(int i=0; i<dataSet.size(); i++)
		{
			result += this.vs.distanceSq(dataSet.get(i).x, parameter);
		}
		result /= dataSet.size();
		
		return result;
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.GradientFunction#gradient(java.lang.Object)
	 */
	@Override
	public double[] gradient(IndexedDataSet<double[]> dataSet, double[] parameter)
	{
		double[] y = this.vs.getNewAddNeutralElement();
		double[] tmp = this.vs.getNewAddNeutralElement();

		for(int i=0; i<dataSet.size(); i++)
		{
			this.vs.copy(tmp, parameter);
			this.vs.sub(tmp, dataSet.get(i).x);
			this.vs.add(y, tmp);
		}
		
		this.vs.mul(y, 2.0d/dataSet.size());
		
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
