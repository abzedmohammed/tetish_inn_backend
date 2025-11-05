package tetish_inn_backend.tetish_inn;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class indexController {
    @GetMapping("")
    public String index() {
        return "These are APIs for TETISH INN";
    }

    @GetMapping("/about")
    public String about() {
        return "This app has been developed by Abzedizo Tetman.... jamaa mnoma";
    }
}