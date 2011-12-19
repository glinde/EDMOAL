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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;


/**
 * This class ensures an integrity of data object indices.
 * It is not possible to have the same data object in more than one data set.
 * It is not possible to have the same data object multiple times in a data set.
 * It is however possible to have multiple data objects with the same content (but different index).  
 * 
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class IndexedDataSet<T> extends AbstractSealable implements List<IndexedDataObject<T>>, Set<IndexedDataObject<T>>, RandomAccess, Serializable
{
	/**  */
	private static final long	serialVersionUID	= 7004911926477840555L;

	private static int instanceCounter = 0;
	
	/** the number of instances of IndexedDataSets. To provide an ordering of indexed data objects (and thus make them comparable). */
	private int instanceNumber;
	
	/**  */
	private ArrayList<IndexedDataObject<T>> list;
	
	
	/** */
	public IndexedDataSet()
	{
		this.list = new ArrayList<IndexedDataObject<T>>();
		this.instanceNumber = IndexedDataSet.instanceCounter;
		IndexedDataSet.instanceCounter++;
	}

	/** */
	public IndexedDataSet(int initialCapacity)
	{
		this.list = new ArrayList<IndexedDataObject<T>>(initialCapacity);
		this.instanceNumber = IndexedDataSet.instanceCounter;
		IndexedDataSet.instanceCounter++;
	}
	
	/**
	 * @param col
	 */
	public IndexedDataSet(Collection<T> col)
	{
		this.list = new ArrayList<IndexedDataObject<T>>(col.size());
		this.instanceNumber = IndexedDataSet.instanceCounter;
		IndexedDataSet.instanceCounter++;
		
		for(T p:col)
		{
			this.add(new IndexedDataObject<T>(p));
		}
	}
	
	/** O(n-index), 
	 * n = this.size()
	 * 
	 * @param index
	 */
	private void reindexFrom(int index)
	{
		this.registerChange();
		
		for(int i=index; i<this.list.size(); i++)
		{
			this.list.get(i).setID(i);
		}
	}

	/** O(1)
	 * @see java.util.List#add(java.lang.Object)
	 */
	@Override
	public boolean add(IndexedDataObject<T> e)
	{
		if(e.isInSet()) throw new IllegalArgumentException("Data Object " + e + " is member of a data set.");
		this.registerChange();
		
		e.setDataSetConnection(this.size(), this);
		this.list.add(e);
		return true;
	}

	/** O(n-index), 
	 * n = this.size()
	 * 
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, IndexedDataObject<T> e)
	{
		if(e.isInSet()) throw new IllegalArgumentException("Data Object " + e + " is member of a data set.");
		this.registerChange();
				
		if (index > this.size() || index < 0) throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size());
		
		e.setDataSet(this);
		this.list.add(index, e);
		this.reindexFrom(index);
		
	}

	/** O(k), 
	 * k = c.size()
	 * 
	 * 
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends IndexedDataObject<T>> c)
	{
		if(c.size() == 0) return false;		
		for(IndexedDataObject<T> e:c)
		{
			if(e.isInSet()) throw new IllegalArgumentException("The collection contains a data object (" + e + ") which is member of a data set.");
		}

		this.registerChange();
		
		int newIndex = this.size();
		this.list.addAll(c);
		this.reindexFrom(newIndex);
		return true;
	}

	/**
	 * O(n-index + k)
	 * n = this.size(), 
	 * k = c.size()
	 * 
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends IndexedDataObject<T>> c)
	{
		for(IndexedDataObject<T> e:c)
		{
			if(e.isInSet()) throw new IllegalArgumentException("The collection contains a data object (" + e + ") which is member of a data set.");
		}
		this.registerChange();
		
		this.list.addAll(index, c);
		this.reindexFrom(index);
		return true;
	}

	/** O(n)
	 * n = this.size()
	 * 
	 * @see java.util.List#clear()
	 */
	@Override
	public void clear()
	{
		this.registerChange();
		
		for(IndexedDataObject<T> d:this.list)
		{
			d.clearDataSetConnection();
		}
		
		this.list.clear();
	}

	/** O(1)
	 *
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o)
	{
		if(!(o instanceof IndexedDataObject)) return false;
		return ((IndexedDataObject<T>)o).isInSet(this);
	}

	/** O(k) 
	 * k = c.size()
	 * 
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c)
	{		
		boolean contained = true;
		
		for(Object o:c)	contained &= this.contains(o);
		
		return contained;
	}

	/** O(1)
	 * 
	 * @see java.util.List#get(int)
	 */
	@Override
	public IndexedDataObject<T> get(int index)
	{
		return this.list.get(index);
	}

	/** O(1)
	 *
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int indexOf(Object o)
	{
		if(!this.contains(o)) return -1;
		if(((IndexedDataObject<T>)o).getDataSet() != this) return -1;
		
		return ((IndexedDataObject<T>)o).getID();
	}

	/** O(1)
	 * 
	 * @see java.util.List#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}

	/** O(1)
	 * 
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator<IndexedDataObject<T>> iterator()
	{
		return new UnmodifyingIterator();
	}

	/** O(1)
	 * 
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o)
	{
		return this.indexOf(o);
	}

	/** O(1)
	 * 
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<IndexedDataObject<T>> listIterator()
	{
		return new UnmodifyingListIterator();
	}

	/** O(1)
	 * 
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<IndexedDataObject<T>> listIterator(int index)
	{
		return new UnmodifyingListIterator(index);
	}

	/** O(n-o.id)
	 * n = this.size()
	 * 
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o)
	{
		if(!this.contains(o)) return false;
		this.registerChange();
		
		IndexedDataObject<T> d = (IndexedDataObject<T>)o;
		
		this.list.remove(d.getID());
		this.reindexFrom(d.getID());
		d.clearDataSetConnection();
		
		return true;
	}

	/** O(n-index)
	 * n = this.size()
	 * 
	 * @see java.util.List#remove(int)
	 */
	@Override
	public IndexedDataObject<T> remove(int index)
	{
		IndexedDataObject<T> d = this.list.get(index);		
		this.registerChange();
		
		this.list.remove(index);
		d.clearDataSetConnection();
		this.reindexFrom(d.getID());
		
		return d;
	}

	/** O(n+k)
	 * n = this.size(), 
	 * k = c.size()
	 * 
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> c)
	{
		ArrayList<IndexedDataObject<T>> newList = new ArrayList<IndexedDataObject<T>>(this.size());
		int removeCounter = 0;
		for(Object o:c)
		{
			if(this.contains(o))
			{
				((IndexedDataObject<T>)o).clearDataSetConnection();
				removeCounter++;
			}
		}
		
		if(removeCounter == 0) return false;
		this.registerChange();
		
		for(IndexedDataObject<T> d:this.list)
		{
			if(d.isInSet()) newList.add(d);
		}
		this.list = newList;
		this.reindexFrom(0);
		
		return true;
	}

	/** approximately O(n+k)
	 * n = this.size(), 
	 * k = c.size()
	 * 
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(Collection<?> c)
	{
		HashSet<IndexedDataObject<T>> contained = new HashSet<IndexedDataObject<T>>((int)(c.size()*1.33d));
		
		for(Object o:c)
		{
			if(this.contains(o)) contained.add((IndexedDataObject<T>)o);
		}
		if(this.size() == contained.size()) return false;

		this.clear();
		this.addAll(contained);
		
		return true;
	}

	/** O(1)
	 * 
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public IndexedDataObject<T> set(int index, IndexedDataObject<T> e)
	{
		if(e.isInSet()) throw new IllegalArgumentException("Data Object " + e + " is member of a data set.");
		this.registerChange();
		
		IndexedDataObject<T> removed = this.list.set(index, e);
		removed.clearDataSetConnection();
		e.setDataSetConnection(index, this);
		
		return removed;
	}

	/** O(1)
	 * 
	 * @see java.util.List#size()
	 */
	@Override
	public int size()
	{
		return this.list.size();
	}

	/** O(toIndex-fromIndex)
	 * 
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<IndexedDataObject<T>> subList(int fromIndex, int toIndex)
	{
		return this.list.subList(fromIndex, toIndex);
	}

	/** O(n)
	 * n = this.size()
	 * 
	 * @see java.util.List#toArray()
	 */
	@Override
	public Object[] toArray()
	{
		return this.list.toArray();
	}

	/** O(n)
	 * n = this.size()
	 * 
	 * @see java.util.List#toArray(T[])
	 */
	@Override
	public <S> S[] toArray(S[] a)
	{
		return this.list.toArray(a);
	}
	

    /*** O(1)
	 * n = this.size()
	 * 
     * Returns the first data object (the item with id 0) of the data set.
     *
     * @return the first data object of the data set.
     * @throws NoSuchElementException if the date set is empty
     */
    public IndexedDataObject<T> first()
    {
    	if(this.isEmpty()) throw new NoSuchElementException();
    	
    	return this.list.get(0);
    }

    /*** O(1)
	 * 
     * Returns the last data object (the item with highest id) of the data set.
     *
     * @return the last data object of the data set.
     * @throws NoSuchElementException if the date set is empty
     */
    public IndexedDataObject<T> last()
    {
    	if(this.isEmpty()) throw new NoSuchElementException();
    	
    	return this.list.get(this.size()-1);
    }

	
	/**
	 * The iterator does not support any modifications in order to ensure the integrity of the data set.
	 * It throws a IteratorModificationsNotSupportedException when ever it is tried to modify the data set through the iterator.
	 * 
	 * @author wink_ro
	 *
	 * @param <S>
	 */
	private class UnmodifyingIterator implements Iterator<IndexedDataObject<T>>
	{
		/**  */
		private Iterator<IndexedDataObject<T>> iter;
		
		/**
		 * 
		 */
		public UnmodifyingIterator()
		{
			 this.iter = IndexedDataSet.this.list.iterator();
		}
		
		/**
		 * @return
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext()
		{
			return iter.hasNext();
		}

		/**
		 * @return
		 * @see java.util.Iterator#next()
		 */
		public IndexedDataObject<T> next()
		{
			return iter.next();
		}

		/**
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove()
		{
			throw new IteratorModificationsNotSupportedException("Operations modifying the contents of the data set through an iterator are not supported.");
		}
	}
	

	/**
	 * The iterator does not support any modifications in order to ensure the integrity of the data set.
	 * It throws a IteratorModificationsNotSupportedException when ever it is tried to modify the data set through the iterator.
	 * 
	 * @author wink_ro
	 *
	 * @param <S>
	 */
	private class UnmodifyingListIterator implements ListIterator<IndexedDataObject<T>>
	{
		private ListIterator<IndexedDataObject<T>> listIter;
		
		public UnmodifyingListIterator()
		{
			this(0);
		}
		
		/** */
		public UnmodifyingListIterator(int index)
		{		
			this.listIter = IndexedDataSet.this.list.listIterator(index);
		}

		/**
		 * @param e
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		public void add(IndexedDataObject<T> e)
		{
			throw new IteratorModificationsNotSupportedException("Operations modifying the contents of the data set through an iterator are not supported.");
		}

		/**
		 * @return
		 * @see java.util.ListIterator#hasNext()
		 */
		public boolean hasNext()
		{
			return listIter.hasNext();
		}

		/**
		 * @return
		 * @see java.util.ListIterator#hasPrevious()
		 */
		public boolean hasPrevious()
		{
			return listIter.hasPrevious();
		}

		/**
		 * @return
		 * @see java.util.ListIterator#next()
		 */
		public IndexedDataObject<T> next()
		{
			return listIter.next();
		}

		/**
		 * @return
		 * @see java.util.ListIterator#nextIndex()
		 */
		public int nextIndex()
		{
			return listIter.nextIndex();
		}

		/**
		 * @return
		 * @see java.util.ListIterator#previous()
		 */
		public IndexedDataObject<T> previous()
		{
			return listIter.previous();
		}

		/**
		 * @return
		 * @see java.util.ListIterator#previousIndex()
		 */
		public int previousIndex()
		{
			return listIter.previousIndex();
		}

		/**
		 * 
		 * @see java.util.ListIterator#remove()
		 */
		public void remove()
		{
			throw new IteratorModificationsNotSupportedException("Operations modifying the contents of the data set through an iterator are not supported.");
		}

		/**
		 * @param e
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		public void set(IndexedDataObject<T> e)
		{
			throw new IteratorModificationsNotSupportedException("Operations modifying the contents of the data set through an iterator are not supported.");
		}
	}


	/**
	 * @return the instanceNumber
	 */
	public int getInstanceNumber()
	{
		return this.instanceNumber;
	}
	
	
}
