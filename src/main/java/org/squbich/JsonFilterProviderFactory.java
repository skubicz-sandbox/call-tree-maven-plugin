package org.squbich;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.squbich.calltree.browser.JsonFilters;
import org.squbich.calltree.model.executions.ClassRoot;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(staticName = "of")
public class JsonFilterProviderFactory {
    private Map<String, Set<String>> fieldsForFilters;

    @Test
    public void createJsonFiltersTest() {
        create(Lists.newArrayList("methods[].method.parentClass", "methods[].method.comment"));
    }

    public FilterProvider create(List<String> fieldPaths) {
        initFiltersMap();

        if (fieldPaths != null) {
            fieldPaths.forEach(path -> {
                List<FieldPathPart> pathParts = Stream.of(path.split("\\.")).map(FieldPathPart::of).collect(Collectors.toList());

                resolveFilterFields(ClassRoot.class, 0, pathParts);
            });
        }

        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        fieldsForFilters.forEach((filterName, fields) -> {
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.filterOutAllExcept(fields);
            filterProvider.addFilter(filterName, filter);
        });

        System.out.println(fieldsForFilters);
        return filterProvider;
    }

    private void initFiltersMap() {
        fieldsForFilters = new HashMap<>();
        try {
            Field[] fields = JsonFilters.class.getDeclaredFields();
            for (Field field : fields) {
                fieldsForFilters.put(field.get(null).toString(), Sets.newHashSet());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resolveFilterFields(Class<?> clazz, int currentElement, List<FieldPathPart> pathParts) {
        try {
            if (currentElement == pathParts.size()) {
                return;
            }
            FieldPathPart fieldPart = pathParts.get(currentElement);
            Method fieldGetter = clazz.getDeclaredMethod(getGetterName(fieldPart.getField()));

            JsonFilter jsonFilter = clazz.getDeclaredAnnotation(JsonFilter.class);
            if (jsonFilter != null) {
                Set<String> fieldsToSerialise = fieldsForFilters.get(jsonFilter.value());

                fieldsToSerialise.add(fieldPart.getField());
            }

            resolveFilterFields(getFieldType(fieldGetter, fieldPart.isCollection()), currentElement + 1, pathParts);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getGetterName(String fieldName) {
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    private Class<?> getFieldType(Method fieldGetter, boolean isCollection) {
        Class<?> type = null;

        if (isCollection) {
            type = (Class<?>) ((ParameterizedType) fieldGetter.getGenericReturnType()).getActualTypeArguments()[0];
        }
        else {
            type = fieldGetter.getReturnType();
        }
        return type;
    }

    @Getter
    @ToString
    private static class FieldPathPart {
        private String expression;
        private String field;
        private boolean collection;

        public static final FieldPathPart of(String expression) {
            FieldPathPart fieldPathPart = new FieldPathPart();
            fieldPathPart.expression = expression;
            fieldPathPart.collection = expression.matches("^[a-zA-Z0-9_]*\\[\\]");
            if (fieldPathPart.collection) {
                fieldPathPart.field = expression.substring(0, expression.length() - 2);
            }
            else {
                fieldPathPart.field = expression;
            }

            return fieldPathPart;
        }

    }
}