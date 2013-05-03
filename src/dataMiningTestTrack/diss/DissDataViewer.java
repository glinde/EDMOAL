/**
 * 
 */
package dataMiningTestTrack.diss;

import io.CSVFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import data.set.IndexedDataSet;
import dataMiningTestTrack.tests.TestVisualizer;

/**
 * @author rwinkler
 *
 */
public class DissDataViewer extends TestVisualizer
{

	
	public void viewDissDataFile(String path) throws FileNotFoundException
	{
		this.printJPG = false;
		this.printPDF = false;
		this.printPNG = false;
		this.printSVG = false;
		
		File file = new File(path);
		
		if(!file.exists()) throw new FileNotFoundException("The file with the path \"" + path + "\" does not exist.");

		CSVFileReader reader = new CSVFileReader();
		try
		{
			reader.openFile(file);
			reader.setIgnoreFirstAttribute(true);
			reader.setFirstLineAsAtributeNames(true);
			ArrayList<double[]> data = reader.readDoubleDataTable();
			reader.closeFile();
			
			IndexedDataSet<double[]> dataSet = new IndexedDataSet<double[]>(data);			
			this.showDataSet(dataSet, null);
		}
		catch (IOException e)
		{
			reader.closeFile();
			e.printStackTrace();
		}
		
	}
}
