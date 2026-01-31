package tn.tn.elfatoora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
	                                       AuthenticationSuccessHandler successHandler) throws Exception {


		http
		  .authorizeRequests()
		    .antMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/videos/**", "/pdf/**",
		                 "/cgu/**", "/webjars/**", "/favicon/**", "/favicon.ico").permitAll()

		    // OIDC Mobile-ID endpoints (authorize/callback/logout)
		    .antMatchers("/sign-in/**").permitAll()

		    // Public auth pages
		    .antMatchers("/", "/auth/**").permitAll()

		    .antMatchers("/myspace/**", "/adhesion/**").hasRole("CLIENT")
		    .antMatchers("/bo/**").hasRole("AGENT_TTN")
			// UNIQUEMENT l'admin peut voir le Health Check
			.antMatchers("/actuator/**").hasRole("ADMIN")
				/**
				 * Accède à http://localhost:9090/actuator/health.
				 * * Le navigateur doit t'ouvrir une petite fenêtre (Pop-up)
				 * te demandant un Nom d'utilisateur et un Mot de passe.
 				 */
		    .anyRequest().authenticated()
		    
	        .and()
	            .formLogin()
	                .loginPage("/auth/login")
	                .loginProcessingUrl("/auth/login")
	                .successHandler(successHandler)
	                .failureUrl("/auth/login?error")
	                .permitAll()
	        .and()
	            .logout()
	                .logoutUrl("/logout")
	                .logoutSuccessUrl("/auth/login?logout")
	                .permitAll()
			.and()
				.httpBasic() // <---  pop-up sur /actuator
	        .and()
	        	.exceptionHandling()
	                	.accessDeniedPage("/error")
        	.and() // On ajoute un .and() pour lier les headers
	            .headers() // contentSecurityPolicy(csp -> csp
				    .contentSecurityPolicy( // contentSecurityPolicy(csp -> csp
					        "default-src 'self'; " +
					        "script-src 'self' 'unsafe-inline'; " + // Autorise vos onclick et scripts inline
					        "style-src 'self' 'unsafe-inline'; " +  // Autorise les styles Bootstrap inline
					        "img-src 'self' data:; " +              // Autorise les images locales ET les SVG Bootstrap (data:)
					        "font-src 'self';"                      // Garde nos polices locales sécurisées
				    	 )
		    .and(); // Fin de la section headers

	    return http.build();
	}


    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {

                Collection<? extends GrantedAuthority> auths = authentication.getAuthorities();
                boolean isAgent = auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENT_TTN"));
                boolean isClient = auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));

                if (isAgent) {
                    response.sendRedirect("/bo/dossiers");
                } else if (isClient) {
                    response.sendRedirect("/myspace");
                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
