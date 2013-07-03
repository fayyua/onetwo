package org.onetwo.common.utils.propconf;

import java.util.Collection;

import org.onetwo.common.utils.propconf.AbstractPropertiesManager.NamespaceProperty;

public interface NamespacePropertiesManager<T extends NamespaceProperty> extends JFishPropertiesManager<T>{

	public NamespaceProperties<T> getNamespaceProperties(String namespace);
	public boolean containsNamespace(String namespace);
	public Collection<NamespaceProperties<T>> getAllNamespaceProperties();
}
