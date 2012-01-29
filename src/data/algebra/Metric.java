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

package data.algebra;


/**
 * The metric is an algebraic structure that provides distance information for objects of the types <code>T</code>.<br>
 * 
 * The interface contains two functions, <code>distance</code> and <code>distanceSq</code>.
 * Often, it is computationally cheaper to calculate the square distance between objects (e.g. Euclidean distance),
 * therefore the interface requires the squared distance as well as the normal distance to be implemented.<br>
 *  
 *
 * <ul> It is necessary for the function <code>distance(x, y)</code> to hold the typical metric conditions:
 * <li> distance(x, y) >= 0  </li>
 * <li> distance(x, y) == 0 if and only if x.equals(y) == true </li>
 * <li> distance(x, y) == distance(y, x) and </li>
 * <li> distance(x, y) + distance(y, z) >= distance(x, z) </li>
 * </ul>
 *  
 * @author Roland Winkler
 */
public interface Metric<T> extends AlgebraicStructure<T>
{
	/**
	 * The distance from object <code>x</code> to object <code>y</code>
	 * 
	 * @param x an object of type <code>T</code> 
	 * @param y an object of type <code>T</code>
	 * @return the distance between <code>x</code> and <code>y</code>
	 */
	public double distance(T x, T y);

	/**
	 * The squared distance from object <code>x</code> to object <code>y</code>
	 * 
	 * @param x an object of type <code>T</code> 
	 * @param y an object of type <code>T</code>
	 * @return the squared distance between <code>x</code> and <code>y</code>
	 */
	public double distanceSq(T x, T y);
}
