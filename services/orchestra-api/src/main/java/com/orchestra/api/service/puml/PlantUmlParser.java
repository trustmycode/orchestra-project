package com.orchestra.api.service.puml;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PlantUmlParser {

    private static final Pattern TITLE_PATTERN = Pattern.compile("^title\\s+(.*)$");
    private static final Pattern PARTICIPANT_PATTERN = Pattern.compile("^(participant|actor|database|queue)\\s+\"?([^\"]+)\"?(\\s+as\\s+(\\w+))?");
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^\"?([^\"]+?)\"?\\s*->\\s*\"?([^\"]+?)\"?\\s*:\\s*(.*)$");
    private static final Pattern BLOCK_START_PATTERN = Pattern.compile("^(alt|opt|loop|par|group)\\s*(.*)$");
    private static final Pattern ELSE_PATTERN = Pattern.compile("^else\\s*(.*)$");
    private static final Pattern END_PATTERN = Pattern.compile("^end$");

    public PumlDocument parse(String content) {
        String[] lines = content.split("\\r?\\n");
        String title = null;
        List<PumlParticipant> participants = new ArrayList<>();
        List<PumlElement> elements = new ArrayList<>();
        
        Stack<PumlBlock> blockStack = new Stack<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("'") || line.startsWith("@startuml") || line.startsWith("@enduml")) {
                continue;
            }

            // Title
            Matcher titleMatcher = TITLE_PATTERN.matcher(line);
            if (titleMatcher.find()) {
                title = titleMatcher.group(1);
                continue;
            }

            // Participant
            Matcher partMatcher = PARTICIPANT_PATTERN.matcher(line);
            if (partMatcher.find()) {
                String name = partMatcher.group(2);
                String alias = partMatcher.group(4);
                participants.add(new PumlParticipant(name, alias != null ? alias : name, partMatcher.group(1)));
                continue;
            }

            // Block Start
            Matcher blockStartMatcher = BLOCK_START_PATTERN.matcher(line);
            if (blockStartMatcher.find()) {
                String typeStr = blockStartMatcher.group(1);
                String condition = blockStartMatcher.group(2);
                PumlBlockType type = PumlBlockType.valueOf(typeStr.toUpperCase());
                if ("GROUP".equals(typeStr.toUpperCase())) type = PumlBlockType.OPT; // Simplify group to opt for now

                PumlBlock block = new PumlBlock(type, condition, new ArrayList<>(), new ArrayList<>());
                if (blockStack.isEmpty()) {
                    elements.add(block);
                } else {
                    addToCurrentContext(blockStack.peek(), block);
                }
                blockStack.push(block);
                continue;
            }

            // Else
            Matcher elseMatcher = ELSE_PATTERN.matcher(line);
            if (elseMatcher.find()) {
                if (!blockStack.isEmpty()) {
                    PumlBlock current = blockStack.peek();
                    current.addElse(new PumlBlock.ElseBlock(elseMatcher.group(1), new ArrayList<>()));
                }
                continue;
            }

            // End
            if (END_PATTERN.matcher(line).matches()) {
                if (!blockStack.isEmpty()) {
                    blockStack.pop();
                }
                continue;
            }

            // Message
            Matcher msgMatcher = MESSAGE_PATTERN.matcher(line);
            if (msgMatcher.find()) {
                PumlMessage msg = new PumlMessage(msgMatcher.group(1), msgMatcher.group(2), msgMatcher.group(3));
                if (blockStack.isEmpty()) {
                    elements.add(msg);
                } else {
                    addToCurrentContext(blockStack.peek(), msg);
                }
                continue;
            }
        }

        return new PumlDocument(title, participants, elements);
    }

    private void addToCurrentContext(PumlBlock currentBlock, PumlElement element) {
        if (currentBlock.elseBlocks().isEmpty()) {
            currentBlock.children().add(element);
        } else {
            // Add to the last else block
            currentBlock.elseBlocks().get(currentBlock.elseBlocks().size() - 1).children().add(element);
        }
    }
}

