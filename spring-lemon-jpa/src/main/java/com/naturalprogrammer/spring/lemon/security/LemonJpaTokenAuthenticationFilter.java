package com.naturalprogrammer.spring.lemon.security;

import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commonsweb.security.LemonCommonsWebTokenAuthenticationFilter;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.Serializable;

public class LemonJpaTokenAuthenticationFilter<U extends AbstractUser<ID>, ID extends Serializable>
	extends LemonCommonsWebTokenAuthenticationFilter {

    private static final Log log = LogFactory.getLog(LemonJpaTokenAuthenticationFilter.class);

    private LemonUserDetailsService<U, ID> userDetailsService;
	
	public LemonJpaTokenAuthenticationFilter(BlueTokenService blueTokenService,
			LemonUserDetailsService<U, ID> userDetailsService) {
		
		super(blueTokenService);
		this.userDetailsService = userDetailsService;
		
		log.info("Created");		
	}

	@Override
	protected UserDto fetchUserDto(JWTClaimsSet claims) {
		
        String username = claims.getSubject();
        U user = userDetailsService.findUserByUsername(username)
        		.orElseThrow(() -> new UsernameNotFoundException(username));

        log.debug("User found ...");

        LemonUtils.ensureCredentialsUpToDate(claims, user);
        UserDto userDto = user.toUserDto();
        userDto.setPassword(null);
        
        return userDto;
	}
}
