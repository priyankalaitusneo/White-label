package com.laitsneo.whitelbl.util;

import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class HelperComponent {

    @Autowired
    private ApplicationContext applicationContext;

    public Set<String> scanURIs() {
        Set<String> uris = new HashSet<>() ;
        Map<String, RequestMappingHandlerMapping> mappings = applicationContext.getBeansOfType(RequestMappingHandlerMapping.class);
        for (RequestMappingHandlerMapping mapping : mappings.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
            for (RequestMappingInfo requestMappingInfo : handlerMethods.keySet()) {
                String path = requestMappingInfo.getDirectPaths().toString();
                String uri = path.substring(1,path.length()-1);
                if(!uri.equalsIgnoreCase("/error") && !uri.equalsIgnoreCase(""))
                {
                    uris.add(uri);
                }
            }
        }
        return uris;
    }
}
