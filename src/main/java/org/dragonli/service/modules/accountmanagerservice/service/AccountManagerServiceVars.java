/**
 * 
 */
package org.dragonli.service.modules.accountmanagerservice.service;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mac
 *
 */
public class AccountManagerServiceVars {
	
	public final static Set<Integer> groups = new HashSet<>();//需要初始化
	public static int groupCount ;//需要注入
	
	public static boolean pauseBefore = false;
	
}
