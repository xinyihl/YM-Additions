package com.xinyihl.ymadditions.client.gui;

import com.xinyihl.ymadditions.client.api.IText;

public class TestString implements IText {
    private String text;
    public TestString(String text) {
        this.text = text;
    }
    @Override
    public String getText() {
        return this.text;
    }
}
