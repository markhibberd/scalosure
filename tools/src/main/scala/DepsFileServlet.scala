package s2js.tools

import java.io._

import javax.servlet._
import javax.servlet.http._

class DepsFileServlet extends HttpServlet with DepsFile {

  override def doGet(req:HttpServletRequest, res:HttpServletResponse) {

    res.setContentType("text/javascript");

    val base = getServletContext.getRealPath(getServletConfig.getInitParameter("base")).toString
    val path = getServletContext.getRealPath(getServletConfig.getInitParameter("path")).toString

    res.getWriter.print(jsDepedencies(path, base))
  }
}

