package org.openntf.xrest.xsp.exec.convertor;

import java.util.Map;
import java.util.Vector;

import org.openntf.xrest.xsp.exec.Context;
import org.openntf.xrest.xsp.model.MapJsonType;
import org.openntf.xrest.xsp.model.MappingField;
import org.openntf.xrest.xsp.model.RouteProcessor;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonObject;

import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;

public class Document2JsonConverter {

	private final Context context;
	private final RouteProcessor routeProcessor;
	private final Document doc;

	public Document2JsonConverter(Document doc, RouteProcessor routeProcessor, Context context) {
		this.context = context;
		this.routeProcessor = routeProcessor;
		this.doc = doc;
	}

	public JsonObject buildJsonFromDocument() throws NotesException {
		JsonObject jo = new JsonJavaObject();
		@SuppressWarnings("unchecked")
		Vector<Item> documentItems = doc.getItems();
		Map<String, MappingField> fieldDefinition = routeProcessor.getMappingFields();
		for (Item item : documentItems) {
			if (fieldDefinition.containsKey(item.getName().toLowerCase())) {
				processItem(jo, item, fieldDefinition.get(item.getName().toLowerCase()));
			}
		}
		for (MappingField field : routeProcessor.getFormulaFields()) {
			processFormulaToJson(jo, field, doc);
		}
		return jo;
	}

	private void processFormulaToJson(JsonObject jo, MappingField field, Document doc) throws NotesException {
		Vector<?> result = context.getSession().evaluate(field.getFormula(), doc);
		field.getType().processValuesToJsonObject(result, jo, field.getJsonName());
	}

	private void processItem(JsonObject jo, Item item, MappingField mappingField) throws NotesException {
		MapJsonType mjType = mappingField.getType();
		switch (item.getType()) {
		case Item.OTHEROBJECT:
		case Item.ATTACHMENT:
		case Item.NOTELINKS:
		case Item.SIGNATURE:
			break;
		case Item.MIME_PART:
		case Item.RICHTEXT:
			// TODO: Should we force MIME Handling.... I think so
			mjType.processItemToJsonObject(item, jo, mappingField.getJsonName());
			break;
		default:
			mjType.processItemToJsonObject(item, jo, mappingField.getJsonName());
		}
	}

}