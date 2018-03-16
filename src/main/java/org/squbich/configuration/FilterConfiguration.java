package org.squbich.configuration;

import org.apache.maven.plugins.annotations.Parameter;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FilterConfiguration {
    private String type;
    private String value;
}