/**
Copyright (c) 2011, The EDMOAL Project

	DLR Deutsches Zentrum fuer Luft- und Raumfahrt e.V.
	German Aerospace Center e.V.
	Institut fuer Flugfuehrung/Institute of Flight Guidance
	Tel. +49 531 295 2500, Fax: +49 531 295 2550
	WWW: http://www.dlr.de/fl/		
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * Neither the name of the DLR nor the names of its contributors
    	may be used to endorse or promote products derived from this software
    	without specific prior written permission.

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


package datamining;

/**
 * If an algorithm is both, iterative and optimizes an objective function, the objective function value
 * can be monitored over time. This interface provides the required meothods for classes like that. 
 * 
 * @author Roland Winkler
 */
public interface IterativeObjectiveFunctionOptimization extends IterativeAlgorithm, ObjectiveFunctionOptimization
{
	/**
	 * Causes the algorithm to record the current objective function value. Note that this function can
	 * be called at any time, even in between iteration steps.
	 */
	public void recordCurrentObjectiveFunctionValue();
	
	/**
	 * Returns the history of recorded objective function values. That is, all objective function values
	 * that have been stored over time in time-ascending order.
	 * 
	 * @return the history of recorded objective function values.
	 */
	public double[] getObjectiveFunctionValueHistory();
	
	/**
	 * States whether or not the objective function values are monitored after each iteration. 
	 * 
	 * @return true, if the objective function values are monitored, false otherwise. 
	 */
	public boolean isObjectiveFunctionMonitoring();
	
	/**
	 * Sets the objective function monitoring. If set to true, the objective function is monitored after each iteration.
	 * 
	 * @param monitor sets the monitoring of the objective function values.
	 */
	public void setObjectiveFunctionMonitoring(boolean monitor);
}
