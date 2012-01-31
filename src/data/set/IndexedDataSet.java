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

import com.sun.org.apache.bcel.internal.classfile.Code;


/**
 * Together with the IndexedDataObject, this class is the backbone of the entire data analysis structure.
 * The indexed data set is designed to give a fast two-way index information about a data object.
 * Any IndexedDataObject, stored in this IndexedDataSet, is also storing its own index in this list.<br>
 * 
 * This class ensures an integrity of data object indices.
 * It is not possible to have the same data object in more than one data set.
 * It is not possible to have the same data object multiple times in a data set.
 * It is however possible to have multiple data objects with the same content (but different index). <br>
 * 
 * This class is essentially an container for the <code>ArrayList</code> class and implements the <code>List</code> Interface.
 * However, it masks all functionality of <code>ArrayList</code> w.r.t. the integrity of the indeces of its
 * IndexedDataObejcts and it provides the Sealable functionality. Note that also the iterator is affected by
 * that because it can be used to change the contents of a list. 
 * 
 * 
 * @see IndexedDataObject
 * @author Roland Winkler
 */
public class IndexedDataSet<T> extends AbstractSealable implements List<IndexedDataObject<T>>, Set<IndexedDataObject<T>>, RandomAccess, Serializable
{
	/**  */
	private static final long	serialVersionUID	= 7004911926477840555L;

	/** The number of IndexedDataSets initialized by the virtual mashiene */
	private static int instanceCounter = 0;
	
	/** The number of instances of IndexedDataSets that were initialized before this instance.
	 * This information provides an ordering of indexed data sets and thus make indexed data objects comparable w.r.t. to their
	 * index and the IndexedDataSet they are assigned to. */
	private int instanceNumber;
	
	/** The ArrayList holding the indexedDataObjects */
	private ArrayList<IndexedDataObject<T>> list;
	
	
	/** The default constructor, creates a new IndexedDataSet instance. */
	public IndexedDataSet()
	{
		this.list = new ArrayList<IndexedDataObject<T>>();
		this.instanceNumber = IndexedDataSet.instanceCounter;
		IndexedDataSet.instanceCounter++;
	}

	/** 
	 * A constructor which provides information for the initial capacity of the set.
	 *  
	 * @param initialCapacity The initial capacity
	 * 
     * @throws IllegalArgumentException - if the specified initial capacity is negative
     */
	public IndexedDataSet(int initialCapacity)
	{
	    if (initialCapacity < 0) throw new IllegalArgumentException("Illegal Capacity: "+ initialCapacity);
	    
		this.list = new ArrayList<IndexedDataObject<T>>(initialCapacity);
		this.instanceNumber = IndexedDataSet.instanceCounter;
		IndexedDataSet.instanceCounter++;
	}
	
	/**
	 * A constructor which produces a IndexedDataSet and it creates for each element of the
	 * specified collection an IndexedDataObject for this instance of the IndexedDataSet. 
	 * 
	 * @param col A collection of Elements that should be added to this instance 
	 * 
	 * @throws NullPointerException - if the specified collection contains one or more null elements
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
	
	/** 
	 * Recalculates the indices for all elements starting with <code>startIndex</code>.<br>
	 * 
	 * Complexity: O(n-startIndex), n = this.size()
	 * 
	 * @param startIndex The start index for reindexation
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
	 */
	private void reindexFrom(int startIndex)
	{
		this.registerChange();
		
		for(int i=startIndex; i<this.list.size(); i++)
		{
			this.list.get(i).setID(i);
		}
	}

	/**
	 *  Adds an <code>IndexedDataObject</code> to the list. 
	 *  It will throw an illegal argument exception if the <code>IndexedDataObject</code> is already part of an <code>IndexedDataSet</code>.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @param e The IndexedDayaObject to add	 * 
	 * @return always true if no exception is provoked
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
	 * 
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(IndexedDataObject<T> e)
	{
		if(e.isInSet()) throw new IllegalArgumentException("Data Object " + e + " is member of a data set.");
		this.registerChange();
		
		e.setDataSetConnection(this.size(), this);
		this.list.add(e);
		return true;
	}

	/**
	 * Adds an <code>IndexedDataObject</code> to the list at index <code>index</code>.<br>
	 * It will throw an illegal argument exception if the <code>IndexedDataObject</code> is already part of an <code>IndexedDataSet</code>.<br>
	 * 
	 * Complexity: O(n-index),  n = this.size()
	 * 
	 * @param index The index at which position the <code>IndexedDataObject</code> should be added
	 * @param e The <code>IndexedDataObject</code> to add
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
	 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index > size())
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

	/** 
	 * Adds all <code>IndexedDataObject</code>s of the Collection to the list.
	 * It will throw an illegal argument exception if any of the <code>IndexedDataObject</code>s is already part of an <code>IndexedDataSet</code>.<br>
	 * 
	 * Complexity: O(k), k = c.size()
	 * 
	 * @param c The Collection of <code>IndexedDataObject</code>s that should be added
	 * 
	 * @return always true if no exception is provoked
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
	 * @throws NullPointerException - if the specified collection contains one or more null elements
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
	 * Adds all <code>IndexedDataObject</code>s of the Collection to the list, at position <code>index</code>.
	 * It will throw an illegal argument exception if any of the <code>IndexedDataObject</code>s is already part of an <code>IndexedDataSet</code>.<br>
	 * 
	 * Complexity: O(n-index + k) n = this.size(), k = c.size()
	 * 
	 * @param index The index at which position the <code>IndexedDataObject</code> should be added
	 * @param c The Collection of <code>IndexedDataObject</code>s that should be added
	 * 
	 * @return always true if no exception is provoked
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
	 * @throws NullPointerException - if the specified collection contains one or more null elements
	 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index > size())
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

	/**
	 * Removes all <code>IndexedDataObject</code>s from this set. And clears the connection of the <code>IndexedDataObject</code>s 
	 * to this <code>IndexedDataSet</code><br>
	 * 
	 * Complexity: O(n) n = this.size()
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
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

	/**
	 * Checks whether an object is contained in this this set. It needs to be an <code>IndexedDataObject</code>
	 * to be part of this set, but as the interface does only allow the type Object as parameter for this function,
	 * it is used. Calling this function with a different types than <code>IndexedDataObject</code> will not
	 * result in an exception, the function will simply return false.<br> 
	 * 
	 * Complexity: O(1)
	 * 
	 * @param o The object that should be checked
	 * 
	 * @return true if the Object is contained, false otherwise
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

	/**
	 * Checksfor all Objects in the specified collection whether they are contained in this set. In short, 
	 * for all objects in the collection <code>contains(Object)<\code> is called.<br> 
	 * 
	 * Complexity: O(k), k = c.size()
	 *  
	 * @param c The collection of objects that should be checked
	 * 
	 * @return true if all Objects are contained in this set, false otherwise. 
	 * 
	 * @see java.util.List#containsAll(java.util.Collection)
	 * @see IndexedDataSet#contains(Object)
	 */
	@Override
	public boolean containsAll(Collection<?> c)
	{		
		boolean contained = true;
		
		for(Object o:c)	contained &= this.contains(o);
		
		return contained;
	}

	/**
	 * Gets the <code>IndexedDataObject</code> of the specified index.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @param index The index of the <code>IndexedDataObject</code> that should be returned 
	 * @return The <code>IndexedDataObject</code> at the position of <code>index</code>
	 * 
	 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
	 * 
	 * @see java.util.List#get(int)
	 */
	@Override
	public IndexedDataObject<T> get(int index)
	{
		return this.list.get(index);
	}

	/**
	 * Gets the index of the specified Object or -1 if it is not contained.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @param o The object for which the index should be return
	 * @return the index of the specified object or -1 if it is not contained
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

	/**
	 * Checks whether or not the set contains any objects.
	 * 
	 * Complexity: O(1)
	 * 
	 * @return true if this set is empty.
	 * 
	 * @see java.util.List#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}

	/**
	 * Gets a new Iterator for iterating over all elements of the set. Note that the Iterator
	 * is an instance of the class <code>IndexedDataSet.UnmodifyingIterator</code> which is not able
	 * to modify the sets content.<br>
	 *  
	 * Complexity: O(1)
	 * 
	 * @result A new UnmodifyingIterator instance
	 * 
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator<IndexedDataObject<T>> iterator()
	{
		return new UnmodifyingIterator();
	}

	/**
	 * Gets the (last) index of the specified Object or -1 if it is not contained. due to the set property of
	 * the <code>IndexedDataSet</code>, this function is identical to <code>indexOf(Object)</code>.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @param o The object for which the index should be return
	 * @return the index of the specified object or -1 if it is not contained
	 *
	 * @see java.util.List#indexOf(java.lang.Object)
	 * @see IndexedDataSet#indexOf(Object)
	 */
	@Override
	public int lastIndexOf(Object o)
	{
		return this.indexOf(o);
	}

	/**
	 * Gets a new Iterator for iterating over all elements of the list. Note that the Iterator
	 * is an instance of the class <code>IndexedDataSet.UnmodifyingIterator</code> which is not able
	 * to modify the sets content.<br>
	 *  
	 * Complexity: O(1)
	 * 
	 * @result A new UnmodifyingIterator instance
	 * 
	 * @see java.util.List#iterator()
	 */
	@Override
	public ListIterator<IndexedDataObject<T>> listIterator()
	{
		return new UnmodifyingListIterator();
	}

	/**
	 * Gets a new Iterator for iterating over all elements of the set. Note that the Iterator
	 * is an instance of the class <code>IndexedDataSet.UnmodifyingIterator</code> which is not able
	 * to modify the sets content.<br>
	 *  
	 * Complexity: O(1)
	 * 
	 * @param index The start index of the iterator
	 * @result A new UnmodifyingIterator instance which starts at index <code>index</code>
	 * 
	 * @see java.util.List#iterator()
	 */
	@Override
	public ListIterator<IndexedDataObject<T>> listIterator(int index)
	{
		return new UnmodifyingListIterator(index);
	}

	/** 
	 * Removes the specified object and reindexes all subsequent <code>IndexedDataObject</code>s.<br>
	 * 
	 * Complexity: O(n-o.id), n = this.size()
	 * 
	 * @param o The object to be removed
	 * @result true if the Object was removed
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
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

	/** 
	 * Removes the object at the specified index and reindexes all subsequent <code>IndexedDataObject</code>s.<br>
	 * 
	 * Complexity: O(n-index), n = this.size()
	 * 
	 * @param index The index of the object to be removed
	 * @result the removed Object
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
	 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
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

	/** 
	 * Removes all objects, contained in the collection and reindexes all other <code>IndexedDataObject</code>s.<br>
	 * 
	 * O(n+k), n = this.size(), k = c.size()
	 * 
	 * @param c The collection of object that are to be removed
	 * @result true if at least one object is removed
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
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

	
	/**
	 * Removes all objects, that are NOT contained in the collection and reindexes all others <code>IndexedDataObject</code>s.<br>
	 *  
	 * Approximate complexity: O(n+k), n = this.size(), k = c.size()
	 * 
	 * @param c The collection of object that are NOT to be removed
	 * @result true if at least one object is removed
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
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

	/** 
	 * Replaces the <code>IndexedDataObject</code> at the specified index and returns the replaced one. The old
	 * <code>IndexedDataObject</code> is disconnected from this <code>IndexedDataSet</code> and is after this function
	 * not longer connected to this <code>IndexedDataSet</code>
	 * 
	 * Complexity: O(1)
	 * 
	 * @param index The index of the object that is to be replaced
	 * @param e the new <code>IndexedDataObject</code> at position <code>index</code>
	 * @return The <code>IndexedDataObject</code> that was replaced at position <code>index</code>
	 * 
	 * @throws ChangeNotAllowedException - if the <code>IndexedDataSet</code> is sealed
	 * @throws NullPointerException - if the specified element is null
	 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
	 * 
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

	/**
	 * Returns the number of stored <code>IndexedDataObject</code>s.<br>
	 * 
	 * Complexity: O(1)
	 * 
	 * @return The number of stored <code>IndexedDataObject</code>s
	 * 
	 * @see java.util.List#size()
	 */
	@Override
	public int size()
	{
		return this.list.size();
	}

	/**
	 * Gets a sublist of all <code>IndexedDataObject</code>s between the two specified indices. Note that the 
	 * first index is included and the last index is excluded. Also the returned List instance is not an 
	 * <code>IndexedDataSet</code> because the contained <code>IndexedDataObject</code>s can only be in one <code>IndexedDataSet</code>.
	 * And they are already contained in this instance. <br>
	 * 
	 * Complexity:  O(k), k = toIndex - fromIndex
	 * 
	 * @param fromIndex The first index of the sublist (included)
	 * @param toIndex The last index of the sublist (excluded)
	 * @return A sublist of all <code>IndexedDataObject</code>s between the two specified indices
	 * @throws: IndexOutOfBoundsException - for illegal index values (fromIndex < 0 || toIndex > size || fromIndex > toIndex)
	 * 
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<IndexedDataObject<T>> subList(int fromIndex, int toIndex)
	{
		return this.list.subList(fromIndex, toIndex);
	}

	/**
	 *  Creates an array, containing all <code>IndexedDataObject</code>s of this <code>IndexedDataSet</code> in ascending order.<br>
	 * 
	 * Complexity: O(n), n = this.size()
	 * 
	 * @return A new array containing all <code>IndexedDataObject</code>s of this <code>IndexedDataSet</code>.
	 * 
	 * @see java.util.List#toArray()
	 */
	@Override
	public Object[] toArray()
	{
		return this.list.toArray();
	}

	/**
	 * Fills all <code>IndexedDataObject</code>s of this <code>IndexedDataSet</code> into the specified array in ascending order.
	 * If the array is too small, a new instance of the array is created and returned.
	 * The specified type of the array must be a supertype of <code>IndexedDataObject</code>.
	 * 
	 * Complexity: O(n),  n = this.size()
	 * 
     * @param a the array into which the elements of the list are to be stored, if it is big enough;
     * 			otherwise, a new array of the same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of <code>IndexedDataObject</code>
     * @throws NullPointerException if the specified array is null
     * 
	 * @see java.util.List#toArray(T[])
	 */
	@Override
	public <S> S[] toArray(S[] a)
	{
		return this.list.toArray(a);
	}
	

    /**
     * Returns the first <code>IndexedDataObject</code> (the item with index 0) of the <code>IndexedDataSet</code>.
     * 
     * Complexity:  O(1)
     *
     * @return the first data object of the data set.
     * @throws NoSuchElementException if the <code>IndexedDataSet</code> is empty
     */
    public IndexedDataObject<T> first()
    {
    	if(this.isEmpty()) throw new NoSuchElementException();
    	
    	return this.list.get(0);
    }

    /**
     * Returns the last <code>IndexedDataObject</code> (the item with index this.size() - 1) of the <code>IndexedDataSet</code>.
     * 
     * Complexity:  O(1)
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
	 * @author Roland Winkler
	 */
	private class UnmodifyingIterator implements Iterator<IndexedDataObject<T>>
	{
		/** The iterator that is masked by this class. */
		private Iterator<IndexedDataObject<T>> iter;
		
		/**
		 * The standard constructor
		 */
		public UnmodifyingIterator()
		{
			 this.iter = IndexedDataSet.this.list.iterator();
		}
		
		/**
		 * Checks whether more elements are in the iteration queue
		 * 
		 * @return true if the iteration has more elements
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext()
		{
			return iter.hasNext();
		}

		/**
		 * Returns the next element of the iteration.
		 * 
		 * @return the next element of the iteration
		 * @throws NoSuchElementException - if the iteration has no more elements
		 * 
		 * @see java.util.Iterator#next()
		 */
		public IndexedDataObject<T> next()
		{
			return iter.next();
		}

		/**
		 * Not supported.
		 * 
		 * @throws Throws always an IteratorModificationsNotSupportedException to ensure the integrity of the <code>IndexedDataSet</code> index structure
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
	 */
	private class UnmodifyingListIterator implements ListIterator<IndexedDataObject<T>>
	{
		/** The iterator that is masked by this class. */
		private ListIterator<IndexedDataObject<T>> listIter;

		/**
		 * The standard constructor
		 */
		public UnmodifyingListIterator()
		{
			this(0);
		}
		
		
		/**
		 * The constructor for <code>UnmodifyingListIterator</code> which starts at the specified index.
		 * 
		 * @param index index of the first element to be returned from the list iterator (by a call to next)
		 * @returns	a list iterator over the elements in this list (in proper sequence), starting at the specified position in the list
		 * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index > size())
		 */
		public UnmodifyingListIterator(int index)
		{
			this.listIter = IndexedDataSet.this.list.listIterator(index);
		}

		/**
		 * Not supported.
		 * 
		 * @param e An element to be added 
		 * @throws Throws always an IteratorModificationsNotSupportedException to ensure the integrity of the <code>IndexedDataSet</code> index structure
		 * 
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		public void add(IndexedDataObject<T> e)
		{
			throw new IteratorModificationsNotSupportedException("Operations modifying the contents of the data set through an iterator are not supported.");
		}

		/**
		 * Checks whether more elements are in the iteration
		 * 
		 * @return true if the iteration has more elements
		 * @see java.util.ListIterator#hasNext()
		 */
		public boolean hasNext()
		{
			return listIter.hasNext();
		}

		/**
		 * Checks whether there elements before the current iterator position
		 * 
		 * @return true if the iteration has more elements
		 * @see java.util.ListIterator#hasPrevious()
		 */
		public boolean hasPrevious()
		{
			return listIter.hasPrevious();
		}

		/**
		 * Returns the next element of the iteration.
		 * 
		 * @return the next element of the iteration
		 * @throws NoSuchElementException - if the iteration has no more elements
		 * 
		 * @see java.util.ListIterator#next()
		 */
		public IndexedDataObject<T> next()
		{
			return listIter.next();
		}

		/**
		 * Returns the next index of the iteration, that is the index of <code>IndexedDataObject</code> that is returned
		 * by calling the <code>next()</code> function.
		 * 
		 * @return the next index of the iteration
		 * @see java.util.ListIterator#nextIndex()
		 */
		public int nextIndex()
		{
			return listIter.nextIndex();
		}

		/**
		 * Returns the previous element of the iteration.
		 * 
		 * @return the previous element of the iteration
		 * @throws NoSuchElementException - if the iteration has no more elements
		 * 
		 * @see java.util.ListIterator#previous()
		 */
		public IndexedDataObject<T> previous()
		{
			return listIter.previous();
		}

		/**
		 * Returns the previous index of the iteration, that is the index of <code>IndexedDataObject</code> that is returned
		 * by calling the <code>previous()</code> function.
		 * 
		 * @return the next index of the iteration
		 * @see java.util.ListIterator#previousIndex()
		 */
		public int previousIndex()
		{
			return listIter.previousIndex();
		}

		/**
		 * Not supported.
		 * 
		 * @throws Throws always an IteratorModificationsNotSupportedException to ensure the integrity of the <code>IndexedDataSet</code> index structure
		 * 
		 * @see java.util.ListIterator#remove()
		 */
		public void remove()
		{
			throw new IteratorModificationsNotSupportedException("Operations modifying the contents of the data set through an iterator are not supported.");
		}

		/**
		 * Not supported.
		 * 
		 * @throws Throws always an IteratorModificationsNotSupportedException to ensure the integrity of the <code>IndexedDataSet</code> index structure
		 * 
		 * @param e An element to set
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		public void set(IndexedDataObject<T> e)
		{
			throw new IteratorModificationsNotSupportedException("Operations modifying the contents of the data set through an iterator are not supported.");
		}
	}


	/**
	 * The <code>instanceNumber</code> is the counter of instances that are created by this VM. The <code>instanceNumber</code> of this particular
	 * <code>IndexedDataSet</code> means that <code>instanceNumber</code> - many other <code>IndexedDataSet</code> have been initialized and
	 * this <code>IndexedDataSet</code> is number <code>instanceNumber</code>.
	 * This information provides an ordering of <code>IndexedDataSet</code>s and thus make <code>IndexedDataSet</code>s comparable w.r.t. to their
	 * index and the <code>IndexedDataSet</code> they are assigned to. So there is a total ordering for all <code>IndexedDataObjects</code>
	 * that are assigned to an <code>IndexedDataSet</code>.
	 * 	
	 * @return the instanceNumber
	 */
	public int getInstanceNumber()
	{
		return this.instanceNumber;
	}
	
	
}
