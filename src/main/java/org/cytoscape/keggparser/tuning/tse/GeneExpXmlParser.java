package org.cytoscape.keggparser.tuning.tse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;

/**
 * A class to parse the xml formatted gene expression data from GeneCards.
 */

public class GeneExpXmlParser {

    private Document document;
    private File geneExpXml;
    private Element rootElement;

    public GeneExpXmlParser(File geneExpXml) {
        this.geneExpXml = geneExpXml;
        loadDocument();
    }


    public void loadDocument() {
        if (document == null)
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.parse(geneExpXml);
                this.rootElement = document.getDocumentElement();
            } catch (Exception e) {
//                System.out.println(e.getMessage());
            }
    }

    public Element getRootElement() {
        if (rootElement == null)
            loadDocument();
        rootElement = document.getDocumentElement();
        return rootElement;
    }

    public double getExpValue(String geneId, String tissue) {
        if (rootElement == null)
            loadDocument();
        rootElement = document.getDocumentElement();
        NodeList elements = rootElement.getElementsByTagName("geneId_" + geneId);
        if (elements.getLength() != 0) {
            Element gene = (Element) elements.item(0);
            return Double.parseDouble(gene.getAttribute(tissue));
        }
        return Double.NaN;
    }

    public HashMap<String, Double> getAllExpValues(String tissue) throws Exception {
        HashMap<String, Double> map = new HashMap<String, Double>();

        try {
            if (rootElement == null)
                loadDocument();
            rootElement = document.getDocumentElement();
            NodeList elements = rootElement.getChildNodes();
            for (int i = 0; i < elements.getLength()-1; i += 2 ) {
                Element element = (Element) elements.item(i).getNextSibling();
                String geneId = element.getNodeName().replace("geneId_", "");
                Double exp = Double.parseDouble(element.getAttribute(tissue));
                if (geneId != null && !geneId.isEmpty())
                    if (!Double.isNaN(exp))
                        map.put(geneId, exp);
            }
        } catch (NumberFormatException e) {
            throw new Exception("Error loading the dataset: " + e.getMessage());
        }
        if (map.size() != 0)
            return map;
        return null;

    }


}
