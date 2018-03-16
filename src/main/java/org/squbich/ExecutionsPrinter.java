package org.squbich;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        CallHierarchy callHierarchy = new CallHierarchy(typeResolver);

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

        if (OutputFormat.JSON.equals(configuration.getOutputConfiguration().getFormat())) {
            try {
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(classRoots);
                System.out.println(json);
                FileWriter fileWriter = new FileWriter("d:/data.json");
                fileWriter.write(json);
                fileWriter.flush();
            }
            catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println(classRoots);
        }
    }


    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }
}