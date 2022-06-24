package dev.soffa.foundation.extra.notifications.slack;

import lombok.Data;

import java.util.List;

@Data
public class Block {
    private String type;
    private Object text;
    private List<Block> elements;

    public Block(String type, Object text) {
        this.type = type;
        this.text = text;
    }

    public Block(String type, List<Block> elements) {
        this.type = type;
        this.elements = elements;
    }

    public static Block section(Block text) {
        return new Block("section", text);
    }

    public static Block markdown(String text) {
        return new Block("mrkdwn", text);
    }

}
