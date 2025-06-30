package ru.blackfan.bfscan.parsing.httprequests.processors;

import jadx.api.plugins.input.data.annotations.EncodedValue;
import jadx.api.plugins.input.data.ILocalVar;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.RootNode;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.blackfan.bfscan.helpers.Helpers;
import ru.blackfan.bfscan.parsing.httprequests.Constants;
import ru.blackfan.bfscan.parsing.httprequests.MultiHTTPRequest;
import ru.blackfan.bfscan.parsing.httprequests.ParameterInfo;

public class KtorResourceProcessor implements AnnotationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(KtorResourceProcessor.class);

    @Override
    public ArgProcessingState processParameterAnnotations(MultiHTTPRequest request,
            ParameterInfo paramInfo,
            String annotationClass,
            Map<String, EncodedValue> annotationValues,
            List<ILocalVar> localVars,
            int methodArg,
            ArgType argType,
            RootNode rootNode) throws Exception {
        return ArgProcessingState.NOT_PROCESSED;
    }

    @Override
    public boolean processMethodAnnotations(MultiHTTPRequest request,
            String annotationClass,
            Map<String, EncodedValue> annotationValues,
            RootNode rn) {
        switch (annotationClass) {
            case Constants.Ktor.RESOURCE, Constants.Ktor.RESOURCE2, Constants.Ktor.LOCATION -> {
                request.addAdditionalInformation("Ktor Resource");

                EncodedValue value = AnnotationUtils.getValue(annotationValues, List.of("path"));
                if (value != null) {
                    String path = Helpers.stringWrapper(value);
                    request.setPath(path, false);
                    return true;
                }
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public boolean processClassAnnotations(MultiHTTPRequest request,
            String annotationClass,
            Map<String, EncodedValue> annotationValues,
            String globalBasePath,
            String className,
            RootNode rn) {
        switch (annotationClass) {
            case Constants.Ktor.RESOURCE, Constants.Ktor.RESOURCE2, Constants.Ktor.LOCATION -> {
                request.addAdditionalInformation("Ktor Resource");

                EncodedValue value = AnnotationUtils.getValue(annotationValues, List.of("path"));
                if (value != null) {
                    String classPath = Helpers.stringWrapper(value);
                    String fullPath = (classPath.startsWith("/")
                            ? globalBasePath.substring(0, globalBasePath.length() - 1)
                            : globalBasePath) + classPath;
                    request.setBasePaths(List.of(fullPath));
                    request.setPath("", false);
                    try {
                        Map<String, Object> parameters = AnnotationUtils.classToRequestParameters(Helpers.loadClass(rn, className), false, rn);
                        AnnotationUtils.appendParametersToRequest(request, parameters);
                    } catch (Exception e) {
                        logger.error("Error parsing parameters for Ktor Resource", e);
                    }
                    return true;
                }
                return false;
            }
            default -> {
                return false;
            }
        }
    }
}