package com.rce.editor;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.rce.editor.gui.EditorFrame;
import com.rce.parser.OpenApiContract;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;

import javax.swing.*;

public class Editor {

    public static String BasePath = "./src/main/resources/";

    public static void main(String[] args) {

        FlatDarculaLaf.setup();
        UIManager.put("Tree.paintLines", true);

        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);

        OpenAPI oldV = new OpenAPIParser().readLocation(BasePath +"old.yml", null, parseOptions).getOpenAPI();
        OpenAPI newV = new OpenAPIParser().readLocation(BasePath + "new.yml", null, parseOptions).getOpenAPI();

        new EditorFrame(new OpenApiContract(newV), new OpenApiContract(oldV));
    }

}
