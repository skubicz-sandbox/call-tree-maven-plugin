package org.squbich.configuration;

import org.squbich.calltree.filter.FilterConfiguration;
import org.squbich.configuration.OutputConfiguration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public class PrinterConfiguration {
    private String rootCaller;
    private String packageScan;
    private OutputConfiguration outputConfiguration;
    private FilterConfiguration filterConfiguration;
}