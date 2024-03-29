package unfiltered.scalate

import com.buzzinate.lezhi.Config
import org.fusesource.scalate.{
  TemplateEngine, Binding, DefaultRenderContext, RenderContext}
import unfiltered.request.{Path,HttpRequest}
import unfiltered.response.{ResponseWriter}
import java.io.{File,Writer,OutputStreamWriter,PrintWriter}
import com.buzzinate.lezhi.util.Logging
import javax.servlet.http.HttpServletRequest

object Scalate extends Logging {

  /** Constructs a ResponseWriter for Scalate templates.
   *  Note that any parameter in the second, implicit set
   *  can be overriden by specifying an implicit value of the
   *  expected type in a pariticular scope. */
  def apply[A, B](request: HttpRequest[HttpServletRequest],
                  template: String,
                  attributes:(String,Any)*)
  ( implicit
    engine: TemplateEngine = defaultEngine,
    contextBuilder: ToRenderContext = defaultRenderContext,
    bindings: List[Binding] = Nil,
    additionalAttributes: Seq[(String, Any)] = Nil,
    userAttributes: Seq[(String, Any)] = Nil
  ) = new ResponseWriter {
    def write(writer: OutputStreamWriter) {
      val printWriter = new PrintWriter(writer)
      try {
        val scalateTemplate = engine.load(template, bindings)
        val context = contextBuilder(Path(request), printWriter, engine)
        (additionalAttributes ++ attributes) foreach {
          case (k,v) => context.attributes(k) = v
        }
        (userAttributes ++ attributes) foreach {
          case (k,v) => context.attributes(k) = v
        }
        context.attributes("request") = request.underlying
        engine.layout(scalateTemplate, context)
      } catch {
        case e if engine.isDevelopmentMode =>
          printWriter.println("Exception: " + e.getMessage)
          e.getStackTrace.foreach(printWriter.println)
        case e => throw e
      }
    }
  }

  /* Function to construct a RenderContext. */
  type ToRenderContext =
    (String, PrintWriter, TemplateEngine) => RenderContext

  private val defaultTemplateDirs = 
    new File("src/main/resources/templates") :: Nil
  private val defaultEngine = new TemplateEngine(defaultTemplateDirs)
  private val defaultRenderContext: ToRenderContext =
    (path, writer, engine) =>
      new DefaultRenderContext(path, engine, writer)
}