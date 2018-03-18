package org.squbich.configuration;

import org.squbich.OutputFormat;

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
    private static final String DEFAULT_TARGET = "stdout";
    private static final OutputFormat DEFAULT_FORMAT = OutputFormat.TEXT;

    public static final OutputConfiguration ofDefault() {
        return of(null, DEFAULT_FORMAT);
    }

//    public static final OutputConfiguration ofDefaultTarget(OutputFormat format) {
//        return of(DEFAULT_TARGET, format);
//    }


    public static final OutputConfiguration ofDefaultFormat(String target) {
        return of(target, DEFAULT_FORMAT);
    }

    private String target;
    private OutputFormat format;
}