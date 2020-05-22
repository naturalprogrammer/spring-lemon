package com.naturalprogrammer.spring.lemondemo.entities;

import com.fasterxml.jackson.annotation.JsonView;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name="usr")
@Getter @Setter @NoArgsConstructor
public class User extends AbstractUser<Long> {

    private static final long serialVersionUID = 2716710947175132319L;

    public static final int NAME_MIN = 1;
    public static final int NAME_MAX = 50;

    @Getter @Setter @ToString
	public static class Tag implements Serializable {
		
		private static final long serialVersionUID = -2129078111926834670L;
		private String name;
	}

	public User(String email, String password, String name) {
		this.email = email;
		this.password = password;
		this.name = name;
	}

	@JsonView(UserUtils.SignupInput.class)
	@NotBlank(message = "{blank.name}", groups = {UserUtils.SignUpValidation.class, UserUtils.UpdateValidation.class})
    @Size(min=NAME_MIN, max=NAME_MAX, groups = {UserUtils.SignUpValidation.class, UserUtils.UpdateValidation.class})
    @Column(nullable = false, length = NAME_MAX) // Note: don't use JPA annotations on getter: https://github.com/naturalprogrammer/spring-lemon/issues/9
    private String name;
	
	@Override
	public Tag toTag() {
		
		Tag tag = new Tag();
		tag.setName(name);
		return tag;
	}
}