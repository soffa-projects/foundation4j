package dev.soffa.foundation.test;

import org.awaitility.Awaitility;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TestUtil {

    public static void awaitUntil(int seconds, Supplier<Boolean> tester) {
        Awaitility.await().atMost(seconds, TimeUnit.SECONDS).until(tester::get);
    }

    public static void setAuthenticationContext(dev.soffa.foundation.model.Authentication authentication, String... permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        Authentication auth = new UsernamePasswordAuthenticationToken(authentication, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

}
