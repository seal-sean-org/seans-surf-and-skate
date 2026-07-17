package io.sealsecurity.demo.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;

/**
 * Storefront for "Sean's Surf & Skate Co."
 *
 * The newsletter / "Deal Alerts" sign-up runs the submitted name through
 * SnakeYAML's Yaml.load(). SnakeYAML 1.33 is vulnerable to CVE-2022-1471
 * (arbitrary object instantiation on untrusted input) — this is the sink
 * Seal Security remediates with a backported sealed patch (1.33+sp1).
 */
@RestController
public class HelloController {

    private static final String CONFIRMATION_MARKER = "<!--CONFIRMATION-->";

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return renderStore(null);
    }

    @PostMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String subscribe(@RequestParam("name") String name) {
        // Vulnerable sink: untrusted input parsed by SnakeYAML (CVE-2022-1471).
        Yaml yaml = new Yaml();
        Object parsed = yaml.load(name);
        String displayName = String.valueOf(parsed);

        String banner = "<div class=\"confirm\">🤙 You're on the list, "
            + escapeHtml(displayName)
            + "! Check your inbox — your <b>25% welcome code</b> is on the way.</div>";
        return renderStore(banner);
    }

    private String renderStore(String confirmationBanner) {
        String page = loadTemplate();
        String banner = confirmationBanner == null ? "" : confirmationBanner;
        return page.replace(CONFIRMATION_MARKER, banner);
    }

    private String loadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("store.html");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "<html><body><h1>Sean's Surf &amp; Skate Co.</h1>"
                + "<p>Storefront temporarily unavailable.</p></body></html>";
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
