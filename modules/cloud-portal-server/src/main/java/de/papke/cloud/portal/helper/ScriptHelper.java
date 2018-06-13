package de.papke.cloud.portal.helper;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.User;

/**
 * Script helper class with utility functions for groovy scripts.
 */
public class ScriptHelper {

    private static final String OBJECTS_FOUND = "OBJECTS FOUND: ";

    private PrintWriter out;

    public ScriptHelper(PrintWriter out) {
        this.out = out;
    }

    /**
     * Method for printing an object on console.
     *
     * @param result
     */
    public void print(Object result) {
    	
    	if (result instanceof User) {
    		User user = (User) result;
    		printUser(user);
    	}
    	else {
    		if (result instanceof Collection<?>) {
    			Collection<?> collection = (Collection<?>) result;
    			printCollection(collection);
    		}
    		else {
    			if (result instanceof Map<?, ?>) {
    				Map<?, ?> map = (Map<?, ?>) result;
    				printMap(map);
    			}
    			else {
    				out.append(String.valueOf(result));
    			}
    		}
    	}
    }
    
    /**
     * Method for printing an object on console.
     *
     * @param result
     */
    public void println(Object result) {
    	print(result);
    	print(Constants.CHAR_NEW_LINE);
    }

    /**
     * Method for printing a collection on console.
     *
     * @param collection
     */
    private void printCollection(Collection<?> collection) {

        Object[] collectionArray = collection.toArray();

        out
                .append(OBJECTS_FOUND)
                .append(String.valueOf(collectionArray.length))
                .append(Constants.CHAR_NEW_LINE)
                .append(Constants.CHAR_NEW_LINE);

        for (Object object : collectionArray) {
            print(object);
            out.append(Constants.CHAR_NEW_LINE);
        }
    }

    /**
     * Method for printing a map on console.
     *
     * @param map
     */
    private void printMap(Map<?, ?> map) {

        Set<?> keySet = map.keySet();

        out
                .append(OBJECTS_FOUND)
                .append(String.valueOf(map.keySet().size()))
                .append(Constants.CHAR_NEW_LINE)
                .append(Constants.CHAR_NEW_LINE);

        for (Object key : keySet) {
            Object value  = map.get(key);
            out
                    .append(String.valueOf(key))
                    .append(": ")
                    .append(String.valueOf(value))
                    .append(Constants.CHAR_NEW_LINE);
        }
    }

    /**
     * Method for printing a user object on console.
     *
     * @param user
     */
    private void printUser(User user) {

        String username = user.getUsername();

        out
                .append(username)
                .append(Constants.CHAR_NEW_LINE);

        for (int i = 0; i < username.length(); i++) {
            out.append("-");
            if (i == username.length() - 1) {
                out.append(Constants.CHAR_NEW_LINE);
            }
        }

        List<String> groups = user.getGroups();

        for (String group : groups) {
            out
                    .append(group)
                    .append(Constants.CHAR_NEW_LINE);
        }
    }
}