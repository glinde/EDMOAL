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


package data.set;

/**
 * The interface for a static data structure. After the data structure is build, nofurther changes are allowed.
 * Therefore, it is sealed. Any attempt in changing the data structure should result in a <code>ChangeNotAllowedException</code>.
 * The change counter counts the current number of registered changes.
 * 
 * @see ChangeNotAllowedException
 * @author Roland Winkler
 */
public interface Sealable
{
	/** Seals the instance, no changes are allowed after sealing it. */
	public void seal();
	
	/**
	 * @return true if the instance is sealed, false otherwise.
	 */
	public boolean isSealed();
	
	/** 
	 * Registers a change of the instance (increments the change counter). This function should be called
	 * BEFORE any change actually takes place. Registering a change without changing anything is not regarded as harmful.
	 * On the other hand, changing something and not registering it is regarded as harmful. The implementation of this function
	 * is also supposed to check whether a change is allowed (which is the case if the object is not jut sealed). If it is 
	 * sealed, this function produces a <code>ChangeNotAllowedException</code>.
	 * 
	 * @throws ChangeNotAllowedException if the instance is sealed.
	 */
	public void registerChange() throws ChangeNotAllowedException;
	
	/**
	 * @return the change counter which is incremented whenever the instance is changed. 
	 */
	public long getChangeCounter();
}
