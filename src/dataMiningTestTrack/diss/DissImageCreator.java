/**
Copyright (c) 2013, The EDMOAL Project

	Roland Winkler
	Richard-Wagner Str. 42
	10585 Berlin, Germany
	roland.winkler@gmail.com
 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
    	this list of conditions and the following disclaimer in the documentation and/or
    	other materials provided with the distribution.
    * The name of Roland Winkler may not be used to endorse or promote products
		derived from this software without specific prior written permission.

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
package dataMiningTestTrack.diss;

import dataMiningTestTrack.tests.TestVisualizer;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class DissImageCreator extends TestVisualizer
{
	public static void createMembershipPlotCoords()
	{
		DissMembershipPlotCoordsCreator plotter = new DissMembershipPlotCoordsCreator(2000);
		plotter.hcm();
		
		plotter.fcm(1.2d);
		plotter.fcm(2.0d);
		plotter.fcm(4.0d);
		
		plotter.nfcm(1.2d);
		plotter.nfcm(2.0d);
		plotter.nfcm(4.0d);
		
		plotter.pfcm(0.2d);
		plotter.pfcm(0.3d);
		plotter.pfcm(0.5d);
		
		plotter.pnfcm(0.2d);
		plotter.pnfcm(0.3d);
		plotter.pnfcm(0.5d);
		
		plotter.rcfcm(0.5d);
		plotter.rcfcm(0.7d);
		plotter.rcfcm(0.9d);
		
		plotter.rcnfcm(0.5d);
		plotter.rcnfcm(0.7d);
		plotter.rcnfcm(0.9d);
		
		plotter.emgmm(0.001d);
		plotter.emgmm(0.5d);
		plotter.emgmm(1.0d);
	}
	
	public static void createDissImages()
	{
		DissImageCreator.createMembershipPlotCoords();
	}
}
