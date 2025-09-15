/*
package gr.aueb.cf.eduapp.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @PostMapping(value = "/test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String testMultipart(HttpServletRequest request) {
        return String.format(
                "Content-Type: %s%nMethod: %s%nPath: %s%n",
                request.getContentType(),
                request.getMethod(),
                request.getRequestURI()
        );
    }

    @PostMapping(value = "/test-simple")
    public String testSimple(@RequestParam String name) {
        return "Hello " + name;
    }
}*/
