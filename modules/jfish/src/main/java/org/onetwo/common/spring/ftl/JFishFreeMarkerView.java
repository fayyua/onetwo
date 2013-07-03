package org.onetwo.common.spring.ftl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.onetwo.common.spring.web.BaseController;
import org.onetwo.common.spring.web.BaseController.UrlHelper;
import org.onetwo.common.spring.web.WebHelper;
import org.onetwo.common.spring.web.utils.JFishWebUtils;
import org.onetwo.common.utils.NiceDate;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

public class JFishFreeMarkerView extends FreeMarkerView {
	public static final String ENTITY_CLASS_KEY = "entityClass";

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
	}
	
	protected void exposeHelpers(Map<String, Object> model, HttpServletRequest request) throws Exception {
		model.put("now", new NiceDate());
		if(!model.containsKey(WebHelper.WEB_HELPER_KEY)){
			model.put(WebHelper.WEB_HELPER_KEY, JFishWebUtils.webHelper(request));
		}

		BaseController<?> c = JFishWebUtils.currentTypeController(BaseController.class);
		if(c!=null){
			//url helper
			model.put(UrlHelper.MODEL_KEY, c.getUrlMeta());

			//entityClass
			if(c.getEntityClass()!=null && !model.containsKey(ENTITY_CLASS_KEY)){
				model.put(ENTITY_CLASS_KEY, c.getEntityClass());
			}
			
		}
	}
}
