package s2js

import scala.reflect.generic.Flags._

import scala.collection.mutable.{
    ListBuffer, StringBuilder
}

import scala.xml.Utility
import scala.tools.nsc.Global
import scala.util.matching.Regex

trait S2JSPrinter {
  
    val global:Global

    import global._

    def debug(name:String, thing:Any) {
        //print(name+" ")
        //println(thing.toString)
    }

    case class RichTree(t:Tree) {
        val ownerName = t.symbol.owner.fullName
        val nameString = t.symbol.nameString
        val inModuleClass = t.symbol.owner.isModuleClass
    }

    implicit def tree2richtree(tree:Tree):RichTree = RichTree(tree)

    val cosmicNames = List("java.lang.Object", "scala.ScalaObject", "scala.Any", "scala.AnyRef", "scala.Product")

    def isCosmicType(x:Tree):Boolean = cosmicNames.contains(x.symbol.fullName)
    def isLocalMember(x:Symbol):Boolean = x.isLocal
    def isCosmicMember(x:Symbol):Boolean = cosmicNames.contains(x.enclClass.fullName)

    object BinaryOperator {
        def unapply(name:Name):Option[String] = Map(
            "$eq$eq"      -> "==",
            "$bang$eq"    -> "!=",
            "$greater"    -> ">",
            "$greater$eq" -> ">=",
            "$less"       -> "<",
            "$less$eq"    -> "<=",
            "$amp$amp"    -> "&&",
            "$plus"       -> "+",
            "$minus"      -> "-",
            "$percent"    -> "%",
            "$bar$bar"    -> "||").get(name.toString)
    }

    def tree2string(tree:Tree):String = {

        debug("f:1", tree)

        // using a list buffer for simplicity and to add hard returns for formatting
        val lb = new ListBuffer[String]

        // identifying all the top-level members of the file, which translate to provide statements in closure-library terms
        val provideList = tree.children filter {
            x => (x.isInstanceOf[PackageDef] || x.isInstanceOf[ClassDef] || x.isInstanceOf[ModuleDef]) && !x.symbol.isSynthetic
        }
        
        val provideSet = provideList.foldLeft(Set.empty[String]) { 
            (a,b) => a + "goog.provide('%s');\n".format(b.symbol.fullName) 
        }

        lb += provideSet.mkString

        // determine the necessary dependencies for requires statements
        findRequiresFrom(tree) foreach {
            x => lb += "goog.require('%s');\n".format(x)
        }

        // this starts the big traverse
        // TODO: should be able to do this in the buildTree function
        lb += tree.children.map(buildPackageLevelItem).mkString

        lb.mkString
    }

    def buildPackageLevelItem(t:Tree):String = t match {
        case x @ ClassDef(_, _, _, _) => buildClass(x)
        case x @ ModuleDef(_, _, Template(_, _, body)) if(!x.symbol.hasFlag(SYNTHETIC)) => body.map(buildPackageLevelItemMember).mkString
        case x @ PackageDef(_, stats) => stats.map(buildPackageLevelItemMember).mkString
        case x =>  ""
    }

    def isDefaultNull(t:DefDef):Boolean = !(t.symbol.nameString.contains("default$") && t.rhs.toString == "null") && !t.mods.isAccessor && !t.symbol.isConstructor

    def buildPackageLevelItemMember(t:Tree):String = t match {
      case x @ DefDef(mods, _, _, _, _, rhs) if isDefaultNull(x) => buildMethod(x, x.symbol.owner.isPackageObjectClass)
      case x @ ValDef(_, _, _, _) => buildField(x, x.symbol.owner.isPackageObjectClass)
      case x @ ModuleDef(_, _, _) => buildPackageLevelItem(x)
      case x =>  ""
    }

    def buildClass(t:ClassDef):String = {

        val lb = new ListBuffer[String]

        val className = t.symbol.fullName

        val superClassName = t.impl.parents filterNot { isCosmicType } headOption

        if(t.symbol.isTrait) {

            lb += "/** @constructor*/\n"
            lb += "%s = function() {};\n".format(className)

        } else {

            val ctorDef = t.impl.body.filter {
                x => x.isInstanceOf[DefDef] && x.symbol.isPrimaryConstructor
            }.head.asInstanceOf[DefDef]

            val ctorArgs = ctorDef.vparamss.flatten.map(_.symbol.nameString)

            lb += "/** @constructor*/\n"
            lb += "%s = function(%s) {\n".format(className, ctorArgs.mkString(","))

            lb += "var self = this;\n"

            // superclass construction and field initialization
            t.impl.foreach {
                case x @ Apply(Select(Super(qual, mix), name), args) if(name.toString == "<init>") => superClassName match {
                    case Some(y) => 
                        val filteredArgs = args.filter(!_.toString.contains("$default$")).map(_.toString)
                        lb += "%s.call(%s);\n".format(y.symbol.fullName, (List("self") ++ filteredArgs).mkString(","))
                        ctorArgs.diff(filteredArgs).foreach {
                            y => lb += "self.%1$s = %1$s;\n".format(y)
                        }
                    case None => 
                        ctorArgs.foreach {
                            y => lb += "self.%1$s = %1$s;\n".format(y)
                        }
                }
                case x => 
            }

            t.impl.body foreach {
                case x @ Apply(fun, args) => lb += buildTree(x)+";\n"
                case x @ ClassDef(_, _, _, _) => lb += buildClass(x)
                case x =>
            }

            lb += "};\n"
        }

        superClassName.foreach {
          x => lb += "goog.inherits(%s, %s);\n".format(className, if(x.symbol.isTrait) "ScalosureObject" else x.symbol.fullName)
        }

        val traits = t.impl.parents filterNot { isCosmicType } filter { _.symbol.isTrait }

        def isIgnoredMember(x:Symbol):Boolean = 
            isCosmicMember(x) || 
            x.isConstructor || 
            x.hasFlag(ACCESSOR)

        val traitMembers = traits map { 
            x => x.tpe.members filterNot { isIgnoredMember } map { m => (m.owner.fullName, buildName(m)) }
        }

        traitMembers.flatten foreach {
            x => lb += "%s.prototype.%s = %s.prototype.%s;\n".format(className, x._2, x._1, x._2)
        }

        val caseMemberNames = List("productPrefix", "productArity", "productElement", "equals", "toString", "canEqual", "hashCode", "copy")

        def isCaseMember(x:Tree):Boolean = caseMemberNames.exists(x.symbol.fullName.endsWith(_))

        def isSynthetic(x:Tree):Boolean = x.symbol.isSynthetic

        if(t.symbol.hasFlag(CASE)) {
            lb ++= t.impl.body filterNot { isCaseMember } map { buildPackageLevelItemMember }
        } else {
            lb ++= t.impl.body.map(buildPackageLevelItemMember)
        }

        return lb.mkString
    }

    def buildTree(t:Tree):String = t match {

        case x @ Literal(Constant(value)) => value match {
            case v:String => "'"+v+"'"
            case v:Unit => ""
            case null => "null"
            case v => v.toString
        }

        case x @ Return(expr) => "return "+buildTree(expr)

        case x @ Apply(TypeApply(y @ Select(Select(_, n), _), _), args) if(n.toString == "Array") =>
          args.map(buildObjectLiteral).mkString("[",",","]")

        case x @ Apply(TypeApply(y @ Select(Select(_, n), _), _), args) if(n.toString.matches("Tuple[0-9]+")) => 
          args.zipWithIndex map { 
            a => "'_%s':%s".format((a._2+1), a._1.toString.replace("\"", "'")) 
          } mkString("{",",","}")

        case x @ Apply(Select(q, n), args) if q.toString.matches("s2js.JsObject") => args.headOption match {
          case Some(Typed(expr, tpt)) if tpt.toString == "_*" => expr.toString
          case _ => args map { buildObjectLiteral } mkString("{",",","}")
        }

        case x @ Apply(Select(q, n), args) if q.toString.matches("s2js.JsArray") => args map {
          buildObjectLiteral
        } mkString("[",",","]")

        case x @ Apply(Select(qualifier, BinaryOperator(op)), args) => "(%s %s %s)".format(
          buildTree(qualifier), op, args.map(buildTree).mkString)

        case x @ Apply(Select(qualifier, name), args) if name.toString.endsWith("s2js.Html") => "html"

        case x:ApplyToImplicitArgs => x.fun match {
          case y => buildTree(y)
        }

        case x @ Apply(fun @ Select(q, n), args) if fun.toString == "scalosure.script.literal" =>
          args.mkString.replace("\"", "")

        case x @ Apply(Select(qualifier, name), args) if qualifier.toString == "s2js.Html" =>
            "%s".format(buildXmlLiteral(args.head).mkString)

        case x @ Apply(Select(qualifier, name), args) if name.toString.endsWith("_$eq") =>
            "%s.%s = %s".format(buildTree(qualifier), 
                name.toString.stripSuffix("_$eq"), args.map(buildTree).mkString)

        case x @ Apply(Select(y @ Super(_, _), name), args) =>
            "%s.superClass_.%s.call(%s)".format(y.symbol.fullName, name.toString, (List("self") ++ args.map(buildTree)).mkString(","))

        case x @ Apply(TypeApply(f, _), args) if(f.symbol.owner.nameString == "ArrowAssoc") => 
          "{%s}".format(buildObjectLiteral(x))

        case x @ Apply(TypeApply(fun @ Select(q, n), _), args) if fun.hasSymbol && fun.symbol.owner.nameString == "JsObject" => {
          args.headOption match {
            case Some(Typed(expr, tpt)) if tpt.toString == "_*" => expr.toString
            case _ => args map { buildObjectLiteral } mkString("{",",","}")
          }
        }

        case x @ Apply(fun, args) =>

            debug("=========================", "")
            debug("f:2a", x)
            //debug("f:2b", fun)
            //debug("f:2c", fun.getClass)

            val argumentList = x.symbol.paramss

            def buildArgs(t:Tree):List[Tree] = t match {
              case Apply(f, xs) => buildArgs(f) ++ xs
              case _ => Nil
            }

            val passedArgs = (buildArgs(fun) ++ args)

            val processedArgs = passedArgs.zip(x.symbol.paramss.flatten) map { 
              case (passed, defined) if defined.tpe.typeSymbol.nameString.matches("""(Function0|\<byname\>)""") => "function() {%s}".format(buildTree(passed))
              case (passed, defined) => buildTree(passed)
            }

            def ownerName(t:Tree) = if(fun.hasSymbol) Some(fun.symbol.owner.nameString) else  None

            val tmp = ownerName(fun) match {
                case Some("Array" | "MapLike") => "%s[%s]"
                case _ => "%s(%s)"
            }

            def buildApply(f:Tree) = tmp.format(buildTree(f), processedArgs.mkString(","))

            def isVarArgs(t:Tree):Boolean = t.tpe.params.headOption match {
              case Some(firstParam) => firstParam.tpe.toString.matches("""\(String, [^)].*\)\*""")
              case None => false
            }

            def isArrayArg(t:Tree):Boolean = t.tpe.params.headOption match {
              case Some(firstParam) => firstParam.tpe.toString.matches("""[a-zA-Z0-9]+\*""")
              case None => false
            }

            fun match {
                case TypeApply(f @ Select(_, _), _) if isVarArgs(f) => {
                  "%s(%s)".format(buildTree(f), args map { buildObjectLiteral } mkString("{",",","}"))
                }
                case TypeApply(f @ Select(_, _), _) if isArrayArg(f) => {
                  "%s(%s)".format(buildTree(f), args map { buildObjectLiteral } mkString("[",",","]"))
                }
                case Apply(f @ Select(_, _), _) if isVarArgs(f)  => {
                  "%s(%s)".format(buildTree(f), args map { buildObjectLiteral } mkString("{",",","}"))
                }
                case Apply(f @ Select(_, _), _) if isArrayArg(f)  => {
                  "%s(%s)".format(buildTree(f), args map { buildObjectLiteral } mkString("[",",","]"))
                }
                case Apply(f, xs) => buildApply(f)
                case f @ Select(_, _) if isVarArgs(f) => {
                  "%s(%s)".format(buildTree(f), args map { buildObjectLiteral } mkString("{",",","}"))
                }
                case f @ Select(_, _) if isArrayArg(f) => {
                  "%s(%s)".format(buildTree(f), args map { buildObjectLiteral } mkString("[",",","]"))
                }
                case y => debug("f:2c", y); buildApply(fun)
            }

        case x @ TypeApply(Select(q, n), args) if(n.toString == "asInstanceOf") => buildTree(q)

        case x @ TypeApply(fun, args) => buildTree(fun)

        case x @ ValDef(mods, name, tpt, rhs) if(x.symbol.isLocal) =>

            "var %s = %s".format(
                x.symbol.nameString, 
                rhs match {
                    case y @ Match(_, _) => buildSwitch(y, true)
                    case y @ Select(q, n) if(n.toString == "unary_$bang") => "!"+buildTree(q)
                    case y => buildTree(y)
                })

        case x @ Ident(name) => if(x.symbol.isLocal) x.symbol.nameString else x.symbol.fullName

        case x @ If(cond, thenp, elsep) =>

            val transformedThen = thenp match {
                case y @ Block(_, _) => buildBlock(y)
                case y => buildTree(y)+";\n"
            }

            val transformedElse = elsep match {
                case y @ Block(_, _) => buildBlock(y)
                case y => buildTree(y)+";\n"
            }

            "%s ? function() {\n%s}() : function() {\n%s}()".format(buildTree(cond), transformedThen, transformedElse)

        case x @ Function(vparams, body) =>
            val args = vparams.map(_.symbol.nameString).mkString(",")

            val impl = body match {
                case y @ Block(_, _) => buildBlock(y)
                case y => buildExpression(y)
            }

            "function(%s) {\n%s}".format(args, impl)

        case EmptyTree => "null"

        case x @ Select(qualifier, name) if(name.toString == "package") => buildTree(qualifier)

        case x @ Select(qualifier, name) => debug("f:3a", qualifier + ": " + qualifier.getClass); qualifier match {
            case y @ New(tt) => debug("f:3b", tt.symbol.fullName); "new " + (if(tt.toString.startsWith("browser")) tt.symbol.nameString else scala2scalosure(tt.symbol))
            case y @ Ident(_) if(name.toString == "apply" && (x.symbol.owner.isSynthetic || x.symbol.owner.nameString == "JsObject")) => debug("f:3c", y); "%s.$apply".format(
              if(y.symbol.isLocal) y.symbol.nameString else y.symbol.fullName)
            case y @ Ident(_) if(y.name.toString == "browser") => debug("f:3d", y); name.toString
            case y @ Ident(_) if name.toString == "apply" && y.symbol.isModule => debug("f:3e", y); if(y.symbol.isLocal) y.symbol.nameString+"."+translateName(name) else y.symbol.fullName+"."+translateName(name)
            case y @ Ident(_) if name.toString == "apply" => debug("f:3f", y.tpe); if(y.symbol.isLocal) y.symbol.nameString else y.symbol.fullName
            case y @ This(_) if(x.symbol.owner.isPackageObjectClass) => debug("f:3g", y.tpe); y.symbol.owner.fullName+"."+name
            case y @ This(_) if(x.symbol.owner.isModuleClass) => debug("f:3h", y.tpe); y.symbol.fullName+"."+name
            case y @ This(_) => debug("f:3i", y.tpe); "self."+name
            case y @ Select(q, n) if name.toString == "apply" && y.symbol.isModule => debug("f:3j", y.tpe); if(y.symbol.isLocal) {
              y.symbol.nameString.replace("scala", "scalosure")+"."+translateName(name) 
            } else {
              scala2scalosure(y.symbol)+"."+translateName(name)
            }
            case y @ Select(q, n) if name.toString == "apply" => debug("f:3k", y.tpe); if(y.symbol.isLocal) {
              buildTree(y)+"."+translateName(name) 
            } else {
              buildTree(y)+"."+translateName(name)
            }
            case y @ Select(q, n) if(n.toString == "Predef" && name.toString == "println") => "console.log"
            case y if(name.toString == "$colon$plus" && y.symbol.nameString == "genericArrayOps") => "%s.push".format(
              y.asInstanceOf[ApplyImplicitView].args.head)
            case y if(name.toString == "unary_$bang") => "!"+buildTree(y)
            case y if(name.toString == "apply") => debug("f:3l", y.tpe); buildTree(y)
            case y if(name.toString == "any2ArrowAssoc") => buildTree(y)
            case y => debug("f:3m", y.tpe); buildTree(y)+"."+name
        }

        case x @ Block(stats, expr) => buildBlock(x)

        case x @ This(n) => if(x.symbol.isModuleClass)  n.toString else "self"

        case x @ Assign(lhs, rhs) => "%s = %s".format(buildTree(lhs), buildTree(rhs))

        // TODO: need to finish
        case x @ Try(block, catches, finalizer) => "try {\n%s\n} catch(err) {\n%s\n}".format(buildTree(block), "", "")

        case x @ LabelDef(name, params, rhs) if(name.toString.startsWith("while")) => 

            val If(cond, thenp, _) = rhs

            val transformedThen = thenp match {
                case y @ Block(stats, expr) => stats.map(buildTree).mkString
                case y => buildTree(y)
            }

            "while(%s) {%s}".format(buildTree(cond), transformedThen)

        case x => x match {
            case y @ Match(_, _) => buildSwitch(y, false)
            case y @ TypeApply(fun, args) => ""
            case y @ Typed(expr, tpt) if tpt.toString == "_*" => expr.toString
            case y => println(y.getClass); "#NOT IMPLEMENTED#"
        }
    }
    
    def buildExpression(t:Tree):String =  buildTree(t) match {
        case z if(t.tpe.toString == "Unit") => 
            if(z == "") "" else "%s;\n".format(z)
        case z => 
            "return %s;\n".format(z)
    }

    def buildBlock(t:Block):String = {
        val stats = t.stats map { buildTree } map { _ + ";\n" }
        
        val expr = t.expr match {
          case x @ Function(_, _) => Some(buildTree(t.expr)) 
          case x => buildTree(x) match {
            case z if t.tpe.toString == "Unit" => if(z == "") None else Some("%s;\n".format(z))
            case z if t.tpe.toString.endsWith(" => Unit") => if(z == "") None else Some("%s".format(z))
            case z => Some("return %s;\n".format(z))
          }
        }

        stats.mkString + expr.getOrElse("")
    }

    def buildSwitch(t:Tree, hasReturn:Boolean):String = t match {

        case x @ Match(selector, cases) => 

            val theSwitch = "switch(%s) {\n%s}".format(
                buildTree(selector), 
                cases.map(y => buildSwitch(y, hasReturn)).mkString)

            if(hasReturn)
                "function() {\n%s}()".format(theSwitch)
            else theSwitch


        case x @ CaseDef(path, guard, body) =>
            
            val part = path match {
                case Ident(n) if(n.toString == "_") => "default"
                case _ => "case %s".format(buildTree(path))
            }

            if(hasReturn) {
                "%s: return %s;\n".format(part, buildTree(body))
            } else {
                "%s: %s;break;\n".format(part, buildTree(body))
            }
    }

    def findRequiresFrom(tree:Tree):Set[String] = {
        
        val s = new collection.mutable.LinkedHashSet[String] 

        var currentFile:scala.tools.nsc.io.AbstractFile = null

        val thingsToIgnore = List("scalosure.script", "s2js.JsObject", "s2js.JsArray", "s2js.Html", "ClassManifest", "scala.runtime.AbstractFunction1",
          "scala.Product", "scala.ScalaObject", "java.lang", "scala.xml", "$default$", "browser")

        def buildName(s:Symbol):String = s.fullName.replace("scala", "scalosure")
          
        def traverse(t:Tree):Unit = t match {

            // check the body of a class
            case x @ Template(parents, self, body) => 

                currentFile = x.symbol.sourceFile

                parents.foreach {
                    y => if(!thingsToIgnore.exists(y.symbol.fullName.contains)) {
                        if(currentFile != y.symbol.sourceFile) {
                          s += buildName(y.symbol)
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
                  s += buildName(q.symbol.owner)
                }

            case x @ Select(New(tpe), _) => tpe match {

                case y @ Select(Select(_, _), _) => 
                  s += buildName(y.symbol)
                case y @ TypeTree() => 
                  if(currentFile != y.symbol.sourceFile) {
                    s += buildName(y.symbol)
                  }
                case y => 
                  if(currentFile != y.symbol.sourceFile) {
                    s += buildName(y.symbol)
                  }
            }

            case x @ Select(Select(_, _), _) if(!x.symbol.isPackage) =>

                if(!thingsToIgnore.exists(x.symbol.fullName.contains)) {
                  s += buildName(x.symbol.owner)
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
            "%s.%s = %s;\n".format(className, buildName(tree.symbol), rhs)
        } else {
            "%s.prototype.%s = %s;\n".format(className, buildName(tree.symbol), rhs)
        }
    }

    def trimNamespace(ns:String) = if(ns.endsWith(".package")) {
        ns.stripSuffix(".package")
    } else ns

    def translateName(s:Name):String = s.toString match {
        case "apply" => "$apply"
        case x => x
    }

    def scala2scalosure(s:Symbol):String = s.fullName.replace("scala", "scalosure")

    def buildName(s:Symbol):String = s.nameString match {
        case "+=" => "$plus$eq"
        case "apply" => "$apply"
        case x => x
    }

    def buildMethod(ts:DefDef, inPackageObject:Boolean=false):String = { 

        val ns = trimNamespace(ts.symbol.owner.fullName)
        val name = ts.symbol.nameString
        val args = ts.vparamss.flatten.map(_.symbol.nameString).mkString(",")

        val l = new ListBuffer[String]

        if(ts.symbol.owner.isModuleClass) {
            l += "%s.%s = function(%s) {\n".format(ns, buildName(ts.symbol), args)
        } else {
            l += "%s.prototype.%s = function(%s) {\n".format(ns, buildName(ts.symbol), args)
        }

        // every method gets a self refrence
        l += "var self = this;\n"

        val stats = ts.rhs match {
            case y @ Block(_, _) => buildBlock(y)
            case y => buildTree(y) match {
                case z if(ts.tpt.symbol.nameString == "Unit") => 
                    if(z == "") "" else "%s;\n".format(z)
                case z => "return %s;\n".format(z)
            }
        }
        
        l += stats

        l += "};\n"

        if(ts.symbol.annotations exists {_.toString == "s2js.ExportSymbol"}) {
            l += "goog.exportSymbol('%1$s', %1$s);\n".format(ns+"."+name)
        }

        l.mkString
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

      case x @ Apply(TypeApply(y @ Select(Select(_, n), _), _), args) if(n.toString == "Map") => args.map {
        buildObjectLiteral
      } mkString("{",",","}")

      case x @ Apply(TypeApply(Select(q, n), _), args) if (n.toString == "$minus$greater") => {

        // this should be a string
        val key = q.asInstanceOf[ApplyImplicitView].args.head.toString.replace("\"", "'")

        // process nested objects
        val values = args map buildObjectLiteral

        "%s:%s".format(key, values.mkString)
      }

      case x:ApplyToImplicitArgs => x.fun match {
        case y @ Apply(TypeApply(Select(n, _), _), args) if(n.toString == "scala.Array") => args map {
          buildObjectLiteral 
        } mkString("[", ",", "]")
        case y => y.getClass.toString
      }

      case x => buildTree(x)
    }
}

