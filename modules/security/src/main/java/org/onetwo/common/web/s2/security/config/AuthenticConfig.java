package org.onetwo.common.web.s2.security.config;

import java.util.ArrayList;
import java.util.List;

import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.ReflectUtils;
import org.onetwo.common.web.s2.security.Authenticator;


/***
 * 验证信息的配置类
 * @author weishao
 *
 */
public class AuthenticConfig {

	public static String DEFAULT_CONIFG_NAME = "DEFAULT_AUTHENTIC_CONIFG";
	public static AuthenticConfig DEFAULT_CONIFG = new AuthenticConfig(DEFAULT_CONIFG_NAME, true, false, false, false);//if no authentic config 
	
	public static String TYPE_URL = "url";
	public static String TYPE_ANNOTATION = "annotation";

	protected String name;
	
	private String authenticationName;
	protected boolean ignore;
	private boolean checkLogin = true;
	private boolean checkTimeout = true;
	private boolean throwIfTimeout = true;
	
	private String[] roles;

	protected List<Authenticator> authenticators;

	protected boolean onlyAuthenticator;
	
	protected String[] permissions;
	
	protected String type = TYPE_ANNOTATION;
	
	protected String redirect;


	public AuthenticConfig(boolean ignore) {
		this.name = DEFAULT_CONIFG_NAME;
		this.ignore = ignore;
	}

	public AuthenticConfig(String name, boolean ignore, boolean checkLogin, boolean checkTimeout, boolean throwIfTimeout) {
		this.name = name;
		this.ignore = ignore;
		this.checkLogin = checkLogin;
		
		this.checkTimeout = checkTimeout;
		this.throwIfTimeout =  throwIfTimeout;
	}

	/****
	 * 
	 * @param name 目前以调用action的方法名为anme
	 * @param onlyAuthenticator
	 */
	public AuthenticConfig(String name, boolean onlyAuthenticator) {
		this.ignore = true;
		this.name = name;
		this.onlyAuthenticator = onlyAuthenticator;
	}

	public String getName() {
		return name;
	}


//	public AuthenticConfig addPermission(String permission) {
//		if(this.permissions==null)
//			this.permissions = new ArrayList<String>();
//		this.permissions.add(permission);
//		return this;
//	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAuthenticators(List<Authenticator> authenticators) {
		this.authenticators = authenticators;
	}

	public void setOnlyAuthenticator(boolean onlyAuthenticator) {
		this.onlyAuthenticator = onlyAuthenticator;
	}

	public boolean isOnlyAuthenticator() {
		return onlyAuthenticator;
	}

	public List<Authenticator> getAuthenticators() {
		return authenticators;
	}

	public void addAuthenticator(Authenticator authenticator) {
		Assert.notNull(authenticator);
		if(this.authenticators==null)
			this.authenticators = new ArrayList<Authenticator>(5);
		this.authenticators.add(authenticator);
	}

	
	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public boolean isCheckLogin() {
		return checkLogin;
	}

	public void setCheckLogin(boolean checkLogin) {
		this.checkLogin = checkLogin;
	}

	public boolean isCheckTimeout() {
		return checkTimeout;
	}

	public void setCheckTimeout(boolean checkTimeout) {
		this.checkTimeout = checkTimeout;
	}

	public boolean isThrowIfTimeout() {
		return throwIfTimeout;
	}

	public void setThrowIfTimeout(boolean throwIfTimeout) {
		this.throwIfTimeout = throwIfTimeout;
	}
	
	public boolean isUrlAuthentic(){
		return TYPE_URL.equals(type);
	}
	
	public boolean isAnnotationAuthentic(){
		return TYPE_ANNOTATION.equals(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAuthenticationName() {
		return authenticationName;
	}

	public void setAuthenticationName(String authenticationName) {
		this.authenticationName = authenticationName;
	}

	public String[] getPermissions() {
		return permissions;
	}

	public void setPermissions(String... permissions) {
		this.permissions = permissions;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public AuthenticConfig copy(){
		return copy(AuthenticConfig.class);
	}
	
	public <T extends AuthenticConfig> T copy(Class<T> clazz){
		T obj = ReflectUtils.newInstance(clazz);
		obj.setName(name);
		obj.setAuthenticationName(authenticationName);
		obj.setAuthenticators(authenticators);
		obj.setCheckLogin(checkLogin);
		obj.setCheckTimeout(checkTimeout);
		obj.setIgnore(ignore);
		obj.setOnlyAuthenticator(onlyAuthenticator);
		obj.setPermissions(permissions);
		obj.setRedirect(redirect);
		obj.setThrowIfTimeout(throwIfTimeout);
		obj.setType(type);
		obj.setRoles(roles);
		return obj;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{name:").append(name)
		.append(", authenticationName:").append(authenticationName)
		.append(", ignore authencation:").append(ignore)
		.append(", permissions:").append(permissions)
		.append(", checkLogin:").append(checkLogin)
		.append(", checkTimeout:").append(checkTimeout)
		.append(", throwIfTimeout:").append(throwIfTimeout)
		.append("}");
		return sb.toString();
	}
	
}
