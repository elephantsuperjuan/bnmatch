package Cytoscape.plugin.BNMatch.INM;
/**
 *
 * @author YULEI
 */
import Cytoscape.plugin.BNMatch.MainPanel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class INMAlgorithm 
{
   static INMEdgeList flyNetwork=new INMEdgeList("");//save all the edges of drosophila protein
   char[][] flyMatrix; //drosophila network matrix, for the fruit fly network transformation
   INMNodeList flyNodes;
   ArrayList<INMNode> originalSubnet=new ArrayList<INMNode>();//yeast subnet   
   INMEdgeList edgesOfSubnet;//Save all of the edges of yeast subnet
   INMEdgeList KNDs;//Save compulsory matching proteins 
   
   ArrayList<Integer> subPosition=new ArrayList<Integer>();//record the equivalent node location of this traverse
   HomogenMap homogenMap;
   HomogenMap fHomogenMapOfSim;//similarity matrix
   HomogenMap sHomogenMapOfSim;//similarity matrix
   
   INMNodeList yeastConnSubnet;	//yeast connected subnet
   INMNodeList flySubnet=new INMNodeList();	//extended drosophila subnet
   INMEdgeList edgesOfYeastConnSubnet;	// edges of yeast connected subnet
   INMEdgeList edgesOfFlySubnet=new INMEdgeList("");	// edges of extended drosophila subnet
   String code,
           MIPSFileName,
           SC_DMFileName,
           yeastFileName,
           KN_DFileName;
   
   static public String fileAbsolutePath;
   static public String fileName;
   INMAlgorithm()
    {
       edgesOfSubnet=new INMEdgeList("");
       yeastConnSubnet=new INMNodeList();
       homogenMap=new HomogenMap("");
       edgesOfYeastConnSubnet=new INMEdgeList("");
       KNDs=new INMEdgeList("");
       
       
//       cfg=new Config();
       flyNetwork.RetrieveEdgeFromFile(Config.getLargeFileName());
       homogenMap.ReadHomogenTable(Config.getTableFileName());
       flyNodes=new INMNodeList();
       Iterator it=flyNetwork.iterator();
       INMEdge edge=null;
       while(it.hasNext())
       {
           edge=(INMEdge)it.next();
           flyNodes.AddNewProtein(edge.GetFirstVertex());
           flyNodes.AddNewProtein(edge.GetSecondVertex());
       }
       
       int index=0;
       it=flyNodes.iterator();
       INMNode node=null;
       while(it.hasNext())
       {
           node=(INMNode)it.next();
           node.setIndex(index);
           index++;
       }  
    }

   public boolean InitializeAlgorithm(String code)
{
    this.code=code;
    this.MIPSFileName=Config.getMipsFileName();
    this.SC_DMFileName=Config.getTableFileName();
    this.yeastFileName=Config.getTargetFileName();
    this.KN_DFileName=Config.getMatchFileName();
    
    if(!ReadASubnet(code))
        return false;
    
    RetrieveSubnetEdge();
    
    return true;
}
   /**
    * read a subnet from a net file
    * @param code
    * @return boolean
    */
    public boolean ReadASubnet(String code)
    {
        FileStream fs=new FileStream(MIPSFileName,FileStream.BiologicalFileType.MIPS_PC);
        String a,b,c,layer,prefix;
        String[] temp=null;
        int pos=0,begin=0;
        prefix=code;
        prefix=prefix+".";
        
        if(fs.Open(MIPSFileName))
        {
            try
              {
                String s = fs.in.readLine().toUpperCase();
                if(s!=null)//first line
                {
                    temp=s.split("\\s");
                    layer=a=temp[0];
                    b=temp[1];
//                    c=temp[2];
                    a+=".";
                    pos=a.indexOf(prefix);
                    if(pos==begin)
                    {
                        INMNode node=new INMNode(b);//set complex code of protein
                        node.setLayerCode(layer);
                        INMNode n=new INMNode("");
                        n=(INMNode)node.clone();
                        originalSubnet.add(n);
                        subPosition.add(new Integer(-1));//the first node is set to -1
                    }
                }
                s=fs.in.readLine().toUpperCase();
                while(s!=null)
                {
                    temp=s.split("\\s");
                    layer=a=temp[0];
                    b=temp[1];
//                    c=temp[2];
                    a+=".";
                    pos=a.indexOf(prefix);
                    if(pos==begin)
                    {
                        INMNode node=new INMNode(b);//set complex code of protein
                        node.setLayerCode(layer);
                        INMNode n=new INMNode("");
                        n=(INMNode)node.clone();
                        originalSubnet.add(n);
                        subPosition.add(new Integer(0));
                    }
                   s=fs.in.readLine().toUpperCase(); 
                }
                
                if(subPosition.size()>0)
                    subPosition.set(0, new Integer(-1));
                subPosition.add(new Integer(0));
                return true;
              } catch (IOException ex)
              {
                Logger.getLogger(INMAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                return false;
              }
        }   
        return false;
    }
  
    /**
     *  get all edges of yeast subnet
     */
    public void RetrieveSubnetEdge()
    {
        INMEdgeList edgeList=new INMEdgeList(yeastFileName);//?
        
        
        int count=originalSubnet.size();
        for(int i=0;i<count-1;i++)
             for(int j=i+1;j<count;j++)
             {
                 INMEdge edge=new INMEdge(originalSubnet.get(i),originalSubnet.get(j));
                 if(edgeList.IsExistedEdge(edge))//？
                     edgesOfSubnet.AddNewEdge(edge);
             }
    }
    

    
 /**
  * execute the matching algorithm
  * @param resultNum
  * @return
  */   
    public INMEdgeList MatchAlgorithm(int resultNum)
    {
        INMEdgeList mateList=new INMEdgeList("");//edge structure to record the matching node(node 1 represents the yeast,node 2 represent the drosophila)
        INMNodeList matchedNodeList=new INMNodeList();//record yeast nodes which are matched
        
        HomogenMap file=new HomogenMap("");
        HomogenMap homogenMapOfSim;
        if(resultNum==1)
            homogenMapOfSim=fHomogenMapOfSim;//Similarity converges to a value only
        else
            homogenMapOfSim=sHomogenMapOfSim;//Similarity converges to the final two values,match the second convergence of the value
               
        if(homogenMapOfSim==null)
            return mateList;
        file=(HomogenMap)homogenMapOfSim.clone();
        //time control
        long start=System.currentTimeMillis();
        long end=0;
        long duration=0;
        
        INMNodeList PQ=new INMNodeList();
        INMNodeList tmp;
/*
        Iterator itt=homogenMapOfSim.homogenMapList.iterator();
        while(itt.hasNext())
        {
            Homogen ho=(Homogen)itt.next();
            Iterator ittt=ho.homogenList.iterator();
            System.out.println(ho.yeastProteinName);
            while(ittt.hasNext())
            {
                  INMNode node=(INMNode)ittt.next();
                  System.out.println(node.proteinName+" "+node.probability);
            }
            System.out.println();
        }
*/        
        INMNode u=new INMNode("");
        INMNode v=new INMNode("");
        INMNode tmpNode3=null;
        double deltaSim=Config.getWeight();
        
        Iterator it=yeastConnSubnet.iterator();
        Iterator tmpIt;
        INMNode node;
        while(it.hasNext())
        {
            tmpNode3=(INMNode)it.next();
            u=(INMNode)tmpNode3.clone();
            tmp=file.GetHomogenFlyProteinList(u.getProteinName());
            
            if(tmp.isEmpty())continue;//no yeast homologous protein
           v=(INMNode)((INMNode)tmp.getFirst()).clone();
            tmpIt=tmp.iterator();
            while(tmpIt.hasNext())
            {
                node=(INMNode)tmpIt.next();
                double nodeSim=node.getProteinProbability();
                if(nodeSim>0 && nodeSim>v.getProteinProbability())
                    v=(INMNode)node.clone();
            }
            if(v.getProteinProbability()>0)
            {
                mateList.AddNewEdge(u.getProteinName(), v.getProteinName(),
                        v.getProteinProbability(),INMEdge.enumEdgeType.DIRECTION);
                u.setProteinProbability(v.getProteinProbability());
                PQ.AddProteinByP(u);
            }   
        }
        
        INMNodeList nu=new INMNodeList();
        INMNodeList nv=new INMNodeList();
        
        while(!PQ.isEmpty())
        {
          
            end=System.currentTimeMillis();
            duration=start-end;
            if(duration>18000000) return mateList;//More than 5 hours,return 
            
            u=(INMNode)PQ.getFirst();
            PQ.removeFirst(); 
            v=mateList.GetFirstEdge(u).GetSecondVertex();         
            
            if(matchedNodeList.isExistProtein(u))
                continue;
            if(matchedNodeList.isExistProtein(v))
            {
                file.RemoveHomogenFlyProtein(u.getProteinName(), v.getProteinName());
                mateList.RemoveEdge(u);
                tmp=file.GetHomogenFlyProteinList(u.getProteinName());               
                if(tmp.isEmpty())
                    continue;
                
                v=(INMNode)((INMNode)(tmp.getFirst())).clone();
                Iterator iterator=tmp.iterator();
                while(iterator.hasNext())
                {
                    node=(INMNode)iterator.next();
                    double nodeSim=node.getProteinProbability();
                    if((nodeSim>0)&& (nodeSim>v.getProteinProbability()))
                        v=(INMNode)node.clone();
                }
                
                if(v.getProteinProbability()>0)
                {
                    mateList.AddNewEdge(u.getProteinName(), v.getProteinName(),
                            v.getProteinProbability(), INMEdge.enumEdgeType.DIRECTION);
                    u.setProteinProbability(v.getProteinProbability());
                    PQ.AddProteinByP(u);
                }
                continue;
            }
            matchedNodeList.AddNewProtein(u);
            matchedNodeList.AddNewProtein(v);
            
            nu.clear();
            nv.clear();
            flyNetwork.SortEdgeList();
            it=flyNetwork.iterator();
            INMEdge edge=null;
            while(it.hasNext())
            {
                edge=(INMEdge)it.next();
                if((edge.GetFirstVertex().equals(v) )&& (!matchedNodeList.isExistProtein(edge.GetSecondVertex())))
                    nv.AddNewProtein(edge.GetSecondVertex());
                if((edge.GetSecondVertex().equals(v)) && (!matchedNodeList.isExistProtein(edge.GetFirstVertex())))
                    nv.AddNewProtein(edge.GetFirstVertex());
            }
            edgesOfYeastConnSubnet.SortEdgeList();
            it=edgesOfYeastConnSubnet.iterator();
            while(it.hasNext())
            {
                edge=(INMEdge)it.next();
                if((edge.GetFirstVertex().equals(u)) && (!matchedNodeList.isExistProtein(edge.GetSecondVertex())))
                    nu.AddNewProtein(edge.GetSecondVertex());
                if((edge.GetSecondVertex().equals(u)) && (!matchedNodeList.isExistProtein(edge.GetFirstVertex())))
                    nu.AddNewProtein(edge.GetFirstVertex());
            }
                      
            nu.SortNodeList();
            nv.SortNodeList();
             
            INMNode tmpNode=new INMNode("");
            INMNode tmpNode1=null;
            INMNode tmpNode2=null;
            boolean mChanged=false;
            it=nu.iterator();
            while(it.hasNext())
            {
                node=(INMNode)it.next();
                tmpIt=nv.iterator();
                while(tmpIt.hasNext())
                {
                    tmpNode1=(INMNode)tmpIt.next();//?
                   tmpNode=(INMNode)tmpNode1.clone();
                    tmpNode.setProteinProbability(0);
                    tmp=file.GetHomogenFlyProteinList(node.getProteinName());
                    
                    Iterator iterator=tmp.iterator();
                    while(iterator.hasNext())
                    {
                        tmpNode2=(INMNode)iterator.next();
                        if(tmpNode2.equals(tmpNode1))
                           tmpNode=(INMNode)tmpNode2.clone();//?

                    }
                    file.RemoveHomogenFlyProtein(node.getProteinName(), tmpNode1.getProteinName());
                    tmpNode.setProteinProbability(tmpNode.getProteinProbability()+deltaSim);
                    file.AddHomogenFlyPotein(node.getProteinName(), tmpNode);                            
                    if((tmpNode.getProteinProbability()>mateList.GetFirstEdge(node).GetEdgeWeight())
                            && (!matchedNodeList.isExistProtein(node)))
                    {
                        mateList.RemoveEdge(node);
                        mateList.AddNewEdge(node.getProteinName(), tmpNode.getProteinName(),
                                tmpNode.getProteinProbability(), INMEdge.enumEdgeType.DIRECTION);
                        mChanged=true;
                    }
                }
                
                if(mChanged)
                {
                    tmpNode.setProteinName(node.getProteinName());
                    PQ.AddProteinByP(tmpNode);
                }
                mChanged=false;                
            }
        }
        return mateList;
    }
   

    public INMEdgeList GetYeastSubnet()
    {
        return edgesOfYeastConnSubnet;
    }


    public int FindYeastConnectivitySubnet(String strCode)
    {
        int subnetNum=0;
        Queue<INMNode> VQ=new LinkedList<INMNode>();
       INMNode u1,u2,v;
       String code=null,code1=null,code2=null,prefix=null;
       prefix=strCode;
       prefix+=".";
        if(originalSubnet.size()==0)return subnetNum;
       subnetNum=1;
        u1=originalSubnet.get(0);
        yeastConnSubnet.AddNewProtein(u1);
        VQ.offer(u1);
        Iterator it=null;
        while(!VQ.isEmpty())
        {
            v=VQ.poll();
            it=edgesOfSubnet.iterator();
            INMEdge edge=null;
            while(it.hasNext())
            {
                edge=(INMEdge)it.next();
                u1=edge.GetFirstVertex();
                u2=edge.GetSecondVertex();
                
                code1=u1.getLayerCode();
                code2=u2.getLayerCode();
                
                code=code1=code1.concat(".");
                int pos1=code.indexOf(prefix);
                
                code=code2=code2.concat(".");
                int pos2=code.indexOf(prefix);
                
                if(pos1!=0 || pos2!=0) continue;//Vertex does not belong to this layer
                
                if(u1.equals(v))
                {
                   if(!yeastConnSubnet.isExistProtein(u2)) 
                   {
                       yeastConnSubnet.AddNewProtein(u2);
                       VQ.offer(u2);
                   }
                   edgesOfYeastConnSubnet.AddNewEdge(edge);
                }
                else if (u2.equals(v))
                {
                    if(!yeastConnSubnet.isExistProtein(u1))
                    {
                        yeastConnSubnet.AddNewProtein(u1);
                        VQ.offer(u1);
                    }
                   edgesOfYeastConnSubnet.AddNewEdge(edge); 
                }
                else
                    continue;
                
                int pos=0;
                code=code1=code1.concat(".");
                if(pos1==0)code=code.substring(prefix.length());
                pos=code.indexOf(".");
                code1=prefix;
                if(pos!=-1)
                {
                    code1+=code.substring(0, pos);//There is a low-level layer
                }
                
                code=code2=code2.concat(".");
                if(pos2==0)code=code.substring(prefix.length());
                pos=code.indexOf(".");
                code2=prefix;
                if(pos!=-1)
                {
                    code2+=code.substring(0, pos);//There is a low-level layer
                }
                if(!code1.equals(code2))
                {
                    subnetNum=2;//Vertices of connected graph is in different low-level layers 
                }
                else if(!prefix.equals(code1) && subnetNum!=2)
                {
                    subnetNum=-1;//Vertices of connected graph is in the same low-level layers
                }
            }            
        }
        
        it=yeastConnSubnet.iterator();
        INMNode node=null;
        while(it.hasNext())
        {
            node=(INMNode)it.next();
            int index=originalSubnet.indexOf(node);
            if(index!=-1)
            originalSubnet.remove(index);
        }
        
        it=edgesOfYeastConnSubnet.iterator();
        INMEdge edge=null;
        while(it.hasNext())
        {
            edge=(INMEdge)it.next();
            edgesOfSubnet.RemoveEdge(edge);
        }
        
        if(yeastConnSubnet.size()<2)subnetNum=-1;
        return subnetNum;
    }
    
    public INMNodeList GetYeastSubnetNodes()
    {
        return yeastConnSubnet;
    }

/**
 * compute the similarity matrix S
 * @param randNo
 * @param yeastSubnnetNo
 * @return
 */
    public int ComputeSim(int randNo,int yeastSubnnetNo)
    {
        if(yeastConnSubnet.size()==0)return 1;

        
        FindFlySubnet();
        if(flySubnet.size()==0) return 1;
        int n1,n2;
        n1=yeastConnSubnet.size();
        n2=flySubnet.size();
        

        double[][] Sk0=new double[n1][n2];
        double[][] Sk1=new double[n1][n2];
        double[][] Sk2=new double[n1][n2];
        double[][] Sim=new double[n1][n2];
        double[][] kk=new double[n1][n2];
        double[][] tmpsk;
        
        double[] wr=new double[n1];
        double[] wi=new double[n1];
        double[][] vl=new double[n1][n1];
        double[][] vr=new double[n1][n1];
        double[][] work=new double[3*n1][3*n1];
        
        INMNodeList[] adjYeast=new INMNodeList[n1];//Adjacency matrix of yeast
        INMNodeList[] adjFly=new INMNodeList[n2];//Adjacency matrix of Drosophila
        
        int index=0;
        int degree=0;
        INMNode first=null,
                second=null;
        
        Iterator it=yeastConnSubnet.iterator();
        
        INMNode node=null;

        INMEdge edge=null;
        //get arrays and vertex degree in connected subnet of yeast 
        while(it.hasNext())
        {
            node=(INMNode)it.next();
            degree=0;
            node.setIndex(index);
            adjYeast[index]=new INMNodeList();
            Iterator iter=edgesOfYeastConnSubnet.iterator();
            while(iter.hasNext())
            {
                edge=(INMEdge)iter.next();
                first=edge.GetFirstVertex();
                second=edge.GetSecondVertex();
                if(first.equals(node))
                {
                    degree++;
  
                    INMNode n=new INMNode("");
                    n=(INMNode)second.clone();
                    adjYeast[index].add(n);
                }
                else if(second.equals(node))
                {
                    degree++;
                    INMNode n=new INMNode("");
                    n=(INMNode)first.clone();
                    adjYeast[index].add(n);
                }
            }
            node.setDegree(degree);
            index++;
        }
        
        index=0;

        it=flySubnet.iterator();
        //get arrays and vertex degree in connected subnet of drosophila
        while(it.hasNext())
        {
            node=(INMNode)it.next();
            degree=0;
            node.setIndex(index);
            adjFly[index]=new INMNodeList();
            Iterator iter=edgesOfFlySubnet.iterator();
            while(iter.hasNext())
            {
                edge=(INMEdge)iter.next();
                first=edge.GetFirstVertex();
                second=edge.GetSecondVertex();
                if(first.equals(node))
                {
                    degree++;
 
                    INMNode n=new INMNode("");
                    n=(INMNode)second.clone();
                    adjFly[index].add(n);
                }
                else if(second.equals(node))
                {
                    degree++;

                    INMNode n=new INMNode("");
                    n=(INMNode)first.clone();
                    adjFly[index].add(n);               
                }
            }
            iter=edgesOfFlySubnet.iterator();
            node.setDegree(degree);
            index++;
        }
       
       int row=0;
       int col=0;
       INMNodeList tmp;
       double sumSim=0.0d;
       double aveSim=0.0d;
        it=yeastConnSubnet.iterator();
        while(it.hasNext())
        {
            node=(INMNode)it.next();
            tmp=homogenMap.GetHomogenFlyProteinList(node.getProteinName());
            if(tmp.isEmpty())continue;
            Iterator iterator=tmp.iterator();
            while(iterator.hasNext())
            {
                INMNode n=(INMNode)iterator.next();
                row=node.getIndex();
                col=flySubnet.FindProteinByName(n.getProteinName()).getIndex();
                Sk2[row][col]=n.getProteinProbability();
                Sim[row][col]=n.getProteinProbability();
                sumSim+=Sim[row][col];
            }
        }
        aveSim = sumSim/(n1*n2);
        
        
        int iIndex,jIndex,uIndex,vIndex;
        int iDegree,jDegree;
        double A1,A3,D1,D2,a1,a3,d1,d2;
        double maxw, sqrtMaxw, deltaSk01,deltaSk02,maxDeltaSk01=0.0d,
                maxDeltaSk02=0,maxSk,minSk;
        
        
        


        INMNode iNode=null,
                jNode=null,
                uNode=null,
                vNode=null;
        int times=0;
        do
        {
            if(times>1000) break;
            
            Iterator iIt=yeastConnSubnet.iterator();
            while(iIt.hasNext())
            {
                iNode=(INMNode)iIt.next();
                Iterator jIt=flySubnet.iterator();
                while(jIt.hasNext())
                {
                    jNode=(INMNode)jIt.next();
         	    A1 = 0.0d;
		    A3 = 0.0d;
		    D1 = 0.0d;
		    D2 = 0.0d;    
                    
                    iIndex=iNode.getIndex();
                    jIndex=jNode.getIndex();
                    iDegree=iNode.getDegree();
                    jDegree=jNode.getDegree();
                    if(Sim[iIndex][jIndex]==0)
                    {
                        Sk1[iIndex][jIndex]=0;
                        continue;
                    }
                    Iterator uIt=yeastConnSubnet.iterator();
                    while(uIt.hasNext())
                    {
                        uNode=(INMNode)uIt.next();
                        uIndex=uNode.getIndex();
                        if(!iNode.equals(uNode))
                        {
                            Iterator vIt=flySubnet.iterator();
                            while(vIt.hasNext())
                            {
                                vNode=(INMNode)vIt.next();
                                if(!jNode.equals(vNode))
                                {
                                    vIndex = vNode.getIndex();
                                    a1 = 0;
                                    a3 = 0;
                                    d1 = 0;
                                    d2 = 0;
                                   
                                    if (adjYeast[iIndex].isExistProtein(uNode))
                                      {
                                        if (adjFly[jIndex].isExistProtein(vNode))
                                          {
                                            a1 = Sk2[uIndex][vIndex];
                                          } else
                                          {
                                            d1 = Sk2[uIndex][vIndex];
                                          }
                                      } else
                                      {
                                        if (adjFly[jIndex].isExistProtein(vNode))
                                          {
                                            d2 = Sk2[uIndex][vIndex];
                                          } else
                                          {
                                            a3 = Sk2[uIndex][vIndex];
                                          }
                                      }
                                    //a2->a, b2->b(A1)
                                    if ((iDegree == 0) && (jDegree == 0))
                                      {
                                        A1 = A1 + a1 / (n1 * n2);
                                      } else if ((iDegree != 0) && (jDegree != 0))
                                      {
                                        A1 = A1 + a1 / (iDegree * jDegree);
                                      } else
                                      {
                                        A1 = 0;
                                      }
                                    //a2 not ->a, b2 not->b(A3)
                                    if ((iDegree == n1) && (jDegree == n2))
                                      {
                                        A3 = A3 + a3 / (n1 * n2);
                                      } else if ((iDegree != n1) && (jDegree != n2))
                                      {
                                        A3 = A3 + a3 / ((n1 - iDegree) * (n2 - jDegree));
                                      } else
                                      {
                                        A3 = 0;
                                      }
                                    //a2->a, b2 not->b(D1)
                                    if ((iDegree == 0) && (jDegree == n2))
                                      {
                                        D1 = D1 + d1 / (n1 * n2);
                                      } else if ((iDegree != 0) && (jDegree != n2))
                                      {
                                        D1 = D1 + d1 / (iDegree * (n2 - jDegree));
                                      } else
                                      {
                                        D1 = 0;
                                      }
                                    //a2 not ->a, b2 ->b(D2)
                                    if ((iDegree == n1) && (jDegree == 0))
                                      {
                                        D2 = D2 + d2 / (n1 * n2);
                                      } else if ((iDegree != n1) && (jDegree != 0))
                                      {
                                        D2 = D2 + d2 / ((n1 - iDegree) * jDegree);
                                      } else
                                      {
                                        D2 = 0;
                                      }                                   
                                }
                            }
                        }                                                
                    }
               if(MainPanel.INMAlgorithm.isSelected())
               {
                Sk1[iIndex][jIndex] = Sim[iIndex][jIndex] * (A1 + A3) / 2;//INM
               }else
               {
                Sk1[iIndex][jIndex] = Sim[iIndex][jIndex]*(A1 + A3 - D1 - D2)/2; //BNM   
               }                    
                }
            }
            
            double alpha = 1;
            double beta = 0;

            int m, n, k, lda, ldb, ldc;
            m = n1;
            n = n1;
            k = n2;
            lda = n1;
            ldb = n2;
            ldc = n1;
            sqrtMaxw = 0;
            maxw = 0;           

            for (int i = 0; i < n1; i++)
                if ((wr[i] + maxw) == 0)
                  {
                    maxw = wr[i];
                  }

            for (int i = 0; i < n1; i++)
                for (int j = 0; j < n2; j++)
                  {
                    if (Sk1[i][j] > sqrtMaxw)
                      {
                        sqrtMaxw = Sk1[i][j];
                      }
                  }

            maxDeltaSk01 = 0;
            deltaSk01 = 0;
            maxDeltaSk02 = 0;
            deltaSk02 = 0;
            maxSk = 0;
            minSk = 1;
            for (int i = 0; i < n1; i++)
                for (int j = 0; j < n2; j++)
                  {
                    Sk1[i][j] = Sk1[i][j] / sqrtMaxw;
                    deltaSk01 = Math.abs(Math.abs(Sk1[i][j]) - Math.abs(Sk2[i][j]));//S(k) - S(k-1)
                    deltaSk02 = Math.abs(Math.abs(Sk1[i][j]) - Math.abs(Sk0[i][j]));//S(k) - S(k-2)
                    if (deltaSk01 > maxDeltaSk01)
                      {
                        maxDeltaSk01 = deltaSk01;
                      }
                    if (deltaSk02 > maxDeltaSk02)
                      {
                        maxDeltaSk02 = deltaSk02;
                      }
                    if (Sk1[i][j] > maxSk)
                      {
                        maxSk = Sk1[i][j];
                      }
                    if (Sk1[i][j] < minSk)
                      {
                        minSk = Sk1[i][j];
                      }
                  }


            tmpsk = Sk2;
            Sk2 = Sk1;
            Sk1 = Sk0;
            Sk0 = tmpsk;
            times++;
        }while(maxDeltaSk01 > 0.01 && maxDeltaSk02 > 0.01);
        
        
        
        int resultNum=1;
        
        ReadKN_D();	//designate mandatory matching points
        it=KNDs.iterator();
        while(it.hasNext())
        {
            edge=(INMEdge)it.next();
            int indexY=0,indexC=0;
            INMNode nodeY=yeastConnSubnet.FindProteinByName(edge.GetFirstVertex().getProteinName());
            INMNode nodeC=flySubnet.FindProteinByName(edge.GetSecondVertex().getProteinName());
            if(nodeY!=null && nodeC!=null)//?
            {
                indexY=nodeY.getIndex();
                indexC=nodeC.getIndex();
                Sk1[indexY][indexC] = 1.1;
                Sk0[indexY][indexC] = 1.1;                
            }            
        }
	SimMatrixToHomogenMap(Sk1, 1);
	if(maxDeltaSk01 > 0.01) 
	{
		SimMatrixToHomogenMap(Sk0, 2);
		resultNum = 2;
	}        
      MainPanel.runInformation.append("ComputeSim finished!\n");
        return resultNum;
    }
 
/**
 * generate subgraph to match the fruit fly and yeast
 */
    public void FindFlySubnet()
    {
        if(yeastConnSubnet.size()==0) return;	//yeast connected subnet is empty
        flySubnet.clear();
        edgesOfFlySubnet.clear();
        
        INMNodeList tmp, tmpFlySubnet;
        Iterator it=yeastConnSubnet.iterator();
        INMNode node;
        while(it.hasNext())
        {
            node=(INMNode)it.next();
            tmp=homogenMap.GetHomogenFlyProteinList(node.getProteinName());
            if(tmp.isEmpty()) continue;
            
            Iterator iter=tmp.iterator();
            while(iter.hasNext())
            {
                INMNode n=(INMNode)iter.next();
                flySubnet.AddNewProtein(n);
            }
        }
//           tmp=homogenMap.GetHomogenFlyProteinList("A");
       
        tmpFlySubnet=flySubnet;
        it=flyNetwork.iterator();
        while(it.hasNext())
        {
          INMEdge edge=(INMEdge)it.next();
          if(flySubnet.isExistProtein(edge.GetFirstVertex()))
          {
              tmpFlySubnet.AddNewProtein(edge.GetSecondVertex());
          }
          else if (flySubnet.isExistProtein(edge.GetSecondVertex()))
          {
              tmpFlySubnet.AddNewProtein(edge.GetFirstVertex());
          }
        }

        flySubnet=tmpFlySubnet;//?
        int count=flySubnet.size();
        for(int i=0;i<count;i++)
        {
            for(int j=i+1;j<count;j++)
            {
                INMEdge e=new INMEdge(((INMNode)flySubnet.get(i)).getProteinName(),
                        ((INMNode)flySubnet.get(j)).getProteinName());
                if(flyNetwork.IsExistedEdge(e))
                    edgesOfFlySubnet.AddNewEdge(e);
            }
        }
        MainPanel.runInformation.append("The GoalSubnet Nodes:"+yeastConnSubnet.size()+"\n");
        MainPanel.runInformation.append("The HomoFlynet Nodes:"+flySubnet.size()+"\n");
    }
    
 /**
  * generate HomogenMap
  * @param s
  * @param resultNum
  */
    public void SimMatrixToHomogenMap(double[][] s,int resultNum)
    {
        HomogenMap homogenMapOfSim=new HomogenMap("");
        Iterator it=yeastConnSubnet.iterator();
        INMNode node=null;        
        while(it.hasNext())
        {
            node=(INMNode)it.next();
            Homogen homo=new Homogen("");//?
            homo.SetYeastProteinID(node.getIndex());
            homo.SetYeastProteinName(node.getProteinName());
            homo.SetYeastProteinScore(0);
            Iterator iter=flySubnet.iterator();
            while(iter.hasNext())
            {
                INMNode n=(INMNode)iter.next();
                homo.AddHomogenFlyProtein(n.getProteinName(), s[node.getIndex()][n.getIndex()]);//?
            }
            homogenMapOfSim.AddHomogenColumn(homo);
        }
        
        if(resultNum==1)
        {
            fHomogenMapOfSim=homogenMapOfSim;//?
        }
        else
            sHomogenMapOfSim=homogenMapOfSim;
    }
    
/**
 * transformation list structure of drosophila network into matrix 
 * @return int
 */    
    public int FlyNetToMatrix()
    {
        int nLen=flyNodes.size();
        flyMatrix=new char[nLen][nLen];//?
        
        for (int i = 0; i < nLen; i++)
          {
            for (int j = 0; j < nLen; j++)
              {
                flyMatrix[i][j] = '0';
              }
          }      
        MainPanel.runInformation.append("FlyNetToMatrix();\n");        
        int row,col;
        int count_edges=0;
        Iterator it=flyNetwork.iterator();
        INMEdge edge=null;
        int index1=0;
        int index2=0;
        while(it.hasNext())
        {
            edge=(INMEdge)it.next();
            index1=flyNodes.indexOf(edge.GetFirstVertex());
            index2=flyNodes.indexOf(edge.GetSecondVertex());
            if(index1!=-1&& index2!=-1)
            {
                row = ((INMNode)flyNodes.get(index1)).getIndex();
                col = ((INMNode)flyNodes.get(index2)).getIndex();
                flyMatrix[row][col] = '1';
                flyMatrix[col][row] = '1';
                count_edges++;                
            }
        }
        return 1;
    }
    
 /**
  * transformation matrix of drosophila network into list structure
  */   
    public void FlyMatrixToNet()
    {
        flyNetwork.clear();
        Iterator it1=flyNodes.iterator();
        Iterator it2=flyNodes.iterator();
        INMNode n1=null;
        INMNode n2=null;
        while(it1.hasNext())
        {
            n1=(INMNode)it1.next();
            while(it2.hasNext())
            {
                n2=(INMNode)it2.next();
                if(flyMatrix[n1.getIndex()][n2.getIndex()]=='1')
                {
                    INMEdge e=new INMEdge(n1,n2);
                    flyNetwork.AddNewEdge(e);
                }
            }
        }
        MainPanel.runInformation.append("FlyMatrixToNet finished!The edges number:"
                +flyNetwork.size()+"\n");
    }
    
/**
 * output matching results to a file
 * @param matchEdges
 * @param randNo
 * @param yeastSubnetNo
 * @param resultNum
 */    
    public void OutPut(INMEdgeList matchEdges,int randNo,int yeastSubnetNo,int resultNum)
    {
        ArrayList<INMNode> matchFlyNodes=new ArrayList<INMNode>();
       
        Iterator it=matchEdges.iterator();
        INMEdge edge=null;
        while(it.hasNext())
        {
            edge=(INMEdge)it.next();
            INMNode n=new INMNode("");
            n=(INMNode)edge.GetSecondVertex().clone();
            matchFlyNodes.add(n);
        }
               
        INMEdgeList tmpFlySubnet=new INMEdgeList("");
        INMEdgeList tmpYeastSubnet=new INMEdgeList("");
        INMEdgeList file=flyNetwork;
        
        int count=matchFlyNodes.size();
        for(int i=0;i<count;i++)
            for(int j=i+1;j<count;j++)
            {
                edge=new INMEdge(matchFlyNodes.get(i).getProteinName(),
                        matchFlyNodes.get(j).getProteinName());
                if(file.IsExistedEdge(edge))
                    tmpFlySubnet.AddNewEdge(edge);
            }
        
        tmpYeastSubnet=edgesOfYeastConnSubnet;
        
        INMNodeList yeastConnNodes=yeastConnSubnet;
         
        ArrayList<INMEdge> tmpFlySubnetArray = new ArrayList<INMEdge>();
        ArrayList<INMEdge> tmpYeastSubnetArray = new ArrayList<INMEdge>();
        ArrayList<INMEdge> edgesOfFlySubnetArray = new ArrayList<INMEdge>();
        ArrayList<INMEdge> matchEdgesArray = new ArrayList<INMEdge>();
        it = tmpFlySubnet.iterator();
        while (it.hasNext())
          {
            edge = (INMEdge) it.next();
            tmpFlySubnetArray.add(edge);
          }
        it = tmpYeastSubnet.iterator();
        while (it.hasNext())
          {
            edge = (INMEdge) it.next();
            tmpYeastSubnetArray.add(edge);
          }
        it = edgesOfFlySubnet.iterator();
        while (it.hasNext())
          {
            edge = (INMEdge) it.next();
            edgesOfFlySubnetArray.add(edge);
          }
        it = matchEdges.iterator();
        while (it.hasNext())
          {
            edge = (INMEdge) it.next();
            matchEdgesArray.add(edge);
          }
                       
        Collections.sort(tmpFlySubnetArray, new Comparator<INMEdge>()//sort the edges
        {

            public int compare(INMEdge edge0, INMEdge edge1)
            {
                return edge0.toString().compareTo(edge1.toString());
            }
        });
        
        Collections.sort(tmpYeastSubnetArray, new Comparator<INMEdge>()//sort the edges
        {

            public int compare(INMEdge edge0, INMEdge edge1)
            {
                return edge0.toString().compareTo(edge1.toString());
            }
        });
        
        Collections.sort(edgesOfFlySubnetArray, new Comparator<INMEdge>()//sort the edges
        {

            public int compare(INMEdge edge0, INMEdge edge1)
            {
                return edge0.toString().compareTo(edge1.toString());
            }
        });
        
        Collections.sort(matchEdgesArray, new Comparator<INMEdge>()//sort the edges
        {

            public int compare(INMEdge edge0, INMEdge edge1)
            {
                return edge0.toString().compareTo(edge1.toString());
            }
        });
    
        try
          {
            File dirFile=new File("output");//build output folder 
             if(!dirFile.exists())
                dirFile.mkdir();
            fileName=randNo+"-"+code+"-"+yeastSubnetNo+"."+resultNum+".sif"; 
            String dirFileName=dirFile.getPath()+"\\"+fileName;
            fileAbsolutePath=dirFile.getAbsolutePath()+"\\"+fileName;
            
            File outputFile=new File(dirFileName);
             
             PrintWriter outputStream=new PrintWriter(new FileOutputStream(outputFile,false));
                         
             for(INMEdge e:tmpFlySubnetArray)
             {
                 outputStream.println(e.GetFirstVertex().getProteinName()+" p2 "+e.GetSecondVertex().getProteinName());
             }
             
             
            for(INMEdge e:tmpYeastSubnetArray)
             {
                 outputStream.println(e.GetFirstVertex().getProteinName()+" p1 "+e.GetSecondVertex().getProteinName());
             }
             
            for(INMEdge e:matchEdgesArray)
             {
                 outputStream.println("-"+e.GetFirstVertex().getProteinName()+" pd -"+e.GetSecondVertex().getProteinName());
             }
             
             outputStream.close();
          } catch (FileNotFoundException e)
          {
            MainPanel.runInformation.append("No such file!\n");
          } catch (IOException e)
          {
            MainPanel.runInformation.append("IO exception!\n");
          } 
    }

/**
 * transform the network of drosophila
 */
    public void FlyNetworkTransform()
    {
      if(FlyNetToMatrix()<0)
      {
          return;
      }
      
      int nLen=flyNodes.size();
      Util.generateSrand(flyMatrix, nLen, nLen);
      FlyMatrixToNet();
    }


    public void SetTransformSeed()
    {
        
    }

/**
 * gey fly protein network
 * @return
 */
    public INMEdgeList GetFlyNetwork()
    {
        return flyNetwork;
    }

    /**
     * get mandatory matching protein pairs
     */
    public void ReadKN_D()
    {
        FileStream file=new FileStream(KN_DFileName,FileStream.BiologicalFileType.KN_D);

        if(file.Open(KN_DFileName))
        {
            try
              {
                String s = file.getBufferedReader().readLine().toUpperCase();
                while(s!=null)
                {
                    String[] textLine = s.split("\\s");
                    
                    KNDs.AddNewEdge(textLine[0].toUpperCase(), textLine[1].toUpperCase(),0, 
                            INMEdge.enumEdgeType.DIRECTION);
                    s = file.getBufferedReader().readLine().toUpperCase();
                }
                
              } catch (FileNotFoundException e)
              {
                MainPanel.runInformation.append("No such file!\n");  
              } catch (IOException e)
              {
                MainPanel.runInformation.append("IO exception!\n");  
              }            
        }
        
    }
}
