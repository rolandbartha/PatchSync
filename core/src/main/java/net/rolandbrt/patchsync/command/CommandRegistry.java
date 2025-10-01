package net.rolandbrt.patchsync.command;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public void register(String name, Command command) {
        commands.put(name.toLowerCase(), command);
        log.info("Registered command: {}", name);
    }

    public void unregister(String name) {
        commands.remove(name);
        log.info("Unregistered command: {}", name);
    }

    public void unregisterAll() {
        commands.clear();
        log.info("Unregistered all commands");
    }

    public void execute(String line) {
        // Trim whitespace and return if empty
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        // Regex: match quoted text OR unquoted words
        // "([^"]*)" matches "..." groups
        // (\\S+) matches normal words (no whitespace)
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(line);

        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1)); // Quoted part (without quotes)
            } else {
                tokens.add(matcher.group(2)); // Regular word
            }
        }

        if (tokens.isEmpty()) {
            return;
        }

        // First token = command, rest = args
        String commandName = tokens.get(0).toLowerCase();
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);

        Command cmd = commands.get(commandName);
        if (cmd != null) {
            cmd.execute(args);
        } else {
            log.info("Unknown command. Type 'help'.");
        }
    }
}