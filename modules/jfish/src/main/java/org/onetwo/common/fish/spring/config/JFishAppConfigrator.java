package org.onetwo.common.fish.spring.config;

import org.onetwo.common.web.config.WebAppConfigurator;

public interface JFishAppConfigrator extends JFishOrmConfigurator, WebAppConfigurator {

	public String[] getXmlBasePackages();
}
