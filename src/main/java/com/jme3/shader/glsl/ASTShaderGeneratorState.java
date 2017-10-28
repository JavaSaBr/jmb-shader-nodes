package com.jme3.shader.glsl;

import com.jme3.material.TechniqueDef;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.glsl.parser.ast.declaration.ExternalFieldDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.FileDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.LocalVarDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.MethodDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.preprocessor.ExtensionPreprocessorASTNode;
import com.jme3.shader.glsl.parser.ast.value.DefineValueASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class with state of shader generator.
 *
 * @author JavaSaBr
 */
public class ASTShaderGeneratorState {

    /**
     * The cache of AST of shader sources.
     */
    private final Map<String, FileDeclarationASTNode> astCache;

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
     * The list of defined variables.
     */
    private final List<String> definedVariables;

    /**
     * The list of result defines.
     */
    private final List<String> resultDefines;

    /**
     * The list of define value nodes.
     */
    private final List<DefineValueASTNode> defineValueNodes;

    /**
     * The list of declared uniforms in imported shaders.
     */
    private final List<ExternalFieldDeclarationASTNode> importedUnforms;

    /**
     * The result list of declared uniforms in imported shaders.
     */
    private final List<ExternalFieldDeclarationASTNode> importedGlobalUniforms;

    /**
     * The list of methods.
     */
    private final List<MethodDeclarationASTNode> methods;

    /**
     * The list of local variables.
     */
    private final List<LocalVarDeclarationASTNode> localVars;

    /**
     * The technique definition.
     */
    private TechniqueDef techniqueDef;

    /**
     * The indent.
     */
    private int indent;

    public ASTShaderGeneratorState() {
        this.astCache = new HashMap<>();
        this.shaderNodeSources = new HashMap<>();
        this.extensions = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.definedVariables = new ArrayList<>();
        this.resultDefines = new ArrayList<>();
        this.defineValueNodes = new ArrayList<>();
        this.importedUnforms = new ArrayList<>();
        this.importedGlobalUniforms = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.localVars = new ArrayList<>();
    }

    /**
     * Gets the cache of AST of shader sources.
     *
     * @return the cache of AST of shader sources.
     */
    public Map<String, FileDeclarationASTNode> getAstCache() {
        return astCache;
    }

    /**
     * Gets the mapping a shader node source path to a AST presentation.
     *
     * @return the mapping a shader node source path to a AST presentation.
     */
    public Map<ShaderNode, FileDeclarationASTNode> getShaderNodeSources() {
        return shaderNodeSources;
    }

    /**
     * Gets the list of define value nodes.
     *
     * @return the list of define value nodes.
     */
    public List<DefineValueASTNode> getDefineValueNodes() {
        return defineValueNodes;
    }

    /**
     * Gets the list of extensions.
     *
     * @return the list of extensions.
     */
    public List<ExtensionPreprocessorASTNode> getExtensions() {
        return extensions;
    }

    /**
     * Gets the result list of declared uniforms in imported shaders.
     *
     * @return the result list of declared uniforms in imported shaders.
     */
    public List<ExternalFieldDeclarationASTNode> getImportedGlobalUniforms() {
        return importedGlobalUniforms;
    }

    /**
     * Gets the list of declared uniforms in imported shaders.
     *
     * @return the list of declared uniforms in imported shaders.
     */
    public List<ExternalFieldDeclarationASTNode> getImportedUnforms() {
        return importedUnforms;
    }

    /**
     * Gets the list of local variables.
     *
     * @return the list of local variables.
     */
    public List<LocalVarDeclarationASTNode> getLocalVars() {
        return localVars;
    }

    /**
     * Gets the list of methods.
     *
     * @return the list of methods.
     */
    public List<MethodDeclarationASTNode> getMethods() {
        return methods;
    }

    /**
     * Gets the list of defined variables.
     *
     * @return the list of defined variables.
     */
    public List<String> getDefinedVariables() {
        return definedVariables;
    }

    /**
     * Gets the list of imports.
     *
     * @return the list of imports.
     */
    public List<String> getImports() {
        return imports;
    }

    /**
     * Gets the list of result defines.
     *
     * @return the list of result defines.
     */
    public List<String> getResultDefines() {
        return resultDefines;
    }

    /**
     * Gets the technique definition.
     *
     * @return the technique definition.
     */
    public TechniqueDef getTechniqueDef() {
        return techniqueDef;
    }

    /**
     * Sets the technique definition.
     *
     * @param techniqueDef the technique definition.
     */
    public void setTechniqueDef(final TechniqueDef techniqueDef) {
        this.techniqueDef = techniqueDef;
    }

    /**
     * Gets the current indent.
     *
     * @return the current indent.
     */
    public int getIndent() {
        return indent;
    }

    /**
     * Sets the new indent.
     *
     * @param indent the new indent.
     */
    public void setIndent(final int indent) {
        this.indent = indent;
    }
}
