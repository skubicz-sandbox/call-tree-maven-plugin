package org.squbich;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.squbich.calltree.filter.CallHierarchyFilter;
import org.squbich.calltree.model.calls.CallHierarchy;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.resolver.CallHierarchyResolver;
import org.squbich.calltree.resolver.TypeResolver;
import org.squbich.calltree.serialize.HierarchySerializer;
import org.squbich.configuration.OutputFormat;
import org.squbich.configuration.PrinterConfiguration;

import com.google.common.io.Resources;

public class CallHierarchyPrinter {
    private static final String PACKAGE_PATTERN = "^([a-zA_Z_][\\.\\w]*)\\.\\*$";//e.g. org.kub.*
    private TypeResolver typeResolver;
    private PrinterConfiguration configuration;

    @java.beans.ConstructorProperties({"typeResolver", "configuration"})
    private CallHierarchyPrinter(final TypeResolver typeResolver, final PrinterConfiguration configuration) {
        this.typeResolver = typeResolver;
        this.configuration = configuration;

        //        this.objectMapper.setFilterProvider(JsonFilterProviderFactory.of().create(Lists
        //                .newArrayList("className",
        //                        "methods[].method.name",
        //                        "methods[].method.comment",
        //                        "methods[].method.parentClass",
        //                        "methods[].method.parameters",
        //                        "methods[].method.annotations",
        //                        "methods[].method.returnType",
        //                        "methods[].executions[].children",
        //                        "methods[].executions[].callExpression",
        //                        "methods[].executions[].method.name",
        //                        "methods[].executions[].method.comment",
        //                        "methods[].executions[].method.parentClass",
        //                        "methods[].executions[].method.parameters",
        //                        "methods[].executions[].method.annotations",
        //                        "methods[].executions[].method.returnType")));
    }


    public static CallHierarchyPrinter of(final TypeResolver typeResolver, final PrinterConfiguration configuration) {
        return new CallHierarchyPrinter(typeResolver, configuration);
    }

    public void print() {
        CallHierarchy classRoots = resolveHierarchy();
        CallHierarchy filtered = CallHierarchyFilter.of(configuration.getFilterConfiguration()).filter(classRoots);

        String hierarchyAsText = hierarchyAsText(filtered);
        print(hierarchyAsText);

    }

    private CallHierarchy resolveHierarchy() {
        CallHierarchyResolver callHierarchyResolver = new CallHierarchyResolver(typeResolver, configuration.getPackageScan());

        CallHierarchy callHierarchy;
        if (configuration.getRootCaller().matches(PACKAGE_PATTERN)) {
            callHierarchy = callHierarchyResolver
                    .resolveHierarchy(configuration.getRootCaller().substring(0, configuration.getRootCaller().length() - 2));
        }
        else {
            JavaFile impl = JavaFile.builder().qualifiedName(QualifiedName.of(configuration.getRootCaller())).build();

            callHierarchy = callHierarchyResolver.resolveHierarchy(impl);
        }

        return callHierarchy;
    }

    private String hierarchyAsText(CallHierarchy callHierarchy) {
        try {
            String hierarchyAsText = null;

            String json = HierarchySerializer.of(configuration.getOutputConfiguration().getElements()).serialize(callHierarchy);
            if (OutputFormat.JSON.equals(configuration.getOutputConfiguration().getFormat())) {
                hierarchyAsText = json;
            }
            else if (OutputFormat.HTML.equals(configuration.getOutputConfiguration().getFormat())) {
                String indexTemplate = readFile("html-template/template-index.html");
                indexTemplate = indexTemplate.replaceFirst("<<!TIMESTAMP!>>", LocalDateTime.now().toString());

                Properties prop = new Properties();
                prop.load(getClass().getClassLoader().getResourceAsStream("html-template/template-build.properties"));
                System.out.println(prop);
                for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                    System.out.println(entry.getValue());
                    String fileToReplace = readFile((String) entry.getValue());
                    System.out.println("<<!" + entry.getKey() + "!>>");
                    indexTemplate = indexTemplate.replaceFirst("<<!" + entry.getKey() + "!>>", Matcher.quoteReplacement(fileToReplace));
                }

                hierarchyAsText = indexTemplate.replaceFirst("<<!DATA!>>", Matcher.quoteReplacement(json)
                        //                        .replaceAll("\\\\\\\\\"", "\\\"")
                        //                        .replaceAll("\"", "\\\\\"")
                );
            }
            else {
                throw new RuntimeException(
                        "Unsupported output format [format = " + configuration.getOutputConfiguration().getFormat() + "]");
            }
            return hierarchyAsText;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String readFile(String fileName) {
        try {
            return Resources.toString(Resources.getResource(fileName), Charset.forName("UTF-8"));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void print(String hierarchyAsText) {
        if (configuration.getOutputConfiguration().getTarget() == null) {
            System.out.println(hierarchyAsText);
        }
        else {
            try {
                FileUtils.write(new File(configuration.getOutputConfiguration().getTarget()), hierarchyAsText, Charset.forName("UTF-8"));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}