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
 * The vector space is maybe one of the most frequently used algebraic structure for data mining.
 * Technically, there should be an other parameter specifying the algebraic body for which the vector space is specified.
 * But unless that is really required by any function, it is implicitly assumed that the body are the real numbers, represented
 * by double values. Therefore, the multiplication with a scalar is implemented for a double value.<br>
 * 
 * This interface provides the framework for all necessary operations, including copying a data object.
 * This is a very important feature because it provides the functionality the general <code>clone()</code> function
 * fails to provide.
 * Also technically the function <code>getNewAddNeutralElement()</code> is a factory function for type <code>T</code>
 * but as it is very useful (for example for calculating the sum of vectors), it is not separated into an other class.
 * This is additionally motivated by the fact that the add neutral element is uniquely defined for each vector space.
 * Similar the getting the add neutral element is getting the dimension of the vector space with <code>getDimension()</code>.
 * The dimension of a vector space is also always specified and therefore, the function must provide a useful answer.<br>
 * 
 * All other operations come in two variations, as inplace operation and as operation that returns a new object of type <code>T</code>.
 * The inplace operations should be implemented without any dynamic memory allocation in order to provide fast code. If that is
 * not possible, please state so in the javadoc of the specific vector space class.
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
	 * @param x the object that is overwritten
	 * @param y the object that is copied
	 */
	public void copy(T x, T y);
	
	/**
	 * Inplace inversion (x := -x)
	 * 
	 * @param x the object is overwritten with its own negative
	 */
	public void inv(T x);
	
	/**
	 * Inplace addition: adds y to x (x := x + y)
	 * 
	 * @param x the object that is overwritten
	 * @param y the object that is added to x
	 */
	public void add(T x, T y);

	/**
	 * Inplace subtraction: Subtracts y from x (x := x - y)
	 * 
	 * @param x the object that is overwritten
	 * @param y the object that is subtracted from x
	 */
	public void sub(T x, T y);
	
	/** Inplace multiplication: multiplies x with a (x := a * x)
	 * 
	 * @param x the object that is overwritten
	 * @param a the value that is multiplied to x
	 */
	public void mul(T x, double a);

	/**
	 * copies x (z := x) 
	 * 
	 * @param x The object to be copied
	 * @return z The new copy of x
	 */
	public T copyNew(T x);
	
	/**
	 * inverts x (z := -x)
	 * 
	 * @param x the object to be inverted
	 * @return z the new, inverted object
	 */
	public T invNew(T x);
	
	/**
	 * Adds y and x (z := x + y)
	 * 
	 * @param x the first parameter
	 * @param y the second parameter
	 * @return z  a new object that holds the result of x + y
	 */
	public T addNew(T x, T y);

	/** Subtracts y from x (z := x - y)
	 * 
	 * @param x the minuend object
	 * @param y the subtrahend object
	 * @return z a new object that holds the result of x - y
	 */
	public T subNew(T x, T y);
	
	/** multiplies x with a (z := a * x)
	 * 
	 * @param x the object that is multiplied
	 * @param a the factor by which x is multiplied
	 * @return z a new object that holds the result of a*x
	 */
	public T mulNew(T x, double a);
	
	/**
	 * Gets the dimension of the vector space (i.e. number of degrees of freedom, number of linear independent basis vectors)
	 * Note that the dimension can be infinite, in this case the value of this function does not hold any useful information.
	 * 
	 * @return the dimension 
	 * 
	 * @see VectorSpace#infiniteDimensionality()
	 */
	public int getDimension();
	
	/**
	 * In some cases, the dimensionality of the vector space is infinite. In this case, this function has to return true.
	 * Otherwise, it has to return false. The value of this function determines if the value of the function <code>getDimension()</code>
	 * is useful or not.
	 * 
	 * @return true if the dimensionality of the vector space is infinite, false otherwise.
	 * 
	 * @see VectorSpace#getDimension()
	 */
	public boolean infiniteDimensionality();
}
