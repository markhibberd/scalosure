package s2js

import scala.reflect.generic.Flags._

import scala.collection.mutable.{
    ListBuffer, StringBuilder
}

import scala.xml.Utility
import scala.tools.nsc.Global
import scala.util.matching.Regex

//import scala.tools.nsc.symtab.Flags._

trait S2JSPrinter {
  
    val global:Global

    import global._
	import definitions._

    def debug(name:String, thing:Any) = {
        print(name+" ")
        println(thing.toString)
    }

    case class RichTree(t:Tree) {
        val ownerName = t.symbol.owner.fullName
        val nameString = t.symbol.nameString
        val inModuleClass = t.symbol.owner.isModuleClass
    }

    implicit def tree2richtree(tree:Tree):RichTree = RichTree(tree)

	object BinaryOperator {
        def unapply(name:Name):Option[String] = Map(
            "$eq$eq"		-> "==",
            "$bang$eq" 		-> "!=",
            "$greater"		-> ">",
            "$greater$eq" 	-> ">=",
            "$less"			-> "<",
            "$less$eq"		-> "<=",
            "$amp$amp"		-> "&&",
            "$plus"		    -> "+",
            "$minus"		-> "-",
            "$bar$bar"		-> "||").get(name.toString)
    }

    def tree2string(tree:Tree):String = {

        val lb = new ListBuffer[String]

        /* Start off with calculating and build provide statements
         *   - isSynthetic allows to ignore default constructors
         */

        tree.children filter {
            x => (x.isInstanceOf[PackageDef] || x.isInstanceOf[ClassDef] || x.isInstanceOf[ModuleDef]) && !x.symbol.isSynthetic
        } foreach { 
            x => lb += "goog.provide('%s');".format(x.symbol.fullName) 
        }

        // determine the necessary dependencies for requires statements
        findRequiresFrom(tree) foreach {
            x => lb += "goog.require('%s');".format(x)
        }

        // this starts the big traverse
        lb += tree.children.map(buildPackageLevelItem).mkString

        lb.mkString("\n")
    }

    def buildPackageLevelItem(t:Tree):String = t match {
        case x @ ClassDef(_, _, _, _) => if(x.symbol.isTrait) buildClass(x) else buildClass(x)
        case x @ ModuleDef(_, _, Template(_, _, body)) => body.map(buildPackageLevelItemMember).mkString("\n")
        case x @ PackageDef(pid, stats) => stats.map(buildPackageLevelItemMember).mkString("\n")
        case x =>  ""
    }

    def buildPackageLevelItemMember(t:Tree):String = t match {
        case x @ DefDef(mods, _, _, _, _, _) if(!mods.isAccessor && !x.symbol.isConstructor && !x.symbol.isSynthetic) => 
            buildMethod(x, x.symbol.owner.isPackageObjectClass)
        case x @ ValDef(_, _, _, _) => 
            buildField(x, x.symbol.owner.isPackageObjectClass)
        case x @ ModuleDef(_, _, _) =>
            buildPackageLevelItem(x)
        case x =>  ""
    }

    def buildClass(t:ClassDef):String = {

        val l = new ListBuffer[String]

        val className = t.symbol.fullName

        // only support single inheritence and ignore java.lang.Object
        val superClassName = t.impl.parents.map(_.symbol.fullName).headOption flatMap {
            x => if(x != "java.lang.Object") Some(x) else None
        }

        val cosmicNames = List("java.lang.Object", "scala.ScalaObject", "scala.Any")

        def isCosmicType(x:Tree):Boolean = cosmicNames.contains(x.symbol.fullName)
        def isLocalMember(x:Symbol):Boolean = x.isLocal
        def isCosmicMember(x:Symbol):Boolean = cosmicNames.contains(x.enclClass.fullName)

        def isIgnoredMember(x:Symbol):Boolean = 
            isCosmicMember(x) || 
            x.isConstructor || 
            x.hasFlag(ACCESSOR)

        val ctorDef = t.impl.body.filter {
            x => x.isInstanceOf[DefDef] && x.symbol.isPrimaryConstructor
        }.head.asInstanceOf[DefDef]

        val ctorArgs = ctorDef.vparamss.flatten.map(_.symbol.nameString)

        l += "/** @constructor*/"
        l += "%s = function(%s) {".format(className, ctorArgs.mkString(","))

        l += "var self = this;"

        // superclass construction and field initialization
        t.impl.foreach {
            case x @ Apply(Select(Super(qual, mix), name), args) if(name.toString == "<init>") => superClassName match {
                case Some(y) => 
                    val filteredArgs = args.filter(!_.toString.contains("$default$")).map(_.toString)
                    l += "%s.call(%s);".format(y, (List("self") ++ filteredArgs).mkString(","))
                    ctorArgs.diff(filteredArgs).foreach {
                        y => l += "self.%1$s = %1$s;".format(y)
                    }
                case None => 
                    ctorArgs.foreach {
                        y => l += "self.%1$s = %1$s;".format(y)
                    }
            }
            case x => 
        }

        t.impl.body foreach {
            case x @ Apply(fun, args) => l += buildTree(x)+";"
            case x @ ClassDef(_, _, _, _) => l += buildClass(x)
            case x =>
        }

        l += "};"

        superClassName.foreach {
            x => l += "goog.inherits(%s, %s);".format(className, x)
        }

        val baseTypes = t.impl.parents filterNot { isCosmicType } filter { _.symbol.isTrait }

        val mixinMembers = baseTypes map { 
            t => t.tpe.members filterNot { isIgnoredMember } map { m => (m.owner.fullName, m.nameString) }
        }

        mixinMembers.flatten foreach {
            x => l += "%s.prototype.%s = %s.prototype.%s;".format(className, x._2, x._1, x._2)
        }

        l ++= t.impl.body.map(buildPackageLevelItemMember)

        return l.mkString("\n")
    }

    def buildTree(t:Tree):String = t match {

        case x @ Literal(Constant(value)) => value match {
            case v:String => "'"+v+"'"
            case v:Unit => ""
            case null => "null"
            case v => v.toString
        }

        case x @ Return(expr) => 
            "return "+buildTree(expr)+";"

        case x @ Apply(TypeApply(y @ Select(Select(_, n), _), _), args) if(n.toString == "Array") => 
            args.map(buildObjectLiteral).mkString("[",",","]")

        case x @ Apply(TypeApply(y @ Select(Select(_, n), _), _), args) if(n.toString == "Map") => 
            args.map(buildObjectLiteral).mkString("{",",","}")

        case x @ Apply(TypeApply(Select(q, n), _), args) if(q.symbol.nameString == "refArrayOps") =>
            val arrayName = q.asInstanceOf[ApplyImplicitView].args.head
            "goog.array.forEach(%s, %s, self)".format(arrayName, args.map(buildTree).mkString)

        case x @ Apply(Select(qualifier, BinaryOperator(op)), args) =>
            "(%s %s %s)".format(buildTree(qualifier), op, args.map(buildTree).mkString)

        case x @ Apply(Select(qualifier, name), args) if name.toString.endsWith("s2js.Html") =>
            "html"

        case x:ApplyToImplicitArgs => x.fun match {
            case y => buildTree(y)
        }

        case x @ Apply(Select(qualifier, name), args) if qualifier.toString == "s2js.Html" =>
            "%s".format(buildXmlLiteral(args.head).mkString)

        case x @ Apply(Select(qualifier, name), args) if name.toString.endsWith("_$eq") =>
            "%s.%s = %s".format(buildTree(qualifier), 
                name.toString.stripSuffix("_$eq"), args.map(buildTree).mkString)

        case x @ Apply(Select(y @ Super(_, _), name), args) =>
            "%s.superClass_.%s.call(%s)".format(y.symbol.fullName, name.toString, (List("self") ++ args.map(buildTree)).mkString(","))

        case x @ Apply(fun, args) =>

            val filteredArgs = args.filter {
                case y @ (TypeApply(_,_) | Select(_,_)) => !y.symbol.hasFlag(DEFAULTPARAM)
                case y => true
            }

            //val transformedArgs = filteredArgs map {
                //case y @ Block(_, Function(_, body)) => y.tpe match {
                    //case z @ TypeRef(_, _, _) => if(body.symbol.owner.isModuleClass) {
                        //body.symbol.fullName
                    //} else {
                        //"this."+body.symbol.simpleName
                    //}
                    //case z => "#WRONG#"
                //}
                //case y => buildTree(y)
            //}

            "%s(%s)".format(buildTree(fun), filteredArgs.map(buildTree).mkString(","))

        case x @ TypeApply(Select(q, n), args) if(n.toString == "asInstanceOf") => 
            q.toString

        case x @ TypeApply(fun, args) => 
            buildTree(fun)

        case x @ ValDef(mods, name, tpt, rhs) if(x.symbol.isLocal) =>

            "var %s = %s".format(
                x.symbol.nameString, 
                rhs match {
                    case y @ Match(_, _) => buildSwitch(y, true)
                    case y => buildTree(y)
                })

        case x @ Ident(name) => name.toString
            
        case x @ If(cond, thenp, elsep) =>

            val transformedThen = thenp match {
                case y @ Block(_, _) => buildBlock(y)
                case y => buildTree(y)+";"
            }

            val transformedElse = elsep match {
                case y @ Block(_, _) => buildBlock(y)
                case y => buildTree(y)+";"
            }

            "%s ? function() { %s }() : function() { %s }()".format(buildTree(cond), transformedThen, transformedElse)

        case x @ Function(vparams, body) =>
            val args = vparams.map(_.symbol.nameString).mkString(",")
            val impl = body match {
                case y @ Block(stats, expr) =>
                    val bStats = stats.map(buildTree).mkString("", ";\n", ";")
                    bStats + buildTree(expr) match {
                        case z if(y.tpe.toString == "Unit") => 
                            if(z == "") "" else "%s;".format(z)
                        case z => 
                            "return %s;".format(z)
                    }
                case y => 
                    buildTree(body) match {
                        case z if(y.tpe.toString == "Unit") => 
                            if(z == "") "" else "%s;".format(z)
                        case z => 
                            "return %s;".format(z)
                    }
            }
            "function(%s) {%s}".format(args, impl)

        case EmptyTree => "null"

        case x @ Select(qualifier, name) if(name.toString == "package") => 
            buildTree(qualifier)

        case x @ Select(qualifier, name) => qualifier match {
            case y @ New(tt) => "new " + tt.tpe.baseClasses.head.fullName
            case y @ Ident(_) if(name.toString == "apply") => if(y.symbol.isLocal) y.symbol.nameString else y.symbol.fullName
            case y @ Ident(_) if(y.name.toString == "browser") => name.toString
            case y @ Ident(_) => if(y.symbol.isLocal) y.symbol.nameString+"."+name.toString else y.symbol.fullName+"."+name.toString
            case y @ This(_) if(x.symbol.owner.isPackageObjectClass) => y.symbol.owner.fullName+"."+name
            case y @ This(_) if(x.symbol.owner.isModuleClass) => y.symbol.fullName+"."+name
            case y @ This(_) => "self."+name
            case y @ Select(q, n) if(n.toString == "Predef" && name.toString == "println") => "console.log"
            case y => buildTree(y) + "." +name
        }

        case x @ Block(stats, expr) => stats.map(buildTree).mkString("", ";", "") + buildTree(expr)

        case x @ This(n) => if(x.symbol.isModuleClass)  n.toString else "self"

        case x @ Assign(lhs, rhs) => "%s = %s".format(buildTree(lhs), buildTree(rhs))

        case x => x match {
            case y @ Match(_, _) => buildSwitch(y, false)
            case y @ TypeApply(fun, args) => ""
            case y => println(y.getClass); "#NOT IMPLEMENTED#"
        }
    }
    
    def buildBlock(t:Block):String = {

        val stats = t.stats.map(buildTree).mkString("", ";\n", ";")

        val expr = buildTree(t.expr) match {
            case z if(t.tpe.toString == "Unit") => 
                if(z == "") "" else "%s;".format(z)
            case z => 
                "return %s;".format(z)
        }

        stats + expr
    }

    def buildSwitch(t:Tree, hasReturn:Boolean):String = t match {

        case x @ Match(selector, cases) => 

            val theSwitch = "switch(%s) {%s}".format(
                buildTree(selector), 
                cases.map(y => buildSwitch(y, hasReturn)).mkString)

            if(hasReturn)
                "function() {%s}()".format(theSwitch)
            else theSwitch


        case x @ CaseDef(path, guard, body) =>
            
            val part = path match {
                case Ident(n) if(n.toString == "_") => "default"
                case _ => "case %s".format(buildTree(path))
            }

            if(hasReturn) {
                "%s: return %s;".format(part, buildTree(body))
            } else {
                "%s: %s;break;".format(part, buildTree(body))
            }
    }

    def findRequiresFrom(tree:Tree):Set[String] = {
        
        val s = new collection.mutable.LinkedHashSet[String] 

        var currentFile:scala.tools.nsc.io.AbstractFile = null
        val thingsToIgnore = List("s2js.Html", "ClassManifest", "scala", "java.lang", "scala.xml", "$default$", "browser")

        def traverse(t:Tree):Unit = t match {

            // check the body of a class
            case x @ Template(parents, self, body) => 

                currentFile = x.symbol.sourceFile

                parents.foreach {
                    y => if(!thingsToIgnore.exists(y.symbol.fullName.contains)) {
                        if(currentFile != y.symbol.sourceFile) {
                            s += y.symbol.fullName
                        }
                    } 
                }

                body.foreach(traverse)
                
            case x @ ValDef(_, _, _, rhs) => 

                rhs match {
                    case y @ Block(stats, expr) => 
                        stats.foreach(traverse)
                        traverse(expr)
                    case y => 
                        traverse(y)
                }

            case x @ DefDef(_, _, _, vparamss, _, rhs) =>

                // TODO: maybe need to dive into vparamss

                rhs match {
                    case y @ Block(stats, expr) => 
                        stats.foreach(traverse)
                        traverse(expr)
                    case y => traverse(y)
                }

            case x @ Apply(Select(q, _), args) if(q.toString.endsWith("Predef")) =>
                args.foreach(traverse)

            // make sure we check all function calls for needed imports
            case x @ Apply(fun, args) =>

                if(fun.toString.contains("scala.Array")) {
                    s += "goog.array"
                } else if(!thingsToIgnore.exists(fun.symbol.fullName.contains)) {
                    traverse(fun)
                }

                args.foreach(traverse)

            case x @ Select(q, _) if(q.toString.endsWith("package")) =>
                if(!thingsToIgnore.exists(x.symbol.fullName.contains)) {
                    s += q.symbol.owner.fullName
                }

            case x @ Select(New(tpe), _) => tpe match {
                case y @ Select(Select(_, _), _) => 
                    s += y.symbol.fullName
                case y @ TypeTree() => 
                    s += y.symbol.fullName
                case y => 
                    if(currentFile != y.symbol.sourceFile) {
                        s += y.symbol.fullName
                    }
            }

            case x @ Select(Select(_, _), _) if(!x.symbol.isPackage) =>

                if(!thingsToIgnore.exists(x.symbol.fullName.contains)) {
                    s += x.symbol.owner.fullName
                }

            case x @ Select(_, _) => 
                
            case x @ Ident(name) =>
            
            case x => 
                x.children.foreach(traverse)
        }

        traverse(tree)

        s.toSet
    }

    def buildField(tree:ValDef, inPackageObject:Boolean=false):String = { 

        val className = trimNamespace(tree.ownerName)
        val name = tree.nameString
        val rhs = buildTree(tree.rhs)

        if(tree.symbol.owner.isModuleClass) {
            "%s.%s = %s;".format(className, name, rhs)
        } else {
            "%s.prototype.%s = %s;".format(className, name, rhs)
        }
    }

    def trimNamespace(ns:String) = if(ns.endsWith(".package")) {
        ns.stripSuffix(".package")
    } else ns

    def buildMethod(ts:DefDef, inPackageObject:Boolean=false):String = { 

        val ns = trimNamespace(ts.symbol.owner.fullName)
        val name = ts.symbol.nameString
        val args = ts.vparamss.flatten.map(_.symbol.nameString).mkString(",")

        val l = new ListBuffer[String]

        if(ts.symbol.owner.isModuleClass) {
            l += "%s.%s = function(%s) {".format(ns, name, args)
        } else {
            l += "%s.prototype.%s = function(%s) {".format(ns, name, args)
        }

        // every method gets a self refrence
        l += "var self = this;"

        ts.rhs match {

            case y @ Block(stats, expr) => 

                l += stats.map(buildTree).mkString("", ";\n", ";")

                l += (buildTree(expr) match {
                    case z if(ts.tpt.symbol.nameString == "Unit") => 
                        if(z == "") "" else "%s;".format(z)
                    case z => 
                        "return %s;".format(z)
                })

            case y => 

                l += (buildTree(y) match {
                    case z if(ts.tpt.symbol.nameString == "Unit") => 
                        if(z == "") "" else "%s;".format(z)
                    case z => "return %s;".format(z)
                })
        }

        l += "};"

        if(ts.symbol.annotations exists {_.toString == "s2js.ExportSymbol"}) {
            l += "goog.exportSymbol('%1$s', %1$s);".format(ns+"."+name)
        }

        l.mkString("\n")
    }

    def buildXmlLiteral(t:Tree):String = t match {

        case x @ Block(_, inner @ Block(stats, a @ Apply(_,_))) => a match {

            case y @ Apply(Select(New(tpt), _), args) if(tpt.toString == "scala.xml.Elem") =>

                val tag = args(1).toString.replace("\"", "")

                val attributes = stats.filter {
                    case Assign(_, _) => true
                    case _ => false
                }.map(buildXmlLiteral).mkString("{",",","}")

                val children = if(args.length > 4) {
                    buildXmlLiteral(args(4))
                } else "[]"
                
                "goog.dom.createDom('%s',%s,%s)".format(
                    tag, attributes, children)

            case y => "nothing"

        }

        case x @ Typed(Block(stats, expr), tpt) => 
        
            stats.filter {
                case Apply(_,_) => true
                case _ => false
            }.map(buildXmlLiteral).filter(_ != "").mkString("[",",","]")

        case x @ Apply(fun, args) if(fun.symbol.fullName == "scala.xml.NodeBuffer.$amp$plus") =>
            buildXmlLiteral(args.head)

        case x @ Apply(Select(New(tpt), _), args) if(tpt.toString == "scala.xml.Text") => 
            val value = args.head.toString.replaceAll("""(\\012|[ ]{2,}|[\r\n]|")""", "")
            if(value == "") "" else "'%s'".format(value)

        case x @ Assign(_, Apply(_, List(name, Apply(_,  List(value)), _))) =>
            val stripName = name.toString.replace("\"", "")
            "'%s':%s".format(stripName, buildTree(value))
            
        case x @ Assign(_, Apply(_, List(name, value @ Select(_, _), _))) => 
            val stripName = name.toString.replace("\"", "")
            "'%s':%s".format(stripName, buildTree(value))

        case x @ Assign(_, Apply(_, List(name, value @ Ident(_), _))) => 
            val stripName = name.toString.replace("\"", "")
            "'%s':%s".format(stripName, buildTree(value))

        case x => buildTree(x)
    }

    def buildObjectLiteral(t:Tree):String = t match {

        case x @ Literal(Constant(value)) => value match {
            case v:String => "'"+v+"'"
            case x:Unit => ""
            case v => v.toString
        }

        case x @ Apply(TypeApply(y @ Select(Select(_, n), _), _), args) if(n.toString == "Map") => 
            
            args.map(buildObjectLiteral).mkString("{",",","}")

        case x @ Apply(TypeApply(Select(q, n), _), args) if (n.toString == "$minus$greater") => 

            // this should be a string
            val key = q.asInstanceOf[ApplyImplicitView].args.head.toString.replace("\"", "'")

            // process nested objects
            val values = args map buildObjectLiteral

            "%s:%s".format(key, values.mkString)

        case x:ApplyToImplicitArgs => x.fun match {
            case y @ Apply(TypeApply(Select(n, _), _), as) if(n.toString == "scala.Array") => 
                as.map(buildObjectLiteral).mkString("[", ",", "]")
            case y => y.getClass.toString
        }

        case x => buildTree(x)
    }
}

// vim: set ts=4 sw=4 et:
