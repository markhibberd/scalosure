package s2js.tools

import java.io._

import javax.servlet._
import javax.servlet.http._

class DepsFileServlet extends HttpServlet with DepsFile {

  def pathFromParam(name:String) = getServletContext.getRealPath(
    getServletConfig.getInitParameter(name))

  override def doGet(req:HttpServletRequest, res:HttpServletResponse) {

    res.setContentType("text/javascript");

    res.getWriter.print(jsDepedencies(pathFromParam("path"), pathFromParam("base")))
  }
}

