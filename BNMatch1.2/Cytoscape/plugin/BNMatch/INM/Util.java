package Cytoscape.plugin.BNMatch.INM;
/**
 *
 * @author YULEI
 */

import java.util.Random;

public class Util 
{

    static public boolean  generateSrand(char[][] Srand,int inLen,int outLen)
    {
        int nrew=0;
        int ntry=0;
        
        int[] i1=new int[inLen * outLen];
        int[] j1=new int[inLen * outLen];
        
        for(int i=1;i<inLen;i++)
            for(int j=0;j<i;j++)
            {
                if(Srand[i][j]=='1')
                {
                    i1[ntry]=i;
                    j1[ntry++]=j;
                }
            }
        System.out.println("nRow : "+inLen+"\tnColumn: "+outLen+"\tTotal: "+ntry);
        
        double Ne=ntry;
        ntry<<=1;
        Random r=new Random();

        for(int i=0;i<ntry;i++)
        {
            int e1=(int) (Ne *r.nextDouble());
            int e2=(int) (Ne *r.nextDouble());
            
	int v1 = i1[e1];
	int v2 = j1[e1];
	int v3 = i1[e2];
	int v4 = j1[e2];
        
	if((v1 != v3) && (v1 != v4) && (v2 != v4) && (v2 != v3))
        {
             if(r.nextDouble()<=0.5d)
             {
                  if(Srand[v1][v3]=='0'&& Srand[v2][v4]=='0')
                  {
		    Srand[v1][v2] = '0';
		    Srand[v3][v4] = '0';
		    Srand[v2][v1] = '0';
		    Srand[v4][v3] = '0';

		    Srand[v1][v3] = '1';
		    Srand[v2][v4] = '1';
		    Srand[v3][v1] = '1';
		    Srand[v4][v2] = '1';
		    nrew++;
		    i1[e1] = v1;
		    j1[e1] = v3;
		    i1[e2] = v2;
		    j1[e2] = v4;                      
                  }
             }else
             {
                 int temp=v3;
                 v3=v4;
                 v4=temp;
		if((Srand[v1][v3] == '0') && (Srand[v2][v4] == '0'))
		{
		    Srand[v1][v2] = '0';
		    Srand[v4][v3] = '0';
		    Srand[v2][v1] = '0';
		    Srand[v3][v4] = '0';

		    Srand[v1][v3] = '1';
		    Srand[v2][v4] = '1';
		    Srand[v3][v1] = '1';
		    Srand[v4][v2] = '1';
		    nrew++;
		    i1[e1] = v1;
		    j1[e1] = v3;
		    i1[e2] = v2;
		    j1[e2] = v4;
		}                 
                 
             }
        }
        }
        return true;
    }

}
