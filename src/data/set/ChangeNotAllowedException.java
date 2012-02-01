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
 * This exception indicates that there was an attempt to modify a sealed object.
 *
 * @author Roland Winkler
 * 
 * @see Sealable
 */
public class ChangeNotAllowedException extends UnsupportedOperationException
{
	/**  */
	private static final long	serialVersionUID	= 2649010021902043363L;

	/**
     * Constructs an IteratorModificationsNotSupportedException with no details.
     * 
     * @see UnsupportedOperationException
	 */
	public ChangeNotAllowedException()
	{
		super();
	}

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param  message the detail message
     * @param  cause the cause 
     * 
     * @see UnsupportedOperationException
	 */
	public ChangeNotAllowedException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	
    /**
     * Constructs an IteratorModificationsNotSupportedException with the specified detail message.
     *
     * @param message the detail message
     * 
     * @see UnsupportedOperationException
     */
	public ChangeNotAllowedException(String message)
	{
		super(message);
	}


    /**
     * Constructs a new exception with the specified cause.
     *
     * @param  cause the cause
     */
	public ChangeNotAllowedException(Throwable cause)
	{
		super(cause);
	}
	
}
