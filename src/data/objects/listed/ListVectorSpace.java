/**
 * TODO File Description
 */
package data.objects.listed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.algebra.VectorSpace;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ListVectorSpace<T> implements VectorSpace<List<T>>, Serializable
{
	/** A vector space of the base object type */
	protected VectorSpace<T> vs;
	
	/** The number of elements in the list this vector space. */
	protected int listLength;
	
	/**
	 * @param vs
	 * @param listLength
	 */
	public ListVectorSpace(VectorSpace<T> vs, int listLength)
	{
		this.vs = vs;
		this.listLength = listLength;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#getNewAddNeutralElement()
	 */
	@Override
	public List<T> getNewAddNeutralElement()
	{
		ArrayList<T> list = new ArrayList<T>(this.listLength);
		
		for(int i=0; i<this.listLength; i++) list.add(this.vs.getNewAddNeutralElement());
		
		return list;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#resetToAddNeutralElement(java.lang.Object)
	 */
	@Override
	public void resetToAddNeutralElement(List<T> x)
	{
		for(int i=0; i<this.listLength; i++) this.vs.resetToAddNeutralElement(x.get(i));
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#copy(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void copy(List<T> x, List<T> y)
	{
		for(int i=0; i<this.listLength; i++) this.vs.copy(x.get(i), y.get(i));
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#inv(java.lang.Object)
	 */
	@Override
	public void inv(List<T> x)
	{
		for(int i=0; i<this.listLength; i++) this.vs.inv(x.get(i));		
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#add(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void add(List<T> x, List<T> y)
	{
		for(int i=0; i<this.listLength; i++) this.vs.add(x.get(i), y.get(i));
		
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#sub(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void sub(List<T> x, List<T> y)
	{
		for(int i=0; i<this.listLength; i++) this.vs.sub(x.get(i), y.get(i));		
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#mul(java.lang.Object, double)
	 */
	@Override
	public void mul(List<T> x, double a)
	{
		for(int i=0; i<this.listLength; i++) this.vs.mul(x.get(i), a);
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#copyNew(java.lang.Object)
	 */
	@Override
	public List<T> copyNew(List<T> x)
	{
		ArrayList<T> list = new ArrayList<T>(this.listLength);
		
		for(int i=0; i<this.listLength; i++) list.add(this.vs.copyNew(x.get(i)));
		
		return list;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#invNew(java.lang.Object)
	 */
	@Override
	public List<T> invNew(List<T> x)
	{
		ArrayList<T> list = new ArrayList<T>(this.listLength);
		
		for(int i=0; i<this.listLength; i++) list.add(this.vs.invNew(x.get(i)));
		
		return list;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#addNew(java.lang.Object, java.lang.Object)
	 */
	@Override
	public List<T> addNew(List<T> x, List<T> y)
	{
		ArrayList<T> list = new ArrayList<T>(this.listLength);
		
		for(int i=0; i<this.listLength; i++) list.add(this.vs.addNew(x.get(i), y.get(i)));
		
		return list;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#subNew(java.lang.Object, java.lang.Object)
	 */
	@Override
	public List<T> subNew(List<T> x, List<T> y)
	{
		ArrayList<T> list = new ArrayList<T>(this.listLength);
		
		for(int i=0; i<this.listLength; i++) list.add(this.vs.subNew(x.get(i), y.get(i)));
		
		return list;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#mulNew(java.lang.Object, double)
	 */
	@Override
	public List<T> mulNew(List<T> x, double a)
	{
		ArrayList<T> list = new ArrayList<T>(this.listLength);
		
		for(int i=0; i<this.listLength; i++) list.add(this.vs.mulNew(x.get(i), a));
		
		return list;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#getDimension()
	 */
	@Override
	public int getDimension()
	{
		return this.vs.getDimension() * this.listLength;
	}

	/* (non-Javadoc)
	 * @see data.algebra.VectorSpace#infiniteDimensionality()
	 */
	@Override
	public boolean infiniteDimensionality()
	{
		return false;
	}
	
}
