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

import java.io.Serializable;

/**
 * An abstract implementation for <code>Sealable</code> objects. It provides all functionalities
 * connected to the <code>Sealable</code> interface. All functions, modifying any field of the
 * subclass just need to call {@link Sealable#registerModification()}.
 *
 * @author Roland Winkler
 */
public abstract class AbstractSealable implements Sealable, Serializable
{

	/**  */
	private static final long	serialVersionUID	= -4169070675125392714L;

	/** A counter for the number of modifications to this object */
	private transient long modificationCounter;
	
	/** Indicates whether or not this object is sealed. */
	private boolean sealed; 

	/** Returns a new AbstractSealable object with 0 modifications. */
	public AbstractSealable()
	{
		this.modificationCounter = 0L;
		this.sealed = false;
	}
	

	/* (non-Javadoc)
	 * @see data.set.Sealable#registerModification()
	 */
	@Override
	public final void registerModification() throws ModificationNotAllowedException
	{
		if(this.sealed) throw new ModificationNotAllowedException("The instance is sealed and can not be modified any more.");
		
		this.modificationCounter++;
	}
	
	/* (non-Javadoc)
	 * @see data.Sealable#getChangeCounter()
	 */
	@Override
	public final long getModificationCounter()
	{
		return this.modificationCounter;
	}

	/* (non-Javadoc)
	 * @see data.set.Sealable#isSealed()
	 */
	@Override
	public final boolean isSealed()
	{
		return this.sealed;
	}

	/* (non-Javadoc)
	 * @see data.Sealable#seal()
	 */
	@Override
	public final void seal()
	{
		this.sealed = true;
	}
}
