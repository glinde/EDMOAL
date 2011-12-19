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
public class FrequentItemSetMining implements Serializable
{

	/**  */
	private static final long	serialVersionUID	= -2592615405350564632L;

	private ArrayList<Transaction> transactions;
	
	private ItemSetTreeNode root;
	
	private ArrayList<ItemSetTreeNode> maximalItemSets;

	private int itemCount;
	private int transactionCount;
	
	private int minSupport;
	
	private int frequentItemSetCount;
	
	private boolean isInitialized;
		
	/**
	 * @param itemCount
	 * @param transactionCount
	 * @param minSupport
	 */
	public FrequentItemSetMining(int itemCount, int transactionCount, int minSupport)
	{
		this.itemCount = itemCount;
		this.transactionCount = transactionCount;
		this.minSupport = minSupport;

		this.transactions = new ArrayList<Transaction>();
		
		this.root = new ItemSetTreeNode(null, this.itemCount, this.transactionCount);
		this.frequentItemSetCount = 1;
		
		this.maximalItemSets = new ArrayList<ItemSetTreeNode>();
		
		this.isInitialized = false;
	}
	
	
	public ArrayList<ItemSetTreeNode> initializeItemList(ArrayList<BitSet> itemList)
	{
		BitSet childTransactions;
		BitSet childItemSet;
		ItemSetTreeNode child;
		ArrayList<ItemSetTreeNode> itemSets = new ArrayList<ItemSetTreeNode>();
		int frequentChildCount;
		int i;
		
		this.root.removeAllChilds();
		System.gc();
		
		this.transactions = null;
		this.transactionCount = 0;
		
		for(BitSet bs : itemList)
		{
			this.transactionCount = Math.max(this.transactionCount, bs.length());
		}
					
		frequentChildCount = 0;
		for(i=0; i<this.itemCount; i++)
		{
			childItemSet = new BitSet(this.itemCount);
			childItemSet.set(i);
			childTransactions = itemList.get(i);
						
			child = new ItemSetTreeNode(this.root, childItemSet, childTransactions);
			
			if(child.getSupport() >= this.minSupport)
			{
				this.root.addChild(child);
				child.setParentsChildIndex(frequentChildCount);
				frequentChildCount++;
			}
			
			itemSets.add(child);
		}

		this.isInitialized = true;
		
		return itemSets;
	}
	
	
	public ArrayList<ItemSetTreeNode> initializeTransactional(ArrayList<Transaction> transactionList)
	{
		BitSet childTransactions;
		BitSet childItemSet;
		ItemSetTreeNode child;
		ArrayList<ItemSetTreeNode> itemSets = new ArrayList<ItemSetTreeNode>();
		int frequentChildCount;
		int i, k;
		
		this.root.removeAllChilds();
		System.gc();
		
		this.transactions = transactionList;
		this.transactionCount = transactionList.size();
					
		frequentChildCount = 0;
		for(i=0; i<this.itemCount; i++)
		{
			childItemSet = new BitSet(this.itemCount);
			childItemSet.set(i);
			childTransactions = new BitSet(this.transactions.size());
			
			for(k=0; k<childTransactions.length(); k++)
				if(this.transactions.get(k).containsItem(i)) childTransactions.set(k);
			
			child = new ItemSetTreeNode(this.root, childItemSet, childTransactions);
			
			if(child.getSupport() >= this.minSupport)
			{
				this.root.addChild(child);
				child.setParentsChildIndex(frequentChildCount);
				frequentChildCount++;
			}
			
			itemSets.add(child);
		}

		this.isInitialized = true;
		
		return itemSets;
	}
	

	public ArrayList<ItemSetTreeNode> initializeTransactionalIgnoringSupport(ArrayList<Transaction> transactionList)
	{
		BitSet childTransactions;
		BitSet childItemSet;
		ItemSetTreeNode child;
		ArrayList<ItemSetTreeNode> itemSets = new ArrayList<ItemSetTreeNode>();
		int frequentChildCount;
		int i, k;
		
		this.root.removeAllChilds();
		System.gc();
		
		this.transactions = transactionList;
		this.transactionCount = transactionList.size();
					
		frequentChildCount = 0;
		for(i=0; i<this.itemCount; i++)
		{
			childItemSet = new BitSet(this.itemCount);
			childItemSet.set(i);
			childTransactions = new BitSet(this.transactions.size());
			
			for(k=0; k<childTransactions.length(); k++)
				if(this.transactions.get(k).containsItem(i)) childTransactions.set(k);
			
			child = new ItemSetTreeNode(this.root, childItemSet, childTransactions);
			
			this.root.addChild(child);
			child.setParentsChildIndex(frequentChildCount);
			frequentChildCount++;
			
			if(child.getSupport() < this.minSupport) child.setFrequent(false);
			
			itemSets.add(child);
		}

		this.isInitialized = true;
		
		return itemSets;
	}
	
	
	
	public void generateFrequentItemSets()
	{
		if(!this.isInitialized) return;
		
		for(ItemSetTreeNode child : this.root.getAllChilds())
		{
			this.generateFISRecursive(child);
		}
	}
	
	protected void generateFISRecursive(ItemSetTreeNode localRoot)
	{
		localRoot.generateChilds(this.minSupport);
		for(ItemSetTreeNode child : localRoot.getAllChilds())
		{
			this.generateFISRecursive(child);
		}
		
		this.frequentItemSetCount++;
	}

	public void generateFrequentMaximalItemSets()
	{
		if(!this.isInitialized) return;
		
		for(ItemSetTreeNode child : this.root.getAllChilds())
		{
			this.generateFMISRecursive(child);
		}
	}
	
	protected void generateFMISRecursive(ItemSetTreeNode localRoot)
	{
		localRoot.generateChilds(this.minSupport);
		BitSet tmp;
				
		for(ItemSetTreeNode child : localRoot.getAllChilds())
		{
			this.generateFMISRecursive(child);
		}
		
		this.frequentItemSetCount++;

		if(localRoot.getAllChilds().size() == 0)
		{
			for(ItemSetTreeNode node : this.maximalItemSets)
			{
				tmp = (BitSet)localRoot.getItemSet().clone();
				tmp.and(node.getItemSet());
				if(localRoot.getItemSet().equals(tmp)) return;
//				if(localRoot.getItemSet().isSubSet(node.getItemSet())) return;
			}
			
			this.maximalItemSets.add(localRoot);
			localRoot.setMaximal(true);
		}
	}
	

	public void generateFrequentMaximalItemSetsIgnoringSupport()
	{
		if(!this.isInitialized) return;
		
		for(ItemSetTreeNode child : this.root.getAllChilds())
		{
			this.generateInFMISRecursive(child);
		}
	}

	protected void generateInFMISRecursive(ItemSetTreeNode localRoot)
	{
		localRoot.generateAllChilds(this.minSupport);
		ArrayList<ItemSetTreeNode> localChilds = new ArrayList<ItemSetTreeNode>(); 
		BitSet tmp;
				
		for(ItemSetTreeNode child : localRoot.getAllChilds())
		{
			this.generateInFMISRecursive(child);
		}
		
		this.frequentItemSetCount++;

		if(!localRoot.isFrequent()) return;
		for(ItemSetTreeNode node : localRoot.getAllChilds())
		{
			if(node.isFrequent()) localChilds.add(node);
		}
			
		if(localChilds.size() == 0)
		{
			for(ItemSetTreeNode node : this.maximalItemSets)
			{
				tmp = (BitSet)localRoot.getItemSet().clone();
				tmp.and(node.getItemSet());
				if(localRoot.getItemSet().equals(tmp)) return;
//				if(localRoot.getItemSet().isSubSet(node.getItemSet())) return;
			}
			
			this.maximalItemSets.add(localRoot);
			localRoot.setMaximal(true);
		}
	}
	
	/**
	 * @return the root
	 */
	public ItemSetTreeNode getRoot()
	{
		return this.root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(ItemSetTreeNode root)
	{
		this.root = root;
	}

	/**
	 * @return the itemCount
	 */
	public int getItemCount()
	{
		return this.itemCount;
	}

	/**
	 * @param itemCount the itemCount to set
	 */
	public void setItemCount(int itemCount)
	{
		this.itemCount = itemCount;
	}
	
	/**
	 * @return the transactionCount
	 */
	public int getTransactionCount()
	{
		return this.transactionCount;
	}

	/**
	 * @param transactionCount the transactionCount to set
	 */
	public void setTransactionCount(int transactionCount)
	{
		this.transactionCount = transactionCount;
	}
	
	public String toString()
	{
		return this.toStringMeta();
	}
	
	public String toStringMeta()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Items = ");
		sb.append(this.itemCount);
		sb.append(" Transactions = ");
		sb.append(this.transactionCount);
		sb.append(" Frequent Item Sets = ");
		sb.append(this.frequentItemSetCount);
		sb.append(" Maximal Item Sets = ");
		sb.append(this.maximalItemSets.size());
		sb.append("\n");
		
		return sb.toString(); 
	}
	
	public String toStringMaximal()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(this.toStringMeta());
		sb.append("\n");		

		for(ItemSetTreeNode node : this.maximalItemSets)
		{
			sb.append(node.toString());
			sb.append("\n");
		}
		
		return sb.toString(); 
	}

	public String toStringFrequent()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(this.toStringMeta());
		sb.append("\n");		
		sb.append(this.root.toString());
		sb.append("\n");
		sb.append("\n");
		
		for(ItemSetTreeNode node:this.root.getAllChilds())
		{
			recordRecursiveString(sb, node, "");
		}
		
		return sb.toString(); 
	}
	
	protected void recordRecursiveString(StringBuffer sb, ItemSetTreeNode localRoot, String recursionOffset)
	{
		String newOffset = recursionOffset + "\t";
		
		for(ItemSetTreeNode node:localRoot.getAllChilds())
		{
			sb.append(recursionOffset);
			sb.append(node.toString());
			sb.append("\n");
		}
		
		for(ItemSetTreeNode node:localRoot.getAllChilds())
		{
			this.recordRecursiveString(sb, node, newOffset);
		}
		
	//	sb.append("\n");
	}

	/**
	 * @return the minSupport
	 */
	public int getMinSupport()
	{
		return this.minSupport;
	}

	/**
	 * @param minSupport the minSupport to set
	 */
	public void setMinSupport(int minSupport)
	{
		this.minSupport = minSupport;
	}

	/**
	 * @return the frequentItemSetCount
	 */
	public int getFrequentItemSetCount()
	{
		return this.frequentItemSetCount;
	}	

	/**
	 * @return the maximalItemSetCount
	 */
	public int getMaximalItemSetCount()
	{
		return this.maximalItemSets.size();
	}	
	
	public void findMaximalItemSets()
	{
		
	}
		
	/**
	 * @param i
	 * @return
	 */
	public Transaction getTransaction(int i)
	{
		return this.transactions.get(i);
	}
	
	public void addTransaction(Transaction trans)
	{
		this.transactions.add(trans);
	}
	
	public void addAllTransactions(Collection<Transaction> transCol)
	{
		this.transactions.addAll(transCol);
	}
	
	public void removeTransaction(Transaction trans)
	{
		this.transactions.remove(trans);
	}

	public void removeTransaction(int i)
	{
		this.transactions.remove(i);
	}
	
	/**
	 * @return the itemsCount
	 */
	public int getItemsCount()
	{
		return this.itemCount;
	}

	/**
	 * @param itemsCount the itemsCount to set
	 */
	public void setItemsCount(int itemCount)
	{
		this.itemCount = itemCount;
	}

	/**
	 * @return the maximalItemSets
	 */
	public ArrayList<ItemSetTreeNode> getMaximalItemSets()
	{
		return this.maximalItemSets;
	}
}
