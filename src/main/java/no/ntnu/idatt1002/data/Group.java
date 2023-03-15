package no.ntnu.idatt1002.data;

import java.util.ArrayList;

/**
 * The Group class represents a group of members. It maintains a HashMap of Member objects
 * where the key is the member's user ID and the value is the Member object. Each group has a
 * group ID and group name.
 */
public class Group {
  ArrayList<User> group = new ArrayList<>(); //The HashMap of Member objects representing the group
  private final int groupId; //The ID of the group
  private String groupName; //The name of the group

  /**
   Constructs a new Group object with the specified group ID and group name.
   *
   * @param groupId the ID of the group
   * @param groupName the name of the group
   */
  public Group(int groupId, String groupName) {
    this.groupId = groupId;
    this.groupName = groupName;
  }

  /**
   Adds a member to the group.
   *
   * @param user the Member object to add to the group
   * @return true if the member was successfully added, false if the member is already in the group
   */
  public boolean addMember(User user) {
    if (memberExists(user.getId())){
      return false;
    } else {
      group.add(user);
      return true;
    }
  }

  /**
   * Removes a member from the group.
   *
   * @param user the Member object to remove from the group
   * @return true if the member was successfully removed, false if the member is not in the group
   */
  public boolean removeMember(User user) {
    if(group.contains(user)){
      group.remove(user);
      return true;
    } else {
      return false;
    }
  }

  public boolean memberExists(Long userId) {
    return group.stream().anyMatch(user -> user.getId() == userId);
  }

  /**
   * Returns the ID of the group.
   *
   * @return the ID of the group
   */
  public int getGroupId() {
    return groupId;
  }

  /**
   * Returns the name of the group.
   *
   * @return the name of the group
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Returns the HashMap of Member objects representing the group.
   *
   * @return the HashMap of Member objects representing the group
   */
  public ArrayList<User> getGroup() {
    return group;
  }

  /**
   * Sets the name of the group.
   *
   * @param groupName the new name of the group
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }
}