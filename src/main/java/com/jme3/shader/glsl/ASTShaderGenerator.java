package com.jme3.shader.glsl;

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
import com.jme3.shader.glsl.parser.ast.declaration.FileDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.MethodDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.preprocessor.ExtensionPreprocessorASTNode;
import com.jme3.shader.glsl.parser.ast.util.ASTUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources;

    /**
     * The list of extensions.
     */
    private final List<ExtensionPreprocessorASTNode> extensions;

    /**
     * The list of imports.
     */
    private final List<String> imports;

    /**
     * The list of methods.
     */
    private final List<MethodDeclarationASTNode> methods;

    public ASTShaderGenerator(final AssetManager assetManager) {
        super(assetManager);
        this.shaderNodeSources = new HashMap<>();
        this.imports = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }

    @Override
    public void initialize(final TechniqueDef techniqueDef) {
        super.initialize(techniqueDef);
        prepareShaderNodeSources(techniqueDef.getShaderNodes());
        indent = 0;
    }

    @Override
    protected String buildShader(final List<ShaderNode> shaderNodes, final ShaderGenerationInfo info,
                                 final ShaderType type) {

        if (type != ShaderType.Vertex && type != ShaderType.Fragment) {
            return null;
        }

        extensions.clear();
        imports.clear();

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

            final FileDeclarationASTNode shaderFile = shaderNodeSources.get(shaderNode);
            ASTUtils.findAllByType(shaderFile, extensions, ExtensionPreprocessorASTNode.class);
        }

        ASTUtils.removeExtensionDuplicates(extensions);

        final StringBuilder sourceDeclaration = new StringBuilder();
        final StringBuilder source = new StringBuilder();

        generateExtensions(sourceDeclaration);
        generateImports(sourceDeclaration);
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

    @Override
    protected void generateDeclarationAndMainBody(final List<ShaderNode> shaderNodes,
                                                  final StringBuilder sourceDeclaration, final StringBuilder source,
                                                  final ShaderGenerationInfo info, final ShaderType type) {

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

            ASTUtils.findAllByType(shaderFile, methods, MethodDeclarationASTNode.class);

            MethodDeclarationASTNode mainMethod = findMainMethod();

            if (mainMethod == null) {
                generateNodeMainSection(source, shaderNode, shaderFile.getText(), info);
                continue;
            }

            String methodBodySource = updateMethodCalls(shaderNode, mainMethod);

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
     * @return the updated source.
     */
    private String updateMethodCalls(final ShaderNode shaderNode, final MethodDeclarationASTNode mainMethod) {

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
     * Finds the main method.
     *
     * @return the main method or null.
     */
    private MethodDeclarationASTNode findMainMethod() {

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

        for (final ShaderNode shaderNode : shaderNodes) {

            if (shaderNode.getDefinition().getType() != type) {
                continue;
            }

            methods.clear();

            final FileDeclarationASTNode shaderFile = shaderNodeSources.get(shaderNode);
            ASTUtils.findAllByType(shaderFile, methods, MethodDeclarationASTNode.class);

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
                final String resultContent = methodContent.replace(methodName,
                        shaderNode.getName() + "_" + methodName);

                builder.append(resultContent).append('\n');
            }
        }
    }

    /**
     * Generates all imports.
     *
     * @param builder the target builder.
     */
    protected void generateImports(final StringBuilder builder) {

        if (imports.isEmpty()) {
            return;
        }

        for (final String anImport : imports) {

            try (final Reader reader = assetManager.loadAsset(new ImportedShaderKey(anImport))) {
                int ch;
                while ((ch = reader.read()) != -1) {
                    builder.append((char) ch);
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            builder.append('\n');
        }

        builder.append('\n');
    }

    /**
     * Generates all extensions.
     *
     * @param builder the target builder.
     */
    protected void generateExtensions(final StringBuilder builder) {

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

    /**
     * Prepares the map with shader source path - parsed AST files.
     *
     * @param shaderNodes the list of shader nodes.
     */
    protected void prepareShaderNodeSources(final List<ShaderNode> shaderNodes) {
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

        if (!USE_AST_CACHE) {

            final String loadedSource = assetManager.loadAsset(new AssetKey<>(shaderSourcePath));
            final GLSLParser parser = GLSLParser.newInstance();

            return parser.parseFileDeclaration(shaderSourcePath, loadedSource);
        }

        final Map<String, FileDeclarationASTNode> cache = AST_CACHE.get();
        final FileDeclarationASTNode cached = cache.get(shaderSourcePath);

        if (cached != null) {
            return cached;
        }

        final String loadedSource = assetManager.loadAsset(new AssetKey<>(shaderSourcePath));
        final GLSLParser parser = GLSLParser.newInstance();
        final FileDeclarationASTNode result = parser.parseFileDeclaration(shaderSourcePath, loadedSource);

        cache.put(shaderSourcePath, result);

        return result;
    }
}
