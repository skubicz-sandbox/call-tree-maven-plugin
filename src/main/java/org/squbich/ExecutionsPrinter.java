package org.squbich;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.executions.ClassRoot;
import org.squbich.calltree.resolver.CallHierarchy;
import org.squbich.calltree.resolver.TypeResolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;

public class ExecutionsPrinter {
    private static final String PACKAGE_PATTERN = "^([a-zA_Z_][\\.\\w]*)\\.\\*$";//e.g. org.kub.*
    private TypeResolver typeResolver;
    private PrinterConfiguration configuration;
    private ObjectMapper objectMapper;

    //TODO do usuniecia
    public ExecutionsPrinter() {
    }

    @java.beans.ConstructorProperties({"typeResolver", "configuration"})
    private ExecutionsPrinter(final TypeResolver typeResolver, final PrinterConfiguration configuration) {
        this.typeResolver = typeResolver;
        this.configuration = configuration;
        this.objectMapper = createObjectMapper();

        this.objectMapper.setFilterProvider(JsonFilterProviderFactory.of().create(Lists
                .newArrayList(
                        "className",
                        "methods[].method.name",
                        "methods[].method.comment",
                        "methods[].method.parentClass",
                        "methods[].method.parameters",
                        "methods[].method.annotations",
                        "methods[].method.returnType",
                        "methods[].executions[].children",
                        "methods[].executions[].callExpression",
                        "methods[].executions[].method.name",
                        "methods[].executions[].method.comment",
                        "methods[].executions[].method.parentClass",
                        "methods[].executions[].method.parameters",
                        "methods[].executions[].method.annotations",
                        "methods[].executions[].method.returnType"
                )));
    }


    public static ExecutionsPrinter of(final TypeResolver typeResolver, final PrinterConfiguration configuration) {
        return new ExecutionsPrinter(typeResolver, configuration);
    }

    public void print() {
        CallHierarchy callHierarchy = new CallHierarchy(typeResolver, "");

        List<ClassRoot> classRoots = new ArrayList<>();
        if (configuration.getRoot().matches(PACKAGE_PATTERN)) {
            List<ClassRoot> out = callHierarchy
                    .resolveHierarchy(configuration.getRoot().substring(0, configuration.getRoot().length() - 2));
            classRoots.addAll(out);
        }
        else {
            JavaFile impl = JavaFile.builder().qualifiedName(QualifiedName.of(configuration.getRoot())).build();

            ClassRoot out = callHierarchy.resolveHierarchy(impl);
            classRoots.add(out);
        }

        try {
//            String json = objectMapper.writeValueAsString(classRoots);
//            System.out.println(json);

            String hierarchyAsText = null;
            if (OutputFormat.JSON.equals(configuration.getOutputConfiguration().getFormat())) {
                hierarchyAsText = objectMapper.writeValueAsString(classRoots);
            } else if(OutputFormat.HTML.equals(configuration.getOutputConfiguration().getFormat())) {
                hierarchyAsText = objectMapper.writeValueAsString(classRoots);
            }
            else {
                throw new RuntimeException("Unsupported output format [format = " + configuration.getOutputConfiguration().getFormat() + "]");
            }
            System.out.println(hierarchyAsText);
            save(hierarchyAsText);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void save(String hierarchyAsText) {
        if(configuration.getOutputConfiguration().getTarget() == null) {
            System.out.println(hierarchyAsText);
        } else {
            try {
                ClassLoader classLoader = getClass().getClassLoader();
//                File indexTemplateFile = new File(classLoader.getResource("index-all.html").getFile());
//                String indexTemplate = FileUtils.readFileToString(indexTemplateFile, Charset.forName("UTF-8"));
                String indexTemplate = Resources.toString(Resources.getResource("index-all.html"), Charset.forName("UTF-8"));
                String html = indexTemplate.replaceFirst("<<!DATA!>>", hierarchyAsText.replaceAll("\\\"", "\\\\\""));

                FileWriter fileWriter = new FileWriter(configuration.getOutputConfiguration().getTarget());
                fileWriter.write(html);
                fileWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }
}