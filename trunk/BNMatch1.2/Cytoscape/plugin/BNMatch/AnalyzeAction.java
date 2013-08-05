package Cytoscape.plugin.BNMatch;

/**
 *
 * @author YULEI
 */
import Cytoscape.plugin.BNMatch.INM.Config;
import Cytoscape.plugin.BNMatch.INM.INM;
import Cytoscape.plugin.BNMatch.INM.INMAlgorithm;
import cytoscape.CyEdge;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.readers.GraphReader;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.visual.VisualPropertyType;

import ding.view.DGraphView;
import giny.model.GraphPerspective;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import com.lowagie.text.pdf.hyphenation.TernaryTree.Iterator;

public class AnalyzeAction implements ActionListener {
	static ArrayList<CyNode> matchNodes;
	static ArrayList<CyEdge> matchEdges;
	ArrayList<CyNode> targetAL;
	ArrayList<CyNode> largeAL;
	ArrayList<CyNode> targetTotalNodes;
	ArrayList<CyNode> largeTotalNodes;
	ArrayList<CyEdge> targetEAL;
	ArrayList<CyEdge> largeEAL;
	int matchNodesCount;
	int matchEdgesCount;
	CyNetwork largeNetwork;
	CyNetwork targetNetwork;
	CyNetwork totalNetwork;
	
	DGraphView largeView;
	DGraphView targetView;
	DGraphView totalView;
	
	int resultIndex = 0;
	static int matchNodesPanelNO = 1;
	String panelTitle;
	BNMatchNetworks BNMNetworks;
	CyNetwork cyNetwork;
	String networkIdentifier;
	Boolean show_common=true;
	AnalyzeAction() {
	}

	public void actionPerformed(ActionEvent e) {
		MainPanel.runInformation.append("Starting analysis\n");
		if (MainPanel.extenalSIFFile.isSelected())// analyze network from
													// existing file
		{
			MainPanel.runInformation.append("Using sif file\n");
			cyNetwork = Cytoscape.getCurrentNetwork();
			networkIdentifier = cyNetwork.getIdentifier();
			if (cyNetwork == null) {
				System.err.println("Can't get a network.");
				return;
			}
			if (cyNetwork.getNodeCount() < 1) {
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
						"Network has not been loaded!", "Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (Cytoscape.getDesktop().getNetworkPanel()
					.getNetworkNode(networkIdentifier).getLevel() != 1) {
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
						"Please select the correct network!");
				return;
			}
		} else {
			MainPanel.runInformation.append("Using source file\n");
			if (MainPanel.str1 == null || MainPanel.str2 == null
					|| MainPanel.str3 == null || MainPanel.str4 == null) {
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
						"Please input the file!");
				return;
			}

			if (MainPanel.weightField.getText().isEmpty()) {
				MainPanel.weightField.setText("0.1");
			}

			Config.setWeight(Double.parseDouble(MainPanel.weightField.getText()));
			INM.runINM();
			MainPanel.runInformation.append("Reading networks...\n");
			GraphReader reader = Cytoscape.getImportHandler().getReader(
					INMAlgorithm.fileAbsolutePath);
			cyNetwork = Cytoscape.createNetwork(reader, true, null);
			Cytoscape.createNetworkView(cyNetwork);
			networkIdentifier = INMAlgorithm.fileName;
		}

		BNMNetworks = new BNMatchNetworks(cyNetwork);// initialize matchNodes of
														// BNMatchNetworks
		if (BNMNetworks.gpClique == null) {
			return;// immediately return
		}
		BNMatchAnalyzeTask task = new BNMatchAnalyzeTask(cyNetwork,
				networkIdentifier);
		JTaskConfig config = new JTaskConfig();
		// config.displayCancelButton(true);
		config.displayStatus(true);
		// Execute Task via TaskManager
		// This automatically pops-open a JTask Dialog Box
		MainPanel.runInformation.append("starting thread..\n");
		TaskManager.executeTask(task, config);
		MainPanel.runInformation.append("starting thread finish....\n");
		while(!task.isCompletedSuccessfully)
		{
			;
		}
		if (task.isCompletedSuccessfully == true) {
			MainPanel.colorWeight.setEnabled(true);
			MainPanel.resetButton.setEnabled(true);
			MainPanel.refreshImmediately.setEnabled(true);
			MainPanel.refreshButton.setEnabled(true);
            int steps=10000;
			MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
			createMatchNodesPanel(matchNodes, matchEdges, largeNetwork,
					targetNetwork);
			MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
			applyVisualMappingBypass(largeNetwork, targetNetwork, matchNodes,
					largeAL, targetAL, matchEdges, largeEAL, targetEAL, show_common);
			MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
			Map<CyNode,CyNode> matchedNodes = new HashMap<CyNode,CyNode>();
			Map<CyEdge,CyEdge> matchedEdges = new HashMap<CyEdge,CyEdge>();
			for(int index=0;index<matchNodes.size();index+=2)
			{
				matchedNodes.put(matchNodes.get(index), matchNodes.get(index+1));
			}
			for(int index=0;index<matchEdges.size();index+=2)
			{
				matchedEdges.put(matchEdges.get(index), matchEdges.get(index+1));
			}
			MainPanel.runInformation.append(String.format("Step %d...\n",++steps));
			applyTotalVisualMappingBypass(totalNetwork,matchedNodes,matchedEdges);
			MainPanel.runInformation.append("Complete successfully!\n");
			JOptionPane.showMessageDialog(Cytoscape.getDesktop(), ""
					+ matchNodesCount + " pairs match nodes," + matchEdgesCount
					+ " pairs match edges have been found.", "Completed",
					JOptionPane.INFORMATION_MESSAGE);
		}

	}
	public void changeVisualState()
	{
		show_common=!show_common;
		applyVisualMappingBypass(largeNetwork, targetNetwork, matchNodes,
				largeAL, targetAL, matchEdges, largeEAL, targetEAL, show_common);
		MainPanel.runInformation.append("Change State Finished!\n");
	}
	public void createMatchNodesPanel(ArrayList<CyNode> matchNodes,
			ArrayList<CyEdge> matchEdges, CyNetwork lNetwork, CyNetwork tNetwork) {
		panelTitle = "result " + matchNodesPanelNO;
		MainPanel.runInformation.append("Create Pannel..."+panelTitle+"...\n");
		BNMatchMatchResultPanel matchResultPanel = new BNMatchMatchResultPanel(
				matchNodes, matchEdges, lNetwork, tNetwork);// create match
															// nodes table panel
		matchResultPanel.setPanelTitle(panelTitle);
		CytoPanel cytoPanel = Cytoscape.getDesktop().getCytoPanel(
				SwingConstants.EAST);
		cytoPanel.add(panelTitle, matchResultPanel);
		matchNodesPanelNO++;
		resultIndex = cytoPanel.indexOfComponent(matchResultPanel);
		cytoPanel.setSelectedIndex(resultIndex);
		cytoPanel.setState(CytoPanelState.DOCK);
	}

	public void applyVisualMappingBypass(CyNetwork ln, CyNetwork tn,
			ArrayList<CyNode> mnAL, ArrayList<CyNode> lnAL,
			ArrayList<CyNode> tnAL, ArrayList<CyEdge> meAL,
			ArrayList<CyEdge> leAL, ArrayList<CyEdge> teAL,Boolean ShowCommon) {
		
		
		setNodeFillColor(tn, tnAL);
		setNodeFillColor(ln, lnAL);

		setNodeShape(tn, targetTotalNodes,tnAL,ShowCommon);
		setNodeShape(ln, largeTotalNodes,lnAL,ShowCommon);

		setEdgeColor(tn, teAL);
		setEdgeColor(ln, leAL);

		setEdgeWidth(meAL);

		Cytoscape.getNetworkView(tn.getIdentifier()).redrawGraph(false, true);
		Cytoscape.getNetworkView(ln.getIdentifier()).redrawGraph(false, true);
	}

	public void applyTotalVisualMappingBypass(CyNetwork totalNetwork,
			Map<CyNode,CyNode> matchNodes, Map<CyEdge,CyEdge> matchEdges) {
		BNMatchVizMapperUtils.UpdateNetwork(totalNetwork, matchNodes,  matchEdges);
		

		
	}
	/**
	 * Sets the fill color for the provided nodes. default way to show node
	 * color WARNING: this overwrites the vizmapper settings!
	 * 
	 * @param network
	 *            network where the nodes should be changed.
	 * @param nodesAL
	 *            ids of the nodes.
	 * @return True when the operation succeeds.
	 */
	public Boolean setNodeFillColor(CyNetwork network, ArrayList<CyNode> nodesAL) {
		return BNMatchVizMapperUtils.setNodeColor(network, nodesAL,
				VisualPropertyType.NODE_FILL_COLOR);
	}

	public Boolean setNodeShape(CyNetwork network, ArrayList<CyNode> totalNodes,ArrayList<CyNode> nodesAL,Boolean ShowCommon) {
		return BNMatchVizMapperUtils.setNodeShape(network, totalNodes,nodesAL,ShowCommon);
	}

	public void setNodeSize() {

	}

	/**
	 * Sets the fill color for the provided edges. WARNING: this overwrites the
	 * vizmapper settings!
	 * 
	 * @param network
	 *            network where the edges should be changed.
	 * @param edgesAL
	 * @return True when the operation succeeds.
	 */
	public Boolean setEdgeColor(CyNetwork network, ArrayList<CyEdge> edgesAL) {
		return BNMatchVizMapperUtils.setEdgeColor(network, edgesAL,
				VisualPropertyType.EDGE_COLOR);
	}

	public void setEdgeWidth(ArrayList<CyEdge> matchEdgesAL) {
		int count = matchEdgesAL.size();
		for (int i = 0; i < count; i = i + 2) {
			BNMatchVizMapperUtils.setEdgeProperty(matchEdgesAL.get(i)
					.getIdentifier(), "Edge Line Width", "10");
			BNMatchVizMapperUtils.setEdgeProperty(matchEdgesAL.get(i + 1)
					.getIdentifier(), "Edge Line Width", "10");
		}
	}

	class BNMatchAnalyzeTask implements Task {
		private TaskMonitor taskMonitor;
		private String str;
		private CyNetwork cyNetwork;
		private boolean isCompletedSuccessfully = false;

		public BNMatchAnalyzeTask(CyNetwork cyNetwork, String str) {
			this.str = str;
			this.cyNetwork = cyNetwork;
		}

		public void setTaskMonitor(TaskMonitor taskMonitor)
				throws IllegalThreadStateException {
			if (this.taskMonitor != null) {
				throw new IllegalStateException(
						"Task Monitor has already been set.");
			}
			this.taskMonitor = taskMonitor;
		}

		public void halt() {
		}

		public String getTitle() {
			return "Create Networks";
		}

		public void run() {
			final GraphPerspective largeGP = BNMNetworks.largeNetworkGP();
			final GraphPerspective targetGP = BNMNetworks.targetNetworkGP();
			final GraphPerspective totalGP = BNMNetworks.totalNetworkGP();
			
			
			final ArrayList arrMatchNodes = BNMNetworks.matchNodes;// match
																	// nodes

			matchNodes = arrMatchNodes;
			BNMatchDataStructure BNMDS = new BNMatchDataStructure(arrMatchNodes);// just
																					// pass
																					// the
																					// match
																					// nodes
																					// to
																					// BNMatchDataStructure
																					// class
			targetTotalNodes=BNMNetworks.targetTotalNodes;
			largeTotalNodes=BNMNetworks.largeTotalNodes;
			targetAL = BNMatchDataStructure.createTargetNetworkMatchNodes();
			largeAL = BNMatchDataStructure.createLargeNetworkMatchNodes();
			targetEAL = BNMatchDataStructure
					.createTargetNetworkMatchEdges(targetGP);
			largeEAL = BNMatchDataStructure
					.createLargeNetworkMatchEdges(largeGP);
			matchEdges = BNMatchDataStructure.createMatchEdges(largeGP,
					targetGP, MainPanel.directedGraph.isSelected());
			matchEdgesCount = matchEdges.size() / 2;

			taskMonitor.setPercentCompleted(0);
			taskMonitor.setStatus("Generating Networks...");
			MainPanel.runInformation.append("Generating Networks...\n");
			int []lNodeIndexs=largeGP.getNodeIndicesArray();
			int []lEdgeIndexs=largeGP.getEdgeIndicesArray();
			largeNetwork = Cytoscape.createNetwork(
					lNodeIndexs,
					lEdgeIndexs, str + "_Large Network "
							+ matchNodesPanelNO + ".sif", cyNetwork);
			
			int []tNodeIndexs=targetGP.getNodeIndicesArray();
			int []tEdgeIndexs=targetGP.getEdgeIndicesArray();
			targetNetwork = Cytoscape.createNetwork(
					tNodeIndexs,
					tEdgeIndexs, str + "_Target Network "
							+ matchNodesPanelNO + ".sif", cyNetwork);

			int []totalNodeIndices=totalGP.getNodeIndicesArray();
			int []totalEdgeIndices=totalGP.getEdgeIndicesArray();
			/*int curtotalNodeIndices=0;
			int curtotalEdgeIndices=0;
			for(int i=0;i<lNodeIndexs.length;i++)
			{
				totalNodeIndices[curtotalNodeIndices]=lNodeIndexs[i];
				curtotalNodeIndices++;
			}
			for(int i=0;i<tNodeIndexs.length;i++)
			{
				totalNodeIndices[curtotalNodeIndices]=tNodeIndexs[i];
				curtotalNodeIndices++;
			}
			
			for(int i=0;i<lEdgeIndexs.length;i++)
			{
				totalEdgeIndices[curtotalEdgeIndices]=lEdgeIndexs[i];
				curtotalEdgeIndices++;
			}
			for(int i=0;i<tEdgeIndexs.length;i++)
			{
				totalEdgeIndices[curtotalEdgeIndices]=tEdgeIndexs[i];
				curtotalEdgeIndices++;
			}
			*/
			totalNetwork=Cytoscape.createNetwork(
					totalNodeIndices,
					totalEdgeIndices, "match_result", cyNetwork);

			largeView = (DGraphView) Cytoscape.createNetworkView(largeNetwork);
			targetView = (DGraphView) Cytoscape.createNetworkView(targetNetwork);
			totalView=(DGraphView) Cytoscape.createNetworkView(totalNetwork);
			
			matchNodesCount = arrMatchNodes.size() / 2;

			BNMatchLayout largeLayout = new BNMatchLayout(largeView);
			BNMatchLayout targetLayout = new BNMatchLayout(targetView);
			BNMatchLayout totalLayout = new BNMatchLayout(totalView);
			
			taskMonitor.setPercentCompleted(10);
			taskMonitor.setStatus("Layouting large network...");
			MainPanel.runInformation.append("Layouting large network...\n");
			largeLayout.doLayout(0, 0, 0, null);

			taskMonitor.setPercentCompleted(50);
			taskMonitor.setStatus("Layouting target network...");
			MainPanel.runInformation.append("Layouting target network...\n");
			targetLayout.doLayout(0, 0, 0, null);
			largeView.fitContent();// Fits all Viewable elements onto the Graph

			taskMonitor.setPercentCompleted(70);
			taskMonitor.setStatus("Layouting total network...");
			MainPanel.runInformation.append("Layouting total network...\n");
			totalLayout.doLayout(0, 0, 0, null);
			
			
			taskMonitor.setPercentCompleted(80);
			taskMonitor.setStatus("Relayouting target network... ");
			
			MainPanel.runInformation.append("Relayouting target network...\n");
			
			for (int j = 0; j < matchNodesCount; j++) {
				
				targetView.getNodeView(targetAL.get(j).getRootGraphIndex())
						.setXPosition(
								largeView.getNodeView(
										largeAL.get(j).getRootGraphIndex())
										.getXPosition());
				targetView.getNodeView(targetAL.get(j).getRootGraphIndex())
						.setYPosition(
								largeView.getNodeView(
										largeAL.get(j).getRootGraphIndex())
										.getYPosition());
				
				//taskMonitor.setPercentCompleted(80 + j / matchNodesCount * 15);
			}
			double max_x=-1;
			for(int j=0;j<matchNodes.size();j+=2)
			{
				double x=largeView.getNodeView(
						largeAL.get(j/2).getRootGraphIndex())
						.getXPosition();
				if(max_x<x)
				{
					max_x=x;
				}
				totalView.getNodeView(matchNodes.get(j).getRootGraphIndex())
				.setXPosition(
						largeView.getNodeView(
								largeAL.get(j/2).getRootGraphIndex())
								.getXPosition());
				totalView.getNodeView(matchNodes.get(j).getRootGraphIndex())
				.setYPosition(
						largeView.getNodeView(
								largeAL.get(j/2).getRootGraphIndex())
								.getYPosition());
				
			
			}
			for(int j=0;j<matchNodes.size();j+=2)
			{
				totalView.getNodeView(matchNodes.get(j+1).getRootGraphIndex())
				.setXPosition(
						largeView.getNodeView(
								largeAL.get(j/2).getRootGraphIndex())
								.getXPosition()+max_x+500);
				totalView.getNodeView(matchNodes.get(j+1).getRootGraphIndex())
				.setYPosition(
						largeView.getNodeView(
								largeAL.get(j/2).getRootGraphIndex())
								.getYPosition());
			}
			targetView.fitContent();
			totalView.fitContent();// Fits all Viewable elements onto the Graph
			
			isCompletedSuccessfully = true;
			MainPanel.runInformation.append(String.format("Relayout finish Completed:%s...\n",String.valueOf(isCompletedSuccessfully)));
			taskMonitor.setPercentCompleted(100);
			taskMonitor.setStatus(String.format("Relayout finish Completed:%s...",String.valueOf(isCompletedSuccessfully)));
		}
	}

}
