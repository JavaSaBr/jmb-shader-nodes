package com.ss.editor.shader.nodes.util;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.rlib.util.dictionary.DictionaryFactory;
import com.ss.rlib.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;

/**
 * The list of all GLSL types.
 *
 * @author JavaSaBr
 */
public enum GLSLType {
    BOOL("bool"),
    INT("int"),
    FLOAT("float"),
    VEC_2("vec2"),
    VEC_3("vec3"),
    VEC_4("vec4"),
    MAT_2("mat2"),
    MAT_3("mat3"),
    MAT_4("mat4"),
    SAMPLER_2D("sampler2D"),
    SAMPLER_CUBE("samplerCube");

    @NotNull
    public static final GLSLType[] VALUES = values();

    @NotNull
    private static final ObjectDictionary<String, GLSLType> RAW_TYPE_TO_ENUM = DictionaryFactory.newObjectDictionary();

    /**
     * Get the enum value for the raw type.
     *
     * @param rawType the raw type.
     * @return the enum value.
     */
    @FromAnyThread
    public static @NotNull GLSLType of(@NotNull final String rawType) {
        return notNull(RAW_TYPE_TO_ENUM.get(rawType));
    }

    /**
     * The type to use in shaders.
     */
    @NotNull
    private String rawType;

    GLSLType(@NotNull final String rawType) {
        this.rawType = rawType;
    }

    /**
     * Get the type to use in shaders.
     *
     * @return the type to use in shaders.
     */
    @FromAnyThread
    public @NotNull String getRawType() {
        return rawType;
    }
}
