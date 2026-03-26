package com.orchestra.api.service.puml;

import java.util.List;

public record PumlDocument(
    String title,
    List<PumlParticipant> participants,
    List<PumlElement> elements
) {}

record PumlParticipant(String name, String alias, String type) {}

interface PumlElement {}

record PumlMessage(String source, String target, String text) implements PumlElement {}

record PumlBlock(
    PumlBlockType type,
    String condition,
    List<PumlElement> children,
    List<ElseBlock> elseBlocks
) implements PumlElement {
    
    public void addElse(ElseBlock elseBlock) {
        this.elseBlocks.add(elseBlock);
    }

    public record ElseBlock(String condition, List<PumlElement> children) {}
}

enum PumlBlockType {
    ALT, OPT, LOOP, PAR
}

