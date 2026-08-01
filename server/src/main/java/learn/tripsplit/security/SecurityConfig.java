package learn.tripsplit.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
// Spring Boot does not turn method security on by itself, so the @PreAuthorize
// annotations on the admin-only endpoints were silently doing nothing.
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtConverter converter;

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String allowedOrigins;

    public SecurityConfig(JwtConverter converter) {
        this.converter = converter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.cors();

        http.authorizeRequests()
                // TODO add antMatchers here to configure access to specific API endpoints
                // allows unauthenticated users to access /authenticate and /register.
                .antMatchers("/api/auth/authenticate", "/api/auth/register").permitAll()
                // Liveness probe for Render's health check and the keep-alive pinger.
                .antMatchers(HttpMethod.GET, "/api/health").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // GroupController is mapped at /api/groups, so the old "/api/group/**"
                // pattern never matched and delete-group was open to any signed-in user.
                .antMatchers(HttpMethod.DELETE, "/api/user/**", "/api/groups/**").hasRole("ADMIN")
                // require authentication for any request...
                .anyRequest().authenticated()
                .and()
                .addFilter(new JwtRequestFilter(authenticationManager(), converter))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins.split(","))
                        .allowedMethods("*");
            }
        };
    }
}
