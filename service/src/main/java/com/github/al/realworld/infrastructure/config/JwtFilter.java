package com.github.al.realworld.infrastructure.config;

import com.github.al.realworld.application.service.JwtService;
import com.github.al.realworld.domain.model.User;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String[] tokenParts = authorization != null ? authorization.split(" ") : new String[0];

        AbstractAuthenticationToken authentication = anonymous();

        if (tokenParts.length > 1) {
            String subject = jwtService.getSubject(tokenParts[1]);
            Optional<User> userOptional = userRepository.findByUsername(subject);
            if (userOptional.isPresent()) {
                authentication = authenticated(userOptional.get());
            }
        }

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private AbstractAuthenticationToken authenticated(User user) {
        return new UsernamePasswordAuthenticationToken(
                Optional.of(user),
                null,
                Collections.emptyList()
        );
    }

    private AbstractAuthenticationToken anonymous() {
        return new AnonymousAuthenticationToken(
                "anonymous",
                Optional.empty(),
                Collections.singletonList(new SimpleGrantedAuthority("anonymous"))
        );
    }
}
