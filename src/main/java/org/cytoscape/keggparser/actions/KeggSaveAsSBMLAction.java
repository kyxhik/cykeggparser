package org.cytoscape.keggparser.actions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.keggparser.KEGGParserPlugin;
import org.cytoscape.keggparser.com.ParsingReportGenerator;
import org.cytoscape.keggparser.dialogs.KeggSaveDialog;
import org.cytoscape.keggparser.parsing.KGMLConverter;
import org.cytoscape.keggparser.parsing.KGMLCreator;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class KeggSaveAsSBMLAction extends AbstractCyAction {
	    private int SBMLLevel;
	    private KeggSaveDialog saveDialog;
	    private File outFile;
	    private String menuName;
	    private String suffix = "";

	    String getSuffix() {
	        return suffix;
	    }
	    
	    public KeggSaveAsSBMLAction(int SBMLLevel, String menuName) {
	        super(menuName == null ? "" : menuName);
	        setPreferredMenu("Apps.KEGGParser.Save network");

	        if (menuName == null)
	            menuName = "";
	        this.menuName = menuName;
	        this.SBMLLevel = SBMLLevel;
	        suffix = ".sbml";
	    }

		int getSBML() {
			return SBMLLevel;
		}


		KeggSaveDialog getSaveDialog() {
			return saveDialog;
		}


		public File getOutFile() {
			
			return outFile;
			
		}
		
		public void actionPerformed(ActionEvent e) {
			
			
			outFile = getSelectedFileFromSaveDialog();
			
			
	        if (outFile != null) {
	        	
		        
	            writeOutFileDirectory();
	            // Create Task
	            final SaveSBMLTask task = new SaveSBMLTask(outFile.getAbsolutePath());
	            KEGGParserPlugin.taskManager.execute(new TaskIterator(task));
	        }
	    }
		
		private void writeOutFileDirectory() {
			
			if (outFile != null) {
	            try {
	            	
	                PrintWriter recentDirWriter = new PrintWriter(saveDialog.getRecentDir());
	                recentDirWriter.write(outFile.getParent());
	                recentDirWriter.close();
	            } catch (FileNotFoundException e1) {
	                LoggerFactory.getLogger(KeggSaveAsSBMLAction.class).error(e1.getMessage());
	            }
	        }
	    }
	    
		private File getSelectedFileFromSaveDialog() {
	        
	            saveDialog = new KeggSaveDialog(".sbml");
	            
	            
	        int response = saveDialog.showSaveDialog(KEGGParserPlugin.cytoscapeDesktopService.getJFrame());
	        if (response == KeggSaveDialog.CANCEL_OPTION)
	            return null;
	        
			  
	        return saveDialog.getSelectedFile();
	    }

		String getMenuName() {
	        return menuName;
	    }

		public class SaveSBMLTask extends AbstractTask {

	        private String outFilePath;
	        private TaskMonitor taskMonitor;

	        public SaveSBMLTask(String outFilePath) {
	            this.outFilePath= outFilePath;
	            
	            super.cancelled = false;
	        }
		
		
	        @Override
	        public void run(TaskMonitor taskMonitor) throws Exception {
	            this.taskMonitor = taskMonitor;
	            taskMonitor.setTitle("Kegg saving task");
	            taskMonitor.setStatusMessage("Saving KGML file.\n\nIt may take a while.\nPlease wait...");
	            ParsingReportGenerator.getInstance().appendLine("Saving the network as SBML File to " +
	                    outFilePath);
	            taskMonitor.setProgress(0);
		
	            try {
	                File existingFile = new File(outFilePath);
	                if ((existingFile).exists())
	                    existingFile.delete();
	                File kgmlFile = createKGMLFile(new File(outFilePath));
	                taskMonitor.setStatusMessage("Converting KGML file " + kgmlFile.getAbsolutePath()
	                        + " to " + suffix + " file");
	                KGMLConverter kgmlConverter = new KGMLConverter();
	                
	                kgmlConverter.translateFromCmdtoSBML(kgmlFile, outFilePath,
	                        SBMLLevel, taskMonitor, this);

	                if (outFile.exists() && outFile.length() >0) {
	                    String successMessage = "SBML File " + outFilePath + " successfully generated.";
	                    taskMonitor.setStatusMessage(successMessage);
	                    ParsingReportGenerator.getInstance().appendLine(successMessage);
	                    taskMonitor.setProgress(1);
	                } else {
	                    ParsingReportGenerator.getInstance().appendLine("Problem saving network as " + suffix);
	                    if (outFile.getName().contains("-"));
	                    
	                    
	                    throw new Exception("The network name contains an invalid character \"-\"");
	                }
	            } catch (Exception e) {
	                throw new Exception("Error while saving KGML " + e.getMessage());
	            } finally {
	                System.gc();
	            }
	        }
		
	        private File createKGMLFile(File outFile) throws Exception {
	            File kgmlFile = getKGMLFile(outFile);
	            KGMLCreator kgmlCreator = new KGMLCreator();
	            kgmlCreator.setFilterForConversion(KGMLConverter.SBML);
	            kgmlCreator.createKGML(KEGGParserPlugin.cyApplicationManager.getCurrentNetwork(), kgmlFile);
	            taskMonitor.setStatusMessage("KGML file " + kgmlFile.getAbsolutePath() + " successfully generated.");
	            ParsingReportGenerator.getInstance().appendLine("KGML file saved to " + kgmlFile.getAbsolutePath());
	            return kgmlFile;
	        }

	        @Override
	        public void cancel() {

	            LoggerFactory.getLogger(KeggSaveAsSBMLAction.class).info("Cancel called!!!");
	            taskMonitor.setProgress(1);
	            super.cancelled = true;
	            System.gc();

	        }
	        public boolean isCancelled() {
	            return cancelled;
	        }
	    }
		
		private File getKGMLFile(File outFile) {
	        String kgmlFileName;
	        if (outFile.getName().contains(suffix))
	            kgmlFileName = outFile.getName().replace(suffix, ".xml");
	        else
	            kgmlFileName = outFile.getName() + ".xml";
	        
	        
	      //  if (outFile.getName().contains(" "))
	        //	kgmlFileName = outFile.getName().replace(" ", "_");
	        
	        File kgmlDir = new File(KEGGParserPlugin.getKEGGParserDir(), "/kgml");
	        if (!kgmlDir.exists())
	            if (!kgmlDir.mkdir()) {
	                LoggerFactory.getLogger(KeggSaveAsSBMLAction.class).
	                        error("Error creating directory " + kgmlDir.getAbsolutePath());
	                kgmlDir = KEGGParserPlugin.getKEGGParserDir();
	            }
	        return new File(kgmlDir, kgmlFileName);
	    }
	        
	        
	        
}
