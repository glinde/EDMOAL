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
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public interface VectorSpace<T> extends AlgebraicStructure<T>
{
	/**
	 * @return the vector space element that is neutral w.r.t. add
	 */
	public T getNewAddNeutralElement();

	/**
	 * @return resets the vector to its add-neutral element
	 */
	public void resetToAddNeutralElement(T x);
	
	/** copies the content of y into x (x := y)
	 * 
	 * @param x
	 * @param y
	 */
	public void copy(T x, T y);
	
	/**
	 * inverts x (x := -x)
	 * 
	 * @param x
	 */
	public void inv(T x);
	
	/**
	 * Adds y to x (x := x + y)
	 * 
	 * @param x
	 * @param y
	 */
	public void add(T x, T y);

	/**
	 * Subtracts y from x (x := x - y)
	 * 
	 * @param x
	 * @param y
	 */
	public void sub(T x, T y);
	
	/** multiplies x with a (x := a * x)
	 * 
	 * @param x
	 * @param a
	 */
	public void mul(T x, double a);

	/**
	 * copies x (z := x) 
	 * 
	 * @param x
	 * @return z
	 */
	public T copyNew(T x);
	
	/**
	 * inverts x (z := -x)
	 * 
	 * @param x
	 * @return z
	 */
	public T invNew(T x);
	
	/**
	 * Adds y and x (z := x + y)
	 * 
	 * @param x
	 * @param y
	 * @return z
	 */
	public T addNew(T x, T y);

	/** Subtracts y from x (z := x - y)
	 * 
	 * @param x
	 * @param y
	 * @return z
	 */
	public T subNew(T x, T y);
	
	/** multiplies x with a (z := a * x)
	 * 
	 * @param a
	 * @param x
	 * @return z
	 */
	public T mulNew(T x, double a);
	
	/**
	 * Gets the dimension of the vector space (i.e. number of degrees of freedom, number of linear independent basis vectors)
	 * Note that the dimension can be positive infinite.
	 * 
	 * @return the dimension 
	 */
	public int getDimension();
}
