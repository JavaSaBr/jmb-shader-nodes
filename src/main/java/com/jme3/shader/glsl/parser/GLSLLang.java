package com.jme3.shader.glsl.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * The class with language constants.
 *
 * @author JavaSaBr
 */
public class GLSLLang {

    public static final String PR_TYPE_IF = "if";
    public static final String PR_TYPE_IFDEF = "ifdef";
    public static final String PR_TYPE_IFNDEF = "ifndef";
    public static final String PR_TYPE_ELIF = "elif";

    public static final String PR_TYPE_DEFINE = "define";
    public static final String PR_TYPE_UNDEF = "undef";
    public static final String PR_TYPE_ELSE = "else";
    public static final String PR_TYPE_ENDIF = "endif";
    public static final String PR_TYPE_ERROR = "error";
    public static final String PR_TYPE_PRAGMA = "pragma";
    public static final String PR_TYPE_EXTENSION = "extension";
    public static final String PR_TYPE_IMPORT = "import";
    public static final String PR_TYPE_VERSION = "version";
    public static final String PR_TYPE_LINE = "line";

    public static final String PR_IF = "#" + PR_TYPE_IF;
    public static final String PR_IFDEF = "#" + PR_TYPE_IFDEF;
    public static final String PR_IFNDEF = "#" + PR_TYPE_IFNDEF;
    public static final String PR_ELIF = "#" + PR_TYPE_ELIF;
    public static final String PR_ELSE = "#" + PR_TYPE_ELSE;
    public static final String PR_ENDIF = "#" + PR_TYPE_ENDIF;
    public static final String PR_DEFINE = "#" + PR_TYPE_DEFINE;
    public static final String PR_EXTENSION = "#" + PR_TYPE_EXTENSION;
    public static final String PR_IMPORT = "#" + PR_TYPE_IMPORT;

    public static final Set<String> KEYWORDS = new HashSet<>();

    static {
        KEYWORDS.add("uniform");
        KEYWORDS.add("in");
        KEYWORDS.add("out");
        KEYWORDS.add("varying");
        KEYWORDS.add("attribute");
        KEYWORDS.add("discard");
        KEYWORDS.add("if");
        KEYWORDS.add("endif");
        KEYWORDS.add("defined");
        KEYWORDS.add("else");
        KEYWORDS.add("ifdef");
        KEYWORDS.add("ifndef");
        KEYWORDS.add("const");
        KEYWORDS.add("break");
        KEYWORDS.add("continue");
        KEYWORDS.add("do");
        KEYWORDS.add("for");
        KEYWORDS.add("while");
        KEYWORDS.add("inout");
        KEYWORDS.add("struct");
    }

    public static final Set<String> PREPROCESSOR = new HashSet<>();
    public static final Set<String> PREPROCESSOR_WITH_CONDITION = new HashSet<>();

    static {
        PREPROCESSOR_WITH_CONDITION.add(PR_TYPE_IF);
        PREPROCESSOR_WITH_CONDITION.add(PR_TYPE_IFDEF);
        PREPROCESSOR_WITH_CONDITION.add(PR_TYPE_IFNDEF);
        PREPROCESSOR_WITH_CONDITION.add(PR_TYPE_ELIF);

        PREPROCESSOR.addAll(PREPROCESSOR_WITH_CONDITION);
        PREPROCESSOR.add(PR_TYPE_DEFINE);
        PREPROCESSOR.add(PR_TYPE_UNDEF);
        PREPROCESSOR.add(PR_TYPE_ELSE);
        PREPROCESSOR.add(PR_TYPE_ENDIF);
        PREPROCESSOR.add(PR_TYPE_ERROR);
        PREPROCESSOR.add(PR_TYPE_PRAGMA);
        PREPROCESSOR.add(PR_TYPE_EXTENSION);
        PREPROCESSOR.add(PR_TYPE_VERSION);
        PREPROCESSOR.add(PR_TYPE_LINE);
    }
}
