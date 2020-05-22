package com.naturalprogrammer.spring.lemondemo.domain;

import com.fasterxml.jackson.annotation.JsonView;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemonreactive.domain.AbstractMongoUser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter @Setter
@TypeAlias("User")
@Document(collection = "usr")
public class User extends AbstractMongoUser<ObjectId> {

    public static final int NAME_MIN = 1;
    public static final int NAME_MAX = 50;

    @Getter @Setter @ToString
	public static class Tag implements Serializable {
		
		private static final long serialVersionUID = -2129078111926834670L;
		private String name;
	}

	@JsonView(UserUtils.SignupInput.class)
	@NotBlank(message = "{blank.name}", groups = {UserUtils.SignUpValidation.class, UserUtils.UpdateValidation.class})
    @Size(min=NAME_MIN, max=NAME_MAX, groups = {UserUtils.SignUpValidation.class, UserUtils.UpdateValidation.class})
    private String name;
	
	@Override
	public Tag toTag() {
		
		Tag tag = new Tag();
		tag.setName(name);
		return tag;
	}
}
