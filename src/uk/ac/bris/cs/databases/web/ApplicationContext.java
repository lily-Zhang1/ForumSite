package uk.ac.bris.cs.databases.web;

import freemarker.template.Configuration;
import uk.ac.bris.cs.databases.api.APIProvider;

/**
 *
 * @author
 */
public class ApplicationContext {
    
    public static ApplicationContext instance = new ApplicationContext();
    /** 防止其他方法调用 */
    private ApplicationContext() {} 
    
    private APIProvider api;

    private Configuration templateConfiguration;

    public static ApplicationContext getInstance() {
        return instance;
    }

    /**
     * @return the api
     */
    public APIProvider getApi() {
        return api;
    }

    /**
     * @param api the api to set
     */
    public void setApi(APIProvider api) {
        this.api = api;
    }

    /**
     * @return the templateConfiguration
     */
    public Configuration getTemplateConfiguration() {
        return templateConfiguration;
    }

    /**
     * @param templateConfiguration the templateConfiguration to set
     */
    public void setTemplateConfiguration(Configuration templateConfiguration) {
        this.templateConfiguration = templateConfiguration;
    }
}
