package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Content;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class UsBotanic implements Converter{

	private static final String title = "UsBotanic";
	
	protected Logger logger;
	
	private String latinName;
	private String variety;
	
	public UsBotanic(Logger logger) {
		this.logger = logger;
	}
	
	public UsBotanic(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("latin-name")) {
					latinName = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("variety")) {
					variety = childElement.getTextContent();
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getNodeName(), title);
				}
			}
			else if (node.getNodeType() == Node.TEXT_NODE) {
				//ignore
			}
			else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
				//ignore
			}
			else
			{
				logger.warn("Unknown Node {} in {} node", node.getNodeName(), title);
			}
		}
	}

	public UsBotanic(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Content> nodes = element.getContent();
		for (int i=0; i < nodes.size(); i++)
		{
			Content node = nodes.get(i);
			if (node.getCType() == Content.CType.Element) {
				org.jdom2.Element childElement = (org.jdom2.Element) node;
				if (childElement.getName().equals("latin-name")) {
					latinName = childElement.getValue();
				}
				else if (childElement.getName().equals("variety")) {
					variety = childElement.getValue();
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getName(), title);
				}
			}
			else if (node.getCType() == Content.CType.Text) {
				//ignore
			}
			else if (node.getCType() == Content.CType.ProcessingInstruction) {
				//ignore
			}
			else
			{
				logger.warn("Unknown Node {} in {} node", node.getCType(), title);
			}
		}
	}

	public String getLatinName() {
		return latinName;
	}

	public String getVariety() {
		return variety;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (latinName != null)
		{
			toStringBuffer.append(" LatinName: ");
			toStringBuffer.append(latinName);
		}
		if (variety != null)
		{
			toStringBuffer.append(" Variety: ");
			toStringBuffer.append(variety);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (latinName != null)
		{
			jsonObject.put("LatinName", latinName);
		}
		if (variety != null)
		{
			jsonObject.put("Variety", variety);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (latinName != null)
		{
			basicDBObject.put("LatinName", latinName);
		}
		if (variety != null)
		{
			basicDBObject.put("Variety", variety);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
