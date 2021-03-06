package org.openntf.xrest.xsp.exec.convertor.datatypes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.ibm.commons.util.io.json.JsonObject;

import lotus.domino.DateTime;
import lotus.domino.Item;
import lotus.domino.NotesException;

public class TimeOnlyMapJsonTypeProcessor extends AbstractMapJsonTypeProcessor {

	@Override
	public void processItemToJsonObject(Item item, JsonObject jo, String jsonPropertyName) throws NotesException {
		DateTime dtCurrent = item.getDateTimeValue();
		Date javaDate = dtCurrent.toJavaDate();
		jo.putJsonProperty(jsonPropertyName, buildISO8601TimeOnly(javaDate));
	}

	@Override
	public void processValuesToJsonObject(List<?> values, JsonObject jo, String jsonPropertyName) throws NotesException {
		if (values != null && !values.isEmpty()) {
			Object value = values.get(0);
			if (value instanceof DateTime) {
				Date dtCurrent = ((DateTime) value).toJavaDate();
				jo.putJsonProperty(jsonPropertyName, buildISO8601TimeOnly(dtCurrent));
			}
		}
	}

	private String buildISO8601TimeOnly(Date javaDate) {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

		return df.format(javaDate);

	}

	public static Date parse(String input) throws java.text.ParseException {

		// NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
		// things a bit. Before we go on we have to repair this.
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

		// this is zero time so we need to add that TZ indicator for
		if (input.endsWith("Z")) {
			input = input.substring(0, input.length() - 1) + "GMT-00:00";
		} else {
			int inset = 6;

			String s0 = input.substring(0, input.length() - inset);
			String s1 = input.substring(input.length() - inset, input.length());

			input = s0 + "GMT" + s1;
		}

		return df.parse(input);

	}
}
