package io.github.ravibits.client;

import io.github.ravibits.client.trie.CommandTrie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Quickey {

  /**
   * 
   */
  private static final String QUICKEY_EL = "quickey-el";
  /**
   * 
   */
  private static final Quickey QUICKEY = new Quickey();
  private static final Map<Integer, String> CHARACTER_MAP = new HashMap<Integer, String>();
  private static final Map<Integer, String> SPECIAL_KEY_MAP = new HashMap<Integer, String>();
  private static final Map<String, String> SHIFT_KEY_MAP = new HashMap<String, String>();
  private static final List<String> INVALID_QUICKEY_TAGS = new ArrayList<String>();

  private static final int TIMEOUT_SEQUENCE_CONTINUATION = 1000;
  static {
    INVALID_QUICKEY_TAGS.add("input");
    INVALID_QUICKEY_TAGS.add("textarea");
    INVALID_QUICKEY_TAGS.add("select");

    SPECIAL_KEY_MAP.put(8, "backspace");
    SPECIAL_KEY_MAP.put(9, "tab");
    SPECIAL_KEY_MAP.put(13, "enter");
    SPECIAL_KEY_MAP.put(16, "shift");
    SPECIAL_KEY_MAP.put(17, "ctrl");
    SPECIAL_KEY_MAP.put(18, "alt");
    SPECIAL_KEY_MAP.put(20, "capslock");
    SPECIAL_KEY_MAP.put(27, "esc");
    SPECIAL_KEY_MAP.put(32, "space");
    SPECIAL_KEY_MAP.put(33, "pageup");
    SPECIAL_KEY_MAP.put(34, "pagedown");
    SPECIAL_KEY_MAP.put(35, "end");
    SPECIAL_KEY_MAP.put(36, "home");
    SPECIAL_KEY_MAP.put(37, "left");
    SPECIAL_KEY_MAP.put(38, "up");
    SPECIAL_KEY_MAP.put(39, "right");
    SPECIAL_KEY_MAP.put(40, "down");
    SPECIAL_KEY_MAP.put(45, "ins");
    SPECIAL_KEY_MAP.put(46, "del");
    SPECIAL_KEY_MAP.put(91, "meta");
    SPECIAL_KEY_MAP.put(93, "meta");
    SPECIAL_KEY_MAP.put(224, "meta");
    CHARACTER_MAP.put(106, "*");
    CHARACTER_MAP.put(107, "+");
    CHARACTER_MAP.put(109, "-");
    CHARACTER_MAP.put(110, ".");
    CHARACTER_MAP.put(111, "/");
    CHARACTER_MAP.put(59, ";");
    CHARACTER_MAP.put(186, ";");
    CHARACTER_MAP.put(187, "=");
    CHARACTER_MAP.put(188, ",");
    CHARACTER_MAP.put(189, "-");
    CHARACTER_MAP.put(190, ".");
    CHARACTER_MAP.put(191, "/");
    CHARACTER_MAP.put(192, "`");
    CHARACTER_MAP.put(219, "[");
    CHARACTER_MAP.put(220, "\\");
    CHARACTER_MAP.put(221, "]");
    CHARACTER_MAP.put(222, "\'");
    for (int i = 1; i < 13; i++) {
      CHARACTER_MAP.put(111 + i, "f" + i);
    }
    for (int i = 0; i < 10; i++) {
      CHARACTER_MAP.put(96 + i, "" + i);
    }

    SHIFT_KEY_MAP.put("`", "~");
    SHIFT_KEY_MAP.put("1", "!");
    SHIFT_KEY_MAP.put("2", "@");
    SHIFT_KEY_MAP.put("3", "#");
    SHIFT_KEY_MAP.put("4", "$");
    SHIFT_KEY_MAP.put("5", "%");
    SHIFT_KEY_MAP.put("6", "^");
    SHIFT_KEY_MAP.put("7", "&");
    SHIFT_KEY_MAP.put("8", "*");
    SHIFT_KEY_MAP.put("9", "(");
    SHIFT_KEY_MAP.put("0", ")");
    SHIFT_KEY_MAP.put("-", "_");
    SHIFT_KEY_MAP.put("=", "+");
    SHIFT_KEY_MAP.put("\\", "|");
    SHIFT_KEY_MAP.put("]", "}");
    SHIFT_KEY_MAP.put("[", "{");
    SHIFT_KEY_MAP.put("'", "\"");
    SHIFT_KEY_MAP.put(";", ":");
    SHIFT_KEY_MAP.put("/", "?");
    SHIFT_KEY_MAP.put(".", ">");
    SHIFT_KEY_MAP.put(",", "<");

  }

  private Timer timer = new Timer() {

    @Override
    public void run() {
      // when the timer elapses; if there is a end node with the current command, that needs to be
      // executed.
      if (lastEndTrieNode != null && lastEndTrieNode.isEnd()) {
        executeCommands(lastEndTrieNode);
        lastEndTrieNode = null;
      }
      currentCommand.clear();
    }
  };
  private List<String> currentCommand = new ArrayList<String>();
  private Map<CommandTrie, List<QuickeyHandler>> commandSequenceMap = new HashMap<CommandTrie, List<QuickeyHandler>>();
  private List<Long> unboundCommands = new ArrayList<Long>();
  // out of all the registered sequences, create a trie so that following if a registered
  // sequence exists or not can be managed real easy. or do we even need a trie? why not just list
  // of lists?
  private Map<Widget, CommandTrie> widgetRootCommandMap = new HashMap<Widget, CommandTrie>();

  private CommandTrie lastEndTrieNode = null;

  public static Quickey getInstance() {
    return QUICKEY;
  }

  private Quickey() {
    // do nothing. everything will be done on bind and unbind
  }

  public Long bind(String sequence, Command command) {
    return bind(sequence, command, null);
  }

  public Long bind(String sequence, Command command, QuickeyOptions options) {
    final QuickeyOptions finalOptions = options == null ? new QuickeyOptions() : options;
    if (!widgetRootCommandMap.containsKey(finalOptions.getWidget())) {
      widgetRootCommandMap.put(finalOptions.getWidget(), new CommandTrie());
      finalOptions.getWidget().addStyleName(QUICKEY_EL);
      finalOptions.getWidget().addDomHandler(new KeyDownHandler() {

        @Override
        public void onKeyDown(KeyDownEvent event) {
          handleKeyDownEvent(event);
        }
      }, KeyDownEvent.getType());
    }
    CommandTrie commandTrie = widgetRootCommandMap.get(finalOptions.getWidget()).add(
        registerSequence(sequence));
    if (!commandSequenceMap.containsKey(commandTrie)) {
      commandSequenceMap.put(commandTrie, new ArrayList<QuickeyHandler>());
    }
    long bindId = (long) (Random.nextDouble() * Long.MAX_VALUE);
    commandSequenceMap.get(commandTrie).add(new QuickeyHandler(bindId, command));

    return bindId;
  }

  public void unbind(Long id) {
    unboundCommands.add(id);
  }

  public void rebind(Long id) {
    unboundCommands.remove(id);
  }

  private void executeCommands(CommandTrie trie) {
    List<QuickeyHandler> commands = commandSequenceMap.get(trie);
    if (commands != null) {
      for (QuickeyHandler c : commands) {
        if (!unboundCommands.contains(c.getId())) {
          c.getCommand().execute();
        }
      }
    }
  }

  private String registerSequence(String sequence) {
    String[] strings = sequence.split(" ");
    String modifiedSequence = "";
    for (String s : strings) {
      modifiedSequence = modifiedSequence + " " + registerCommand(s);
    }
    return modifiedSequence.trim();
  }

  private String registerCommand(String command) {
    // capital letters should have shift
    // shift characters should have shift
    List<String> modifiers = new ArrayList<String>();
    String character;
    if (command.indexOf("-") != -1) {
      String[] parts = command.split("-");
      character = parts[parts.length - 1];
      for (String s : parts) {
        modifiers.add(s);
      }
      modifiers.remove(character);
    } else {
      character = command;
    }
    if (character.matches("[A-Z~!@#$%^&*()_+\\|}{\":?><]") && !modifiers.contains("shift")) {
      modifiers.add("shift");
    }
    return getModifierString(modifiers) + character;
  }

  private List<String> getModifiers(KeyDownEvent event) {
    List<String> modifiers = new ArrayList<String>();
    if (event.isAltKeyDown()) {
      modifiers.add("alt");
    }
    if (event.isControlKeyDown()) {
      modifiers.add("ctrl");
    }
    if (event.isShiftKeyDown()) {
      modifiers.add("shift");
    }
    if (event.isMetaKeyDown()) {
      modifiers.add("meta");
    }
    Collections.sort(modifiers);
    return modifiers;
  }

  private String getModifierString(List<String> modifiers) {
    String finalString = "";
    for (String s : modifiers) {
      finalString = finalString + s + "-";
    }
    return finalString;
  }

  private String getCharacterFromEvent(KeyDownEvent event) {
    int keyCode = event.getNativeKeyCode();
    String character = null;
    if (keyCode >= 48 && keyCode <= 90) {
      character = new Character((char) keyCode).toString();
      if (character.matches("[a-zA-z0-9]")) {
        if (!event.isShiftKeyDown()) {
          character = character.toLowerCase();
        }
      }
    }
    if (CHARACTER_MAP.containsKey(keyCode)) {
      character = CHARACTER_MAP.get(keyCode);
    } else if (!event.isAnyModifierKeyDown() && SPECIAL_KEY_MAP.containsKey(keyCode)) {
      character = SPECIAL_KEY_MAP.get(keyCode);
    }

    if (character != null && event.isShiftKeyDown() && SHIFT_KEY_MAP.containsKey(character)) {
      character = SHIFT_KEY_MAP.get(character);
    }
    return character;
  }

  /**
   * @param event
   */
  public void handleKeyDownEvent(final KeyDownEvent event) {
    timer.cancel();
    timer.schedule(TIMEOUT_SEQUENCE_CONTINUATION);
    if (!canThisEventBeHandled(event)) {
      return;
    }
    String character = getCharacterFromEvent(event);
    if (character != null) {
      String command = getModifierString(getModifiers(event)) + character;
      currentCommand.add(command);
      // if the currentCommand leads to a single leaf data structure; execute that command
      // and reset the currentCommand.
      // if the currentCommand doesn't lead to any data structure, reset the current command.
      // TODO refactor.
      CommandTrie endTrieNodeForCurrentCommand = widgetRootCommandMap.get(event.getSource())
          .getEndTrieNode(
              currentCommand);
      if (endTrieNodeForCurrentCommand == null) {
        // then make sure command(n-1) is not a command in itself and n is also not a command
        if (lastEndTrieNode != null && lastEndTrieNode.isEnd()) {
          executeCommands(lastEndTrieNode);
          event.preventDefault();
          currentCommand.clear();
          currentCommand.add(command);
          endTrieNodeForCurrentCommand = widgetRootCommandMap.get(event.getSource())
              .getEndTrieNode(currentCommand);
          if (endTrieNodeForCurrentCommand != null && endTrieNodeForCurrentCommand.isEnd()
              && endTrieNodeForCurrentCommand.getChildren().isEmpty()) {
            executeCommands(endTrieNodeForCurrentCommand);
            currentCommand.clear();
          }
          lastEndTrieNode = null;
        } else {
          currentCommand.clear();
          lastEndTrieNode = null;
        }
      } else if (endTrieNodeForCurrentCommand.isEnd()
          && endTrieNodeForCurrentCommand.getChildren().isEmpty()) {
        executeCommands(endTrieNodeForCurrentCommand);
        currentCommand.clear();
        event.preventDefault();
        lastEndTrieNode = null;
      } else {
        // there is more than one command leading from here registered; so carry on with it.
        lastEndTrieNode = endTrieNodeForCurrentCommand;
        event.preventDefault();
      }
    }
    event.stopPropagation();
  }

  /**
   * @param event
   * @return
   */
  private boolean canThisEventBeHandled(final KeyDownEvent event) {
    Element targetElement = Element.as(event.getNativeEvent().getEventTarget());

    if (Arrays.asList(targetElement.getClassName().split(" ")).contains(QUICKEY_EL)) {
      return true;
    }

    // if the event is from places where we dont want the shortcuts to be; return
    if (targetElement.getPropertyBoolean("isContentEditable")
        || INVALID_QUICKEY_TAGS.contains(targetElement.getTagName().toLowerCase())) {
      return false;
    }
    return true;
  }

  public static class QuickeyOptions {

    private Widget widget = RootPanel.get();

    private boolean isPropagatable = false;

    /**
     * @return the widget
     */
    public Widget getWidget() {
      return widget;
    }

    /**
     * @param widget
     *          the widget to set
     * @return
     */
    public QuickeyOptions setWidget(Widget widget) {
      this.widget = widget;
      return this;
    }

    /**
     * @return the isPropagatable
     */
    public boolean isPropagatable() {
      return isPropagatable;
    }

    /**
     * @param isPropagatable
     *          the isPropagatable to set
     */
    public QuickeyOptions setPropagatable(boolean isPropagatable) {
      this.isPropagatable = isPropagatable;
      return this;
    }

  }

}
