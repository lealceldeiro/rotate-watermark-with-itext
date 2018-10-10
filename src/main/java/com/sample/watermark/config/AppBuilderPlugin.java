package com.sample.watermark.config;


import springfox.documentation.service.ObjectVendorExtension;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.Collections;

import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;

public class AppBuilderPlugin implements OperationBuilderPlugin {
    @Override
    public void apply(OperationContext operationContext) {
        if (operationContext.findControllerAnnotation(WaterMarkArea.class).isPresent()) {
            ObjectVendorExtension extension = new ObjectVendorExtension("x-area");
            extension.addProperty(new StringVendorExtension("area", "watermark"));
            operationContext.operationBuilder().extensions(Collections.singletonList(extension));
        }
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return pluginDoesApply(documentationType);
    }
}
