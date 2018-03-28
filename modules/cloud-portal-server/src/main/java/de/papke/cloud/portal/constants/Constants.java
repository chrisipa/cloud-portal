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
	public static final String CHAR_TAB = "\t";
	public static final String CHAR_DOT = ".";
	public static final String CHAR_DOUBLE_DOT = ":";
	public static final String CHAR_BRACE_OPEN = "{";
	public static final String CHAR_BRACE_CLOSE = "}";
	public static final String CHAR_BRAKET_OPEN = "[";
	public static final String CHAR_BRAKET_CLOSE = "]";
	public static final String CHAR_COMMA = ",";
	
	public static final String ACTION_INIT = "init";
	public static final String ACTION_APPLY = "apply";
	public static final String ACTION_DESTROY = "destroy";
	
	public static final String VM_EXPIRATION_DAYS_STRING = "expiration_days";
	
	public static final String KEY_FILE_PREFIX = "id_rsa";
	public static final String KEY_FILE_SUFFIX = ".pub";
	
	public static final String TMP_FOLDER_PREFIX = System.getProperty("java.io.tmpdir") + "/tmp-";
	
	public static final String FOLDER_TERRAFORM = "terraform";
	
	public static final String VAR_TYPE_SECRET = "secret";
	
	private Constants() {}
}
