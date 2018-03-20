package org.squbich.configuration;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class OutputConfiguration {
    private static final OutputFormat DEFAULT_FORMAT = OutputFormat.JSON;
    private static final List<String> DEFAULT_ELEMENTS = Lists
            .newArrayList("className", "methods[].method.name", "methods[].calls[].children", "methods[].calls[].expression");

    private String target;
    private OutputFormat format;
    private List<String> elements;

    public static final OutputConfiguration ofDefault() {
        return of(null, DEFAULT_FORMAT, DEFAULT_ELEMENTS);
    }

    public static final OutputConfiguration ofDefaultWhenNull(String target, OutputFormat format, List<String> elements) {
        return of(target, format == null ? DEFAULT_FORMAT : format, elements == null ? DEFAULT_ELEMENTS : elements);
    }
}