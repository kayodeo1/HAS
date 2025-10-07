/**
 *
 */
package com.oou.has.model;

import java.util.ResourceBundle;

/**
 *
 */
public class Constants {
	public static final String APP_BASE_URL;

	static ResourceBundle rs = ResourceBundle.getBundle("resources.config");

	public static String APP_BASE_NAME;
	public static String MONNIFY_BASE_URL;
	public static String MONNIFY_KEY;
	public static String MONNIFY_SECRET;
	public static String MONNIFY_CONTRACT_CODE;

	static {
		APP_BASE_NAME = rs.getString("app.base.name");
		MONNIFY_BASE_URL = rs.getString("monnify.base.url");
		MONNIFY_KEY = rs.getString("monnify.api.key");
		MONNIFY_SECRET = rs.getString("monnify.api.secret");
		MONNIFY_CONTRACT_CODE = rs.getString("monnify.contract.code");
		APP_BASE_URL = rs.getString("app.base.url");

	}

}
