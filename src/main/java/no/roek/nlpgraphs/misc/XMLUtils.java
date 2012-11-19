package no.roek.nlpgraphs.misc;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.detailedretrieval.PlagiarismReference;
import no.roek.nlpgraphs.graph.Graph;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

public class XMLUtils {

	public static List<PlagiarismReference> getPlagiarismReferences(String annotationFile) {
		Fileutils.createParentFolders(annotationFile);
		List<PlagiarismReference> plagiarisms = new ArrayList<>();

		SAXBuilder builder = new SAXBuilder();
		InputStream is;
		try {
			is = new FileInputStream(annotationFile);
			Document document = builder.build(is);
			Element root = document.getRootElement();
			String filename = root.getAttribute("reference").getValue();

			List<Element> elements = root.getChildren("feature");
			for (int i = 0; i < elements.size(); i++) {
				Element row = elements.get(i);
				String plagType = row.getAttribute("name").getValue();
				if(plagType.equals("plagiarism") || plagType.equals("detected-plagiarism") || plagType.equals("simulated")) {
					String type = row.getAttributeValue("type");
					String obfuscation = row.getAttributeValue("obfuscation");
					String language = row.getAttributeValue("this_language");
					String offset = row.getAttributeValue("this_offset");
					String length = row.getAttributeValue("this_length");
					String sourceReference = row.getAttributeValue("source_reference");
					String sourceLanguage = row.getAttributeValue("source_language");
					String sourceOffset = row.getAttributeValue("source_offset");
					String sourceLength = row.getAttributeValue("source_length");

					plagiarisms.add(new PlagiarismReference(filename, plagType, type, obfuscation, language, offset, length, sourceReference, sourceLanguage, sourceOffset, sourceLength));
				}
			}

		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		return plagiarisms;
	}
	
	public static PlagiarismReference getPlagiarismReference(Graph train, Graph test, double similarity, boolean detectedPlagiarism) {
		String filename = test.getFilename();
		String offset = Integer.toString(test.getOffset());
		String length = Integer.toString(test.getLength());
		String sourceReference = train.getFilename();
		String sourceOffset = Integer.toString(train.getOffset());
		String sourceLength = Integer.toString(train.getLength());
		String name = detectedPlagiarism ? "detected-plagiarism" : "candidate-passage";
		return new PlagiarismReference(filename, name, offset, length, sourceReference, sourceOffset, sourceLength, similarity);
	}
	
	public static void writeResults(String dir, String file, List<PlagiarismReference> plagiarisms) {
		Element root = new Element("document");
		root.setAttribute("reference", file);
		for (PlagiarismReference plagiarismReference : plagiarisms) {
			Element reference = new Element("feature");
			reference.setAttribute("name", plagiarismReference.getName());
			reference.setAttribute("this_offset", plagiarismReference.getOffset());
			reference.setAttribute("this_length", plagiarismReference.getLength());
			reference.setAttribute("obfuscation", Double.toString(plagiarismReference.getSimilarity()));
			reference.setAttribute("source_reference", plagiarismReference.getSourceReference());
			reference.setAttribute("source_offset", plagiarismReference.getSourceOffset());
			reference.setAttribute("source_length", plagiarismReference.getSourceLength());
			root.addContent(reference);
		}

		Document doc = new Document();
		doc.setContent(root);

		XMLOutputter outputter = new XMLOutputter();

		FileWriter writer = null;
		try {
			writer = new FileWriter(dir+Fileutils.replaceFileExtention(file, "xml"));
			outputter.output(doc, writer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
}


