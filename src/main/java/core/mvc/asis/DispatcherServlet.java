package core.mvc.asis;

import core.mvc.ControllerExecutor;
import core.mvc.ModelAndView;
import core.mvc.tobe.AnnotationHandlerMapping;
import core.mvc.tobe.HandlerExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "dispatcher", urlPatterns = "/", loadOnStartup = 1)
public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    private RequestMapping rm;
    private AnnotationHandlerMapping annotationHandlerMapping;

    @Override
    public void init() throws ServletException {
        rm = new RequestMapping();
        rm.initMapping();
        annotationHandlerMapping = new AnnotationHandlerMapping("core.mvc.asis", "next.controller");
        annotationHandlerMapping.initialize();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ModelAndView modelAndView = new ModelAndView();
        logger.debug("Method : {}, Request URI : {}", req.getMethod(), req.getRequestURI());

        ControllerExecutor controllerExecutor = new ControllerExecutor(rm, annotationHandlerMapping);
        Object executor = controllerExecutor.findExecutor(req);
        try {
            if (executor instanceof Controller) {
                modelAndView = new ModelAndView(((Controller) executor).execute(req, resp));
            }

            if (executor instanceof HandlerExecution) {
                modelAndView = ((HandlerExecution) executor).handle(req, resp);
            }

            this.viewRender(modelAndView, req, resp);
        } catch (Exception e) {
            throw new ServletException("Displatcher servlet throw Exception", e);
        }
    }

    private void viewRender(ModelAndView modelAndView, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        modelAndView.getView().render(modelAndView.getModel(), req, resp);
    }

}
