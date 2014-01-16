/**
 * 
 */
package io.github.ravibits.client;

import com.google.gwt.user.client.Command;

/**
 * @author Ravi Madabhushi
 * 
 */
public class QuickeyHandler {

  private Long id;

  private Command command;

  /**
   * @param id
   * @param command
   */
  public QuickeyHandler(Long id, Command command) {
    super();
    this.id = id;
    this.command = command;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the command
   */
  public Command getCommand() {
    return command;
  }

  /**
   * @param command
   *          the command to set
   */
  public void setCommand(Command command) {
    this.command = command;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "QuickeyHandler [id=" + id + ", command=" + command + "]";
  }

}
