package data.objects.matrix;

public class FeatureSpaceClusterSampling2D extends FeatureSpaceSampling2D
{
	private int clusterID;
	
	public FeatureSpaceClusterSampling2D(int sizeX, int sizeY)
	{
		super(sizeX, sizeY);
		
		this.clusterID = 0;
	}
	
	public FeatureSpaceClusterSampling2D(int sizeX, int sizeY, double[] lowerLeftCorner, double[] upperRightCorner)
	{
		super(sizeX, sizeY, lowerLeftCorner, upperRightCorner);
		
		this.clusterID = 0;
	}

	public int getClusterID()
	{
		return clusterID;
	}

	public void setClusterID(int clusterID)
	{
		this.clusterID = clusterID;
	}
	
}
