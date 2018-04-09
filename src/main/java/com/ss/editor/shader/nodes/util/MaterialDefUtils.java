package com.ss.editor.shader.nodes.util;

import static com.ss.rlib.util.ObjectUtils.notNull;
import static com.ss.rlib.util.ReflectionUtils.getFieldValue;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.rlib.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * The utility class.
 *
 * @author JavaSaBr
 */
public class MaterialDefUtils {

    @NotNull
    private static final Field MAT_PARAMS_FIELD;

    static {
        MAT_PARAMS_FIELD = ReflectionUtils.getUnsafeField(new MaterialDef(), "matParams");
    }

    /**
     * Get the reference to mat params map of the material definition.
     *
     * @param def the material definition.
     * @return the mat params.
     */
    @FromAnyThread
    public static @NotNull Map<String, MatParam> getMatParams(@NotNull MaterialDef def) {
        return notNull(getFieldValue(def, MAT_PARAMS_FIELD));
    }
}
