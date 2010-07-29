package Cytoscape.plugin.BNMatch.INM;
/**
 *
 * @author YULEI
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class INMNodeList extends LinkedList
{
      
public INMNode FindProteinByName(String proteinName)
{
    INMNode tmp = new INMNode(proteinName);
    int pos = this.indexOf(tmp);
    if (pos != -1)
    {
        return (INMNode) this.get(pos);
    } else
    {
        return null;
    }
}



/**
 * Add a protein called protein.If the protein already exists, then save the larger probability protein.
 * @param objProtein
 */
public void AddNewProtein(INMNode protein)
{
       int index=indexOf(protein);
       INMNode node=null;
       if(-1==index)
       {
           node=new INMNode("");
           node=(INMNode)protein.clone();
           add(node);
       }
       else
       {
         node=(INMNode)get(index);
        if(node.getProteinProbability()<protein.getProteinProbability())
            node.setProteinProbability(protein.getProteinProbability());
       }
}

public void AddNewProtein(String proteinName,double probability)
{
    INMNode node=new INMNode(proteinName,probability);
    AddNewProtein(node);
}
/**
 * Remove all proteins 
 * @param protein
 */
public  void RemoveProtein(INMNode protein)
{
    Collection<INMNode> c=new LinkedList<INMNode>();
    c.add(protein);
    this.removeAll(c);
}

public void RemoveProtein(String proteinName,double probability)
{
    INMNode node=new INMNode(proteinName,probability); 
    this.RemoveProtein(node);
}

public void SortNodeList()
{
    Collections.sort(this,new Comparator<INMNode>()
    {
        public int compare(INMNode node0,INMNode node1)
        {
            return node0.getProteinName().compareTo(node1.getProteinName());//sort the node according to its name
        }
    });
}


public void SortedAddProtein(String proteinName,double probability)
{
    INMNode node=new INMNode(proteinName,probability);
    SortedAddProtein(node);
}

public void SortedAddProtein(INMNode protein)
{
   int pos= Collections.binarySearch(this, protein, new Comparator<INMNode>()
        {

            public int compare(INMNode node0, INMNode node1)
            {
                return node0.getProteinName().compareTo(node1.getProteinName());
            }
        });//pos,the position to insert the element
  
    if(pos>=0)
        this.add(pos, protein);
    else
        this.add(-pos-1,protein);       
}

public boolean isExistProtein(INMNode node)
{
    return contains(node);
}

public void AddProteinByP(INMNode protein)
{
    double p = protein.getProteinProbability();
    int index = 0;
    INMNode node=null;
    if (this.isEmpty())
      {
        add(index, protein);
      } else
      {
        Iterator it = iterator();
        while (it.hasNext())
          {
            node = (INMNode) it.next();
            if (node.getProteinProbability() > p)
            {
                index++;
               continue;
            }
            else
            {   
                break;
            }
          }      
         add(index, protein);
      }
}

public INMNode GetProtein(int index)
{
    return (INMNode)get(index);
}
}
