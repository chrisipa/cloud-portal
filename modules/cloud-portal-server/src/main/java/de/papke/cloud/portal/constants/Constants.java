package de.papke.cloud.portal.constants;

public class Constants {
	
	public static final String CHAR_EMPTY = "";
	public static final String CHAR_DIAMOND = "#";
	public static final String CHAR_EQUAL = "=";
	public static final String CHAR_DASH = "-";
	public static final String CHAR_WHITESPACE = " ";
	public static final String CHAR_DOUBLE_QUOTE = "\"";
	public static final String CHAR_SINGLE_QUOTE = "'";
	public static final String CHAR_NEW_LINE = "\n";
	public static final String CHAR_DOT = ".";
	
	public static final String ACTION_INIT = "init";
	public static final String ACTION_APPLY = "apply";
	public static final String ACTION_DESTROY = "destroy";
	
	public static final String VM_EXPIRATION_DAYS_STRING = "expiration_days";
	
	public static final String TMP_FOLDER_PREFIX = System.getProperty("java.io.tmpdir") + "/tmp-";
	
	private Constants() {}
}
