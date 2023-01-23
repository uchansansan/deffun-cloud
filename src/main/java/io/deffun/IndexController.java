package io.deffun;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/api")
@Secured(SecurityRule.IS_ANONYMOUS)
public class IndexController {
    @Get
    public String list() {
        return "[ { \"name\" : \"some\" } ]";
    }
}
