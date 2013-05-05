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

import generation.data.HyperrectangleUniformGenerator;
import io.CSVFileReader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import data.objects.doubleArray.DAEuclideanMetric;
import etc.Parallel;
import etc.SimpleStatistics;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ClusterPropertyTester
{

	public static double calculatMaxRV(ArrayList<double[]> data, int repetitions)
	{
		double maxRV = 0.0d;
		ArrayList<RVExperiment> rvExperimentList = new ArrayList<RVExperiment>();
		HyperrectangleUniformGenerator initGen = new HyperrectangleUniformGenerator(data);
		ArrayList<double[]> initpositions = initGen.generateDataObjects(repetitions);
		for(int i=0; i<repetitions; i++)
		{
			rvExperimentList.add(new RVExperiment(data.get(0).length, data, initpositions.get(i), 100));
		}
				
		Parallel.ForFJ(rvExperimentList, new Parallel.Operation<RVExperiment>(){
			public void perform(RVExperiment exp)
			{
				exp.applyAlgorithm();
			}
		});

		for(int i=0; i<repetitions; i++)
		{
			if(rvExperimentList.get(i).getMaxRV() > maxRV) maxRV = rvExperimentList.get(i).getMaxRV();
		}
		
		return maxRV;
	}
	/**
	 * @param cluster
	 * @return contains the following information: [data objects, centre dist mean, centre dist variance, centre max dist, centre RV, max RV]
	 */
	public static double[] clusterProperties(ArrayList<double[]> cluster)
	{
		// data objects, radius, max distant data object, centre RV, max RV		
		double[] centre = new double[cluster.get(0).length];
		double distMax=0.0d;
		DAEuclideanMetric metric = new  DAEuclideanMetric();
		double[] distancesSq = new double[cluster.size()];
		
		for(int j=0; j<cluster.size(); j++)
		{
			for(int k=0; k<centre.length; k++)
			{
				centre[k] += cluster.get(j)[k];
			}
		}
		for(int k=0; k<centre.length; k++)
		{
			centre[k] /= cluster.size();
		}
		
		for(int j=0; j<cluster.size(); j++)
		{
			distancesSq[j] = metric.distanceSq(centre, cluster.get(j));
			if(distancesSq[j] > distMax) distMax = distancesSq[j];
		}		
		distMax = Math.sqrt(distMax);
		double[] meanVar = SimpleStatistics.mean_variance(distancesSq);
				
		return new double[]{cluster.size(), distMax, meanVar[0], meanVar[1], meanVar[1]/meanVar[0]*meanVar[0], calculatMaxRV(cluster, 10)};
	}
	
	public static ArrayList<double[]> analyseClusters(File clusterDir)
	{
		ArrayList<double[]> clusterPropertyList = new ArrayList<double[]>(1000);
		
		if(!clusterDir.exists() || !clusterDir.isDirectory()) throw new IllegalArgumentException("The path \"" + clusterDir.getPath() + "\" is does not exist or is not a directory.");
		
		File[] subDirList = clusterDir.listFiles();
				
		FilenameFilter filter = new FilenameFilter()
			{
				@Override
				public boolean accept(File file, String name)
				{
					if(name.startsWith("cluster") && name.endsWith(".csv")) return true;
					return false;
				}
			};
		
		ArrayList<double[]> cluster;
		CSVFileReader reader = new CSVFileReader();
		reader.setFirstLineAsAtributeNames(true);
		reader.setIgnoreFirstAttribute(true);
			
		
		for(int i=0; i<subDirList.length; i++)
		{
			if(!subDirList[i].isDirectory()) continue;
			File[] clusterFileList = subDirList[i].listFiles(filter);
//			System.out.println(Arrays.toString(clusterFileList));
			
			for(int j=0; j<clusterFileList.length; j++)
			{
				try
				{
					reader.openFile(clusterFileList[j]);
					cluster = reader.readDoubleDataTable();
					clusterPropertyList.add(ClusterPropertyTester.clusterProperties(cluster));
					reader.closeFile();
				}
				catch(IOException e)
				{
					reader.closeFile();
					e.printStackTrace();
				}
			}
		}
		
		return clusterPropertyList;
	}
}
