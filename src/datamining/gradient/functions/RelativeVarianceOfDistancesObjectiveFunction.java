/**
Copyright (c) 2012, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
	roland.winkler@gmail.com
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * The name of Roland Winkler may not be used to endorse or promote products
		derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.
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
public class RelativeVarianceOfDistancesObjectiveFunction extends AbstractObjectiveFunction<double[], double[]>
{
	protected final DAEuclideanVectorSpace vs;
		
	/**
	 * @param c
	 */
	public RelativeVarianceOfDistancesObjectiveFunction(RelativeVarianceOfDistancesObjectiveFunction c)
	{
		super(c);
		this.vs = c.vs;
	}

	/**
	 * @param dimension
	 */
	public RelativeVarianceOfDistancesObjectiveFunction(IndexedDataSet<double[]> dataSet) throws DataSetNotSealedException
	{		
		super(dataSet, new double[dataSet.first().x.length]);
		this.vs = new DAEuclideanVectorSpace(dataSet.first().x.length);
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#functionValue(data.set.IndexedDataSet, java.lang.Object)
	 */
	@Override
	public double functionValue()
	{
		double[] distances = new double[this.getDataCount()];
		
		for(int j=0; j<this.getDataCount(); j++)
		{
			distances[j] = this.vs.distanceSq(this.parameter, this.data.get(j).x);
		}
		
		double[] meanVar = SimpleStatistics.mean_variance(distances);
		
		return meanVar[1]/(meanVar[0] * meanVar[0]);
	}

	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object)
	 */
	@Override
	public double[] gradient()
	{
		double[] grad = this.vs.getNewAddNeutralElement();
		
		this.gradient(grad);
		
		return grad;
	}
	
	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object, java.lang.Object)
	 */
//	@Override
//	public void gradient(double[] gradient)
//	{
//		double[] distances = new double[this.getDataCount()];
//		double[] distancesSq = new double[this.getDataCount()];
//		
//		for(int j=0; j<this.getDataCount(); j++)
//		{
//			distances[j] = this.vs.distanceSq(parameter, this.data.get(j).x);
//			distancesSq[j] = distances[j] * distances[j];
//		}
//		
//		
//		double mean = SimpleStatistics.mean(distances);
//		double meanSq = SimpleStatistics.mean(distancesSq);
//		
//		double factor = 4.0d/((this.getDataCount() - 1) * (mean*mean) * this.vs.getDimension());
//		double[] gradTMP = this.vs.getNewAddNeutralElement();
//		double[] gradTMP2 = this.vs.getNewAddNeutralElement();
//
//		for(int j=0; j<this.getDataCount(); j++)
//		{
//			this.vs.copy(gradTMP, this.parameter);
//			this.vs.sub(gradTMP, this.getDataSet().get(j).x);
//			this.vs.mul(gradTMP, distancesSq[j]);
//			
//			this.vs.add(gradTMP2, gradTMP);
//		}
//
//		this.vs.resetToAddNeutralElement(gradient);
//		this.vs.add(gradient, gradTMP2);
//		
//
//		for(int j=0; j<this.getDataCount(); j++)
//		{
//			this.vs.copy(gradTMP, this.parameter);
//			this.vs.sub(gradTMP, this.getDataSet().get(j).x);			
//			this.vs.add(gradTMP2, gradTMP);
//		}
//
//		this.vs.mul(gradTMP2, meanSq/mean);
//		this.vs.sub(gradient, gradTMP2);
//
//		this.vs.mul(gradient, factor);
//	}
	
	/* (non-Javadoc)
	 * @see datamining.gradient.functions.GradientFunction#gradient(data.set.IndexedDataSet, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void gradient(double[] gradient)
	{
		double[] distancesSq = new double[this.getDataCount()];
		double sumSq = 0.0d;
		double sumQu = 0.0d;
		
		for(int j=0; j<this.getDataCount(); j++)
		{
			distancesSq[j] = this.vs.distanceSq(this.parameter, this.data.get(j).x);		
			sumSq += distancesSq[j]; 
			sumQu += distancesSq[j]*distancesSq[j];
		}		
				
		double factor = 4.0d*this.getDataCount();
		double[] tmp = this.vs.getNewAddNeutralElement();
		double[] tmpA = this.vs.getNewAddNeutralElement();
		double[] tmpB = this.vs.getNewAddNeutralElement();

		for(int j=0; j<this.getDataCount(); j++)
		{
			this.vs.copy(tmp, this.parameter);
			this.vs.sub(tmp, this.getDataSet().get(j).x);
			this.vs.mul(tmp, distancesSq[j]);
			this.vs.add(tmpA, tmp);
			
			this.vs.copy(tmp, this.parameter);
			this.vs.sub(tmp, this.getDataSet().get(j).x);
			this.vs.add(tmpB, tmp);
		}

		this.vs.mul(tmpA, 1.0d/(sumSq*sumSq));
		this.vs.mul(tmpB, sumQu/(sumSq*sumSq*sumSq));
				
		this.vs.copy(gradient, tmpA);
		this.vs.sub(gradient, tmpB);
		this.vs.mul(gradient, factor);
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
		return "Relative Variance Objective Function";
	}

}
