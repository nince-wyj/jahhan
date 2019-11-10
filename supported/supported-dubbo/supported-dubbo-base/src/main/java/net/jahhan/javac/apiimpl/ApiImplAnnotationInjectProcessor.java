package net.jahhan.javac.apiimpl;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import net.jahhan.lombok.annotation.ApiImpl;

@SupportedAnnotationTypes(value = { ApiImpl.className })
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
public class ApiImplAnnotationInjectProcessor extends AbstractProcessor {
	private Messager messager;
	private Trees trees;
	private TreeMaker make;
	private Names names;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);
		this.trees = Trees.instance(env);
		this.messager = processingEnv.getMessager();
		Context context = ((JavacProcessingEnvironment) env).getContext();
		this.make = TreeMaker.instance(context);
		this.names = Names.instance(context);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (TypeElement typeElement : annotations) {
			Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(typeElement);

			for (Element element : annotatedElements) {
				messager.printMessage(Diagnostic.Kind.NOTE,
						"===============" + ((TypeElement) element).getQualifiedName());
				if (element.getKind() == ElementKind.CLASS) {
					JCTree tree = (JCTree) trees.getTree(element);
					tree.accept(new TreeTranslator() {
						@Override
						public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
							super.visitClassDef(jcClassDecl);
							jcClassDecl.defs = jcClassDecl.defs.prepend(makeInnerClassDecl(jcClassDecl));
						}
					});
				}
			}
		}
		return true;
	}

	private JCTree.JCAnnotation makeInnerClassDecl(JCTree.JCClassDecl jcVariableDecl) {
		JCAnnotation annotation = make.Annotation(make.Ident(names.fromString("com.alibaba.dubbo.config.annotation.Service")),
				List.<JCExpression> nil());
		return annotation;
	}

}