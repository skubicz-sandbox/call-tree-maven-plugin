package org.squbich;

import org.squbich.configuration.FilterConfiguration;
import org.squbich.configuration.OutputConfiguration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public class PrinterConfiguration {
    private String root;
    private OutputConfiguration outputConfiguration;
    private FilterConfiguration filterConfiguration;
}