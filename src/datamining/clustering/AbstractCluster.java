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


package datamining.clustering;

/**
 * An abstract class for the basic functionality a cluster should provide.
 *
 * @author Roland Winkler
 */
public class AbstractCluster implements Cluster, Cloneable
{
	/**  */
	private static final long	serialVersionUID	= -1818571761181329327L;

	/** States whether or not the cluster is activated */
	protected boolean activated;
	
	/** The index of the cluster */
	protected int clusterIndex;
	
	/** The standard constructor. 
	 * 
	 * @param index The index of the cluster.
	 */
	public AbstractCluster(int index)
	{
		this.activated = true;
	}
	
	/** The copy constructor */
	public AbstractCluster(AbstractCluster cluster)
	{
		this.activated = cluster.activated;
		this.clusterIndex = cluster.clusterIndex;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.Cluster#isActivated()
	 */
	@Override
	public boolean isActivated()
	{
		return this.activated;
	}

	/* (non-Javadoc)
	 * @see datamining.clustering.Cluster#setActivated(boolean)
	 */
	@Override
	public void setActivated(boolean activated)
	{
		this.activated = activated;
	}
	
	/* (non-Javadoc)
	 * @see datamining.clustering.Cluster#getClusterIndex()
	 */
	@Override
	public int getClusterIndex()
	{
		return this.clusterIndex;
	}

	
	/* (non-Javadoc)
	 * @see datamining.clustering.Cluster#setClusterIndex(int)
	 */
	@Override
	public void setClusterIndex(int index)
	{
		this.clusterIndex = index;
	}
	
	/**
	 * @TODO: remove.
	 * 
	 * @param clone
	 */
	public void clone(AbstractCluster clone)
	{
		clone.activated = this.activated;
		clone.clusterIndex = this.clusterIndex;
	}
}
