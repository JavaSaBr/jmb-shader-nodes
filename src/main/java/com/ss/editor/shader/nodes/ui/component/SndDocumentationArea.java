package com.ss.editor.shader.nodes.ui.component;

import com.ss.editor.annotation.FxThread;
import com.ss.editor.ui.control.code.BaseCodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * The implementation of text area to edit/show documentation of a shader node definition.
 *
 * @author JavaSaBr
 */
public class SndDocumentationArea extends BaseCodeArea {

    @NotNull
    protected static final String[][] AVAILABLE_CLASSES = {
            {"GLOBALVAR", CSS_KEYWORD},
            {"INPUT", CSS_VALUE_TYPE},
            {"OUTPUT", CSS_VALUE_VALUE},
            {CLASS_COMMENT, CSS_COMMENT},
    };

    @NotNull
    private static final Pattern PATTERN = Pattern.compile(
            "(?<GLOBALVAR>(@global))"
                    + "|(?<INPUT>(@input))"
                    + "|(?<OUTPUT>(@output))"
                    + "|(?<" + CLASS_COMMENT + ">" + COMMENT_PATTERN + ")"
    );

    @Override
    @FxThread
    protected @NotNull String[][] getAvailableClasses() {
        return AVAILABLE_CLASSES;
    }

    @Override
    @FxThread
    protected @NotNull StyleSpans<? extends Collection<String>> calculateStyleSpans(@NotNull final String text) {
        return computeHighlighting(PATTERN, text);
    }
}
