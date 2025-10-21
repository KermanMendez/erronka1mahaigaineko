package model;

import org.jdatepicker.impl.DateComponentFormatter;

@SuppressWarnings("serial")
public class DateFormater extends DateComponentFormatter {
		private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");

		@Override
		public String valueToString(Object value) throws java.text.ParseException {
			if (value instanceof java.util.Date) {
				return sdf.format((java.util.Date) value);
			} else if (value instanceof java.util.Calendar) {
				return sdf.format(((java.util.Calendar) value).getTime());
			}
			return super.valueToString(value);
		}
	}
