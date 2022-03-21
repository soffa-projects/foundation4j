package dev.soffa.foundation.spring.config;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Hidden
@Controller
@ConditionalOnProperty(value = {"app.redoc.enabled", "app.openapi.enabled"}, havingValue = "true")
public class RedocController {

    @GetMapping("/")
    @ResponseBody
    @Hidden
    @Operation(hidden = true)
    public String redocHome(HttpServletRequest request) {
        return String.format("<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <title>API</title>\n" +
            "    <meta charset=\"utf-8\"/>\n" +
            "    <meta content=\"width=device-width, initial-scale=1\" name=\"viewport\">\n" +
            "    <link rel=\"stylesheet\" type=\"text/css\" href=\"%1$s/redoc.css\">\n\n" +
            "    <link rel=\"shortcut icon\" href=\"%1$s/redoc.ico\">\n\n" +
            "    <link href=\"https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700\" rel=\"stylesheet\">\n" +
            "    <style>\n" +
            "        body {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<redoc spec-url='%1$s/v3/api-docs'></redoc>\n" +
            "<script src=\"%1$s/redoc.standalone.js\"></script>\n" +
            "</body>\n" +
            "</html>\n", request.getContextPath());
    }

}
