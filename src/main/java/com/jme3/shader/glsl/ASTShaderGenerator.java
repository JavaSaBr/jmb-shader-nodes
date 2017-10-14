package com.jme3.shader.glsl;

import static com.jme3.shader.glsl.parser.ast.util.ASTUtils.findAllByType;
import static java.lang.System.getProperty;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.cache.AssetCache;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.*;
import com.jme3.shader.Shader.ShaderType;
import com.jme3.shader.glsl.parser.GLSLParser;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.ExternalFieldDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.FileDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.LocalVarDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.MethodDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.preprocessor.ExtensionPreprocessorASTNode;
import com.jme3.shader.glsl.parser.ast.util.ASTUtils;
import com.ss.editor.FileExtensions;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The base implementation of a shader generator with using AST GLSL to generate a result shader.
 *
 * @author JavaSaBr
 */
public abstract class ASTShaderGenerator extends Glsl100ShaderGenerator {

    private static final boolean USE_AST_CACHE;

    static {
        USE_AST_CACHE = Boolean.parseBoolean(getProperty("ASTShaderGenerator.useCache", "false"));
    }

    private class ImportedShaderKey extends AssetKey<Reader> {

        private ImportedShaderKey(final String name) {
            super(name);
        }

        @Override
        public Class<? extends AssetCache> getCacheType() {
            return null;
        }
    }

    /**
     * The cache of AST of shader sources.
     */
    private static final ThreadLocal<Map<String, FileDeclarationASTNode>> AST_CACHE = new ThreadLocal<Map<String, FileDeclarationASTNode>>() {

        @Override
        protected Map<String, FileDeclarationASTNode> initialValue() {
            return new HashMap<>();
        }
    };

    /**
     * The mapping a shader node source path to a AST presentation.
     */
    private static final ThreadLocal<Map<ShaderNode, FileDeclarationASTNode>> SHADER_NODE_SOURCES = new ThreadLocal<Map<ShaderNode, FileDeclarationASTNode>>() {

        @Override
        protected Map<ShaderNode, FileDeclarationASTNode> initialValue() {
            return new HashMap<>();
        }
    };

    /**
     * The list of extensions.
     */
    private static final ThreadLocal<List<ExtensionPreprocessorASTNode>> EXTENSIONS = new ThreadLocal<List<ExtensionPreprocessorASTNode>>() {

        @Override
        protected List<ExtensionPreprocessorASTNode> initialValue() {
            return new ArrayList<>();
        }
    };

    /**
     * The list of imports.
     */
    private static final ThreadLocal<List<String>> IMPORTS = new ThreadLocal<List<String>>() {

        @Override
        protected List<String> initialValue() {
            return new ArrayList<>();
        }
    };

    /**
     * The list of declared fields in imports.
     */
    private static final ThreadLocal<List<ExternalFieldDeclarationASTNode>> IMPORTED_FIELDS = new ThreadLocal<List<ExternalFieldDeclarationASTNode>>() {

        @Override
        protected List<ExternalFieldDeclarationASTNode> initialValue() {
            return new ArrayList<>();
        }
    };

    /**
     * The list of declared uniforms in imports.
     */
    private static final ThreadLocal<List<ExternalFieldDeclarationASTNode>> IMPORTED_GLOBAL_UNIFORMS = new ThreadLocal<List<ExternalFieldDeclarationASTNode>>() {

        @Override
        protected List<ExternalFieldDeclarationASTNode> initialValue() {
            return new ArrayList<>();
        }
    };

    /**
     * The list of methods.
     */
    private static final ThreadLocal<List<MethodDeclarationASTNode>> METHODS = new ThreadLocal<List<MethodDeclarationASTNode>>() {

        @Override
        protected List<MethodDeclarationASTNode> initialValue() {
            return new ArrayList<>();
        }
    };

    /**
     * The list of local variables.
     */
    private static final ThreadLocal<List<LocalVarDeclarationASTNode>> LOCAL_VARS = new ThreadLocal<List<LocalVarDeclarationASTNode>>() {

        @Override
        protected List<LocalVarDeclarationASTNode> initialValue() {
            return new ArrayList<>();
        }
    };

    /**
     * The indent.
     */
    private static final ThreadLocal<AtomicInteger> INDENT = new ThreadLocal<AtomicInteger>() {

        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };

    /**
     * The technique definition.
     */
    private static final ThreadLocal<TechniqueDef> TECHNIQUE_DEF = new ThreadLocal<>();

    public ASTShaderGenerator(final AssetManager assetManager) {
        super(assetManager);
    }

    @Override
    protected void indent() {
        INDENT.get().incrementAndGet();
    }

    @Override
    protected void unIndent() {
        final AtomicInteger counter = INDENT.get();
        if (counter.decrementAndGet() < 0) {
            counter.set(0);
        }
    }

    @Override
    protected void appendIndent(final StringBuilder source) {
        source.append(getIndent(INDENT.get().get()));
    }

    @Override
    public void initialize(final TechniqueDef techniqueDef) {
        super.initialize(techniqueDef);
        TECHNIQUE_DEF.set(techniqueDef);
        INDENT.get().set(0);
        IMPORTED_GLOBAL_UNIFORMS.get().clear();
        prepareShaderNodeSources(techniqueDef.getShaderNodes());
    }

    @Override
    public Shader generateShader(final String definesSourceCode) {
        final TechniqueDef techniqueDef = TECHNIQUE_DEF.get();
        final Shader result = super.generateShader(definesSourceCode);

        final List<UniformBinding> worldBindings = techniqueDef.getWorldBindings();
        final List<ExternalFieldDeclarationASTNode> globalUniforms = IMPORTED_GLOBAL_UNIFORMS.get();

        ASTUtils.removeExists(globalUniforms, worldBindings);

        if (!globalUniforms.isEmpty()) {
            for (final ExternalFieldDeclarationASTNode field : globalUniforms) {

                final NameASTNode nameNode = field.getName();
                final String name = nameNode.getName();

                final UniformBinding binding = UniformBinding.valueOf(name.substring(2, name.length()));
                result.addUniformBinding(binding);
            }
        }

        return result;
    }

    @Override
    protected String buildShader(final List<ShaderNode> shaderNodes, final ShaderGenerationInfo info,
                                 final ShaderType type) {

        if (type != ShaderType.Vertex && type != ShaderType.Fragment) {
            return null;
        }

        final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources = SHADER_NODE_SOURCES.get();
        final List<ExtensionPreprocessorASTNode> extensions = EXTENSIONS.get();
        extensions.clear();

        final List<String> imports = IMPORTS.get();
        imports.clear();

        findImportsAndExtensions(shaderNodes, type, shaderNodeSources, extensions, imports);

        ASTUtils.removeExtensionDuplicates(extensions);

        final StringBuilder sourceDeclaration = new StringBuilder();
        final StringBuilder source = new StringBuilder();

        generateExtensions(extensions, sourceDeclaration);

        final List<ExternalFieldDeclarationASTNode> importedFields = IMPORTED_FIELDS.get();
        importedFields.clear();

        generateImports(imports, importedFields, sourceDeclaration);

        final List<ExternalFieldDeclarationASTNode> importedGlobalUniforms = IMPORTED_GLOBAL_UNIFORMS.get();

        ASTUtils.copyGlobalUniforms(importedFields, importedGlobalUniforms);

        generateUniforms(sourceDeclaration, info, type);

        if (type == ShaderType.Vertex) {
            generateAttributes(sourceDeclaration, info);
        }

        generateVaryings(sourceDeclaration, info, type);
        generateMethods(shaderNodes, type, sourceDeclaration);

        generateStartOfMainSection(source, info, type);
        generateDeclarationAndMainBody(shaderNodes, sourceDeclaration, source, info, type);
        generateEndOfMainSection(source, info, type);

        sourceDeclaration.append(source);

        return sourceDeclaration.toString();
    }

    /**
     * Find all imports and extensions from the shader nodes.
     *
     * @param shaderNodes       the shader nodes.
     * @param type              the current type.
     * @param shaderNodeSources the shader node sources.
     * @param extensions        the extensions.
     * @param imports           the imports.
     */
    private void findImportsAndExtensions(final List<ShaderNode> shaderNodes, final ShaderType type,
                                          final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources,
                                          final List<ExtensionPreprocessorASTNode> extensions,
                                          final List<String> imports) {

        for (final ShaderNode shaderNode : shaderNodes) {

            final ShaderNodeDefinition definition = shaderNode.getDefinition();
            if (definition.getType() != type) {
                continue;
            }

            final List<String> defImports = definition.getImports();
            for (final String anImport : defImports) {
                if (!imports.contains(anImport)) {
                    imports.add(anImport);
                }
            }

            findAllByType(shaderNodeSources.get(shaderNode), extensions, ExtensionPreprocessorASTNode.class);
        }
    }

    @Override
    protected void generateDeclarationAndMainBody(final List<ShaderNode> shaderNodes,
                                                  final StringBuilder sourceDeclaration, final StringBuilder source,
                                                  final ShaderGenerationInfo info, final ShaderType type) {

        final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources = SHADER_NODE_SOURCES.get();
        final List<LocalVarDeclarationASTNode> localVariables = LOCAL_VARS.get();
        final List<MethodDeclarationASTNode> methods = METHODS.get();
        final List<String> unusedNodes = info.getUnusedNodes();

        for (final ShaderNode shaderNode : shaderNodes) {

            if (unusedNodes.contains(shaderNode.getName())) {
                continue;
            }

            final ShaderNodeDefinition definition = shaderNode.getDefinition();
            if (definition.getType() != type) {
                continue;
            }

            final FileDeclarationASTNode shaderFile = shaderNodeSources.get(shaderNode);

            methods.clear();

            findAllByType(shaderFile, methods, MethodDeclarationASTNode.class);

            final MethodDeclarationASTNode mainMethod = findMainMethod(methods);
            if (mainMethod == null) {
                generateNodeMainSection(source, shaderNode, shaderFile.getText(), info);
                continue;
            }

            findAllByType(mainMethod, localVariables, LocalVarDeclarationASTNode.class);

            String methodBodySource = updateMethodCalls(shaderNode, mainMethod, methods);
            methodBodySource = updateLocalVariables(shaderNode, methodBodySource, localVariables);

            comment(source, shaderNode, "Begin");
            startCondition(shaderNode.getCondition(), source);

            final List<String> declaredInputs = new ArrayList<>();

            for (VariableMapping mapping : shaderNode.getInputMapping()) {

                final ShaderNodeVariable rightVariable = mapping.getRightVariable();
                final ShaderNodeVariable leftVariable = mapping.getLeftVariable();

                //Variables fed with a sampler matparam or world param are replaced by the matparam itself
                //It avoids issue with samplers that have to be uniforms.
                if (isWorldOrMaterialParam(rightVariable) && rightVariable.getType().startsWith("sampler")) {
                    methodBodySource = replace(methodBodySource, leftVariable, rightVariable.getPrefix() + rightVariable.getName());
                } else {

                    if (leftVariable.getType().startsWith("sampler")) {
                        throw new IllegalArgumentException("a Sampler must be a uniform");
                    }

                    map(mapping, source);
                }

                String newName = shaderNode.getName() + "_" + leftVariable.getName();
                if (!declaredInputs.contains(newName)) {
                    methodBodySource = replace(methodBodySource, leftVariable, newName);
                    declaredInputs.add(newName);
                }
            }

            for (final ShaderNodeVariable var : definition.getInputs()) {
                final ShaderNodeVariable variable = new ShaderNodeVariable(var.getType(), shaderNode.getName(), var.getName(), var.getMultiplicity());
                final String fullName = shaderNode.getName() + "_" + var.getName();
                if (!declaredInputs.contains(fullName)) {
                    if (!isVarying(info, variable)) {
                        declareVariable(source, variable);
                    }
                    methodBodySource = replaceVariableName(methodBodySource, variable);
                    declaredInputs.add(fullName);
                }
            }

            for (ShaderNodeVariable var : definition.getOutputs()) {
                ShaderNodeVariable v = new ShaderNodeVariable(var.getType(), shaderNode.getName(), var.getName(), var.getMultiplicity());
                if (!declaredInputs.contains(shaderNode.getName() + "_" + var.getName())) {
                    if (!isVarying(info, v)) {
                        declareVariable(source, v);
                    }
                    methodBodySource = replaceVariableName(methodBodySource, v);
                }
            }

            appendIndent(source);
            source.append(methodBodySource);
            source.append('\n');

            for (VariableMapping mapping : shaderNode.getOutputMapping()) {
                map(mapping, source);
            }
            endCondition(shaderNode.getCondition(), source);
            comment(source, shaderNode, "End");
        }
    }

    @Override
    protected String replace(final String source, final ShaderNodeVariable var, final String newName) {
        return ASTUtils.replaceVar(source, var.getName(), newName);
    }

    /**
     * Updates the method calls from the main method.
     *
     * @param shaderNode the shader node.
     * @param mainMethod the main method.
     * @param methods    the list of all methods.
     * @return the updated source.
     */
    private String updateMethodCalls(final ShaderNode shaderNode, final MethodDeclarationASTNode mainMethod,
                                     final List<MethodDeclarationASTNode> methods) {

        String methodBodySource = mainMethod.getBody().getText();

        if (methods.size() < 2) {
            return methodBodySource;
        }

        for (final MethodDeclarationASTNode methodDeclaration : methods) {

            final NameASTNode methodName = methodDeclaration.getName();
            final String name = methodName.getName();

            if (name.equals("main")) {
                continue;
            }

            // replace calls of the declared methods.
            methodBodySource = methodBodySource.replace(name, shaderNode.getName() + "_" + name);
        }

        return methodBodySource;
    }

    /**
     * Updates the method calls from the main method.
     *
     * @param shaderNode the shader node.
     * @param source     the current source.
     * @param localVars  the list of all local variables.
     * @return the updated source.
     */
    private String updateLocalVariables(final ShaderNode shaderNode, String source,
                                        final List<LocalVarDeclarationASTNode> localVars) {

        if (localVars.isEmpty()) {
            return source;
        }

        for (final LocalVarDeclarationASTNode localVar : localVars) {

            final NameASTNode methodName = localVar.getName();
            final String name = methodName.getName();

            // replace calls of the declared methods.
            source = ASTUtils.replaceVar(source, name, shaderNode.getName() + "_" + name);
        }

        return source;
    }

    /**
     * Finds the main method.
     *
     * @param methods the list of methods.
     * @return the main method or null.
     */
    private MethodDeclarationASTNode findMainMethod(final List<MethodDeclarationASTNode> methods) {

        if (methods.isEmpty()) {
            return null;
        }

        for (final MethodDeclarationASTNode methodDeclaration : methods) {

            final NameASTNode methodName = methodDeclaration.getName();
            final String name = methodName.getName();

            if (name.equals("main")) {
                return methodDeclaration;
            }
        }

        return null;
    }

    /**
     * Generate all not main methods of all shader nodes.
     *
     * @param shaderNodes the shader nodes.
     * @param type        the shader type.
     * @param builder     the target builder.
     */
    protected void generateMethods(final List<ShaderNode> shaderNodes, final ShaderType type,
                                   final StringBuilder builder) {

        final List<MethodDeclarationASTNode> methods = METHODS.get();
        methods.clear();

        final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources = SHADER_NODE_SOURCES.get();

        for (final ShaderNode shaderNode : shaderNodes) {

            if (shaderNode.getDefinition().getType() != type) {
                continue;
            }

            methods.clear();

            final FileDeclarationASTNode shaderFile = shaderNodeSources.get(shaderNode);
            findAllByType(shaderFile, methods, MethodDeclarationASTNode.class);

            if (methods.size() < 2) {
                continue;
            }

            for (final MethodDeclarationASTNode method : methods) {

                final NameASTNode name = method.getName();
                final String methodName = name.getName();

                if ("main".equals(methodName)) {
                    continue;
                }

                final String methodContent = method.getText();
                final String resultContent = methodContent.replace(methodName, shaderNode.getName() + "_" + methodName);

                builder.append(resultContent).append('\n');
            }
        }
    }

    /**
     * Generates all imports.
     *
     * @param imports        the list of imports.
     * @param importedFields the list of imported fields.
     * @param builder        the target builder.
     */
    protected void generateImports(final List<String> imports,
                                   final List<ExternalFieldDeclarationASTNode> importedFields,
                                   final StringBuilder builder) {

        if (imports.isEmpty()) {
            return;
        }

        for (final String anImport : imports) {
            final FileDeclarationASTNode shaderFile = parseShaderSource(anImport);
            findAllByType(shaderFile, importedFields, ExternalFieldDeclarationASTNode.class);
            builder.append(shaderFile.getText()).append('\n');
        }

        builder.append('\n');
    }

    /**
     * Generates all extensions.
     *
     * @param extensions the list of extensions.
     * @param builder    the target builder.
     */
    protected void generateExtensions(final List<ExtensionPreprocessorASTNode> extensions,
                                      final StringBuilder builder) {

        if (extensions.isEmpty()) {
            return;
        }

        for (final ExtensionPreprocessorASTNode extension : extensions) {
            builder.append(extension.getText()).append('\n');
        }

        builder.append('\n');
    }

    @Override
    protected void generateUniforms(final StringBuilder source, final ShaderGenerationInfo info,
                                    final ShaderType type) {
        switch (type) {
            case Vertex:
                generateUniforms(source, info.getVertexUniforms());
                break;
            case Fragment:
                generateUniforms(source, info.getFragmentUniforms());
                break;
        }
    }

    @Override
    protected void generateUniforms(final StringBuilder source, final List<ShaderNodeVariable> uniforms) {

        final List<ExternalFieldDeclarationASTNode> importedFields = IMPORTED_FIELDS.get();

        source.append("\n");
        for (final ShaderNodeVariable var : uniforms) {
            if (isExist(var, importedFields)) continue;
            declareVariable(source, var, false, "uniform");
        }
    }

    @Override
    protected void generateAttributes(final StringBuilder source, final ShaderGenerationInfo info) {

        final List<ExternalFieldDeclarationASTNode> importedFields = IMPORTED_FIELDS.get();

        source.append("\n");

        boolean inPosition = false;

        for (final ShaderNodeVariable var : info.getAttributes()) {
            if (var.getName().equals("inPosition")) {
                inPosition = true;
                var.setCondition(null);
                fixInPositionType(var);
                //keep track on the InPosition variable to avoid iterating through attributes again
                inPosTmp = var;
            }
            if (isExist(var, importedFields)) continue;
            declareAttribute(source, var);
        }

        if (!inPosition) {
            inPosTmp = new ShaderNodeVariable("vec3", "inPosition");
            if (isExist(inPosTmp, importedFields)) return;
            declareAttribute(source, inPosTmp);
        }
    }

    /**
     * Check of existing the variable in the imported shaders.
     *
     * @param variable       the variable.
     * @param importedFields the list of fields from imported shader.
     * @return true if the variable is exists.
     */
    private boolean isExist(final ShaderNodeVariable variable,
                            final List<ExternalFieldDeclarationASTNode> importedFields) {

        if (importedFields.isEmpty()) {
            return false;
        }

        final String name = variable.getName();
        final String prefix = variable.getPrefix();

        final int length = prefix.length() + name.length();

        for (final ExternalFieldDeclarationASTNode field : importedFields) {
            final NameASTNode nameASTNode = field.getName();
            final String fieldName = nameASTNode.getName();
            if (fieldName.length() != length || !fieldName.startsWith(prefix)) {
                continue;
            } else if (fieldName.endsWith(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Prepares the map with shader source path - parsed AST files.
     *
     * @param shaderNodes the list of shader nodes.
     */
    protected void prepareShaderNodeSources(final List<ShaderNode> shaderNodes) {

        final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources = SHADER_NODE_SOURCES.get();
        shaderNodeSources.clear();

        for (final ShaderNode shaderNode : shaderNodes) {

            final ShaderNodeDefinition definition = shaderNode.getDefinition();

            final int index = findShaderIndexFromVersion(shaderNode, definition.getType());
            final String shaderSourcePath = definition.getShadersPath().get(index);

            shaderNodeSources.put(shaderNode, parseShaderSource(shaderSourcePath));
        }
    }

    /**
     * Parses the shader source by the shader source path.
     *
     * @param shaderSourcePath the path to the shader source file.
     * @return the parsed shader source as AST file.
     */
    protected FileDeclarationASTNode parseShaderSource(final String shaderSourcePath) {

        final Map<String, FileDeclarationASTNode> cache = AST_CACHE.get();
        final FileDeclarationASTNode cached = USE_AST_CACHE ? cache.get(shaderSourcePath) : null;

        if (cached != null) {
            return cached;
        }

        final String loadedSource;

        if (shaderSourcePath.endsWith(FileExtensions.GLSL_LIB)) {

            final StringBuilder builder = new StringBuilder();

            try (final Reader reader = assetManager.loadAsset(new ImportedShaderKey(shaderSourcePath))) {
                int ch;
                while ((ch = reader.read()) != -1) {
                    builder.append((char) ch);
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            loadedSource = builder.toString();

        } else {
            loadedSource = assetManager.loadAsset(new AssetKey<>(shaderSourcePath));
        }

        final GLSLParser parser = GLSLParser.newInstance();
        final FileDeclarationASTNode result = parser.parseFileDeclaration(shaderSourcePath, loadedSource);

        if (USE_AST_CACHE) {
            cache.put(shaderSourcePath, result);
        }

        return result;
    }
}
