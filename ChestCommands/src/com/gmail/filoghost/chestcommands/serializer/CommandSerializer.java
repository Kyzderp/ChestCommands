package com.gmail.filoghost.chestcommands.serializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.BroadcastIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.ConsoleIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.GiveIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.GiveMoneyIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.OpIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.OpenIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.PlayerIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.ServerIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.SoundIconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.TellIconCommand;
import com.gmail.filoghost.chestcommands.util.ErrorLogger;
import com.google.common.collect.Lists;

public class CommandSerializer {

	private static Map<Pattern, Class<? extends IconCommand>> commandTypesMap = new HashMap<Pattern, Class<? extends IconCommand>>();
	static {
		commandTypesMap.put(commandPattern("console:"), ConsoleIconCommand.class);
		commandTypesMap.put(commandPattern("op:"), OpIconCommand.class);
		commandTypesMap.put(commandPattern("open:"), OpenIconCommand.class);
		commandTypesMap.put(commandPattern("server:?"), ServerIconCommand.class); // The colon is optional.
		commandTypesMap.put(commandPattern("tell:"), TellIconCommand.class);
		commandTypesMap.put(commandPattern("broadcast:"), BroadcastIconCommand.class);
		commandTypesMap.put(commandPattern("give:"), GiveIconCommand.class);
		commandTypesMap.put(commandPattern("give-?money:"), GiveMoneyIconCommand.class);
		commandTypesMap.put(commandPattern("sound:"), SoundIconCommand.class);
	}
	
	private static Pattern commandPattern(String regex) {
		return Pattern.compile("^(?i)" + regex); // Case insensitive and only at the beginning.
	}
	
	public static void checkClassConstructors(ErrorLogger errorLogger) {
		for (Class<? extends IconCommand> clazz : commandTypesMap.values()) {
			try {
				clazz.getDeclaredConstructor(String.class).newInstance("");
			} catch (Exception ex) {
				String className = clazz.getName().replace("Command", "");
				className = className.substring(className.lastIndexOf('.') + 1, className.length());
				errorLogger.addError("Unable to register the \"" + className + "\" command type(" + ex.getClass().getName() + "), please inform the developer (filoghost). The plugin will still work, but all the \"" + className + "\" commands will be treated as normal commands.");
			}
		}
	}
	
	public static List<IconCommand> readCommands(String input) {
		
		if (input.contains(";")) {
			
			String[] split = input.split(";");
			List<IconCommand> iconCommands = Lists.newArrayList();
			
			for (String command : split) {
				String trim = command.trim();
				
				if (trim.length() > 0) {
					iconCommands.add(matchCommand(trim));
				}
			}
			
			return iconCommands;
			
		} else {
			
			return Arrays.asList(matchCommand(input));
		}
	}
	
	public static IconCommand matchCommand(String input) {
		
		for (Entry<Pattern, Class<? extends IconCommand>> entry : commandTypesMap.entrySet()) {
			Matcher matcher = entry.getKey().matcher(input);
			if (matcher.find()) {
				
				String cleanedCommand = matcher.replaceFirst("");
				
				try {
					return entry.getValue().getDeclaredConstructor(String.class).newInstance(cleanedCommand);
				} catch (Exception e) {
					// Checked at startup.
				}
			}
		}
		
		return new PlayerIconCommand(input); // Normal command, no match found.
	}
	
}