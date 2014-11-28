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


package datamining.clustering.protoype;

import java.util.ArrayList;

import data.algebra.VectorSpace;
import datamining.clustering.Cluster;

/**
 * This interface specifies the functionality a prototype need to provide.
 * A prototype represents a cluster, or in other words, is an approximation of all data objects of a cluster.
 * Since the prototype represents a cluster, it is often associated with the cluster it self, which is
 * why this interface extends the {@link Cluster} interface.<br>
 * 
 * The functions, provided in this cluster are mostly related to the iterative nature of typical
 * prototype based clustering algorithms. Starting from an initialisation location, the
 * prototype is moved around until it settles in its final location. Therefore, the way of the 
 * prototype represents the sequence of locations the prototype inherits during the clustering process.  
 *
 * @author Roland Winkler
 */
public interface Prototype<T> extends Cluster, Cloneable
{
	/**
	 * Moves the prototype to the specified position. If activated, the new position is recorded in the way of the prototype.
	 * 
	 * @param pos The new position of the prototype.
	 */
	public void moveTo(T pos);
	
	/**
	 * Moves the prototype by the specified difference. The new position is then according to the
	 * vector space: <code>x<sub>new</sub> = x<sub>old</sub> + x<sub>dif</sub></code>.  If
	 * activated, the new position is recorded in the way of the prototype.
	 * 
	 * @param dif The difference of the current prototype location to its new location.
	 */
	public void moveBy(T dif);
	
	/**
	 * Resets the prototype to its initial position. All history of the prototypes movements will be removed.
	 */
	public void resetToInitialPosition();
	
	/**
	 * Initialises the prototype with the specified position. If it was moved before,
	 * all history of the prototypes movements will be removed.
	 * 
	 * @param pos The position the prototype should be initialised with.
	 */
	public void initializeWithPosition(T pos);

	/**
	 * Returns the initial position.
	 * 
	 * @return The initial position.
	 */
	public T getInitialPosition();

	/**
	 * Returns the recorded way of the prototype. That is, the recorded locations the prototype
	 * moved to during the clustering process.
	 * 
	 * @return The recorded way of the prototype.
	 */
	public ArrayList<T> getWay();
		
	/**
	 * Returns true, if the way is recorded, false otherwise.
	 * 
	 * @return True, if the way is recorded, false otherwise.
	 */
	public boolean isRecordWay();

	/**
	 * If it is set to true, the way is recorded when calling the functins {@link #moveTo(Object)} or {@link #moveBy(Object)}. 
	 * 
	 * @param recordWay Whether or not to record the way of the prototype.
	 */
	public void setRecordWay(boolean recordWay);
	
	/**
	 * Returns the current position of the prototype.
	 * 
	 * @return The current position of the prototype.
	 */
	public T getPosition();

	/**
	 * Sets the current position of the prototype. This function does not record the way
	 * of the prototype.
	 * 
	 * @param pos The new position of the prototype.
	 */
	public void setPosition(T pos);
	
	/**
	 * Returns the vector space, associated with the prototype. 
	 * 
	 * @return The vector space, associated with the prototype.
	 */
	public VectorSpace<T> getVectorSpace();

	/**
	 * Clones the prototype.
	 * 
	 * @return A new prototype Object that is identical to this one.
	 */
	public Prototype<T> clone();
}
