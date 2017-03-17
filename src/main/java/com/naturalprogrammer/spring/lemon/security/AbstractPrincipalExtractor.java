package com.naturalprogrammer.spring.lemon.security;

import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.naturalprogrammer.spring.lemon.domain.AbstractUserRepository;
import com.naturalprogrammer.spring.lemon.mail.MailSender;
import com.naturalprogrammer.spring.lemon.util.LemonUtil;

@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public abstract class AbstractPrincipalExtractor<U extends AbstractUser<U,?>>
		implements LemonPrincipalExtractor {

    private static final Log log = LogFactory.getLog(AbstractPrincipalExtractor.class);
    
    private PasswordEncoder passwordEncoder;
	private UserDetailsServiceImpl<U,?> userDetailsService;
    private AbstractUserRepository<U, ?> userRepository;
    private MailSender mailSender;
	private String provider = LemonPrincipalExtractor.DEFAULT;
    private String usernameColumnName = "email";
    
    @Autowired
    public void createAbstractPrincipalExtractor(PasswordEncoder passwordEncoder, UserDetailsServiceImpl<U, ?> userDetailsService,
			AbstractUserRepository<U, ?> userRepository, MailSender mailSender) {

    	this.passwordEncoder = passwordEncoder;
		this.userDetailsService = userDetailsService;
		this.userRepository = userRepository;
		this.mailSender = mailSender;
	}


    @Override
    public Object extractPrincipal(Map<String, Object> map) {
    	
    	AbstractUser<U,?> user;
    	
		try {
			
			// Return the user if it already exists
			user = userDetailsService
				.loadUserByUsername((String) map.get(usernameColumnName));
			
		} catch (UsernameNotFoundException e) {
			
			user = createUser(map);
		}
		
		map.put(AUTHORITIES, user.getAuthorities());		
		return user;		
    }
    
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	protected U createUser(Map<String, Object> map) {
		
		U user = newUser(map);
		
		user.setUsername((String) map.get(usernameColumnName));
		
		String password = LemonUtil.uid();
		user.setPassword(passwordEncoder.encode(password));
		
		userRepository.save(user);
		
		LemonUtil.afterCommit(() -> {
			
			try {
				
				mailSender.send(user.getEmail(), "Your new passsword", password);
				
			} catch (MessagingException e) {
				
				log.warn("Could not send mail after registering " + user.getEmail(), e);
			}
		});
		
		return user.decorate(user);
	}
	
	protected abstract U newUser(Map<String, Object> principalMap);

	@Override
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public String getUsernameColumnName() {
		return usernameColumnName;
	}

	public void setUsernameColumnName(String usernameColumnName) {
		this.usernameColumnName = usernameColumnName;
	}
}