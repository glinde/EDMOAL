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
