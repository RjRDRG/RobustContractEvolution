package com.rce.editor.io;

import com.rce.editor.structures.Result;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

public class ResultIO {

    public static Result readFromYaml(String path) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(path);
        Yaml yaml = new Yaml(new Constructor(Result.class));
        return yaml.load(inputStream);
    }

    public static void writeToYaml(Result result, String path) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(path);
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        yaml.dump(result, writer);
    }
}
