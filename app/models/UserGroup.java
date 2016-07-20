package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import controllers.UserController;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.util.List;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 13/06/16.
 */
@Entity
@JsonPropertyOrder({ JsonKeys.GROUP_ID}) //ensure that groupID is the first element in json.
public class UserGroup extends Model {

	@Id
	@GeneratedValue
	@Column(name = JsonKeys.GROUP_ID)
	@JsonProperty(JsonKeys.GROUP_ID)
	private Long id;
	@Constraints.Required
    @JsonProperty(JsonKeys.GROUP_NAME)
    private String name;

    //TODO: Delete this attribute
    @Constraints.Required
    @JsonProperty(JsonKeys.GROUP_DESCRIPTION)
    private String description;

	@OneToMany(mappedBy = "group")
    @JsonIgnore	// to prevent endless recursion.
	private List<User> users;

	public static Model.Finder<Long, UserGroup> find = new Model.Finder<Long, UserGroup>(UserGroup.class);

	public UserGroup(String name, String description, List<User> users) {
		super();
		this.name = name;
		this.description = description;
		this.users = users;
	}

	public UserGroup(UserGroup requestGroup) {
		super();
		this.name=requestGroup.getName();
		this.description=requestGroup.getDescription();
		this.users=requestGroup.getUsers();
		for(User u:users){
//            System.out.println(">> updating user: "+u);
			u.setGroup(this);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<User> getUsers() {
		return users;
	}

	/**
	 * Replaces the current users with the given users.
	 * @param users
     */
	public void setUsers(List<User> users) {
		this.users = users;
		for(User u: users){
			u.setGroup(this);
		}
	}

	/**
	 * Adds one user to this group, updates the user's group as well.
	 * @param user
     */
	public void addUser(User user) {
		if (!users.contains(user)) {
			users.add(user);
			user.setGroup(this);
			this.save();
		}
	}

	@Override
	public String toString() {
		return "UserGroup [id=" + id + ", name=" + name + ", description="
				+ description+"]";
	}

    /**
     * Removes a specific user from the users of this group.
     * @param user
     */
    public void removeUser(User user) {
        if(users.contains(user)){
            users.remove(user);
            this.update();
        }
    }
}
