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


package datamining.frequentDataMining;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.ArrayList;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public class ItemSetTreeNode implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= 6406442637765851871L;
	private ItemSetTreeNode parent;
	private ArrayList<ItemSetTreeNode> childs;
	private int parentsChildIndex;
	
	private BitSet itemSet;
	private BitSet transactionList;
	
	private boolean frequent;
	private boolean maximal;
	
	public ItemSetTreeNode(ItemSetTreeNode parent, int itemCount, int transactionCount)
	{		
		this.childs = new ArrayList<ItemSetTreeNode>();
		this.parent = parent;
		this.parentsChildIndex = -1;
		this.maximal = false;
		this.frequent = true;
		
		this.itemSet = new BitSet(itemCount);
		this.transactionList = new BitSet(transactionCount);
		this.transactionList.set(0, transactionCount-1);
	}
	
	public ItemSetTreeNode(ItemSetTreeNode parent, BitSet items, BitSet transactions)
	{		
		this.childs = new ArrayList<ItemSetTreeNode>();
		this.parent = parent;
		this.parentsChildIndex = -1;
		this.maximal = false;
		this.frequent = true;
		
		this.itemSet = items;
		this.transactionList = transactions;
	}
	
	public ArrayList<ItemSetTreeNode> generateChilds(int minSupport)
	{
		if(this.parent == null) return this.childs;
		ArrayList<ItemSetTreeNode> parentsSuccessiveChilds = this.parent.getSuccessiveChilds(this);
		ItemSetTreeNode tmpNode;
		int childCounter = 0;
		
		BitSet tmpItemSet;	
		BitSet tmpTransactionList;
		
		for(ItemSetTreeNode node : parentsSuccessiveChilds)
		{
			tmpItemSet = (BitSet)this.itemSet.clone();
			tmpTransactionList = (BitSet)this.transactionList.clone();
			
			tmpItemSet.or(node.itemSet);
			tmpTransactionList.and(node.transactionList);
			
			tmpNode = new ItemSetTreeNode(this, tmpItemSet, tmpTransactionList);
			
			//System.out.println("tmpNode: " + tmpNode.toString());
			
			if(tmpNode.getSupport() >= minSupport)
			{
				tmpNode.parentsChildIndex = childCounter;
				this.childs.add(tmpNode);
				
				childCounter++;
			}
		}
		
		return this.childs;
	}

	public ArrayList<ItemSetTreeNode> generateAllChilds(int minSupport)
	{
		if(this.parent == null) return this.childs;
		ArrayList<ItemSetTreeNode> parentsSuccessiveChilds = this.parent.getSuccessiveChilds(this);
		ItemSetTreeNode tmpNode;
		int childCounter = 0;
		
		BitSet tmpItemSet;	
		BitSet tmpTransactionList;
		
		for(ItemSetTreeNode node : parentsSuccessiveChilds)
		{
			tmpItemSet = (BitSet)this.itemSet.clone();
			tmpTransactionList = (BitSet)this.transactionList.clone();
			
			tmpItemSet.or(node.itemSet);
			tmpTransactionList.and(node.transactionList);
			
			tmpNode = new ItemSetTreeNode(this, tmpItemSet, tmpTransactionList);
						
			tmpNode.parentsChildIndex = childCounter;
			if(tmpNode.getSupport() < minSupport) tmpNode.frequent = false;
			this.childs.add(tmpNode);
			
			childCounter++;			
		}
		
		return this.childs;
	}
	
	public ArrayList<ItemSetTreeNode> getSuccessiveChilds(ItemSetTreeNode child)
	{
		ArrayList<ItemSetTreeNode> succChilds = new ArrayList<ItemSetTreeNode>(this.childs.size());
		int i;
		for(i=child.getParentsChildIndex()+1; i<this.childs.size(); i++)
			succChilds.add(this.childs.get(i));
		
		return succChilds;
	}
	
	public void setIntersectItemSets(ItemSetTreeNode node)
	{
		this.itemSet.and(node.itemSet);
		this.transactionList.or(node.transactionList);
	}
	
	public void setUnifyItemSets(ItemSetTreeNode node)
	{
		this.itemSet.or(node.itemSet);
		this.transactionList.and(node.transactionList);
	}
	
	public void fillLevelTraversation(ArrayList<ArrayList<ItemSetTreeNode>> levelSets, int level)
	{
		if(levelSets.size() <= level) levelSets.add(new ArrayList<ItemSetTreeNode>());
		
		levelSets.get(level).add(this);
						
		for(ItemSetTreeNode c : this.childs) c.fillLevelTraversation(levelSets, level+1);
	}
	
	public boolean containsItem(int i)
	{
		return this.itemSet.get(i);
	}

	public boolean containsTransaction(int i)
	{
		return this.transactionList.get(i);
	}
	
	
	/**
	 * @param e
	 * @return
	 * @see java.util.Vector#add(java.lang.Object)
	 */
	public boolean addChild(ItemSetTreeNode child)
	{
		return this.childs.add(child);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Vector#addAll(java.util.Collection)
	 */
	public boolean addAllChilds(Collection<ItemSetTreeNode> childCol)
	{
		return this.childs.addAll(childCol);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.Vector#get(int)
	 */
	public ItemSetTreeNode getChild(int i)
	{
		return this.childs.get(i);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.Vector#get(int)
	 */
	public ArrayList<ItemSetTreeNode> getAllChilds()
	{
		return new ArrayList<ItemSetTreeNode>(this.childs);
	}
	
		/**
	 * @param o
	 * @return
	 * @see java.util.Vector#remove(java.lang.Object)
	 */
	public boolean removeChild(ItemSetTreeNode child)
	{
		return this.childs.remove(child);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.Vector#removeAll(java.util.Collection)
	 */
	public boolean removeChilds(Collection<ItemSetTreeNode> childCol)
	{
		return this.childs.removeAll(childCol);
	}

	/**
	 * 
	 * @see java.util.Vector#removeAllElements()
	 */
	public void removeAllChilds()
	{
		this.childs.clear();
	}

	/**
	 * @return the support
	 */
	public int getSupport()
	{
		return this.transactionList.cardinality();
	}

	/**
	 * @return the parent
	 */
	public ItemSetTreeNode getParent()
	{
		return this.parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(ItemSetTreeNode parent)
	{
		this.parent = parent;
	}
	
	public boolean isRoot()
	{
		return this.parent == null;
	}
	
	public boolean isLeaf()
	{
		return this.childs.size() == 0;
	}

	/**
	 * @return the parentsChildIndex
	 */
	public int getParentsChildIndex()
	{
		return this.parentsChildIndex;
	}

	/**
	 * @param parentsChildIndex the parentsChildIndex to set
	 */
	public void setParentsChildIndex(int parentsChildIndex)
	{
		this.parentsChildIndex = parentsChildIndex;
	}

	/**
	 * @return the itemSet
	 */
	public BitSet getItemSet()
	{
		return this.itemSet;
	}

	/**
	 * @param itemSet the itemSet to set
	 */
	public void setItemSet(BitSet itemSet)
	{
		this.itemSet = itemSet;
	}

	/**
	 * @return the transactionList
	 */
	public BitSet getTransactionList()
	{
		return this.transactionList;
	}

	/**
	 * @param transactionList the transactionList to set
	 */
	public void setTransactionList(BitSet transactionList)
	{
		this.transactionList = transactionList;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		int i;
		
		if(this.isRoot()) sb.append("root ");
		else
		{	
			for(i=0; i<this.itemSet.length(); i++)
			{
				if(this.itemSet.get(i))
				{
//					sb.append(StringOperations.intToString(i, 4));
					sb.append(String.format("%04d", i));
					sb.append(" ");
				}
			}
		}
		sb.append("- ");
		
		sb.append(this.transactionList.toString());
		
		return sb.toString();
	}

	public boolean isMaximal()
	{
		return this.maximal;
	}

	public void setMaximal(boolean maximal)
	{
		this.maximal = maximal;
	}

	public boolean isFrequent()
	{
		return this.frequent;
	}

	public void setFrequent(boolean frequent)
	{
		this.frequent = frequent;
	}
	
	
	
}
