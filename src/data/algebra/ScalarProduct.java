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
 * The scalar product is usually only specified for a vector space of type <code>T</code>. However,
 * it is not necessary to specify a vector space in order to provide the functionality
 * of a scalar product. <br>
 * 
 * Note, that in general, it is required to have an object of the inner type of a vector space as result of a scalar product.
 * So this is not the most general form of a scalar product that is possible.
 * Specifying an Object type as result of the scalar product (and x and y as vectors of this object type), it would prevent
 * using atomar data types as scalar product results. in particular, <code>double</code> would not be possible. To keep
 * the balance between generality and performance, only real values (ok, double-valued) scalar products are considered here.
 * If the extended functionality is required in the future, a complete rearrangement of the algebra package will be required. 
 *
 * @author Roland Winkler
 */
public interface ScalarProduct<T> extends Norm<T>, AlgebraicStructure<T>
{
	/**
	 * Applies the scalar product on two objects
	 * 
	 * @param x First object to calculate the scalar product with
	 * @param y Second object to calculate the scalar product with
	 * @return The (real) scalar product of x and y as double value.
	 */
	public double scalarProduct(T x, T y);
}
