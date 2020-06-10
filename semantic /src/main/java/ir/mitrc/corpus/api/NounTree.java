package ir.mitrc.corpus.api;


import java.awt.BorderLayout;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public class NounTree extends JFrame {
	TreeModel tree;
	private JTree jtree;
	InputStream ontPath=this.getClass().getClassLoader().getResourceAsStream("root-ontology.owl");
	ApiFactory wnApi=new ApiFactory(ontPath);
	ArrayList<String> loopArray=new ArrayList<String>();
	
	
	public NounTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
      
         
        Isynset topSynset=ApiFactory.getSynset("http://www.mitrc.ir/mobina#«هستینه-0،...»");
        DefaultMutableTreeNode topNode = new DefaultMutableTreeNode(topSynset.getLabel()/*+" : "+topSynset.getGloss()*/);

        root.add(topNode);
		

		List<String> nounSynsets=wnApi.listAllNounSynsets();		
		
		for (String nounUri:nounSynsets){
			Isynset synset=ApiFactory.getSynset(nounUri);
			addNode(root,synset);
		}
		
		jtree = new JTree(root);
        add(jtree);
        this.add(new JScrollPane(jtree), BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("JTree Example");        
        //this.pack();
        this.setVisible(true);		
		
	
	}
	private DefaultMutableTreeNode addNode(DefaultMutableTreeNode root,Isynset synset) {
		String nodeString=synset.getLabel()/*+" : "+synset.getGloss()*/;
		DefaultMutableTreeNode node=new DefaultMutableTreeNode(nodeString);
		ArrayList<String> allFathersSynsets=synset.getRelatedSynset(NounRelationType.HypernymOrInstanceOf);
		boolean flag=false;
		if (allFathersSynsets==null ){
			root.add(node);
			System.out.println(node);
			return node;
			
		}
			
		for(String fatherSynsetUri:allFathersSynsets){
			
			if (fatherSynsetUri==null)
				continue;
			Isynset fatherSynset=ApiFactory.getSynset(fatherSynsetUri);
			String fatherNodeString=fatherSynset.getLabel()/*+" : "+synset.getGloss()*/;
			findLoop(fatherSynsetUri,new ArrayList<String>(),0);
			DefaultMutableTreeNode fatherNode=find(root,fatherNodeString);
			if (fatherNode!=null){
				try{
					fatherNode.add(node);
					flag=true;
					
				}catch(Exception e){
					System.err.println(node);
				}
				//System.out.println(node);
			}
			else{
				try{
					addNode(root,fatherSynset).add(node);
					flag=true;
					
				}catch(Exception e){
					System.err.println(node);
				}
				

				//System.out.println(node);
			}
		}
		if (!flag)
			root.add(node);
		return node;
	}
	private boolean findLoop(String fatherSynsetUri,ArrayList<String> babaArrayList, int i) {
		
		i++;
		Isynset synset=ApiFactory.getSynset(fatherSynsetUri);
		//String fatherNodeString=synset.getLabel()/*+" : "+synset.getGloss()*/;
		ArrayList<String> allFathersSynsets=synset.getRelatedSynset(NounRelationType.Hypernym);
		if (i>60)
			return true;
		boolean result=false;
		if (allFathersSynsets==null)
			return false;
		for(String synsetUri:allFathersSynsets){
			ArrayList<String> tempArray=(ArrayList<String>) babaArrayList.clone();
			if (synsetUri==null)
				continue;
			if (babaArrayList.contains(synsetUri)){
				System.out.println("LOOOOOP: "+synsetUri);
				loopArray.add(synsetUri);
				return true;
			}
			else{
				tempArray.add(synsetUri);
				if (result)
					return true;
				if (i>60)
					return true;
				if (!loopArray.contains(synsetUri))
					result=result || findLoop(synsetUri,tempArray,i);
				else {
					System.err.println("loopArra: "+synsetUri);
				}
				
			}
		}
		return result;
	}
	
	private DefaultMutableTreeNode find(DefaultMutableTreeNode root, String s) {
	    @SuppressWarnings("unchecked")
		Enumeration<TreeNode> e = root.depthFirstEnumeration();
	    while (e.hasMoreElements()) {
	        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
	        if (node.toString().equalsIgnoreCase(s)) {
	            return node;
	        }
	    }
	    return null;
	}
	
	public static void main(String [] args){
		NounTree nTree=new NounTree();
	}
}
