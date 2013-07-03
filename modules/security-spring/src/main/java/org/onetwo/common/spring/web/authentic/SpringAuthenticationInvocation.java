package org.onetwo.common.spring.web.authentic;

import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.sso.SSOService;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.web.s2.security.AbstractAuthenticationInvocation;
import org.onetwo.common.web.s2.security.AuthenticationInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

@Service(AuthenticationInvocation.NAME)
public class SpringAuthenticationInvocation extends AbstractAuthenticationInvocation implements InitializingBean, ApplicationContextAware {

	private ExpressionParser parser = new SpelExpressionParser();
	
	private ApplicationContext applicationContext;
	private SSOService ssoService;

	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.ssoService = SpringUtils.getHighestOrder(applicationContext, SSOService.class);
		Assert.notNull(ssoService, "there is not sso service config");
	}

	@Override
	public SSOService getSSOService() {
		return ssoService;
	}

	@Override
	protected Object getContextValue(String expr) {
		return parser.parseExpression(expr).getValue();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
