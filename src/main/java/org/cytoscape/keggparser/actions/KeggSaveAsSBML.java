package org.cytoscape.keggparser.actions;
import org.cytoscape.keggparser.parsing.KGMLConverter;


public class KeggSaveAsSBML extends KeggSaveAsSBMLAction{
	public KeggSaveAsSBML() {
		super(KGMLConverter.SBML, "Save as SBML");
		setMenuGravity(3);
        setPreferredMenu("Apps.KEGGParser.Save network");
	}
	

}
