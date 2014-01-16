/**
 * 
 */
package io.github.ravibits.client.trie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ravi Madabhushi
 * 
 */
public class CommandTrie {

  private Map<String, CommandTrie> children = new HashMap<String, CommandTrie>();

  private boolean isEnd;

  private String command;

  /**
   * @return the children
   */
  public Map<String, CommandTrie> getChildren() {
    return children;
  }

  /**
   * @return the isEnd
   */
  public boolean isEnd() {
    return isEnd;
  }

  /**
   * @param isEnd
   *          the isEnd to set
   */
  public void setEnd(boolean isEnd) {
    this.isEnd = isEnd;
  }

  /**
   * @return the command
   */
  public String getCommand() {
    return command;
  }

  /**
   * @param command
   *          the command to set
   */
  public void setCommand(String command) {
    this.command = command;
  }

  public CommandTrie add(String command) {
    if (command == null || command.trim().isEmpty()) {
      return this;
    }
    String[] sequences = command.split(" ");
    CommandTrie currentTrieNode = this;
    for (String s : sequences) {
      if (currentTrieNode.getChildren().containsKey(s)) {
        currentTrieNode = currentTrieNode.getChildren().get(s);
      } else {
        CommandTrie newCommandTrie = new CommandTrie();
        newCommandTrie.setCommand(s);
        currentTrieNode.getChildren().put(s, newCommandTrie);
        currentTrieNode = newCommandTrie;
      }
    }
    currentTrieNode.setEnd(true);
    return currentTrieNode;
  }

  public CommandTrie getEndTrieNode(List<String> currentCommand) {
    if (currentCommand == null || currentCommand.isEmpty()) {
      return null;
    }
    CommandTrie currentNode = this;
    for (String s : currentCommand) {
      if (currentNode.getChildren().containsKey(s)) {
        currentNode = currentNode.getChildren().get(s);
      } else {
        return null;
      }
    }
    return currentNode;
  }

}
