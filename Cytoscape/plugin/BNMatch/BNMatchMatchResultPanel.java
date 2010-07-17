/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Cytoscape.plugin.BNMatch;
/**
 *
 * @author YULEI
 */
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.actions.GinyUtils;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author YULEI
 */
public class BNMatchMatchResultPanel extends JPanel
{
    String resultTitle;
    JScrollPane matchListPanel;
    JPanel nodesPanel;
    JPanel edgesPanel;
    JPanel bottomPanel;
    ArrayList<CyNode> matchNodes;
    ArrayList<CyEdge> matchEdges;
    
    Object matchNodesData[][];
    int matchNodesCount;
    Object matchEdgesData[][];
    int matchEdgesCount;
    
    final int TABLEHEIGHT=120;
    final int TABLEWIDTH=200;
    CyNetwork largeNetwork;
    CyNetwork targetNetwork;
    CyNetworkView largeNetworkView;
    CyNetworkView targetNetworkView;
    int type;
    BNMatchMatchResultPanel(ArrayList<CyNode> matchNodes,ArrayList<CyEdge> matchEdges,CyNetwork largeNetwork,
            CyNetwork targetNetwork)
    {
        this.matchNodes=matchNodes;
        this.matchEdges=matchEdges;
        
        this.largeNetwork=largeNetwork;
        this.targetNetwork=targetNetwork;
        largeNetworkView=Cytoscape.createNetworkView(largeNetwork);
        targetNetworkView=Cytoscape.createNetworkView(targetNetwork);
        matchNodesCount=matchNodes.size()/2;
        matchEdgesCount=matchEdges.size()/2;
        
        setLayout(new BorderLayout());
        matchListPanel=selectNodesOrEdgesPanel();
        bottomPanel=createBottomPanel();
        add(matchListPanel,BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);
        this.setSize(this.getMinimumSize());
    }

    private JScrollPane selectNodesOrEdgesPanel()
    {
        JPanel nodesOrEdges = new JPanel();
        nodesOrEdges.setLayout(new BoxLayout(nodesOrEdges, BoxLayout.Y_AXIS));

        JRadioButton matchNodesRadio = new JRadioButton("Matched nodes",true);
        matchNodesRadio.setToolTipText("matched nodes list");
        JRadioButton matchEdgesRadio = new JRadioButton("Matched edges",false);
        matchNodesRadio.setToolTipText("matched edges list");

        matchNodesRadio.setActionCommand("1");//match nodes
        matchEdgesRadio.setActionCommand("2");//match edges
        matchNodesRadio.addActionListener(new NodesOrEdgesPanelAction());
        matchEdgesRadio.addActionListener(new NodesOrEdgesPanelAction());
        
        ButtonGroup scopeOptions = new ButtonGroup();
        scopeOptions.add(matchNodesRadio);
        scopeOptions.add(matchEdgesRadio);
        
        nodesOrEdges.add(matchNodesRadio);
        nodesOrEdges.add(matchEdgesRadio);
        //panel.setPreferredSize(new java.awt.Dimension(185, 164));
        nodesOrEdges.setToolTipText("Please select a search algorithm"); 
        
        JPanel options=new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        nodesPanel=createMatchListPanel(1);
        nodesPanel.setVisible(matchNodesRadio.isSelected());
        edgesPanel=createMatchListPanel(2);
        edgesPanel.setVisible(matchEdgesRadio.isSelected());
        options.add(nodesPanel);
        options.add(edgesPanel);
        type=1;//first selection is nodes
        
        JPanel p=new JPanel();
        p.setLayout(new BorderLayout());
        p.add(nodesOrEdges,BorderLayout.NORTH);
        p.add(options,BorderLayout.CENTER);
        JScrollPane scrollPanel = new JScrollPane(p);
        scrollPanel.setBorder(BorderFactory.createTitledBorder("nodes,edges"));
        return scrollPanel;
    }

    private JPanel createMatchListPanel(int type)
    {
        TableModel dataModel=null;
        String str="";
        this.type = type;

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        if (1 == type)
        {
            str = "Match Nodes( " + matchNodesCount + " in total pairs)";
            dataModel = new BNMatchMatchResultPanel.MatchNodesTableModel(matchNodes);
        }
        if (2 == type)
        {
            str = "Match Edges( " + matchEdgesCount + " in total pairs)";
            dataModel = new BNMatchMatchResultPanel.MatchEdgesTableModel(matchEdges);
        }
        panel.setBorder(BorderFactory.createTitledBorder(str));

        JTable table = new JTable(dataModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setPreferredScrollableViewportSize(new Dimension(TABLEWIDTH, TABLEHEIGHT));
        table.setFillsViewportHeight(true);
        table.setGridColor(Color.LIGHT_GRAY);

        ListSelectionModel rowSM = table.getSelectionModel();

        rowSM.addListSelectionListener(new BNMatchMatchResultPanel.TableRowSelectionAction(table));

        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.getViewport().setBackground(Color.GRAY);


        panel.add(scrollpane, BorderLayout.CENTER);
        panel.setToolTipText("information of the identified complexes");
        return panel;
    }

    
/**
 * create the close button for this match nodes panel
 * @return JPanel
 */
     private JPanel createBottomPanel() 
     {
        JPanel panel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.setToolTipText("Close this match nodes panel");
        closeButton.addActionListener(new ClosePanelAction(this));
        panel.add(closeButton);
        return panel;
    }   
    
    public String getPanelTitle() 
    {
        return resultTitle;
    }
    public void setPanelTitle(String title) 
    {
        resultTitle = title;
    }
   
    public String getResultTitle() {
        return resultTitle;
    }
    public void setResultTitle(String title) {
        resultTitle = title;
    }
    /**
     * Handles the data to be displayed 
     * methods need to be implements
     *   public int getRowCount();
     *   public int getColumnCount();
     *   public Object getValueAt(int row, int column);
     */
    private class MatchNodesTableModel extends AbstractTableModel 
    {
        //Create column headings
        String[] columnNames = {"LNetwork Nodes", "TNetwork Nodes"};
        Object[][] data;    //the actual table data
        ArrayList<CyNode> matchNodesTableModel;
        public MatchNodesTableModel(ArrayList<CyNode> matchNodesTableModel) 
        {
               this.matchNodesTableModel=matchNodesTableModel;
               matchNodesList(); 
        }
        
        @SuppressWarnings("empty-statement")
        public void matchNodesList()
        {
            int matchNodesCount=matchNodesTableModel.size();
            data=new Object[matchNodesCount/2][2];
            for (int i = 0; i < matchNodesCount; i=i+2) 
            {
                data[i/2][0]=matchNodesTableModel.get(i+1).getIdentifier();//large network
                data[i/2][1]=matchNodesTableModel.get(i).getIdentifier();;//target network
            }          
        }
        
         public String getColumnName(int col) 
        {
            return columnNames[col];
        }

        public int getRowCount() 
        {
            return data.length;
        }

        public int getColumnCount() 
        {
            return columnNames.length;
        }

        public Object getValueAt(int row, int col) 
        {
            return data[row][col];
        }

        public void setValueAt(Object object, int row, int col) 
        {
            data[row][col] = object;
            fireTableCellUpdated(row, col);
        }

        public Class getColumnClass(int c) 
        {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) 
        {
            return false;
        }
        
    }
    
    
    private class MatchEdgesTableModel extends AbstractTableModel 
    {
        //Create column headings
        String[] columnNames = {"LNetwork Edges", "TNetwork Edges"};
        Object[][] data;    //the actual table data
        ArrayList<CyEdge> matchEdgesTableModel;
        public MatchEdgesTableModel(ArrayList<CyEdge> matchEdgesTableModel) 
        {
               this.matchEdgesTableModel=matchEdgesTableModel;
               matchEdgesList(); 
        }
        
        @SuppressWarnings("empty-statement")
        public void matchEdgesList()
        {
            int matchEdgesCount=matchEdgesTableModel.size();
            data=new Object[matchEdgesCount/2][2];
            for (int i = 0; i < matchEdgesCount; i=i+2) 
            {
                data[i/2][0]=matchEdgesTableModel.get(i+1).getIdentifier();//large network
                data[i/2][1]=matchEdgesTableModel.get(i).getIdentifier();;//target network
            }          
        }
        
         public String getColumnName(int col) 
        {
            return columnNames[col];
        }

        public int getRowCount() 
        {
            return data.length;
        }

        public int getColumnCount() 
        {
            return columnNames.length;
        }

        public Object getValueAt(int row, int col) 
        {
            return data[row][col];
        }

        public void setValueAt(Object object, int row, int col) 
        {
            data[row][col] = object;
            fireTableCellUpdated(row, col);
        }

        public Class getColumnClass(int c) 
        {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) 
        {
            return false;
        }
        
    }

private class NodesOrEdgesPanelAction extends AbstractAction
{
        public void actionPerformed(ActionEvent e)
        {
           if("1" ==e.getActionCommand())
           {
               type=1;
               edgesPanel.setVisible(false);
               nodesPanel.setVisible(true);               
           }
           if("2" ==e.getActionCommand())
           {
               type=2;
               nodesPanel.setVisible(false);
               edgesPanel.setVisible(true);
           }
        }
    
}
    
      /**
     * Handles the close press for this results panel
     */
    private class ClosePanelAction extends AbstractAction 
    {
        BNMatchMatchResultPanel trigger;
        ClosePanelAction(BNMatchMatchResultPanel trigger) 
        {
            this.trigger = trigger;
        }
        public void actionPerformed(ActionEvent e) 
        {
            CytoscapeDesktop desktop = Cytoscape.getDesktop();
            CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.EAST);
            String message = "Confirm to close the " + resultTitle + "?";
            int result = JOptionPane.showOptionDialog(Cytoscape.getDesktop(), 
                    new Object[]{message}, "Confirm", JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (result == JOptionPane.YES_OPTION) 
            {
                cytoPanel.remove(trigger);
                ParameterSet.removeResultParams(trigger.getPanelTitle());
            }
            if (cytoPanel.getCytoPanelComponentCount() == 0) 
            {
                cytoPanel.setState(CytoPanelState.HIDE);
            }
        }
    }
    
     /**
     * Handler to select nodes  when a row(rows) is selected
     */
    private class TableRowSelectionAction implements ListSelectionListener 
    {
        JTable table;
        int[] rows;
        TableRowSelectionAction(JTable table)
        {
            this.table=table;
        }
        public void valueChanged(ListSelectionEvent e) 
        {
           // JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "11");
            if (e.getValueIsAdjusting()) return;          
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            
            if (!lsm.isSelectionEmpty()) 
            {  
                rows=table.getSelectedRows();
                if(1==type)
                selectMatchNodes(rows);
                if(2==type)
                selectMatchEdges(rows);
            }
        }
    }
    
        /**
     * Selects a cluster in the view that is selected by the user in the browser table
     *
     * @param gpCluster Cluster to be selected
     */
    public void selectMatchNodes(int[] indices) 
    {           
            if (largeNetwork!= null && targetNetwork!=null) 
            {  
              if(largeNetworkView==null)
               largeNetworkView=Cytoscape.createNetworkView(largeNetwork);
              if(targetNetworkView==null)
               targetNetworkView=Cytoscape.createNetworkView(targetNetwork);
                
               // System.err.println(largeNetwork);
               // System.err.println(largeNetworkView);
                GinyUtils.deselectAllNodes(largeNetworkView);
                GinyUtils.deselectAllNodes(targetNetworkView);
                
                for(int i:indices)
                {
                largeNetwork.setSelectedNodeState(matchNodes.get(2*i+1),true);
                targetNetwork.setSelectedNodeState(matchNodes.get(2*i),true);
                }

                if(Cytoscape.getDesktop().getCytoPanel(SwingConstants.EAST).getState() == CytoPanelState.DOCK) 
                {
                    Cytoscape.getDesktop().setFocus(largeNetworkView.getIdentifier());
                    Cytoscape.getDesktop().setFocus(targetNetworkView.getIdentifier());
                }
            } 
            else 
            {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                                "There is no network to select nodes.", "", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    
    public void selectMatchEdges(int[] indices) 
    {           
            if (largeNetwork!= null && targetNetwork!=null) 
            {  
              if(largeNetworkView==null)
               largeNetworkView=Cytoscape.createNetworkView(largeNetwork);
              if(targetNetworkView==null)
               targetNetworkView=Cytoscape.createNetworkView(targetNetwork);
                
                GinyUtils.deselectAllEdges(largeNetworkView);
                GinyUtils.deselectAllEdges(targetNetworkView);
                
                for(int i:indices)
                {
                largeNetwork.setSelectedEdgeState(matchEdges.get(2*i+1),true);
                targetNetwork.setSelectedEdgeState(matchEdges.get(2*i),true);
                }

                if(Cytoscape.getDesktop().getCytoPanel(SwingConstants.EAST).getState() == CytoPanelState.DOCK) 
                {
                    Cytoscape.getDesktop().setFocus(largeNetworkView.getIdentifier());
                    Cytoscape.getDesktop().setFocus(targetNetworkView.getIdentifier());
                }
            } 
            else 
            {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                                "There is no network to select edges.", "", JOptionPane.INFORMATION_MESSAGE);
            }
        }    
}
